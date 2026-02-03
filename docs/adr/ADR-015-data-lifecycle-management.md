# ADR-015: Data Lifecycle Management (Archiving, Retention, Backup)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Compliance Team, Security Team  
**Tier**: Advanced  
**Tags**: data, retention, archiving, backup, recovery, compliance, privacy  

## Context
Enterprise ERP systems require strict data lifecycle controls for compliance (SOX, GDPR, HIPAA), performance, and cost management. SAP-grade systems provide formal retention, archiving, legal hold, and disaster recovery standards. This ADR defines ChiroERP’s platform-wide strategy for data retention, archiving, anonymization, and backup/recovery targets.

## Decision
Adopt a **Data Lifecycle Management (DLM) standard** that defines data classification, retention policies, archiving tiers, legal hold, and backup/recovery objectives. Implementation is **not started**; this ADR defines the standard.

### Data Classification
- **Class A (Regulated Financial/Audit)**: Journal entries, invoices, payments, tax documents.
- **Class B (Operational Master Data)**: Customers, vendors, products, pricing.
- **Class C (Operational Logs/Telemetry)**: Access logs, validation errors, traces.
- **Class D (PII/PHI)**: Personal data subject to GDPR/HIPAA.

### Data Classification Matrix

| Class | Data Types | Retention | Archive Strategy | Regulatory Driver | Examples |
|-------|-----------|-----------|------------------|-------------------|----------|
| **A1 - Financial Transactions** | GL postings, journal entries, fiscal period data | 7-10 years | Immutable cold storage after 2 years | SOX, IFRS, GAAP | `financial_accounting.journal_entry`, `financial_accounting.fiscal_period` |
| **A2 - Tax & Statutory** | Tax returns, statutory reports, audit trails | 7-10 years (jurisdiction) | Immutable cold storage immediately | IRS, HMRC, local tax codes | `financial_accounting.tax_filing`, `financial_accounting.statutory_report` |
| **A3 - Accounts Payable/Receivable** | Invoices, payments, receipts, credit memos | 7 years | Warm archive after 2 years | SOX, Commercial Code | `accounts_payable.invoice`, `accounts_receivable.payment` |
| **A4 - Payroll** | Payroll runs, wage history, tax withholdings | 7 years | Encrypted cold storage after 1 year | FLSA, DOL, IRS | `human_capital.payroll_run`, `human_capital.wage_history` |
| **B1 - Master Data (Active)** | Active customers, vendors, products, employees | Indefinite while active | Warm archive 2 years after inactive | Business operational | `customer_relation.customer`, `procurement.vendor` |
| **B2 - Master Data (Historical)** | Deactivated/merged records | 5 years post-deactivation | Cold storage after 1 year | Business + compliance | `customer_relation.customer_archive` |
| **B3 - Configuration Data** | Pricing, workflows, approval chains | Versioned; retain all | Compress after 1 year | Audit trail | `platform.configuration_version` |
| **C1 - Application Logs** | Business event logs, API calls | 90 days detailed, 2 years aggregated | Purge detailed after 90 days | Operational troubleshooting | `*.application_log` |
| **C2 - Audit Logs** | Authorization decisions, SoD violations, data access | 7 years | Cold storage after 1 year | SOX, GDPR Article 30 | `platform.audit_log`, `platform.authorization_log` |
| **C3 - System Telemetry** | Metrics, traces, performance data | 30 days detailed, 1 year rollups | Purge after retention | Observability | Prometheus metrics, OpenTelemetry traces |
| **D1 - Employee PII** | SSN, DOB, salary, medical info | Minimum necessary + 7 years post-termination | Encrypted, restricted access | GDPR, HIPAA, CCPA | `human_capital.employee_sensitive` |
| **D2 - Customer PII** | Names, addresses, payment methods | Active + statute of limitations | Anonymize for analytics | GDPR, CCPA | `customer_relation.contact_info` |
| **D3 - Health Information (PHI)** | Medical records, chiropractic treatment notes | 6-10 years post-treatment | Encrypted cold, HIPAA-compliant | HIPAA, HITECH | `clinical.patient_record` (if applicable) |

### Retention Policies (Default)
- **Class A**: 7-10 years (jurisdiction-specific). Immutable storage; append-only corrections.
- **Class B**: Retained while active; archive after inactivity threshold (e.g., 24 months).
- **Class C**: Short-lived (e.g., 30-180 days) with aggregated rollups retained.
- **Class D**: Minimum necessary; retain only as long as lawful basis exists.

### Jurisdiction-Specific Retention Rules

### United States
- **Federal Tax Records**: 7 years (IRS statute of limitations)
- **SOX Financial Records**: 7 years (Sarbanes-Oxley Section 802)
- **FLSA Payroll**: 3 years (wage records), 2 years (timecards)
- **HIPAA Medical**: 6 years from creation or last use
- **State-Specific**: Varies (e.g., California: 4 years for employment records)

### European Union (GDPR)
- **Financial Transactions**: 6-10 years (varies by member state)
- **Tax Documents**: 10 years (e.g., Germany HGB §257)
- **Employee Records**: 10 years post-termination (Austria), 5 years (UK)
- **Marketing Consent**: Until consent withdrawn
- **Right to Erasure**: Must honor unless legal/contractual obligation

### United Kingdom (Post-Brexit)
- **VAT Records**: 6 years (HMRC)
- **Corporate Tax**: 6 years from end of accounting period
- **Payroll**: 3 years after end of tax year
- **GDPR-UK**: Aligned with EU GDPR but independent enforcement

### Canada
- **CRA Tax**: 6 years from end of tax year
- **PIPEDA Personal Info**: Reasonable period for business purpose
- **Quebec (Bill 64)**: Specific consent and retention rules

### Australia
- **ATO Tax**: 5 years after prepared or obtained
- **Fair Work**: 7 years for employee records
- **Privacy Act**: Reasonable period, destroy when no longer needed

### Implementation Strategy
```kotlin
data class RetentionPolicy(
    val dataClass: String,
    val jurisdiction: String,
    val retentionYears: Int,
    val archiveTier: ArchiveTier,
    val legalBasis: String
)

object RetentionRuleEngine {
    fun getRetentionPolicy(
        dataClass: String,
        tenantJurisdiction: String,
        dataCreatedAt: Instant
    ): RetentionPolicy {
        // Example: US tenant, financial transaction
        if (dataClass == "A1" && tenantJurisdiction == "US") {
            return RetentionPolicy(
                dataClass = "A1",
                jurisdiction = "US",
                retentionYears = 7,
                archiveTier = ArchiveTier.COLD_AFTER_2_YEARS,
                legalBasis = "SOX Section 802, IRS 26 CFR 1.6001-1"
            )
        }
        
        // Default to most conservative (longest) retention
        return getConservativeDefault(dataClass)
    }
    
    fun calculatePurgeDate(
        dataCreatedAt: Instant,
        policy: RetentionPolicy
    ): Instant {
        return dataCreatedAt.plus(policy.retentionYears.years)
    }
}
```

### Archiving Strategy
- **Online (Hot)**: Active data in primary databases.
- **Warm**: Archived partitions or separate schema for queryable history.
- **Cold**: Object storage (immutable) for long-term compliance.
- **Restore**: Standard tooling to rehydrate archived data for audits or legal requests.

### Archiving Strategy Implementation

### Storage Tiers

| Tier | Storage Type | Performance | Cost | Use Case | Retention | Technology |
|------|-------------|-------------|------|----------|-----------|------------|
| **Hot (Online)** | PostgreSQL primary | <10ms reads | $$$ | Active transactions, current period | Current + 1 prior fiscal year | PostgreSQL with NVMe SSD |
| **Warm (Nearline)** | Partitioned tables or archive schema | 50-200ms reads | $$ | Historical queries, audits | 2-7 years | PostgreSQL partitions, compressed |
| **Cold (Offline)** | Object storage (S3/Azure Blob) | Minutes to restore | $ | Long-term compliance, legal hold | 7-10+ years | S3 Glacier/Intelligent Tiering |
| **Frozen (Vault)** | Immutable object storage | Hours to restore | $ (minimal) | Regulatory archives post-retention | Permanent until legal purge | S3 Glacier Deep Archive |

### Archiving Workflow

```kotlin
interface ArchivingService {
    /**
     * Move data from hot to warm tier based on age policy
     */
    suspend fun archiveToWarm(
        context: BoundedContext,
        dataClass: String,
        olderThan: Duration
    ): ArchiveResult
    
    /**
     * Move data from warm to cold (object storage)
     */
    suspend fun archiveToCold(
        context: BoundedContext,
        dataClass: String,
        olderThan: Duration
    ): ArchiveResult
    
    /**
     * Restore archived data for audit/legal inquiry
     */
    suspend fun restoreFromArchive(
        archiveId: String,
        targetSchema: String,
        requestedBy: String,
        justification: String
    ): RestoreResult
    
    /**
     * Permanent purge after retention expiry (with legal hold check)
     */
    suspend fun purgeExpiredData(
        dataClass: String,
        expiredBefore: Instant
    ): PurgeResult
}

data class ArchiveResult(
    val recordsArchived: Long,
    val bytesArchived: Long,
    val archiveLocation: String,
    val archiveManifest: String, // JSON manifest with metadata
    val checksumSHA256: String
)

// Example: Financial Accounting Archive Job
class FinancialAccountingArchiveJob : ScheduledJob {
    override suspend fun execute() {
        // Move journal entries older than 2 years to warm storage
        archivingService.archiveToWarm(
            context = BoundedContext.FINANCIAL_ACCOUNTING,
            dataClass = "A1",
            olderThan = Duration.ofDays(730)
        )
        
        // Move journal entries older than 7 years to cold storage
        archivingService.archiveToCold(
            context = BoundedContext.FINANCIAL_ACCOUNTING,
            dataClass = "A1",
            olderThan = Duration.ofDays(2555) // ~7 years
        )
        
        // Check for purge eligibility (10 years + no legal hold)
        val purgeEligible = findRecordsEligibleForPurge(
            dataClass = "A1",
            retentionExpired = Instant.now().minus(10.years)
        )
        
        purgeEligible.forEach { batch ->
            if (!hasActiveLegalHold(batch)) {
                archivingService.purgeExpiredData(
                    dataClass = "A1",
                    expiredBefore = batch.oldestTimestamp
                )
            }
        }
    }
}
```

### PostgreSQL Partitioning Strategy

```sql
-- Example: Journal Entry table with yearly partitioning
CREATE TABLE financial_accounting.journal_entry (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    posting_date DATE NOT NULL,
    fiscal_year INT NOT NULL,
    document_type VARCHAR(20),
    amount NUMERIC(19,2),
    company_code VARCHAR(4),
    created_at TIMESTAMP NOT NULL,
    -- ... other fields
) PARTITION BY RANGE (fiscal_year);

-- Hot: Current + 1 prior year (2025, 2026)
CREATE TABLE financial_accounting.journal_entry_2026
    PARTITION OF financial_accounting.journal_entry
    FOR VALUES FROM (2026) TO (2027);

CREATE TABLE financial_accounting.journal_entry_2025
    PARTITION OF financial_accounting.journal_entry
    FOR VALUES FROM (2025) TO (2026);

-- Warm: 2-7 years old (compressed)
CREATE TABLE financial_accounting.journal_entry_2024
    PARTITION OF financial_accounting.journal_entry
    FOR VALUES FROM (2024) TO (2025)
    WITH (COMPRESSION = lz4); -- PostgreSQL 14+ or TimescaleDB

CREATE TABLE financial_accounting.journal_entry_2023
    PARTITION OF financial_accounting.journal_entry
    FOR VALUES FROM (2023) TO (2024)
    WITH (COMPRESSION = lz4);

-- Cold: Older than 7 years archived to S3
-- Partitions dropped after archival, metadata retained
```

### Archive Manifest Format

```json
{
  "archiveId": "FIN-ACCT-JE-2019-20231201",
  "boundedContext": "financial_accounting",
  "dataClass": "A1",
  "entityType": "journal_entry",
  "fiscalYear": 2019,
  "archivedAt": "2023-12-01T00:00:00Z",
  "archivedBy": "system-archive-job",
  "recordCount": 125847,
  "sizeBytes": 45728394,
  "storageLocation": "s3://chiroerp-archive-us-east/financial/2019/journal_entry.parquet.gz",
  "checksumSHA256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "retentionPolicy": {
    "dataClass": "A1",
    "jurisdiction": "US",
    "retentionYears": 10,
    "purgeEligibleDate": "2029-12-31T23:59:59Z"
  },
  "legalHold": false,
  "encryptionKeyId": "arn:aws:kms:us-east-1:123456789:key/abc-123"
}
```

### Legal Hold & e-Discovery
- Legal hold overrides retention and purge schedules.
- Preserve audit trails and evidence of hold actions.
- Access restricted to authorized compliance roles.

### Legal Hold & e-Discovery Implementation

### Legal Hold Workflow

```kotlin
data class LegalHold(
    val holdId: String,
    val caseNumber: String,
    val caseName: String,
    val requestedBy: String, // Legal/Compliance officer
    val requestedAt: Instant,
    val approvedBy: String,
    val approvedAt: Instant,
    val scope: LegalHoldScope,
    val status: LegalHoldStatus,
    val notificationSent: Boolean,
    val custodians: List<String>, // Users whose data is on hold
    val preservationOrder: String // Court order or internal reference
)

data class LegalHoldScope(
    val tenantIds: List<String>,
    val dataClasses: List<String>,
    val entities: List<String>, // e.g., "customer", "vendor", "employee"
    val dateRangeStart: Instant?,
    val dateRangeEnd: Instant?,
    val keywords: List<String>?, // For targeted preservation
    val custodians: List<String>? // Specific user IDs
)

enum class LegalHoldStatus {
    PENDING_APPROVAL,
    ACTIVE,
    RELEASED,
    EXPIRED
}

interface LegalHoldService {
    /**
     * Initiate legal hold (requires compliance role authorization)
     */
    suspend fun createLegalHold(
        request: LegalHoldRequest,
        requestedBy: String
    ): LegalHold
    
    /**
     * Check if data is under active legal hold before purge
     */
    suspend fun isUnderLegalHold(
        tenantId: String,
        dataClass: String,
        entityId: String
    ): Boolean
    
    /**
     * Release legal hold after case closure
     */
    suspend fun releaseLegalHold(
        holdId: String,
        releasedBy: String,
        justification: String
    ): LegalHoldReleaseResult
    
    /**
     * Generate preservation report for e-discovery
     */
    suspend fun generatePreservationReport(
        holdId: String
    ): PreservationReport
}

// Example: Prevent purge if legal hold exists
class DataPurgeService {
    suspend fun purgeExpiredData(
        tenantId: String,
        dataClass: String,
        expiredBefore: Instant
    ) {
        val candidates = findPurgeCandidates(dataClass, expiredBefore)
        
        candidates.forEach { record ->
            // Check legal hold before deletion
            if (legalHoldService.isUnderLegalHold(
                tenantId, dataClass, record.id
            )) {
                logger.warn("Skipping purge for ${record.id}: active legal hold")
                auditLog.log(
                    event = "PURGE_BLOCKED_LEGAL_HOLD",
                    entityId = record.id,
                    dataClass = dataClass
                )
                return@forEach // Skip this record
            }
            
            // Safe to purge
            deleteRecord(record)
            auditLog.log(
                event = "DATA_PURGED",
                entityId = record.id,
                retentionExpired = expiredBefore
            )
        }
    }
}
```

### e-Discovery Export

```kotlin
data class eDiscoveryRequest(
    val requestId: String,
    val legalHoldId: String?,
    val requestedBy: String,
    val scope: DiscoveryScope,
    val format: ExportFormat, // PST, CSV, JSON, native
    val encryptionRequired: Boolean,
    val deadline: Instant
)

data class DiscoveryScope(
    val tenantIds: List<String>,
    val custodians: List<String>,
    val dateRange: DateRange,
    val keywords: List<String>,
    val dataClasses: List<String>
)

enum class ExportFormat {
    PST,        // For email-like data
    CSV,        // Tabular data
    JSON,       // Structured data
    NATIVE,     // Original format
    LOAD_FILE   // Concordance/metadata
}

interface eDiscoveryService {
    /**
     * Create e-discovery export package
     */
    suspend fun createExportPackage(
        request: eDiscoveryRequest
    ): ExportPackage
    
    /**
     * Generate load file for legal review platform (Relativity, Concordance)
     */
    suspend fun generateLoadFile(
        exportId: String,
        format: LoadFileFormat
    ): LoadFile
}

// Example: Export financial transactions for litigation
class FinancialEDiscoveryExporter {
    suspend fun exportForLitigation(
        caseNumber: String,
        vendorId: String,
        dateRange: DateRange
    ): ExportPackage {
        val scope = DiscoveryScope(
            tenantIds = listOf(getCurrentTenant()),
            custodians = getUsersWhoAccessedVendor(vendorId),
            dateRange = dateRange,
            keywords = listOf(vendorId, "payment", "invoice"),
            dataClasses = listOf("A3") // AP/AR data
        )
        
        return eDiscoveryService.createExportPackage(
            eDiscoveryRequest(
                requestId = UUID.randomUUID().toString(),
                legalHoldId = getLegalHoldForCase(caseNumber),
                requestedBy = getCurrentUser(),
                scope = scope,
                format = ExportFormat.CSV,
                encryptionRequired = true,
                deadline = Instant.now().plus(7.days)
            )
        )
    }
}
```

### Audit Trail for Legal Hold

```kotlin
// Every legal hold action must be audited
sealed class LegalHoldAuditEvent {
    data class HoldCreated(
        val holdId: String,
        val caseNumber: String,
        val scope: LegalHoldScope,
        val requestedBy: String
    ) : LegalHoldAuditEvent()
    
    data class HoldReleased(
        val holdId: String,
        val releasedBy: String,
        val justification: String
    ) : LegalHoldAuditEvent()
    
    data class PurgeBlocked(
        val entityId: String,
        val holdId: String,
        val attemptedBy: String
    ) : LegalHoldAuditEvent()
    
    data class DataExported(
        val exportId: String,
        val holdId: String?,
        val recordCount: Long,
        val exportedBy: String
    ) : LegalHoldAuditEvent()
}
```

### Backup & Recovery
- Define **RPO/RTO** targets per context tier:
  - Tier 1 (Financial, Identity): RPO <= 15 minutes, RTO <= 2 hours.
  - Tier 2 (Core Ops): RPO <= 1 hour, RTO <= 6 hours.
  - Tier 3 (Reporting/BI): RPO <= 24 hours, RTO <= 24 hours.
- Encrypted backups with key rotation and off-site replication.
- Quarterly restore drills; audit artifacts stored.

### Backup & Recovery Strategy

### RPO/RTO Matrix

| Tier | Contexts | RPO Target | RTO Target | Backup Frequency | Backup Method | Recovery Method |
|------|----------|-----------|-----------|------------------|---------------|-----------------|
| **Tier 1 - Critical** | Financial Accounting, Tenancy/Identity, Accounts Payable, Accounts Receivable | ≤ 15 min | ≤ 2 hours | Continuous (WAL streaming) | PostgreSQL streaming replication + PITR | Automated failover + point-in-time recovery |
| **Tier 2 - Core Ops** | Customer Relations, Inventory, Sales Orders, Procurement | ≤ 1 hour | ≤ 6 hours | Hourly incrementals + daily full | PostgreSQL pg_basebackup + WAL archiving | Restore from backup + replay WAL |
| **Tier 3 - Supporting** | Reporting/BI, Analytics, Document Management | ≤ 4 hours | ≤ 24 hours | Daily full | Snapshot or pg_dump | Restore from snapshot |

### Backup Implementation

```kotlin
data class BackupPolicy(
    val policyId: String,
    val tier: BackupTier,
    val rpoMinutes: Int,
    val rtoMinutes: Int,
    val frequency: BackupFrequency,
    val retentionDays: Int,
    val encryptionEnabled: Boolean,
    val offSiteReplication: Boolean,
    val verificationRequired: Boolean
)

enum class BackupTier {
    TIER_1_CRITICAL,
    TIER_2_CORE,
    TIER_3_SUPPORTING
}

enum class BackupFrequency {
    CONTINUOUS,      // WAL streaming
    HOURLY,          // Incremental
    DAILY,           // Full
    WEEKLY           // Archive
}

interface BackupService {
    /**
     * Execute backup based on policy
     */
    suspend fun executeBackup(
        context: BoundedContext,
        policy: BackupPolicy
    ): BackupResult
    
    /**
     * Verify backup integrity
     */
    suspend fun verifyBackup(
        backupId: String
    ): BackupVerificationResult
    
    /**
     * Restore from backup to specific point in time
     */
    suspend fun restoreToPointInTime(
        context: BoundedContext,
        targetTimestamp: Instant,
        targetEnvironment: Environment
    ): RestoreResult
    
    /**
     * Execute disaster recovery failover
     */
    suspend fun executeDRFailover(
        context: BoundedContext,
        targetRegion: String
    ): FailoverResult
}
```

### PostgreSQL Continuous Backup (Tier 1)

```yaml
# PostgreSQL Streaming Replication Config
# Primary Database (US-East-1)
primary:
  host: pg-primary-01.chiroerp.internal
  port: 5432
  wal_level: replica
  max_wal_senders: 10
  wal_keep_size: 1GB
  archive_mode: on
  archive_command: 'aws s3 cp %p s3://chiroerp-wal-archive/primary/%f'

# Hot Standby (US-East-1, Same AZ for HA)
hot_standby_local:
  host: pg-standby-01.chiroerp.internal
  port: 5432
  hot_standby: on
  max_standby_streaming_delay: 30s
  wal_receiver_timeout: 5s
  
# Warm Standby (US-West-2, DR Region)
warm_standby_dr:
  host: pg-standby-dr-01.chiroerp-dr.internal
  port: 5432
  recovery_mode: standby
  recovery_target_timeline: latest
  restore_command: 'aws s3 cp s3://chiroerp-wal-archive/primary/%f %p'
```

```kotlin
// Automated Failover Logic (Tier 1)
class PostgreSQLFailoverOrchestrator {
    suspend fun detectPrimaryFailure(): Boolean {
        val healthChecks = listOf(
            checkDatabaseConnection(),
            checkReplicationLag(),
            checkDiskSpace(),
            checkCPULoad()
        )
        
        // Fail if 2+ health checks fail within 30 seconds
        return healthChecks.count { !it } >= 2
    }
    
    suspend fun executeAutomatedFailover() {
        logger.critical("Primary database failure detected. Initiating failover...")
        
        // 1. Promote hot standby to primary
        promoteToPrimary("pg-standby-01")
        
        // 2. Update DNS to point to new primary
        updateDNSRecord("pg-primary.chiroerp.internal", "pg-standby-01")
        
        // 3. Notify operations team
        sendAlert(
            severity = Severity.CRITICAL,
            message = "Database failover completed. New primary: pg-standby-01"
        )
        
        // 4. Audit log
        auditLog.log(
            event = "DR_FAILOVER_EXECUTED",
            targetHost = "pg-standby-01",
            downtime = measureDowntime()
        )
        
        logger.info("Failover completed. RTO: ${measureDowntime()}")
    }
}
```

### Backup Retention Schedule

```kotlin
data class BackupRetentionSchedule(
    val daily: Int,    // Keep daily backups for X days
    val weekly: Int,   // Keep weekly backups for X weeks
    val monthly: Int,  // Keep monthly backups for X months
    val yearly: Int    // Keep yearly backups for X years
)

object BackupRetentionPolicy {
    val TIER_1 = BackupRetentionSchedule(
        daily = 30,    // 30 daily backups
        weekly = 12,   // 12 weekly backups (3 months)
        monthly = 24,  // 24 monthly backups (2 years)
        yearly = 7     // 7 yearly backups
    )
    
    val TIER_2 = BackupRetentionSchedule(
        daily = 14,    // 14 daily backups
        weekly = 8,    // 8 weekly backups (2 months)
        monthly = 12,  // 12 monthly backups (1 year)
        yearly = 5     // 5 yearly backups
    )
    
    val TIER_3 = BackupRetentionSchedule(
        daily = 7,     // 7 daily backups
        weekly = 4,    // 4 weekly backups (1 month)
        monthly = 6,   // 6 monthly backups
        yearly = 3     // 3 yearly backups
    )
    
    fun shouldRetain(backup: Backup, schedule: BackupRetentionSchedule): Boolean {
        val age = Duration.between(backup.createdAt, Instant.now())
        
        return when {
            // Keep all daily backups within retention window
            age < Duration.ofDays(schedule.daily.toLong()) -> true
            
            // Keep weekly backups (Sunday) within retention window
            backup.createdAt.dayOfWeek == DayOfWeek.SUNDAY &&
            age < Duration.ofDays(schedule.weekly * 7L) -> true
            
            // Keep monthly backups (1st of month) within retention window
            backup.createdAt.dayOfMonth == 1 &&
            age < Duration.ofDays(schedule.monthly * 30L) -> true
            
            // Keep yearly backups (Jan 1st) within retention window
            backup.createdAt.month == Month.JANUARY &&
            backup.createdAt.dayOfMonth == 1 &&
            age < Duration.ofDays(schedule.yearly * 365L) -> true
            
            else -> false
        }
    }
}
```

### Quarterly Disaster Recovery Drills

```kotlin
data class DRDrill(
    val drillId: String,
    val scheduledDate: LocalDate,
    val contexts: List<BoundedContext>,
    val drillType: DrillType,
    val targetRTO: Duration,
    val targetRPO: Duration,
    val participants: List<String>,
    val results: DrillResults?
)

enum class DrillType {
    FULL_FAILOVER,           // Complete region failover
    PARTIAL_RESTORE,         // Single context restore
    POINT_IN_TIME_RECOVERY,  // PITR test
    DATA_CENTER_OUTAGE       // Simulate full DC failure
}

data class DrillResults(
    val success: Boolean,
    val actualRTO: Duration,
    val actualRPO: Duration,
    val issuesEncountered: List<String>,
    val lessonsLearned: List<String>,
    val actionItems: List<String>
)

class DisasterRecoveryDrillService {
    suspend fun executeDrill(drill: DRDrill): DrillResults {
        logger.info("Starting DR drill: ${drill.drillType}")
        val startTime = Instant.now()
        
        try {
            when (drill.drillType) {
                DrillType.FULL_FAILOVER -> executeFullFailoverDrill(drill)
                DrillType.PARTIAL_RESTORE -> executePartialRestoreDrill(drill)
                DrillType.POINT_IN_TIME_RECOVERY -> executePITRDrill(drill)
                DrillType.DATA_CENTER_OUTAGE -> executeDCOutageDrill(drill)
            }
            
            val actualRTO = Duration.between(startTime, Instant.now())
            
            return DrillResults(
                success = true,
                actualRTO = actualRTO,
                actualRPO = measureDataLoss(),
                issuesEncountered = emptyList(),
                lessonsLearned = listOf("Drill completed successfully"),
                actionItems = emptyList()
            )
            
        } catch (e: Exception) {
            logger.error("DR drill failed", e)
            return DrillResults(
                success = false,
                actualRTO = Duration.between(startTime, Instant.now()),
                actualRPO = Duration.ZERO,
                issuesEncountered = listOf(e.message ?: "Unknown error"),
                lessonsLearned = emptyList(),
                actionItems = listOf("Investigate drill failure: ${e.message}")
            )
        } finally {
            // Always log drill execution
            auditLog.log(
                event = "DR_DRILL_EXECUTED",
                drillId = drill.drillId,
                drillType = drill.drillType,
                duration = Duration.between(startTime, Instant.now())
            )
        }
    }
    
    suspend fun generateDrillReport(drillId: String): DrillReport {
        val drill = getDrill(drillId)
        val results = drill.results!!
        
        return DrillReport(
            drillId = drillId,
            executedAt = drill.scheduledDate,
            rtoComparison = RTOComparison(
                target = drill.targetRTO,
                actual = results.actualRTO,
                metTarget = results.actualRTO <= drill.targetRTO
            ),
            rpoComparison = RPOComparison(
                target = drill.targetRPO,
                actual = results.actualRPO,
                metTarget = results.actualRPO <= drill.targetRPO
            ),
            recommendations = results.actionItems
        )
    }
}

// Schedule: Quarterly drills for all Tier 1 contexts
@Scheduled(cron = "0 0 2 1 */3 *") // 2 AM on 1st of every 3rd month
suspend fun scheduledDRDrill() {
    val drill = DRDrill(
        drillId = UUID.randomUUID().toString(),
        scheduledDate = LocalDate.now(),
        contexts = listOf(
            BoundedContext.FINANCIAL_ACCOUNTING,
            BoundedContext.TENANCY_IDENTITY
        ),
        drillType = DrillType.POINT_IN_TIME_RECOVERY,
        targetRTO = Duration.ofHours(2),
        targetRPO = Duration.ofMinutes(15),
        participants = listOf("ops-team@chiroerp.com", "dba-team@chiroerp.com")
    )
    
    val results = drDrillService.executeDrill(drill)
    val report = drDrillService.generateDrillReport(drill.drillId)
    
    sendDrillReportToStakeholders(report)
}
```

### Data Minimization & Privacy
- PII masking in non-prod environments.
- Anonymization/pseudonymization for archived datasets used in analytics.
- Right-to-erasure workflows with audit evidence, respecting legal hold exceptions.

### Data Minimization & Privacy Controls

### PII Masking for Non-Production

```kotlin
interface DataMaskingService {
    /**
     * Mask PII fields when copying to non-prod environments
     */
    fun maskPII(value: String, fieldType: PIIFieldType): String
    
    /**
     * Create sanitized dataset for development/testing
     */
    suspend fun createSanitizedDataset(
        sourceSchema: String,
        targetEnvironment: Environment
    ): SanitizationResult
}

enum class PIIFieldType {
    SSN,              // 123-45-6789 -> XXX-XX-6789
    EMAIL,            // john.doe@example.com -> j***@example.com
    PHONE,            // (555) 123-4567 -> (XXX) XXX-4567
    CREDIT_CARD,      // 4111-1111-1111-1111 -> XXXX-XXXX-XXXX-1111
    NAME,             // John Doe -> J*** D***
    ADDRESS,          // 123 Main St -> XXX Main St
    BANK_ACCOUNT,     // 1234567890 -> XXXXXX7890
    SALARY            // 150000 -> <redacted> or randomized range
}

class PIIMaskingService : DataMaskingService {
    override fun maskPII(value: String, fieldType: PIIFieldType): String {
        return when (fieldType) {
            PIIFieldType.SSN -> value.takeLast(4).let { "XXX-XX-$it" }
            PIIFieldType.EMAIL -> {
                val parts = value.split("@")
                "${parts[0].first()}***@${parts[1]}"
            }
            PIIFieldType.PHONE -> value.takeLast(4).let { "(XXX) XXX-$it" }
            PIIFieldType.CREDIT_CARD -> value.takeLast(4).let { "XXXX-XXXX-XXXX-$it" }
            PIIFieldType.NAME -> value.split(" ").joinToString(" ") { 
                "${it.first()}***" 
            }
            PIIFieldType.ADDRESS -> "*** ${value.split(" ").drop(1).joinToString(" ")}"
            PIIFieldType.BANK_ACCOUNT -> value.takeLast(4).let { "XXXXXX$it" }
            PIIFieldType.SALARY -> "<redacted>"
        }
    }
    
    override suspend fun createSanitizedDataset(
        sourceSchema: String,
        targetEnvironment: Environment
    ): SanitizationResult {
        require(targetEnvironment != Environment.PRODUCTION) {
            "Cannot sanitize production environment"
        }
        
        // Identify PII columns
        val piiColumns = identifyPIIColumns(sourceSchema)
        
        // Copy data with masking
        val recordsProcessed = copyWithMasking(
            source = sourceSchema,
            target = "${sourceSchema}_${targetEnvironment.name.toLowerCase()}",
            maskingRules = piiColumns
        )
        
        return SanitizationResult(
            recordsProcessed = recordsProcessed,
            columnsM masked = piiColumns.size,
            targetSchema = "${sourceSchema}_${targetEnvironment.name.toLowerCase()}"
        )
    }
}

// Example: Sanitize customer data for dev environment
suspend fun sanitizeCustomerDataForDev() {
    val result = maskingService.createSanitizedDataset(
        sourceSchema = "customer_relation",
        targetEnvironment = Environment.DEVELOPMENT
    )
    
    logger.info("""
        Sanitized ${result.recordsProcessed} records
        Masked ${result.columnsMasked} PII columns
        Target: ${result.targetSchema}
    """.trimIndent())
}
```

### Anonymization for Analytics

```kotlin
data class AnonymizationPolicy(
    val technique: AnonymizationTechnique,
    val kAnonymity: Int = 5,  // K-anonymity: each record indistinguishable from k-1 others
    val lDiversity: Int = 2,  // L-diversity: sensitive attributes have at least l values
    val suppressionThreshold: Double = 0.05 // Suppress if >5% would be removed
)

enum class AnonymizationTechnique {
    GENERALIZATION,   // Age 32 -> Age range 30-40
    SUPPRESSION,      // Remove identifying attributes
    PERTURBATION,     // Add noise to numerical values
    PSEUDONYMIZATION, // Replace with consistent fake identifiers
    AGGREGATION       // Only provide aggregate statistics
}

class AnalyticsAnonymizationService {
    /**
     * Create anonymized dataset for analytics/BI
     */
    suspend fun anonymizeForAnalytics(
        sourceTable: String,
        policy: AnonymizationPolicy
    ): AnonymizedDataset {
        val records = fetchRecords(sourceTable)
        
        return when (policy.technique) {
            AnonymizationTechnique.GENERALIZATION -> {
                generalizeAttributes(records, policy)
            }
            AnonymizationTechnique.SUPPRESSION -> {
                suppressIdentifyingAttributes(records, policy)
            }
            AnonymizationTechnique.PERTURBATION -> {
                perturbNumericalValues(records, policy)
            }
            AnonymizationTechnique.PSEUDONYMIZATION -> {
                pseudonymizeIdentifiers(records, policy)
            }
            AnonymizationTechnique.AGGREGATION -> {
                aggregateToPreventReidentification(records, policy)
            }
        }
    }
    
    private fun generalizeAttributes(
        records: List<Record>,
        policy: AnonymizationPolicy
    ): AnonymizedDataset {
        // Example: Generalize age into ranges
        val anonymized = records.map { record ->
            record.copy(
                age = generalizeAge(record.age),
                zipCode = generalizeZipCode(record.zipCode), // 12345 -> 123XX
                salary = generalizeSalary(record.salary) // 75000 -> 70000-80000
            )
        }
        
        // Verify k-anonymity
        val satisfiesKAnonymity = verifyKAnonymity(anonymized, policy.kAnonymity)
        require(satisfiesKAnonymity) {
            "Dataset does not satisfy k-anonymity requirement"
        }
        
        return AnonymizedDataset(
            records = anonymized,
            technique = policy.technique,
            kAnonymity = policy.kAnonymity
        )
    }
    
    private fun generalizeAge(age: Int): String {
        return when (age) {
            in 0..17 -> "0-17"
            in 18..24 -> "18-24"
            in 25..34 -> "25-34"
            in 35..44 -> "35-44"
            in 45..54 -> "45-54"
            in 55..64 -> "55-64"
            else -> "65+"
        }
    }
}
```

### Right to Erasure (GDPR Article 17)

```kotlin
data class ErasureRequest(
    val requestId: String,
    val dataSubjectId: String, // Customer/Employee ID
    val requestedBy: String,
    val requestedAt: Instant,
    val reason: ErasureReason,
    val scope: ErasureScope,
    val status: ErasureStatus
)

enum class ErasureReason {
    GDPR_ARTICLE_17,        // Right to erasure
    CCPA_RIGHT_TO_DELETE,   // California Consumer Privacy Act
    CONSENT_WITHDRAWN,      // User withdrew consent
    DATA_NO_LONGER_NECESSARY,
    OBJECTION_TO_PROCESSING
}

data class ErasureScope(
    val includeTransactionalHistory: Boolean,
    val includeAuditLogs: Boolean,
    val includeBackups: Boolean
)

enum class ErasureStatus {
    PENDING_VERIFICATION,
    LEGAL_HOLD_CHECK,
    APPROVED,
    PARTIALLY_COMPLETED,
    COMPLETED,
    REJECTED
}

interface DataErasureService {
    /**
     * Process right-to-erasure request with legal hold check
     */
    suspend fun processErasureRequest(
        request: ErasureRequest
    ): ErasureResult
    
    /**
     * Verify no legal holds prevent erasure
     */
    suspend fun checkLegalHoldExceptions(
        dataSubjectId: String
    ): List<LegalHold>
    
    /**
     * Execute erasure across all systems
     */
    suspend fun executeErasure(
        request: ErasureRequest
    ): ErasureExecutionResult
}

class GDPRErasureService : DataErasureService {
    override suspend fun processErasureRequest(
        request: ErasureRequest
    ): ErasureResult {
        // 1. Verify identity of requester
        verifyDataSubjectIdentity(request.dataSubjectId, request.requestedBy)
        
        // 2. Check for legal hold exceptions
        val legalHolds = checkLegalHoldExceptions(request.dataSubjectId)
        if (legalHolds.isNotEmpty()) {
            return ErasureResult.Rejected(
                reason = "Active legal hold prevents erasure",
                legalHolds = legalHolds
            )
        }
        
        // 3. Check for legitimate interests that override right to erasure
        val legitimateInterests = checkLegitimateInterests(request.dataSubjectId)
        if (legitimateInterests.isNotEmpty()) {
            return ErasureResult.PartiallyApproved(
                reason = "Some data must be retained for legitimate interests",
                retainedData = legitimateInterests,
                erasableData = identifyErasableData(request.dataSubjectId)
            )
        }
        
        // 4. Execute erasure
        val executionResult = executeErasure(request)
        
        // 5. Audit log
        auditLog.log(
            event = "DATA_ERASURE_COMPLETED",
            dataSubjectId = request.dataSubjectId,
            reason = request.reason,
            recordsErased = executionResult.recordsErased
        )
        
        return ErasureResult.Completed(executionResult)
    }
    
    override suspend fun executeErasure(
        request: ErasureRequest
    ): ErasureExecutionResult {
        val recordsErased = mutableMapOf<String, Int>()
        
        // Erase from all bounded contexts
        BoundedContext.values().forEach { context ->
            val erased = eraseFromContext(context, request.dataSubjectId)
            recordsErased[context.name] = erased
        }
        
        // Erase from backups (if scope includes backups)
        if (request.scope.includeBackups) {
            markForErasureInBackups(request.dataSubjectId)
        }
        
        // Retain audit trail of erasure request (GDPR allows this)
        if (!request.scope.includeAuditLogs) {
            retainErasureAuditTrail(request)
        }
        
        return ErasureExecutionResult(
            requestId = request.requestId,
            recordsErased = recordsErased,
            totalRecords = recordsErased.values.sum(),
            completedAt = Instant.now()
        )
    }
    
    private suspend fun checkLegitimateInterests(
        dataSubjectId: String
    ): List<LegitimateInterest> {
        val interests = mutableListOf<LegitimateInterest>()
        
        // Example: Open invoices must be retained for accounting
        val openInvoices = findOpenInvoices(dataSubjectId)
        if (openInvoices.isNotEmpty()) {
            interests.add(
                LegitimateInterest(
                    reason = "Outstanding financial obligations",
                    dataClass = "A3",
                    retentionBasis = "Commercial law requires retention of open receivables",
                    records = openInvoices.size
                )
            )
        }
        
        // Example: Tax records must be retained for statute of limitations
        val recentTaxRecords = findTaxRecordsWithinStatute(dataSubjectId)
        if (recentTaxRecords.isNotEmpty()) {
            interests.add(
                LegitimateInterest(
                    reason = "Tax compliance obligations",
                    dataClass = "A2",
                    retentionBasis = "IRS requires 7-year retention",
                    records = recentTaxRecords.size
                )
            )
        }
        
        return interests
    }
}

// Example: Customer requests data erasure
suspend fun handleCustomerErasureRequest(customerId: String, customerEmail: String) {
    val request = ErasureRequest(
        requestId = UUID.randomUUID().toString(),
        dataSubjectId = customerId,
        requestedBy = customerEmail,
        requestedAt = Instant.now(),
        reason = ErasureReason.GDPR_ARTICLE_17,
        scope = ErasureScope(
            includeTransactionalHistory = true,
            includeAuditLogs = false, // Retain audit trail per GDPR recital 39
            includeBackups = true
        ),
        status = ErasureStatus.PENDING_VERIFICATION
    )
    
    val result = erasureService.processErasureRequest(request)
    
    when (result) {
        is ErasureResult.Completed -> {
            sendErasureConfirmation(customerEmail, result)
        }
        is ErasureResult.PartiallyApproved -> {
            sendPartialErasureNotice(customerEmail, result)
        }
        is ErasureResult.Rejected -> {
            sendErasureRejectionNotice(customerEmail, result)
        }
    }
}
```

### Tenant Isolation & Residency
- Retention and archiving must respect tenant isolation tier (row/schema/DB).
- Regional data residency enforced for both primary and archive storage.

### Tenant Isolation & Data Residency

### Multi-Tenant Data Lifecycle

```kotlin
data class TenantDataLifecyclePolicy(
    val tenantId: String,
    val isolationTier: IsolationTier,
    val dataResidency: DataResidency,
    val retentionOverrides: Map<String, RetentionPolicy>, // Override defaults per data class
    val archiveStrategy: ArchiveStrategy,
    val backupPolicy: BackupPolicy
)

enum class IsolationTier {
    ROW_LEVEL,      // Standard tier: shared schema, row-level isolation
    SCHEMA_LEVEL,   // Premium tier: dedicated schema per tenant
    DATABASE_LEVEL  // Enterprise tier: dedicated database
}

data class DataResidency(
    val primaryRegion: String,      // e.g., "us-east-1", "eu-west-1"
    val allowedRegions: List<String>,
    val prohibitedRegions: List<String>,
    val crossBorderTransferAllowed: Boolean,
    val regulatoryFramework: RegulatoryFramework
)

enum class RegulatoryFramework {
    GDPR_EU,           // European Union
    GDPR_UK,           // United Kingdom post-Brexit
    CCPA_CALIFORNIA,   // California Consumer Privacy Act
    PIPEDA_CANADA,     // Personal Information Protection
    APPI_JAPAN,        // Act on Protection of Personal Information
    LGPD_BRAZIL,       // Lei Geral de Proteção de Dados
    POPIA_SOUTH_AFRICA // Protection of Personal Information Act
}

class TenantDataLifecycleService {
    suspend fun archiveForTenant(
        tenantId: String,
        dataClass: String
    ) {
        val policy = getTenantPolicy(tenantId)
        
        // Ensure archive respects residency
        val archiveLocation = determineArchiveLocation(
            primaryRegion = policy.dataResidency.primaryRegion,
            allowedRegions = policy.dataResidency.allowedRegions
        )
        
        // Execute archival based on isolation tier
        when (policy.isolationTier) {
            IsolationTier.ROW_LEVEL -> {
                archiveService.archiveToWarm(
                    schema = "shared",
                    filter = "tenant_id = '$tenantId'",
                    dataClass = dataClass
                )
            }
            IsolationTier.SCHEMA_LEVEL -> {
                archiveService.archiveToWarm(
                    schema = "tenant_$tenantId",
                    filter = null, // All data in schema belongs to tenant
                    dataClass = dataClass
                )
            }
            IsolationTier.DATABASE_LEVEL -> {
                archiveService.archiveToWarm(
                    database = "tenant_$tenantId",
                    dataClass = dataClass
                )
            }
        }
        
        // Store archive manifest with residency metadata
        storeArchiveManifest(
            tenantId = tenantId,
            location = archiveLocation,
            residency = policy.dataResidency
        )
    }
    
    suspend fun backupForTenant(
        tenantId: String
    ) {
        val policy = getTenantPolicy(tenantId)
        
        // Backup to region(s) allowed by residency policy
        policy.dataResidency.allowedRegions.forEach { region ->
            backupService.executeBackup(
                tenantId = tenantId,
                targetRegion = region,
                isolationTier = policy.isolationTier,
                encryption = true
            )
        }
        
        // Enforce cross-border transfer restrictions
        if (!policy.dataResidency.crossBorderTransferAllowed) {
            require(policy.dataResidency.allowedRegions.all { 
                isSameJurisdiction(policy.dataResidency.primaryRegion, it)
            }) {
                "Tenant prohibits cross-border data transfer"
            }
        }
    }
}
```

### Data Residency Enforcement

```kotlin
object DataResidencyEnforcement {
    /**
     * Validate archive location complies with tenant residency policy
     */
    fun validateArchiveLocation(
        tenantId: String,
        proposedLocation: String
    ): ValidationResult {
        val policy = getTenantPolicy(tenantId)
        val residency = policy.dataResidency
        
        val region = extractRegion(proposedLocation)
        
        // Check if region is allowed
        if (region !in residency.allowedRegions) {
            return ValidationResult.Invalid(
                "Archive location $proposedLocation not in allowed regions: ${residency.allowedRegions}"
            )
        }
        
        // Check if region is prohibited
        if (region in residency.prohibitedRegions) {
            return ValidationResult.Invalid(
                "Archive location $proposedLocation is in prohibited regions"
            )
        }
        
        // Check cross-border transfer restrictions
        if (!residency.crossBorderTransferAllowed) {
            if (!isSameJurisdiction(residency.primaryRegion, region)) {
                return ValidationResult.Invalid(
                    "Cross-border transfer to $region prohibited for tenant $tenantId"
                )
            }
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * GDPR-specific: Ensure EU data stays in EU
     */
    fun enforceGDPRResidency(tenantId: String, targetRegion: String): Boolean {
        val policy = getTenantPolicy(tenantId)
        
        if (policy.dataResidency.regulatoryFramework == RegulatoryFramework.GDPR_EU) {
            val euRegions = listOf("eu-west-1", "eu-central-1", "eu-north-1", "eu-south-1")
            return targetRegion in euRegions
        }
        
        return true
    }
}

// Example: EU tenant cannot archive to US
val euTenantPolicy = TenantDataLifecyclePolicy(
    tenantId = "tenant-eu-001",
    isolationTier = IsolationTier.SCHEMA_LEVEL,
    dataResidency = DataResidency(
        primaryRegion = "eu-west-1",
        allowedRegions = listOf("eu-west-1", "eu-central-1"),
        prohibitedRegions = listOf("us-east-1", "us-west-2", "ap-southeast-1"),
        crossBorderTransferAllowed = false,
        regulatoryFramework = RegulatoryFramework.GDPR_EU
    ),
    retentionOverrides = emptyMap(),
    archiveStrategy = ArchiveStrategy.WARM_AFTER_2_YEARS,
    backupPolicy = BackupPolicy.TIER_1
)
```

### Tenant-Specific Retention Overrides

```kotlin
class TenantRetentionService {
    /**
     * Allow tenants to set stricter retention (shorter) than defaults
     * Cannot set longer retention for regulated data classes
     */
    suspend fun setTenantRetentionOverride(
        tenantId: String,
        dataClass: String,
        requestedRetentionYears: Int
    ): Result<RetentionPolicy> {
        val defaultPolicy = getDefaultRetentionPolicy(dataClass)
        
        // Validate override request
        when {
            // Class A (financial): Cannot reduce below regulatory minimum
            dataClass.startsWith("A") -> {
                if (requestedRetentionYears < defaultPolicy.retentionYears) {
                    return Result.failure(
                        IllegalArgumentException(
                            "Cannot reduce retention for $dataClass below ${defaultPolicy.retentionYears} years"
                        )
                    )
                }
            }
            
            // Class B/C: Can reduce, but warn about business implications
            dataClass.startsWith("B") || dataClass.startsWith("C") -> {
                if (requestedRetentionYears < defaultPolicy.retentionYears) {
                    logger.warn(
                        "Tenant $tenantId reducing retention for $dataClass from " +
                        "${defaultPolicy.retentionYears} to $requestedRetentionYears years"
                    )
                }
            }
            
            // Class D (PII): Support GDPR "data minimization" principle
            dataClass.startsWith("D") -> {
                logger.info(
                    "Tenant $tenantId exercising data minimization for $dataClass"
                )
            }
        }
        
        val overridePolicy = RetentionPolicy(
            dataClass = dataClass,
            jurisdiction = defaultPolicy.jurisdiction,
            retentionYears = requestedRetentionYears,
            archiveTier = defaultPolicy.archiveTier,
            legalBasis = "Tenant override (stricter than regulatory minimum)"
        )
        
        // Save override
        saveTenantRetentionOverride(tenantId, overridePolicy)
        
        return Result.success(overridePolicy)
    }
}

// Example: Healthcare tenant needs 10-year medical record retention
tenantRetentionService.setTenantRetentionOverride(
    tenantId = "tenant-healthcare-001",
    dataClass = "D3", // PHI
    requestedRetentionYears = 10 // HIPAA + state law requirement
)
```

### Monitoring & Metrics

### Key Performance Indicators (KPIs)

```kotlin
data class DataLifecycleMetrics(
    // Archiving
    val recordsArchivedDaily: Long,
    val bytesArchivedDaily: Long,
    val archiveJobDuration: Duration,
    val archiveFailureRate: Double,
    
    // Retention & Purge
    val recordsPurgedDaily: Long,
    val bytesPurgedDaily: Long,
    val purgeBlockedByLegalHold: Int,
    
    // Backup & Recovery
    val backupSuccessRate: Double,
    val averageBackupDuration: Duration,
    val actualRPO: Duration,
    val actualRTO: Duration,
    
    // Storage
    val hotStorageTB: Double,
    val warmStorageTB: Double,
    val coldStorageTB: Double,
    val storageCostReduction: Double, // Percentage
    
    // Compliance
    val legalHoldsActive: Int,
    val erasureRequestsPending: Int,
    val erasureRequestsCompleted: Int,
    val residencyViolations: Int,
    
    // Privacy
    val piiMaskingCoverage: Double, // Percentage of non-prod
    val anonymizedDatasetsCreated: Int,
    val kAnonymityViolations: Int
)

// Alert thresholds
object DataLifecycleAlerts {
    fun checkMetrics(metrics: DataLifecycleMetrics) {
        // Archive job performance
        if (metrics.archiveJobDuration > Duration.ofHours(4)) {
            alert(Severity.WARNING, "Archive job duration exceeds 4 hours")
        }
        
        if (metrics.archiveFailureRate > 0.01) { // 1%
            alert(Severity.CRITICAL, "Archive failure rate exceeds 1%")
        }
        
        // Backup SLA violations
        if (metrics.actualRTO > Duration.ofHours(2)) {
            alert(Severity.CRITICAL, "RTO exceeded 2-hour target")
        }
        
        if (metrics.actualRPO > Duration.ofMinutes(15)) {
            alert(Severity.CRITICAL, "RPO exceeded 15-minute target")
        }
        
        // Compliance violations
        if (metrics.residencyViolations > 0) {
            alert(Severity.CRITICAL, "Data residency violations detected")
        }
        
        if (metrics.kAnonymityViolations > 0) {
            alert(Severity.HIGH, "K-anonymity violations in analytics datasets")
        }
        
        // Storage growth
        if (metrics.hotStorageTB > 50.0) { // Example threshold
            alert(Severity.WARNING, "Hot storage exceeds 50 TB, consider archiving")
        }
    }
}
```

### Dashboards

**Executive Dashboard:**
- Total data under management (TB)
- Storage cost trend (monthly)
- Compliance status (green/yellow/red)
- DR drill results (last 4 quarters)

**Operations Dashboard:**
- Archive job status (last 24 hours)
- Backup success rate (last 7 days)
- Purge job statistics (records/bytes purged)
- Legal hold count (active/released)

**Compliance Dashboard:**
- Retention policy coverage (% of data classified)
- Erasure request backlog (count, age)
- Residency compliance by tenant
- DR drill RTO/RPO actuals vs targets

## Alternatives Considered

### Alternative 1: Database-Level Archiving Only
- **Approach**: Rely on PostgreSQL tablespace management and partitioning for hot/warm/cold data separation. No application-level archiving logic.
- **Pros**:
  - Simple implementation (mostly DBA work)
  - No application code changes
  - PostgreSQL native performance optimizations
- **Cons**:
  - No cross-database archiving (limited to single PostgreSQL cluster)
  - Cannot support object storage tiers (S3, Azure Blob)
  - No business logic for retention policies (purely storage-based)
  - Cannot handle GDPR erasure requests (requires application logic)
  - Limited audit trail for data lifecycle events
- **Decision**: Rejected. Does not meet compliance requirements for GDPR, SOX, and HIPAA. Cannot support multi-tier archiving (database → object storage → tape).

### Alternative 2: Application-Level Archiving (Selected Approach)
- **Approach**: Implement archiving logic in application services. Archive jobs read from operational PostgreSQL, write to object storage (S3/Azure Blob), and optionally purge from source. Restoration service queries archive when needed.
- **Pros**:
  - Full control over retention policies (per tenant, per data class)
  - Supports multi-tier archiving (DB → S3 → Glacier)
  - Business logic for GDPR erasure, legal holds, and compliance
  - Rich audit trail (who archived what, when, why)
  - Can archive across bounded contexts (coordination via events)
- **Cons**:
  - Requires development effort (6 months, 3.5 FTE)
  - Operational complexity (archive job scheduling, monitoring)
  - Performance overhead for restoration queries
- **Decision**: Selected. Only approach that satisfies all compliance and business requirements. Aligns with event-driven architecture (archival events trigger downstream cleanup).

### Alternative 3: Cloud Object Storage Only (No Database Archive)
- **Approach**: Write all data directly to object storage (S3, Azure Blob) with lifecycle policies. No PostgreSQL for operational data.
- **Pros**:
  - Infinite scalability
  - Low storage costs
  - Built-in lifecycle management (S3 Lifecycle, Azure Blob tiers)
- **Cons**:
  - Poor query performance (no SQL, no indexes)
  - Cannot support transactional workloads
  - No referential integrity or ACID guarantees
  - Not suitable for operational ERP data
- **Decision**: Rejected for operational data. However, object storage is used as secondary archival tier (after PostgreSQL archive).

### Alternative 4: Third-Party Archival Services (Cohesity, Rubrik, Commvault)
- **Approach**: Use enterprise data management platforms for backup, archiving, and compliance. Agents installed on PostgreSQL servers.
- **Pros**:
  - Mature tooling with compliance features (legal holds, e-discovery)
  - Vendor support for backup/restore operations
  - Integrated disaster recovery capabilities
- **Cons**:
  - High licensing costs ($$$$$ per TB)
  - Vendor lock-in
  - Limited customization for tenant-specific policies
  - Not integrated with application business logic (e.g., GDPR erasure workflows)
  - Requires additional infrastructure (agent deployment, network bandwidth)
- **Decision**: Rejected for primary archiving. Considered for backup/DR tier in Phase 3+ if enterprise customers demand.

### Alternative 5: Hybrid Approach (Database Partitioning + Application Archiving)
- **Approach**: Use PostgreSQL partitioning for recent data (hot partitions), combined with application-level archiving for older data (cold partitions → S3). Best of both worlds.
- **Pros**:
  - Optimal query performance for recent data (PostgreSQL indexes)
  - Cost-effective long-term storage (S3)
  - Supports compliance requirements (application logic for GDPR, SOX)
  - Can leverage PostgreSQL declarative partitioning (automatic partition creation/deletion)
- **Cons**:
  - Added complexity (partition management + archival jobs)
  - Requires cross-team coordination (DBA + application teams)
- **Decision**: Deferred to Phase 5+. Current approach (Alternative 2) is sufficient for Phase 4. Consider hybrid model if query performance degrades.

## Consequences
### Positive
- Compliance-ready lifecycle controls aligned with SAP-grade expectations
- Reduced storage costs via tiered archiving
- Clear recovery objectives and disaster readiness

### Negative / Risks
- Added operational complexity and storage management overhead
- Requires cross-context coordination and policy governance
- Risk of accidental data loss if policies misconfigured

### Neutral
- Some workloads will require archive-aware query patterns

## Compliance

### GDPR Compliance: Data Retention and Erasure
- **Data Retention Policies**: Each data class (Financial, Clinical, Customer PII, etc.) has documented retention period. Policies stored in `data_retention_policies` table, versioned, and auditable.
- **Right to Erasure (Article 17)**: Erasure requests trigger application-level deletion workflows. All personal data (PII, PHI) across all bounded contexts erased within 30 days. Tombstone records replace deleted values for audit trail.
- **Right to Data Portability (Article 20)**: Data export includes archived data from all tiers (PostgreSQL + S3). Export format: JSON or CSV, structured for machine readability.
- **Data Minimization**: Archive jobs exclude PII fields not required for legal retention (e.g., email addresses in financial transactions). Only business-critical fields archived.
- **Retention Justification**: Each retention policy documents legal basis (legitimate interest, contract, legal obligation). Displayed in GDPR data audit reports.
- **Automated Purge**: Data exceeding retention period automatically purged (soft delete → hard delete after 90-day grace period). Purge jobs run weekly with audit logging.
- **Cross-Border Transfers**: Archived data respects data residency requirements (EU data in EU S3 buckets, US data in US buckets). No cross-region archiving without explicit consent.

### SOX Compliance: Financial Data Retention
- **7-Year Retention**: All financial transactions (GL postings, invoices, payments) retained for minimum 7 years per SOX Section 802. Enforced by retention policies.
- **Immutable Audit Trail**: Archived financial data is write-once (S3 Object Lock, Azure Immutable Blob). Cannot be modified or deleted until retention period expires.
- **Period Close Lockdown**: Archived data for closed fiscal periods is read-only. No modifications permitted after period close event published.
- **Restoration Audit**: All archive restoration requests logged (who, what, when, business justification). SOX auditors can review restoration logs to detect unauthorized access.
- **Backup Integrity**: Quarterly backup validation drills verify data integrity (checksums, hash verification). Results documented for SOX 404 internal controls testing.
- **Disaster Recovery Testing**: Semi-annual DR drills test RTO/RPO compliance (target: RTO <4 hours, RPO <15 minutes). Drill results reviewed by audit committee.

### HIPAA Compliance: PHI Retention and Destruction
- **6-Year Retention (HIPAA 164.530(j))**: Protected Health Information (PHI) in clinical modules retained for minimum 6 years from creation/last effective date.
- **Secure Destruction**: PHI purge jobs use cryptographic erasure (destroy encryption keys) or overwrite with random data. Destruction certificates generated per NIST 800-88 guidelines.
- **Minimum Necessary Rule**: Archived PHI limited to minimum data elements required for legal retention. Sensitive fields (e.g., diagnosis codes) archived separately with additional encryption.
- **Access Logging**: All access to archived PHI logged (user ID, timestamp, access reason, data elements accessed). Logs retained for 6 years per HIPAA audit requirements.
- **Business Associate Agreements (BAA)**: Archive storage providers (AWS, Azure) covered by BAA. Contracts include data destruction guarantees and audit rights.
- **Breach Notification**: Archive restore jobs trigger security logging. Unauthorized restoration attempts escalated to security team within 1 hour.

### Industry-Specific Retention Requirements
- **Legal Hold Compliance**: Legal hold workflows prevent purging of data subject to litigation or regulatory investigation. Hold applies across all tiers (operational DB, archive, backups).
- **Medical Records (State Laws)**: US state laws require 7-10 year retention for medical records (varies by state). Configurable retention policies per tenant jurisdiction.
- **Tax Records (IRS)**: 7-year retention for tax-related financial records (1099s, W-2s, tax filings). Aligned with IRS audit period.
- **Employment Records**: Employee data retained per jurisdiction (3-7 years post-termination). PII in employment records anonymized after legal minimum.

### Service Level Objectives (SLOs)
- **Archival Performance**: `<1 hour` per 100,000 records for hot-to-warm archival (PostgreSQL → S3). Monitored per bounded context.
- **Restoration Time**: `<15 minutes` for warm data retrieval (S3 Standard), `<24 hours` for cold data retrieval (S3 Glacier).
- **Purge Completion**: `>99%` of eligible records purged within 7 days of retention expiration. Monitored weekly.
- **Backup RPO**: `<15 minutes` (transaction log backups every 15 minutes).
- **Backup RTO**: `<4 hours` for full database restoration (tested quarterly).
- **Erasure Request SLA**: `<30 days` for GDPR erasure requests (95th percentile). Escalation at 21 days.
- **Legal Hold Response**: `<1 hour` to apply legal hold after request received. Automated workflow with manual approval.
- **Data Residency Compliance**: `100%` of archived data stored in correct geographic region (validated monthly).

## Implementation Plan
### Implementation Plan (Not Started)

- Phase 1: Define retention policies per data class and region; create governance playbook.
- Phase 2: Implement archive tiers and purge jobs for one pilot context.
- Phase 3: Add legal hold workflows and audit reporting.
- Phase 4: Establish backup/restore automation and quarterly drill cadence.
- Phase 5: Extend anonymization and masking tooling for analytics and non-prod.

### Phase 1: Foundation & Governance (Months 1-2)

**Objectives:**
- Define comprehensive retention policies for all data classes
- Create data classification catalog with 50+ entity types
- Establish governance playbook and approval workflows
- Document jurisdiction-specific requirements (US, EU, UK, Canada, Australia)

**Deliverables:**
- Data Classification Matrix (13 classes: A1-A4, B1-B3, C1-C3, D1-D3)
- Jurisdiction-Specific Retention Rules (5+ jurisdictions)
- Data Lifecycle Governance Playbook
- Retention Policy Engine (Kotlin service)

**Success Criteria:**
- All 12 bounded contexts classified
- Retention policies approved by legal/compliance
- Governance playbook reviewed by stakeholders

### Phase 2: Archiving Infrastructure (Months 3-5)

**Objectives:**
- Implement 4-tier storage strategy (Hot/Warm/Cold/Frozen)
- Set up PostgreSQL partitioning for time-series data
- Configure object storage (S3/Azure Blob) with lifecycle policies
- Build archive/restore tooling

**Deliverables:**
- PostgreSQL table partitioning for Financial Accounting (pilot context)
- S3 Glacier integration with archive manifests
- Archive job scheduler (Quartz/cron)
- Archive manifest format and metadata catalog
- Restore tooling for audit requests

**Success Criteria:**
- Financial Accounting journal entries archived successfully
- Warm-to-cold transition working (2-year threshold)
- Restore from cold storage tested (<4 hours)
- Archive manifest checksums verified

### Phase 3: Legal Hold & e-Discovery (Months 6-7)

**Objectives:**
- Implement legal hold workflows with approval chain
- Build e-Discovery export capabilities (CSV, JSON, PST)
- Integrate legal hold checks into purge jobs
- Create preservation reporting

**Deliverables:**
- Legal Hold Service (create, activate, release)
- e-Discovery Export Service with multiple formats
- Purge job integration (block on active hold)
- Legal hold audit trail
- Preservation report generator

**Success Criteria:**
- Legal hold prevents automatic purge
- e-Discovery export tested with sample litigation scenario
- Audit trail shows all hold actions
- Compliance team trained on workflows

### Phase 4: Backup & Disaster Recovery (Months 8-10)

**Objectives:**
- Implement RPO/RTO-compliant backup strategy (3 tiers)
- Set up PostgreSQL streaming replication (Tier 1)
- Configure backup retention schedules (daily/weekly/monthly/yearly)
- Establish quarterly DR drill process

**Deliverables:**
- PostgreSQL Streaming Replication (hot standby + warm DR)
- Automated failover for Tier 1 contexts (Financial, Identity)
- Backup retention automation (30-day daily, 12-week weekly, etc.)
- DR drill runbook and reporting
- First quarterly DR drill executed successfully

**Success Criteria:**
- Tier 1 contexts meet RPO ≤ 15 min, RTO ≤ 2 hours
- Tier 2 contexts meet RPO ≤ 1 hour, RTO ≤ 6 hours
- Automated failover tested (actual RTO < 2 hours)
- First DR drill passes with documented lessons learned

### Phase 5: Privacy Controls (Months 11-12)

**Objectives:**
- Implement PII masking for non-production environments
- Build anonymization engine for analytics datasets
- Create GDPR Right-to-Erasure workflows
- Develop data minimization tooling

**Deliverables:**
- PII Masking Service (8 field types: SSN, email, phone, etc.)
- Anonymization Service (k-anonymity, l-diversity)
- GDPR Erasure Service with legal hold integration
- Sanitized dataset generator for dev/test/QA
- Data minimization audit reports

**Success Criteria:**
- Non-prod environments have zero plaintext PII
- Anonymized analytics dataset passes k-anonymity verification (k ≥ 5)
- Right-to-erasure request processed end-to-end (<30 days)
- GDPR Article 30 compliance audit passed

### Phase 6: Tenant Isolation & Residency (Month 13-14)

**Objectives:**
- Implement tenant-specific lifecycle policies
- Enforce data residency requirements (GDPR, CCPA, etc.)
- Support tenant retention overrides (stricter than defaults)
- Build multi-region archive/backup

**Deliverables:**
- Tenant Data Lifecycle Policy Service
- Data Residency Enforcement (region validation)
- Tenant retention override API
- Multi-region backup orchestration
- Residency compliance reporting

**Success Criteria:**
- EU tenant data stays in EU regions only
- Tenant can set stricter retention for PII (data minimization)
- Cross-border transfer blocked for restricted tenants
- Residency violation alerts working

### Phase 7: Production Rollout (Months 15-16)

**Objectives:**
- Pilot with 2 contexts (Financial Accounting, Customer Relations)
- Gradual rollout to remaining 10 contexts
- Monitor archive/purge job performance
- Conduct compliance audits

**Deliverables:**
- Pilot rollout to Financial Accounting (A1-A4 data classes)
- Pilot rollout to Customer Relations (B1-B2, D2 data classes)
- Full production rollout (all 12 contexts)
- Performance dashboards (archive throughput, storage savings)
- SOX/GDPR compliance certification

**Success Criteria:**
- Zero data loss incidents during rollout
- Archive jobs complete within maintenance windows
- Storage costs reduced by 40-60% (cold storage)
- Compliance audit passed with zero findings

### Phase 8: Continuous Improvement (Ongoing)

**Objectives:**
- Quarterly DR drills with post-drill reviews
- Annual retention policy reviews
- Data minimization audits
- Archive efficiency optimization

**Deliverables:**
- Quarterly DR drill reports
- Annual retention policy updates
- Data minimization recommendations
- Archive compression improvements

**Success Criteria:**
- DR drills consistently meet RTO/RPO targets
- Retention policies updated per regulatory changes
- Year-over-year storage cost reductions
- Zero compliance violations

## References

### Related ADRs
- ADR-005 (Multi-Tenancy), ADR-009 (Financial), ADR-010 (Validation), ADR-014 (Auth Objects/SoD)

### External References
- SAP analogs: ILM, ADK, data aging/archiving, legal hold
