# ADR-047: Localization & Regulatory Framework

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Product Team, Compliance Team
**Priority**: P1 (High)
**Tier**: Core
**Tags**: localization, compliance, country-packs, tax, e-invoicing, legal

## Context
ChiroERP is intended to operate across jurisdictions. Finance, tax, invoicing, banking, audit, and reporting requirements vary by country and evolve over time. This ADR defines a **localization and regulatory content model** that delivers country-specific rules and templates as versioned, data-driven “country packs” (similar to SAP localization content) so that new markets and regulatory updates are delivered through content releases rather than bespoke code forks.

### Design Goals
- Add and update regulatory content by country with minimal core-code change
- Version and effective-date regulatory content (audit-ready, reproducible)
- Separate deterministic domain logic from country-specific policy content
- Provide controlled rollout (preview, validation, certification, deprecation)

### Coverage Requirements (Examples)
- EU: VAT rules, e-invoicing profiles (PEPPOL/FatturaPA), audit files (SAF-T/GDPdU), statutory reporting
- APAC: GST rules and local audit/reporting formats
- LATAM: e-invoicing and country-specific fiscal document requirements

### Problem Statement
How do we enable ChiroERP to operate in 50+ countries with minimal engineering effort per market?

## Decision
Implement a **Country Pack Framework** that externalizes regulatory rules, templates, and formats into versioned, data-driven country packs (similar to SAP Localization or Oracle GLI).

---

## Core Concepts

### 1. Country Pack Model

```kotlin
// Domain: Localization (new domain)
data class CountryPack(
    val countryPackId: CountryPackId,
    val countryCode: String, // ISO 3166-1 alpha-2 (US, DE, IN, BR)
    val countryName: String,
    val version: SemanticVersion, // e.g., 1.2.0
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val status: CountryPackStatus, // ACTIVE, DEPRECATED, SUPERSEDED

    // Regulatory Components
    val taxRules: TaxRuleSet,
    val eInvoicingConfig: EInvoicingConfig?,
    val chartOfAccountsTemplate: CoATemplate,
    val bankingFormats: List<BankingFormat>,
    val statutoryReports: List<StatutoryReportTemplate>,
    val legalEntitiesConfig: LegalEntityConfig,

    // Localization Components
    val localeConfig: LocaleConfig,
    val addressFormat: AddressFormat,
    val dateFormats: DateFormats,
    val numberFormats: NumberFormats,

    // Compliance Rules
    val dataRetentionRules: DataRetentionRules,
    val auditFileFormats: List<AuditFileFormat>, // SAF-T, GDPdU, etc.
    val fiscalYearRules: FiscalYearRules,

    // Metadata
    val maintainer: String, // ChiroERP, Partner, or Customer
    val certifications: List<Certification>, // e.g., "Certified for German GoBD"
    val releaseNotes: String
)

enum class CountryPackStatus {
    DRAFT, ACTIVE, DEPRECATED, SUPERSEDED
}
```

---

## Component Deep Dive

### 2. Tax Rules (Country-Specific)

#### Tax Jurisdiction Model
```kotlin
data class TaxRuleSet(
    val jurisdictionRules: List<JurisdictionRule>,
    val taxTypes: List<TaxType>,
    val exemptionRules: List<ExemptionRule>,
    val reverseChargeRules: List<ReverseChargeRule>
)

// Example: Germany VAT
data class JurisdictionRule(
    val jurisdictionId: String, // "DE-VAT"
    val jurisdictionName: String, // "Germany VAT"
    val taxType: TaxType, // VAT
    val taxAuthority: String, // "Bundeszentralamt für Steuern"
    val registrationRequired: Boolean,
    val rates: List<TaxRate>,
    val filingFrequency: FilingFrequency, // MONTHLY, QUARTERLY
    val returnFormat: String // "ELSTER-XML"
)

data class TaxRate(
    val rateId: String, // "DE-VAT-STANDARD"
    val rateName: String, // "Standard Rate"
    val percentage: BigDecimal, // 19%
    val applicableFrom: LocalDate,
    val applicableTo: LocalDate?,
    val applicableTo: List<ProductCategory>?, // null = all products
    val conditions: List<TaxCondition>
)

// Example: India GST Reverse Charge
data class ReverseChargeRule(
    val ruleId: String,
    val description: String,
    val applicableWhen: String, // "Service received from unregistered vendor"
    val buyerLiability: Boolean, // true = buyer pays tax
    val gstType: String // "IGST", "CGST+SGST"
)
```

#### Tax Determination Integration
```kotlin
// Finance Tax module calls localization service
@ApplicationScoped
class TaxDeterminationService {

    @Inject
    lateinit var countryPackRepository: CountryPackRepository

    fun determineTax(context: TaxContext): TaxResult {
        val countryPack = countryPackRepository.getActiveCountryPack(
            context.billingAddress.countryCode
        )

        val applicableRules = countryPack.taxRules.jurisdictionRules.filter {
            it.applicableTo(context)
        }

        // Evaluate tax rates (standard, reduced, exempt)
        return TaxCalculationEngine.calculate(applicableRules, context)
    }
}
```

---

### 3. E-Invoicing Support

#### E-Invoicing Configuration
```kotlin
data class EInvoicingConfig(
    val mandatoryFrom: LocalDate?, // null = optional
    val mandatoryThreshold: Money?, // null = all transactions
    val formats: List<EInvoiceFormat>,
    val clearanceRequired: Boolean, // Italy, Saudi Arabia
    val clearanceEndpoint: String?,
    val digitalSignatureRequired: Boolean,
    val certificationAuthority: String?
)

data class EInvoiceFormat(
    val formatId: String, // "PEPPOL-BIS-3.0", "FatturaPA-1.2.2"
    val formatName: String,
    val schemaLocation: String, // URL to XSD
    val transformationService: String, // Service to convert JournalEntry → XML
    val validationRules: List<ValidationRule>
)
```

#### Country-Specific Examples

##### Italy (FatturaPA)
```yaml
country: IT
eInvoicing:
  mandatoryFrom: 2019-01-01
  mandatoryThreshold: null # all B2B transactions
  formats:
    - formatId: FatturaPA-1.2.2
      formatName: Fattura Elettronica
      clearanceRequired: true
      clearanceEndpoint: https://sdi.fatturapa.gov.it
      digitalSignatureRequired: true
      certificationAuthority: AgID
```

##### PEPPOL (EU)
```yaml
country: EU
eInvoicing:
  mandatoryFrom: null # optional, but encouraged
  formats:
    - formatId: PEPPOL-BIS-3.0
      formatName: PEPPOL Business Interoperability Specifications
      clearanceRequired: false
      accessPointRequired: true # Use PEPPOL access point
```

##### India (GST E-Invoice)
```yaml
country: IN
eInvoicing:
  mandatoryFrom: 2020-10-01
  mandatoryThreshold: 500 crores turnover
  formats:
    - formatId: GST-E-INVOICE-1.0
      formatName: GST E-Invoice JSON
      clearanceRequired: true
      clearanceEndpoint: https://einvoice1.gst.gov.in
      irn_generation: required # Invoice Reference Number
```

---

### 4. Chart of Accounts Templates

#### CoA Template Model
```kotlin
data class CoATemplate(
    val templateId: String, // "US-GAAP-SMB", "DE-SKR04", "IN-IndAS"
    val templateName: String,
    val accountingStandard: String, // GAAP, IFRS, IndAS, etc.
    val industryVertical: String?, // Manufacturing, Retail, Services
    val accounts: List<GLAccountTemplate>
)

data class GLAccountTemplate(
    val accountNumber: String, // "1000", "40000"
    val accountName: String, // "Cash", "Revenue"
    val accountType: AccountType, // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    val accountGroup: String, // "Current Assets", "Operating Revenue"
    val balanceSheet: BalanceSheetSection?, // CURRENT_ASSETS, NON_CURRENT_LIABILITIES
    val profitLoss: PLSection?, // OPERATING_REVENUE, COGS
    val taxRelevant: Boolean,
    val reconciliationRequired: Boolean,
    val defaultCurrency: String?
)
```

#### Country-Specific CoA Examples

##### Germany (SKR 04)
```kotlin
val germanCoA = CoATemplate(
    templateId = "DE-SKR04",
    templateName = "Standardkontenrahmen SKR 04",
    accountingStandard = "HGB", // German GAAP
    accounts = listOf(
        GLAccountTemplate("1200", "Bank", ASSET, "Umlaufvermögen"),
        GLAccountTemplate("1600", "Forderungen aus L+L", ASSET, "Forderungen"),
        GLAccountTemplate("4000", "Umsatzerlöse 19% USt", REVENUE, "Betriebliche Erträge"),
        GLAccountTemplate("1776", "Vorsteuer 19%", ASSET, "Sonstige Vermögensgegenstände")
    )
)
```

##### United States (GAAP - Manufacturing)
```kotlin
val usGaapCoA = CoATemplate(
    templateId = "US-GAAP-MFG",
    templateName = "U.S. GAAP - Manufacturing",
    accountingStandard = "US-GAAP",
    industryVertical = "Manufacturing",
    accounts = listOf(
        GLAccountTemplate("1010", "Cash - Operating", ASSET, "Current Assets"),
        GLAccountTemplate("1200", "Accounts Receivable", ASSET, "Current Assets"),
        GLAccountTemplate("1300", "Inventory - Raw Materials", ASSET, "Current Assets"),
        GLAccountTemplate("5000", "Cost of Goods Sold", EXPENSE, "COGS")
    )
)
```

---

### 5. Banking Formats

#### Banking Format Model
```kotlin
data class BankingFormat(
    val formatId: String, // "SEPA-XML", "ACH-NACHA", "MT101"
    val formatName: String,
    val formatType: BankingFormatType, // PAYMENT_FILE, BANK_STATEMENT
    val fileExtension: String, // ".xml", ".txt"
    val generatorService: String, // "SEPAPaymentGenerator"
    val parserService: String?, // For bank statement import
    val validationRules: List<ValidationRule>
)

enum class BankingFormatType {
    PAYMENT_FILE, // Outgoing payments
    BANK_STATEMENT, // Incoming MT940, CAMT.053
    DIRECT_DEBIT // SEPA Direct Debit
}
```

#### Country-Specific Formats

##### SEPA (EU)
```yaml
country: EU
bankingFormats:
  - formatId: SEPA-CREDIT-TRANSFER-XML
    formatName: SEPA Credit Transfer (pain.001.001.03)
    formatType: PAYMENT_FILE
    fileExtension: .xml
    schema: ISO 20022
    generatorService: SEPAPaymentGenerator
```

##### ACH (United States)
```yaml
country: US
bankingFormats:
  - formatId: ACH-NACHA
    formatName: ACH NACHA Format
    formatType: PAYMENT_FILE
    fileExtension: .ach
    generatorService: ACHPaymentGenerator
```

##### India (NEFT/RTGS)
```yaml
country: IN
bankingFormats:
  - formatId: NEFT-H2H
    formatName: NEFT Host-to-Host
    formatType: PAYMENT_FILE
    generatorService: NEFTPaymentGenerator
```

---

### 6. Statutory Reporting

#### Statutory Report Template Model
```kotlin
data class StatutoryReportTemplate(
    val reportId: String, // "DE-VAT-RETURN", "IN-GSTR-3B"
    val reportName: String,
    val reportType: StatutoryReportType,
    val filingFrequency: FilingFrequency,
    val filingDeadline: FilingDeadline, // "20th of following month"
    val outputFormat: String, // "XML", "PDF", "JSON"
    val dataMapping: List<DataMapping>, // GL accounts → report line items
    val submissionMethod: SubmissionMethod // ONLINE, UPLOAD, PAPER
)

enum class StatutoryReportType {
    VAT_RETURN, GST_RETURN, AUDIT_FILE, TAX_DECLARATION, FINANCIAL_STATEMENT
}

enum class FilingFrequency {
    MONTHLY, QUARTERLY, ANNUALLY
}
```

#### Country-Specific Examples

##### Germany (VAT Return - UStVA)
```yaml
reportId: DE-VAT-RETURN
reportName: Umsatzsteuer-Voranmeldung (UStVA)
reportType: VAT_RETURN
filingFrequency: MONTHLY
filingDeadline: 10th of following month
outputFormat: ELSTER-XML
submissionMethod: ONLINE # via ELSTER portal
dataMapping:
  - line: "81" # Taxable sales 19%
    glAccounts: ["4000", "4010"]
  - line: "86" # Input VAT 19%
    glAccounts: ["1576", "1776"]
```

##### India (GST Return - GSTR-3B)
```yaml
reportId: IN-GSTR-3B
reportName: GSTR-3B Monthly Summary Return
reportType: GST_RETURN
filingFrequency: MONTHLY
filingDeadline: 20th of following month
outputFormat: JSON
submissionMethod: ONLINE # via GST portal
dataMapping:
  - line: "3.1(a)" # Outward taxable supplies
    glAccounts: ["3000-series"]
  - line: "4(A)(5)" # Input Tax Credit
    glAccounts: ["1700-series"]
```

---

### 7. Audit File Formats (GDPdU, SAF-T)

#### Audit File Model
```kotlin
data class AuditFileFormat(
    val formatId: String, // "SAF-T-PT", "GDPdU-DE"
    val formatName: String,
    val standardVersion: String,
    val mandatoryFrom: LocalDate?,
    val scope: AuditFileScope, // ACCOUNTING, INVENTORY, PAYROLL
    val fileStructure: FileStructure, // XML, CSV-ZIP
    val retentionPeriod: Period // 10 years, 7 years, etc.
)

enum class AuditFileScope {
    ACCOUNTING, // GL, AP, AR
    INVENTORY,  // Stock movements
    PAYROLL,
    ALL
}
```

#### Country Examples

##### Portugal (SAF-T PT)
```yaml
formatId: SAF-T-PT
formatName: Standard Audit File for Tax (Portugal)
standardVersion: 1.04_01
mandatoryFrom: 2013-01-01
scope: ACCOUNTING
fileStructure: XML
retentionPeriod: 10 years
```

##### Germany (GDPdU)
```yaml
formatId: GDPdU-DE
formatName: Grundsätze zum Datenzugriff und zur Prüfbarkeit digitaler Unterlagen
mandatoryFrom: 2002-01-01
scope: ALL
fileStructure: CSV-ZIP # with index.xml
retentionPeriod: 10 years
```

---

## Locale Configuration

### Locale Model
```kotlin
data class LocaleConfig(
    val defaultLanguage: String, // ISO 639-1 (en, de, pt, hi)
    val defaultCurrency: String, // ISO 4217 (USD, EUR, INR, BRL)
    val defaultTimezone: String, // IANA (America/New_York, Europe/Berlin)
    val weekStartDay: DayOfWeek, // MONDAY (ISO 8601) vs SUNDAY (US)
    val measurementSystem: MeasurementSystem // METRIC, IMPERIAL
)

data class DateFormats(
    val shortDateFormat: String, // "MM/dd/yyyy" (US), "dd.MM.yyyy" (DE)
    val longDateFormat: String,
    val timeFormat: String // "h:mm a" (US), "HH:mm" (DE)
)

data class NumberFormats(
    val decimalSeparator: String, // "." (US), "," (DE/BR)
    val thousandsSeparator: String, // "," (US), "." (DE)
    val currencyFormat: String // "$1,000.00" (US), "1.000,00 €" (DE)
)

data class AddressFormat(
    val addressLines: Int, // 2-3 lines typical
    val postalCodeFormat: String, // Regex: "^[0-9]{5}$" (US), "^[0-9]{5}$" (DE)
    val postalCodePosition: PostalCodePosition, // BEFORE_CITY, AFTER_CITY
    val stateRequired: Boolean // true (US), false (DE)
)
```

---

## Legal Entity Configuration

### Legal Entity Model
```kotlin
data class LegalEntityConfig(
    val legalForms: List<LegalForm>, // GmbH, AG (DE), LLC, Corp (US)
    val registrationNumberFormats: Map<String, String>, // VAT, EIN, etc.
    val requiredRegistrations: List<RegistrationType>
)

data class LegalForm(
    val code: String, // "DE-GMBH"
    val name: String, // "Gesellschaft mit beschränkter Haftung"
    val abbreviation: String, // "GmbH"
    val minShareCapital: Money?, // €25,000 for GmbH
    val liabilityType: LiabilityType // LIMITED, UNLIMITED
)

enum class RegistrationType {
    TAX_ID, VAT_ID, TRADE_REGISTER, STATISTICAL_CODE
}
```

---

## Data Retention & Compliance

### Data Retention Rules
```kotlin
data class DataRetentionRules(
    val accountingRecords: Period, // 10 years (Germany), 7 years (US)
    val invoices: Period,
    val contracts: Period,
    val employeeRecords: Period,
    val gdprApplicable: Boolean, // EU = true
    val rightToErasure: Boolean // GDPR = true
)
```

---

## Country Pack Deployment

### Installation Process
```kotlin
@Path("/admin/country-packs")
class CountryPackAdminResource {

    @Inject
    lateinit var countryPackService: CountryPackService

    @POST
    @Path("/install")
    fun installCountryPack(
        @Valid request: InstallCountryPackRequest
    ): Response {
        // Download country pack from ChiroERP marketplace
        val countryPack = countryPackService.downloadCountryPack(
            countryCode = request.countryCode,
            version = request.version
        )

        // Validate compatibility
        countryPackService.validateCompatibility(countryPack)

        // Install (creates tax rules, CoA templates, etc.)
        countryPackService.install(countryPack)

        return Response.ok().build()
    }

    @GET
    @Path("/available")
    fun getAvailableCountryPacks(): List<CountryPackMetadata> {
        return countryPackService.listAvailableCountryPacks()
    }
}
```

### Tenant Activation
```kotlin
// After installing country pack, tenants activate it
@POST
@Path("/tenants/{tenantId}/activate-country-pack")
fun activateCountryPack(
    @PathParam("tenantId") tenantId: TenantId,
    @Valid request: ActivateCountryPackRequest
): Response {
    // Associate tenant with country pack
    tenantService.activateCountryPack(tenantId, request.countryCode)

    // Initialize CoA from template (if new tenant)
    if (request.initializeCoA) {
        financeService.initializeCoA(tenantId, request.coaTemplateId)
    }

    return Response.ok().build()
}
```

---

## Integration Points

### Finance Domain Integration
```kotlin
// Finance GL uses CoA template
@ApplicationScoped
class GLAccountService {

    fun initializeCoA(tenantId: TenantId, templateId: String) {
        val countryPack = countryPackRepository.getActiveCountryPack(
            tenantId.defaultCountry
        )

        val template = countryPack.chartOfAccountsTemplate

        template.accounts.forEach { accountTemplate ->
            glAccountRepository.save(
                GLAccount(
                    tenantId = tenantId,
                    accountNumber = accountTemplate.accountNumber,
                    accountName = accountTemplate.accountName,
                    accountType = accountTemplate.accountType,
                    // ... copy all fields
                )
            )
        }
    }
}
```

### Tax Domain Integration
```kotlin
// Finance Tax uses tax rules from country pack
@ApplicationScoped
class TaxCalculationService {

    fun calculateTax(invoiceId: InvoiceId): TaxResult {
        val invoice = invoiceRepository.findById(invoiceId)
        val countryPack = countryPackRepository.getActiveCountryPack(
            invoice.billingAddress.countryCode
        )

        // Use country pack tax rules
        return TaxEngine.calculate(
            taxRules = countryPack.taxRules,
            context = TaxContext(invoice)
        )
    }
}
```

### Treasury Integration
```kotlin
// Treasury uses banking formats from country pack
@ApplicationScoped
class PaymentFileGeneratorService {

    fun generatePaymentFile(paymentBatch: PaymentBatch): ByteArray {
        val countryPack = countryPackRepository.getActiveCountryPack(
            paymentBatch.companyCode.country
        )

        val format = countryPack.bankingFormats.first {
            it.formatType == PAYMENT_FILE && it.formatId == paymentBatch.requestedFormat
        }

        // Delegate to format-specific generator
        return when (format.formatId) {
            "SEPA-XML" -> sepaGenerator.generate(paymentBatch)
            "ACH-NACHA" -> achGenerator.generate(paymentBatch)
            else -> throw UnsupportedFormatException(format.formatId)
        }
    }
}
```

---

## Country Pack Versioning

### Semantic Versioning
```
Version Format: MAJOR.MINOR.PATCH

MAJOR: Breaking changes (e.g., new mandatory tax rule)
MINOR: New features (e.g., new audit file format support)
PATCH: Bug fixes (e.g., corrected tax rate)

Example: DE Country Pack 2.1.3
- 2.x.x: Supports German VAT reform 2020
- x.1.x: Added GoBD-compliant audit export
- x.x.3: Fixed SKR04 account 1776 mapping
```

### Upgrade Process
```kotlin
@ApplicationScoped
class CountryPackUpgradeService {

    fun upgrade(countryCode: String, toVersion: SemanticVersion) {
        val currentPack = countryPackRepository.getActiveCountryPack(countryCode)
        val newPack = countryPackRepository.findByVersion(countryCode, toVersion)

        // Run migration scripts
        val migrations = newPack.getMigrations(fromVersion = currentPack.version)
        migrations.forEach { migration ->
            migration.execute()
        }

        // Mark old pack as superseded
        currentPack.status = SUPERSEDED
        newPack.status = ACTIVE

        countryPackRepository.save(currentPack)
        countryPackRepository.save(newPack)
    }
}
```

---

## Performance Standards

### Country Pack Caching
```kotlin
@ApplicationScoped
class CountryPackCacheService {

    @CacheResult(cacheName = "country-packs")
    fun getCountryPack(countryCode: String): CountryPack {
        return countryPackRepository.getActiveCountryPack(countryCode)
    }
}

// Cache configuration
quarkus.cache.caffeine.country-packs.maximum-size=50
quarkus.cache.caffeine.country-packs.expire-after-write=1h
```

### SLOs

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Tax Rule Lookup | < 20ms p95 | > 50ms |
| CoA Template Load | < 100ms | > 200ms |
| Payment File Generation | < 500ms | > 1s |
| Country Pack Activation | < 5s | > 10s |

---

## Alternatives Considered

### 1. Hardcoded Regulatory Logic in Core Domains
**Pros**: Simple initially
**Cons**: High cost of change; difficult versioning/certification; poor scalability across jurisdictions
**Decision**: Rejected in favor of versioned country packs

### 2. Partner Localization (SAP LSP Model)
**Pros**: Outsource complexity to local partners
**Cons**: Quality inconsistency, dependency on partners
**Decision**: Deferred—can be added later for niche markets

### 3. Multi-Instance (Separate Deployment per Country)
**Pros**: Total isolation
**Cons**: High operational overhead; fragments global reporting and cross-company processes
**Decision**: Rejected—violates multi-tenancy architecture (ADR-005)

### 4. Runtime Customization Only (ADR-012)
**Pros**: Flexible
**Cons**: Customers shouldn't need to know tax laws
**Decision**: Rejected—regulatory compliance is our responsibility

---

## Consequences

### Positive
- ✅ **Global Scale**: Support 50+ countries with data-driven packs
- ✅ **Regulatory Compliance**: Tax, e-invoicing, audit files built-in
- ✅ **Faster Go-to-Market**: New country = new country pack (no code changes)
- ✅ **Certifications**: Country packs can be independently certified (e.g., "GoBD-compliant")
- ✅ **Partner Ecosystem**: Partners can build/maintain country packs

### Negative
- ❌ **Complexity**: Country pack model is complex to build/maintain
- ❌ **Testing Burden**: Must test each country pack separately
- ❌ **Version Management**: Upgrades can break tenant configurations
- ❌ **Initial Effort**: Building first 10 country packs is significant work

### Neutral
- Country pack marketplace (free vs paid packs) TBD
- Who maintains country packs (ChiroERP vs partners) TBD
- Certification process (how to validate country pack correctness) TBD

---

## Compliance

### Data Residency
- Country packs do NOT handle data residency (separate concern—ADR-005 Multi-Tenancy)
- GDPR compliance: Country pack specifies retention rules, but enforcement is system-wide

### Audit Trail
- Country pack activation/upgrade logged
- Tax rule evaluation logged (for audit defense)

---

## Implementation Plan

### Phase 1: Foundation (Months 1-2)
- ✅ Country Pack domain model
- ✅ Tax rule engine integration
- ✅ CoA template system
- ✅ Country pack repository and cache

### Phase 2: Core Country Packs (Months 3-4)
- ✅ United States (GAAP, ACH, Sales Tax)
- ✅ Germany (HGB, SKR04, SEPA, VAT, GDPdU)
- ✅ United Kingdom (FRS 102, BACS, VAT, MTD)

### Phase 3: E-Invoicing (Months 5-6)
- ✅ PEPPOL integration (EU)
- ✅ FatturaPA (Italy)
- ✅ India GST E-Invoice

### Phase 4: Expansion (Months 7-12)
- ✅ Additional country packs (France, Spain, Netherlands, India, Brazil, Mexico)
- ✅ Partner enablement (country pack SDK)
- ✅ Certification process

---

## References

### Related ADRs
- ADR-030: Tax Engine & Compliance (country pack provides tax rules)
- ADR-044: Configuration & Rules Framework (tax determination logic)
- ADR-009: Financial Accounting Domain (CoA templates)
- ADR-026: Treasury & Cash Management (banking formats)

### Technology References
- [PEPPOL](https://peppol.org/)
- [SAF-T (OECD)](https://www.oecd.org/tax/forum-on-tax-administration/publications-and-products/technologies/46263843.pdf)
- [FatturaPA (Italy)](https://www.fatturapa.gov.it/)
- [India GST E-Invoice](https://einvoice1.gst.gov.in/)
- [ISO 20022](https://www.iso20022.org/)

### Industry References
- SAP Localization (Country Versions)
- Oracle Global Ledger Infrastructure (GLI)
- Microsoft Dynamics 365 Regulatory Services
- Odoo Localization Modules

---

**Note**: This ADR defines the country-pack approach for delivering regulatory content (tax, e-invoicing, statutory reports, banking formats) as versioned data. Implementation sequencing typically follows the configuration and org foundations (ADR-044/045) and workflow/tasking (ADR-046).
