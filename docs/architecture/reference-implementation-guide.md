# ChiroERP Reference Implementation Guide

**Status**: Implementation Roadmap
**Date**: 2026-02-03
**Purpose**: Validate architecture with two end-to-end flows (O2C + P2P) across two countries (US + Germany)
**Target Duration**: 6 months
**Team Size**: 8-10 engineers (2 flows × 4-5 devs)

---

## Executive Summary

This reference implementation proves ChiroERP's architecture by building **two complete business processes** spanning **multiple domains** and **multiple countries**:

1. **Order-to-Cash (O2C)**: Sales Order → Credit Check → Inventory → Shipping → Invoicing → Payment → Dunning
2. **Procure-to-Pay (P2P)**: Purchase Requisition → RFQ → PO → Goods Receipt → Invoice Verification → Payment

### Why These Two?

| Flow | Spans Domains | Country Complexity | Business Value |
|------|---------------|-------------------|----------------|
| **O2C** | Sales, Finance AR, Inventory, Shipping, Treasury | Revenue recognition rules, VAT vs Sales Tax, e-invoicing | $1M+ ARR deals require O2C |
| **P2P** | Procurement, Inventory, Finance AP, Treasury | Import duties, VAT reverse charge, different payment formats | Operational efficiency (70% of transactions) |

### Countries Selected

**Primary Market (Africa-Focused)**

| Country | Tax System | E-Invoicing | Payment Format | CoA | Complexity |
|---------|------------|-------------|----------------|-----|------------|
| **Kenya** 🇰🇪 | VAT 16% | Mandatory (KRA eTIMS) | M-Pesa, RTGS, PesaLink | IFRS-based | High (eTIMS, M-Pesa, EAC) |
| **Tanzania** 🇹🇿 | VAT 18% | Mandatory (TRA VFD/EFD) | M-Pesa, SWIFT | IFRS-based | High (VFD devices, EAC) |

**Secondary Markets (Global Reference)**

| Country | Tax System | E-Invoicing | Payment Format | CoA | Complexity |
|---------|------------|-------------|----------------|-----|------------|
| **United States** 🇺🇸 | Sales Tax (jurisdiction-based) | None | ACH (NACHA) | US-GAAP | Medium |
| **Germany** 🇩🇪 | VAT 19%/7% | Optional (PEPPOL) | SEPA (ISO 20022) | SKR04 (HGB) | High (EU regulations) |

**Goal**: If O2C + P2P work in Kenya + Tanzania, they'll work across:
- ✅ All 6 East African Community (EAC) countries: Kenya, Tanzania, Uganda, Rwanda, Burundi, South Sudan
- ✅ All 54 African countries (ECOWAS, SADC, COMESA)
- ✅ Global markets (proven with US + Germany patterns)

**Market Strategy**: Kenya is the **anchor market** (largest economy in East Africa, tech hub), Tanzania validates EAC harmonization, US/Germany prove global scalability.

---

## Reference Implementation Scope

### What's IN Scope

#### 1. Order-to-Cash (O2C) - Kenya Market 🇰🇪 **[PRIMARY]**
- ✅ Customer master data with KRA PIN (MDM)
- ✅ Sales order creation (Sales domain)
- ✅ Credit limit check (Finance AR)
- ✅ Inventory reservation (Inventory)
- ✅ Pricing with discounts (Configuration framework ADR-044)
- ✅ VAT 16% calculation (Tax Engine ADR-047 - Kenya Country Pack)
- ✅ Shipping notification (Logistics)
- ✅ **KRA eTIMS invoice submission** (mandatory - Finance AR)
- ✅ Invoice generation with IFRS posting (Finance AR)
- ✅ **Cu number (Control Unit) retrieval from KRA**
- ✅ **Payment receipt via M-Pesa API integration** (Treasury ADR-026)
- ✅ Payment receipt via RTGS/PesaLink (local bank transfers)
- ✅ Automatic dunning after 30 days (Workflow ADR-046)
- ✅ **Withholding tax (WHT) calculation** for corporate customers (2%/5%/10%)

#### 1b. Order-to-Cash (O2C) - US Market 🇺🇸 **[SECONDARY]**
- ✅ Customer master data (MDM)
- ✅ Sales order creation (Sales domain)
- ✅ Credit limit check (Finance AR)
- ✅ Inventory reservation (Inventory)
- ✅ Pricing with discounts (Configuration framework ADR-044)
- ✅ Sales tax calculation - jurisdiction-based (Tax Engine ADR-047)
- ✅ Shipping notification (Logistics)
- ✅ Invoice generation with US-GAAP posting (Finance AR)
- ✅ Payment receipt via ACH (Treasury ADR-026)
- ✅ Automatic dunning after 30 days (Workflow ADR-046)

#### 2. Order-to-Cash (O2C) - Tanzania Market 🇹🇿 **[PRIMARY - EAC Validation]**
- ✅ Customer master data with TIN (Tax Identification Number)
- ✅ Sales order creation
- ✅ Credit limit check
- ✅ Inventory reservation
- ✅ Pricing with condition technique (ADR-044)
- ✅ VAT 18% calculation (ADR-047 Tanzania Country Pack)
- ✅ Shipping notification
- ✅ **TRA VFD/EFD integration** (Virtual Fiscal Device - mandatory)
- ✅ Invoice generation with IFRS posting
- ✅ **Verification code retrieval from TRA**
- ✅ Payment receipt via M-Pesa Tanzania
- ✅ Payment receipt via SWIFT (Tanzanian banks)
- ✅ Automatic dunning (Swahili language support)

#### 2b. Order-to-Cash (O2C) - Germany Market 🇩🇪 **[SECONDARY]**
- ✅ Customer master data with VAT registration
- ✅ Sales order creation
- ✅ Credit limit check
- ✅ Inventory reservation
- ✅ Pricing with condition technique (ADR-044)
- ✅ VAT calculation 19%/7% (ADR-047 Germany Country Pack)
- ✅ Shipping notification
- ✅ Invoice generation with SKR04 posting (HGB)
- ✅ PEPPOL e-invoice submission (ADR-047)
- ✅ Payment receipt via SEPA (ISO 20022)
- ✅ Automatic dunning (German language)

#### 3. Procure-to-Pay (P2P) - Kenya Market 🇰🇪 **[PRIMARY]**
- ✅ Supplier master data with KRA PIN (MDM)
- ✅ Purchase requisition (Procurement)
- ✅ RFQ process (optional)
- ✅ Purchase order creation with approval workflow (ADR-046)
- ✅ Goods receipt (Inventory)
- ✅ Three-way match (PO + GR + Invoice)
- ✅ **Supplier invoice verification with KRA eTIMS validation** (verify Cu number)
- ✅ Invoice verification (Finance AP)
- ✅ **Withholding VAT calculation** (6% for non-registered suppliers)
- ✅ **Import duty & customs handling** (for imported goods)
- ✅ IFRS posting to GL
- ✅ **Payment via M-Pesa B2B** (for small suppliers)
- ✅ Payment via RTGS/PesaLink (Treasury)
- ✅ Vendor aging report

#### 3b. Procure-to-Pay (P2P) - US Market 🇺🇸 **[SECONDARY]**
- ✅ Supplier master data (MDM)
- ✅ Purchase requisition (Procurement)
- ✅ RFQ process (optional)
- ✅ Purchase order creation with approval workflow (ADR-046)
- ✅ Goods receipt (Inventory)
- ✅ Three-way match (PO + GR + Invoice)
- ✅ Invoice verification (Finance AP)
- ✅ US-GAAP posting to GL
- ✅ Payment via ACH (Treasury)
- ✅ Vendor aging report

#### 4. Procure-to-Pay (P2P) - Tanzania Market 🇹🇿 **[PRIMARY - EAC Validation]**
- ✅ Supplier master data with TIN
- ✅ Purchase requisition
- ✅ Purchase order with multi-level approval (ADR-046)
- ✅ Goods receipt
- ✅ **Invoice verification with TRA VFD validation** (verify verification code)
- ✅ **Withholding tax (WHT) calculation** for services (5%/10%/15%)
- ✅ IFRS posting to GL
- ✅ Payment via M-Pesa Tanzania
- ✅ Payment via SWIFT (Tanzanian banks)
- ✅ **EAC customs documentation** (for cross-border trade)

#### 4b. Procure-to-Pay (P2P) - Germany Market 🇩🇪 **[SECONDARY]**
- ✅ Supplier master data with VAT registration
- ✅ Purchase requisition
- ✅ Purchase order with multi-level approval (ADR-046)
- ✅ Goods receipt
- ✅ Invoice verification with VAT reverse charge (EU supplier)
- ✅ SKR04 posting to GL
- ✅ PEPPOL e-invoice receipt
- ✅ Payment via SEPA (ISO 20022)
- ✅ GDPdU audit file export (ADR-047)

#### 5. Cross-Cutting Capabilities
- ✅ Multi-tenancy isolation (ADR-005)
- ✅ Organizational model (ADR-045): CompanyCode, Plant, CostCenter
- ✅ Configuration framework (ADR-044): Pricing, posting rules, approval schemas
- ✅ Workflow engine (ADR-046): PO approval, dunning
- ✅ Country packs (ADR-047): US + Germany
- ✅ Event-driven integration (ADR-003): Kafka events across domains
- ✅ CQRS read models (ADR-001): PostgreSQL write, MongoDB read
- ✅ Frontend UI (ADR-048): React micro-frontends for all screens

### What's OUT of Scope (Deferred to Full Product)

- ❌ Other 68 modules (Manufacturing, Quality, Maintenance, etc.)
- ❌ Other 48 countries (France, India, Brazil, etc.)
- ❌ Advanced features (Intercompany, Consignment, Rebates, etc.)
- ❌ Mobile native apps (PWA sufficient for reference)
- ❌ Extensibility marketplace (ADR-049 - API hooks only)
- ❌ Performance optimization beyond basic (detailed tuning in Phase 2)

---

## Architecture Components to Validate

### 1. Microservices Architecture
**Domains Involved**: 10 of 72 modules

| Domain | Modules | Purpose |
|--------|---------|---------|
| **Sales** | Sales Order Management | Create orders, pricing, fulfillment |
| **Finance** | GL, AR, AP | Invoicing, payments, accounting |
| **Inventory** | Stock Management, Reservation | Stock availability, allocation |
| **Procurement** | Purchase Order Management | PO creation, approval |
| **Treasury** | Cash Management, Payments | ACH, SEPA payments |
| **Master Data** | Customer, Supplier | Centralized master data |
| **Configuration** | Rules Engine | Pricing, posting, tax rules |
| **Workflow** | Temporal Workflows | PO approval, dunning |
| **Localization** | Country Packs | US + Germany tax, CoA, payments |
| **Platform** | Auth, Notifications | Shared services |

**Validation**: Can 10 microservices communicate via REST + Kafka?

---

### 2. Event-Driven Integration (Kafka)

#### O2C Event Chain
```
SalesOrderCreatedEvent (Sales)
  ↓
CreditCheckRequestedEvent (Finance AR)
  ↓
CreditCheckCompletedEvent (Finance AR)
  ↓
InventoryReservationCreatedEvent (Inventory)
  ↓
ShipmentConfirmedEvent (Logistics)
  ↓
InvoiceCreatedEvent (Finance AR)
  ↓
InvoicePostedEvent (Finance GL)
  ↓ (30 days later)
DunningNoticeGeneratedEvent (Finance AR)
```

#### P2P Event Chain
```
PurchaseRequisitionCreatedEvent (Procurement)
  ↓
PurchaseOrderCreatedEvent (Procurement)
  ↓
PurchaseOrderApprovedEvent (Procurement) → Triggers Workflow
  ↓
GoodsReceiptCreatedEvent (Inventory)
  ↓
SupplierInvoiceReceivedEvent (Finance AP)
  ↓
InvoiceVerifiedEvent (Finance AP) → Three-way match
  ↓
PaymentExecutedEvent (Treasury)
```

**Validation**: Are events consumed reliably? Dead letter queues? Saga rollback?

---

### 3. CQRS Read Models

#### Example: Sales Order Read Model
```kotlin
// Write Model: PostgreSQL (sales-order-service)
@Entity
@Table(name = "sales_orders")
data class SalesOrder(
    @Id val id: UUID,
    val tenantId: UUID,
    val companyCodeId: UUID,
    val customerId: UUID,
    val orderNumber: String,
    val orderDate: LocalDate,
    val totalAmount: BigDecimal,
    val currency: String,
    val status: OrderStatus,
    val version: Long, // Optimistic locking
)

// Read Model: MongoDB (sales-query-service)
data class SalesOrderReadModel(
    val id: String,
    val tenantId: String,
    val orderNumber: String,
    val orderDate: String,
    val customerName: String, // Denormalized from Customer
    val customerCountry: String,
    val lineItems: List<LineItemReadModel>,
    val totalAmount: BigDecimal,
    val currency: String,
    val status: String,
    val invoiceNumber: String?, // Denormalized from Invoice
    val paymentStatus: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
```

**Validation**: Read model updated < 1 second after write? Eventual consistency working?

---

### 4. Configuration Framework (ADR-044)

#### US Pricing Configuration
```kotlin
// Pricing Schema: US Market
val usPricingSchema = PricingSchema(
    schemaId = UUID.randomUUID(),
    tenantId = tenant.id,
    companyCodeId = usCompanyCode.id,
    salesOrgId = usSalesOrg.id,
    version = "1.0.0",
    effectiveFrom = LocalDate.of(2026, 1, 1),
    steps = listOf(
        // Step 1: Base Price
        PricingStep(
            stepNumber = 1,
            conditionType = "BASE_PRICE",
            calculationType = "ABSOLUTE",
            sourceTable = "material_prices",
            amount = null, // Looked up
        ),
        // Step 2: Customer Discount
        PricingStep(
            stepNumber = 2,
            conditionType = "CUSTOMER_DISCOUNT",
            calculationType = "PERCENTAGE",
            amount = -5.0, // 5% discount
        ),
        // Step 3: Sales Tax
        PricingStep(
            stepNumber = 3,
            conditionType = "SALES_TAX",
            calculationType = "PERCENTAGE",
            amount = 8.25, // California sales tax
            postingRuleId = salesTaxPostingRule.id,
        ),
    ),
)
```

#### Germany Pricing Configuration (Condition Technique)
```kotlin
// Pricing Schema: Germany Market
val germanyPricingSchema = PricingSchema(
    schemaId = UUID.randomUUID(),
    tenantId = tenant.id,
    companyCodeId = germanyCompanyCode.id,
    salesOrgId = germanySalesOrg.id,
    version = "1.0.0",
    effectiveFrom = LocalDate.of(2026, 1, 1),
    steps = listOf(
        // Step 1: Base Price
        PricingStep(
            stepNumber = 1,
            conditionType = "PR00", // SAP-style condition types
            calculationType = "ABSOLUTE",
            sourceTable = "material_prices",
        ),
        // Step 2: Material Discount
        PricingStep(
            stepNumber = 2,
            conditionType = "K007",
            calculationType = "PERCENTAGE",
            amount = -10.0,
        ),
        // Step 3: Customer Discount
        PricingStep(
            stepNumber = 3,
            conditionType = "K020",
            calculationType = "PERCENTAGE",
            sourceTable = "customer_discounts",
        ),
        // Step 4: Freight Surcharge
        PricingStep(
            stepNumber = 4,
            conditionType = "KF00",
            calculationType = "ABSOLUTE",
            amount = 50.0,
        ),
        // Step 5: VAT 19%
        PricingStep(
            stepNumber = 5,
            conditionType = "MWST",
            calculationType = "PERCENTAGE",
            amount = 19.0,
            postingRuleId = vatPostingRule.id,
        ),
    ),
)
```

**Validation**: Can change pricing without code? 10-step calculation < 100ms?

---

### 5. Organizational Model (ADR-045)

#### US Organization Setup
```kotlin
// Tenant
val tenant = Tenant(
    id = UUID.randomUUID(),
    name = "Acme Corporation",
    baseCurrency = "USD",
    status = TenantStatus.ACTIVE,
)

// Company Code (Legal Entity)
val usCompanyCode = CompanyCode(
    id = UUID.randomUUID(),
    tenantId = tenant.id,
    code = "US01",
    name = "Acme Inc.",
    country = "US",
    currency = "USD",
    fiscalYearVariant = "K4", // Calendar year
    chartOfAccountsId = usGaapCoA.id,
    taxRegistrations = listOf(
        TaxRegistration("US", "FEDERAL", "12-3456789"), // EIN
        TaxRegistration("US", "CA", "123-456-789"), // California
    ),
)

// Plant (Warehouse/Factory)
val usPlant = Plant(
    id = UUID.randomUUID(),
    tenantId = tenant.id,
    code = "1000",
    name = "US West Coast Warehouse",
    country = "US",
    city = "Los Angeles",
    plantType = PlantType.WAREHOUSE,
)

// Plant Assignment
val plantAssignment = PlantAssignment(
    plantId = usPlant.id,
    companyCodeId = usCompanyCode.id,
    costCenterId = warehouseCostCenter.id,
    profitCenterId = westCoastProfitCenter.id,
    effectiveFrom = LocalDate.of(2026, 1, 1),
)

// Sales Organization
val usSalesOrg = SalesOrganization(
    id = UUID.randomUUID(),
    tenantId = tenant.id,
    code = "US10",
    name = "US Sales Org",
    currency = "USD",
    distributionChannels = listOf("WHOLESALE", "RETAIL"),
    divisions = listOf("ELECTRONICS", "APPLIANCES"),
)
```

#### Germany Organization Setup
```kotlin
// Company Code (Legal Entity)
val germanyCompanyCode = CompanyCode(
    id = UUID.randomUUID(),
    tenantId = tenant.id,
    code = "DE01",
    name = "Acme GmbH",
    country = "DE",
    currency = "EUR",
    fiscalYearVariant = "K4",
    chartOfAccountsId = skr04CoA.id,
    taxRegistrations = listOf(
        TaxRegistration("DE", "VAT", "DE123456789"), // VAT number
    ),
)

// Plant
val germanyPlant = Plant(
    id = UUID.randomUandom(),
    tenantId = tenant.id,
    code = "2000",
    name = "Germany Distribution Center",
    country = "DE",
    city = "Frankfurt",
    plantType = PlantType.WAREHOUSE,
)

// Sales Organization
val germanySalesOrg = SalesOrganization(
    id = UUID.randomUUID(),
    tenantId = tenant.id,
    code = "DE10",
    name = "Germany Sales Org",
    currency = "EUR",
    distributionChannels = listOf("WHOLESALE"),
    divisions = listOf("ELECTRONICS"),
)
```

**Validation**: Can support multi-country legal entities? Org-based authorization?

---

### 6. Country Packs (ADR-047)

#### Kenya Country Pack 🇰🇪 **[PRIMARY IMPLEMENTATION]**
```kotlin
val kenyaCountryPack = CountryPack(
    countryCode = "KE",
    version = SemanticVersion(1, 0, 0),
    effectiveFrom = LocalDate.of(2026, 1, 1),
    status = CountryPackStatus.ACTIVE,
    taxRules = TaxRuleSet(
        rules = listOf(
            // Standard VAT 16%
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "VAT",
                rate = 16.0,
                compoundTax = false,
            ),
            // Zero-rated items (exports, basic food items)
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "VAT_ZERO",
                rate = 0.0,
                applicableTo = listOf("EXPORTS", "BASIC_FOODS", "AGRICULTURAL_INPUTS"),
            ),
            // Exempt items (financial services, education, healthcare)
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "VAT_EXEMPT",
                rate = 0.0,
                applicableTo = listOf("FINANCIAL_SERVICES", "EDUCATION", "HEALTHCARE"),
            ),
            // Withholding VAT 6% (for non-registered suppliers)
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "WITHHOLDING_VAT",
                rate = 6.0,
                applicableWhen = "SUPPLIER_NOT_VAT_REGISTERED",
            ),
            // Withholding Tax on Services
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "WHT_CONSULTANCY",
                rate = 5.0,
                applicableTo = listOf("PROFESSIONAL_SERVICES", "CONSULTANCY"),
            ),
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "WHT_MANAGEMENT_FEE",
                rate = 10.0,
                applicableTo = listOf("MANAGEMENT_FEES"),
            ),
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "WHT_RENTAL",
                rate = 10.0,
                applicableTo = listOf("RENTAL_INCOME"),
            ),
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "WHT_DIVIDENDS",
                rate = 5.0,
                applicableTo = listOf("DIVIDENDS"),
            ),
            // Import Duty (ranges from 0% to 35% depending on HS code)
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "IMPORT_DUTY",
                rate = 25.0, // Average, actual rate from HS code lookup
                applicableTo = listOf("IMPORTED_GOODS"),
            ),
            // Excise Duty (for specific goods like alcohol, tobacco, fuel)
            JurisdictionRule(
                jurisdiction = "KE",
                taxType = "EXCISE_DUTY",
                rate = 0.0, // Specific rates per unit
                applicableTo = listOf("ALCOHOL", "TOBACCO", "PETROLEUM", "SOFT_DRINKS"),
            ),
        ),
    ),
    coaTemplate = CoATemplate(
        templateId = UUID.randomUUID(),
        name = "Kenya IFRS Chart of Accounts",
        accountingStandard = "IFRS",
        industryVertical = "GENERAL",
        accounts = listOf(
            // Assets
            GLAccountTemplate("1000", "Cash and Cash Equivalents", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1010", "Petty Cash", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1020", "Bank - KCB Current Account", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1030", "Bank - Equity Bank", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1040", "M-Pesa Paybill Account", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1100", "Trade Receivables", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1110", "Other Receivables", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1120", "Allowance for Doubtful Debts", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1200", "Inventory - Raw Materials", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1210", "Inventory - Finished Goods", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1300", "Prepaid Expenses", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1310", "VAT Input", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1320", "Withholding Tax Receivable", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1500", "Property, Plant and Equipment", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1510", "Accumulated Depreciation", AccountType.ASSET, "BALANCE_SHEET"),

            // Liabilities
            GLAccountTemplate("2000", "Trade Payables", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2010", "Other Payables", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2100", "VAT Output", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2110", "Withholding VAT Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2120", "Withholding Tax Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2130", "PAYE Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2140", "NSSF Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2150", "NHIF Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2200", "Bank Loans", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2300", "Accrued Expenses", AccountType.LIABILITY, "BALANCE_SHEET"),

            // Equity
            GLAccountTemplate("3000", "Share Capital", AccountType.EQUITY, "BALANCE_SHEET"),
            GLAccountTemplate("3100", "Retained Earnings", AccountType.EQUITY, "BALANCE_SHEET"),
            GLAccountTemplate("3200", "Current Year Profit/Loss", AccountType.EQUITY, "BALANCE_SHEET"),

            // Revenue
            GLAccountTemplate("4000", "Sales Revenue", AccountType.REVENUE, "INCOME_STATEMENT"),
            GLAccountTemplate("4010", "Export Sales", AccountType.REVENUE, "INCOME_STATEMENT"),
            GLAccountTemplate("4100", "Service Revenue", AccountType.REVENUE, "INCOME_STATEMENT"),
            GLAccountTemplate("4200", "Other Income", AccountType.REVENUE, "INCOME_STATEMENT"),

            // Cost of Sales
            GLAccountTemplate("5000", "Cost of Goods Sold", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("5100", "Direct Labor", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("5200", "Manufacturing Overheads", AccountType.EXPENSE, "INCOME_STATEMENT"),

            // Operating Expenses
            GLAccountTemplate("6000", "Salaries and Wages", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6100", "Rent Expense", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6200", "Utilities", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6300", "Transport and Delivery", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6400", "Marketing and Advertising", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6500", "Depreciation Expense", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6600", "Bank Charges", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6610", "M-Pesa Charges", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6700", "Professional Fees", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6800", "Insurance", AccountType.EXPENSE, "INCOME_STATEMENT"),
            // ... 100+ accounts total
        ),
    ),
    bankingFormats = listOf(
        BankingFormat(
            formatId = "RTGS_KE",
            name = "Kenya RTGS Format",
            type = BankingFormatType.PAYMENT_FILE,
            fileExtension = "csv",
            specification = "CBK_RTGS",
        ),
        BankingFormat(
            formatId = "PESALINK_KE",
            name = "PesaLink Format",
            type = BankingFormatType.PAYMENT_FILE,
            fileExtension = "csv",
            specification = "IPSL_PESALINK",
        ),
        BankingFormat(
            formatId = "MPESA_API",
            name = "M-Pesa B2B/B2C API",
            type = BankingFormatType.API_INTEGRATION,
            apiEndpoint = "https://api.safaricom.co.ke/mpesa/",
            specification = "MPESA_DARAJA_API_V2",
        ),
    ),
    eInvoicingConfig = EInvoicingConfig(
        format = "KRA_ETIMS_JSON",
        clearanceRequired = true, // Mandatory in Kenya
        clearanceEndpoint = "https://etims.kra.go.ke/api/",
        digitalSignatureRequired = false, // Not required but Cu number is
        additionalFields = mapOf(
            "deviceSerialNumber" to "REQUIRED", // eTIMS device serial
            "internalData" to "REQUIRED", // eTIMS internal data
            "receiptSignature" to "REQUIRED", // eTIMS signature
        ),
    ),
    auditFileFormat = null, // Kenya does not have specific audit file format (uses eTIMS export)
    statutoryReports = listOf(
        StatutoryReportTemplate(
            reportId = "KE_VAT_RETURN",
            name = "Kenya VAT 3 Return",
            filingFrequency = FilingFrequency.MONTHLY,
            format = "PDF", // Filed via iTax portal
            submissionMethod = SubmissionMethod.PORTAL_UPLOAD,
            apiEndpoint = "https://itax.kra.go.ke/",
        ),
        StatutoryReportTemplate(
            reportId = "KE_WHT_RETURN",
            name = "Withholding Tax Return",
            filingFrequency = FilingFrequency.MONTHLY,
            format = "PDF",
            submissionMethod = SubmissionMethod.PORTAL_UPLOAD,
            apiEndpoint = "https://itax.kra.go.ke/",
        ),
        StatutoryReportTemplate(
            reportId = "KE_PAYE_RETURN",
            name = "PAYE Return (P10)",
            filingFrequency = FilingFrequency.MONTHLY,
            format = "PDF",
            submissionMethod = SubmissionMethod.PORTAL_UPLOAD,
            apiEndpoint = "https://itax.kra.go.ke/",
        ),
    ),
    localeConfig = LocaleConfig(
        language = "en", // English and Swahili
        country = "KE",
        currency = "KES", // Kenyan Shilling
        timezone = "Africa/Nairobi", // EAT (UTC+3)
        dateFormat = "dd/MM/yyyy",
        numberFormat = NumberFormat(
            decimalSeparator = ".",
            thousandsSeparator = ",",
            currencyFormat = "KES #,##0.00",
        ),
        addressFormat = AddressFormat(
            format = "{street}\n{building}\n{city}\n{postalCode}\nKenya",
            postalCodeRequired = true,
            postalCodeFormat = "^\\d{5}$", // 5-digit postal code
        ),
    ),
    customIntegrations = listOf(
        CustomIntegration(
            integrationId = "KRA_ETIMS",
            name = "KRA eTIMS Integration",
            type = IntegrationType.E_INVOICING,
            apiEndpoint = "https://etims.kra.go.ke/api/",
            authMethod = AuthMethod.API_KEY,
            description = "Mandatory electronic tax invoice management system",
        ),
        CustomIntegration(
            integrationId = "MPESA_DARAJA",
            name = "M-Pesa Daraja API",
            type = IntegrationType.PAYMENT,
            apiEndpoint = "https://api.safaricom.co.ke/",
            authMethod = AuthMethod.OAUTH2,
            description = "M-Pesa mobile money payment integration (C2B, B2C, B2B)",
        ),
        CustomIntegration(
            integrationId = "IPSL_PESALINK",
            name = "PesaLink Bank Transfer",
            type = IntegrationType.PAYMENT,
            apiEndpoint = "https://pesalink.co.ke/api/",
            authMethod = AuthMethod.API_KEY,
            description = "Real-time interbank transfer system",
        ),
    ),
)
```

**Kenya-Specific Requirements**:
- ✅ **KRA eTIMS Integration**: Mandatory e-invoicing with Control Unit (Cu) number
- ✅ **M-Pesa Integration**: C2B (Customer to Business), B2C (Business to Customer), B2B (Business to Business)
- ✅ **Withholding Tax**: 5-10% on services, 6% withholding VAT for non-registered suppliers
- ✅ **IFRS Accounting**: Kenya follows IFRS standards, not US-GAAP
- ✅ **Statutory Deductions**: PAYE, NSSF, NHIF integration for payroll
- ✅ **Import Duties**: HS code-based duty calculation for imported goods
- ✅ **EAC Customs**: East African Community customs documentation

#### US Country Pack 🇺🇸 **[SECONDARY REFERENCE]**
```kotlin
val usCountryPack = CountryPack(
    countryCode = "US",
    version = SemanticVersion(1, 0, 0),
    effectiveFrom = LocalDate.of(2026, 1, 1),
    status = CountryPackStatus.ACTIVE,
    taxRules = TaxRuleSet(
        rules = listOf(
            JurisdictionRule(
                jurisdiction = "US-CA",
                taxType = "SALES_TAX",
                rate = 8.25,
                compoundTax = false,
            ),
            JurisdictionRule(
                jurisdiction = "US-NY",
                taxType = "SALES_TAX",
                rate = 8.875,
                compoundTax = false,
            ),
            // 50 states + 3000+ jurisdictions
        ),
    ),
    coaTemplate = CoATemplate(
        templateId = UUID.randomUUID(),
        name = "US-GAAP Manufacturing",
        accountingStandard = "US-GAAP",
        industryVertical = "MANUFACTURING",
        accounts = listOf(
            GLAccountTemplate("1000", "Cash", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1200", "Accounts Receivable", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("2000", "Accounts Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("4000", "Sales Revenue", AccountType.REVENUE, "INCOME_STATEMENT"),
            // ... 200+ accounts
        ),
    ),
    bankingFormats = listOf(
        BankingFormat(
            formatId = "ACH_NACHA",
            name = "ACH NACHA Format",
            type = BankingFormatType.PAYMENT_FILE,
            fileExtension = "txt",
            specification = "NACHA",
        ),
    ),
    eInvoicingConfig = null, // US does not require e-invoicing
    auditFileFormat = null, // US does not require specific audit file
    localeConfig = LocaleConfig(
        language = "en",
        country = "US",
        currency = "USD",
        timezone = "America/Los_Angeles",
        dateFormat = "MM/dd/yyyy",
        numberFormat = NumberFormat(
            decimalSeparator = ".",
            thousandsSeparator = ",",
            currencyFormat = "$#,##0.00",
        ),
    ),
)
```

#### Tanzania Country Pack 🇹🇿 **[PRIMARY - EAC VALIDATION]**
```kotlin
val tanzaniaCountryPack = CountryPack(
    countryCode = "TZ",
    version = SemanticVersion(1, 0, 0),
    effectiveFrom = LocalDate.of(2026, 1, 1),
    status = CountryPackStatus.ACTIVE,
    taxRules = TaxRuleSet(
        rules = listOf(
            // Standard VAT 18%
            JurisdictionRule(
                jurisdiction = "TZ",
                taxType = "VAT",
                rate = 18.0,
                compoundTax = false,
            ),
            // Zero-rated items
            JurisdictionRule(
                jurisdiction = "TZ",
                taxType = "VAT_ZERO",
                rate = 0.0,
                applicableTo = listOf("EXPORTS", "BASIC_FOODS"),
            ),
            // Withholding Tax on Services
            JurisdictionRule(
                jurisdiction = "TZ",
                taxType = "WHT_SERVICES",
                rate = 5.0,
                applicableTo = listOf("CONSULTANCY", "PROFESSIONAL_SERVICES"),
            ),
            JurisdictionRule(
                jurisdiction = "TZ",
                taxType = "WHT_RENT",
                rate = 10.0,
                applicableTo = listOf("RENTAL_INCOME"),
            ),
            JurisdictionRule(
                jurisdiction = "TZ",
                taxType = "WHT_DIVIDENDS",
                rate = 10.0,
                applicableTo = listOf("DIVIDENDS"),
            ),
        ),
    ),
    coaTemplate = CoATemplate(
        templateId = UUID.randomUUID(),
        name = "Tanzania IFRS Chart of Accounts",
        accountingStandard = "IFRS",
        industryVertical = "GENERAL",
        accounts = listOf(
            GLAccountTemplate("1000", "Cash and Bank", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1010", "M-Pesa TZ", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1100", "Trade Receivables", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1200", "Inventory", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1300", "VAT Input", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("2000", "Trade Payables", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2100", "VAT Output", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("2110", "WHT Payable", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("4000", "Sales Revenue", AccountType.REVENUE, "INCOME_STATEMENT"),
            GLAccountTemplate("5000", "Cost of Sales", AccountType.EXPENSE, "INCOME_STATEMENT"),
            GLAccountTemplate("6000", "Operating Expenses", AccountType.EXPENSE, "INCOME_STATEMENT"),
            // ... 100+ accounts
        ),
    ),
    bankingFormats = listOf(
        BankingFormat(
            formatId = "SWIFT_TZ",
            name = "Tanzania SWIFT Payments",
            type = BankingFormatType.PAYMENT_FILE,
            fileExtension = "txt",
            specification = "SWIFT_MT101",
        ),
        BankingFormat(
            formatId = "MPESA_TZ_API",
            name = "M-Pesa Tanzania API",
            type = BankingFormatType.API_INTEGRATION,
            apiEndpoint = "https://openapi.m-pesa.com/",
            specification = "VODACOM_MPESA_API",
        ),
    ),
    eInvoicingConfig = EInvoicingConfig(
        format = "TRA_VFD_JSON",
        clearanceRequired = true, // Mandatory Virtual Fiscal Device
        clearanceEndpoint = "https://vfd.tra.go.tz/api/",
        digitalSignatureRequired = false,
        additionalFields = mapOf(
            "vfdSerialNumber" to "REQUIRED",
            "verificationCode" to "REQUIRED", // From TRA
        ),
    ),
    auditFileFormat = null,
    statutoryReports = listOf(
        StatutoryReportTemplate(
            reportId = "TZ_VAT_RETURN",
            name = "Tanzania VAT Return",
            filingFrequency = FilingFrequency.MONTHLY,
            format = "PDF",
            submissionMethod = SubmissionMethod.PORTAL_UPLOAD,
            apiEndpoint = "https://itax.tra.go.tz/",
        ),
    ),
    localeConfig = LocaleConfig(
        language = "sw", // Swahili
        country = "TZ",
        currency = "TZS", // Tanzanian Shilling
        timezone = "Africa/Dar_es_Salaam", // EAT (UTC+3)
        dateFormat = "dd/MM/yyyy",
        numberFormat = NumberFormat(
            decimalSeparator = ".",
            thousandsSeparator = ",",
            currencyFormat = "TZS #,##0.00",
        ),
    ),
    customIntegrations = listOf(
        CustomIntegration(
            integrationId = "TRA_VFD",
            name = "TRA Virtual Fiscal Device",
            type = IntegrationType.E_INVOICING,
            apiEndpoint = "https://vfd.tra.go.tz/api/",
            authMethod = AuthMethod.API_KEY,
            description = "Mandatory electronic fiscal device system",
        ),
        CustomIntegration(
            integrationId = "MPESA_VODACOM_TZ",
            name = "M-Pesa Tanzania (Vodacom)",
            type = IntegrationType.PAYMENT,
            apiEndpoint = "https://openapi.m-pesa.com/",
            authMethod = AuthMethod.OAUTH2,
            description = "M-Pesa mobile money (Vodacom Tanzania)",
        ),
    ),
)
```

**Tanzania-Specific Requirements**:
- ✅ **TRA VFD/EFD Integration**: Virtual/Electronic Fiscal Device (mandatory)
- ✅ **M-Pesa Tanzania**: Different API from Kenya (Vodacom vs Safaricom)
- ✅ **VAT 18%**: Higher than Kenya (16%)
- ✅ **EAC Harmonization**: Validates East African Community compliance

#### Germany Country Pack 🇩🇪 **[SECONDARY REFERENCE]**
```kotlin
val germanyCountryPack = CountryPack(
    countryCode = "DE",
    version = SemanticVersion(1, 0, 0),
    effectiveFrom = LocalDate.of(2026, 1, 1),
    status = CountryPackStatus.ACTIVE,
    taxRules = TaxRuleSet(
        rules = listOf(
            JurisdictionRule(
                jurisdiction = "DE",
                taxType = "VAT",
                rate = 19.0, // Standard VAT
                compoundTax = false,
            ),
            JurisdictionRule(
                jurisdiction = "DE",
                taxType = "VAT_REDUCED",
                rate = 7.0, // Reduced VAT (food, books)
                compoundTax = false,
            ),
            // Reverse charge for EU suppliers
            ReverseChargeRule(
                sourceCountry = "EU",
                destCountry = "DE",
                reverseCharge = true,
            ),
        ),
    ),
    coaTemplate = CoATemplate(
        templateId = UUID.randomUUID(),
        name = "SKR04",
        accountingStandard = "HGB",
        industryVertical = "MANUFACTURING",
        accounts = listOf(
            GLAccountTemplate("1000", "Kasse", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1200", "Forderungen aus Lieferungen und Leistungen", AccountType.ASSET, "BALANCE_SHEET"),
            GLAccountTemplate("1600", "Verbindlichkeiten aus Lieferungen und Leistungen", AccountType.LIABILITY, "BALANCE_SHEET"),
            GLAccountTemplate("8400", "Umsatzerlöse", AccountType.REVENUE, "INCOME_STATEMENT"),
            // ... 300+ accounts (SKR04 standard)
        ),
    ),
    bankingFormats = listOf(
        BankingFormat(
            formatId = "SEPA_XML",
            name = "SEPA Credit Transfer",
            type = BankingFormatType.PAYMENT_FILE,
            fileExtension = "xml",
            specification = "ISO 20022 pain.001.001.03",
        ),
    ),
    eInvoicingConfig = EInvoicingConfig(
        format = "PEPPOL-BIS-3.0",
        clearanceRequired = false, // Germany: optional but encouraged
        clearanceEndpoint = null,
        digitalSignatureRequired = false,
    ),
    auditFileFormat = AuditFileFormat(
        formatId = "GDPdU_DE",
        name = "GDPdU Export",
        type = AuditFileFormatType.COMPRESSED_CSV,
        fileExtension = "zip",
        specification = "GDPdU",
        retentionPeriodYears = 10,
    ),
    statutoryReports = listOf(
        StatutoryReportTemplate(
            reportId = "DE_VAT_RETURN",
            name = "UStVA (VAT Return)",
            filingFrequency = FilingFrequency.MONTHLY,
            format = "ELSTER-XML",
            submissionMethod = SubmissionMethod.API,
            apiEndpoint = "https://www.elster.de/eportal/",
        ),
    ),
    localeConfig = LocaleConfig(
        language = "de",
        country = "DE",
        currency = "EUR",
        timezone = "Europe/Berlin",
        dateFormat = "dd.MM.yyyy",
        numberFormat = NumberFormat(
            decimalSeparator = ",",
            thousandsSeparator = ".",
            currencyFormat = "#.##0,00 €",
        ),
    ),
)
```

**Validation**: Tax rules loaded correctly? CoA templates applied? SEPA payment file generated?

---

### 7. Workflow Engine (ADR-046 - Temporal.io)

#### PO Approval Workflow
```kotlin
// Workflow Definition
@WorkflowInterface
interface PurchaseOrderApprovalWorkflow {
    @WorkflowMethod
    fun approvePurchaseOrder(poId: UUID): ApprovalResult
}

@WorkflowImpl
class PurchaseOrderApprovalWorkflowImpl : PurchaseOrderApprovalWorkflow {

    private val poService = Workflow.newActivityStub(
        PurchaseOrderService::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .build()
    )

    override fun approvePurchaseOrder(poId: UUID): ApprovalResult {
        // Fetch PO
        val po = poService.getPurchaseOrder(poId)

        // Fetch approval schema (ADR-044)
        val approvalSchema = poService.getApprovalSchema(
            companyCodeId = po.companyCodeId,
            documentType = "PURCHASE_ORDER",
        )

        // Multi-level approval
        for (level in approvalSchema.levels.sortedBy { it.levelNumber }) {
            val approvers = poService.determineApprovers(
                level = level,
                amount = po.totalAmount,
                companyCodeId = po.companyCodeId,
            )

            if (approvers.isEmpty()) continue

            // Parallel approval within level
            val levelResult = if (level.approvalMode == ApprovalMode.ALL_REQUIRED) {
                approveAllRequired(poId, level, approvers)
            } else {
                approveAnyRequired(poId, level, approvers)
            }

            if (!levelResult.approved) {
                poService.rejectPurchaseOrder(poId, levelResult.reason)
                return ApprovalResult(approved = false, reason = levelResult.reason)
            }
        }

        // All levels approved
        poService.approvePurchaseOrder(poId)
        return ApprovalResult(approved = true)
    }

    private fun approveAllRequired(
        poId: UUID,
        level: ApprovalLevel,
        approvers: List<UUID>,
    ): LevelApprovalResult {
        val tasks = approvers.map { approverId ->
            Async.function { poService.createApprovalTask(poId, level.levelNumber, approverId) }
        }

        // Wait for all approvals (with timeout)
        val results = Async.await(
            tasks,
            Duration.ofDays(level.escalationTimeoutDays.toLong()),
        )

        val allApproved = results.all { it.approved }

        return if (allApproved) {
            LevelApprovalResult(approved = true)
        } else {
            val rejectedBy = results.filter { !it.approved }.map { it.approverId }
            LevelApprovalResult(approved = false, reason = "Rejected by: $rejectedBy")
        }
    }
}
```

**Validation**: Multi-level approval works? Escalation after timeout? Saga rollback on rejection?

---

### 8. Frontend UI (ADR-048)

#### Sales Order Creation Screen (React)
```tsx
// sales-mfe/src/pages/SalesOrderCreate.tsx
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button, FormField, CustomerSelect, DatePicker } from '@chiroerp/design-system';
import { useCreateSalesOrder } from '@/hooks/useSalesOrders';
import { salesOrderSchema } from '@/schemas/sales-order';

export const SalesOrderCreate = () => {
  const createMutation = useCreateSalesOrder();

  const form = useForm({
    resolver: zodResolver(salesOrderSchema),
    defaultValues: {
      orderDate: new Date(),
      paymentTerms: 'NET30',
      lineItems: [{ materialId: '', quantity: 1 }],
    },
  });

  const onSubmit = async (data: SalesOrderFormData) => {
    try {
      const order = await createMutation.mutateAsync(data);
      toast.success(`Order ${order.orderNumber} created successfully`);
      navigate(`/sales/orders/${order.id}`);
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">Create Sales Order</h1>

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FormField
          name="customerId"
          label="Customer"
          required
          render={({ field }) => (
            <CustomerSelect
              {...field}
              placeholder="Select customer"
            />
          )}
        />

        <FormField
          name="orderDate"
          label="Order Date"
          required
          render={({ field }) => <DatePicker {...field} />}
        />

        <FormField
          name="paymentTerms"
          label="Payment Terms"
          render={({ field }) => (
            <Select {...field}>
              <SelectOption value="NET30">Net 30 Days</SelectOption>
              <SelectOption value="NET60">Net 60 Days</SelectOption>
              <SelectOption value="IMMEDIATE">Immediate</SelectOption>
            </Select>
          )}
        />

        <LineItemsFieldArray name="lineItems" />

        <div className="flex gap-4">
          <Button type="submit" isLoading={createMutation.isLoading}>
            Create Order
          </Button>
          <Button variant="secondary" onClick={() => navigate('/sales/orders')}>
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
};
```

**Validation**: All O2C + P2P screens built? Mobile responsive? < 2.5s page load?

---

## Implementation Phases

### Phase 1: Infrastructure & Master Data (Months 1-2)
**Duration**: 8 weeks
**Team**: Platform Team (2 devs) + All hands (8 devs)

#### Week 1-2: Core Infrastructure
- [ ] Kubernetes cluster setup (Africa/Nairobi region for Kenya)
- [ ] Kafka cluster (3 brokers, replication factor 3)
- [ ] PostgreSQL (write databases for 10 domains)
- [ ] MongoDB (read model database with replicas)
- [ ] Redis (cache, master-slave)
- [ ] Temporal.io cluster (workflow engine)
- [ ] CI/CD pipelines (GitHub Actions)
- [ ] VPN/VPC for KRA eTIMS test environment connectivity

#### Week 3-4: Platform Services
- [ ] Authentication service (Keycloak)
- [ ] API Gateway (Kong)
- [ ] Configuration service (ADR-044)
- [ ] Notification service (Email/SMS for Kenya)
- [ ] Logging (ELK stack)
- [ ] Monitoring (Prometheus + Grafana + Jaeger)

#### Week 5-6: Master Data & Kenya Country Pack 🇰🇪
- [ ] **Kenya Country Pack Installation**:
  - [ ] Load tax rules (VAT 16%, WHT 5-10%, Withholding VAT 6%)
  - [ ] Load Kenya IFRS CoA template (100+ accounts)
  - [ ] Configure KRA eTIMS connection (test environment)
  - [ ] Configure M-Pesa Daraja API sandbox (OAuth2)
  - [ ] Configure PesaLink test endpoint
  - [ ] Configure RTGS Kenya test endpoint
- [ ] Create test tenant: **"Acme Kenya Ltd"**
  - [ ] Organization structure (HQ Nairobi, Branch Mombasa)
  - [ ] KRA PIN: A123456789X
  - [ ] VAT Registration: Yes
  - [ ] eTIMS Device Serial: TEST-KE-001
- [ ] Customer master (10 test customers with valid KRA PINs)
- [ ] Supplier master (10 test suppliers with KRA PINs)
- [ ] Product master:
  - [ ] 20 products (10 finished goods, 10 raw materials)
  - [ ] HS codes for import duty calculation
  - [ ] Excise duty flags (alcohol, tobacco)
- [ ] Chart of Accounts finalization for Acme Kenya Ltd

#### Week 7-8: Tanzania Country Pack 🇹🇿
- [ ] **Tanzania Country Pack Installation**:
  - [ ] Load tax rules (VAT 18%, WHT 5-10%)
  - [ ] Load Tanzania IFRS CoA template
  - [ ] Configure TRA VFD test environment
  - [ ] Configure M-Pesa Tanzania sandbox (Vodacom API)
- [ ] Create test tenant: **"Beta Tanzania Ltd"**
  - [ ] Organization structure (HQ Dar es Salaam)
  - [ ] TIN: 123-456-789
  - [ ] VFD Serial: TEST-TZ-001

**Milestone 1**: 🎯 Kenya Country Pack active with KRA eTIMS connectivity, M-Pesa sandbox working

---

### Phase 2: Kenya O2C 🇰🇪 (Month 3) **[HIGHEST PRIORITY]**
**Duration**: 4 weeks
**Team**: Team A (4 devs) + Platform (1 dev)
**Goal**: First customer demo-ready flow for Kenyan market

#### Week 9-10: Sales Order → Invoice
- [ ] **Sales Order Creation**:
  - [ ] Customer selection with KRA PIN validation
  - [ ] Product selection with HS code
  - [ ] Pricing with discounts (ADR-044 configuration)
  - [ ] **VAT 16% calculation** (Tax Engine ADR-047)
  - [ ] Multi-level approval (ADR-046 workflow)
  - [ ] Emit `SalesOrderCreatedEvent`
- [ ] **Credit Check**: Customer credit limit (Finance AR)
- [ ] **Inventory Reservation**: Available-to-Promise (ATP) check
- [ ] **Shipping**: Create delivery with tracking
- [ ] Frontend: Sales order creation screen (Swahili/English)

#### Week 11-12: KRA eTIMS Invoice & Payment
- [ ] **Invoice Generation**:
  - [ ] IFRS posting to GL (Finance GL ADR-009)
  - [ ] **KRA eTIMS submission** (mandatory):
    - [ ] Device serial number: TEST-KE-001
    - [ ] Internal data payload generation
    - [ ] Signature generation
    - [ ] **Cu number retrieval** (Control Unit number from KRA)
    - [ ] Handle eTIMS error responses
  - [ ] Invoice PDF with Cu number displayed
  - [ ] Receivable creation (Finance AR)
  - [ ] Emit `InvoiceCreatedEvent`
- [ ] **Payment Receipt**:
  - [ ] **M-Pesa C2B integration** (Daraja API):
    - [ ] OAuth2 token retrieval and refresh
    - [ ] Register C2B URL callback
    - [ ] Receive payment notification webhook
    - [ ] Automatic invoice application
  - [ ] PesaLink payment (manual entry)
  - [ ] RTGS Kenya payment (manual entry)
  - [ ] **Withholding Tax calculation** (2%/5%/10% for corporate customers)
  - [ ] Automatic GL posting (Cash + WHT Receivable)
  - [ ] Emit `PaymentReceivedEvent`
- [ ] Frontend: Invoice display with Cu number, payment receipt screen

#### Week 13: Dunning & Testing
- [ ] **Dunning Workflow** (Temporal ADR-046):
  - [ ] Automatic overdue detection (30 days)
  - [ ] Email/SMS notifications (Swahili/English templates)
  - [ ] Escalation rules (1st notice, 2nd notice, collection)
  - [ ] Frontend: Dunning management screen
- [ ] **End-to-End Testing**:
  - [ ] 5 complete O2C cycles (Kenya)
  - [ ] KRA eTIMS Cu number validation
  - [ ] M-Pesa C2B payment simulation (sandbox)
  - [ ] Withholding tax scenarios (5%, 10%)
  - [ ] Performance test: 100 orders/hour

**Milestone 2**: 🎯 **Kenyan customer can see working O2C with eTIMS compliance + M-Pesa payments**

---

### Phase 3: Tanzania O2C 🇹🇿 (Month 4)
**Duration**: 4 weeks
**Team**: Team A (4 devs) + Platform (1 dev)
**Goal**: Validate EAC harmonization and fiscal device diversity

#### Week 14-15: Sales Order → Invoice (Tanzania)
- [ ] Sales order creation with TIN validation
- [ ] **VAT 18% calculation** (Tanzania rate)
- [ ] Inventory reservation (same logic as Kenya)
- [ ] Shipping with **EAC customs documentation**
- [ ] Frontend: Swahili localization

#### Week 16-17: TRA VFD Invoice & Payment
- [ ] **Invoice Generation**:
  - [ ] **TRA VFD integration** (Virtual Fiscal Device):
    - [ ] VFD serial number: TEST-TZ-001
    - [ ] **Verification code retrieval** from TRA
    - [ ] Fiscal signature generation
  - [ ] Invoice with verification code
  - [ ] IFRS posting to GL
  - [ ] Emit `InvoiceCreatedEvent`
- [ ] **Payment Receipt**:
  - [ ] **M-Pesa Tanzania integration** (Vodacom API)
  - [ ] SWIFT payments (Tanzanian banks)
  - [ ] Automatic GL posting
  - [ ] Emit `PaymentReceivedEvent`

#### Week 18: Testing
- [ ] 5 complete O2C cycles (Tanzania)
- [ ] TRA VFD verification code validation
- [ ] EAC customs documentation accuracy
- [ ] Cross-country test: Kenya HQ, Tanzania branch

**Milestone 3**: 🎯 TRA VFD invoice working, validates EAC patterns scale across East Africa

---

### Phase 4: Kenya P2P 🇰🇪 (Month 5)
**Duration**: 4 weeks
**Team**: Team B (4 devs) + Platform (1 dev)
**Goal**: Complete Kenya coverage with procurement flow

#### Week 19-20: Purchase Requisition → PO
- [ ] **Purchase Requisition creation**
- [ ] **Multi-level approval** (ADR-046 workflow):
  - [ ] Manager approval
  - [ ] Director approval (>KES 500,000)
  - [ ] CFO approval (>KES 2,000,000)
  - [ ] Frontend: Task Inbox (ADR-046)
- [ ] **Purchase Order** with supplier (KRA PIN validation)
- [ ] Email PO to supplier
- [ ] Emit `PurchaseOrderApprovedEvent`

#### Week 21-22: Invoice Verification & Payment
- [ ] **Goods Receipt**: 3-way match (PO, GR, Invoice)
- [ ] **Supplier Invoice Verification**:
  - [ ] **KRA eTIMS validation** (verify Cu number on supplier invoice)
  - [ ] **Withholding VAT calculation** (6% for non-VAT registered suppliers)
  - [ ] **Import duty calculation** (HS code-based, 0-35%)
  - [ ] IFRS posting to GL (Inventory + VAT Input + Import Duty + WHT Payable)
  - [ ] Emit `InvoiceVerifiedEvent`
- [ ] **Payment Processing**:
  - [ ] **M-Pesa B2B** (for small suppliers <KES 100,000)
  - [ ] PesaLink (interbank, KES 100,000 - KES 1,000,000)
  - [ ] RTGS Kenya (large payments >KES 1,000,000)
  - [ ] **Withholding tax deduction** (5-10%)
  - [ ] Automatic GL posting (Bank + Payables + WHT Payable)
  - [ ] Emit `PaymentProcessedEvent`
- [ ] Frontend: Invoice verification screen, payment proposal screen

#### Week 23-24: Testing
- [ ] 5 complete P2P cycles (Kenya)
- [ ] Import duty scenarios (imported goods with HS codes)
- [ ] Withholding VAT scenarios (non-registered suppliers)
- [ ] M-Pesa B2B payment simulation (sandbox)
- [ ] Performance test: 500 POs/hour

**Milestone 4**: 🎯 **Complete Kenya O2C + P2P ready for enterprise customer pitch ($5M deal)**

---

### Phase 5: Tanzania P2P 🇹🇿 (Month 6)
**Duration**: 2 weeks
**Team**: Team B (4 devs)

#### Week 25-26: Tanzania P2P
- [ ] Purchase Requisition to PO (Tanzania)
- [ ] Goods Receipt
- [ ] Supplier invoice verification (TRA VFD validation)
- [ ] Payment via M-Pesa Tanzania / SWIFT
- [ ] **EAC customs documentation** (cross-border procurement)
- [ ] Testing: 5 complete P2P cycles (Tanzania)

**Milestone 5**: 🎯 Complete Tanzania P2P, validates EAC compliance

---

### Phase 6: Testing & Documentation (Month 6)
**Duration**: 2 weeks
**Team**: All hands (8 devs)

#### Week 27-28: Final Validation
- [ ] **Regression Testing**:
  - [ ] 20 O2C cycles (10 Kenya, 10 Tanzania)
  - [ ] 20 P2P cycles (10 Kenya, 10 Tanzania)
  - [ ] Cross-country scenarios (Kenya HQ, Tanzania branch)
- [ ] **Performance Testing**:
  - [ ] 1,000 orders/hour (Kenya)
  - [ ] 10,000 invoices/day (Kenya + Tanzania)
  - [ ] KRA eTIMS Cu number retrieval latency (< 2 seconds)
  - [ ] M-Pesa API rate limit testing (sandbox)
  - [ ] Database query optimization
- [ ] **Security Testing**:
  - [ ] Penetration testing (OWASP Top 10)
  - [ ] KRA eTIMS API key protection
  - [ ] M-Pesa OAuth2 token refresh security
  - [ ] SQL injection, XSS, CSRF tests
- [ ] **Documentation**:
  - [ ] **Kenya deployment guide**:
    - [ ] KRA eTIMS device registration process
    - [ ] M-Pesa Daraja API production certification
    - [ ] PesaLink/RTGS bank setup
  - [ ] User training materials (Swahili/English)
  - [ ] API documentation (all 72 modules)
  - [ ] System administration guide
  - [ ] Troubleshooting guide (eTIMS errors, M-Pesa failures)

**Milestone 6**: 🎯 **Production-ready system for Kenya + Tanzania, ready for first customer deployment**

---

### Phase 7: US & Germany Validation (Optional - Post-Launch)
**Duration**: 2 weeks each country
**Team**: 2 devs

#### Week 29-30: US O2C + P2P (Simplified)
- [ ] Install US Country Pack (Sales Tax, ACH, US-GAAP)
- [ ] Run O2C + P2P (no new development, just configuration)
- [ ] Testing: 10 cycles each

#### Week 31-32: Germany O2C + P2P (Simplified)
- [ ] Install Germany Country Pack (VAT 19%, PEPPOL, SKR04)
- [ ] Run O2C + P2P (no new development, just configuration)
- [ ] Testing: 10 cycles each

**Milestone 7**: 🎯 **Prove ChiroERP scales globally (not just Africa)**

#### Week 4: Payment Execution
- [ ] Payment proposal generation
- [ ] ACH payment file generation (NACHA format)
- [ ] Payment execution API (Treasury)
- [ ] GL posting (AP, cash)
- [ ] Frontend: Payment proposal, Payment run screen

**Milestone 4**: US P2P complete (PR → PO → GR → Invoice → Payment)

---

### Phase 5: Procure-to-Pay (Germany) (Month 6)

#### Week 1-2: Purchase Order (Germany)
- [ ] PO with EUR currency
- [ ] Multi-level approval (German org structure)
- [ ] SKR04 posting rules
- [ ] Frontend: German localization

#### Week 3: VAT Reverse Charge
- [ ] EU supplier detection
- [ ] VAT reverse charge logic
- [ ] Posting: Input tax = Output tax (self-assessment)
- [ ] Frontend: VAT reverse charge indicator

#### Week 4: SEPA Payment + GDPdU Export
- [ ] SEPA credit transfer XML (ISO 20022)
- [ ] Payment file generation
- [ ] GDPdU audit file export (10-year retention)
- [ ] Frontend: SEPA payment status, Audit file download

**Milestone 5**: Germany P2P complete (including VAT reverse charge + SEPA)

---

### Phase 6: Testing & Documentation (Month 6)

#### Week 1: Integration Testing
- [ ] End-to-end O2C tests (US + Germany)
- [ ] End-to-end P2P tests (US + Germany)
- [ ] Event flow testing (Kafka)
- [ ] Workflow testing (Temporal)
- [ ] CQRS consistency testing (write vs read)

#### Week 2: Performance Testing
- [ ] Load test: 100 concurrent users
- [ ] Load test: 1,000 sales orders/hour
- [ ] Load test: 10,000 invoice postings/day
- [ ] Database query optimization
- [ ] Kafka consumer lag monitoring

#### Week 3-4: Documentation
- [ ] API documentation (Swagger/OpenAPI)
- [ ] User guides (O2C + P2P)
- [ ] Admin guides (country pack setup)
- [ ] Developer guides (add new country)
- [ ] Architecture decision records update

**Milestone 6**: Reference implementation complete, tested, documented

---

## Success Metrics

### Functional Completeness
- ✅ O2C flow works in US (Sales → Invoice → Payment → Dunning)
- ✅ O2C flow works in Germany (including PEPPOL e-invoice)
- ✅ P2P flow works in US (PR → PO → GR → Invoice → Payment)
- ✅ P2P flow works in Germany (including VAT reverse charge + SEPA)
- ✅ All 10 domains integrated via Kafka events
- ✅ All UI screens functional (React micro-frontends)

### Technical Validation
- ✅ Microservices communicate via REST + Kafka
- ✅ CQRS read models updated < 1 second
- ✅ Configuration framework working (no code changes for pricing)
- ✅ Organizational model supports multi-country
- ✅ Workflow engine executes multi-level approvals
- ✅ Country packs loaded correctly (US + Germany)

### Performance
- ✅ Sales order creation < 500ms (p95)
- ✅ Invoice posting < 1s (p95)
- ✅ Pricing calculation (10 steps) < 100ms (p95)
- ✅ Workflow start latency < 100ms (p95)
- ✅ UI page load < 2.5s (LCP)

### Quality
- ✅ 80%+ unit test coverage
- ✅ 100% critical path integration tests
- ✅ Zero P0 bugs in O2C + P2P flows
- ✅ WCAG 2.1 AA compliance (accessibility)

---

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Kafka event ordering issues** | Medium | High | Use partition keys (tenantId + orderId), idempotent consumers |
| **CQRS eventual consistency bugs** | High | Medium | Add consistency checks, fallback to write DB if stale |
| **Temporal workflow version conflicts** | Low | High | Test workflow versioning, use non-breaking changes |
| **PEPPOL integration delays** | Medium | Medium | Mock PEPPOL AP for testing, integrate real AP in Phase 2 |
| **Performance bottlenecks** | High | Medium | Load test early (Week 10), optimize queries, add caching |
| **Team knowledge gaps** | Medium | Medium | Pair programming, architecture reviews, training |

---

## Team Structure

### Team A: Order-to-Cash (4 engineers)
- **Tech Lead** (1): Architecture, Kafka, CQRS
- **Backend Devs** (2): Sales, Finance AR, Inventory services
- **Frontend Dev** (1): React screens (Order, Invoice, Payment)

### Team B: Procure-to-Pay (4 engineers)
- **Tech Lead** (1): Workflow engine, approvals
- **Backend Devs** (2): Procurement, Finance AP, Treasury services
- **Frontend Dev** (1): React screens (PO, GR, Invoice Verification)

### Platform Team (2 engineers)
- **DevOps** (1): Infrastructure, CI/CD, monitoring
- **Platform Dev** (1): Auth, Config, Country Packs, MDM

---

## Deliverables

### Code
- [ ] 10 microservices (Sales, Finance GL/AR/AP, Inventory, Procurement, Treasury, MDM, Config, Workflow)
- [ ] 2 country packs (US, Germany)
- [ ] 15+ React screens (O2C + P2P)
- [ ] 50+ REST API endpoints
- [ ] 20+ Kafka event types
- [ ] 5+ Temporal workflows

### Documentation
- [ ] API documentation (Swagger)
- [ ] User guides (20 pages)
- [ ] Admin guides (15 pages)
- [ ] Architecture diagrams (10 diagrams)
- [ ] Developer guides (25 pages)

### Test Artifacts
- [ ] 500+ unit tests
- [ ] 50+ integration tests
- [ ] 10+ E2E tests (Playwright)
- [ ] Load test results
- [ ] Accessibility audit report

---

## Post-Implementation: Scaling to 50 Countries

Once US + Germany work, scaling to 50 countries is **data-driven** (not code):

### Adding France (Example)
1. **Create France Country Pack** (ADR-047)
   - VAT 20%/10%/5.5%
   - FEC audit file format
   - SEPA payments (same as Germany)
   - French CoA (PCG)
2. **Setup France Company Code** (ADR-045)
   - FR01 company code
   - Plant in Paris
   - Sales org for France
3. **Test O2C + P2P** (1 week)
4. **Deploy** (no code changes)

**Effort**: 2 weeks (vs 6 months for reference implementation)

---

## Conclusion

This reference implementation **validates the entire ChiroERP architecture** with:
- ✅ Two critical business flows (O2C + P2P)
- ✅ Two countries with different tax/payment/audit requirements (US + Germany)
- ✅ 10 microservices integrated via REST + Kafka
- ✅ Configuration framework, org model, workflow engine, country packs
- ✅ Full-stack (backend + frontend)
- ✅ Production-grade quality (80% test coverage, performance targets met)

**If this works, ChiroERP can scale to 72 modules × 50 countries with confidence.**

**Next Steps**:
1. Approve this implementation plan
2. Allocate 8-10 engineers for 6 months
3. Execute Phases 1-6
4. Demo to early customers (Month 7)
5. Iterate based on feedback
6. Scale to remaining modules + countries (12-18 months)

**Total Investment**: $600K-$800K (6 months × 10 engineers × $10K-$13K/engineer/month)
**Expected ROI**: First $5M deal closed with O2C + P2P demo (3-6 months post-implementation)
