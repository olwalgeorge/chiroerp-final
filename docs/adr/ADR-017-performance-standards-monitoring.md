# ADR-017: Performance Standards & Monitoring

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Platform Team, SRE Team  
**Tier**: Core  
**Tags**: performance, monitoring, slos, observability, scalability  

## Context
SAP-grade ERP systems define clear performance standards, monitor them continuously, and enforce capacity planning. This ADR defines platform-wide performance targets, monitoring baselines, and optimization guidance to ensure predictable user experience and scaling behavior.

## Decision
Adopt **platform-wide performance standards** and a **monitoring framework** with defined SLIs/SLOs, load testing requirements, and capacity planning. Implementation is **not started**; this ADR defines the standard.

### Performance Standards

### API Latency Targets (p95)

| Tier | Use Case | p50 Target | p95 Target | p99 Target | Timeout |
|------|----------|------------|------------|------------|---------|
| **Tier 1** | Financial posting, payment approval, document posting | <= 100 ms | <= 300 ms | <= 500 ms | 2s |
| **Tier 2** | Order entry, inventory update, customer creation | <= 150 ms | <= 500 ms | <= 800 ms | 5s |
| **Tier 3** | Operational reports (materialized views) | <= 500 ms | <= 1500 ms | <= 2500 ms | 10s |
| **Tier 4** | Analytics queries, complex reports | <= 2000 ms | <= 5000 ms | <= 10000 ms | 30s |
| **Tier 5** | Batch operations, long-running processes | N/A | N/A | N/A | 300s |

### Throughput & Concurrency Targets

| Service | Baseline TPS | Peak TPS | Concurrent Users | Queue Capacity |
|---------|--------------|----------|------------------|----------------|
| Financial Accounting API | 100 tx/s | 500 tx/s | 500 users | 10,000 |
| AP/AR API | 80 tx/s | 400 tx/s | 400 users | 8,000 |
| Sales Order API | 150 tx/s | 750 tx/s | 1,000 users | 15,000 |
| Inventory API | 200 tx/s | 1,000 tx/s | 500 users | 20,000 |
| CRM API | 120 tx/s | 600 tx/s | 800 users | 12,000 |

### Database Performance Standards

| Metric | Target | Measurement |
|--------|--------|-------------|
| Query p95 latency | <= 50 ms | Slow query log threshold |
| Query p99 latency | <= 100 ms | Requires investigation |
| Connection pool utilization | <= 70% | Average during business hours |
| Transaction duration p95 | <= 200 ms | Long-running tx log |
| Index hit ratio | >= 95% | PostgreSQL pg_stat_user_indexes |
| Cache hit ratio | >= 90% | PostgreSQL pg_stat_database |
| Replication lag | <= 5 seconds | Streaming replication monitor |

### Message Broker Performance Standards

| Metric | Target | Action Threshold |
|--------|--------|------------------|
| Producer latency p95 | <= 10 ms | <= 50 ms (alert) |
| Consumer lag | <= 1000 messages | >= 10,000 (alert) |
| End-to-end latency p95 | <= 100 ms | <= 500 ms (warning) |
| Dead letter queue depth | 0 messages | >= 10 (investigate) |
| Partition rebalance frequency | <= 1 per hour | >= 5 per hour (alert) |
| Broker disk utilization | <= 70% | >= 85% (critical) |

### Batch Job Performance Standards

| Job Type | SLA Duration | Max Retries | Failure Action |
|----------|--------------|-------------|----------------|
| Month-end close (per entity) | <= 2 hours | 3 | Escalate to SRE |
| Daily ETL load (standard tenant) | <= 1 hour | 3 | Auto-rollback |
| Weekly inventory valuation | <= 30 minutes | 2 | Manual review |
| Nightly backup | <= 4 hours | 1 | Page on-call |
| Report generation (bulk) | <= 15 minutes | 2 | Notify admin |
| Data archival job | <= 8 hours | 1 | Manual intervention |

### JVM Performance Standards

| Metric | Target | Critical Threshold |
|--------|--------|-------------------|
| Heap utilization | <= 70% | >= 85% |
| GC pause time p95 | <= 100 ms | >= 500 ms |
| GC frequency | <= 10 per minute | >= 30 per minute |
| Thread count | <= 200 | >= 500 |
| CPU utilization (per pod) | <= 70% | >= 90% |
| Memory utilization (per pod) | <= 80% | >= 95% |

### Observability Stack Implementation

### Technology Stack

```yaml
observability:
  metrics:
    collection: Micrometer + Prometheus
    storage: Prometheus (30 days retention) + Thanos (long-term)
    visualization: Grafana
    alerting: Prometheus Alertmanager + PagerDuty
  
  tracing:
    instrumentation: OpenTelemetry
    backend: Jaeger / Tempo
    sampling: 1% baseline, 100% on errors
    retention: 7 days traces, 90 days aggregated
  
  logging:
    collection: Logback + Fluentd
    storage: Elasticsearch / Loki
    visualization: Kibana / Grafana
    retention: 30 days hot, 90 days warm, 1 year cold
  
  apm:
    tool: Elastic APM / Datadog
    coverage: All Tier 1 and Tier 2 services
```

### Prometheus Metrics Standards

```kotlin
// Service-level metrics
@Component
class ServiceMetrics(
    private val meterRegistry: MeterRegistry
) {
    // Counters
    val requestCounter = Counter.builder("http.requests.total")
        .description("Total HTTP requests")
        .tags("service", "financial-accounting", "context", "fi")
        .register(meterRegistry)
    
    val errorCounter = Counter.builder("http.errors.total")
        .description("Total HTTP errors")
        .tags("service", "financial-accounting", "status", "5xx")
        .register(meterRegistry)
    
    val businessErrorCounter = Counter.builder("business.errors.total")
        .description("Business validation errors")
        .tags("service", "financial-accounting", "error_code", "")
        .register(meterRegistry)
    
    // Timers (automatically track count, sum, max, percentiles)
    val requestTimer = Timer.builder("http.request.duration")
        .description("HTTP request duration")
        .tags("service", "financial-accounting", "endpoint", "", "method", "")
        .publishPercentiles(0.5, 0.95, 0.99)
        .register(meterRegistry)
    
    val databaseTimer = Timer.builder("database.query.duration")
        .description("Database query duration")
        .tags("service", "financial-accounting", "query", "")
        .publishPercentiles(0.5, 0.95, 0.99)
        .register(meterRegistry)
    
    // Gauges
    val activeConnections = Gauge.builder("database.connections.active") { getActiveConnections() }
        .description("Active database connections")
        .tags("service", "financial-accounting", "pool", "default")
        .register(meterRegistry)
    
    val queueDepth = Gauge.builder("kafka.consumer.lag") { getConsumerLag() }
        .description("Kafka consumer lag")
        .tags("service", "financial-accounting", "topic", "", "group", "")
        .register(meterRegistry)
    
    // Histograms
    val payloadSize = DistributionSummary.builder("http.request.payload.size")
        .description("HTTP request payload size in bytes")
        .tags("service", "financial-accounting")
        .baseUnit("bytes")
        .register(meterRegistry)
}

// Instrumentation example
@RestController
@RequestMapping("/api/v1/financial/journal-entries")
class JournalEntryController(
    private val journalEntryService: JournalEntryService,
    private val metrics: ServiceMetrics
) {
    
    @PostMapping
    @Timed(value = "journal.entry.post", percentiles = [0.5, 0.95, 0.99])
    suspend fun postJournalEntry(@RequestBody request: PostJournalEntryRequest): JournalEntryResponse {
        val timer = Timer.start(metrics.meterRegistry)
        
        try {
            metrics.requestCounter.increment()
            
            val result = journalEntryService.post(request)
            
            timer.stop(metrics.requestTimer.tag("endpoint", "post-journal-entry")
                                          .tag("method", "POST")
                                          .tag("status", "200"))
            
            return result
            
        } catch (e: BusinessValidationException) {
            metrics.businessErrorCounter.tag("error_code", e.errorCode).increment()
            timer.stop(metrics.requestTimer.tag("status", "400"))
            throw e
        } catch (e: Exception) {
            metrics.errorCounter.increment()
            timer.stop(metrics.requestTimer.tag("status", "500"))
            throw e
        }
    }
}
```

### OpenTelemetry Tracing Configuration

```kotlin
// application.yml
quarkus:
  otel:
    enabled: true
    exporter:
      otlp:
        endpoint: http://jaeger-collector:4317
    traces:
      sampler: parentbased_traceidratio
      sampler.arg: 0.01  # 1% sampling
    resource:
      attributes:
        service.name: financial-accounting-service
        service.version: 1.2.3
        deployment.environment: production
        tenant.id: ${TENANT_ID}

// Custom span instrumentation
@ApplicationScoped
class JournalEntryService(
    private val tracer: Tracer
) {
    
    suspend fun post(request: PostJournalEntryRequest): JournalEntryResponse {
        val span = tracer.spanBuilder("journal-entry-post")
            .setSpanKind(SpanKind.SERVER)
            .setAttribute("tenant.id", request.tenantId)
            .setAttribute("document.type", request.documentType)
            .setAttribute("amount", request.totalAmount.toString())
            .startSpan()
        
        return span.makeCurrent().use {
            try {
                // Validation span
                val validationSpan = tracer.spanBuilder("validate-journal-entry").startSpan()
                validateJournalEntry(request)
                validationSpan.end()
                
                // Authorization span
                val authzSpan = tracer.spanBuilder("check-authorization").startSpan()
                checkAuthorization(request)
                authzSpan.end()
                
                // Database span (auto-instrumented)
                val result = repository.save(request)
                
                // Event publishing span
                val eventSpan = tracer.spanBuilder("publish-event").startSpan()
                publishEvent(result)
                eventSpan.end()
                
                span.setStatus(StatusCode.OK)
                result
                
            } catch (e: Exception) {
                span.recordException(e)
                span.setStatus(StatusCode.ERROR, e.message ?: "Unknown error")
                throw e
            } finally {
                span.end()
            }
        }
    }
}
```

### Grafana Dashboard Definitions

```yaml
# dashboards/service-overview.json (simplified)
{
  "dashboard": {
    "title": "Financial Accounting Service - Overview",
    "tags": ["financial", "service"],
    "refresh": "10s",
    "panels": [
      {
        "title": "Request Rate (req/s)",
        "targets": [
          {
            "expr": "rate(http_requests_total{service=\"financial-accounting\"}[5m])",
            "legendFormat": "{{method}} {{endpoint}}"
          }
        ]
      },
      {
        "title": "Request Latency (p50, p95, p99)",
        "targets": [
          {
            "expr": "histogram_quantile(0.50, rate(http_request_duration_bucket{service=\"financial-accounting\"}[5m]))",
            "legendFormat": "p50"
          },
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_bucket{service=\"financial-accounting\"}[5m]))",
            "legendFormat": "p95"
          },
          {
            "expr": "histogram_quantile(0.99, rate(http_request_duration_bucket{service=\"financial-accounting\"}[5m]))",
            "legendFormat": "p99"
          }
        ]
      },
      {
        "title": "Error Rate (%)",
        "targets": [
          {
            "expr": "100 * (rate(http_errors_total{service=\"financial-accounting\"}[5m]) / rate(http_requests_total{service=\"financial-accounting\"}[5m]))",
            "legendFormat": "5xx errors"
          },
          {
            "expr": "100 * (rate(business_errors_total{service=\"financial-accounting\"}[5m]) / rate(http_requests_total{service=\"financial-accounting\"}[5m]))",
            "legendFormat": "Business errors"
          }
        ]
      },
      {
        "title": "Database Connection Pool",
        "targets": [
          {
            "expr": "database_connections_active{service=\"financial-accounting\"}",
            "legendFormat": "Active"
          },
          {
            "expr": "database_connections_idle{service=\"financial-accounting\"}",
            "legendFormat": "Idle"
          },
          {
            "expr": "database_connections_max{service=\"financial-accounting\"}",
            "legendFormat": "Max"
          }
        ]
      },
      {
        "title": "Kafka Consumer Lag",
        "targets": [
          {
            "expr": "kafka_consumer_lag{service=\"financial-accounting\"}",
            "legendFormat": "{{topic}} - {{partition}}"
          }
        ]
      },
      {
        "title": "JVM Heap Usage",
        "targets": [
          {
            "expr": "100 * (jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"})",
            "legendFormat": "Heap utilization %"
          }
        ]
      },
      {
        "title": "GC Pause Time (p95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(jvm_gc_pause_seconds_bucket[5m]))",
            "legendFormat": "{{gc}} - p95"
          }
        ]
      }
    ]
  }
}
```

### Alerting Rules

```yaml
# prometheus/alerts/service-alerts.yml
groups:
  - name: service_alerts
    interval: 30s
    rules:
      # Latency alerts
      - alert: HighLatencyTier1
        expr: |
          histogram_quantile(0.95, 
            rate(http_request_duration_bucket{tier="1"}[5m])
          ) > 0.3
        for: 5m
        labels:
          severity: critical
          tier: tier1
        annotations:
          summary: "Tier 1 API latency p95 > 300ms"
          description: "{{ $labels.service }} {{ $labels.endpoint }} has p95 latency of {{ $value }}s"
      
      - alert: HighLatencyTier2
        expr: |
          histogram_quantile(0.95, 
            rate(http_request_duration_bucket{tier="2"}[5m])
          ) > 0.5
        for: 10m
        labels:
          severity: warning
          tier: tier2
        annotations:
          summary: "Tier 2 API latency p95 > 500ms"
          description: "{{ $labels.service }} has elevated latency"
      
      # Error rate alerts
      - alert: HighErrorRate
        expr: |
          100 * (
            rate(http_errors_total[5m]) / 
            rate(http_requests_total[5m])
          ) > 5
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Error rate > 5%"
          description: "{{ $labels.service }} error rate is {{ $value }}%"
      
      # Database alerts
      - alert: DatabaseConnectionPoolExhausted
        expr: |
          database_connections_active / 
          database_connections_max > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool > 90% utilized"
          description: "{{ $labels.service }} pool {{ $labels.pool }} at {{ $value }}%"
      
      - alert: SlowDatabaseQueries
        expr: |
          histogram_quantile(0.95, 
            rate(database_query_duration_bucket[5m])
          ) > 0.1
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Database query p95 > 100ms"
          description: "{{ $labels.service }} queries are slow"
      
      # Kafka alerts
      - alert: KafkaConsumerLagHigh
        expr: kafka_consumer_lag > 10000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Kafka consumer lag > 10k messages"
          description: "{{ $labels.service }} topic {{ $labels.topic }} lag is {{ $value }}"
      
      - alert: KafkaConsumerLagCritical
        expr: kafka_consumer_lag > 100000
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Kafka consumer lag > 100k messages"
          description: "{{ $labels.service }} is severely behind"
      
      # JVM alerts
      - alert: HighHeapUsage
        expr: |
          100 * (
            jvm_memory_used_bytes{area="heap"} / 
            jvm_memory_max_bytes{area="heap"}
          ) > 85
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "JVM heap usage > 85%"
          description: "{{ $labels.service }} heap at {{ $value }}%"
      
      - alert: LongGCPauses
        expr: |
          histogram_quantile(0.95, 
            rate(jvm_gc_pause_seconds_bucket[5m])
          ) > 0.5
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "GC pause p95 > 500ms"
          description: "{{ $labels.service }} experiencing long GC pauses"
      
      # Availability alerts
      - alert: ServiceDown
        expr: up{job=~".*-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service is down"
          description: "{{ $labels.job }} has been down for 1 minute"
```

### Service Level Objectives (SLOs)

### SLO Definitions

| Service | SLI | SLO Target | Error Budget (Monthly) | Measurement Window |
|---------|-----|------------|------------------------|-------------------|
| Financial Accounting | Availability | 99.9% | 43 minutes | 30 days rolling |
| Financial Accounting | Latency (p95) | <= 300 ms | 5% violations | 7 days rolling |
| Financial Accounting | Error rate | <= 0.1% | 0.5% budget | 7 days rolling |
| AP/AR Services | Availability | 99.9% | 43 minutes | 30 days rolling |
| AP/AR Services | Latency (p95) | <= 300 ms | 5% violations | 7 days rolling |
| Sales Order | Availability | 99.5% | 3.6 hours | 30 days rolling |
| Sales Order | Latency (p95) | <= 500 ms | 5% violations | 7 days rolling |
| Inventory | Availability | 99.5% | 3.6 hours | 30 days rolling |
| Inventory | Latency (p95) | <= 500 ms | 5% violations | 7 days rolling |
| Reporting | Availability | 99.0% | 7.2 hours | 30 days rolling |
| Reporting | Latency (p95) | <= 1500 ms | 10% violations | 7 days rolling |

### Error Budget Policy

```kotlin
data class SLOConfig(
    val service: String,
    val sli: String,
    val target: Double,
    val errorBudget: Double,
    val windowDays: Int
)

data class ErrorBudgetStatus(
    val service: String,
    val sli: String,
    val budgetRemaining: Double,
    val budgetConsumed: Double,
    val status: BudgetStatus,
    val projectedBurnRate: Double
)

enum class BudgetStatus {
    HEALTHY,        // > 50% budget remaining
    WARNING,        // 25-50% budget remaining
    CRITICAL,       // < 25% budget remaining
    EXHAUSTED       // 0% budget remaining
}

@ApplicationScoped
class ErrorBudgetService {
    
    fun calculateErrorBudget(
        service: String,
        slo: SLOConfig,
        actualPerformance: Double,
        windowStart: Instant,
        windowEnd: Instant
    ): ErrorBudgetStatus {
        
        val totalBudget = slo.errorBudget
        val actualViolations = (slo.target - actualPerformance).coerceAtLeast(0.0)
        val budgetConsumed = actualViolations / totalBudget
        val budgetRemaining = 1.0 - budgetConsumed
        
        // Calculate burn rate (% budget consumed per day)
        val windowDuration = Duration.between(windowStart, windowEnd)
        val daysElapsed = windowDuration.toDays().toDouble()
        val burnRatePerDay = budgetConsumed / daysElapsed
        val projectedBurnRate = burnRatePerDay * slo.windowDays
        
        val status = when {
            budgetRemaining <= 0.0 -> BudgetStatus.EXHAUSTED
            budgetRemaining < 0.25 -> BudgetStatus.CRITICAL
            budgetRemaining < 0.50 -> BudgetStatus.WARNING
            else -> BudgetStatus.HEALTHY
        }
        
        return ErrorBudgetStatus(
            service = service,
            sli = slo.sli,
            budgetRemaining = budgetRemaining,
            budgetConsumed = budgetConsumed,
            status = status,
            projectedBurnRate = projectedBurnRate
        )
    }
    
    fun getErrorBudgetPolicy(status: BudgetStatus): String {
        return when (status) {
            BudgetStatus.HEALTHY -> 
                "Normal operations. Focus on new features and improvements."
            
            BudgetStatus.WARNING -> 
                "Elevated risk. Review reliability. Consider postponing risky deployments."
            
            BudgetStatus.CRITICAL -> 
                "High risk. Freeze non-critical changes. Focus on reliability improvements."
            
            BudgetStatus.EXHAUSTED -> 
                "Budget exhausted. Deployment freeze except for reliability fixes. Root cause analysis required."
        }
    }
}
```

### SLO Monitoring Dashboard

```promql
# Availability SLO (99.9%)
# Target: 99.9% uptime = 43 minutes downtime per 30 days
100 * (
  1 - (
    sum(rate(http_errors_total{status=~"5.."}[30d])) /
    sum(rate(http_requests_total[30d]))
  )
)

# Latency SLO (p95 <= 300ms for Tier 1)
# Target: 95% of requests <= 300ms
100 * (
  sum(rate(http_request_duration_bucket{tier="1", le="0.3"}[7d])) /
  sum(rate(http_request_duration_count{tier="1"}[7d]))
)

# Error Budget Remaining (Availability)
# Shows % of error budget remaining
100 * (
  1 - (
    (0.999 - current_availability) / (1 - 0.999)
  )
)

# Burn Rate Alert (fast burn = incidents)
# Triggers if consuming budget > 10x normal rate
(
  rate(http_errors_total{status=~"5.."}[1h]) /
  rate(http_requests_total[1h])
) > (0.001 * 10)  # 10x the 0.1% error rate SLO
```

### Load & Performance Testing Framework

### Testing Stack

```yaml
performance_testing:
  load_testing:
    tool: k6 (primary), JMeter (legacy compatibility)
    scenarios:
      - smoke: 10 VUs, 2 minutes (sanity check)
      - load: 100 VUs, 10 minutes (baseline)
      - stress: 100 → 500 VUs, 20 minutes (find limits)
      - spike: 100 → 1000 VUs sudden, 5 minutes (elasticity)
      - soak: 100 VUs, 4 hours (memory leaks)
  
  database_testing:
    tool: pgbench (PostgreSQL), custom JMeter SQL
    scenarios:
      - concurrent_reads: 500 connections, read-only
      - concurrent_writes: 100 connections, write-heavy
      - mixed_workload: 80% reads, 20% writes
  
  integration_testing:
    tool: k6 + Docker Compose
    scenarios:
      - end_to_end: Full workflow tests
      - chaos: Network failures, service restarts
```

### k6 Load Test Example

```javascript
// load-tests/journal-entry-post.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const journalEntryDuration = new Trend('journal_entry_post_duration');

// Test configuration
export const options = {
  stages: [
    { duration: '2m', target: 50 },   // Ramp up to 50 VUs
    { duration: '5m', target: 50 },   // Stay at 50 VUs
    { duration: '2m', target: 100 },  // Ramp up to 100 VUs
    { duration: '5m', target: 100 },  // Stay at 100 VUs
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: {
    'http_req_duration': ['p(95)<300'],  // 95% of requests < 300ms
    'http_req_failed': ['rate<0.01'],     // Error rate < 1%
    'errors': ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_KEY = __ENV.API_KEY;

export default function () {
  const payload = JSON.stringify({
    tenantId: 'TENANT-001',
    documentType: 'GENERAL_LEDGER',
    documentDate: '2026-02-01',
    postingDate: '2026-02-01',
    fiscalYear: 2026,
    fiscalPeriod: 2,
    companyCode: 'US01',
    currency: 'USD',
    lineItems: [
      {
        accountNumber: '1000',
        debitAmount: 1000.00,
        creditAmount: 0,
        costCenter: 'CC-100',
        description: 'Load test transaction'
      },
      {
        accountNumber: '5000',
        debitAmount: 0,
        creditAmount: 1000.00,
        costCenter: 'CC-100',
        description: 'Load test transaction'
      }
    ]
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${API_KEY}`,
      'X-Tenant-ID': 'TENANT-001',
    },
  };

  const response = http.post(
    `${BASE_URL}/api/v1/financial/journal-entries`,
    payload,
    params
  );

  // Record custom metrics
  journalEntryDuration.add(response.timings.duration);

  // Checks
  const result = check(response, {
    'status is 201': (r) => r.status === 201,
    'response has documentNumber': (r) => JSON.parse(r.body).documentNumber !== undefined,
    'latency < 300ms': (r) => r.timings.duration < 300,
  });

  errorRate.add(!result);

  sleep(1); // 1 second think time
}

// Teardown (optional)
export function teardown(data) {
  // Clean up test data if needed
}
```

### JMeter Test Plan Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="ChiroERP Load Test">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
          </elementProp>
          <elementProp name="TENANT_ID" elementType="Argument">
            <stringProp name="Argument.value">TENANT-001</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- Thread Group -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Journal Entry Posting">
        <intProp name="ThreadGroup.num_threads">100</intProp>
        <intProp name="ThreadGroup.ramp_time">60</intProp>
        <longProp name="ThreadGroup.duration">600</longProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP Request -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="POST Journal Entry">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/financial/journal-entries</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{&#xd;
  "tenantId": "${TENANT_ID}",&#xd;
  "documentType": "GENERAL_LEDGER",&#xd;
  "lineItems": [...]&#xd;
}</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <elementProp name="HTTPsampler.header_manager" elementType="HeaderManager">
            <collectionProp name="HeaderManager.headers">
              <elementProp name="Content-Type" elementType="Header">
                <stringProp name="Header.value">application/json</stringProp>
              </elementProp>
              <elementProp name="X-Tenant-ID" elementType="Header">
                <stringProp name="Header.value">${TENANT_ID}</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
        </HTTPSamplerProxy>
        <hashTree>
          <!-- Response Assertion -->
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Assert 201">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">201</stringProp>
            </collectionProp>
            <intProp name="Assertion.test_type">8</intProp>
          </ResponseAssertion>
        </hashTree>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### Performance Test Scenarios

| Scenario | VUs/TPS | Duration | Purpose | Success Criteria |
|----------|---------|----------|---------|------------------|
| **Smoke Test** | 10 VUs | 2 min | Sanity check after deployment | 0% errors, p95 < SLO |
| **Load Test** | 100 VUs | 10 min | Baseline performance validation | p95 < SLO, 0 timeouts |
| **Stress Test** | 100→500 VUs | 20 min | Find breaking point | Identify degradation point |
| **Spike Test** | 100→1000 VUs | 5 min | Test elasticity/auto-scaling | No crashes, recovery < 2min |
| **Soak Test** | 100 VUs | 4 hours | Detect memory leaks | Stable memory, no degradation |
| **Breakpoint Test** | Ramp to failure | 30 min | Maximum capacity | Document max TPS |
| **Chaos Test** | 100 VUs + failures | 15 min | Resilience validation | Graceful degradation |

### Database Performance Optimization

### Query Optimization Standards

```sql
-- GOOD: Indexed query with selective WHERE clause
EXPLAIN ANALYZE
SELECT je.document_number, je.posting_date, je.net_amount
FROM financial.journal_entries je
WHERE je.tenant_id = 'TENANT-001'
  AND je.fiscal_year = 2026
  AND je.fiscal_period = 2
  AND je.posting_status = 'POSTED'
ORDER BY je.posting_date DESC
LIMIT 100;

-- Index: CREATE INDEX idx_je_tenant_fy_fp_status 
--        ON journal_entries(tenant_id, fiscal_year, fiscal_period, posting_status);

-- BAD: Unindexed function in WHERE clause
SELECT * FROM journal_entries
WHERE LOWER(document_number) = 'je-123';  -- Prevents index usage

-- BETTER: Case-insensitive index
CREATE INDEX idx_je_doc_num_ci ON journal_entries(LOWER(document_number));
```

### Index Strategy

```sql
-- 1. Tenant isolation (ALWAYS first column in multi-tenant indexes)
CREATE INDEX idx_invoice_tenant ON invoices(tenant_id);

-- 2. Foreign keys (for joins and referential integrity checks)
CREATE INDEX idx_invoice_customer ON invoices(customer_id);

-- 3. Query patterns (cover common WHERE/ORDER BY columns)
CREATE INDEX idx_invoice_tenant_date ON invoices(tenant_id, invoice_date DESC);
CREATE INDEX idx_invoice_tenant_status ON invoices(tenant_id, invoice_status);

-- 4. Covering indexes (include frequently selected columns)
CREATE INDEX idx_invoice_tenant_date_covering 
ON invoices(tenant_id, invoice_date DESC) 
INCLUDE (invoice_number, customer_id, total_amount);

-- 5. Partial indexes (for specific conditions)
CREATE INDEX idx_invoice_unpaid 
ON invoices(tenant_id, due_date) 
WHERE invoice_status IN ('POSTED', 'PARTIALLY_PAID');

-- 6. Expression indexes (for computed columns)
CREATE INDEX idx_invoice_amount_outstanding 
ON invoices((total_amount - paid_amount)) 
WHERE (total_amount - paid_amount) > 0;
```

### Connection Pooling Configuration

```kotlin
// application.yml
quarkus:
  datasource:
    jdbc:
      # Connection pool sizing: connections = ((core_count * 2) + effective_spindle_count)
      # For 4 cores + 1 SSD: (4 * 2) + 1 = 9, use 10-20 for safety margin
      min-size: 10
      max-size: 20
      
      # Connection lifecycle
      initial-size: 10
      max-lifetime: 30m          # Close connections after 30 min
      idle-timeout: 10m          # Remove idle connections after 10 min
      
      # Performance tuning
      acquisition-timeout: 5s    # Max wait for connection
      leak-detection-interval: 30s
      validation-query: "SELECT 1"
      validate-on-borrow: false  # Trust pool, validate periodically instead
      background-validation-interval: 2m

// Connection pool monitoring
@ApplicationScoped
class DataSourceMetrics(
    @Inject private val dataSource: AgroalDataSource,
    private val meterRegistry: MeterRegistry
) {
    
    @PostConstruct
    fun registerMetrics() {
        Gauge.builder("datasource.connections.active") { 
            dataSource.metrics.activeCount() 
        }.register(meterRegistry)
        
        Gauge.builder("datasource.connections.idle") { 
            dataSource.metrics.idleCount() 
        }.register(meterRegistry)
        
        Gauge.builder("datasource.connections.max") { 
            dataSource.metrics.maxUsedCount() 
        }.register(meterRegistry)
        
        Gauge.builder("datasource.connections.awaiting") { 
            dataSource.metrics.awaitingCount() 
        }.register(meterRegistry)
    }
}
```

### Query Performance Monitoring

```kotlin
@ApplicationScoped
class QueryPerformanceInterceptor : StatementInspector {
    
    private val slowQueryThreshold = Duration.ofMillis(100)
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun inspect(sql: String): String {
        val startTime = System.nanoTime()
        
        return sql.also {
            val duration = Duration.ofNanos(System.nanoTime() - startTime)
            
            if (duration > slowQueryThreshold) {
                logger.warn(
                    "Slow query detected: {}ms - SQL: {}",
                    duration.toMillis(),
                    sql.take(500) // Truncate for logging
                )
                
                // Emit metric
                meterRegistry.counter(
                    "database.slow.queries",
                    "threshold", slowQueryThreshold.toMillis().toString()
                ).increment()
            }
        }
    }
}

// PostgreSQL slow query log configuration
// postgresql.conf
log_min_duration_statement = 100  # Log queries > 100ms
log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '
log_statement = 'none'
log_duration = off
log_lock_waits = on
```

### Caching Strategy

### Multi-Layer Caching Architecture

```
┌─────────────────────────────────────────────────────┐
│                  Application Layer                   │
│  ┌──────────────────┐      ┌──────────────────┐    │
│  │  L1: Caffeine    │      │  L2: Redis       │    │
│  │  (In-Memory)     │─────▶│  (Distributed)   │    │
│  │  TTL: 5-60s      │      │  TTL: 5-60min    │    │
│  └──────────────────┘      └──────────────────┘    │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│              L3: Database Query Cache                │
│  PostgreSQL Materialized Views + Refresh Jobs       │
│  TTL: 5-15 minutes                                   │
└─────────────────────────────────────────────────────┘
```

### Caffeine (L1) Cache Implementation

```kotlin
@ApplicationScoped
class CacheConfiguration {
    
    @Produces
    @Named("authorizationCache")
    fun authorizationCache(): Cache<String, AuthorizationResult> {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build()
    }
    
    @Produces
    @Named("customerCache")
    fun customerCache(): Cache<String, Customer> {
        return Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(Duration.ofMinutes(15))
            .recordStats()
            .build()
    }
    
    @Produces
    @Named("productCache")
    fun productCache(): Cache<String, Product> {
        return Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build()
    }
}

@ApplicationScoped
class CustomerService(
    @Named("customerCache") private val cache: Cache<String, Customer>
) {
    
    suspend fun getCustomer(tenantId: String, customerId: String): Customer {
        val cacheKey = "$tenantId:$customerId"
        
        return cache.get(cacheKey) {
            // Cache miss - fetch from database
            repository.findByIdAndTenant(customerId, tenantId)
                ?: throw CustomerNotFoundException(customerId)
        }
    }
    
    suspend fun updateCustomer(customer: Customer) {
        repository.save(customer)
        
        // Invalidate cache
        val cacheKey = "${customer.tenantId}:${customer.customerId}"
        cache.invalidate(cacheKey)
        
        // Publish cache invalidation event for other instances
        eventPublisher.publish(CacheInvalidationEvent(
            cacheType = "customer",
            key = cacheKey
        ))
    }
}
```

### Redis (L2) Cache Implementation

```kotlin
@ApplicationScoped
class RedisCustomerCache(
    private val redisClient: RedisClient
) {
    
    private val ttl = Duration.ofMinutes(15)
    
    suspend fun get(tenantId: String, customerId: String): Customer? {
        val key = "customer:$tenantId:$customerId"
        val json = redisClient.get(key) ?: return null
        return Json.decodeFromString<Customer>(json)
    }
    
    suspend fun set(customer: Customer) {
        val key = "customer:${customer.tenantId}:${customer.customerId}"
        val json = Json.encodeToString(customer)
        redisClient.setex(key, ttl.seconds.toInt(), json)
    }
    
    suspend fun invalidate(tenantId: String, customerId: String) {
        val key = "customer:$tenantId:$customerId"
        redisClient.del(key)
    }
    
    suspend fun invalidatePattern(pattern: String) {
        // Use with caution - KEYS command blocks Redis in production
        // Better: use Redis keyspace notifications or maintain key sets
        val keys = redisClient.keys(pattern)
        if (keys.isNotEmpty()) {
            redisClient.del(*keys.toTypedArray())
        }
    }
}

// Cache warming on startup
@ApplicationScoped
class CacheWarmer(
    private val customerCache: RedisCustomerCache,
    private val productCache: RedisProductCache
) {
    
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    fun warmCaches() {
        logger.info("Starting cache warming...")
        
        // Warm top 1000 customers
        val topCustomers = customerRepository.findTopByOrderVolume(limit = 1000)
        topCustomers.forEach { customerCache.set(it) }
        
        // Warm active products
        val activeProducts = productRepository.findActive()
        activeProducts.forEach { productCache.set(it) }
        
        logger.info("Cache warming completed")
    }
}
```

### Cache Invalidation Strategy

```kotlin
// Event-driven cache invalidation
@ApplicationScoped
class CacheInvalidationHandler(
    private val redisClient: RedisClient,
    @Named("customerCache") private val l1Cache: Cache<String, Customer>
) {
    
    @Incoming("cache-invalidation-events")
    suspend fun handleInvalidation(event: CacheInvalidationEvent) {
        when (event.cacheType) {
            "customer" -> {
                // Invalidate L1 cache
                l1Cache.invalidate(event.key)
                
                // Invalidate L2 cache
                redisClient.del("customer:${event.key}")
            }
            "product" -> {
                // Similar logic
            }
        }
        
        logger.debug("Cache invalidated: ${event.cacheType}:${event.key}")
    }
}

// Write-through cache pattern
@ApplicationScoped
class CustomerServiceWithCache(
    private val repository: CustomerRepository,
    private val l1Cache: Cache<String, Customer>,
    private val l2Cache: RedisCustomerCache,
    private val eventPublisher: EventPublisher
) {
    
    suspend fun updateCustomer(customer: Customer) {
        // 1. Update database
        repository.save(customer)
        
        // 2. Update L2 cache (write-through)
        l2Cache.set(customer)
        
        // 3. Invalidate L1 cache (safer than updating)
        val cacheKey = "${customer.tenantId}:${customer.customerId}"
        l1Cache.invalidate(cacheKey)
        
        // 4. Publish invalidation event for other instances
        eventPublisher.publish(CacheInvalidationEvent(
            cacheType = "customer",
            key = cacheKey
        ))
    }
}
```

### Cache Metrics & Monitoring

```kotlin
@ApplicationScoped
class CacheMetrics(
    @Named("authorizationCache") private val authzCache: Cache<String, AuthorizationResult>,
    @Named("customerCache") private val customerCache: Cache<String, Customer>,
    private val meterRegistry: MeterRegistry
) {
    
    @Scheduled(every = "30s")
    fun recordCacheMetrics() {
        recordCacheStats("authorization", authzCache.stats())
        recordCacheStats("customer", customerCache.stats())
    }
    
    private fun recordCacheStats(cacheName: String, stats: CacheStats) {
        meterRegistry.gauge(
            "cache.size",
            Tags.of("cache", cacheName),
            stats.requestCount()
        )
        
        meterRegistry.gauge(
            "cache.hit.ratio",
            Tags.of("cache", cacheName),
            stats.hitRate()
        )
        
        meterRegistry.gauge(
            "cache.miss.ratio",
            Tags.of("cache", cacheName),
            stats.missRate()
        )
        
        meterRegistry.gauge(
            "cache.eviction.count",
            Tags.of("cache", cacheName),
            stats.evictionCount().toDouble()
        )
        
        meterRegistry.gauge(
            "cache.load.success.count",
            Tags.of("cache", cacheName),
            stats.loadSuccessCount().toDouble()
        )
        
        meterRegistry.gauge(
            "cache.load.failure.count",
            Tags.of("cache", cacheName),
            stats.loadFailureCount().toDouble()
        )
    }
}
```

### JVM Performance Tuning

### JVM Configuration Standards

```bash
# G1GC configuration (recommended for heap > 4GB)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1ReservePercent=10
-XX:InitiatingHeapOccupancyPercent=45
-XX:G1HeapRegionSize=16m

# Heap sizing (container-aware)
-XX:InitialRAMPercentage=50.0
-XX:MaxRAMPercentage=80.0
-XX:MinRAMPercentage=50.0

# GC logging
-Xlog:gc*:file=/var/log/app/gc.log:time,uptime,level,tags:filecount=5,filesize=10M

# Thread stack size
-Xss512k

# String deduplication (reduces memory for duplicate strings)
-XX:+UseStringDeduplication

# Diagnostic flags (development/staging only)
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/app/heapdump.hprof
-XX:+UnlockDiagnosticVMOptions
-XX:+LogVMOutput
-XX:LogFile=/var/log/app/jvm.log
```

### Container Resource Limits

```yaml
# kubernetes/deployment.yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "2000m"

# JVM will automatically detect container limits (Java 11+)
# No need for explicit -Xmx/-Xms flags
```

### Performance Profiling

```kotlin
// JFR (Java Flight Recorder) configuration
// Add to JVM args: -XX:StartFlightRecording=duration=60s,filename=/tmp/recording.jfr

// Programmatic JFR control
@ApplicationScoped
class PerformanceProfiler {
    
    fun startProfiling(durationSeconds: Int = 60): Path {
        val recording = FlightRecorderMXBean.getFlightRecorderMXBean()
            .newRecording()
        
        recording.apply {
            setName("performance-profile-${Instant.now()}")
            setDuration(Duration.ofSeconds(durationSeconds.toLong()))
            setMaxSize(100 * 1024 * 1024) // 100 MB
            setDestination("/tmp/profile-${UUID.randomUUID()}.jfr")
            start()
        }
        
        return Paths.get(recording.destination.toString())
    }
}

// Async profiler integration (for CPU profiling)
// docker run -d --name async-profiler \
//   -v /tmp:/tmp \
//   async-profiler:latest \
//   -d 60 -f /tmp/flamegraph.html [PID]
```

### Scalability & Capacity Planning

### Horizontal Scaling Strategy

| Service Type | Scaling Method | Trigger | Min Replicas | Max Replicas | Target Utilization |
|--------------|----------------|---------|--------------|--------------|-------------------|
| Stateless API | HPA (CPU/Memory) | 70% CPU | 3 | 20 | 70% CPU |
| Event Consumer | HPA (Queue Lag) | 5000 lag | 2 | 15 | 1000 lag/pod |
| Batch Processor | Manual/Scheduled | Time-based | 1 | 10 | N/A |
| Database | Vertical + Read Replicas | Connection pool | 1 primary | 5 replicas | 70% connections |

### Kubernetes HPA Configuration

```yaml
# hpa/financial-accounting-api.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: financial-accounting-api
  namespace: chiroerp
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: financial-accounting-api
  minReplicas: 3
  maxReplicas: 20
  metrics:
    # CPU-based scaling
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    
    # Memory-based scaling
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
    
    # Custom metric: request rate
    - type: Pods
      pods:
        metric:
          name: http_requests_per_second
        target:
          type: AverageValue
          averageValue: "100"
  
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300  # Wait 5 min before scale down
      policies:
        - type: Percent
          value: 50  # Scale down max 50% of pods at once
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0  # Scale up immediately
      policies:
        - type: Percent
          value: 100  # Double pods if needed
          periodSeconds: 30
        - type: Pods
          value: 4  # Add max 4 pods at once
          periodSeconds: 30
      selectPolicy: Max
```

### KEDA (Event-Driven Autoscaling) for Kafka Consumers

```yaml
# keda/financial-consumer-scaler.yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: financial-event-consumer
  namespace: chiroerp
spec:
  scaleTargetRef:
    name: financial-event-consumer
  minReplicaCount: 2
  maxReplicaCount: 15
  pollingInterval: 30
  cooldownPeriod: 300
  
  triggers:
    # Scale based on Kafka consumer lag
    - type: kafka
      metadata:
        bootstrapServers: kafka-cluster:9092
        consumerGroup: financial-accounting-consumer-group
        topic: financial.journal-entry.events
        lagThreshold: "1000"  # Scale up if lag > 1000 per partition
        offsetResetPolicy: latest
    
    # Fallback CPU trigger
    - type: cpu
      metricType: Utilization
      metadata:
        value: "80"
```

### Capacity Planning Model

```kotlin
data class CapacityForecast(
    val service: String,
    val currentMonthly: Metrics,
    val forecastedMonthly: Metrics,
    val growthRate: Double,
    val capacityHeadroom: Double,
    val recommendedAction: String
)

data class Metrics(
    val requests: Long,
    val avgLatency: Duration,
    val p95Latency: Duration,
    val errorRate: Double,
    val cpuUtilization: Double,
    val memoryUtilization: Double
)

@ApplicationScoped
class CapacityPlanner {
    
    fun forecastCapacity(
        service: String,
        historicalData: List<Metrics>,
        growthRate: Double = 0.20  // Default 20% MoM growth
    ): CapacityForecast {
        
        val current = historicalData.last()
        
        // Linear growth forecast
        val forecastedRequests = (current.requests * (1 + growthRate)).toLong()
        
        // Estimate resource needs based on current utilization
        val forecastedCpu = current.cpuUtilization * (1 + growthRate)
        val forecastedMemory = current.memoryUtilization * (1 + growthRate)
        
        val forecasted = Metrics(
            requests = forecastedRequests,
            avgLatency = current.avgLatency,
            p95Latency = current.p95Latency,
            errorRate = current.errorRate,
            cpuUtilization = forecastedCpu,
            memoryUtilization = forecastedMemory
        )
        
        // Calculate headroom (target 30% headroom for spikes)
        val cpuHeadroom = 1.0 - (forecastedCpu / 70.0)  // 70% target
        val memoryHeadroom = 1.0 - (forecastedMemory / 80.0)  // 80% target
        val capacityHeadroom = minOf(cpuHeadroom, memoryHeadroom)
        
        val recommendation = when {
            capacityHeadroom < 0.10 -> "URGENT: Scale up immediately. <10% headroom remaining."
            capacityHeadroom < 0.20 -> "WARNING: Plan scaling within 2 weeks. <20% headroom."
            capacityHeadroom < 0.30 -> "ADVISORY: Monitor closely. Approaching target utilization."
            else -> "HEALTHY: Sufficient capacity for forecasted growth."
        }
        
        return CapacityForecast(
            service = service,
            currentMonthly = current,
            forecastedMonthly = forecasted,
            growthRate = growthRate,
            capacityHeadroom = capacityHeadroom,
            recommendedAction = recommendation
        )
    }
    
    fun calculateRequiredReplicas(
        currentReplicas: Int,
        currentCpuUtilization: Double,
        targetCpuUtilization: Double = 70.0
    ): Int {
        // Formula: new_replicas = current_replicas * (current_utilization / target_utilization)
        val requiredReplicas = ceil(
            currentReplicas * (currentCpuUtilization / targetCpuUtilization)
        ).toInt()
        
        return requiredReplicas.coerceAtLeast(currentReplicas)
    }
}

// Quarterly capacity review report
@ApplicationScoped
class CapacityReportGenerator {
    
    @Scheduled(cron = "0 0 9 1 */3 *") // 9 AM on 1st of every 3rd month
    fun generateQuarterlyReport() {
        val services = listOf(
            "financial-accounting-api",
            "ap-api",
            "ar-api",
            "sales-order-api",
            "inventory-api"
        )
        
        val report = services.map { service ->
            val historicalData = metricsRepository.getLastNMonths(service, 3)
            capacityPlanner.forecastCapacity(service, historicalData)
        }
        
        // Generate report
        val markdown = buildString {
            appendLine("# Quarterly Capacity Planning Report")
            appendLine("**Date**: ${LocalDate.now()}")
            appendLine()
            
            report.forEach { forecast ->
                appendLine("## ${forecast.service}")
                appendLine("- **Current Monthly Requests**: ${forecast.currentMonthly.requests:N0}")
                appendLine("- **Forecasted Monthly Requests**: ${forecast.forecastedMonthly.requests:N0}")
                appendLine("- **Growth Rate**: ${forecast.growthRate * 100}%")
                appendLine("- **Capacity Headroom**: ${forecast.capacityHeadroom * 100}%")
                appendLine("- **Recommendation**: ${forecast.recommendedAction}")
                appendLine()
            }
        }
        
        // Send report to Slack/Email
        notificationService.sendReport(markdown)
    }
}
```

### Database Scaling Strategy

```yaml
# PostgreSQL scaling tiers
database_scaling:
  tier_1_small:
    cpu: 2
    memory: 8GB
    storage: 100GB
    iops: 3000
    max_connections: 100
    use_case: Development, small tenants
  
  tier_2_medium:
    cpu: 4
    memory: 16GB
    storage: 500GB
    iops: 6000
    max_connections: 200
    use_case: Production, standard tenants
  
  tier_3_large:
    cpu: 8
    memory: 32GB
    storage: 1TB
    iops: 12000
    max_connections: 500
    use_case: High-volume tenants
  
  tier_4_xlarge:
    cpu: 16
    memory: 64GB
    storage: 2TB
    iops: 20000
    max_connections: 1000
    use_case: Enterprise tenants

# Read replica configuration
read_replicas:
  count: 3
  lag_threshold: 5s
  routing:
    - pattern: "SELECT.*FROM.*reporting.*"
      target: read_replica
    - pattern: "SELECT.*FOR UPDATE"
      target: primary
    - default: primary
```

### Cost Optimization Model

```kotlin
data class CostForecast(
    val service: String,
    val currentMonthlyCost: BigDecimal,
    val forecastedMonthlyCost: BigDecimal,
    val costPerRequest: BigDecimal,
    val optimizationOpportunities: List<String>
)

@ApplicationScoped
class CostOptimizer {
    
    fun analyzeCosts(service: String, metrics: Metrics): CostForecast {
        // AWS pricing (example)
        val costPerPodHour = BigDecimal("0.05") // $0.05/pod/hour
        val costPerGBStorage = BigDecimal("0.10") // $0.10/GB/month
        val costPerMillionRequests = BigDecimal("0.20")
        
        val currentReplicas = getCurrentReplicas(service)
        val hoursPerMonth = 24 * 30
        
        val computeCost = costPerPodHour * currentReplicas.toBigDecimal() * hoursPerMonth.toBigDecimal()
        val requestCost = (metrics.requests.toBigDecimal() / BigDecimal("1_000_000")) * costPerMillionRequests
        val currentCost = computeCost + requestCost
        
        // Analyze optimization opportunities
        val opportunities = mutableListOf<String>()
        
        if (metrics.cpuUtilization < 30) {
            opportunities.add("LOW_UTILIZATION: CPU < 30%. Consider reducing replica count or downsizing instances.")
        }
        
        if (metrics.memoryUtilization < 40) {
            opportunities.add("OVERPROVISIONED_MEMORY: Memory < 40%. Reduce memory requests by 25%.")
        }
        
        if (metrics.avgLatency < Duration.ofMillis(50)) {
            opportunities.add("OVERPROVISIONED_COMPUTE: Avg latency < 50ms. May be able to reduce resources.")
        }
        
        // Forecasted cost with 20% growth
        val forecastedCost = currentCost * BigDecimal("1.20")
        val costPerRequest = currentCost / metrics.requests.toBigDecimal()
        
        return CostForecast(
            service = service,
            currentMonthlyCost = currentCost,
            forecastedMonthlyCost = forecastedCost,
            costPerRequest = costPerRequest,
            optimizationOpportunities = opportunities
        )
    }
}
```

## Alternatives Considered

### Alternative 1: Commercial APM Tools (Datadog, New Relic, Dynatrace)
- **Approach**: Use fully managed Application Performance Monitoring (APM) SaaS platforms for metrics, traces, logs, and alerting.
- **Pros**:
  - Turnkey solution (minimal setup)
  - Rich out-of-box dashboards and integrations
  - AI-powered anomaly detection
  - Vendor support and SLAs
  - Excellent trace visualization (distributed tracing)
- **Cons**:
  - High costs ($$$ per host per month, scales with data volume)
  - Vendor lock-in (proprietary agents, APIs)
  - Data privacy concerns (metrics sent to third-party)
  - Limited customization for domain-specific KPIs
  - Less control over data retention policies
- **Decision**: Rejected for primary monitoring. Considered for supplementary observability in Phase 5+ if customer contracts justify cost. Deferred pending budget review.

### Alternative 2: Open-Source Stack (Prometheus + Grafana + Loki + Tempo) - Selected Approach
- **Approach**: Self-hosted observability stack. Prometheus for metrics, Grafana for dashboards, Loki for logs, Tempo for traces. Micrometer for instrumentation.
- **Pros**:
  - Full control (on-prem or cloud deployment)
  - No per-host licensing costs (infrastructure costs only)
  - Mature ecosystem (community support, extensive integrations)
  - PromQL for flexible queries
  - Open standards (OpenTelemetry, OTLP)
  - Data sovereignty (no third-party data sharing)
- **Cons**:
  - Requires operational expertise (deployment, tuning, scaling)
  - Infrastructure management overhead (HA setup, storage, backups)
  - Limited out-of-box AI features (manual threshold tuning)
  - Steeper learning curve for teams unfamiliar with PromQL
- **Decision**: Selected. Aligns with cost-conscious startup phase and provides sufficient functionality for Phase 4-5 requirements. Open-source flexibility supports future customization.

### Alternative 3: Cloud-Native Monitoring (AWS CloudWatch, Azure Monitor, GCP Cloud Monitoring)
- **Approach**: Use cloud provider's native monitoring services. CloudWatch for AWS-hosted services, Azure Monitor for Azure deployments.
- **Pros**:
  - Native integration with cloud infrastructure (auto-discovery)
  - Simple setup for cloud resources
  - Pay-per-use pricing
  - Built-in alerting and dashboards
- **Cons**:
  - Cloud vendor lock-in
  - Limited cross-cloud visibility (cannot monitor AWS + Azure in single pane)
  - Poor support for custom business metrics
  - Weak distributed tracing capabilities
  - High costs at scale (per metric, per log GB)
- **Decision**: Rejected for primary monitoring. Used for cloud infrastructure monitoring (EC2 health, RDS metrics) as complementary layer to Prometheus/Grafana.

### Alternative 4: Hybrid Approach (Prometheus + Commercial APM for Critical Services)
- **Approach**: Use Prometheus/Grafana for most services, but deploy Datadog/New Relic APM on Tier 1 critical services (e.g., Payment Processing, GL Posting).
- **Pros**:
  - Cost optimization (pay only for critical services)
  - Best-of-breed tools for different use cases
  - Vendor support where it matters most
- **Cons**:
  - Operational complexity (two monitoring stacks)
  - Training overhead (team learns two toolsets)
  - Inconsistent dashboards and alerting workflows
  - Data correlation challenges across stacks
- **Decision**: Deferred to Phase 6+ as optimization. Simpler to standardize on Prometheus/Grafana initially. Re-evaluate if Tier 1 services require advanced APM features (code-level profiling, deployment tracking).

### Alternative 5: Custom Monitoring Platform (Build In-House)
- **Approach**: Develop custom monitoring and alerting platform tailored to ERP domain. Custom metrics collectors, dashboards, and alerting engine.
- **Pros**:
  - Perfect fit for ERP-specific KPIs (e.g., posting latency by account type)
  - Full control over features and roadmap
  - No licensing costs
- **Cons**:
  - Massive development effort (12-18 months, 5+ FTE)
  - Opportunity cost (delays core ERP features)
  - Maintenance burden (bug fixes, updates)
  - Reinventing mature OSS solutions (Prometheus, Grafana)
- **Decision**: Rejected. Not a core competency. Leverage existing OSS tools and focus development effort on ERP features.

## Consequences
### Positive
- Clear performance expectations aligned with enterprise ERP standards
- Proactive detection of bottlenecks and regressions
- Supports predictable scaling and SLA commitments

### Negative / Risks
- Requires investment in performance tooling and environments
- Ongoing maintenance of benchmarks and baselines
- Potential false positives if thresholds are poorly tuned

### Neutral
- Some legacy flows may require phased optimization

## Compliance

### SOX Compliance: Performance Monitoring for Financial SLAs
- **Transaction Latency Monitoring**: Financial transactions (GL posting, invoice creation, payment processing) monitored for SLA compliance (<200ms P95). Violations logged for SOX internal controls testing.
- **System Availability Audit**: 99.9% uptime SLA for financial modules. Downtime incidents documented with root cause analysis and remediation evidence.
- **Change Impact Analysis**: Performance benchmarks run before/after deployments to financial modules. Regressions >10% require rollback or hotfix.
- **Audit Trail**: All performance alerts and incidents logged with resolution time. Logs retained for 7 years per SOX Section 802.
- **Capacity Planning**: Quarterly capacity reviews ensure infrastructure can support financial period close workloads (month-end, quarter-end, year-end). Forecasts documented for audit.
- **Incident Response SLA**: Critical performance incidents (P95 latency >500ms for financial services) escalated within 15 minutes. Resolution SLA: <2 hours.

### SRE Principles: Error Budgets and SLOs
- **Error Budget Policy**: Each service has monthly error budget (e.g., 99.9% availability = 43 minutes downtime per month). Budget exhaustion triggers deployment freeze until performance restored.
- **SLO Dashboard**: Real-time SLO compliance dashboard for all Tier 1 services. Displays current availability, latency, and error rate vs. targets.
- **Blameless Postmortems**: All SLO violations trigger postmortem process. Focus on systemic issues, not individual blame. Postmortems published internally with action items.
- **Service Tiering**: Tier 1 services (Payment, GL) have stricter SLOs (99.9%) than Tier 2 (Reporting, 99.5%) or Tier 3 (Batch Jobs, 99.0%). Resource allocation prioritized by tier.
- **Reliability Reviews**: Quarterly SRE reviews evaluate SLO trends, error budget burn rate, and incident patterns. Informs capacity planning and architecture improvements.

### GDPR Compliance: Monitoring Data Privacy
- **Metrics Anonymization**: Performance metrics do not include PII (customer names, email addresses). Only anonymized identifiers (e.g., `tenant_id`, `transaction_id`).
- **Log Retention**: Performance logs retained per ADR-015 data lifecycle policies. Logs containing PII masked after 90 days, full logs deleted after 1 year unless legally required.
- **Access Controls**: Monitoring dashboards restricted to operations team. No customer data visible in metrics (only aggregated counts, latencies).
- **Trace Sampling**: Distributed traces sampled at 1% to minimize PII collection. Full tracing enabled only for incident investigation (with approval).

### Capacity Planning and Forecasting
- **Growth Projections**: Capacity forecasts model 3x annual growth (tenant count, transaction volume, data size). Infrastructure scaling planned quarterly.
- **Performance Testing**: Load tests simulate peak workloads (period close, tax season) before deployment. Results documented for capacity review.
- **Resource Efficiency**: Monitor cost per transaction ($/txn) to ensure scalability. Target: <$0.01 per financial transaction at scale.

### Service Level Objectives (SLOs)
- **Monitoring System Availability**: `99.95%` uptime for Prometheus/Grafana stack. Monitoring failure escalated immediately (cannot detect production issues).
- **Metrics Collection Latency**: `<15 seconds` from metric generation to availability in Prometheus (scrape interval: 10s, propagation: 5s).
- **Alert Latency**: `<2 minutes` from threshold breach to PagerDuty notification (P95). Critical alerts (Tier 1 services) target <30 seconds.
- **Dashboard Load Time**: `<3 seconds` for standard Grafana dashboards. Heavy dashboards (>50 panels) target <10 seconds.
- **Query Performance**: `<5 seconds` for PromQL queries powering dashboards (95th percentile). Slow queries logged for optimization.
- **Alert Accuracy**: `<5%` false positive rate on critical alerts. Noisy alerts tuned or disabled to prevent alert fatigue.
- **Incident Detection**: `>95%` of production incidents detected by automated alerts before customer reports. Measure via postmortem analysis.
- **Data Retention**: Prometheus metrics retained for 30 days (high-resolution), 1 year (downsampled). Logs retained per ADR-015 policies.

## Implementation Plan
### Implementation Plan (Not Started)

### Phase 1: Foundation (Months 1-2)
**Objective**: Establish basic monitoring and alerting infrastructure

**Deliverables**:
- ✅ Deploy Prometheus + Grafana stack
- ✅ Implement Micrometer instrumentation in all services
- ✅ Create 5 core dashboards (Service Overview, Database, Kafka, JVM, Business Metrics)
- ✅ Define and document SLIs/SLOs for Tier 1 services
- ✅ Configure basic alerting rules (latency, error rate, availability)
- ✅ Set up PagerDuty integration

**Success Criteria**:
- All services emitting metrics to Prometheus
- Dashboards accessible and updating in real-time
- Alerts firing correctly during synthetic test failures
- On-call rotation established

### Phase 2: Observability Enhancement (Months 3-4)
**Objective**: Add distributed tracing and advanced logging

**Deliverables**:
- ✅ Deploy Jaeger/Tempo for distributed tracing
- ✅ Instrument services with OpenTelemetry
- ✅ Implement structured logging with Fluentd + Elasticsearch
- ✅ Create trace-aware dashboards linking logs + traces + metrics
- ✅ Add business event tracking (orders, invoices, payments)
- ✅ Implement error budget tracking and reporting

**Success Criteria**:
- End-to-end traces visible across service boundaries
- Logs correlated with trace IDs
- Business metrics dashboards operational
- Error budget status visible in dashboards

### Phase 3: Performance Testing Framework (Months 5-6)
**Objective**: Build automated performance testing pipeline

**Deliverables**:
- ✅ Set up k6 load testing framework
- ✅ Create load test scenarios for 20 critical workflows
- ✅ Integrate performance tests into CI/CD pipeline
- ✅ Establish performance regression gates
- ✅ Create synthetic monitoring for production
- ✅ Document performance baselines for all services

**Success Criteria**:
- Automated load tests running on every release candidate
- Performance regression detected and blocked in CI
- Baseline performance metrics documented
- Synthetic monitors covering critical user journeys

### Phase 4: Database & Caching Optimization (Months 7-9)
**Objective**: Optimize database performance and implement caching

**Deliverables**:
- ✅ Implement slow query logging and monitoring
- ✅ Create index optimization strategy and apply to all schemas
- ✅ Deploy Redis cluster for L2 caching
- ✅ Implement Caffeine (L1) cache in all API services
- ✅ Add cache hit ratio monitoring and alerting
- ✅ Optimize top 20 slowest queries
- ✅ Implement connection pooling best practices

**Success Criteria**:
- p95 query latency < 50ms
- Cache hit ratio > 80% for customer/product lookups
- Connection pool utilization < 70%
- Zero slow query alerts (>100ms) during business hours

### Phase 5: Scalability & HPA (Months 10-11)
**Objective**: Implement auto-scaling and capacity planning

**Deliverables**:
- ✅ Configure HPA for all stateless services
- ✅ Implement KEDA for Kafka consumer scaling
- ✅ Create capacity planning model and forecasting tool
- ✅ Establish quarterly capacity review process
- ✅ Implement cost optimization recommendations
- ✅ Test auto-scaling under synthetic load

**Success Criteria**:
- Services auto-scale based on load within 2 minutes
- Capacity forecasts generated automatically
- Cost per request tracked and optimized
- Successfully handled 5x load spike without manual intervention

### Phase 6: Advanced Optimization (Months 12-14)
**Objective**: JVM tuning, profiling, and advanced diagnostics

**Deliverables**:
- ✅ JVM tuning for all production services (G1GC optimization)
- ✅ Implement JFR profiling and analysis
- ✅ Add async profiler for CPU flamegraphs
- ✅ Optimize GC pause times (p95 < 100ms)
- ✅ Implement heap dump analysis on OOM
- ✅ Thread pool tuning and monitoring

**Success Criteria**:
- GC pause time p95 < 100ms
- Heap utilization stable at 60-70%
- No OOM errors in production
- CPU flamegraphs available on-demand

### Phase 7: Production Hardening (Months 15-16)
**Objective**: Chaos testing and resilience validation

**Deliverables**:
- ✅ Implement chaos testing framework (Chaos Mesh)
- ✅ Run chaos experiments (pod failures, network latency, resource exhaustion)
- ✅ Validate SLO compliance during chaos tests
- ✅ Document performance characteristics under failure scenarios
- ✅ Create runbooks for performance incidents
- ✅ Establish SRE best practices documentation

**Success Criteria**:
- Services maintain SLOs during pod failures
- Graceful degradation demonstrated under resource constraints
- Mean time to detection (MTTD) < 2 minutes
- Mean time to resolution (MTTR) < 30 minutes for performance incidents

### Phase 8: Continuous Improvement (Months 17+)
**Objective**: Ongoing optimization and refinement

**Activities**:
- Monthly SLO review and adjustment
- Quarterly capacity planning reviews
- Bi-annual performance benchmark updates
- Continuous cost optimization
- Performance tuning based on production telemetry
- Regular chaos testing (monthly)

## References

### Related ADRs
- ADR-008 (CI/CD Resilience), ADR-010 (Validation), ADR-016 (Analytics)

### External References
- SAP analogs: ST03N performance monitor, EarlyWatch Alerts
