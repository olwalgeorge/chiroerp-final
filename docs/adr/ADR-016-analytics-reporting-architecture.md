# ADR-016: Analytics & Reporting Architecture

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Data Platform Team, Product Team
**Tier**: Advanced
**Tags**: analytics, reporting, bi, data-warehouse, dashboards

## Context
SAP-grade ERPs provide embedded analytics and enterprise reporting (financial statements, operational KPIs, ad-hoc queries). This ADR defines a standardized analytics/reporting architecture that avoids fragmentation and protects transactional performance while enabling operational and enterprise reporting.

## Decision
Adopt a **dual-layer analytics architecture**: **Operational Reporting** for near-real-time dashboards and **Enterprise Analytics** for historical BI and financial statements. Implementation is **not started**; this ADR defines the standard.

### Reporting Layers
1. **Operational Reporting (OLTP-adjacent)**
   - Read-optimized projections/materialized views per bounded context.
   - Near-real-time KPIs (minutes latency) for operational dashboards.
   - Cached query results for high-traffic dashboards.

2. **Enterprise Analytics (OLAP)**
   - Central **data warehouse** (star/snowflake schemas) fed by CDC/events.
   - Historical and cross-domain analytics (financial consolidation, multi-entity reporting).
   - Support for ad-hoc analysis, trending, forecasting.

### Reporting Layers Architecture

### Layer 1: Operational Reporting (Near Real-Time)

**Purpose**: Provide operational teams with real-time/near-real-time dashboards and KPIs without impacting transactional performance.

**Technology Stack**:
- **Storage**: PostgreSQL materialized views + Redis cache
- **Refresh**: Incremental refresh every 5-15 minutes
- **Latency**: <5 minutes for critical KPIs, <15 minutes for standard KPIs
- **Query Optimization**: Indexed projections, pre-aggregated summaries

**Implementation Pattern**:

```kotlin
// Example: Accounts Receivable Aging Materialized View
@Entity
@Table(name = "mv_ar_aging_summary", schema = "accounts_receivable_reporting")
data class ARAgingSummaryView(
    @Id
    val id: UUID,
    val tenantId: String,
    val customerId: String,
    val customerName: String,
    val totalOutstanding: BigDecimal,
    val current: BigDecimal,
    val days30: BigDecimal,
    val days60: BigDecimal,
    val days90: BigDecimal,
    val over90: BigDecimal,
    val lastUpdated: Instant
)

// Materialized view SQL
"""
CREATE MATERIALIZED VIEW accounts_receivable_reporting.mv_ar_aging_summary AS
SELECT
    gen_random_uuid() as id,
    i.tenant_id,
    i.customer_id,
    c.customer_name,
    SUM(i.amount_outstanding) as total_outstanding,
    SUM(CASE WHEN CURRENT_DATE - i.due_date <= 0 THEN i.amount_outstanding ELSE 0 END) as current,
    SUM(CASE WHEN CURRENT_DATE - i.due_date BETWEEN 1 AND 30 THEN i.amount_outstanding ELSE 0 END) as days_30,
    SUM(CASE WHEN CURRENT_DATE - i.due_date BETWEEN 31 AND 60 THEN i.amount_outstanding ELSE 0 END) as days_60,
    SUM(CASE WHEN CURRENT_DATE - i.due_date BETWEEN 61 AND 90 THEN i.amount_outstanding ELSE 0 END) as days_90,
    SUM(CASE WHEN CURRENT_DATE - i.due_date > 90 THEN i.amount_outstanding ELSE 0 END) as over_90,
    NOW() as last_updated
FROM accounts_receivable.invoice i
JOIN customer_relation.customer c ON i.customer_id = c.id
WHERE i.status IN ('POSTED', 'PARTIALLY_PAID')
GROUP BY i.tenant_id, i.customer_id, c.customer_name;

CREATE UNIQUE INDEX idx_ar_aging_tenant_customer
ON accounts_receivable_reporting.mv_ar_aging_summary(tenant_id, customer_id);
"""

// Scheduled refresh job
@Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
class ARAgingRefreshJob {
    suspend fun refreshARAging() {
        jdbcTemplate.execute(
            "REFRESH MATERIALIZED VIEW CONCURRENTLY accounts_receivable_reporting.mv_ar_aging_summary"
        )

        // Update cache
        redisCache.del("ar:aging:*")
    }
}
```

**Operational Reporting Catalog**:

| Report Name | Source Context | Refresh Interval | Complexity | Users |
|------------|----------------|------------------|------------|-------|
| **AR Aging Summary** | Accounts Receivable | 15 min | Medium | Collections team, CFO |
| **AP Payment Schedule** | Accounts Payable | 15 min | Medium | AP team, Treasury |
| **Inventory Levels** | Inventory Management | 5 min | Low | Warehouse, Procurement |
| **Daily Sales Dashboard** | Sales Orders | 5 min | Low | Sales team, Management |
| **Open Purchase Orders** | Procurement | 15 min | Low | Procurement, Receiving |
| **Cash Position** | Financial Accounting | 15 min | High | Treasury, CFO |
| **Customer Service Tickets** | Customer Relations | 5 min | Low | Support team |
| **Production Status** | Manufacturing | 5 min | Medium | Production managers |

### Layer 2: Enterprise Analytics (Historical BI)

**Purpose**: Cross-domain analytics, historical trends, financial reporting, regulatory compliance, executive dashboards.

**Technology Stack**:
- **Data Warehouse**: PostgreSQL (cost-effective) or Snowflake/BigQuery (cloud-native scale)
- **ETL/ELT**: Apache Kafka + Kafka Connect + dbt (data build tool)
- **Modeling**: Star schema (facts + dimensions) with slowly changing dimensions (SCD Type 2)
- **BI Tools**: Apache Superset (open-source) or Tableau/Power BI (enterprise)
- **Latency**: 15 minutes to 24 hours depending on report type

**Data Warehouse Schema**:

```sql
-- Dimension Tables (Slowly Changing Dimension Type 2)

CREATE TABLE dw.dim_customer (
    customer_key BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    customer_name VARCHAR(200),
    customer_group VARCHAR(50),
    credit_limit NUMERIC(19,2),
    payment_terms VARCHAR(20),
    territory_id VARCHAR(50),
    territory_name VARCHAR(100),
    industry VARCHAR(50),
    customer_status VARCHAR(20),
    -- SCD Type 2 fields
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    is_current BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    -- Audit
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_dim_customer_id ON dw.dim_customer(customer_id, is_current);
CREATE INDEX idx_dim_customer_tenant ON dw.dim_customer(tenant_id, is_current);

CREATE TABLE dw.dim_product (
    product_key BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    product_category VARCHAR(50),
    product_group VARCHAR(50),
    unit_of_measure VARCHAR(20),
    standard_cost NUMERIC(19,4),
    list_price NUMERIC(19,4),
    is_active BOOLEAN,
    -- SCD Type 2
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    is_current BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE dw.dim_date (
    date_key INT PRIMARY KEY,
    full_date DATE NOT NULL,
    day_of_week INT,
    day_name VARCHAR(10),
    day_of_month INT,
    day_of_year INT,
    week_of_year INT,
    month_number INT,
    month_name VARCHAR(10),
    quarter INT,
    year INT,
    fiscal_year INT,
    fiscal_quarter INT,
    fiscal_period INT,
    is_weekend BOOLEAN,
    is_holiday BOOLEAN,
    holiday_name VARCHAR(100)
);

CREATE TABLE dw.dim_time (
    time_key INT PRIMARY KEY,
    hour INT,
    minute INT,
    hour_name VARCHAR(10), -- "08:00", "14:30"
    day_period VARCHAR(10), -- "Morning", "Afternoon", "Evening"
    business_hour BOOLEAN
);

CREATE TABLE dw.dim_company (
    company_key BIGSERIAL PRIMARY KEY,
    company_code VARCHAR(4) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    company_name VARCHAR(200),
    legal_entity VARCHAR(200),
    currency VARCHAR(3),
    country VARCHAR(2),
    tax_id VARCHAR(50),
    fiscal_year_variant VARCHAR(2),
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    is_current BOOLEAN DEFAULT true
);

CREATE TABLE dw.dim_cost_center (
    cost_center_key BIGSERIAL PRIMARY KEY,
    cost_center_id VARCHAR(20) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    cost_center_name VARCHAR(100),
    cost_center_group VARCHAR(50),
    manager_id VARCHAR(50),
    manager_name VARCHAR(100),
    department VARCHAR(50),
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    is_current BOOLEAN DEFAULT true
);

-- Fact Tables

CREATE TABLE dw.fact_invoice (
    invoice_key BIGSERIAL PRIMARY KEY,
    -- Foreign keys to dimensions
    customer_key BIGINT REFERENCES dw.dim_customer(customer_key),
    product_key BIGINT REFERENCES dw.dim_product(product_key),
    date_key INT REFERENCES dw.dim_date(date_key),
    time_key INT REFERENCES dw.dim_time(time_key),
    company_key BIGINT REFERENCES dw.dim_company(company_key),
    -- Degenerate dimensions (IDs from source)
    invoice_id VARCHAR(50) NOT NULL,
    invoice_number VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    -- Measures
    invoice_amount NUMERIC(19,2),
    tax_amount NUMERIC(19,2),
    discount_amount NUMERIC(19,2),
    net_amount NUMERIC(19,2),
    amount_paid NUMERIC(19,2),
    amount_outstanding NUMERIC(19,2),
    -- Dimensions as attributes
    invoice_type VARCHAR(20), -- STANDARD, CREDIT_MEMO, DEBIT_MEMO
    invoice_status VARCHAR(20), -- DRAFT, POSTED, PAID, CANCELLED
    payment_terms VARCHAR(20),
    currency VARCHAR(3),
    -- Dates
    invoice_date DATE,
    due_date DATE,
    posting_date DATE,
    payment_date DATE,
    -- Audit
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_fact_invoice_customer ON dw.fact_invoice(customer_key, date_key);
CREATE INDEX idx_fact_invoice_tenant_date ON dw.fact_invoice(tenant_id, date_key);
CREATE INDEX idx_fact_invoice_status ON dw.fact_invoice(invoice_status, date_key);

CREATE TABLE dw.fact_payment (
    payment_key BIGSERIAL PRIMARY KEY,
    customer_key BIGINT REFERENCES dw.dim_customer(customer_key),
    date_key INT REFERENCES dw.dim_date(date_key),
    company_key BIGINT REFERENCES dw.dim_company(company_key),
    -- Degenerate dimensions
    payment_id VARCHAR(50) NOT NULL,
    payment_number VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    -- Measures
    payment_amount NUMERIC(19,2),
    applied_amount NUMERIC(19,2),
    unapplied_amount NUMERIC(19,2),
    -- Attributes
    payment_method VARCHAR(20), -- CHECK, WIRE, ACH, CREDIT_CARD
    payment_status VARCHAR(20),
    currency VARCHAR(3),
    -- Dates
    payment_date DATE,
    clearing_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE dw.fact_journal_entry (
    journal_entry_key BIGSERIAL PRIMARY KEY,
    company_key BIGINT REFERENCES dw.dim_company(company_key),
    cost_center_key BIGINT REFERENCES dw.dim_cost_center(cost_center_key),
    date_key INT REFERENCES dw.dim_date(date_key),
    -- Degenerate dimensions
    journal_entry_id VARCHAR(50) NOT NULL,
    journal_entry_number VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    line_number INT,
    -- Measures
    debit_amount NUMERIC(19,2),
    credit_amount NUMERIC(19,2),
    net_amount NUMERIC(19,2), -- debit - credit
    -- Attributes
    account_number VARCHAR(20),
    account_name VARCHAR(100),
    document_type VARCHAR(20),
    posting_status VARCHAR(20),
    fiscal_year INT,
    fiscal_period INT,
    currency VARCHAR(3),
    -- Dates
    posting_date DATE,
    document_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_fact_je_company_period ON dw.fact_journal_entry(company_key, fiscal_year, fiscal_period);
CREATE INDEX idx_fact_je_account ON dw.fact_journal_entry(account_number, date_key);

CREATE TABLE dw.fact_sales_order (
    sales_order_key BIGSERIAL PRIMARY KEY,
    customer_key BIGINT REFERENCES dw.dim_customer(customer_key),
    product_key BIGINT REFERENCES dw.dim_product(product_key),
    date_key INT REFERENCES dw.dim_date(date_key),
    company_key BIGINT REFERENCES dw.dim_company(company_key),
    -- Degenerate dimensions
    sales_order_id VARCHAR(50) NOT NULL,
    sales_order_number VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    line_number INT,
    -- Measures
    order_quantity NUMERIC(19,3),
    shipped_quantity NUMERIC(19,3),
    invoiced_quantity NUMERIC(19,3),
    unit_price NUMERIC(19,4),
    line_amount NUMERIC(19,2),
    discount_amount NUMERIC(19,2),
    net_amount NUMERIC(19,2),
    cost_amount NUMERIC(19,2),
    margin_amount NUMERIC(19,2),
    -- Attributes
    order_status VARCHAR(20), -- OPEN, PARTIALLY_SHIPPED, SHIPPED, INVOICED, CANCELLED
    sales_rep_id VARCHAR(50),
    sales_rep_name VARCHAR(100),
    currency VARCHAR(3),
    -- Dates
    order_date DATE,
    requested_date DATE,
    promised_date DATE,
    ship_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE dw.fact_inventory_movement (
    inventory_movement_key BIGSERIAL PRIMARY KEY,
    product_key BIGINT REFERENCES dw.dim_product(product_key),
    date_key INT REFERENCES dw.dim_date(date_key),
    company_key BIGINT REFERENCES dw.dim_company(company_key),
    -- Degenerate dimensions
    movement_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    -- Measures
    movement_quantity NUMERIC(19,3),
    movement_value NUMERIC(19,2),
    -- Attributes
    movement_type VARCHAR(20), -- RECEIPT, SHIPMENT, ADJUSTMENT, TRANSFER
    warehouse_id VARCHAR(20),
    warehouse_name VARCHAR(100),
    location_id VARCHAR(50),
    reason_code VARCHAR(20),
    -- Dates
    movement_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### Data Ingestion
- **CDC + Event Streams** for near-real-time updates.
- Batch backfill for large historical loads.
- Data quality checks on ingestion with quarantine for malformed records.

### Data Ingestion Pipeline

### CDC (Change Data Capture) Strategy

**Technology**: Debezium + Apache Kafka

```yaml
# Debezium PostgreSQL Connector Configuration
name: financial-accounting-cdc
connector.class: io.debezium.connector.postgresql.PostgresConnector
database.hostname: pg-primary-01.chiroerp.internal
database.port: 5432
database.user: debezium_user
database.password: ${DEBEZIUM_PASSWORD}
database.dbname: chiroerp_production
database.server.name: financial_accounting
plugin.name: pgoutput
table.include.list: >
  financial_accounting.journal_entry,
  financial_accounting.fiscal_period,
  accounts_payable.invoice,
  accounts_payable.payment,
  accounts_receivable.invoice,
  accounts_receivable.payment
slot.name: debezium_financial_accounting
publication.name: debezium_publication

# Transforms
transforms: unwrap,addFields
transforms.unwrap.type: io.debezium.transforms.ExtractNewRecordState
transforms.addFields.type: org.apache.kafka.connect.transforms.InsertField$Value
transforms.addFields.static.field: ingestion_timestamp
transforms.addFields.static.value: ${kafka.timestamp}
```

**Kafka Topics Structure**:
```
financial-accounting.journal_entry.cdc
financial-accounting.fiscal_period.cdc
accounts-payable.invoice.cdc
accounts-payable.payment.cdc
accounts-receivable.invoice.cdc
accounts-receivable.payment.cdc
customer-relation.customer.cdc
inventory.product.cdc
sales.sales_order.cdc
```

### ETL Pipeline (Kafka → Data Warehouse)

```kotlin
// Kafka Consumer → Data Warehouse ETL
@Component
class InvoiceFactETLConsumer {

    @KafkaListener(
        topics = ["accounts-receivable.invoice.cdc"],
        groupId = "data-warehouse-etl"
    )
    suspend fun processInvoiceCDC(message: InvoiceCDCMessage) {
        try {
            // 1. Data quality checks
            validateInvoiceData(message)

            // 2. Lookup dimension keys
            val customerKey = lookupOrCreateCustomerDimension(message.customerId)
            val productKey = lookupOrCreateProductDimension(message.productId)
            val dateKey = getDateKey(message.invoiceDate)
            val timeKey = getTimeKey(message.createdAt)
            val companyKey = lookupCompanyDimension(message.companyCode)

            // 3. Transform to fact record
            val factInvoice = FactInvoice(
                invoiceId = message.invoiceId,
                invoiceNumber = message.invoiceNumber,
                tenantId = message.tenantId,
                customerKey = customerKey,
                productKey = productKey,
                dateKey = dateKey,
                timeKey = timeKey,
                companyKey = companyKey,
                invoiceAmount = message.amount,
                taxAmount = message.taxAmount,
                discountAmount = message.discountAmount,
                netAmount = message.netAmount,
                amountPaid = message.amountPaid,
                amountOutstanding = message.amountOutstanding,
                invoiceType = message.invoiceType,
                invoiceStatus = message.status,
                paymentTerms = message.paymentTerms,
                currency = message.currency,
                invoiceDate = message.invoiceDate,
                dueDate = message.dueDate,
                postingDate = message.postingDate,
                paymentDate = message.paymentDate
            )

            // 4. Upsert to warehouse
            dataWarehouseRepository.upsertFactInvoice(factInvoice)

            // 5. Update metrics
            etlMetrics.recordSuccess("fact_invoice")

        } catch (e: DataQualityException) {
            // Quarantine bad records
            quarantineRecord(message, e.message)
            etlMetrics.recordQuarantine("fact_invoice")

        } catch (e: Exception) {
            logger.error("ETL failed for invoice ${message.invoiceId}", e)
            etlMetrics.recordFailure("fact_invoice")
            throw e // Kafka will retry
        }
    }

    private fun validateInvoiceData(message: InvoiceCDCMessage) {
        require(message.amount > BigDecimal.ZERO) {
            "Invoice amount must be positive"
        }
        require(message.customerId.isNotBlank()) {
            "Customer ID is required"
        }
        require(message.invoiceDate != null) {
            "Invoice date is required"
        }
        require(message.netAmount == message.amount + message.taxAmount - message.discountAmount) {
            "Net amount calculation mismatch"
        }
    }
}
```

### Slowly Changing Dimension (SCD) Type 2 Handler

```kotlin
class CustomerDimensionService {

    /**
     * Lookup or create customer dimension with SCD Type 2 support
     */
    suspend fun lookupOrCreateCustomerDimension(
        customerId: String,
        tenantId: String,
        customerData: CustomerData
    ): Long {
        // Find current dimension record
        val currentDim = findCurrentCustomerDimension(customerId, tenantId)

        if (currentDim == null) {
            // First time seeing this customer, create new dimension
            return createCustomerDimension(customerId, tenantId, customerData)
        }

        // Check if attributes have changed
        if (hasCustomerChanged(currentDim, customerData)) {
            // Close current record
            closeCustomerDimension(currentDim)

            // Create new version
            return createCustomerDimension(
                customerId = customerId,
                tenantId = tenantId,
                customerData = customerData,
                version = currentDim.version + 1
            )
        }

        // No change, return existing key
        return currentDim.customerKey
    }

    private suspend fun createCustomerDimension(
        customerId: String,
        tenantId: String,
        customerData: CustomerData,
        version: Int = 1
    ): Long {
        return jdbcTemplate.queryForObject("""
            INSERT INTO dw.dim_customer (
                customer_id, tenant_id, customer_name, customer_group,
                credit_limit, payment_terms, territory_id, territory_name,
                industry, customer_status,
                effective_from, effective_to, is_current, version
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                NOW(), NULL, true, ?
            )
            RETURNING customer_key
        """, Long::class.java,
            customerId, tenantId, customerData.name, customerData.group,
            customerData.creditLimit, customerData.paymentTerms,
            customerData.territoryId, customerData.territoryName,
            customerData.industry, customerData.status,
            version
        )
    }

    private suspend fun closeCustomerDimension(dim: DimCustomer) {
        jdbcTemplate.update("""
            UPDATE dw.dim_customer
            SET effective_to = NOW(), is_current = false
            WHERE customer_key = ?
        """, dim.customerKey)
    }

    private fun hasCustomerChanged(current: DimCustomer, new: CustomerData): Boolean {
        return current.customerName != new.name ||
               current.creditLimit != new.creditLimit ||
               current.paymentTerms != new.paymentTerms ||
               current.territoryId != new.territoryId ||
               current.customerStatus != new.status
    }
}
```

### Batch Backfill for Historical Data

```kotlin
class HistoricalDataBackfillJob {

    /**
     * Backfill historical invoices from source database to warehouse
     */
    suspend fun backfillInvoices(
        tenantId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        batchSize: Int = 10000
    ) {
        logger.info("Starting invoice backfill for tenant $tenantId from $startDate to $endDate")

        var offset = 0
        var totalProcessed = 0

        while (true) {
            // Fetch batch from source
            val batch = sourceDatabase.query("""
                SELECT * FROM accounts_receivable.invoice
                WHERE tenant_id = ?
                  AND invoice_date BETWEEN ? AND ?
                ORDER BY invoice_date, id
                LIMIT ? OFFSET ?
            """, tenantId, startDate, endDate, batchSize, offset)

            if (batch.isEmpty()) break

            // Process batch
            batch.forEach { invoice ->
                processInvoiceForWarehouse(invoice)
            }

            totalProcessed += batch.size
            offset += batchSize

            logger.info("Backfilled $totalProcessed invoices...")

            // Rate limiting to avoid overloading source DB
            delay(100.milliseconds)
        }

        logger.info("Backfill complete: $totalProcessed invoices processed")
    }
}
```

### Data Quality Checks

```kotlin
data class DataQualityRule(
    val ruleName: String,
    val entityType: String,
    val fieldName: String,
    val checkType: QualityCheckType,
    val threshold: Double?,
    val criticalRule: Boolean
)

enum class QualityCheckType {
    NOT_NULL,
    UNIQUE,
    RANGE,
    REFERENTIAL_INTEGRITY,
    FORMAT,
    BUSINESS_LOGIC
}

class DataQualityService {

    private val rules = listOf(
        DataQualityRule("invoice_amount_positive", "invoice", "amount", QualityCheckType.RANGE, null, true),
        DataQualityRule("invoice_customer_exists", "invoice", "customer_id", QualityCheckType.REFERENTIAL_INTEGRITY, null, true),
        DataQualityRule("invoice_date_not_future", "invoice", "invoice_date", QualityCheckType.BUSINESS_LOGIC, null, true),
        DataQualityRule("payment_amount_positive", "payment", "amount", QualityCheckType.RANGE, null, true),
        DataQualityRule("journal_entry_balanced", "journal_entry", "net_amount", QualityCheckType.BUSINESS_LOGIC, 0.01, true)
    )

    suspend fun validateRecord(entityType: String, record: Map<String, Any?>): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()

        rules.filter { it.entityType == entityType }.forEach { rule ->
            val violation = checkRule(rule, record)
            if (violation != null) {
                violations.add(violation)
            }
        }

        return violations
    }

    private fun checkRule(rule: DataQualityRule, record: Map<String, Any?>): QualityViolation? {
        val fieldValue = record[rule.fieldName]

        return when (rule.checkType) {
            QualityCheckType.NOT_NULL -> {
                if (fieldValue == null) {
                    QualityViolation(rule.ruleName, "Field ${rule.fieldName} is null")
                } else null
            }

            QualityCheckType.RANGE -> {
                if (fieldValue is BigDecimal && fieldValue <= BigDecimal.ZERO) {
                    QualityViolation(rule.ruleName, "Field ${rule.fieldName} must be positive")
                } else null
            }

            QualityCheckType.BUSINESS_LOGIC -> {
                when (rule.ruleName) {
                    "invoice_date_not_future" -> {
                        val invoiceDate = fieldValue as? LocalDate
                        if (invoiceDate != null && invoiceDate.isAfter(LocalDate.now())) {
                            QualityViolation(rule.ruleName, "Invoice date cannot be in future")
                        } else null
                    }
                    "journal_entry_balanced" -> {
                        val netAmount = fieldValue as? BigDecimal ?: BigDecimal.ZERO
                        if (netAmount.abs() > (rule.threshold ?: BigDecimal.ZERO).toBigDecimal()) {
                            QualityViolation(rule.ruleName, "Journal entry not balanced: $netAmount")
                        } else null
                    }
                    else -> null
                }
            }

            else -> null
        }
    }
}

// Quarantine table for failed records
CREATE TABLE dw.quarantine (
    quarantine_id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50),
    source_id VARCHAR(100),
    tenant_id VARCHAR(50),
    raw_data JSONB,
    quality_violations JSONB,
    quarantined_at TIMESTAMP DEFAULT NOW(),
    resolved_at TIMESTAMP,
    resolution_action VARCHAR(50) -- 'FIXED', 'DISCARDED', 'IGNORED'
);
```

### Canonical Reporting Model
- Define **Reporting Canonical Entities** (e.g., `FactInvoice`, `FactPayment`, `DimCustomer`, `DimProduct`).
- Versioned schemas with controlled evolution.

### Standard ERP Reports Catalog

### Financial Reports

| Report Code | Report Name | Frequency | Regulatory | Source Tables | Output Format |
|-------------|------------|-----------|------------|---------------|---------------|
| **FIN-001** | Profit & Loss Statement | Monthly, Quarterly, Annual | Yes (GAAP/IFRS) | fact_journal_entry, dim_date | PDF, XLSX |
| **FIN-002** | Balance Sheet | Monthly, Quarterly, Annual | Yes (GAAP/IFRS) | fact_journal_entry, dim_date | PDF, XLSX |
| **FIN-003** | Cash Flow Statement | Monthly, Quarterly, Annual | Yes (GAAP/IFRS) | fact_journal_entry, fact_payment | PDF, XLSX |
| **FIN-004** | Trial Balance | Monthly | No | fact_journal_entry | XLSX, CSV |
| **FIN-005** | General Ledger Detail | On-demand | Yes (SOX) | fact_journal_entry | PDF, XLSX |
| **FIN-006** | Financial Statement Notes | Quarterly, Annual | Yes (SEC) | Multiple | PDF |
| **FIN-007** | Intercompany Reconciliation | Monthly | Yes (consolidation) | fact_journal_entry | XLSX |

### Accounts Receivable Reports

| Report Code | Report Name | Frequency | Regulatory | Source Tables | Output Format |
|-------------|------------|-----------|------------|---------------|---------------|
| **AR-001** | AR Aging Summary | Daily | No | fact_invoice, dim_customer | PDF, XLSX |
| **AR-002** | AR Aging Detail | Daily | No | fact_invoice, dim_customer | XLSX, CSV |
| **AR-003** | Customer Account Statement | Monthly | No | fact_invoice, fact_payment | PDF |
| **AR-004** | Collections Dashboard | Real-time | No | mv_ar_aging_summary | Dashboard |
| **AR-005** | Credit Limit Analysis | Weekly | No | fact_invoice, dim_customer | XLSX |
| **AR-006** | Write-off Report | Monthly | Yes (SOX) | fact_invoice | PDF, XLSX |
| **AR-007** | Sales by Customer | Monthly | No | fact_invoice, dim_customer | XLSX |

### Accounts Payable Reports

| Report Code | Report Name | Frequency | Regulatory | Source Tables | Output Format |
|-------------|------------|-----------|------------|---------------|---------------|
| **AP-001** | AP Aging Summary | Daily | No | fact_payment (AP), dim_vendor | PDF, XLSX |
| **AP-002** | AP Aging Detail | Daily | No | fact_payment (AP), dim_vendor | XLSX, CSV |
| **AP-003** | Payment Forecast | Weekly | No | fact_payment | XLSX |
| **AP-004** | Vendor Spend Analysis | Monthly | No | fact_payment, dim_vendor | XLSX |
| **AP-005** | 1099 Report | Annual | Yes (IRS) | fact_payment, dim_vendor | PDF, XLSX |
| **AP-006** | Payment Run Report | Per run | No | fact_payment | PDF |
| **AP-007** | Duplicate Payment Detection | Weekly | Yes (SOX) | fact_payment | XLSX |

### Inventory Reports

| Report Code | Report Name | Frequency | Regulatory | Source Tables | Output Format |
|-------------|------------|-----------|------------|---------------|---------------|
| **INV-001** | Inventory Valuation | Monthly | Yes (GAAP) | fact_inventory_movement, dim_product | PDF, XLSX |
| **INV-002** | Stock Levels by Warehouse | Real-time | No | mv_inventory_levels | Dashboard |
| **INV-003** | Slow-Moving Inventory | Monthly | No | fact_inventory_movement, fact_sales_order | XLSX |
| **INV-004** | Stock Turnover Analysis | Monthly | No | fact_inventory_movement, fact_sales_order | XLSX |
| **INV-005** | Inventory Adjustments | Monthly | Yes (SOX) | fact_inventory_movement | PDF, XLSX |
| **INV-006** | ABC Analysis | Quarterly | No | fact_sales_order, dim_product | XLSX |
| **INV-007** | Reorder Point Report | Weekly | No | mv_inventory_levels | XLSX |

### Sales Reports

| Report Code | Report Name | Frequency | Regulatory | Source Tables | Output Format |
|-------------|------------|-----------|------------|---------------|---------------|
| **SALES-001** | Sales Dashboard | Real-time | No | fact_sales_order, dim_customer | Dashboard |
| **SALES-002** | Sales by Region | Monthly | No | fact_sales_order, dim_customer | XLSX |
| **SALES-003** | Sales by Product | Monthly | No | fact_sales_order, dim_product | XLSX |
| **SALES-004** | Sales Rep Performance | Monthly | No | fact_sales_order | XLSX |
| **SALES-005** | Win/Loss Analysis | Quarterly | No | fact_sales_order | XLSX |
| **SALES-006** | Sales Forecast vs Actual | Monthly | No | fact_sales_order | XLSX |
| **SALES-007** | Customer Profitability | Quarterly | No | fact_sales_order, fact_invoice | XLSX |

### Executive Reports

| Report Code | Report Name | Frequency | Regulatory | Source Tables | Output Format |
|-------------|------------|-----------|------------|---------------|---------------|
| **EXEC-001** | Executive Dashboard | Real-time | No | Multiple fact tables | Dashboard |
| **EXEC-002** | KPI Scorecard | Monthly | No | Multiple fact tables | PDF |
| **EXEC-003** | Revenue Analysis | Monthly | No | fact_invoice, fact_sales_order | PDF, XLSX |
| **EXEC-004** | Cost Analysis | Monthly | No | fact_journal_entry, fact_payment | PDF, XLSX |
| **EXEC-005** | Budget vs Actual | Monthly | No | fact_journal_entry, dim_budget | XLSX |
| **EXEC-006** | Trend Analysis | Quarterly | No | Multiple fact tables | PDF |
| **EXEC-007** | Board Report Package | Quarterly | No | All financial statements | PDF |

### Report Implementation Examples

### Example 1: Profit & Loss Statement (FIN-001)

```kotlin
data class ProfitLossStatement(
    val companyCode: String,
    val companyName: String,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val currency: String,
    val revenue: ProfitLossSection,
    val costOfGoodsSold: ProfitLossSection,
    val operatingExpenses: ProfitLossSection,
    val otherIncome: ProfitLossSection,
    val otherExpenses: ProfitLossSection,
    val taxExpense: ProfitLossSection,
    val netIncome: BigDecimal
)

data class ProfitLossSection(
    val sectionName: String,
    val accounts: List<PLAccount>,
    val subtotal: BigDecimal
)

data class PLAccount(
    val accountNumber: String,
    val accountName: String,
    val currentPeriod: BigDecimal,
    val yearToDate: BigDecimal,
    val priorYearSamePeriod: BigDecimal
)

class ProfitLossReportService {

    suspend fun generateProfitLoss(
        tenantId: String,
        companyCode: String,
        fiscalYear: Int,
        fiscalPeriod: Int
    ): ProfitLossStatement {

        // Query journal entries for the period
        val journalEntries = jdbcTemplate.query("""
            SELECT
                je.account_number,
                je.account_name,
                SUM(je.net_amount) as period_amount,
                (SELECT SUM(net_amount)
                 FROM dw.fact_journal_entry ytd
                 WHERE ytd.account_number = je.account_number
                   AND ytd.fiscal_year = ?
                   AND ytd.fiscal_period <= ?
                   AND ytd.tenant_id = ?) as ytd_amount,
                (SELECT SUM(net_amount)
                 FROM dw.fact_journal_entry py
                 WHERE py.account_number = je.account_number
                   AND py.fiscal_year = ? - 1
                   AND py.fiscal_period = ?
                   AND py.tenant_id = ?) as prior_year_amount
            FROM dw.fact_journal_entry je
            JOIN dw.dim_company c ON je.company_key = c.company_key
            WHERE je.tenant_id = ?
              AND c.company_code = ?
              AND je.fiscal_year = ?
              AND je.fiscal_period = ?
              AND je.posting_status = 'POSTED'
            GROUP BY je.account_number, je.account_name
            ORDER BY je.account_number
        """,
            fiscalYear, fiscalPeriod, tenantId, // YTD
            fiscalYear, fiscalPeriod, tenantId, // Prior year
            tenantId, companyCode, fiscalYear, fiscalPeriod
        )

        // Group accounts by P&L section
        val revenue = filterAccountsByRange(journalEntries, "4000", "4999")
        val cogs = filterAccountsByRange(journalEntries, "5000", "5999")
        val opex = filterAccountsByRange(journalEntries, "6000", "6999")
        val otherIncome = filterAccountsByRange(journalEntries, "7000", "7499")
        val otherExpenses = filterAccountsByRange(journalEntries, "7500", "7999")
        val tax = filterAccountsByRange(journalEntries, "8000", "8099")

        // Calculate net income
        val netIncome = revenue.subtotal - cogs.subtotal - opex.subtotal +
                       otherIncome.subtotal - otherExpenses.subtotal - tax.subtotal

        return ProfitLossStatement(
            companyCode = companyCode,
            companyName = getCompanyName(companyCode),
            fiscalYear = fiscalYear,
            fiscalPeriod = fiscalPeriod,
            currency = getCompanyCurrency(companyCode),
            revenue = revenue,
            costOfGoodsSold = cogs,
            operatingExpenses = opex,
            otherIncome = otherIncome,
            otherExpenses = otherExpenses,
            taxExpense = tax,
            netIncome = netIncome
        )
    }

    suspend fun exportToPDF(statement: ProfitLossStatement): ByteArray {
        // Use iText or similar library to generate PDF
        return PdfGenerator.generate {
            title("Profit & Loss Statement")
            subtitle("${statement.companyName} - FY${statement.fiscalYear} Period ${statement.fiscalPeriod}")

            table {
                header("Account", "Account Name", "Current Period", "Year to Date", "Prior Year")

                section("Revenue") {
                    statement.revenue.accounts.forEach { account ->
                        row(account.accountNumber, account.accountName,
                            account.currentPeriod.format(),
                            account.yearToDate.format(),
                            account.priorYearSamePeriod.format())
                    }
                    subtotal("Total Revenue", statement.revenue.subtotal.format())
                }

                section("Cost of Goods Sold") {
                    statement.costOfGoodsSold.accounts.forEach { account ->
                        row(account.accountNumber, account.accountName,
                            account.currentPeriod.format(),
                            account.yearToDate.format(),
                            account.priorYearSamePeriod.format())
                    }
                    subtotal("Total COGS", statement.costOfGoodsSold.subtotal.format())
                }

                grossProfit("Gross Profit",
                    (statement.revenue.subtotal - statement.costOfGoodsSold.subtotal).format())

                // ... remaining sections

                netIncome("Net Income", statement.netIncome.format())
            }
        }
    }
}
```

### Example 2: AR Aging Report (AR-001)

```kotlin
data class ARAgingReport(
    val tenantId: String,
    val reportDate: LocalDate,
    val customers: List<ARAgingCustomer>,
    val totals: ARAgingTotals
)

data class ARAgingCustomer(
    val customerId: String,
    val customerName: String,
    val totalOutstanding: BigDecimal,
    val current: BigDecimal,
    val days30: BigDecimal,
    val days60: BigDecimal,
    val days90: BigDecimal,
    val over90: BigDecimal,
    val percentOver30: Double
)

data class ARAgingTotals(
    val totalOutstanding: BigDecimal,
    val current: BigDecimal,
    val days30: BigDecimal,
    val days60: BigDecimal,
    val days90: BigDecimal,
    val over90: BigDecimal
)

class ARAgingReportService {

    suspend fun generateARAgingReport(
        tenantId: String,
        asOfDate: LocalDate = LocalDate.now()
    ): ARAgingReport {

        val customers = jdbcTemplate.query("""
            WITH invoice_aging AS (
                SELECT
                    i.customer_key,
                    c.customer_id,
                    c.customer_name,
                    i.amount_outstanding,
                    CASE
                        WHEN ? - i.due_date <= 0 THEN i.amount_outstanding
                        ELSE 0
                    END as current,
                    CASE
                        WHEN ? - i.due_date BETWEEN 1 AND 30 THEN i.amount_outstanding
                        ELSE 0
                    END as days_30,
                    CASE
                        WHEN ? - i.due_date BETWEEN 31 AND 60 THEN i.amount_outstanding
                        ELSE 0
                    END as days_60,
                    CASE
                        WHEN ? - i.due_date BETWEEN 61 AND 90 THEN i.amount_outstanding
                        ELSE 0
                    END as days_90,
                    CASE
                        WHEN ? - i.due_date > 90 THEN i.amount_outstanding
                        ELSE 0
                    END as over_90
                FROM dw.fact_invoice i
                JOIN dw.dim_customer c ON i.customer_key = c.customer_key AND c.is_current = true
                WHERE i.tenant_id = ?
                  AND i.invoice_status IN ('POSTED', 'PARTIALLY_PAID')
                  AND i.amount_outstanding > 0
            )
            SELECT
                customer_id,
                customer_name,
                SUM(amount_outstanding) as total_outstanding,
                SUM(current) as current,
                SUM(days_30) as days_30,
                SUM(days_60) as days_60,
                SUM(days_90) as days_90,
                SUM(over_90) as over_90,
                CASE
                    WHEN SUM(amount_outstanding) > 0
                    THEN (SUM(days_30) + SUM(days_60) + SUM(days_90) + SUM(over_90)) / SUM(amount_outstanding) * 100
                    ELSE 0
                END as percent_over_30
            FROM invoice_aging
            GROUP BY customer_id, customer_name
            HAVING SUM(amount_outstanding) > 0
            ORDER BY SUM(amount_outstanding) DESC
        """,
            asOfDate, asOfDate, asOfDate, asOfDate, asOfDate, tenantId
        ).map { rs ->
            ARAgingCustomer(
                customerId = rs.getString("customer_id"),
                customerName = rs.getString("customer_name"),
                totalOutstanding = rs.getBigDecimal("total_outstanding"),
                current = rs.getBigDecimal("current"),
                days30 = rs.getBigDecimal("days_30"),
                days60 = rs.getBigDecimal("days_60"),
                days90 = rs.getBigDecimal("days_90"),
                over90 = rs.getBigDecimal("over_90"),
                percentOver30 = rs.getDouble("percent_over_30")
            )
        }

        val totals = ARAgingTotals(
            totalOutstanding = customers.sumOf { it.totalOutstanding },
            current = customers.sumOf { it.current },
            days30 = customers.sumOf { it.days30 },
            days60 = customers.sumOf { it.days60 },
            days90 = customers.sumOf { it.days90 },
            over90 = customers.sumOf { it.over90 }
        )

        return ARAgingReport(
            tenantId = tenantId,
            reportDate = asOfDate,
            customers = customers,
            totals = totals
        )
    }
}
```

### Reporting & BI Tooling
- Embedded reporting service for standard ERP reports (P&L, Balance Sheet, AR/AP aging).
- Self-service BI with governed datasets and role-based access.
- Export formats: PDF, XLSX, CSV.

### Performance & Isolation
- Reporting workload must not degrade transactional performance.
- Use read replicas and warehouse separation.
- Query limits and caching for heavy reports.

### Security & Compliance
- Fine-grained access controls using authorization objects and tenant isolation.
- Row-level security in warehouse for multi-tenant reporting.
- Audit logging for report access and exports.

## Alternatives Considered

### Alternative 1: Embedded Reporting Only (No Warehouse)
- **Approach**: Build all reports directly on operational PostgreSQL databases using materialized views and read replicas. No separate data warehouse.
- **Pros**:
  - Simplest architecture (no ETL, no warehouse)
  - Real-time data (no latency from CDC pipelines)
  - Lower infrastructure costs
- **Cons**:
  - Reporting queries degrade transactional performance
  - Cannot support complex analytics (multi-table joins across contexts)
  - No historical analysis (operational DBs retain only active data)
  - Limited scalability (vertical scaling only)
  - No cross-context reporting (bounded contexts have separate databases)
- **Decision**: Rejected. Cannot meet SAP-grade reporting requirements (financial consolidation, multi-dimensional analysis, historical trends). Reporting workload would impact transactional SLAs.

### Alternative 2: External BI Tools Only (Power BI, Tableau)
- **Approach**: Provide database access to external BI tools. Users create ad-hoc reports directly against operational databases or read replicas.
- **Pros**:
  - Leverage existing BI tool investments
  - User self-service (no dev team bottleneck)
  - Mature visualization capabilities
- **Cons**:
  - No governance (users can create inconsistent KPIs)
  - Performance unpredictability (uncontrolled queries)
  - Security risks (direct database access, no row-level security)
  - Cannot enforce tenant isolation in multi-tenant queries
  - No canonical data models (each user builds own joins)
- **Decision**: Rejected as sole approach. External BI tools are supported as visualization layer, but require governed datasets from warehouse (not direct database access).

### Alternative 3: Data Warehouse with Star Schema (Selected Approach)
- **Approach**: Build dedicated analytics database (PostgreSQL or Snowflake) with star/snowflake schema. CDC pipelines replicate data from operational databases. BI tools query warehouse via curated datasets.
- **Pros**:
  - Workload isolation (reporting does not impact transactions)
  - Historical data retention (unlimited analytical history)
  - Cross-context reporting (financial + clinical + operations)
  - Governed datasets (canonical KPIs, consistent definitions)
  - Supports complex analytics (multi-dimensional analysis, drill-downs)
  - Tenant isolation via row-level security
- **Cons**:
  - Data latency (15-minute CDC pipeline delay)
  - Infrastructure costs (warehouse, CDC pipelines, storage)
  - Operational complexity (ETL job monitoring, data quality checks)
  - Requires data engineering team (6-8 months, 4.5 FTE)
- **Decision**: Selected. Only approach that meets SAP-grade reporting requirements (financial consolidation, compliance reporting, trend analysis) without degrading transactional performance.

### Alternative 4: Real-Time Analytics (ClickHouse, Apache Druid)
- **Approach**: Use columnar OLAP database optimized for real-time ingestion and fast aggregations. Event-driven ingestion from Redpanda topics.
- **Pros**:
  - Sub-second query latency for aggregations
  - Real-time data (no batch delay)
  - Horizontal scalability
  - Excellent for time-series and event data
- **Cons**:
  - Limited SQL support (no window functions, CTEs in ClickHouse)
  - Not suitable for transactional reports (balance sheets, trial balance)
  - Operational complexity (new technology stack)
  - Poor support for late-arriving data corrections
- **Decision**: Deferred to Phase 6+ for operational dashboards (e.g., real-time inventory). Not suitable for financial reporting requirements in Phase 4-5.

### Alternative 5: Hybrid Approach (Operational Views + Enterprise Warehouse)
- **Approach**: Dual-layer reporting: (1) Operational layer with materialized views for real-time reports (e.g., today's sales), (2) Enterprise warehouse for historical and cross-context reports (e.g., quarterly financial statements).
- **Pros**:
  - Best of both worlds (real-time + historical)
  - Reduced warehouse load (operational reports stay on source DBs)
  - Flexibility for different report types
- **Cons**:
  - Increased complexity (two reporting layers to maintain)
  - Confusion for users (which layer to query?)
  - Requires sophisticated report routing logic
- **Decision**: Deferred to Phase 6+ as optimization. Phase 4-5 focuses on enterprise warehouse only for simplicity.

## Consequences
### Positive
- SAP-grade reporting capabilities with clear separation of workloads
- Scalable analytics without impacting transactional systems
- Consistent KPI definitions across modules

### Negative / Risks
- Additional platform complexity (warehouse, pipelines, governance)
- Requires dedicated data engineering and BI tooling
- Data latency trade-offs between operational and enterprise layers

### Neutral
- Some reports will migrate from OLTP to OLAP over time

## Compliance

### SOX Compliance: Financial Reporting Controls
- **Report Accuracy**: All financial reports (P&L, Balance Sheet, Cash Flow) generated from warehouse data with checksums validated against operational source. Monthly reconciliation process.
- **Change Control**: Financial report definitions versioned in Git. Changes require business owner approval + IT review. Deployment to production requires sign-off.
- **Segregation of Duties (SoD)**: Report developers cannot publish reports to production without approval. Report approvers cannot modify report SQL logic.
- **Audit Trail**: All report executions logged (who ran what report, when, with what parameters). Logs retained for 7 years per SOX Section 802.
- **Access Controls**: Financial reports restricted to users with authorization objects (per ADR-014). Row-level security enforces tenant isolation.
- **Data Lineage**: Warehouse tables document source systems (which operational database, which CDC pipeline). Lineage viewable in data catalog.
- **Quarterly Certification**: CFO/Controller certifies accuracy of financial reports before publication. Certification workflow includes data validation checks (e.g., balance = debits - credits).

### GDPR Compliance: Analytics Data Minimization
- **PII Minimization**: Warehouse contains only aggregated/pseudonymized data where possible. Customer names replaced with `customer_id` in analytical datasets.
- **Purpose Limitation**: Analytical datasets document business purpose (e.g., "Revenue trending", "Cost center analysis"). Purpose displayed in data catalog.
- **Right to Access**: GDPR data exports include analytical data if it contains PII. Export indicates source report and business purpose.
- **Right to Erasure**: Erasure requests trigger warehouse purge jobs. All analytical records containing deleted PII are tombstoned or re-aggregated.
- **Data Retention**: Warehouse data retained per data lifecycle policies (ADR-015). Historical financial data retained for 7 years (SOX), but PII fields masked after 3 years unless legally required.
- **Cross-Border Transfers**: Analytical datasets respect data residency requirements. EU tenant data warehoused in EU region, US tenant data in US region.

### Report Access Controls (Linked to ADR-014)
- **Authorization Objects**: Report access enforced via authorization objects (e.g., `F_REPORT_P&L`, `F_REPORT_TRIAL_BALANCE`). Users assigned objects via roles.
- **Row-Level Security**: Warehouse queries automatically filter by `tenant_id` and user's assigned cost centers/legal entities. Multi-tenant queries blocked by default.
- **Column-Level Security**: Sensitive fields (e.g., salary, SSN) visible only to users with `SENSITIVE_DATA_ACCESS` object. Masked for other users.
- **Report Execution Approval**: High-cost reports (>10 million rows) require manager approval before execution. Approval workflow integrated with reporting portal.
- **Export Controls**: Report exports (CSV, Excel) logged and restricted to users with `DATA_EXPORT` authorization object. DLP policies scan exports for PII.

### OWASP Top 10 for APIs: Reporting API Security
- **API4 - Resource Exhaustion**: Report queries have timeout limits (5 minutes for operational reports, 30 minutes for batch reports). Circuit breakers prevent runaway queries.
- **API5 - Broken Function Level Authorization**: Report API endpoints validate authorization objects before executing queries. No role-based bypasses.
- **API8 - Injection**: Report parameters sanitized via parameterized queries. No dynamic SQL construction from user input.

### Service Level Objectives (SLOs)
- **Report Generation Time**: `<10 seconds` for operational reports (single tenant, <100k rows), `<5 minutes` for enterprise reports (multi-tenant, <10M rows).
- **Data Freshness**: `<15 minutes` for operational datasets (CDC pipeline latency), `<4 hours` for enterprise datasets (batch ETL window).
- **Query Availability**: `99.9%` uptime for reporting warehouse (downtime only during maintenance windows).
- **Concurrent Users**: Support `500 concurrent report users` without degradation.
- **Report Accuracy**: `<0.01%` variance between warehouse reports and operational source (monthly reconciliation).
- **Audit Log Completeness**: `100%` of report executions logged (no missing entries).

### Domain SLO References

Analytics pipelines and dashboards must respect upstream domain SLOs. The following table provides a cross-reference to domain-specific performance and accuracy targets:

| Domain | ADR | Key SLOs | Impact on Analytics |
|--------|-----|----------|---------------------|
| **Financial Accounting (FI)** | [ADR-009](ADR-009-financial-accounting-domain.md) | Journal posting <200ms p95, period close <5min/1M txn, consolidation <15min/50 entities | Financial statements, trial balance, close dashboards |
| **Fixed Assets (FI-AA)** | [ADR-021](ADR-021-fixed-asset-accounting.md) | Depreciation run within monthly close SLA, immutable asset transaction history | Asset register, depreciation schedules |
| **Controlling (CO)** | [ADR-028](ADR-028-controlling-management-accounting.md) | Allocation run <2h p95, CO-FI reconciliation 99.9%, profitability by 08:00, ABC variance <1% | Cost center reports, CO-PA, margin analysis |
| **Inventory (MM-IM)** | [ADR-024](ADR-024-inventory-management.md) | POS stock updates <60s, 99.5% accuracy, offline reconciliation <24h | Inventory levels, stock turns, shrinkage |
| **Warehouse Execution (WES/WMS)** | [ADR-038](ADR-038-warehouse-execution-wms.md) | Wave release <5min, pick accuracy ≥99.9%, putaway <30min p95, task allocation <10s, labor utilization ≥85% | Warehouse throughput, labor productivity, pick accuracy, dock-to-stock |
| **Sales & Distribution (SD)** | [ADR-025](ADR-025-sales-distribution.md) | Order processing <2s p95, pricing deterministic, idempotent submissions | Sales dashboards, O2C analytics, revenue |
| **Treasury (TR-CM)** | [ADR-026](ADR-026-treasury-cash-management.md) | Cash position by 08:00 local, reconciliation audit trail, dual approval trace | Cash position, liquidity forecasting |
| **Procurement (MM-PUR)** | [ADR-023](ADR-023-procurement.md) | PO/GR updates <60s to inventory, all approvals logged | Spend analytics, supplier KPIs |
| **Revenue Recognition (ASC 606)** | [ADR-022](ADR-022-revenue-recognition.md) | Recognition run within close SLA, GL reconciliation, immutable schedules | Revenue waterfall, deferred revenue, ASC 606 disclosures |
| **Master Data Governance (MDG)** | [ADR-027](ADR-027-master-data-governance.md) | 95%+ data quality, <10min propagation, full change history | Dimension consistency, master data lineage, data quality dashboards |
| **Intercompany (IC)** | [ADR-029](ADR-029-intercompany-accounting.md) | Match p95 <30min/100k pairs, elimination readiness 100%, variance <0.01% | IC reconciliation, elimination dashboards, consolidated reporting |
| **Tax Engine & Compliance** | [ADR-030](ADR-030-tax-engine-compliance.md) | Tax calc p95 <150ms, return readiness 100%, variance <0.01% | Tax sub-ledger, VAT/GST returns, compliance dashboards |
| **Period Close Orchestration** | [ADR-031](ADR-031-period-close-orchestration.md) | Close <3 days p95, task success >=99%, audit log 100% | Close status dashboards, task progress, exception tracking |
| **Budgeting & Planning (FP&A)** | [ADR-032](ADR-032-budgeting-planning-fpa.md) | Forecast run p95 <30min/500k lines, variance freshness <4h | Plan vs actual, variance analysis, forecast dashboards |
| **Lease Accounting (IFRS 16)** | [ADR-033](ADR-033-lease-accounting-ifrs16.md) | Recognition run p95 <30min/5k leases, posting variance <0.01% | ROU assets, lease liabilities, disclosure reports |
| **HR Integration** | [ADR-034](ADR-034-hr-integration-payroll-events.md) | Payroll ingestion p95 <2h, accuracy 99.9%, T&E p95 <3 days | Headcount, labor cost, payroll analytics |
| **Manufacturing (PP)** | [ADR-037](ADR-037-manufacturing-production.md) | MRP p95 <2h/100k SKUs, yield ≥98%, OTP ≥95%, WIP variance <0.5% | Production KPIs, yield, throughput, OEE |
| **ESG & Sustainability** *(Add-on)* | [ADR-035](ADR-035-esg-sustainability-reporting.md) | Emission accuracy ≥95%, Scope 3 ≥80% coverage, disclosure <2h | Carbon footprint, ESG dashboards, CSRD/GRI reports |
| **Project Accounting (PS)** *(Add-on)* | [ADR-036](ADR-036-project-accounting.md) | Cost posting <24h, milestone 100% accuracy, close <5 days p95 | Project profitability, WBS analytics, resource utilization |
| **Quality Management (QM)** *(Add-on)* | [ADR-039](ADR-039-quality-management.md) | NC closure <30 days p95, CAPA ≥90% effective, PPM ≤500 | Quality KPIs, supplier PPM, CAPA dashboards |
| **Plant Maintenance (PM)** *(Add-on)* | [ADR-040](ADR-040-plant-maintenance.md) | PM compliance ≥95%, availability ≥95%, MTTR year-over-year decrease | Maintenance KPIs, equipment availability, cost dashboards |

## Implementation Plan
### Implementation Plan (Not Started)

- Phase 1: Define canonical reporting models and KPI dictionary.
- Phase 2: Build operational projections/materialized views for 1-2 contexts.
- Phase 3: Establish warehouse, CDC pipelines, and data quality checks.
- Phase 4: Deliver core financial statements and operational dashboards.
- Phase 5: Self-service BI portal with governed datasets.

## References

### Related ADRs
- ADR-003 (Event-Driven Integration), ADR-009 (Financial), ADR-014 (Auth Objects/SoD), ADR-015 (Data Lifecycle)
- ADR-021 (Fixed Assets), ADR-022 (RevRec), ADR-023 (Procurement), ADR-024 (Inventory), ADR-025 (Sales), ADR-026 (Treasury), ADR-027 (MDG)
- ADR-028 (Controlling), ADR-029 (Intercompany), ADR-030 (Tax), ADR-031 (Period Close), ADR-032 (FP&A), ADR-033 (Lease Accounting)
- ADR-034 (HR Integration), ADR-037 (Manufacturing), ADR-038 (Warehouse Execution)
- ADR-035 (ESG), ADR-036 (Project Accounting), ADR-039 (Quality), ADR-040 (Plant Maintenance) *(Add-ons)*

### External References
- SAP analogs: Embedded Analytics, BW/4HANA, SAC
