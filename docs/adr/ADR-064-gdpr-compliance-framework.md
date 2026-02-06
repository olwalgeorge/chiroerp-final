# ADR-064: GDPR Compliance Framework

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Legal Team, Compliance Team  
**Priority**: P1 (High - Legal/Regulatory)  
**Tier**: Platform Core  
**Tags**: gdpr, privacy, compliance, data-protection, legal, eu

---

## Context

**Problem**: ChiroERP has **partial GDPR compliance** but lacks comprehensive automation for **data subject rights**, **consent management**, and **breach notification**. This creates:

- **Legal risk**: €20M or 4% global revenue fines (whichever is higher)
- **Manual processing**: 4-6 weeks to respond to Data Subject Access Requests (DSAR)
- **EU market barriers**: 60% of EU enterprises require GDPR certification
- **Consent gaps**: No granular opt-in/opt-out management
- **Breach exposure**: No automated <72-hour breach notification
- **Audit challenges**: Incomplete data processing records

**Current GDPR Coverage**:
- ✅ **Basic data encryption** (AES-256 at-rest, TLS 1.3 in-transit)
- ✅ **Role-based access control** (RBAC with least privilege)
- ✅ **Data retention policies** (ADR-015 Data Lifecycle Management)
- ⚠️ **Data subject rights**: Manual, slow (4-6 weeks)
- ❌ **Consent management**: No granular tracking
- ❌ **Breach notification**: Manual process (cannot meet 72-hour SLA)
- ❌ **Data Processing Agreements**: Manual templates, no automation
- ❌ **DPIA workflows**: Ad-hoc, not standardized

**GDPR Requirements** (Regulation (EU) 2016/679):

| Article | Requirement | Current State | Target State |
|---------|-------------|---------------|--------------|
| **Art. 15** | Right to Access | Manual (4-6 weeks) | Automated (<48 hours) |
| **Art. 16** | Right to Rectification | Manual | Automated (<7 days) |
| **Art. 17** | Right to Erasure | Partial (ADR-015) | Full automation (<30 days) |
| **Art. 18** | Right to Restriction | Not implemented | Automated (<7 days) |
| **Art. 20** | Right to Data Portability | Not implemented | Automated (<48 hours) |
| **Art. 21** | Right to Object | Not implemented | Automated (<7 days) |
| **Art. 33** | Breach Notification (DPA) | Manual (slow) | Automated (<72 hours) |
| **Art. 34** | Breach Notification (Individuals) | Manual (slow) | Automated (immediate) |
| **Art. 35** | Data Protection Impact Assessment | Ad-hoc | Standardized workflow |

**Market Reality**:
- **SAP**: Data Privacy Integration (DPI) for S/4HANA, automated DSAR
- **Oracle**: Data Privacy Management Cloud, AI-powered breach detection
- **Dynamics 365**: Privacy Management (Priva), automated consent tracking
- **Salesforce**: Privacy Center, automated DSAR workflows
- **ChiroERP**: ⚠️ Partial coverage, manual processes

**Customer Quote** (DPO, Enterprise):
> "We operate in EU and must respond to DSARs within 30 days. Your current 4-6 week manual process doesn't meet our legal obligations. We need automated DSAR workflows."

**Legal Context**:
- **GDPR fines 2023**: €2.1B total (up 50% from 2022)
- **Largest fine**: Meta €1.2B (data transfers)
- **Average DSAR response time**: 18 days (best-in-class), 45 days (average)
- **Breach notification failures**: €50K-500K fines per violation

---

## Decision

Build a **GDPR Compliance Framework** providing:

1. **Data Subject Rights Automation** (Art. 15-21)
   - Self-service DSAR portal
   - Automated data discovery & export
   - Rectification, erasure, restriction workflows

2. **Consent Management System**
   - Granular consent tracking (purpose-specific)
   - Opt-in/opt-out management
   - Consent withdrawal automation

3. **Breach Notification Automation** (Art. 33-34)
   - Automated breach detection
   - <72-hour DPA notification
   - Individual notification workflows

4. **Data Processing Agreements (DPA)**
   - Template library (processors, sub-processors)
   - Digital signature workflows
   - Renewal tracking

5. **Data Protection Impact Assessments (DPIA)**
   - Standardized DPIA templates
   - Risk assessment workflows
   - Automated triggers (high-risk processing)

**Target**: Q1-Q2 2027 (production-ready)

---

## Architecture

### 1. GDPR Compliance Service

**New Platform Service**: `platform-shared/gdpr-compliance-service`

```kotlin
/**
 * GDPR Compliance Service
 * Manages data subject rights, consent, and breach notifications
 */
@Service
class GDPRComplianceService(
    private val dsarProcessor: DSARProcessor,
    private val consentManager: ConsentManager,
    private val breachNotifier: BreachNotifier,
    private val dpaManager: DPAManager,
    private val dpiaWorkflow: DPIAWorkflow,
    private val dataInventory: DataInventory,
    private val auditLog: AuditLog
) {
    
    /**
     * Process Data Subject Access Request (DSAR)
     * Art. 15: Right to Access
     * 
     * SLA: <48 hours for automated export
     */
    suspend fun submitDSAR(request: DSARRequest): DSARResponse {
        logger.info("Processing DSAR for data subject ${request.dataSubjectId}")
        
        // Step 1: Verify identity (required by GDPR)
        if (!verifyIdentity(request)) {
            return DSARResponse.IdentityVerificationRequired(
                verificationMethod = IdentityVerificationMethod.EMAIL_CONFIRMATION,
                message = "Please verify your identity via email"
            )
        }
        
        // Step 2: Validate request scope
        val validationResult = validateDSARRequest(request)
        if (!validationResult.isValid) {
            return DSARResponse.InvalidRequest(
                errors = validationResult.errors
            )
        }
        
        // Step 3: Create DSAR record (audit trail)
        val dsarRecord = DSARRecord(
            id = UUID.randomUUID(),
            tenantId = request.tenantId,
            dataSubjectId = request.dataSubjectId,
            requestType = request.requestType,
            requestDate = Instant.now(),
            status = DSARStatus.PROCESSING,
            requestedBy = request.requestedBy,
            verificationStatus = VerificationStatus.VERIFIED
        )
        dsarRepository.save(dsarRecord)
        
        // Step 4: Process request asynchronously (can take 10-30 min)
        coroutineScope.launch {
            try {
                val result = when (request.requestType) {
                    DSARType.ACCESS -> processAccessRequest(dsarRecord)
                    DSARType.RECTIFICATION -> processRectificationRequest(dsarRecord, request)
                    DSARType.ERASURE -> processErasureRequest(dsarRecord)
                    DSARType.RESTRICTION -> processRestrictionRequest(dsarRecord)
                    DSARType.PORTABILITY -> processPortabilityRequest(dsarRecord)
                    DSARType.OBJECTION -> processObjectionRequest(dsarRecord)
                }
                
                // Update DSAR record
                dsarRecord.status = DSARStatus.COMPLETED
                dsarRecord.completedAt = Instant.now()
                dsarRecord.resultUrl = result.url
                dsarRepository.update(dsarRecord)
                
                // Notify data subject
                notifyDataSubject(dsarRecord, result)
                
                // Audit log
                auditLog.record(
                    event = GDPREvent.DSAR_COMPLETED,
                    tenantId = request.tenantId,
                    dataSubjectId = request.dataSubjectId,
                    details = mapOf(
                        "dsarId" to dsarRecord.id,
                        "requestType" to request.requestType,
                        "processingTime" to Duration.between(dsarRecord.requestDate, dsarRecord.completedAt).toMinutes()
                    )
                )
            } catch (e: Exception) {
                logger.error("DSAR processing failed", e)
                dsarRecord.status = DSARStatus.FAILED
                dsarRecord.errorMessage = e.message
                dsarRepository.update(dsarRecord)
            }
        }
        
        // Step 5: Return acknowledgment
        return DSARResponse.Acknowledged(
            dsarId = dsarRecord.id,
            estimatedCompletionTime = Instant.now().plus(48, ChronoUnit.HOURS),
            message = "Your request is being processed. You will be notified within 48 hours."
        )
    }
    
    /**
     * Process Right to Access request
     * Art. 15: Data Subject Access Request
     * 
     * Returns all personal data held by ChiroERP
     */
    private suspend fun processAccessRequest(dsar: DSARRecord): DSARResult {
        logger.info("Processing access request for DSAR ${dsar.id}")
        
        // Step 1: Discover all personal data (across all modules)
        val personalData = dataInventory.discover(
            tenantId = dsar.tenantId,
            dataSubjectId = dsar.dataSubjectId
        )
        
        logger.info("Discovered ${personalData.size} data records for data subject ${dsar.dataSubjectId}")
        
        // Step 2: Export data to machine-readable format (JSON)
        val exportData = ExportData(
            dataSubject = DataSubjectInfo(
                id = dsar.dataSubjectId,
                identifiers = personalData.identifiers
            ),
            exportDate = Instant.now(),
            exportFormat = "JSON",
            categories = personalData.groupBy { it.category }.mapValues { (category, records) ->
                DataCategoryExport(
                    category = category,
                    recordCount = records.size,
                    records = records.map { it.toJson() }
                )
            },
            legalBasis = personalData.groupBy { it.legalBasis }.mapKeys { it.key.toString() }
                .mapValues { it.value.size },
            processingPurposes = personalData.flatMap { it.processingPurposes }.distinct(),
            dataRecipients = personalData.flatMap { it.recipients }.distinct(),
            retentionPeriods = personalData.associate { it.category to it.retentionPeriod }
        )
        
        // Step 3: Generate PDF report (human-readable)
        val pdfReport = generateDSARReport(dsar, exportData)
        
        // Step 4: Upload to secure storage (S3 with pre-signed URL, 30-day expiry)
        val jsonUrl = s3Storage.upload(
            bucket = "chiroerp-dsar-exports",
            key = "${dsar.id}/export.json",
            content = exportData.toJson().toByteArray(),
            expiryDays = 30
        )
        
        val pdfUrl = s3Storage.upload(
            bucket = "chiroerp-dsar-exports",
            key = "${dsar.id}/report.pdf",
            content = pdfReport,
            expiryDays = 30
        )
        
        return DSARResult(
            dsarId = dsar.id,
            url = pdfUrl,
            additionalUrls = mapOf(
                "json_export" to jsonUrl,
                "pdf_report" to pdfUrl
            ),
            recordCount = personalData.size,
            categories = exportData.categories.keys.toList()
        )
    }
    
    /**
     * Process Right to Erasure request
     * Art. 17: Right to be Forgotten
     * 
     * Deletes or anonymizes all personal data (with legal basis check)
     */
    private suspend fun processErasureRequest(dsar: DSARRecord): DSARResult {
        logger.info("Processing erasure request for DSAR ${dsar.id}")
        
        // Step 1: Discover all personal data
        val personalData = dataInventory.discover(
            tenantId = dsar.tenantId,
            dataSubjectId = dsar.dataSubjectId
        )
        
        // Step 2: Check legal obligations (cannot erase if retention required)
        val retentionChecks = personalData.map { record ->
            RetentionCheck(
                recordId = record.id,
                category = record.category,
                canErase = record.canErase(),
                reason = record.retentionReason()
            )
        }
        
        val cannotErase = retentionChecks.filter { !it.canErase }
        if (cannotErase.isNotEmpty()) {
            logger.warn("Cannot erase ${cannotErase.size} records due to legal retention")
            // Still proceed with erasure for records that can be deleted
        }
        
        // Step 3: Erase or anonymize data (transactional)
        return database.transaction {
            var erasedCount = 0
            var anonymizedCount = 0
            
            for (record in personalData) {
                if (record.canErase()) {
                    // Hard delete
                    dataInventory.delete(record)
                    erasedCount++
                } else if (record.requiresRetention()) {
                    // Anonymize (keep for legal/financial reporting)
                    dataInventory.anonymize(record)
                    anonymizedCount++
                }
            }
            
            logger.info("Erasure completed: $erasedCount deleted, $anonymizedCount anonymized")
            
            // Generate erasure certificate
            val certificate = generateErasureCertificate(
                dsar = dsar,
                erasedCount = erasedCount,
                anonymizedCount = anonymizedCount,
                cannotEraseRecords = cannotErase
            )
            
            val certificateUrl = s3Storage.upload(
                bucket = "chiroerp-dsar-exports",
                key = "${dsar.id}/erasure_certificate.pdf",
                content = certificate,
                expiryDays = 365 // Keep for 1 year (audit trail)
            )
            
            DSARResult(
                dsarId = dsar.id,
                url = certificateUrl,
                recordCount = erasedCount + anonymizedCount,
                message = "Erased: $erasedCount, Anonymized: $anonymizedCount, Retained (legal): ${cannotErase.size}"
            )
        }
    }
    
    /**
     * Process Right to Data Portability request
     * Art. 20: Data Portability
     * 
     * Export data in structured, machine-readable format (CSV/JSON/XML)
     */
    private suspend fun processPortabilityRequest(dsar: DSARRecord): DSARResult {
        // Similar to Access Request, but only includes data:
        // - Provided by data subject (not derived/inferred)
        // - Processed with consent or contract legal basis
        // - In structured, machine-readable format
        
        val personalData = dataInventory.discover(
            tenantId = dsar.tenantId,
            dataSubjectId = dsar.dataSubjectId
        ).filter { record ->
            // Only data with consent or contract legal basis
            record.legalBasis in listOf(LegalBasis.CONSENT, LegalBasis.CONTRACT)
        }.filter { record ->
            // Only data provided by user (not derived)
            record.dataSource == DataSource.USER_PROVIDED
        }
        
        // Export in CSV format (most portable)
        val csvExport = generateCSVExport(personalData)
        
        val csvUrl = s3Storage.upload(
            bucket = "chiroerp-dsar-exports",
            key = "${dsar.id}/portability_export.csv",
            content = csvExport.toByteArray(),
            expiryDays = 30
        )
        
        return DSARResult(
            dsarId = dsar.id,
            url = csvUrl,
            recordCount = personalData.size
        )
    }
}

data class DSARRequest(
    val tenantId: UUID,
    val dataSubjectId: UUID, // Customer ID, User ID, etc.
    val requestType: DSARType,
    val requestedBy: UUID, // Who submitted request
    val identityVerification: IdentityVerification,
    val additionalInfo: Map<String, String> = emptyMap()
)

enum class DSARType {
    ACCESS,         // Art. 15: Right to Access
    RECTIFICATION,  // Art. 16: Right to Rectification
    ERASURE,        // Art. 17: Right to Erasure (Right to be Forgotten)
    RESTRICTION,    // Art. 18: Right to Restriction of Processing
    PORTABILITY,    // Art. 20: Right to Data Portability
    OBJECTION       // Art. 21: Right to Object
}

sealed class DSARResponse {
    data class Acknowledged(
        val dsarId: UUID,
        val estimatedCompletionTime: Instant,
        val message: String
    ) : DSARResponse()
    
    data class IdentityVerificationRequired(
        val verificationMethod: IdentityVerificationMethod,
        val message: String
    ) : DSARResponse()
    
    data class InvalidRequest(
        val errors: List<String>
    ) : DSARResponse()
}

data class DSARRecord(
    val id: UUID,
    val tenantId: UUID,
    val dataSubjectId: UUID,
    val requestType: DSARType,
    val requestDate: Instant,
    var status: DSARStatus,
    val requestedBy: UUID,
    var verificationStatus: VerificationStatus,
    var completedAt: Instant? = null,
    var resultUrl: String? = null,
    var errorMessage: String? = null
)

enum class DSARStatus {
    PENDING_VERIFICATION,
    PROCESSING,
    COMPLETED,
    FAILED,
    REJECTED
}

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    FAILED
}
```

---

### 2. Data Inventory & Discovery

**Automatic Personal Data Discovery**:

```kotlin
/**
 * Data Inventory Service
 * Discovers and catalogs all personal data across ChiroERP
 */
@Service
class DataInventory(
    private val databaseInspector: DatabaseInspector,
    private val dataClassifier: DataClassifier
) {
    
    /**
     * Discover all personal data for data subject
     * Scans all modules and databases
     */
    suspend fun discover(
        tenantId: UUID,
        dataSubjectId: UUID
    ): List<PersonalDataRecord> {
        val records = mutableListOf<PersonalDataRecord>()
        
        // Scan all modules (parallel execution)
        val modules = listOf(
            "customer-core",
            "user-management",
            "sales-order",
            "invoice",
            "financial-gl",
            "audit-log",
            "communication-email"
        )
        
        coroutineScope {
            modules.map { module ->
                async {
                    discoverInModule(module, tenantId, dataSubjectId)
                }
            }.awaitAll().forEach { moduleRecords ->
                records.addAll(moduleRecords)
            }
        }
        
        logger.info("Discovered ${records.size} personal data records across ${modules.size} modules")
        
        return records
    }
    
    /**
     * Discover personal data in specific module
     */
    private suspend fun discoverInModule(
        module: String,
        tenantId: UUID,
        dataSubjectId: UUID
    ): List<PersonalDataRecord> {
        val records = mutableListOf<PersonalDataRecord>()
        
        // Get all tables containing personal data (from data catalog)
        val tables = dataCatalog.getTablesWithPersonalData(module)
        
        for (table in tables) {
            // Query table for data subject records
            val rows = databaseInspector.query(
                table = table.name,
                where = "${table.dataSubjectColumn} = :dataSubjectId AND tenant_id = :tenantId",
                params = mapOf(
                    "dataSubjectId" to dataSubjectId,
                    "tenantId" to tenantId
                )
            )
            
            for (row in rows) {
                // Classify data (PII category, sensitivity)
                val classification = dataClassifier.classify(table, row)
                
                records.add(
                    PersonalDataRecord(
                        id = UUID.randomUUID(),
                        module = module,
                        tableName = table.name,
                        recordId = row["id"] as UUID,
                        category = classification.category,
                        sensitivity = classification.sensitivity,
                        data = row,
                        legalBasis = table.legalBasis,
                        processingPurposes = table.processingPurposes,
                        recipients = table.recipients,
                        retentionPeriod = table.retentionPeriod,
                        dataSource = DataSource.USER_PROVIDED
                    )
                )
            }
        }
        
        return records
    }
    
    /**
     * Delete personal data record
     */
    suspend fun delete(record: PersonalDataRecord) {
        databaseInspector.delete(
            table = record.tableName,
            where = "id = :recordId",
            params = mapOf("recordId" to record.recordId)
        )
    }
    
    /**
     * Anonymize personal data record
     * Replace PII with anonymized values
     */
    suspend fun anonymize(record: PersonalDataRecord) {
        val anonymizedData = record.data.mapValues { (key, value) ->
            if (isPIIField(key)) {
                anonymizeValue(value)
            } else {
                value
            }
        }
        
        databaseInspector.update(
            table = record.tableName,
            recordId = record.recordId,
            data = anonymizedData
        )
    }
    
    /**
     * Anonymize value based on type
     */
    private fun anonymizeValue(value: Any?): Any? {
        return when (value) {
            is String -> {
                when {
                    isEmail(value) -> "anonymized@example.com"
                    isPhone(value) -> "+00000000000"
                    else -> "ANONYMIZED"
                }
            }
            is Int -> 0
            is Double -> 0.0
            else -> null
        }
    }
}

data class PersonalDataRecord(
    val id: UUID,
    val module: String,
    val tableName: String,
    val recordId: UUID,
    val category: DataCategory,
    val sensitivity: DataSensitivity,
    val data: Map<String, Any?>,
    val legalBasis: LegalBasis,
    val processingPurposes: List<ProcessingPurpose>,
    val recipients: List<String>,
    val retentionPeriod: String,
    val dataSource: DataSource
) {
    /**
     * Check if record can be erased
     * Returns false if legal/financial retention required
     */
    fun canErase(): Boolean {
        // Cannot erase if:
        // 1. Legal retention required (tax, accounting)
        if (legalBasis == LegalBasis.LEGAL_OBLIGATION) {
            return false
        }
        
        // 2. Active contract (e.g., ongoing subscription)
        if (legalBasis == LegalBasis.CONTRACT && isActiveContract()) {
            return false
        }
        
        // 3. Financial records (7-year retention for tax)
        if (category in listOf(DataCategory.FINANCIAL, DataCategory.TAX)) {
            return false
        }
        
        return true
    }
    
    /**
     * Check if record requires retention (anonymization instead of deletion)
     */
    fun requiresRetention(): Boolean {
        return legalBasis == LegalBasis.LEGAL_OBLIGATION ||
               category in listOf(DataCategory.FINANCIAL, DataCategory.TAX)
    }
    
    /**
     * Get reason for retention (if cannot erase)
     */
    fun retentionReason(): String? {
        return when {
            legalBasis == LegalBasis.LEGAL_OBLIGATION -> "Legal retention required"
            category == DataCategory.FINANCIAL -> "Financial records (7-year tax retention)"
            category == DataCategory.TAX -> "Tax records (legal requirement)"
            isActiveContract() -> "Active contract"
            else -> null
        }
    }
    
    private fun isActiveContract(): Boolean {
        // Check if contract is still active
        // TODO: Implement contract status check
        return false
    }
}

enum class DataCategory {
    IDENTITY,      // Name, email, phone, address
    FINANCIAL,     // Bank account, credit card, payment history
    TAX,           // Tax ID, VAT number
    HEALTH,        // Health records (special category, Art. 9)
    EMPLOYMENT,    // Job title, salary
    COMMUNICATION, // Emails, messages
    USAGE,         // Login history, analytics
    PREFERENCES    // Settings, consent
}

enum class DataSensitivity {
    PUBLIC,        // Publicly available
    INTERNAL,      // Internal use only
    CONFIDENTIAL,  // Restricted access
    SENSITIVE      // Special category (Art. 9)
}

enum class LegalBasis {
    CONSENT,              // Art. 6(1)(a): Consent
    CONTRACT,             // Art. 6(1)(b): Contractual necessity
    LEGAL_OBLIGATION,     // Art. 6(1)(c): Legal obligation
    VITAL_INTERESTS,      // Art. 6(1)(d): Vital interests
    PUBLIC_INTEREST,      // Art. 6(1)(e): Public interest
    LEGITIMATE_INTERESTS  // Art. 6(1)(f): Legitimate interests
}

enum class ProcessingPurpose {
    SERVICE_DELIVERY,
    BILLING,
    MARKETING,
    ANALYTICS,
    COMPLIANCE,
    FRAUD_PREVENTION,
    CUSTOMER_SUPPORT
}

enum class DataSource {
    USER_PROVIDED,  // Data provided by user
    DERIVED,        // Calculated/derived from other data
    INFERRED,       // Inferred from behavior
    THIRD_PARTY     // Obtained from third party
}
```

---

### 3. Consent Management

**Granular Consent Tracking**:

```kotlin
/**
 * Consent Manager
 * Tracks and manages data subject consents
 */
@Service
class ConsentManager(
    private val consentRepository: ConsentRepository,
    private val auditLog: AuditLog
) {
    
    /**
     * Record consent from data subject
     */
    suspend fun recordConsent(request: ConsentRequest): ConsentRecord {
        val consent = ConsentRecord(
            id = UUID.randomUUID(),
            tenantId = request.tenantId,
            dataSubjectId = request.dataSubjectId,
            purpose = request.purpose,
            status = ConsentStatus.GRANTED,
            grantedAt = Instant.now(),
            expiryDate = request.expiryDate,
            consentMethod = request.consentMethod,
            consentText = request.consentText,
            metadata = request.metadata
        )
        
        consentRepository.save(consent)
        
        // Audit log
        auditLog.record(
            event = GDPREvent.CONSENT_GRANTED,
            tenantId = request.tenantId,
            dataSubjectId = request.dataSubjectId,
            details = mapOf(
                "consentId" to consent.id,
                "purpose" to request.purpose
            )
        )
        
        return consent
    }
    
    /**
     * Withdraw consent
     * Must stop processing immediately
     */
    suspend fun withdrawConsent(
        tenantId: UUID,
        dataSubjectId: UUID,
        purpose: ProcessingPurpose
    ): ConsentRecord {
        val consent = consentRepository.findActive(tenantId, dataSubjectId, purpose)
            ?: throw NotFoundException("No active consent found")
        
        consent.status = ConsentStatus.WITHDRAWN
        consent.withdrawnAt = Instant.now()
        consentRepository.update(consent)
        
        // Trigger consent withdrawal actions
        // 1. Stop marketing emails
        if (purpose == ProcessingPurpose.MARKETING) {
            stopMarketingEmails(dataSubjectId)
        }
        
        // 2. Stop analytics tracking
        if (purpose == ProcessingPurpose.ANALYTICS) {
            disableAnalytics(dataSubjectId)
        }
        
        // Audit log
        auditLog.record(
            event = GDPREvent.CONSENT_WITHDRAWN,
            tenantId = tenantId,
            dataSubjectId = dataSubjectId,
            details = mapOf(
                "consentId" to consent.id,
                "purpose" to purpose
            )
        )
        
        return consent
    }
    
    /**
     * Check if consent is valid for purpose
     */
    suspend fun hasValidConsent(
        tenantId: UUID,
        dataSubjectId: UUID,
        purpose: ProcessingPurpose
    ): Boolean {
        val consent = consentRepository.findActive(tenantId, dataSubjectId, purpose)
            ?: return false
        
        // Check if consent expired
        if (consent.expiryDate != null && consent.expiryDate.isBefore(Instant.now())) {
            return false
        }
        
        return consent.status == ConsentStatus.GRANTED
    }
}

data class ConsentRecord(
    val id: UUID,
    val tenantId: UUID,
    val dataSubjectId: UUID,
    val purpose: ProcessingPurpose,
    var status: ConsentStatus,
    val grantedAt: Instant,
    var withdrawnAt: Instant? = null,
    val expiryDate: Instant? = null,
    val consentMethod: ConsentMethod,
    val consentText: String,
    val metadata: Map<String, String> = emptyMap()
)

enum class ConsentStatus {
    GRANTED,
    WITHDRAWN,
    EXPIRED
}

enum class ConsentMethod {
    WEB_FORM,        // Checkbox on web form
    EMAIL_CLICK,     // Click in email
    API,             // API call
    PHONE,           // Phone call
    PAPER_FORM       // Paper form (scanned)
}

data class ConsentRequest(
    val tenantId: UUID,
    val dataSubjectId: UUID,
    val purpose: ProcessingPurpose,
    val expiryDate: Instant? = null,
    val consentMethod: ConsentMethod,
    val consentText: String,
    val metadata: Map<String, String> = emptyMap()
)
```

---

### 4. Breach Notification Automation

**<72-Hour Breach Notification**:

```kotlin
/**
 * Breach Notifier
 * Automates breach detection and notification (Art. 33-34)
 */
@Service
class BreachNotifier(
    private val breachRepository: BreachRepository,
    private val dpaNotifier: DPANotifier,
    private val dataSubjectNotifier: DataSubjectNotifier,
    private val auditLog: AuditLog
) {
    
    /**
     * Report data breach
     * Art. 33: Notification to DPA within 72 hours
     */
    suspend fun reportBreach(incident: BreachIncident): BreachRecord {
        logger.error("Data breach reported: ${incident.description}")
        
        // Step 1: Create breach record
        val breach = BreachRecord(
            id = UUID.randomUUID(),
            tenantId = incident.tenantId,
            incidentDate = incident.incidentDate,
            discoveryDate = Instant.now(),
            breachType = incident.breachType,
            severity = incident.severity,
            description = incident.description,
            affectedDataSubjects = incident.affectedDataSubjects,
            affectedDataCategories = incident.affectedDataCategories,
            status = BreachStatus.UNDER_INVESTIGATION,
            notificationDeadline = Instant.now().plus(72, ChronoUnit.HOURS)
        )
        
        breachRepository.save(breach)
        
        // Step 2: Assess severity (requires DPA notification?)
        val requiresDPANotification = assessBreachSeverity(breach)
        
        if (requiresDPANotification) {
            // Step 3: Notify DPA within 72 hours (automated)
            val dpaNotification = dpaNotifier.notify(
                breach = breach,
                dpa = getDPAForTenant(incident.tenantId)
            )
            
            breach.dpaNotifiedAt = dpaNotification.sentAt
            breach.dpaNotificationId = dpaNotification.id
            breachRepository.update(breach)
        }
        
        // Step 4: Assess if data subjects need notification (Art. 34)
        val requiresDataSubjectNotification = assessDataSubjectNotificationRequirement(breach)
        
        if (requiresDataSubjectNotification) {
            // Notify all affected data subjects
            dataSubjectNotifier.notifyDataSubjects(breach)
            breach.dataSubjectsNotifiedAt = Instant.now()
            breachRepository.update(breach)
        }
        
        // Step 5: Audit log
        auditLog.record(
            event = GDPREvent.BREACH_REPORTED,
            tenantId = incident.tenantId,
            details = mapOf(
                "breachId" to breach.id,
                "severity" to breach.severity,
                "affectedCount" to breach.affectedDataSubjects,
                "dpaNotified" to requiresDPANotification,
                "dataSubjectsNotified" to requiresDataSubjectNotification
            )
        )
        
        return breach
    }
    
    /**
     * Assess if breach requires DPA notification
     * Art. 33(1): Notification required unless unlikely to result in risk
     */
    private fun assessBreachSeverity(breach: BreachRecord): Boolean {
        // Always notify DPA for:
        // 1. High severity breaches
        if (breach.severity == BreachSeverity.HIGH || breach.severity == BreachSeverity.CRITICAL) {
            return true
        }
        
        // 2. Breaches affecting sensitive data (Art. 9)
        if (breach.affectedDataCategories.contains(DataCategory.HEALTH)) {
            return true
        }
        
        // 3. Large-scale breaches (>1000 data subjects)
        if (breach.affectedDataSubjects > 1000) {
            return true
        }
        
        // 4. Financial data breaches
        if (breach.affectedDataCategories.contains(DataCategory.FINANCIAL)) {
            return true
        }
        
        // Low severity, minimal risk: may not require notification
        return false
    }
    
    /**
     * Assess if data subjects need notification
     * Art. 34(1): Notification required if high risk to rights and freedoms
     */
    private fun assessDataSubjectNotificationRequirement(breach: BreachRecord): Boolean {
        // Always notify data subjects for:
        // 1. Critical/high severity breaches
        if (breach.severity in listOf(BreachSeverity.CRITICAL, BreachSeverity.HIGH)) {
            return true
        }
        
        // 2. Sensitive data breaches (Art. 9)
        if (breach.affectedDataCategories.any { it == DataCategory.HEALTH }) {
            return true
        }
        
        // 3. Identity theft risk (credentials, financial data)
        if (breach.breachType in listOf(BreachType.UNAUTHORIZED_ACCESS, BreachType.CREDENTIAL_THEFT)) {
            return true
        }
        
        return false
    }
}

data class BreachIncident(
    val tenantId: UUID,
    val incidentDate: Instant,
    val breachType: BreachType,
    val severity: BreachSeverity,
    val description: String,
    val affectedDataSubjects: Int,
    val affectedDataCategories: List<DataCategory>,
    val reportedBy: UUID
)

enum class BreachType {
    UNAUTHORIZED_ACCESS,
    DATA_EXFILTRATION,
    RANSOMWARE,
    ACCIDENTAL_DISCLOSURE,
    CREDENTIAL_THEFT,
    MISCONFIGURATION,
    INSIDER_THREAT,
    LOST_DEVICE,
    OTHER
}

enum class BreachSeverity {
    LOW,        // Minimal risk
    MEDIUM,     // Some risk
    HIGH,       // Significant risk
    CRITICAL    // Severe risk
}

data class BreachRecord(
    val id: UUID,
    val tenantId: UUID,
    val incidentDate: Instant,
    val discoveryDate: Instant,
    val breachType: BreachType,
    val severity: BreachSeverity,
    val description: String,
    val affectedDataSubjects: Int,
    val affectedDataCategories: List<DataCategory>,
    var status: BreachStatus,
    val notificationDeadline: Instant, // 72 hours from discovery
    var dpaNotifiedAt: Instant? = null,
    var dpaNotificationId: UUID? = null,
    var dataSubjectsNotifiedAt: Instant? = null
)

enum class BreachStatus {
    UNDER_INVESTIGATION,
    DPA_NOTIFIED,
    DATA_SUBJECTS_NOTIFIED,
    RESOLVED,
    CLOSED
}
```

---

### 5. Data Protection Impact Assessment (DPIA)

**Standardized DPIA Workflow**:

```kotlin
/**
 * DPIA Workflow Service
 * Manages Data Protection Impact Assessments (Art. 35)
 */
@Service
class DPIAWorkflow(
    private val dpiaRepository: DPIARepository,
    private val riskAssessment: RiskAssessment
) {
    
    /**
     * Initiate DPIA
     * Required for high-risk processing activities
     */
    suspend fun initiateDPIA(request: DPIARequest): DPIA {
        val dpia = DPIA(
            id = UUID.randomUUID(),
            tenantId = request.tenantId,
            projectName = request.projectName,
            description = request.description,
            processingActivities = request.processingActivities,
            dataCategories = request.dataCategories,
            dataSubjectCategories = request.dataSubjectCategories,
            status = DPIAStatus.DRAFT,
            createdAt = Instant.now(),
            createdBy = request.createdBy
        )
        
        dpiaRepository.save(dpia)
        
        return dpia
    }
    
    /**
     * Assess risks (Step 3 of DPIA)
     */
    suspend fun assessRisks(dpiaId: UUID): List<Risk> {
        val dpia = dpiaRepository.findById(dpiaId)
            ?: throw NotFoundException("DPIA not found")
        
        val risks = riskAssessment.assess(
            processingActivities = dpia.processingActivities,
            dataCategories = dpia.dataCategories
        )
        
        dpia.risks = risks
        dpia.status = DPIAStatus.RISK_ASSESSMENT_COMPLETE
        dpiaRepository.update(dpia)
        
        return risks
    }
    
    /**
     * Check if DPIA is required for processing activity
     * Art. 35(3): DPIA required for high-risk processing
     */
    fun isDPIARequired(processingActivity: ProcessingActivity): Boolean {
        // DPIA required for (Art. 35(3)):
        
        // 1. Systematic and extensive profiling with automated decision-making
        if (processingActivity.isAutomatedDecisionMaking &&
            processingActivity.isSystematic &&
            processingActivity.isExtensive) {
            return true
        }
        
        // 2. Large-scale processing of special categories (Art. 9)
        if (processingActivity.dataCategories.contains(DataCategory.HEALTH) &&
            processingActivity.scale == ProcessingScale.LARGE) {
            return true
        }
        
        // 3. Systematic monitoring of publicly accessible areas
        if (processingActivity.involvesMonitoring &&
            processingActivity.location == ProcessingLocation.PUBLIC_AREA) {
            return true
        }
        
        return false
    }
}

data class DPIA(
    val id: UUID,
    val tenantId: UUID,
    val projectName: String,
    val description: String,
    val processingActivities: List<ProcessingActivity>,
    val dataCategories: List<DataCategory>,
    val dataSubjectCategories: List<String>,
    var status: DPIAStatus,
    val createdAt: Instant,
    val createdBy: UUID,
    var risks: List<Risk> = emptyList(),
    var mitigations: List<Mitigation> = emptyList(),
    var approvedBy: UUID? = null,
    var approvedAt: Instant? = null
)

enum class DPIAStatus {
    DRAFT,
    RISK_ASSESSMENT_COMPLETE,
    MITIGATION_PLAN_COMPLETE,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

data class ProcessingActivity(
    val name: String,
    val purpose: ProcessingPurpose,
    val dataCategories: List<DataCategory>,
    val isAutomatedDecisionMaking: Boolean,
    val isSystematic: Boolean,
    val isExtensive: Boolean,
    val scale: ProcessingScale,
    val involvesMonitoring: Boolean,
    val location: ProcessingLocation
)

enum class ProcessingScale {
    SMALL,      // <1,000 data subjects
    MEDIUM,     // 1,000-10,000
    LARGE       // >10,000
}

enum class ProcessingLocation {
    PRIVATE_AREA,
    PUBLIC_AREA,
    ONLINE
}

data class Risk(
    val id: UUID,
    val description: String,
    val likelihood: RiskLikelihood,
    val impact: RiskImpact,
    val riskLevel: RiskLevel
)

enum class RiskLikelihood {
    RARE,
    UNLIKELY,
    POSSIBLE,
    LIKELY,
    ALMOST_CERTAIN
}

enum class RiskImpact {
    NEGLIGIBLE,
    MINOR,
    MODERATE,
    MAJOR,
    SEVERE
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class Mitigation(
    val riskId: UUID,
    val description: String,
    val implementationStatus: MitigationStatus
)

enum class MitigationStatus {
    PLANNED,
    IN_PROGRESS,
    IMPLEMENTED,
    VERIFIED
}
```

---

## GDPR Compliance Dashboard

**Self-Service Portal**:

```yaml
GDPR Compliance Dashboard:
  
  Data Subject Rights:
    Submit Request:
      Request Type:
        - [ ] Access my data (Art. 15)
        - [ ] Rectify my data (Art. 16)
        - [x] Delete my data (Art. 17)
        - [ ] Restrict processing (Art. 18)
        - [ ] Data portability (Art. 20)
        - [ ] Object to processing (Art. 21)
      
      Identity Verification:
        Email: [john.doe@example.com]
        [Send Verification Code]
      
      Additional Information:
        [Optional: Describe your request]
      
      [Submit Request]
    
    My Requests:
      - Request #12345 (Delete my data)
        Status: Processing
        Submitted: 2026-02-05
        Est. Completion: 2026-02-07 (within 48 hours)
        [View Details]
      
      - Request #12344 (Access my data)
        Status: Completed
        Submitted: 2026-01-15
        Completed: 2026-01-16
        [Download Report]
  
  Consent Management:
    My Consents:
      ✓ Service Delivery (Required)
        Granted: 2024-01-01
        Status: Active
        [Cannot withdraw - required for service]
      
      ✓ Marketing Emails
        Granted: 2024-01-01
        Status: Active
        [Withdraw Consent]
      
      ✗ Analytics Tracking
        Withdrawn: 2025-06-15
        [Re-grant Consent]
      
      ✓ Third-Party Integrations
        Granted: 2024-03-10
        Expires: 2027-03-10
        [Manage]
  
  Data Processing Activities:
    My Data:
      Identity Information:
        - Name, Email, Phone
        Legal Basis: Contract
        Purpose: Service Delivery
        Retention: Active account + 7 years
      
      Financial Information:
        - Payment history, Invoices
        Legal Basis: Legal Obligation
        Purpose: Billing, Tax Compliance
        Retention: 7 years (tax law)
      
      Communication History:
        - Support tickets, Emails
        Legal Basis: Legitimate Interest
        Purpose: Customer Support
        Retention: 3 years
  
  Privacy Settings:
    Data Portability:
      [Download all my data (JSON)]
      [Download all my data (CSV)]
    
    Marketing Preferences:
      ☑ Email newsletters
      ☐ Product updates
      ☑ Event invitations
      [Save Preferences]
```

---

## Implementation Roadmap

### Phase 1: Data Subject Rights (Q1 2027)

**Deliverables**:
- [ ] DSAR automation (Access, Erasure, Portability)
- [ ] Data inventory & discovery (scan all modules)
- [ ] Identity verification workflows
- [ ] Self-service DSAR portal

**Timeline**: 10 weeks (January-March 2027)
**Resources**: 2 backend engineers, 1 frontend engineer, 1 compliance specialist

### Phase 2: Consent & Breach Management (Q2 2027)

**Deliverables**:
- [ ] Consent management system (granular tracking)
- [ ] Breach notification automation (<72 hours)
- [ ] DPA notification workflows
- [ ] Data subject breach notifications

**Timeline**: 8 weeks (April-May 2027)
**Resources**: 2 backend engineers, 1 frontend engineer

### Phase 3: DPIA & DPA (Q2 2027)

**Deliverables**:
- [ ] DPIA workflow (templates, risk assessment)
- [ ] DPA management (digital signatures, renewal tracking)
- [ ] Compliance dashboard (audit reports)

**Timeline**: 6 weeks (May-June 2027)
**Resources**: 1 backend engineer, 1 frontend engineer, 1 compliance specialist

---

## Cost Estimate

### Development Costs

| Item | Cost | Timeline |
|------|------|----------|
| **Backend Engineers** (2 FTE × 6 months) | $240K-300K | Q1-Q2 2027 |
| **Frontend Engineers** (1 FTE × 5 months) | $100K-125K | Q1-Q2 2027 |
| **Compliance Specialist** (1 FTE × 4 months) | $80K-100K | Q1-Q2 2027 |
| **Legal Consultant** (DPA templates, review) | $20K-30K | Q1 2027 |
| **Total Development** | **$440K-555K** | |

### Infrastructure & Services

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **Storage** (DSAR exports, audit logs) | $5K-10K | S3 with 30-day expiry |
| **Email Service** (breach notifications) | $2K-5K | SendGrid, AWS SES |
| **Identity Verification** (KYC service) | $3K-8K | Onfido, Jumio (optional) |
| **Data Discovery Tools** | $10K-20K | BigID, OneTrust (optional) |
| **Total Infrastructure** | **$20K-43K** | |

### Total Investment: **$460K-598K** (first year)

---

## Success Metrics

### Legal/Compliance KPIs
- ✅ **DSAR response time**: <48 hours automated (from 4-6 weeks manual)
- ✅ **Breach notification time**: <72 hours DPA (Art. 33 compliance)
- ✅ **Consent management**: 100% purpose-specific tracking
- ✅ **DPIA coverage**: 100% high-risk processing activities

### Technical KPIs
- ✅ **Data discovery completeness**: >95% (all personal data cataloged)
- ✅ **Erasure success rate**: >99% (with audit trail)
- ✅ **Identity verification rate**: >90% automated (minimal manual review)

### Business KPIs
- ✅ **EU enterprise deals unblocked**: +30% deal closure rate
- ✅ **GDPR-related incidents**: <5/year (from 20+)
- ✅ **Legal/compliance costs**: 60% reduction (automation)
- ✅ **Customer satisfaction**: >4.5/5 (privacy trust)

---

## Integration with Other ADRs

- **ADR-015**: Data Lifecycle Management (retention policies, deletion)
- **ADR-058**: SOC 2 Compliance (audit trails, access controls)
- **ADR-059**: ISO 27001 ISMS (data protection, vendor management)
- **ADR-062**: Multi-Country Tax Engine (Avalara DPA, cross-border transfers)
- **ADR-007**: Authentication & Authorization (identity verification)

---

## Consequences

### Positive ✅
- **Legal compliance**: Full GDPR compliance, EU market access
- **Reduced fines risk**: Automated breach notification, proper DSAR handling
- **Competitive advantage**: 48-hour DSAR (vs 30-day legal requirement)
- **Customer trust**: Transparent data processing, easy consent management
- **Operational efficiency**: 60% reduction in manual compliance work

### Negative ⚠️
- **Development complexity**: Data discovery across 92 modules challenging
- **Storage overhead**: DSAR exports, audit logs (30-day retention)
- **Performance impact**: Data discovery queries can be slow
- **User experience**: Identity verification adds friction

### Risks 🚨
- **Incomplete data discovery**: Missing personal data in new modules
  - Mitigation: Data catalog enforcement, automated scanning
- **False positives**: Over-deletion (erase data still needed for legal retention)
  - Mitigation: Legal basis checks, retention rules engine
- **Breach notification delays**: Manual assessment bottleneck
  - Mitigation: Automated severity assessment, 24/7 monitoring
- **Consent fatigue**: Too many consent requests
  - Mitigation: Bundled consents, sensible defaults

---

## References

### Legal/Regulatory
- **GDPR**: Regulation (EU) 2016/679
- **Art. 15-21**: Data Subject Rights
- **Art. 33-34**: Breach Notification
- **Art. 35**: Data Protection Impact Assessment
- **EDPB Guidelines**: Data Subject Rights, Breach Notification, DPIA

### Industry Best Practices
- SAP Data Privacy Integration (DPI)
- Oracle Data Privacy Management Cloud
- Salesforce Privacy Center

### Related ADRs
- ADR-007: Authentication & Authorization
- ADR-015: Data Lifecycle Management
- ADR-058: SOC 2 Type II Compliance
- ADR-059: ISO 27001 ISMS
- ADR-062: Multi-Country Tax Engine

---

*Document Owner*: Chief Privacy Officer (DPO)  
*Review Frequency*: Quarterly  
*Next Review*: May 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q1 2027**
