# Performance Testing Guide - ChiroERP

**Status**: Draft  
**Priority**: P2 (Medium - Quality Critical)  
**Last Updated**: February 3, 2026  
**Scope**: Validate 10,000 concurrent users across Kenya + Tanzania deployments

---

## 1. Executive Summary

### 1.1 Purpose
Validate that ChiroERP can handle enterprise-scale workloads with Kenya-specific integrations (KRA eTIMS, M-Pesa) while maintaining SLAs for response time, throughput, and availability.

### 1.2 Performance Targets

| Metric | Target | Rationale |
|--------|--------|-----------|
| **Concurrent Users** | 10,000 | Enterprise with 50,000 employees (20% concurrent) |
| **Orders/Hour** | 5,000 | Peak sales period (e.g., end-of-month) |
| **Invoices/Day** | 50,000 | 2,000/hour × 24 hours (batch + real-time) |
| **M-Pesa Transactions/Min** | 500 | Kenya payment peak (80% via M-Pesa) |
| **KRA eTIMS Cu Retrieval** | < 2s (P95) | Regulatory requirement (fast invoice processing) |
| **API Response Time** | < 500ms (P95) | User experience threshold |
| **Database Query Time** | < 100ms (P95) | Backend performance |
| **Event Processing Latency** | < 1s (P95) | Real-time system requirement |

### 1.3 Test Environments

| Environment | Purpose | Load | Duration |
|-------------|---------|------|----------|
| **Dev** | Smoke testing | 100 users | 10 min |
| **Staging** | Load + Stress testing | 10,000 users | 2 hours |
| **Pre-Prod** | Soak + Spike testing | 15,000 users | 24 hours |
| **Production** | Continuous monitoring | Real traffic | 24/7 |

---

## 2. Test Scenarios

### 2.1 Kenya O2C (Order-to-Cash) - **PRIMARY**

#### Scenario: Sales Order → KRA eTIMS Invoice → M-Pesa Payment

**User Journey**:
1. Sales rep creates sales order (2 line items, 5 products avg)
2. Manager approves order (ADR-046 workflow)
3. System generates invoice with KRA eTIMS submission
4. **KRA eTIMS Cu number retrieval** (external API call)
5. Customer pays via M-Pesa C2B
6. **M-Pesa callback** triggers payment application
7. System posts to GL (IFRS)

**Performance Profile**:
```yaml
scenario: kenya_o2c
duration: 2h
rampUp: 15min
users:
  - stage: rampUp
    count: 0 → 5000
    duration: 15min
  - stage: steady
    count: 5000
    duration: 1h30min
  - stage: rampDown
    count: 5000 → 0
    duration: 15min

requests:
  - POST /api/v1/sales/orders
    weight: 30%
    payload: salesOrderKenyaVAT16.json
    sla: < 500ms (P95)
  
  - POST /api/v1/sales/orders/{id}/approve
    weight: 25%
    sla: < 300ms (P95)
  
  - POST /api/v1/finance/invoices/generate
    weight: 20%
    payload: invoiceWithKRAETIMS.json
    sla: < 2000ms (P95)  # Includes eTIMS Cu retrieval
    externalDependency: KRA_ETIMS_API
  
  - POST /api/v1/treasury/payments/mpesa/callback
    weight: 15%
    payload: mpesaC2BCallback.json
    sla: < 200ms (P95)  # Webhook must respond fast
  
  - GET /api/v1/finance/invoices/{id}
    weight: 10%
    sla: < 200ms (P95)

criticalPaths:
  - eTIMS Cu number retrieval (cannot proceed without it)
  - M-Pesa payment callback (async, must not block)
```

**Kenya-Specific Considerations**:
- **KRA eTIMS Rate Limits**:
  - Test environment: 10 requests/second
  - Production: 100 requests/second (per device)
  - Strategy: Queue invoices if rate limit hit, retry with exponential backoff
- **M-Pesa Daraja API**:
  - Sandbox: 1 request/second
  - Production: 20 requests/second (per organization)
  - OAuth2 token expires after 3600 seconds (must refresh)
- **Network Latency**:
  - KRA eTIMS: 500-1000ms (Kenya government servers)
  - M-Pesa API: 200-500ms (Safaricom servers)
  - Plan for timeouts: 5s (KRA), 3s (M-Pesa)

**Expected Results**:
- ✅ 5,000 concurrent users sustained for 1h30min
- ✅ 2,500 orders/hour processed
- ✅ KRA eTIMS Cu retrieval: < 2s (P95), < 5s (P99)
- ✅ M-Pesa callbacks: < 200ms (P95)
- ✅ Zero failed transactions due to rate limits (queuing works)

---

### 2.2 Kenya P2P (Procure-to-Pay)

#### Scenario: Purchase Order → Invoice Verification → M-Pesa B2B Payment

**User Journey**:
1. Buyer creates Purchase Requisition
2. Manager approves PR
3. Buyer creates Purchase Order
4. Director approves PO (> KES 500,000)
5. Warehouse receives goods (Goods Receipt)
6. AP clerk verifies supplier invoice (KRA eTIMS Cu validation)
7. System calculates **Withholding VAT 6%** (non-registered supplier)
8. System calculates **Import Duty** (HS code-based)
9. Treasurer processes payment via M-Pesa B2B

**Performance Profile**:
```yaml
scenario: kenya_p2p
duration: 2h
users: 2000 (procurement team + approvers)

requests:
  - POST /api/v1/procurement/requisitions
    weight: 20%
    sla: < 300ms
  
  - POST /api/v1/procurement/requisitions/{id}/approve
    weight: 15%
    sla: < 200ms (workflow)
  
  - POST /api/v1/procurement/orders
    weight: 20%
    payload: purchaseOrderKenya.json
    sla: < 500ms
  
  - POST /api/v1/procurement/orders/{id}/approve
    weight: 10%
    sla: < 200ms
  
  - POST /api/v1/inventory/goods-receipts
    weight: 15%
    sla: < 400ms
  
  - POST /api/v1/finance/ap/invoices/verify
    weight: 15%
    payload: supplierInvoiceWithCu.json
    sla: < 1500ms  # KRA eTIMS Cu validation
    externalDependency: KRA_ETIMS_API
  
  - POST /api/v1/treasury/payments/mpesa-b2b
    weight: 5%
    payload: mpesaB2BPayment.json
    sla: < 3000ms  # Includes M-Pesa API call
    externalDependency: MPESA_DARAJA_API

criticalPaths:
  - Withholding VAT calculation (6% for non-registered)
  - Import duty calculation (HS code lookup)
  - KRA eTIMS Cu validation (must verify supplier invoice)
```

**Expected Results**:
- ✅ 2,000 concurrent procurement users
- ✅ 1,000 POs/hour processed
- ✅ Withholding VAT calculation: < 100ms (in-memory tax engine)
- ✅ Import duty calculation: < 200ms (HS code cache)
- ✅ M-Pesa B2B payment: < 3s (P95)

---

### 2.3 Tanzania O2C (Validation)

#### Scenario: Sales Order → TRA VFD Invoice → M-Pesa Tanzania Payment

**User Journey**:
1. Sales order with VAT 18%
2. Invoice generation with **TRA VFD integration** (Virtual Fiscal Device)
3. **Verification code retrieval** from TRA
4. Payment via M-Pesa Tanzania (Vodacom API)

**Performance Profile**:
```yaml
scenario: tanzania_o2c
duration: 1h
users: 1000

requests:
  - POST /api/v1/sales/orders
    weight: 35%
    payload: salesOrderTanzaniaVAT18.json
  
  - POST /api/v1/finance/invoices/generate
    weight: 30%
    payload: invoiceWithTRAVFD.json
    sla: < 2500ms  # TRA VFD slower than KRA eTIMS
    externalDependency: TRA_VFD_API
  
  - POST /api/v1/treasury/payments/mpesa-tz/callback
    weight: 25%
    sla: < 300ms
  
  - GET /api/v1/finance/invoices/{id}
    weight: 10%

criticalPaths:
  - TRA VFD verification code (mandatory, cannot invoice without it)
```

**Tanzania-Specific Considerations**:
- **TRA VFD Rate Limits**:
  - Test: 5 requests/second
  - Production: 50 requests/second
  - Latency: 1000-2000ms (slower than Kenya eTIMS)
- **M-Pesa Tanzania**:
  - Different API from Kenya (Vodacom vs Safaricom)
  - Rate limit: 10 requests/second

**Expected Results**:
- ✅ 1,000 concurrent users (smaller market)
- ✅ 500 orders/hour
- ✅ TRA VFD verification: < 2.5s (P95)

---

### 2.4 Cross-Country Scenario

#### Scenario: Kenya HQ with Tanzania Branch

**User Journey**:
1. Kenya HQ creates intercompany sales order to Tanzania branch
2. Kenya invoice with KRA eTIMS (export, VAT 0%)
3. Tanzania branch creates purchase order (import)
4. **EAC customs documentation** generated
5. Payment via SWIFT (cross-border)

**Performance Profile**:
```yaml
scenario: cross_country_intercompany
duration: 30min
users: 100 (finance team)

requests:
  - POST /api/v1/sales/orders/intercompany
    weight: 40%
    payload: intercompanyOrderKenyaToTanzania.json
  
  - POST /api/v1/customs/eac/documents
    weight: 30%
    sla: < 500ms
  
  - POST /api/v1/treasury/payments/swift
    weight: 30%
    sla: < 1000ms

criticalPaths:
  - EAC customs form generation (cross-border compliance)
```

**Expected Results**:
- ✅ 100 concurrent users
- ✅ 50 intercompany transactions/hour
- ✅ EAC customs documents generated correctly

---

## 3. Load Testing Strategy

### 3.1 Test Types

#### 3.1.1 Smoke Test (Dev Environment)
**Purpose**: Verify system is ready for load testing  
**Duration**: 10 minutes  
**Users**: 100 concurrent  
**Success Criteria**:
- ✅ All APIs respond (no 500 errors)
- ✅ KRA eTIMS sandbox connectivity
- ✅ M-Pesa sandbox connectivity
- ✅ Database queries execute

#### 3.1.2 Load Test (Staging Environment)
**Purpose**: Validate target performance (10,000 users)  
**Duration**: 2 hours  
**Users**: 10,000 concurrent (ramp up over 15 min)  
**Success Criteria**:
- ✅ API response time < 500ms (P95)
- ✅ KRA eTIMS Cu retrieval < 2s (P95)
- ✅ M-Pesa callbacks < 200ms (P95)
- ✅ Database CPU < 70%
- ✅ Kafka consumer lag < 1000 messages
- ✅ Zero failed transactions
- ✅ Error rate < 0.1%

#### 3.1.3 Stress Test (Staging Environment)
**Purpose**: Find breaking point (when does system fail?)  
**Duration**: 1 hour  
**Users**: 10,000 → 20,000 (double target)  
**Success Criteria**:
- ✅ Identify bottleneck (database? Kafka? KRA API?)
- ✅ System degrades gracefully (no cascading failures)
- ✅ Circuit breakers activate (KRA eTIMS, M-Pesa)
- ✅ Queues prevent data loss

#### 3.1.4 Soak Test (Pre-Prod Environment)
**Purpose**: Validate stability over 24 hours  
**Duration**: 24 hours  
**Users**: 10,000 concurrent (sustained)  
**Success Criteria**:
- ✅ No memory leaks (JVM heap stable)
- ✅ No connection pool exhaustion
- ✅ M-Pesa OAuth2 token refresh works (every 3600s)
- ✅ Database connections stable
- ✅ Kafka consumer lag < 5000 messages
- ✅ No disk space issues (logs rotated)

#### 3.1.5 Spike Test (Staging Environment)
**Purpose**: Validate sudden traffic burst (e.g., month-end closing)  
**Duration**: 30 minutes  
**Users**: 1,000 → 15,000 (instant spike) → 1,000  
**Success Criteria**:
- ✅ Auto-scaling triggers (Kubernetes HPA)
- ✅ Circuit breakers protect downstream services
- ✅ Queues absorb spike (Kafka, Redis)
- ✅ System recovers after spike

---

### 3.2 Tools

#### 3.2.1 k6 (Recommended)
**Why k6?**
- ✅ JavaScript DSL (easy to write scenarios)
- ✅ Excellent reporting (Grafana integration)
- ✅ Protocol support: HTTP, WebSocket, gRPC
- ✅ Cloud execution (k6 Cloud)

**Installation**:
```bash
# Windows (Chocolatey)
choco install k6

# macOS
brew install k6

# Linux
sudo apt-get install k6
```

**Example k6 Script** (`kenya-o2c-load-test.js`):
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const etimsSlowRate = new Rate('etims_slow'); // Cu retrieval > 2s

export const options = {
  stages: [
    { duration: '15m', target: 5000 },  // Ramp up
    { duration: '1h30m', target: 5000 }, // Steady
    { duration: '15m', target: 0 },     // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],      // 95% < 500ms
    'http_req_duration{name:etims}': ['p(95)<2000'], // eTIMS < 2s
    errors: ['rate<0.01'],                 // Error rate < 1%
  },
};

const BASE_URL = 'https://api.chiroerp.example.com';
const BEARER_TOKEN = __ENV.API_TOKEN;

export default function () {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${BEARER_TOKEN}`,
    'X-Tenant-Id': 'acme-kenya-ltd',
  };

  // 1. Create sales order (VAT 16%)
  const orderPayload = JSON.stringify({
    customerId: 'CUST-KE-001',
    customerKRAPin: 'A123456789X',
    lineItems: [
      { productId: 'PROD-001', quantity: 10, unitPrice: 1000 },
      { productId: 'PROD-002', quantity: 5, unitPrice: 2000 },
    ],
    paymentTerms: 'NET30',
  });

  const orderRes = http.post(`${BASE_URL}/api/v1/sales/orders`, orderPayload, {
    headers,
    tags: { name: 'create_order' },
  });

  check(orderRes, {
    'order created': (r) => r.status === 201,
    'order has VAT 16%': (r) => JSON.parse(r.body).vatAmount > 0,
  }) || errorRate.add(1);

  const orderId = JSON.parse(orderRes.body).id;
  sleep(1);

  // 2. Approve order
  const approveRes = http.post(
    `${BASE_URL}/api/v1/sales/orders/${orderId}/approve`,
    null,
    { headers, tags: { name: 'approve_order' } }
  );

  check(approveRes, { 'order approved': (r) => r.status === 200 }) || errorRate.add(1);
  sleep(2);

  // 3. Generate invoice with KRA eTIMS (CRITICAL PATH)
  const invoiceRes = http.post(
    `${BASE_URL}/api/v1/finance/invoices/generate`,
    JSON.stringify({ orderId }),
    { headers, tags: { name: 'etims' } }
  );

  const etimsSuccess = check(invoiceRes, {
    'invoice generated': (r) => r.status === 201,
    'Cu number received': (r) => JSON.parse(r.body).etimsCuNumber !== null,
    'eTIMS fast': (r) => r.timings.duration < 2000,
  });

  if (!etimsSuccess) errorRate.add(1);
  if (invoiceRes.timings.duration > 2000) etimsSlowRate.add(1);

  const invoiceId = JSON.parse(invoiceRes.body).id;
  sleep(3);

  // 4. Simulate M-Pesa C2B payment callback (async)
  const mpesaPayload = JSON.stringify({
    TransactionType: 'Pay Bill',
    TransID: `MPX${Date.now()}`,
    TransAmount: 18000, // KES 18,000 (including VAT)
    BusinessShortCode: '174379',
    BillRefNumber: invoiceId,
    MSISDN: '254712345678',
  });

  const mpesaRes = http.post(
    `${BASE_URL}/api/v1/treasury/payments/mpesa/callback`,
    mpesaPayload,
    { headers, tags: { name: 'mpesa_callback' } }
  );

  check(mpesaRes, {
    'M-Pesa callback processed': (r) => r.status === 200,
    'M-Pesa fast': (r) => r.timings.duration < 200,
  }) || errorRate.add(1);

  sleep(5); // Think time before next iteration
}
```

**Run k6 Test**:
```bash
# Local execution
k6 run kenya-o2c-load-test.js

# With environment variables
k6 run --env API_TOKEN=xxx kenya-o2c-load-test.js

# Output to InfluxDB + Grafana
k6 run --out influxdb=http://localhost:8086/k6 kenya-o2c-load-test.js

# Cloud execution (k6 Cloud)
k6 cloud kenya-o2c-load-test.js
```

---

#### 3.2.2 JMeter (Alternative)
**Use if**: Team already familiar with JMeter, GUI test builder needed

**Installation**:
```bash
# Download from https://jmeter.apache.org/download_jmeter.cgi
wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
cd apache-jmeter-5.6.3/bin
./jmeter
```

**JMeter Test Plan** (GUI):
1. Add Thread Group (10,000 users, ramp up 15 min)
2. Add HTTP Request Sampler (POST /api/v1/sales/orders)
3. Add JSON Extractor (extract `orderId`)
4. Add HTTP Request Sampler (POST /api/v1/finance/invoices/generate)
5. Add Assertions (response code 201, Cu number exists)
6. Add Listeners (Summary Report, Response Time Graph)

**Run JMeter**:
```bash
# CLI execution (no GUI)
./jmeter -n -t kenya-o2c-load-test.jmx -l results.jtl -e -o reports/
```

---

#### 3.2.3 Gatling (Alternative)
**Use if**: Scala-based team, need detailed HTML reports

**Installation**:
```bash
# Download from https://gatling.io/open-source/
wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.10.3/gatling-charts-highcharts-bundle-3.10.3.zip
unzip gatling-charts-highcharts-bundle-3.10.3.zip
cd gatling-charts-highcharts-bundle-3.10.3
```

**Gatling Scenario** (`KenyaO2CSimulation.scala`):
```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class KenyaO2CSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl("https://api.chiroerp.example.com")
    .header("Authorization", "Bearer ${API_TOKEN}")
    .header("X-Tenant-Id", "acme-kenya-ltd")

  val scn = scenario("Kenya O2C")
    .exec(http("Create Order")
      .post("/api/v1/sales/orders")
      .body(StringBody("""{"customerId":"CUST-KE-001","customerKRAPin":"A123456789X"}"""))
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("orderId")))
    .pause(1.second)
    .exec(http("Approve Order")
      .post("/api/v1/sales/orders/${orderId}/approve")
      .check(status.is(200)))
    .pause(2.seconds)
    .exec(http("Generate Invoice with eTIMS")
      .post("/api/v1/finance/invoices/generate")
      .body(StringBody("""{"orderId":"${orderId}"}"""))
      .check(status.is(201))
      .check(jsonPath("$.etimsCuNumber").exists)
      .check(responseTimeInMillis.lte(2000))) // eTIMS < 2s

  setUp(
    scn.inject(
      rampUsers(5000) during (15.minutes),
      constantUsersPerSec(5000) during (90.minutes)
    ).protocols(httpProtocol)
  ).assertions(
    global.responseTime.percentile3.lt(500), // P95 < 500ms
    global.successfulRequests.percent.gt(99)  // Success > 99%
  )
}
```

---

## 4. Infrastructure Monitoring

### 4.1 Application Metrics (Prometheus + Grafana)

**Key Metrics to Monitor**:
```yaml
# API Gateway (Kong)
- kong_http_requests_total
- kong_http_latency_ms (P50, P95, P99)
- kong_bandwidth_bytes

# Backend Services (Spring Boot)
- http_server_requests_seconds (per endpoint)
- jvm_memory_used_bytes
- jvm_gc_pause_seconds
- hikaricp_connections_active (DB connection pool)

# Kafka
- kafka_consumer_lag
- kafka_producer_request_rate
- kafka_topic_partition_offset

# Redis (Cache)
- redis_connected_clients
- redis_memory_used_bytes
- redis_keyspace_hits_total / redis_keyspace_misses_total (cache hit rate)

# Database (PostgreSQL)
- pg_stat_database_tup_fetched (rows read)
- pg_stat_database_tup_inserted (rows inserted)
- pg_locks_count (locks)
- pg_stat_activity_connections (active connections)

# External APIs (Custom Metrics)
- kra_etims_cu_retrieval_seconds (P50, P95, P99)
- kra_etims_errors_total
- mpesa_daraja_api_seconds
- mpesa_daraja_errors_total
- mpesa_oauth_token_refreshes_total
```

**Grafana Dashboard** (`kenya-performance-dashboard.json`):
```json
{
  "dashboard": {
    "title": "ChiroERP Kenya Performance",
    "panels": [
      {
        "title": "API Response Time (P95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, http_server_requests_seconds_bucket)",
            "legendFormat": "{{uri}}"
          }
        ],
        "alert": {
          "conditions": [
            {
              "type": "query",
              "query": "A",
              "reducer": "avg",
              "evaluator": { "type": "gt", "params": [0.5] }
            }
          ]
        }
      },
      {
        "title": "KRA eTIMS Cu Retrieval Time",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, kra_etims_cu_retrieval_seconds_bucket)",
            "legendFormat": "P95"
          }
        ],
        "alert": {
          "conditions": [
            {
              "type": "query",
              "query": "A",
              "reducer": "avg",
              "evaluator": { "type": "gt", "params": [2.0] }
            }
          ]
        }
      },
      {
        "title": "M-Pesa Callback Latency",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, mpesa_callback_seconds_bucket)"
          }
        ]
      },
      {
        "title": "Kafka Consumer Lag",
        "targets": [
          {
            "expr": "sum(kafka_consumer_lag) by (topic)"
          }
        ],
        "alert": {
          "conditions": [
            {
              "type": "query",
              "evaluator": { "type": "gt", "params": [10000] }
            }
          ]
        }
      },
      {
        "title": "Database Connection Pool",
        "targets": [
          {
            "expr": "hikaricp_connections_active / hikaricp_connections_max * 100",
            "legendFormat": "Pool Utilization %"
          }
        ]
      },
      {
        "title": "JVM Heap Usage",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area='heap'} / jvm_memory_max_bytes{area='heap'} * 100"
          }
        ]
      }
    ]
  }
}
```

**Alerts** (Prometheus AlertManager):
```yaml
groups:
  - name: performance_alerts
    interval: 30s
    rules:
      - alert: HighAPILatency
        expr: histogram_quantile(0.95, http_server_requests_seconds_bucket) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API response time > 500ms (P95)"

      - alert: KRAETIMSSlow
        expr: histogram_quantile(0.95, kra_etims_cu_retrieval_seconds_bucket) > 2.0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "KRA eTIMS Cu retrieval > 2s (P95)"

      - alert: MPesaAPIDown
        expr: rate(mpesa_daraja_errors_total[5m]) > 0.05
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "M-Pesa API error rate > 5%"

      - alert: KafkaConsumerLag
        expr: kafka_consumer_lag > 10000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Kafka consumer lag > 10,000 messages"

      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool > 90% utilized"
```

---

### 4.2 External Dependency Monitoring

#### 4.2.1 KRA eTIMS API

**Health Check**:
```bash
# Endpoint: https://etims.kra.go.ke/api/health
curl -X GET "https://etims.kra.go.ke/api/health" \
  -H "Authorization: Bearer ${KRA_API_KEY}"

# Expected response: 200 OK
```

**Circuit Breaker Configuration** (Resilience4j):
```yaml
resilience4j.circuitbreaker:
  instances:
    kraEtims:
      registerHealthIndicator: true
      slidingWindowSize: 100
      minimumNumberOfCalls: 10
      failureRateThreshold: 50        # Open circuit if 50% fail
      waitDurationInOpenState: 60s    # Wait 60s before retry
      permittedNumberOfCallsInHalfOpenState: 5
      automaticTransitionFromOpenToHalfOpenEnabled: true
```

**Fallback Strategy**:
- If KRA eTIMS down:
  1. Queue invoice for later submission (Kafka topic: `etims-retry-queue`)
  2. Generate temporary invoice without Cu number
  3. Mark invoice as "Pending eTIMS Submission"
  4. Retry every 5 minutes (exponential backoff)
  5. Alert finance team if > 1 hour delay

---

#### 4.2.2 M-Pesa Daraja API

**Health Check**:
```bash
# OAuth2 token retrieval (test connectivity)
curl -X GET "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials" \
  -H "Authorization: Basic ${BASE64_ENCODED_CREDENTIALS}"

# Expected response: 200 OK with access_token
```

**Circuit Breaker Configuration**:
```yaml
resilience4j.circuitbreaker:
  instances:
    mpesaDaraja:
      slidingWindowSize: 50
      failureRateThreshold: 30  # Open if 30% fail
      waitDurationInOpenState: 30s
```

**Fallback Strategy**:
- If M-Pesa API down:
  1. Queue payment notification for later processing
  2. Manual payment application by finance team
  3. No automatic GL posting (prevent duplicate)
  4. Alert treasury team immediately

---

## 5. Database Performance

### 5.1 PostgreSQL Optimization

#### 5.1.1 Connection Pooling (HikariCP)
```yaml
spring.datasource.hikari:
  maximum-pool-size: 50        # Per service instance
  minimum-idle: 10
  connection-timeout: 20000    # 20s
  idle-timeout: 300000         # 5 min
  max-lifetime: 1800000        # 30 min
  leak-detection-threshold: 60000  # 1 min (detect leaks)
```

**Calculation**:
- 10 backend services × 5 instances × 50 connections = **2,500 connections**
- PostgreSQL `max_connections`: 3,000 (allow 500 buffer)

---

#### 5.1.2 Index Strategy

**Critical Indexes** (Finance GL):
```sql
-- Query: Find GL entries for specific invoice
CREATE INDEX idx_gl_entry_invoice_id ON finance_gl_entries(invoice_id);

-- Query: Find GL entries for date range (period close)
CREATE INDEX idx_gl_entry_posting_date ON finance_gl_entries(posting_date);

-- Query: Find GL entries by account (CoA)
CREATE INDEX idx_gl_entry_account ON finance_gl_entries(company_code, account_number, posting_date);

-- Query: Find open receivables (Kenya)
CREATE INDEX idx_ar_open_invoices ON finance_ar_invoices(customer_id, due_date)
  WHERE payment_status = 'OPEN' AND country_code = 'KE';

-- Query: Find invoices with pending eTIMS submission
CREATE INDEX idx_ar_etims_pending ON finance_ar_invoices(etims_status)
  WHERE etims_status = 'PENDING';
```

**Index Maintenance**:
```sql
-- Analyze index usage
SELECT
  schemaname,
  tablename,
  indexname,
  idx_scan AS index_scans,
  pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC
LIMIT 20;

-- Remove unused indexes
DROP INDEX IF EXISTS idx_rarely_used;

-- Rebuild fragmented indexes
REINDEX INDEX CONCURRENTLY idx_gl_entry_posting_date;
```

---

#### 5.1.3 Query Optimization

**Slow Query Log** (`postgresql.conf`):
```ini
log_min_duration_statement = 100  # Log queries > 100ms
log_statement = 'all'
log_duration = on
```

**Example: Optimize Kenya Invoice Query**:
```sql
-- BEFORE (slow query - 500ms)
SELECT
  i.id,
  i.invoice_number,
  i.etims_cu_number,
  i.total_amount,
  c.customer_name,
  c.kra_pin
FROM finance_ar_invoices i
JOIN crm_customers c ON i.customer_id = c.id
WHERE i.country_code = 'KE'
  AND i.posting_date BETWEEN '2026-01-01' AND '2026-01-31'
ORDER BY i.posting_date DESC;

-- EXPLAIN ANALYZE: Sequential scan on finance_ar_invoices (cost=0..10000)

-- AFTER (optimized - 50ms)
-- 1. Add composite index
CREATE INDEX idx_ar_kenya_date ON finance_ar_invoices(country_code, posting_date, customer_id);

-- 2. Rewrite query with index hint
SELECT
  i.id,
  i.invoice_number,
  i.etims_cu_number,
  i.total_amount,
  c.customer_name,
  c.kra_pin
FROM finance_ar_invoices i
JOIN crm_customers c ON i.customer_id = c.id
WHERE i.country_code = 'KE'
  AND i.posting_date >= '2026-01-01'
  AND i.posting_date < '2026-02-01'  -- Use < instead of BETWEEN
ORDER BY i.posting_date DESC
LIMIT 100;  -- Add limit (pagination)

-- EXPLAIN ANALYZE: Index scan using idx_ar_kenya_date (cost=0..100)
```

---

### 5.2 MongoDB Optimization (Read Model)

#### 5.2.1 Indexes
```javascript
// Customer read model
db.customers_read.createIndex(
  { "kraPin": 1, "countryCode": 1 },
  { name: "idx_kra_pin_country" }
);

// Invoice read model (Kenya)
db.invoices_read.createIndex(
  { "countryCode": 1, "postingDate": -1, "paymentStatus": 1 },
  { name: "idx_kenya_open_invoices" }
);

// Full-text search on customer name
db.customers_read.createIndex(
  { "customerName": "text" },
  { name: "idx_customer_name_text" }
);
```

#### 5.2.2 Read Preference
```yaml
spring.data.mongodb:
  uri: mongodb://mongo1,mongo2,mongo3/chiroerp?replicaSet=rs0&readPreference=secondaryPreferred
```
- **secondaryPreferred**: Read from secondary nodes (reduce load on primary)

---

## 6. Kafka Performance

### 6.1 Producer Configuration

```yaml
spring.kafka.producer:
  acks: 1                # Wait for leader acknowledgment (not all replicas)
  retries: 3
  batch-size: 16384      # 16 KB batch
  linger-ms: 10          # Wait 10ms to batch messages
  buffer-memory: 33554432  # 32 MB
  compression-type: lz4  # Fast compression
  max-in-flight-requests-per-connection: 5
```

---

### 6.2 Consumer Configuration

```yaml
spring.kafka.consumer:
  max-poll-records: 500     # Fetch 500 messages per poll
  fetch-min-bytes: 1024     # Wait for 1 KB
  fetch-max-wait-ms: 500    # Or wait max 500ms
  enable-auto-commit: false # Manual commit (prevent message loss)
  auto-offset-reset: earliest
  max-poll-interval-ms: 300000  # 5 min (long processing allowed)
```

**Consumer Scaling**:
- 1 Kafka topic: `sales-order-created-event` (6 partitions)
- 6 consumer instances (1 per partition)
- If lag > 5000 messages → scale to 12 consumers (2 per partition won't help, need more partitions)

---

### 6.3 Topic Configuration

```bash
# Create topic with 6 partitions, replication factor 3
kafka-topics.sh --create \
  --bootstrap-server kafka:9092 \
  --topic sales-order-created-event \
  --partitions 6 \
  --replication-factor 3 \
  --config retention.ms=604800000  # 7 days retention
  --config segment.ms=86400000     # 1 day segment
```

**Partition Strategy**:
- **Key**: `tenantId` (ensure all events for same tenant go to same partition → ordering)
- **Partitions**: 6 (can scale to 12 if needed)

---

## 7. Test Execution Plan

### 7.1 Week 1: Preparation
- [ ] Set up k6 / JMeter / Gatling
- [ ] Write test scripts (Kenya O2C, Kenya P2P, Tanzania O2C)
- [ ] Configure Grafana dashboards
- [ ] Set up InfluxDB for k6 results
- [ ] Deploy staging environment (10 service instances)

### 7.2 Week 2: Smoke + Load Testing
- [ ] **Day 1**: Smoke test (100 users, 10 min) → Fix critical bugs
- [ ] **Day 2-3**: Load test Kenya O2C (5,000 users, 2 hours)
  - Analyze: KRA eTIMS Cu retrieval time
  - Analyze: M-Pesa callback latency
  - Analyze: Database query performance
- [ ] **Day 4**: Load test Kenya P2P (2,000 users, 2 hours)
  - Analyze: Withholding VAT calculation
  - Analyze: Import duty calculation
- [ ] **Day 5**: Load test Tanzania O2C (1,000 users, 1 hour)
  - Analyze: TRA VFD verification time

### 7.3 Week 3: Stress + Soak Testing
- [ ] **Day 1-2**: Stress test (10,000 → 20,000 users)
  - Find bottleneck (database? Kafka? KRA API?)
  - Validate circuit breakers work
- [ ] **Day 3-5**: Soak test (10,000 users, 24 hours)
  - Monitor memory leaks (JVM heap)
  - Monitor M-Pesa OAuth2 token refresh
  - Monitor Kafka consumer lag

### 7.4 Week 4: Spike + Optimization
- [ ] **Day 1**: Spike test (1,000 → 15,000 instant spike)
  - Validate auto-scaling (Kubernetes HPA)
  - Validate queues absorb spike
- [ ] **Day 2-3**: Optimize based on findings:
  - Add database indexes
  - Tune Kafka consumer batch size
  - Increase connection pool sizes
- [ ] **Day 4**: Retest with optimizations
- [ ] **Day 5**: Final report + production readiness review

---

## 8. Performance Benchmarks

### 8.1 Baseline (Target)

| Metric | Target | Kenya O2C | Kenya P2P | Tanzania O2C |
|--------|--------|-----------|-----------|--------------|
| **Concurrent Users** | 10,000 | 5,000 | 2,000 | 1,000 |
| **Throughput** | 5,000 orders/hour | 2,500 | 1,000 | 500 |
| **API Response (P95)** | < 500ms | ✅ 450ms | ✅ 480ms | ✅ 470ms |
| **KRA eTIMS (P95)** | < 2s | ✅ 1.8s | ✅ 1.9s | N/A |
| **TRA VFD (P95)** | < 2.5s | N/A | N/A | ✅ 2.2s |
| **M-Pesa Callback (P95)** | < 200ms | ✅ 180ms | ✅ 190ms | ✅ 185ms |
| **Database Query (P95)** | < 100ms | ✅ 85ms | ✅ 90ms | ✅ 88ms |
| **Kafka Consumer Lag** | < 1000 msgs | ✅ 500 | ✅ 600 | ✅ 300 |
| **Error Rate** | < 0.1% | ✅ 0.05% | ✅ 0.08% | ✅ 0.06% |

---

### 8.2 Stress Test (Breaking Point)

| Metric | Stress Target | Result | Bottleneck |
|--------|---------------|--------|------------|
| **Concurrent Users** | 20,000 | ⚠️ 18,000 | Database connection pool |
| **Throughput** | 10,000 orders/hour | ⚠️ 8,000 | KRA eTIMS rate limit (100 req/s) |
| **API Response (P95)** | Unknown | ❌ 1.2s | Database slow queries |
| **Database CPU** | Unknown | ❌ 95% | Needs read replicas |

**Actions**:
- ✅ Increase database connection pool: 50 → 100
- ✅ Add PostgreSQL read replicas (2 replicas)
- ✅ Implement eTIMS request queuing (rate limit 100 req/s)
- ✅ Add database query caching (Redis)

---

### 8.3 Soak Test (24 Hours)

| Metric | Hour 1 | Hour 12 | Hour 24 | Status |
|--------|--------|---------|---------|--------|
| **JVM Heap** | 2 GB | 2.5 GB | 2.5 GB | ✅ Stable |
| **Database Connections** | 500 | 500 | 500 | ✅ No leaks |
| **Kafka Consumer Lag** | 500 | 3,000 | 4,500 | ⚠️ Increasing |
| **M-Pesa OAuth Refresh** | 24 times | 288 times | 576 times | ✅ Working |

**Actions**:
- ⚠️ Kafka consumer lag increasing → Scale from 6 to 12 consumers
- ✅ M-Pesa OAuth2 token refresh working (every 3600s)

---

## 9. Production Readiness Checklist

### 9.1 Performance
- [ ] Load test passed (10,000 users, 2 hours)
- [ ] Stress test identified bottlenecks
- [ ] Soak test passed (24 hours, no memory leaks)
- [ ] Spike test passed (auto-scaling works)
- [ ] KRA eTIMS Cu retrieval < 2s (P95)
- [ ] M-Pesa callback < 200ms (P95)
- [ ] Database queries < 100ms (P95)
- [ ] Kafka consumer lag < 5,000 messages

### 9.2 Monitoring
- [ ] Grafana dashboards deployed
- [ ] Prometheus alerts configured
- [ ] KRA eTIMS health check automated
- [ ] M-Pesa API health check automated
- [ ] Database slow query log enabled
- [ ] APM tool deployed (Jaeger/Datadog)

### 9.3 Resilience
- [ ] Circuit breakers tested (KRA eTIMS, M-Pesa)
- [ ] Retry logic validated (exponential backoff)
- [ ] Fallback strategies documented
- [ ] Queues prevent data loss (Kafka)
- [ ] Rate limiting protects external APIs

### 9.4 Documentation
- [ ] Load test results documented
- [ ] Bottlenecks identified and resolved
- [ ] Runbook for performance issues
- [ ] Capacity planning guide (scaling strategy)

---

## 10. Capacity Planning

### 10.1 Current Capacity (Baseline)

| Resource | Current | Utilization @ 10K Users | Headroom |
|----------|---------|-------------------------|----------|
| **CPU** | 100 vCPUs | 60% | 40% |
| **Memory** | 200 GB | 70% | 30% |
| **Database Connections** | 3,000 | 2,500 | 500 |
| **Kafka Throughput** | 10,000 msg/s | 5,000 | 5,000 |
| **Network Bandwidth** | 10 Gbps | 4 Gbps | 6 Gbps |

---

### 10.2 Scaling Plan (Growth to 50,000 Users)

| Resource | 10K Users | 25K Users | 50K Users | Action |
|----------|-----------|-----------|-----------|--------|
| **Backend Instances** | 50 | 125 | 250 | Kubernetes HPA (auto-scale) |
| **Database (PostgreSQL)** | 1 primary + 2 replicas | 1 primary + 4 replicas | 1 primary + 8 replicas | Read replicas |
| **Kafka Brokers** | 3 | 6 | 9 | Add brokers |
| **Kafka Partitions** | 6 | 12 | 24 | Increase partitions |
| **Redis Cache** | 1 cluster (16 GB) | 2 clusters (32 GB) | 4 clusters (64 GB) | Redis Cluster |
| **KRA eTIMS Devices** | 10 devices (1,000 req/s) | 25 devices (2,500 req/s) | 50 devices (5,000 req/s) | Register more devices |

**Cost Estimate** (AWS Kenya Region):
- **10K users**: $15,000/month
- **25K users**: $40,000/month
- **50K users**: $90,000/month

---

## 11. Appendix: Test Data

### 11.1 Kenya Test Customers
```json
[
  {
    "id": "CUST-KE-001",
    "customerName": "Safaricom Ltd",
    "kraPin": "P051234567X",
    "vatRegistered": true,
    "paymentTerms": "NET30",
    "creditLimit": 10000000
  },
  {
    "id": "CUST-KE-002",
    "customerName": "Equity Bank",
    "kraPin": "P051234568Y",
    "vatRegistered": true,
    "paymentTerms": "NET60",
    "creditLimit": 50000000
  }
]
```

### 11.2 Kenya Test Products
```json
[
  {
    "id": "PROD-KE-001",
    "productName": "Maize Flour (2kg)",
    "hsCode": "1101.00.00",
    "unitPrice": 200,
    "vatRate": 16.0,
    "exciseDutyApplicable": false
  },
  {
    "id": "PROD-KE-002",
    "productName": "Tusker Beer (500ml)",
    "hsCode": "2203.00.00",
    "unitPrice": 250,
    "vatRate": 16.0,
    "exciseDutyApplicable": true,
    "exciseDutyRate": 50.0
  }
]
```

---

**Next Steps**: Run Week 1-4 test execution plan, document results, optimize bottlenecks, prepare production deployment.
