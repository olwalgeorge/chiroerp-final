# ADR-062: Multi-Country Tax Engine

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Finance Domain Team, Localization Team  
**Priority**: P1 (High - Global Market Expansion)  
**Tier**: Domain Core (Finance)  
**Tags**: tax, localization, compliance, global, vat, gst, sales-tax, integration

---

## Context

**Problem**: ChiroERP's current tax engine supports only **basic VAT** in 3 jurisdictions (US, Germany, India). To compete with SAP, Oracle, and Dynamics 365 in global markets, we need **comprehensive tax determination** for 50+ countries with real-time rate updates, nexus determination, exemption management, and automated filing.

**Current State**:
- **Manual tax configuration** (per-tenant, error-prone)
- **No automatic rate updates** (tax law changes require manual intervention)
- **No nexus determination** (US sales tax complexity not supported)
- **No exemption certificate management** (B2B tax exemptions handled manually)
- **No tax filing automation** (customers manually prepare tax returns)
- **Limited jurisdictions**: US (basic sales tax), Germany (VAT), India (GST)

**Market Reality**:
- **65% of ChiroERP prospects** operate in 3+ countries (global expansion)
- **80% of enterprise deals** require multi-country tax compliance
- **Tax compliance risk**: $50K-500K fines per violation (incorrect tax calculation)
- **Lost deals**: 20-25% of international pipeline due to tax limitations
- **Customer churn**: 15% cite tax complexity as reason for considering alternatives

**Competitive Position**:

| Capability | SAP | Oracle | D365 | NetSuite | ChiroERP |
|------------|-----|--------|------|----------|----------|
| **Countries Supported** | 190+ | 200+ | 100+ | 100+ | **3** ❌ |
| **Real-Time Tax Rates** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Nexus Determination** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Exemption Mgmt** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Tax Filing** | ✅ | ✅ | Partial | ✅ | ❌ |
| **Third-Party Integration** | Vertex | Vertex/Avalara | Avalara | SuiteTax | **None** ❌ |

**Customer Quote** (VP Finance, Manufacturing):
> "We sell to 18 countries across EU, APAC, and Americas. Your tax engine only supports 3. We need Avalara integration or we can't use ChiroERP for international operations."

---

## Decision

Build a **Multi-Country Tax Engine** with:

1. **Integration with Avalara AvaTax** (primary provider, 19,000+ tax jurisdictions)
2. **Integration with Vertex Cloud** (secondary provider, enterprise-focused)
3. **Fallback to Built-In Tax Engine** (for basic scenarios, cost optimization)
4. **Real-Time Tax Determination** (API-based, <100ms response time)
5. **Automated Tax Rate Updates** (no manual intervention)
6. **Nexus Management** (US economic nexus, EU establishment)
7. **Exemption Certificate Handling** (B2B, government, non-profit)
8. **Tax Reporting & Filing** (pre-populated returns, e-filing for select jurisdictions)

**Target**: Q1-Q4 2027 (phased rollout by region)

---

## Architecture

### 1. Tax Engine Core (Abstraction Layer)

**New Platform Service**: `finance-gl/tax-engine-service`

```kotlin
/**
 * Tax Engine Service
 * Abstraction layer over multiple tax providers (Avalara, Vertex, Built-In)
 */
@Service
class TaxEngineService(
    private val avalaraProvider: AvalaTaxProvider,
    private val vertexProvider: VertexCloudProvider,
    private val builtInProvider: BuiltInTaxProvider,
    private val nexusService: NexusService,
    private val exemptionService: ExemptionCertificateService
) {
    
    /**
     * Calculate tax for transaction
     * Routes to appropriate provider based on tenant configuration
     */
    suspend fun calculateTax(request: TaxCalculationRequest): TaxCalculationResult {
        val tenant = getTenantConfig(request.tenantId)
        val provider = selectProvider(tenant, request)
        
        logger.info("Calculating tax for ${request.transactionType} using ${provider::class.simpleName}")
        
        try {
            // Pre-calculation: Check exemptions
            val exemption = if (request.customerTaxExempt) {
                exemptionService.getValidExemption(
                    tenantId = request.tenantId,
                    customerId = request.customerId,
                    jurisdiction = request.shipTo.country
                )
            } else null
            
            if (exemption != null && exemption.isValid) {
                return TaxCalculationResult.Exempt(
                    exemptionCertificate = exemption,
                    reason = "Customer has valid tax exemption certificate"
                )
            }
            
            // Nexus check: Do we have tax obligation in this jurisdiction?
            val hasNexus = nexusService.hasNexus(
                tenantId = request.tenantId,
                jurisdiction = request.shipTo.toJurisdiction()
            )
            
            if (!hasNexus) {
                return TaxCalculationResult.NoTaxDue(
                    reason = "No nexus in ${request.shipTo.country}"
                )
            }
            
            // Calculate tax via provider
            val result = when (provider) {
                is AvalaTaxProvider -> avalaraProvider.calculateTax(request)
                is VertexCloudProvider -> vertexProvider.calculateTax(request)
                is BuiltInTaxProvider -> builtInProvider.calculateTax(request)
                else -> throw IllegalStateException("Unknown provider: $provider")
            }
            
            // Post-calculation: Store for audit trail
            storeTaxCalculation(
                tenantId = request.tenantId,
                transactionId = request.transactionId,
                result = result,
                provider = provider::class.simpleName!!
            )
            
            return result
            
        } catch (e: TaxProviderException) {
            logger.error("Tax calculation failed: ${e.message}", e)
            
            // Fallback strategy
            return when (tenant.taxConfig.fallbackStrategy) {
                FallbackStrategy.USE_BUILTIN -> {
                    logger.warn("Falling back to built-in tax engine")
                    builtInProvider.calculateTax(request)
                }
                FallbackStrategy.USE_LAST_KNOWN_RATE -> {
                    logger.warn("Using last known tax rate")
                    useLastKnownRate(request)
                }
                FallbackStrategy.FAIL -> {
                    throw e
                }
            }
        }
    }
    
    /**
     * Select provider based on tenant configuration and transaction characteristics
     */
    private fun selectProvider(
        tenant: TenantConfig,
        request: TaxCalculationRequest
    ): TaxProvider {
        // Priority 1: Tenant preference (if specified)
        if (tenant.taxConfig.preferredProvider != null) {
            return getProviderInstance(tenant.taxConfig.preferredProvider)
        }
        
        // Priority 2: Transaction complexity (use external provider for complex scenarios)
        if (isComplexTransaction(request)) {
            return avalaraProvider // Default for complex
        }
        
        // Priority 3: Cost optimization (use built-in for simple scenarios)
        if (isSimpleTransaction(request) && tenant.taxConfig.costOptimizationEnabled) {
            return builtInProvider
        }
        
        // Default: Avalara (most comprehensive)
        return avalaraProvider
    }
    
    private fun isComplexTransaction(request: TaxCalculationRequest): Boolean {
        return request.shipTo.country != request.billTo.country || // Cross-border
               request.lineItems.any { it.productType == ProductType.DIGITAL } || // Digital goods
               request.lineItems.size > 10 || // Large order
               request.customerType == CustomerType.GOVERNMENT // B2G
    }
    
    private fun isSimpleTransaction(request: TaxCalculationRequest): Boolean {
        return request.shipTo.country == "US" && // Domestic US
               request.lineItems.all { it.productType == ProductType.PHYSICAL } && // Physical goods only
               !request.customerTaxExempt // Not exempt
    }
}

data class TaxCalculationRequest(
    val tenantId: UUID,
    val transactionId: UUID,
    val transactionType: TransactionType,
    val transactionDate: LocalDate,
    val customerId: UUID,
    val customerType: CustomerType,
    val customerTaxExempt: Boolean,
    val billTo: Address,
    val shipTo: Address,
    val lineItems: List<TaxLineItem>,
    val currency: String,
    val documentCode: String? = null // For audit trail
)

data class TaxLineItem(
    val lineNumber: Int,
    val productId: UUID,
    val productCode: String,
    val description: String,
    val productType: ProductType,
    val taxCategory: TaxCategory, // e.g., GENERAL, FOOD, CLOTHING, DIGITAL
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val lineAmount: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO
)

enum class TransactionType {
    SALES_INVOICE,
    PURCHASE_INVOICE,
    CREDIT_MEMO,
    DEBIT_MEMO,
    QUOTE,
    SALES_ORDER
}

enum class ProductType {
    PHYSICAL,
    DIGITAL,
    SERVICE,
    SUBSCRIPTION
}

enum class TaxCategory {
    GENERAL,           // Standard tax rate
    FOOD,              // Reduced rate in many jurisdictions
    CLOTHING,          // Exempt or reduced in some US states
    DIGITAL_GOODS,     // Special rules (EU VAT, US Wayfair)
    MEDICAL,           // Often exempt
    EDUCATION,         // Often exempt
    FINANCIAL_SERVICES // Often exempt (EU VAT)
}

sealed class TaxCalculationResult {
    data class Taxable(
        val totalTax: BigDecimal,
        val lineItems: List<TaxLineItemResult>,
        val summary: TaxSummary,
        val provider: String,
        val transactionId: String // Provider's transaction ID
    ) : TaxCalculationResult()
    
    data class Exempt(
        val exemptionCertificate: ExemptionCertificate,
        val reason: String
    ) : TaxCalculationResult()
    
    data class NoTaxDue(
        val reason: String
    ) : TaxCalculationResult()
}

data class TaxLineItemResult(
    val lineNumber: Int,
    val taxableAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val taxRate: BigDecimal,
    val jurisdiction: TaxJurisdiction,
    val taxType: TaxType, // VAT, GST, Sales Tax, etc.
    val taxBreakdown: List<TaxComponent> // For multi-tier jurisdictions (US: State + County + City)
)

data class TaxComponent(
    val jurisdiction: String,
    val jurisType: JurisdictionType,
    val taxRate: BigDecimal,
    val taxAmount: BigDecimal,
    val taxName: String // e.g., "California State Tax", "Los Angeles County Tax"
)

enum class JurisdictionType {
    COUNTRY,
    STATE,
    COUNTY,
    CITY,
    DISTRICT
}

data class TaxSummary(
    val subtotal: BigDecimal,
    val totalTax: BigDecimal,
    val totalAmount: BigDecimal, // subtotal + totalTax
    val taxByJurisdiction: Map<String, BigDecimal>,
    val taxByType: Map<TaxType, BigDecimal>
)

enum class TaxType {
    VAT,         // Value Added Tax (EU, UK, etc.)
    GST,         // Goods & Services Tax (India, Australia, Canada, etc.)
    SALES_TAX,   // US State Sales Tax
    USE_TAX,     // US Use Tax (for untaxed purchases)
    EXCISE,      // Excise Tax (alcohol, tobacco, fuel)
    CUSTOMS_DUTY,// Import duties
    WITHHOLDING  // Withholding tax (cross-border services)
}
```

---

### 2. Avalara AvaTax Integration (Primary Provider)

**Why Avalara?**
- **19,000+ tax jurisdictions** worldwide (US, Canada, EU, APAC, LATAM)
- **Real-time rate updates** (daily, automatic)
- **Returns filing** (automated for 12,000+ jurisdictions)
- **Exemption certificate management** (CertCapture)
- **98% of Fortune 500** use Avalara or Vertex

```kotlin
/**
 * Avalara AvaTax Provider
 * REST API integration with AvaTax v2
 */
@Component
class AvalaTaxProvider(
    private val httpClient: HttpClient,
    private val credentialService: CredentialService
) : TaxProvider {
    
    private val baseUrl = "https://rest.avatax.com/api/v2"
    
    override suspend fun calculateTax(request: TaxCalculationRequest): TaxCalculationResult {
        val credentials = credentialService.getAvalaraCredentials(request.tenantId)
        
        val avalaraRequest = AvaTaxCreateTransactionModel(
            companyCode = credentials.companyCode,
            type = mapTransactionType(request.transactionType),
            customerCode = request.customerId.toString(),
            date = request.transactionDate.toString(),
            addresses = AvaTaxAddresses(
                shipFrom = null, // Will use company default
                shipTo = mapAddress(request.shipTo, AddressType.ShipTo),
                billTo = mapAddress(request.billTo, AddressType.BillTo)
            ),
            lines = request.lineItems.map { lineItem ->
                AvaTaxLineItem(
                    number = lineItem.lineNumber.toString(),
                    itemCode = lineItem.productCode,
                    description = lineItem.description,
                    taxCode = mapTaxCategory(lineItem.taxCategory),
                    quantity = lineItem.quantity.toDouble(),
                    amount = lineItem.lineAmount.toDouble(),
                    discounted = lineItem.discountAmount > BigDecimal.ZERO,
                    taxIncluded = false // ChiroERP uses tax-exclusive pricing
                )
            },
            commit = false, // Don't commit yet (commit on invoice posting)
            currencyCode = request.currency,
            description = "ChiroERP ${request.transactionType} ${request.documentCode ?: ""}"
        )
        
        val response = httpClient.post("$baseUrl/transactions/create") {
            basicAuth(credentials.accountId, credentials.licenseKey)
            contentType(ContentType.Application.Json)
            setBody(avalaraRequest)
        }
        
        if (response.status != HttpStatusCode.Created) {
            throw TaxProviderException("Avalara API error: ${response.status}")
        }
        
        val avalaraResponse: AvaTaxTransactionModel = response.body()
        
        return TaxCalculationResult.Taxable(
            totalTax = avalaraResponse.totalTax.toBigDecimal(),
            lineItems = avalaraResponse.lines.map { line ->
                TaxLineItemResult(
                    lineNumber = line.lineNumber.toInt(),
                    taxableAmount = line.taxableAmount.toBigDecimal(),
                    taxAmount = line.tax.toBigDecimal(),
                    taxRate = line.rate.toBigDecimal(),
                    jurisdiction = TaxJurisdiction(
                        country = avalaraResponse.addresses.shipTo.country,
                        state = avalaraResponse.addresses.shipTo.region,
                        county = null,
                        city = avalaraResponse.addresses.shipTo.city
                    ),
                    taxType = mapTaxType(line.taxType),
                    taxBreakdown = line.details.map { detail ->
                        TaxComponent(
                            jurisdiction = detail.jurisdictionName,
                            jurisType = mapJurisType(detail.jurisType),
                            taxRate = detail.rate.toBigDecimal(),
                            taxAmount = detail.tax.toBigDecimal(),
                            taxName = detail.taxName
                        )
                    }
                )
            },
            summary = TaxSummary(
                subtotal = avalaraResponse.totalAmount.toBigDecimal() - avalaraResponse.totalTax.toBigDecimal(),
                totalTax = avalaraResponse.totalTax.toBigDecimal(),
                totalAmount = avalaraResponse.totalAmount.toBigDecimal(),
                taxByJurisdiction = avalaraResponse.summary.groupBy { it.jurisName }
                    .mapValues { it.value.sumOf { s -> s.tax }.toBigDecimal() },
                taxByType = avalaraResponse.summary.groupBy { mapTaxType(it.taxType) }
                    .mapValues { it.value.sumOf { s -> s.tax }.toBigDecimal() }
            ),
            provider = "Avalara AvaTax",
            transactionId = avalaraResponse.id.toString()
        )
    }
    
    /**
     * Commit transaction (make permanent in Avalara)
     * Called when invoice is posted in ChiroERP
     */
    suspend fun commitTransaction(
        tenantId: UUID,
        transactionCode: String
    ): Boolean {
        val credentials = credentialService.getAvalaraCredentials(tenantId)
        
        val response = httpClient.post("$baseUrl/companies/${credentials.companyCode}/transactions/$transactionCode/commit") {
            basicAuth(credentials.accountId, credentials.licenseKey)
            contentType(ContentType.Application.Json)
            setBody(AvaTaxCommitTransactionModel(commit = true))
        }
        
        return response.status == HttpStatusCode.OK
    }
    
    /**
     * Void transaction (cancel)
     * Called when invoice is voided/deleted in ChiroERP
     */
    suspend fun voidTransaction(
        tenantId: UUID,
        transactionCode: String,
        reason: String
    ): Boolean {
        val credentials = credentialService.getAvalaraCredentials(tenantId)
        
        val response = httpClient.post("$baseUrl/companies/${credentials.companyCode}/transactions/$transactionCode/void") {
            basicAuth(credentials.accountId, credentials.licenseKey)
            contentType(ContentType.Application.Json)
            setBody(AvaTaxVoidTransactionModel(code = "DocVoided"))
        }
        
        return response.status == HttpStatusCode.OK
    }
    
    private fun mapTransactionType(type: TransactionType): String {
        return when (type) {
            TransactionType.SALES_INVOICE -> "SalesInvoice"
            TransactionType.PURCHASE_INVOICE -> "PurchaseInvoice"
            TransactionType.CREDIT_MEMO -> "ReturnInvoice"
            TransactionType.QUOTE -> "SalesOrder"
            TransactionType.SALES_ORDER -> "SalesOrder"
            else -> "SalesInvoice"
        }
    }
    
    private fun mapTaxCategory(category: TaxCategory): String {
        // Avalara tax codes
        return when (category) {
            TaxCategory.GENERAL -> "P0000000" // Tangible personal property
            TaxCategory.FOOD -> "PF050000" // Food and grocery
            TaxCategory.CLOTHING -> "PC040100" // Clothing
            TaxCategory.DIGITAL_GOODS -> "DC010200" // Digital goods
            TaxCategory.MEDICAL -> "PM000000" // Medical
            TaxCategory.EDUCATION -> "PE000000" // Educational
            TaxCategory.FINANCIAL_SERVICES -> "PF000000" // Financial services
        }
    }
}

// Avalara API Models
data class AvaTaxCreateTransactionModel(
    val companyCode: String,
    val type: String,
    val customerCode: String,
    val date: String,
    val addresses: AvaTaxAddresses,
    val lines: List<AvaTaxLineItem>,
    val commit: Boolean,
    val currencyCode: String,
    val description: String
)

data class AvaTaxAddresses(
    val shipFrom: AvaTaxAddress?,
    val shipTo: AvaTaxAddress,
    val billTo: AvaTaxAddress
)

data class AvaTaxAddress(
    val line1: String,
    val city: String,
    val region: String, // State/Province
    val country: String,
    val postalCode: String
)

data class AvaTaxLineItem(
    val number: String,
    val itemCode: String,
    val description: String,
    val taxCode: String,
    val quantity: Double,
    val amount: Double,
    val discounted: Boolean,
    val taxIncluded: Boolean
)
```

**Avalara API Endpoints Used**:

| Operation | Endpoint | Purpose |
|-----------|----------|---------|
| **Calculate Tax** | POST `/transactions/create` | Real-time tax calculation |
| **Commit Transaction** | POST `/transactions/{code}/commit` | Make transaction permanent |
| **Void Transaction** | POST `/transactions/{code}/void` | Cancel/void transaction |
| **List Tax Codes** | GET `/taxcodes` | Retrieve Avalara tax codes |
| **Validate Address** | POST `/addresses/resolve` | Address validation |
| **Get Tax Rates** | GET `/taxrates/bypostalcode` | Tax rates by postal code |

---

### 3. Nexus Management

**Economic Nexus** (US Wayfair ruling - sales tax obligation without physical presence):

```kotlin
/**
 * Nexus Service
 * Determines if tenant has tax obligation in jurisdiction
 */
@Service
class NexusService(
    private val nexusRepository: NexusRepository,
    private val salesAnalytics: SalesAnalyticsService
) {
    
    /**
     * Check if tenant has nexus (tax obligation) in jurisdiction
     */
    suspend fun hasNexus(
        tenantId: UUID,
        jurisdiction: TaxJurisdiction
    ): Boolean {
        // 1. Check explicit nexus configuration (user-defined)
        val explicitNexus = nexusRepository.findByTenantAndJurisdiction(
            tenantId = tenantId,
            country = jurisdiction.country,
            state = jurisdiction.state
        )
        
        if (explicitNexus != null && explicitNexus.status == NexusStatus.ACTIVE) {
            return true
        }
        
        // 2. Check economic nexus (US states only)
        if (jurisdiction.country == "US" && jurisdiction.state != null) {
            val economicNexus = checkEconomicNexus(
                tenantId = tenantId,
                state = jurisdiction.state
            )
            
            if (economicNexus) {
                // Auto-register nexus
                registerNexus(
                    tenantId = tenantId,
                    jurisdiction = jurisdiction,
                    nexusType = NexusType.ECONOMIC,
                    reason = "Economic nexus threshold exceeded"
                )
                return true
            }
        }
        
        // 3. Check EU VAT establishment (if applicable)
        if (isEUCountry(jurisdiction.country)) {
            val euEstablishment = hasEUEstablishment(tenantId, jurisdiction.country)
            if (euEstablishment) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Check US economic nexus (state-specific thresholds)
     */
    private suspend fun checkEconomicNexus(
        tenantId: UUID,
        state: String
    ): Boolean {
        val threshold = getEconomicNexusThreshold(state)
        
        // Get sales to state in trailing 12 months
        val salesData = salesAnalytics.getSalesByState(
            tenantId = tenantId,
            state = state,
            period = Period.ofMonths(12)
        )
        
        // Check if threshold exceeded
        return salesData.totalRevenue >= threshold.revenueThreshold ||
               salesData.transactionCount >= threshold.transactionThreshold
    }
    
    /**
     * Economic nexus thresholds by US state
     * Source: Avalara Economic Nexus Guide
     */
    private fun getEconomicNexusThreshold(state: String): EconomicNexusThreshold {
        return when (state) {
            "CA" -> EconomicNexusThreshold(500_000.toBigDecimal(), null) // California: $500K revenue
            "TX" -> EconomicNexusThreshold(500_000.toBigDecimal(), null) // Texas: $500K revenue
            "NY" -> EconomicNexusThreshold(500_000.toBigDecimal(), 100) // New York: $500K AND 100 transactions
            "FL" -> EconomicNexusThreshold(100_000.toBigDecimal(), null) // Florida: $100K revenue
            "WA" -> EconomicNexusThreshold(100_000.toBigDecimal(), null) // Washington: $100K revenue
            // ... 45 other states (all states post-Wayfair have economic nexus)
            else -> EconomicNexusThreshold(100_000.toBigDecimal(), 200) // Default: SD v. Wayfair ruling
        }
    }
}

data class EconomicNexusThreshold(
    val revenueThreshold: BigDecimal, // Annual revenue in USD
    val transactionThreshold: Int?    // Number of transactions (if applicable)
)

data class NexusRegistration(
    val id: UUID,
    val tenantId: UUID,
    val country: String,
    val state: String?,
    val nexusType: NexusType,
    val status: NexusStatus,
    val effectiveDate: LocalDate,
    val endDate: LocalDate?,
    val registrationNumber: String?, // e.g., Sales tax permit number
    val reason: String
)

enum class NexusType {
    PHYSICAL_PRESENCE,  // Office, warehouse, employees
    ECONOMIC,           // Sales threshold exceeded (US Wayfair)
    MARKETPLACE,        // Sales via marketplace (Amazon, eBay)
    AFFILIATE,          // Affiliate marketing
    CLICK_THROUGH,      // Online advertising
    INVENTORY           // Inventory stored (Amazon FBA)
}

enum class NexusStatus {
    ACTIVE,
    PENDING,
    INACTIVE,
    SUSPENDED
}
```

**Nexus Dashboard** (UI):

```yaml
Nexus Management Dashboard:
  Active Nexus:
    - Country / State
    - Nexus Type (Physical, Economic, etc.)
    - Effective Date
    - Registration Number
    - Status (Active, Pending)
  
  Economic Nexus Monitor:
    - State
    - Current Sales (trailing 12 months)
    - Threshold
    - Status (Below, Approaching 80%, Exceeded)
    - Action (No action, Register, File returns)
  
  Nexus Recommendations:
    - "You have exceeded the economic nexus threshold in California. Register for sales tax permit."
    - "Your sales in Texas are approaching the $500K threshold (currently $425K)."
  
  Actions:
    - Register Nexus (manual entry)
    - Auto-detect Economic Nexus (scan all US states)
    - Download Registration Guide (per state)
```

---

### 4. Exemption Certificate Management

**B2B Tax Exemptions** (resale certificates, government exemptions):

```kotlin
/**
 * Exemption Certificate Service
 * Manages tax exemption certificates (B2B, government, non-profit)
 */
@Service
class ExemptionCertificateService(
    private val certificateRepository: ExemptionCertificateRepository,
    private val documentStorage: DocumentStorageService,
    private val avalaraCertCapture: AvalaraCertCaptureService // Optional Avalara CertCapture integration
) {
    
    /**
     * Get valid exemption certificate for customer in jurisdiction
     */
    suspend fun getValidExemption(
        tenantId: UUID,
        customerId: UUID,
        jurisdiction: String
    ): ExemptionCertificate? {
        val certificates = certificateRepository.findByCustomer(
            tenantId = tenantId,
            customerId = customerId
        )
        
        return certificates.firstOrNull { cert ->
            cert.isValid &&
            cert.expirationDate?.isAfter(LocalDate.now()) != false &&
            cert.applicableJurisdictions.contains(jurisdiction)
        }
    }
    
    /**
     * Upload exemption certificate (customer self-service or admin)
     */
    suspend fun uploadCertificate(
        tenantId: UUID,
        customerId: UUID,
        certificateFile: ByteArray,
        metadata: ExemptionCertificateMetadata
    ): ExemptionCertificate {
        // 1. Store document in S3/Azure Blob
        val documentUrl = documentStorage.upload(
            tenantId = tenantId,
            folder = "exemption-certificates",
            filename = "${customerId}_${metadata.certificateNumber}.pdf",
            content = certificateFile
        )
        
        // 2. Create certificate record
        val certificate = ExemptionCertificate(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            customerId = customerId,
            certificateType = metadata.exemptionType,
            certificateNumber = metadata.certificateNumber,
            issuingJurisdiction = metadata.issuingJurisdiction,
            applicableJurisdictions = metadata.applicableJurisdictions,
            issueDate = metadata.issueDate,
            expirationDate = metadata.expirationDate,
            documentUrl = documentUrl,
            status = ExemptionStatus.PENDING_REVIEW,
            isValid = false // Requires review
        )
        
        certificateRepository.save(certificate)
        
        // 3. Optional: Send to Avalara CertCapture for validation
        if (avalaraCertCapture.isEnabled(tenantId)) {
            avalaraCertCapture.submitForValidation(certificate)
        }
        
        return certificate
    }
    
    /**
     * Validate exemption certificate (manual or automated)
     */
    suspend fun validateCertificate(
        certificateId: UUID,
        validatedBy: UUID,
        isValid: Boolean,
        notes: String?
    ) {
        val certificate = certificateRepository.findById(certificateId)
            ?: throw NotFoundException("Certificate not found")
        
        certificateRepository.update(
            certificate.copy(
                status = if (isValid) ExemptionStatus.APPROVED else ExemptionStatus.REJECTED,
                isValid = isValid,
                validatedBy = validatedBy,
                validatedAt = Instant.now(),
                validationNotes = notes
            )
        )
    }
}

data class ExemptionCertificate(
    val id: UUID,
    val tenantId: UUID,
    val customerId: UUID,
    val certificateType: ExemptionType,
    val certificateNumber: String,
    val issuingJurisdiction: String,
    val applicableJurisdictions: Set<String>, // e.g., ["US", "US-CA", "US-TX"]
    val issueDate: LocalDate,
    val expirationDate: LocalDate?,
    val documentUrl: String,
    val status: ExemptionStatus,
    val isValid: Boolean,
    val validatedBy: UUID? = null,
    val validatedAt: Instant? = null,
    val validationNotes: String? = null
)

enum class ExemptionType {
    RESALE,              // Resale certificate (B2B)
    GOVERNMENT,          // Government entity
    NON_PROFIT,          // 501(c)(3) non-profit
    AGRICULTURAL,        // Agricultural production
    MANUFACTURING,       // Manufacturing equipment
    EXPORT,              // Export (zero-rated)
    DIPLOMATIC,          // Diplomatic exemption
    OTHER
}

enum class ExemptionStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    EXPIRED,
    SUSPENDED
}
```

**Exemption Certificate UI** (Customer Portal):

```yaml
Customer Tax Exemption Portal:
  My Certificates:
    - Certificate Number
    - Type (Resale, Government, etc.)
    - Valid Jurisdictions
    - Expiration Date
    - Status (Active, Pending, Expired)
    - Actions (View, Download, Renew)
  
  Upload New Certificate:
    - Certificate Type (dropdown)
    - Certificate Number
    - Issuing Jurisdiction
    - Valid In (multi-select: US states, countries)
    - Issue Date / Expiration Date
    - Upload PDF (drag & drop)
    - Submit for Review
  
  Exemption Status:
    - "Your California Resale Certificate is valid until Dec 31, 2026"
    - "Your Texas certificate expires in 30 days. Please renew."
    - "New certificate pending review (ETA: 2 business days)"
```

---

### 5. Tax Reporting & Filing

**Tax Returns** (pre-populated, ready for filing):

```kotlin
/**
 * Tax Reporting Service
 * Generates tax returns for filing
 */
@Service
class TaxReportingService(
    private val taxTransactionRepository: TaxTransactionRepository,
    private val avalaraTaxFilingService: AvalaraTaxFilingService
) {
    
    /**
     * Generate tax return for period
     */
    suspend fun generateTaxReturn(
        tenantId: UUID,
        jurisdiction: TaxJurisdiction,
        period: TaxPeriod
    ): TaxReturn {
        // 1. Retrieve all tax transactions for period
        val transactions = taxTransactionRepository.findByPeriod(
            tenantId = tenantId,
            jurisdiction = jurisdiction,
            startDate = period.startDate,
            endDate = period.endDate
        )
        
        // 2. Aggregate by tax type and rate
        val salesSummary = transactions
            .filter { it.transactionType == TransactionType.SALES_INVOICE }
            .groupBy { it.taxRate }
            .mapValues { entry ->
                TaxReturnLine(
                    taxableAmount = entry.value.sumOf { it.taxableAmount },
                    taxAmount = entry.value.sumOf { it.taxAmount },
                    transactionCount = entry.value.size
                )
            }
        
        val purchaseSummary = transactions
            .filter { it.transactionType == TransactionType.PURCHASE_INVOICE }
            .groupBy { it.taxRate }
            .mapValues { entry ->
                TaxReturnLine(
                    taxableAmount = entry.value.sumOf { it.taxableAmount },
                    taxAmount = entry.value.sumOf { it.taxAmount },
                    transactionCount = entry.value.size
                )
            }
        
        // 3. Calculate net tax due
        val totalSalesTax = salesSummary.values.sumOf { it.taxAmount }
        val totalPurchaseTax = purchaseSummary.values.sumOf { it.taxAmount }
        val netTaxDue = totalSalesTax - totalPurchaseTax
        
        // 4. Create tax return
        return TaxReturn(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            jurisdiction = jurisdiction,
            period = period,
            filingFrequency = getFilingFrequency(tenantId, jurisdiction),
            salesLines = salesSummary,
            purchaseLines = purchaseSummary,
            totalSalesTax = totalSalesTax,
            totalPurchaseTax = totalPurchaseTax,
            netTaxDue = netTaxDue,
            status = TaxReturnStatus.DRAFT,
            dueDate = calculateDueDate(period, jurisdiction)
        )
    }
    
    /**
     * File tax return (submit to tax authority)
     * Uses Avalara Returns for automated filing
     */
    suspend fun fileTaxReturn(
        taxReturnId: UUID,
        filedBy: UUID
    ): TaxReturnFilingResult {
        val taxReturn = getTaxReturn(taxReturnId)
        
        // File via Avalara Returns (if enabled and jurisdiction supported)
        return if (avalaraTaxFilingService.isSupported(taxReturn.jurisdiction)) {
            avalaraTaxFilingService.fileReturn(taxReturn)
        } else {
            // Manual filing required
            TaxReturnFilingResult.ManualFilingRequired(
                downloadUrl = generateFilingPackage(taxReturn),
                instructions = getFilingInstructions(taxReturn.jurisdiction)
            )
        }
    }
}

data class TaxReturn(
    val id: UUID,
    val tenantId: UUID,
    val jurisdiction: TaxJurisdiction,
    val period: TaxPeriod,
    val filingFrequency: FilingFrequency,
    val salesLines: Map<BigDecimal, TaxReturnLine>, // Key: tax rate
    val purchaseLines: Map<BigDecimal, TaxReturnLine>,
    val totalSalesTax: BigDecimal,
    val totalPurchaseTax: BigDecimal,
    val netTaxDue: BigDecimal,
    val status: TaxReturnStatus,
    val dueDate: LocalDate,
    val filedDate: LocalDate? = null,
    val confirmationNumber: String? = null
)

data class TaxReturnLine(
    val taxableAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val transactionCount: Int
)

enum class FilingFrequency {
    MONTHLY,
    QUARTERLY,
    ANNUALLY
}

enum class TaxReturnStatus {
    DRAFT,
    READY_TO_FILE,
    FILED,
    PAID,
    OVERDUE
}

data class TaxPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodType: FilingFrequency
)
```

**Tax Return Dashboard**:

```yaml
Tax Filing Dashboard:
  Upcoming Returns:
    - Jurisdiction: California
    - Period: Q4 2025
    - Due Date: Jan 31, 2026
    - Net Tax Due: $12,450
    - Status: Ready to File
    - Actions: [Preview, File, Download]
  
  Filed Returns:
    - Jurisdiction: Texas
    - Period: Q3 2025
    - Filed Date: Oct 25, 2025
    - Confirmation: TX-2025-Q3-1234567
    - Payment: $8,320 (Paid)
  
  Return Details:
    Sales Summary:
      - Taxable Sales: $1,245,000
      - Tax Collected: $99,600 (8%)
      - Exempt Sales: $50,000
      - Transactions: 1,247
    
    Purchase Summary:
      - Taxable Purchases: $320,000
      - Tax Paid: $25,600 (8%)
      - Exempt Purchases: $10,000
      - Transactions: 342
    
    Net Tax Due: $73,400
    Due Date: Jan 31, 2026
```

---

### 6. Built-In Tax Engine (Fallback)

**Simple Tax Engine** (for cost optimization, no external API dependency):

```kotlin
/**
 * Built-In Tax Provider
 * Fallback for simple tax scenarios (single-jurisdiction, fixed rates)
 */
@Component
class BuiltInTaxProvider(
    private val taxRateRepository: TaxRateRepository
) : TaxProvider {
    
    override suspend fun calculateTax(request: TaxCalculationRequest): TaxCalculationResult {
        val taxRates = taxRateRepository.findByJurisdiction(
            country = request.shipTo.country,
            state = request.shipTo.state,
            effectiveDate = request.transactionDate
        )
        
        if (taxRates.isEmpty()) {
            return TaxCalculationResult.NoTaxDue(
                reason = "No tax rates configured for ${request.shipTo.country}"
            )
        }
        
        val lineResults = request.lineItems.map { lineItem ->
            val applicableRate = taxRates.firstOrNull { rate ->
                rate.taxCategory == lineItem.taxCategory || rate.taxCategory == TaxCategory.GENERAL
            } ?: taxRates.first()
            
            val taxableAmount = lineItem.lineAmount - lineItem.discountAmount
            val taxAmount = taxableAmount * applicableRate.rate / BigDecimal(100)
            
            TaxLineItemResult(
                lineNumber = lineItem.lineNumber,
                taxableAmount = taxableAmount,
                taxAmount = taxAmount,
                taxRate = applicableRate.rate,
                jurisdiction = TaxJurisdiction(
                    country = request.shipTo.country,
                    state = request.shipTo.state
                ),
                taxType = applicableRate.taxType,
                taxBreakdown = listOf(
                    TaxComponent(
                        jurisdiction = "${request.shipTo.country}${request.shipTo.state?.let { " - $it" } ?: ""}",
                        jurisType = JurisdictionType.STATE,
                        taxRate = applicableRate.rate,
                        taxAmount = taxAmount,
                        taxName = applicableRate.name
                    )
                )
            )
        }
        
        return TaxCalculationResult.Taxable(
            totalTax = lineResults.sumOf { it.taxAmount },
            lineItems = lineResults,
            summary = TaxSummary(
                subtotal = request.lineItems.sumOf { it.lineAmount },
                totalTax = lineResults.sumOf { it.taxAmount },
                totalAmount = request.lineItems.sumOf { it.lineAmount } + lineResults.sumOf { it.taxAmount },
                taxByJurisdiction = mapOf(request.shipTo.country to lineResults.sumOf { it.taxAmount }),
                taxByType = mapOf(lineResults.first().taxType to lineResults.sumOf { it.taxAmount })
            ),
            provider = "Built-In Tax Engine",
            transactionId = UUID.randomUUID().toString()
        )
    }
}

data class TaxRate(
    val id: UUID,
    val country: String,
    val state: String?,
    val taxCategory: TaxCategory,
    val rate: BigDecimal, // Percentage (e.g., 8.25 for 8.25%)
    val taxType: TaxType,
    val name: String,
    val effectiveDate: LocalDate,
    val endDate: LocalDate?
)
```

**Tax Rate Configuration** (Admin UI):

```yaml
Built-In Tax Rates:
  Manage Tax Rates:
    - Country: United States
    - State: California
    - Tax Category: General
    - Tax Type: Sales Tax
    - Rate: 7.25%
    - Effective Date: Jan 1, 2025
    - End Date: (none)
    - Actions: [Edit, Deactivate]
  
  Add Tax Rate:
    - Country (dropdown)
    - State/Province (dropdown)
    - Tax Category (General, Food, etc.)
    - Tax Type (VAT, GST, Sales Tax)
    - Rate (%)
    - Effective Date
    - End Date (optional)
  
  Bulk Import:
    - Upload CSV (country, state, rate, effective date)
    - Preview (100 rates)
    - Import
```

---

## Implementation Roadmap

### Phase 1: Avalara Integration (Q1 2027)

**Deliverables**:
- [ ] Avalara AvaTax integration (calculate, commit, void)
- [ ] Tax calculation API (abstraction layer)
- [ ] North America support (US, Canada, Mexico)
- [ ] Nexus management (US economic nexus)

**Timeline**: 10 weeks (January-March 2027)
**Resources**: 2 backend engineers, 1 integration specialist

### Phase 2: Exemption & Reporting (Q2 2027)

**Deliverables**:
- [ ] Exemption certificate management
- [ ] Tax reporting (return generation)
- [ ] Tax filing integration (Avalara Returns)
- [ ] Built-in tax engine (fallback)

**Timeline**: 8 weeks (April-May 2027)
**Resources**: 2 backend engineers, 1 frontend engineer

### Phase 3: EU Expansion (Q3 2027)

**Deliverables**:
- [ ] EU VAT support (27 countries)
- [ ] UK VAT (post-Brexit)
- [ ] MOSS (Mini One Stop Shop) for digital services
- [ ] Intrastat reporting

**Timeline**: 8 weeks (July-August 2027)
**Resources**: 2 backend engineers, 1 localization specialist

### Phase 4: APAC & Rest of World (Q4 2027)

**Deliverables**:
- [ ] India GST (enhanced)
- [ ] Australia GST
- [ ] Singapore GST
- [ ] Japan Consumption Tax
- [ ] Vertex Cloud integration (alternative provider)

**Timeline**: 10 weeks (September-November 2027)
**Resources**: 2 backend engineers, 1 integration specialist

---

## Cost Estimate

### Development Costs

| Item | Cost | Timeline |
|------|------|----------|
| **Backend Engineers** (2 FTE × 9 months) | $360K-450K | Q1-Q4 2027 |
| **Integration Specialists** (2 FTE × 5 months) | $100K-125K | Q1 + Q4 2027 |
| **Frontend Engineer** (1 FTE × 2 months) | $40K-50K | Q2 2027 |
| **Localization Specialist** (1 FTE × 2 months) | $30K-40K | Q3 2027 |
| **Total Development** | **$530K-665K** | |

### Software Licensing (Annual)

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **Avalara AvaTax** | $30K-100K | Based on transaction volume (10K-100K/month) |
| **Avalara CertCapture** | $10K-30K | Optional (exemption certificates) |
| **Avalara Returns** | $15K-50K | Optional (automated filing) |
| **Vertex Cloud** | $40K-120K | Alternative/secondary provider |
| **Total Licensing** | **$55K-180K** | Varies by customer usage |

### Infrastructure & Operations

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **API Gateway** (rate limiting, caching) | $6K-12K | Reduce external API calls |
| **Database Storage** (tax audit logs) | $3K-6K | 3-year retention |
| **Monitoring** (API uptime, performance) | $2K-4K | Datadog, New Relic |
| **Total Infrastructure** | **$11K-22K** | |

### Total Investment: **$596K-867K** (first year)

---

## Success Metrics

### Technical KPIs
- ✅ **Tax calculation accuracy**: >99.9% (validated against Avalara)
- ✅ **API response time**: <100ms (P95)
- ✅ **API availability**: >99.9% (including Avalara uptime)
- ✅ **Supported jurisdictions**: 50+ countries (190+ by 2028)

### Business KPIs
- ✅ **International deal closure**: +30% (from 65% to 85%)
- ✅ **Tax compliance incidents**: <5 per year (vs 20+ currently)
- ✅ **Customer satisfaction**: >4.5/5 (tax accuracy/ease)
- ✅ **Revenue from intl customers**: +40% (Y/Y)

### Operational KPIs
- ✅ **Support ticket reduction**: 60% (tax-related)
- ✅ **Tax return filing time**: 80% reduction (from 8 hours to <2 hours)
- ✅ **Economic nexus monitoring**: 100% automated (no manual tracking)

---

## Alternatives Considered

### 1. Build 100% In-House Tax Engine
**Rejected**: Maintaining 190+ country tax rates impossible, legal liability, no automated filing.

### 2. Avalara Only (No Built-In Fallback)
**Rejected**: High cost for simple scenarios ($0.10-0.50 per transaction), vendor lock-in.

### 3. Vertex Only (Instead of Avalara)
**Rejected**: Vertex more expensive, less SMB-friendly, but keep as secondary option.

### 4. Multi-Provider (Avalara + Vertex + Sovos + Taxjar)
**Rejected**: Too complex, start with Avalara + Built-In, add Vertex later if needed.

---

## Consequences

### Positive ✅
- **Global market access**: 50+ countries (vs 3 currently)
- **Competitive parity**: Match SAP/Oracle/D365 tax capabilities
- **Revenue growth**: +40% from international customers
- **Reduced liability**: Automated rate updates, audit trails
- **Customer confidence**: "Avalara-powered" is market standard
- **Sales velocity**: Tax no longer deal blocker

### Negative ⚠️
- **Licensing cost**: $55K-180K/year (Avalara + Vertex)
- **External dependency**: Avalara API downtime impacts ChiroERP
- **Complexity**: Multi-provider orchestration logic
- **Data privacy**: Sending transaction data to Avalara (GDPR consideration)

### Risks 🚨
- **Avalara API changes**: Breaking changes require updates
  - Mitigation: API versioning, automated testing, Avalara partnership
- **Cost overruns**: High-volume customers exceed licensing tiers
  - Mitigation: Cost optimization (built-in engine for simple txns), volume discounts
- **Tax law changes**: New regulations require rapid updates
  - Mitigation: Avalara handles rate updates (daily), monitor tax authority changes
- **Competitive pricing**: Avalara expensive for SMB
  - Mitigation: Built-in engine for low-volume customers, tiered pricing

---

## Compliance & Integration

### Integration with Other ADRs

- **ADR-009**: Financial Accounting Domain (GL posting, tax accounts)
- **ADR-058**: SOC 2 Compliance (audit trails, access controls)
- **ADR-059**: ISO 27001 ISMS (data protection, vendor management)
- **ADR-064**: GDPR Compliance (data transfer to Avalara, data residency)
- **ADR-065**: Country-Specific Localization (EU VAT, India GST, Brazil SPED)

### Data Privacy (Avalara Integration)

```yaml
GDPR Considerations:
  Data Processing Agreement:
    - Avalara is Data Processor (ChiroERP is Data Controller)
    - DPA signed with Avalara (GDPR Article 28)
    - Data residency: EU data processed in EU (Avalara EU region)
  
  Data Minimization:
    - Only send necessary fields (address, amount, product code)
    - No customer PII (names, emails) unless required
    - Anonymize customer codes (hash customer IDs)
  
  Data Retention:
    - Avalara retains tax calculations 7 years (legal requirement)
    - ChiroERP audit logs: 3 years (ADR-015)
    - Customer can request deletion (GDPR right to erasure)
  
  Cross-Border Transfers:
    - EU to US: Standard Contractual Clauses (SCCs)
    - Data Processing Addendum (DPA) with Avalara
    - Transfer Impact Assessment (TIA) completed
```

---

## References

### Tax Authorities
- IRS: https://www.irs.gov/businesses/sales-tax
- EU VAT: https://taxation-customs.ec.europa.eu/
- India GST: https://www.gst.gov.in/

### Tax Providers
- Avalara: https://www.avalara.com/
- Vertex: https://www.vertexinc.com/
- Taxjar: https://www.taxjar.com/

### Related ADRs
- ADR-009: Financial Accounting Domain
- ADR-058: SOC 2 Type II Compliance
- ADR-059: ISO 27001 ISMS
- ADR-064: GDPR Compliance
- ADR-065: Country-Specific Localization

---

*Document Owner*: Finance Domain Lead  
*Review Frequency*: Quarterly  
*Next Review*: April 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q1 2027**
