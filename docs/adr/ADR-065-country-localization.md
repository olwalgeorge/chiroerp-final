# ADR-065: Country-Specific Localization (15+ Countries)

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Localization Team, Tax Team  
**Priority**: P1 (High - Global Expansion)  
**Tier**: Platform Core  
**Tags**: localization, internationalization, tax, compliance, regional, country-packs

---

## Context

**Problem**: ChiroERP currently supports **only 3 countries** (US, Germany, India) with **basic localization**. This severely limits international expansion:

- **Market limitation**: 65% of prospects operate in 3+ countries
- **Lost deals**: 20-25% of international pipeline lost due to localization gaps
- **Manual workarounds**: Customers configure country-specific rules manually (error-prone)
- **Tax complexity**: Each country has unique tax rules (VAT, GST, sales tax)
- **E-invoicing mandates**: 40+ countries require electronic invoicing formats
- **Statutory reporting**: Country-specific reports (Intrastat, SPED, STP, MTD)

**Current Localization Coverage**:

| Country | Tax Engine | E-Invoicing | Statutory Reports | Chart of Accounts | Status |
|---------|------------|-------------|-------------------|-------------------|--------|
| 🇺🇸 United States | Basic sales tax | ❌ No | ❌ No | ✅ US GAAP | Partial |
| 🇩🇪 Germany | Basic VAT | ❌ No | ❌ Intrastat manual | ✅ SKR03/04 | Partial |
| 🇮🇳 India | Basic GST | ❌ No | ❌ GSTR manual | ✅ Indian | Partial |
| Others | ❌ Not supported | ❌ No | ❌ No | ⚠️ Generic | Not supported |

**Competitive Reality**:

| ERP System | Countries Supported | E-Invoicing | Statutory Reporting | Localization Depth |
|------------|---------------------|-------------|---------------------|-------------------|
| **SAP** | 190+ | 40+ countries | 100+ reports | Deep (full compliance) |
| **Oracle** | 200+ | 45+ countries | 120+ reports | Deep (full compliance) |
| **Dynamics 365** | 100+ | 35+ countries | 80+ reports | Medium-Deep |
| **NetSuite** | 100+ | 30+ countries | 70+ reports | Medium |
| **ChiroERP** | **3** | **0** | **0** | **Shallow (basic only)** |

**Regional Requirements** (Top 15 Priority Countries):

### North America
- 🇺🇸 **United States**: Sales tax (50 states, 10K+ jurisdictions), 1099 reporting, W-2 forms, Nexus rules
- 🇨🇦 **Canada**: GST/HST/PST/QST (5 tax types), T4/T5 slips, CRA reporting, bilingual (EN/FR)
- 🇲🇽 **Mexico**: CFDI 4.0 e-invoicing (mandatory), SAT reporting, ISR/IVA taxes

### Europe (EU)
- 🇬🇧 **United Kingdom**: MTD (Making Tax Digital) mandatory, VAT returns, CIS, PAYE
- 🇩🇪 **Germany**: E-Rechnung (B2G mandatory 2025), Intrastat, DATEV export, GoBD compliance
- 🇫🇷 **France**: Chorus Pro (B2G mandatory), FEC export, TVA, Intrastat
- 🇪🇸 **Spain**: SII (Suministro Inmediato de Información), Batuz (Basque), TicketBAI
- 🇮🇹 **Italy**: Fattura Elettronica (SDI), esterometro

### Latin America
- 🇧🇷 **Brazil**: NF-e/NFC-e/NFS-e, SPED Fiscal/Contábil/Contribuições, ICMS/IPI/PIS/COFINS

### Asia-Pacific
- 🇦🇺 **Australia**: BAS (GST returns), STP (Single Touch Payroll), ATO integration
- 🇸🇬 **Singapore**: GST F5/F7 returns, IRAS submission, GIRO payments
- 🇯🇵 **Japan**: JCT (Japanese Consumption Tax), Qualified Invoice (2023), e-Tax
- 🇮🇳 **India**: Enhanced GST (GSTR-1/3B/9), E-Way Bill, E-Invoice (IRN)

### Middle East
- 🇦🇪 **UAE**: VAT (5%), Excise Tax, FAF integration, Arabic support
- 🇸🇦 **Saudi Arabia**: ZATCA e-invoicing Phase 2, VAT (15%), Zakat

**E-Invoicing Mandates** (Critical Compliance):

| Country | E-Invoice Format | Mandate | Penalty for Non-Compliance |
|---------|------------------|---------|---------------------------|
| 🇧🇷 Brazil | NF-e (XML) | Mandatory | Cannot sell without NF-e |
| 🇲🇽 Mexico | CFDI 4.0 (XML) | Mandatory | 5-10% revenue fine |
| 🇮🇹 Italy | FatturaPA (XML) | Mandatory B2G/B2B/B2C | €500-50K fine |
| 🇪🇸 Spain | Facturae (XML) | Mandatory B2G | €150-6K fine |
| 🇫🇷 France | Chorus Pro | Mandatory B2G | Contract rejection |
| 🇮🇳 India | IRN (JSON) | Mandatory >₹5cr | ₹10K fine per invoice |
| 🇸🇦 Saudi | ZATCA (XML/JSON) | Phase 2 2024 | SAR 50K fine |

**Customer Quote** (CFO, Global Manufacturing):
> "We have operations in 12 countries. Your system works for US, but we need Brazil NF-e, Mexico CFDI, Spain SII, and UK MTD. Without these, we can't expand ChiroERP beyond US subsidiaries."

---

## Decision

Build a **Country-Specific Localization Framework** with **15+ country packs** providing:

1. **Country Packs** (plug-and-play localization modules)
   - Tax configuration (rates, rules, exemptions)
   - E-invoicing formats (XML generation, validation)
   - Statutory reports (pre-built templates)
   - Chart of Accounts templates (country-specific)
   - Language translations

2. **Tax Compliance** (integrated with ADR-062)
   - VAT/GST/Sales Tax automation
   - Withholding tax (WHT)
   - Reverse charge mechanisms
   - Cross-border tax rules

3. **E-Invoicing Engine**
   - XML/JSON generation (Peppol, UBL, country formats)
   - Digital signatures (PKI, HSM)
   - Government portal integration (API, web service)
   - Real-time validation

4. **Statutory Reporting**
   - Intrastat (EU trade reporting)
   - VAT returns (MTD, SII, MOSS)
   - E-filing integration (government portals)

5. **Regional Compliance**
   - Data residency (EU, China, Russia)
   - Audit trails (GoBD, SOX, local GAAP)
   - Language support (RTL for Arabic)

**Target**: Q1-Q4 2027 (phased rollout, 15 countries)

---

## Architecture

### 1. Localization Framework

**New Structure**: `localization/` (country-specific modules)

```
localization/
├── common-localization/          # Shared localization infrastructure
│   ├── CountryPackLoader.kt     # Load country packs dynamically
│   ├── TaxEngine.kt              # Integrate with ADR-062
│   ├── EInvoiceEngine.kt         # E-invoice generation
│   ├── StatutoryReportEngine.kt  # Report generation
│   └── ComplianceValidator.kt    # Validate compliance rules
├── north-america/
│   ├── united-states/
│   │   ├── USTaxProvider.kt      # US sales tax (50 states)
│   │   ├── US1099Reporter.kt     # 1099 reporting
│   │   └── config/
│   │       ├── us-tax-rates.yaml
│   │       ├── us-coa-gaap.yaml
│   │       └── us-validations.yaml
│   ├── canada/
│   │   ├── CANTaxProvider.kt     # GST/HST/PST/QST
│   │   ├── CANT4Reporter.kt      # T4/T5 slips
│   │   └── config/
│   └── mexico/
│       ├── MXTaxProvider.kt      # ISR/IVA
│       ├── MXCFDIEngine.kt       # CFDI 4.0 e-invoicing
│       └── config/
├── europe/
│   ├── united-kingdom/
│   │   ├── UKTaxProvider.kt      # VAT, CIS
│   │   ├── UKMTDIntegration.kt   # Making Tax Digital API
│   │   └── config/
│   ├── germany/
│   │   ├── DETaxProvider.kt      # USt (VAT)
│   │   ├── DEIntrastatReporter.kt
│   │   ├── DEDATEVExporter.kt    # DATEV export
│   │   └── config/
│   ├── france/
│   │   ├── FRTaxProvider.kt      # TVA
│   │   ├── FRChorusProIntegration.kt
│   │   └── config/
│   ├── spain/
│   │   ├── ESTaxProvider.kt      # IVA
│   │   ├── ESSIIIntegration.kt   # SII real-time reporting
│   │   └── config/
│   └── italy/
│       ├── ITTaxProvider.kt      # IVA
│       ├── ITSDIIntegration.kt   # Sistema di Interscambio
│       └── config/
├── latin-america/
│   └── brazil/
│       ├── BRTaxProvider.kt      # ICMS/IPI/PIS/COFINS
│       ├── BRNFeEngine.kt        # NF-e generation
│       ├── BRSPEDExporter.kt     # SPED Fiscal
│       └── config/
├── asia-pacific/
│   ├── australia/
│   │   ├── AUTaxProvider.kt      # GST
│   │   ├── AUSTPIntegration.kt   # Single Touch Payroll
│   │   └── config/
│   ├── singapore/
│   │   ├── SGTaxProvider.kt      # GST
│   │   ├── SGIRASIntegration.kt  # IRAS submission
│   │   └── config/
│   ├── japan/
│   │   ├── JPTaxProvider.kt      # JCT (Consumption Tax)
│   │   ├── JPQualifiedInvoice.kt # Qualified Invoice 2023
│   │   └── config/
│   └── india/
│       ├── INTaxProvider.kt      # GST (enhanced)
│       ├── INEWayBillEngine.kt   # E-Way Bill
│       ├── INEInvoiceEngine.kt   # E-Invoice (IRN)
│       └── config/
└── middle-east/
    ├── uae/
    │   ├── AETaxProvider.kt      # VAT
    │   ├── AEFAFIntegration.kt   # Federal Tax Authority
    │   └── config/
    └── saudi-arabia/
        ├── SATaxProvider.kt      # VAT, Zakat
        ├── SAZATCAIntegration.kt # ZATCA e-invoicing
        └── config/
```

---

### 2. Country Pack Framework

**Dynamic Country Pack Loading**:

```kotlin
/**
 * Country Pack Loader
 * Dynamically loads and activates country-specific localization
 */
@Service
class CountryPackLoader(
    private val countryPackRegistry: CountryPackRegistry,
    private val taxEngine: TaxEngine,
    private val eInvoiceEngine: EInvoiceEngine
) {
    
    /**
     * Load country pack for tenant
     */
    suspend fun loadCountryPack(
        tenantId: UUID,
        countryCode: String
    ): CountryPack {
        logger.info("Loading country pack for $countryCode")
        
        // Step 1: Find country pack
        val countryPack = countryPackRegistry.findByCode(countryCode)
            ?: throw NotFoundException("Country pack not found: $countryCode")
        
        // Step 2: Validate compatibility
        if (!countryPack.isCompatible(currentPlatformVersion)) {
            throw IncompatibleVersionException(
                "Country pack $countryCode requires platform version ${countryPack.minPlatformVersion}"
            )
        }
        
        // Step 3: Load configuration
        val config = loadCountryConfiguration(countryPack)
        
        // Step 4: Register tax provider
        taxEngine.registerProvider(
            countryCode = countryCode,
            provider = countryPack.taxProvider
        )
        
        // Step 5: Register e-invoicing engine
        if (countryPack.eInvoiceEngine != null) {
            eInvoiceEngine.registerEngine(
                countryCode = countryCode,
                engine = countryPack.eInvoiceEngine
            )
        }
        
        // Step 6: Load Chart of Accounts template
        if (countryPack.coaTemplate != null) {
            loadCOATemplate(tenantId, countryPack.coaTemplate)
        }
        
        // Step 7: Load statutory report templates
        loadStatutoryReports(tenantId, countryPack.statutoryReports)
        
        // Step 8: Activate country pack for tenant
        tenantCountryPackRepository.save(
            TenantCountryPack(
                tenantId = tenantId,
                countryCode = countryCode,
                version = countryPack.version,
                activatedAt = Instant.now(),
                status = CountryPackStatus.ACTIVE
            )
        )
        
        logger.info("Country pack $countryCode activated for tenant $tenantId")
        
        return countryPack
    }
    
    /**
     * Load country configuration from YAML
     */
    private fun loadCountryConfiguration(countryPack: CountryPack): CountryConfiguration {
        val configPath = "localization/${countryPack.region}/${countryPack.countryCode}/config/"
        
        return CountryConfiguration(
            taxRates = loadYaml("${configPath}tax-rates.yaml"),
            taxRules = loadYaml("${configPath}tax-rules.yaml"),
            validations = loadYaml("${configPath}validations.yaml"),
            chartOfAccounts = loadYaml("${configPath}coa.yaml"),
            statutoryReports = loadYaml("${configPath}statutory-reports.yaml")
        )
    }
}

data class CountryPack(
    val countryCode: String,        // ISO 3166-1 alpha-2 (US, GB, DE, etc.)
    val countryName: String,
    val region: String,              // north-america, europe, asia-pacific, etc.
    val version: String,
    val minPlatformVersion: String,
    val taxProvider: TaxProvider,
    val eInvoiceEngine: EInvoiceEngine?,
    val statutoryReports: List<StatutoryReport>,
    val coaTemplate: ChartOfAccountsTemplate?,
    val supportedLanguages: List<String>,
    val dataResidencyRequired: Boolean = false
)

enum class CountryPackStatus {
    ACTIVE,
    INACTIVE,
    DEPRECATED,
    BETA
}

data class TenantCountryPack(
    val tenantId: UUID,
    val countryCode: String,
    val version: String,
    val activatedAt: Instant,
    val status: CountryPackStatus
)
```

---

### 3. E-Invoicing Engine

**Multi-Format E-Invoice Generation**:

```kotlin
/**
 * E-Invoice Engine
 * Generates electronic invoices in country-specific formats
 */
@Service
class EInvoiceEngine(
    private val xmlGenerator: XMLGenerator,
    private val digitalSigner: DigitalSigner,
    private val governmentPortal: GovernmentPortalIntegration
) {
    
    private val engines = mutableMapOf<String, CountryEInvoiceEngine>()
    
    /**
     * Register country-specific e-invoice engine
     */
    fun registerEngine(countryCode: String, engine: CountryEInvoiceEngine) {
        engines[countryCode] = engine
    }
    
    /**
     * Generate e-invoice for country
     */
    suspend fun generateEInvoice(
        invoice: Invoice,
        countryCode: String
    ): EInvoice {
        val engine = engines[countryCode]
            ?: throw UnsupportedCountryException("E-invoicing not supported for $countryCode")
        
        logger.info("Generating e-invoice for invoice ${invoice.id} (country: $countryCode)")
        
        // Step 1: Validate invoice data (country-specific rules)
        val validationResult = engine.validate(invoice)
        if (!validationResult.isValid) {
            throw ValidationException("Invoice validation failed: ${validationResult.errors}")
        }
        
        // Step 2: Generate XML/JSON (country-specific format)
        val document = engine.generateDocument(invoice)
        
        // Step 3: Digital signature (if required)
        val signedDocument = if (engine.requiresDigitalSignature) {
            digitalSigner.sign(
                document = document,
                certificate = getCertificate(invoice.tenantId, countryCode)
            )
        } else {
            document
        }
        
        // Step 4: Submit to government portal (if required)
        val submissionResult = if (engine.requiresGovernmentSubmission) {
            governmentPortal.submit(
                countryCode = countryCode,
                document = signedDocument,
                tenantId = invoice.tenantId
            )
        } else {
            null
        }
        
        // Step 5: Create e-invoice record
        val eInvoice = EInvoice(
            id = UUID.randomUUID(),
            invoiceId = invoice.id,
            tenantId = invoice.tenantId,
            countryCode = countryCode,
            format = engine.format,
            document = signedDocument,
            governmentId = submissionResult?.governmentId,
            status = if (submissionResult?.isSuccess == true) {
                EInvoiceStatus.SUBMITTED
            } else {
                EInvoiceStatus.GENERATED
            },
            generatedAt = Instant.now(),
            submittedAt = submissionResult?.submittedAt
        )
        
        eInvoiceRepository.save(eInvoice)
        
        logger.info("E-invoice generated: ${eInvoice.id}, government ID: ${eInvoice.governmentId}")
        
        return eInvoice
    }
}

interface CountryEInvoiceEngine {
    val countryCode: String
    val format: EInvoiceFormat
    val requiresDigitalSignature: Boolean
    val requiresGovernmentSubmission: Boolean
    
    fun validate(invoice: Invoice): ValidationResult
    fun generateDocument(invoice: Invoice): String
}

enum class EInvoiceFormat {
    UBL_2_1,           // Universal Business Language (Peppol)
    CFDI_4_0,          // Mexico
    NF_E,              // Brazil
    FATTURA_PA,        // Italy
    FACTURAE,          // Spain
    ZATCA_XML,         // Saudi Arabia
    IRN_JSON,          // India
    CUSTOM_XML,
    CUSTOM_JSON
}

data class EInvoice(
    val id: UUID,
    val invoiceId: UUID,
    val tenantId: UUID,
    val countryCode: String,
    val format: EInvoiceFormat,
    val document: String,
    val governmentId: String?,
    var status: EInvoiceStatus,
    val generatedAt: Instant,
    var submittedAt: Instant? = null,
    var approvedAt: Instant? = null,
    var errorMessage: String? = null
)

enum class EInvoiceStatus {
    GENERATED,
    SUBMITTED,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

---

### 4. Country-Specific Implementation Examples

#### 4.1 Brazil (Complex Localization)

```kotlin
/**
 * Brazil NF-e (Nota Fiscal Eletrônica) Engine
 * Most complex e-invoicing in the world
 */
@Service
class BrazilNFeEngine : CountryEInvoiceEngine {
    
    override val countryCode = "BR"
    override val format = EInvoiceFormat.NF_E
    override val requiresDigitalSignature = true
    override val requiresGovernmentSubmission = true
    
    override fun validate(invoice: Invoice): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Brazil-specific validations
        if (invoice.customer.cnpj == null && invoice.customer.cpf == null) {
            errors.add("Customer must have CNPJ (business) or CPF (individual)")
        }
        
        if (invoice.lineItems.any { it.cfop == null }) {
            errors.add("All line items must have CFOP (tax classification)")
        }
        
        if (invoice.lineItems.any { it.ncm == null }) {
            errors.add("All line items must have NCM (product classification)")
        }
        
        // ICMS validation (state tax)
        if (invoice.lineItems.any { it.icmsRate == null }) {
            errors.add("All line items must have ICMS rate")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    override fun generateDocument(invoice: Invoice): String {
        // Generate NF-e XML (version 4.00)
        return xmlGenerator.generate {
            element("nfeProc") {
                attribute("versao", "4.00")
                attribute("xmlns", "http://www.portalfiscal.inf.br/nfe")
                
                element("NFe") {
                    element("infNFe") {
                        attribute("Id", "NFe${invoice.nfeKey}")
                        attribute("versao", "4.00")
                        
                        // Identificação da NF-e
                        element("ide") {
                            element("cUF") { text(invoice.customer.state.ufCode) }
                            element("natOp") { text(invoice.operationType) }
                            element("mod") { text("55") } // Modelo 55 (NF-e)
                            element("serie") { text(invoice.series.toString()) }
                            element("nNF") { text(invoice.number.toString()) }
                            element("dhEmi") { text(invoice.issuedAt.toString()) }
                            element("tpNF") { text(if (invoice.type == InvoiceType.SALE) "1" else "0") }
                            element("idDest") { text(determineIdDest(invoice)) }
                            element("cMunFG") { text(invoice.seller.cityCode) }
                            element("tpImp") { text("1") } // Retrato
                            element("tpEmis") { text("1") } // Normal
                            element("finNFe") { text("1") } // Normal
                        }
                        
                        // Emitente
                        element("emit") {
                            element("CNPJ") { text(invoice.seller.cnpj) }
                            element("xNome") { text(invoice.seller.name) }
                            element("xFant") { text(invoice.seller.tradeName) }
                            element("enderEmit") {
                                element("xLgr") { text(invoice.seller.street) }
                                element("nro") { text(invoice.seller.number) }
                                element("xBairro") { text(invoice.seller.district) }
                                element("cMun") { text(invoice.seller.cityCode) }
                                element("xMun") { text(invoice.seller.city) }
                                element("UF") { text(invoice.seller.state) }
                                element("CEP") { text(invoice.seller.postalCode) }
                            }
                            element("IE") { text(invoice.seller.stateRegistration) }
                            element("CRT") { text(invoice.seller.taxRegime) }
                        }
                        
                        // Destinatário
                        element("dest") {
                            if (invoice.customer.cnpj != null) {
                                element("CNPJ") { text(invoice.customer.cnpj) }
                            } else {
                                element("CPF") { text(invoice.customer.cpf) }
                            }
                            element("xNome") { text(invoice.customer.name) }
                            // ... similar address structure
                        }
                        
                        // Produtos/Serviços
                        invoice.lineItems.forEachIndexed { index, item ->
                            element("det") {
                                attribute("nItem", (index + 1).toString())
                                
                                element("prod") {
                                    element("cProd") { text(item.productCode) }
                                    element("cEAN") { text(item.eanCode ?: "SEM GTIN") }
                                    element("xProd") { text(item.description) }
                                    element("NCM") { text(item.ncm) }
                                    element("CFOP") { text(item.cfop) }
                                    element("uCom") { text(item.unit) }
                                    element("qCom") { text(item.quantity.toString()) }
                                    element("vUnCom") { text(item.unitPrice.toString()) }
                                    element("vProd") { text(item.totalAmount.toString()) }
                                }
                                
                                // Tributos (ICMS, IPI, PIS, COFINS)
                                element("imposto") {
                                    element("ICMS") {
                                        element("ICMS00") { // Tributado integralmente
                                            element("orig") { text(item.origin.toString()) }
                                            element("CST") { text("00") }
                                            element("modBC") { text("0") }
                                            element("vBC") { text(item.icmsBaseCalc.toString()) }
                                            element("pICMS") { text(item.icmsRate.toString()) }
                                            element("vICMS") { text(item.icmsAmount.toString()) }
                                        }
                                    }
                                    
                                    element("IPI") {
                                        element("IPITrib") {
                                            element("CST") { text("50") }
                                            element("vBC") { text(item.ipiBaseCalc.toString()) }
                                            element("pIPI") { text(item.ipiRate.toString()) }
                                            element("vIPI") { text(item.ipiAmount.toString()) }
                                        }
                                    }
                                    
                                    element("PIS") {
                                        element("PISAliq") {
                                            element("CST") { text("01") }
                                            element("vBC") { text(item.pisBaseCalc.toString()) }
                                            element("pPIS") { text("1.65") }
                                            element("vPIS") { text(item.pisAmount.toString()) }
                                        }
                                    }
                                    
                                    element("COFINS") {
                                        element("COFINSAliq") {
                                            element("CST") { text("01") }
                                            element("vBC") { text(item.cofinsBaseCalc.toString()) }
                                            element("pCOFINS") { text("7.6") }
                                            element("vCOFINS") { text(item.cofinsAmount.toString()) }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Totais
                        element("total") {
                            element("ICMSTot") {
                                element("vBC") { text(invoice.icmsBase.toString()) }
                                element("vICMS") { text(invoice.icmsTotal.toString()) }
                                element("vBCST") { text("0.00") }
                                element("vST") { text("0.00") }
                                element("vProd") { text(invoice.subtotal.toString()) }
                                element("vNF") { text(invoice.total.toString()) }
                            }
                        }
                        
                        // Transporte
                        element("transp") {
                            element("modFrete") { text("9") } // Sem frete
                        }
                        
                        // Pagamento
                        element("pag") {
                            element("detPag") {
                                element("tPag") { text(invoice.paymentMethod.code) }
                                element("vPag") { text(invoice.total.toString()) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Brazil SPED Fiscal Exporter
 * EFD-ICMS/IPI (Escrituração Fiscal Digital)
 */
@Service
class BrazilSPEDExporter {
    
    /**
     * Generate SPED Fiscal file (text format, pipe-delimited)
     */
    fun generateSPEDFiscal(
        tenantId: UUID,
        period: YearMonth
    ): String {
        val lines = mutableListOf<String>()
        
        // Registro 0000: Abertura do arquivo
        lines.add("|0000|014|0|${period.year}${period.monthValue.toString().padStart(2, '0')}|...")
        
        // Registro 0001: Abertura do bloco 0
        lines.add("|0001|0|")
        
        // Registro 0005: Dados complementares do contribuinte
        lines.add("|0005|ChiroERP|Rua Exemplo 123|São Paulo|SP|01310-000|...|")
        
        // Registro C100: Documento - Nota Fiscal (código 01, 1B, 04 e 55)
        val invoices = invoiceRepository.findByPeriod(tenantId, period)
        for (invoice in invoices) {
            lines.add("|C100|0|1|${invoice.number}|...")
            
            // Registro C170: Itens do documento
            for (item in invoice.lineItems) {
                lines.add("|C170|${item.number}|${item.description}|...")
            }
        }
        
        // Registro 9999: Encerramento do arquivo
        lines.add("|9999|${lines.size + 1}|")
        
        return lines.joinToString("\n")
    }
}
```

#### 4.2 United Kingdom (MTD - Making Tax Digital)

```kotlin
/**
 * UK Making Tax Digital Integration
 * Mandatory for all VAT-registered businesses (£85K+ turnover)
 */
@Service
class UKMTDIntegration(
    private val hmrcClient: HMRCApiClient
) {
    
    /**
     * Submit VAT return to HMRC
     * Must be submitted within 1 month + 7 days of period end
     */
    suspend fun submitVATReturn(
        tenantId: UUID,
        period: TaxPeriod
    ): VATReturnSubmission {
        logger.info("Submitting VAT return for period ${period.startDate} to ${period.endDate}")
        
        // Step 1: Calculate VAT figures
        val vatReturn = calculateVATReturn(tenantId, period)
        
        // Step 2: Validate (9-box VAT return)
        validateVATReturn(vatReturn)
        
        // Step 3: Submit to HMRC via API
        val submission = hmrcClient.submitVATReturn(
            vrn = getTenantVRN(tenantId),
            periodKey = period.key,
            vatReturn = VATReturnRequest(
                periodKey = period.key,
                vatDueSales = vatReturn.box1,           // Box 1: VAT due on sales
                vatDueAcquisitions = vatReturn.box2,     // Box 2: VAT due on acquisitions
                totalVatDue = vatReturn.box3,            // Box 3: Total VAT due (Box 1 + Box 2)
                vatReclaimedCurrPeriod = vatReturn.box4, // Box 4: VAT reclaimed
                netVatDue = vatReturn.box5,              // Box 5: Net VAT due (Box 3 - Box 4)
                totalValueSalesExVAT = vatReturn.box6,   // Box 6: Total value of sales ex VAT
                totalValuePurchasesExVAT = vatReturn.box7, // Box 7: Total value of purchases ex VAT
                totalValueGoodsSuppliedExVAT = vatReturn.box8, // Box 8: Total value of goods supplied ex VAT
                totalAcquisitionsExVAT = vatReturn.box9  // Box 9: Total acquisitions ex VAT
            )
        )
        
        // Step 4: Store submission record
        val record = VATReturnSubmission(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            period = period,
            submittedAt = Instant.now(),
            processingDate = submission.processingDate,
            receiptId = submission.formBundleNumber,
            status = VATReturnStatus.SUBMITTED
        )
        
        vatReturnRepository.save(record)
        
        logger.info("VAT return submitted: ${record.id}, receipt: ${record.receiptId}")
        
        return record
    }
    
    /**
     * Retrieve VAT obligations from HMRC
     */
    suspend fun getVATObligations(tenantId: UUID): List<VATObligation> {
        val vrn = getTenantVRN(tenantId)
        val obligations = hmrcClient.getVATObligations(vrn)
        
        return obligations.map { obligation ->
            VATObligation(
                periodKey = obligation.periodKey,
                startDate = obligation.start,
                endDate = obligation.end,
                dueDate = obligation.due,
                status = obligation.status, // O = Open, F = Fulfilled
                received = obligation.received
            )
        }
    }
}

data class VATReturnRequest(
    val periodKey: String,
    val vatDueSales: BigDecimal,          // Box 1
    val vatDueAcquisitions: BigDecimal,   // Box 2
    val totalVatDue: BigDecimal,          // Box 3
    val vatReclaimedCurrPeriod: BigDecimal, // Box 4
    val netVatDue: BigDecimal,            // Box 5
    val totalValueSalesExVAT: BigDecimal, // Box 6
    val totalValuePurchasesExVAT: BigDecimal, // Box 7
    val totalValueGoodsSuppliedExVAT: BigDecimal, // Box 8
    val totalAcquisitionsExVAT: BigDecimal // Box 9
)
```

#### 4.3 Spain (SII - Suministro Inmediato de Información)

```kotlin
/**
 * Spain SII (Immediate Supply of Information)
 * Real-time invoice reporting to AEAT (Agencia Tributaria)
 */
@Service
class SpainSIIIntegration(
    private val aeatClient: AEATApiClient,
    private val certificateManager: CertificateManager
) {
    
    /**
     * Submit invoice to AEAT within 4 days of issue
     */
    suspend fun submitInvoiceToSII(invoice: Invoice): SIISubmission {
        logger.info("Submitting invoice ${invoice.number} to AEAT SII")
        
        // Step 1: Generate SII XML
        val siiXML = generateSIIXML(invoice)
        
        // Step 2: Sign with certificate (required)
        val certificate = certificateManager.getCertificate(invoice.tenantId, "ES")
        val signedXML = signXML(siiXML, certificate)
        
        // Step 3: Submit to AEAT
        val response = aeatClient.submitInvoice(
            nif = invoice.seller.nif,
            xml = signedXML,
            invoiceType = if (invoice.type == InvoiceType.SALE) "FacturaEmitida" else "FacturaRecibida"
        )
        
        // Step 4: Store submission record
        val submission = SIISubmission(
            id = UUID.randomUUID(),
            invoiceId = invoice.id,
            tenantId = invoice.tenantId,
            submittedAt = Instant.now(),
            csv = response.csv, // Código Seguro de Verificación
            status = if (response.estadoFactura == "Correcto") {
                SIIStatus.ACCEPTED
            } else {
                SIIStatus.REJECTED
            },
            errorCode = response.codigoError,
            errorMessage = response.descripcionError
        )
        
        siiSubmissionRepository.save(submission)
        
        return submission
    }
    
    /**
     * Generate SII XML format
     */
    private fun generateSIIXML(invoice: Invoice): String {
        return xmlGenerator.generate {
            element("sii:SuministroLRFacturasEmitidas") {
                attribute("xmlns:sii", "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/ssii/fact/ws/SuministroInformacion.xsd")
                
                element("sii:Cabecera") {
                    element("sii:IDVersionSii") { text("1.1") }
                    element("sii:Titular") {
                        element("sii:NombreRazon") { text(invoice.seller.name) }
                        element("sii:NIF") { text(invoice.seller.nif) }
                    }
                }
                
                element("sii:RegistroLRFacturasEmitidas") {
                    element("sii:PeriodoImpositivo") {
                        element("sii:Ejercicio") { text(invoice.issuedAt.year.toString()) }
                        element("sii:Periodo") { text(invoice.issuedAt.monthValue.toString().padStart(2, '0')) }
                    }
                    
                    element("sii:IDFactura") {
                        element("sii:IDEmisorFactura") {
                            element("sii:NIF") { text(invoice.seller.nif) }
                        }
                        element("sii:NumSerieFacturaEmisor") { text(invoice.number) }
                        element("sii:FechaExpedicionFacturaEmisor") {
                            text(invoice.issuedAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        }
                    }
                    
                    element("sii:FacturaExpedida") {
                        element("sii:TipoFactura") { text("F1") } // Factura
                        element("sii:ClaveRegimenEspecialOTrascendencia") { text("01") } // Régimen general
                        element("sii:ImporteTotal") { text(invoice.total.toString()) }
                        element("sii:DescripcionOperacion") { text(invoice.description) }
                        
                        element("sii:Contraparte") {
                            element("sii:NombreRazon") { text(invoice.customer.name) }
                            if (invoice.customer.isSpanish) {
                                element("sii:NIF") { text(invoice.customer.nif) }
                            } else {
                                element("sii:IDOtro") {
                                    element("sii:CodigoPais") { text(invoice.customer.country) }
                                    element("sii:IDType") { text("02") } // NIF-IVA
                                    element("sii:ID") { text(invoice.customer.vatId) }
                                }
                            }
                        }
                        
                        element("sii:TipoDesglose") {
                            element("sii:DesgloseFactura") {
                                element("sii:Sujeta") {
                                    element("sii:NoExenta") {
                                        element("sii:TipoNoExenta") { text("S1") }
                                        element("sii:DesgloseIVA") {
                                            element("sii:DetalleIVA") {
                                                element("sii:TipoImpositivo") { text("21.00") }
                                                element("sii:BaseImponible") { text(invoice.subtotal.toString()) }
                                                element("sii:CuotaRepercutida") { text(invoice.vatAmount.toString()) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

## Implementation Roadmap

### Phase 1: Foundation & North America (Q1 2027)

**Deliverables**:
- [ ] Localization framework (country pack loader)
- [ ] E-invoicing engine (core)
- [ ] 🇺🇸 United States (enhanced sales tax via ADR-062)
- [ ] 🇨🇦 Canada (GST/HST/PST/QST, T4/T5)
- [ ] 🇲🇽 Mexico (CFDI 4.0 e-invoicing, SAT)

**Timeline**: 12 weeks (January-March 2027)
**Resources**: 3 backend engineers, 1 integration specialist, 1 localization expert

### Phase 2: Western Europe (Q2 2027)

**Deliverables**:
- [ ] 🇬🇧 United Kingdom (MTD, VAT, CIS)
- [ ] 🇩🇪 Germany (E-Rechnung, Intrastat, DATEV, GoBD)
- [ ] 🇫🇷 France (Chorus Pro, FEC, TVA)
- [ ] 🇪🇸 Spain (SII, Batuz, TicketBAI)

**Timeline**: 10 weeks (April-June 2027)
**Resources**: 3 backend engineers, 1 integration specialist, 2 localization experts

### Phase 3: Latin America & Asia-Pacific (Q3 2027)

**Deliverables**:
- [ ] 🇧🇷 Brazil (NF-e, SPED Fiscal, all tax types)
- [ ] 🇦🇺 Australia (BAS, STP, ATO)
- [ ] 🇸🇬 Singapore (GST, IRAS)
- [ ] 🇮🇳 India (Enhanced GST, E-Way Bill, E-Invoice)

**Timeline**: 12 weeks (July-September 2027)
**Resources**: 4 backend engineers, 2 integration specialists, 2 localization experts

### Phase 4: Europe & Middle East (Q4 2027)

**Deliverables**:
- [ ] 🇮🇹 Italy (Fattura Elettronica, SDI)
- [ ] 🇯🇵 Japan (JCT, Qualified Invoice, e-Tax)
- [ ] 🇦🇪 UAE (VAT, FAF, Arabic support)
- [ ] 🇸🇦 Saudi Arabia (ZATCA Phase 2, Zakat)

**Timeline**: 10 weeks (October-December 2027)
**Resources**: 3 backend engineers, 2 integration specialists, 2 localization experts

---

## Cost Estimate

### Development Costs (Per Country)

| Country Complexity | Dev Cost | Timeline | Examples |
|-------------------|----------|----------|----------|
| **Simple** | $50K-80K | 3-4 weeks | UAE, Singapore |
| **Medium** | $80K-150K | 6-8 weeks | UK, Canada, Australia |
| **Complex** | $150K-250K | 10-12 weeks | Germany, France, Spain |
| **Very Complex** | $250K-400K | 14-16 weeks | Brazil, Mexico, India |

### Total Development Costs (15 Countries)

| Item | Cost | Timeline |
|------|------|----------|
| **Framework** (country pack loader, e-invoice engine) | $200K-300K | Q1 2027 |
| **Simple Countries** (2): UAE, Singapore | $100K-160K | Q4 2027 |
| **Medium Countries** (5): UK, Canada, Australia, Japan, Saudi | $400K-750K | Q2-Q4 2027 |
| **Complex Countries** (5): Germany, France, Spain, Italy, Mexico | $750K-1.25M | Q2-Q4 2027 |
| **Very Complex Countries** (3): Brazil, India (enhanced), US (enhanced) | $750K-1.2M | Q1+Q3 2027 |
| **Total Development** | **$2.2M-3.66M** | 12 months |

### Infrastructure & Services

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **Government Portal APIs** | $50K-100K | HMRC, AEAT, SAT, SEFAZ, etc. |
| **Digital Certificates** (PKI, HSM) | $20K-40K | E-invoice signing |
| **Translation Services** | $30K-60K | 15+ languages |
| **Compliance Consulting** | $100K-200K | Country-specific legal review |
| **Testing & Certification** | $50K-100K | Government certification (Brazil, Mexico) |
| **Total Infrastructure** | **$250K-500K** | |

### Total Investment: **$2.45M-4.16M** (first year, 15 countries)

---

## Success Metrics

### Business KPIs
- ✅ **Supported countries**: 15+ by end 2027 (from 3 currently)
- ✅ **International deal closure**: +40% (65% → 90% of prospects)
- ✅ **Revenue from international**: +60% Y/Y
- ✅ **Average deal size**: +35% (multi-country deployments)

### Technical KPIs
- ✅ **E-invoice generation success**: >99% (first-time)
- ✅ **Government submission success**: >95% (e.g., HMRC, AEAT)
- ✅ **Localization coverage**: 100% (tax + e-invoice + statutory reports)
- ✅ **Country pack load time**: <10 seconds

### Operational KPIs
- ✅ **Localization support tickets**: <5% of total tickets
- ✅ **Compliance audit success**: 100% (zero findings)
- ✅ **Customer satisfaction (international)**: >4.5/5

---

## Integration with Other ADRs

- **ADR-062**: Multi-Country Tax Engine (tax calculation, Avalara integration)
- **ADR-009**: Financial Accounting (GL posting, COA)
- **ADR-058**: SOC 2 (audit trails, change management)
- **ADR-059**: ISO 27001 (data residency, vendor management)
- **ADR-064**: GDPR (data protection, cross-border transfers)

---

## Consequences

### Positive ✅
- **Market expansion**: 15+ countries (from 3), 90% international prospect coverage
- **Competitive parity**: SAP/Oracle level localization depth
- **Revenue growth**: +60% international revenue
- **Compliance**: Zero legal/tax violations (automated)
- **Customer satisfaction**: World-class localization experience

### Negative ⚠️
- **Development cost**: $2.45M-4.16M (highest single investment)
- **Maintenance burden**: 15 countries × ongoing updates
- **Complexity**: Country-specific logic difficult to test
- **Government API dependencies**: External API downtime impact

### Risks 🚨
- **Regulatory changes**: E-invoicing mandates change frequently
  - Mitigation: Quarterly regulatory reviews, country pack versioning
- **Government API downtime**: Cannot generate e-invoices
  - Mitigation: Offline mode, queue-and-retry
- **Translation quality**: Poor translations damage brand
  - Mitigation: Professional translation services, native speakers
- **Certificate expiry**: E-invoices fail if certificate expires
  - Mitigation: 90-day expiry alerts, auto-renewal

---

## References

### Regulatory Sources
- **EU**: VAT Directive, Intrastat regulation
- **Brazil**: NF-e Manual (version 4.00), SPED documentation
- **Mexico**: CFDI 4.0 specification, SAT portal
- **UK**: HMRC Making Tax Digital API
- **Spain**: AEAT SII specification

### Industry Standards
- **Peppol**: Pan-European Public Procurement Online (UBL 2.1)
- **UBL**: Universal Business Language (ISO/IEC 19845)
- **OASIS**: e-invoicing standards

### Related ADRs
- ADR-009: Financial Accounting Domain
- ADR-062: Multi-Country Tax Engine
- ADR-058: SOC 2 Compliance
- ADR-059: ISO 27001 ISMS
- ADR-064: GDPR Compliance Framework

---

*Document Owner*: Head of International Expansion  
*Review Frequency*: Quarterly (regulatory changes)  
*Next Review*: May 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q1 2027**
