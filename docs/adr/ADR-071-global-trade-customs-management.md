# ADR-071: Global Trade & Customs Management

**Status**: Planned (P3 - 2028)  
**Date**: 2026-02-06  
**Decision Makers**: CTO, VP Product, Head of Supply Chain  
**Consulted**: Customers (importers/exporters), customs brokers, trade compliance experts  
**Informed**: Sales, customer success, implementation teams

---

## Context

### Business Problem

Companies with $100M+ revenue doing international trade (import/export) face complex customs compliance requirements:

- **Customs Declarations**: CBP Form 7501 (US imports), EU SAD (Single Administrative Document), Canada B3
- **HS Code Classification**: 10-digit Harmonized System codes (manual classification = 30 min per SKU, error-prone)
- **Free Trade Agreements (FTA)**: USMCA, EU FTAs, CPTPP → 5-15% duty savings if qualified
- **Duty Drawback**: 99% duty refund on re-exported goods (complex tracking, manual claims = months)
- **Trade Sanctions**: OFAC, BIS Entity List → penalties $10K-$100K for violations
- **Country of Origin**: Rules of Origin (ROO) tracking for FTA qualification

Without automated global trade management, companies face:
- **Manual customs declarations** (30-60 min per shipment, error rate 5-10%)
- **Missed FTA opportunities** (lose 5-10% duty savings, $500K-$5M annual)
- **Duty miscalculations** (overpayment or underpayment → penalties)
- **Sanctions violations** (ship to denied parties → fines, shipment delays)
- **Slow duty drawback** (manual tracking = 6-12 months for 99% refund)

### Current State (2026)

**Existing Capabilities**:
- **ADR-023 (Procurement)**: Purchase orders, supplier management (no customs integration)
- **ADR-025 (Sales Order Management)**: Sales orders, invoicing (no export compliance)
- **ADR-024 (Inventory)**: Stock tracking (no country of origin, HS codes)

**Limitations**:
- No customs declaration generation
- No HS code classification (manual Excel lookup)
- No FTA qualification (manual certificate of origin)
- No duty calculation (customs broker estimates)
- No sanctions screening (manual checks, if at all)
- No duty drawback tracking (manual Excel, claims take 6-12 months)

### Market Opportunity

**Target Market**:
- Manufacturing companies $100M+ revenue exporting/importing (30% of ChiroERP manufacturing customers)
- Retail/distribution $100M+ revenue importing from Asia (15% of ChiroERP retail customers)
- Food & beverage, electronics, automotive, industrial equipment

**US Import/Export Market**:
- **$3.2T imports** (2024): China $500B, Mexico $400B, Canada $350B, EU $500B
- **$2.1T exports** (2024): Canada $300B, Mexico $280B, China $150B, EU $400B

**Competitive Landscape**:
| Vendor | Product | Investment | Capabilities | Gap vs ChiroERP |
|--------|---------|------------|--------------|-----------------|
| **SAP** | SAP GTS (Global Trade Services) | $1.5M-3M | Customs mgmt, sanctions, FTA, duty drawback | ✅ Feature parity target |
| **Oracle** | Oracle Global Trade Management | $1M-2.5M | HS classification, compliance, trade docs | ✅ Feature parity |
| **Microsoft** | Dynamics 365 Trade & Logistics | $500K-1.5M | Basic customs, HS codes | ✅ Superior features |
| **Amber Road** | Amber Road GTM (now E2open) | $300K-1M | Cloud GTM, FTA management | ✅ Native ERP integration |

**Customer ROI**:
- **Duty Savings**: 5-15% (FTA qualification, USMCA/EU/CPTPP = $500K-$5M annual for $100M trade volume)
- **Customs Declaration Time**: -70% (30 min manual → 9 min automated)
- **Duty Drawback Recovery**: 99% refund in 3-6 months (vs 6-12 months manual, 80% recovery)
- **Penalty Reduction**: <1 violation/year (vs 3-5 manual, $10K-$100K penalties)
- **HS Code Classification**: -75% time (30 min → 7 min with ML 80% accuracy)

---

## Decision

### Selected Approach: Native Global Trade Module with ML HS Classification

Build **native global trade & customs management** integrated with ChiroERP procurement and sales, including:

1. **Import/Export Compliance**: Customs declarations (CBP 7501, EU SAD, Canada B3), commercial invoices, packing lists
2. **HS Code Classification**: ML auto-classification (product description → HS code 80% accuracy)
3. **Free Trade Agreements (FTA)**: USMCA, EU, CPTPP qualification + certificate of origin generation
4. **Duty Calculation**: Import duty (ad valorem, specific, compound), VAT/GST, anti-dumping
5. **Country of Origin Tracking**: Rules of Origin (ROO) with BOM rollup (material traceability)
6. **Duty Drawback**: Track imported materials → exported finished goods → 99% refund claims
7. **Trade Sanctions Screening**: Real-time OFAC, BIS DPL, EU sanctions screening

### Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│             Global Trade Module (New)                             │
├──────────────────────────────────────────────────────────────────┤
│  HS Code        FTA           Duty          Sanctions    Drawback │
│  Classifier     Engine        Calculator    Screener     Tracker  │
│  (ML 80%        (USMCA,       (tariff       (OFAC,       (import  │
│   accuracy)     EU, CPTPP)    rates)        BIS DPL)     → export)│
└────────────┬───────────────────────────────────────────────────┬─┘
             │                                                   │
    ┌────────▼────────┬────────────┬────────────┐       ┌───────▼─────┐
    │                 │            │            │       │             │
┌───▼───┐      ┌──────▼─────┐ ┌───▼────┐  ┌───▼────┐  │ Commercial  │
│Procure│      │   Sales    │ │Invento │  │Product │  │   Invoice   │
│ ment  │      │   Orders   │ │  ry    │  │ Master │  │  + Packing  │
│(ADR-23│      │ (ADR-025)  │ │(ADR-24)│  │        │  │    List     │
└───────┘      └────────────┘ └────────┘  └────────┘  └─────────────┘
```

---

## Implementation Details

### Domain Models (New Bounded Context: `global-trade`)

#### Core Entities

| Entity | Attributes | Purpose |
|--------|-----------|---------|
| **HSCode** | hsCode (10-digit), description, tariffRate (%), country, effectiveDate, expiresDate | Harmonized System code master data |
| **CustomsDeclaration** | declarationId, type (import/export), shipmentId, customsOffice, declarationDate, status (draft/filed/cleared/rejected), documents | Customs declaration (CBP 7501, EU SAD) |
| **FTAAgreement** | agreementId, name (USMCA/EU/CPTPP), countries[], rulesOfOrigin (RVC%, tariff shift, de minimis), certificateTemplate | Free trade agreement metadata |
| **CountryOfOrigin** | productId, originCountry, qualificationBasis (wholly obtained/substantial transformation/RVC), supportingDocs | Product origin tracking |
| **DutyDrawbackClaim** | claimId, importEntryId, exportShipmentId, dutyPaid, dutyRefund (99%), claimDate, status (pending/approved/paid) | Duty drawback 99% refund |
| **SanctionsList** | listType (OFAC/BIS DPL/EU), entityName, address, country, addedDate, reason | Denied parties list |
| **TradeDocument** | documentId, type (commercial invoice/packing list/certificate of origin), shipmentId, generatedDate, pdfUrl | Trade documentation |

#### Domain Events

| Event | Payload | Purpose |
|-------|---------|---------|
| `HSCodeClassifiedEvent` | productId, hsCode, confidence (ML score), method (manual/auto) | HS code assigned to product |
| `CustomsDeclarationFiledEvent` | declarationId, customsOffice, filedDate, estimatedClearanceDate | Declaration submitted to customs |
| `FTAQualificationDeterminedEvent` | productId, agreementId, qualified (yes/no), reason | FTA qualification result |
| `SanctionViolationDetectedEvent` | entityName, entityType (customer/vendor), sanctionsList, blockedTransactionId | Denied party detected |
| `DutyDrawbackClaimSubmittedEvent` | claimId, dutyPaid, estimatedRefund, submittedDate | Drawback claim filed |

---

### Global Trade Capabilities

#### 1. HS Code Classification

**Problem**: Manual HS code classification = 30 min per SKU, 10-15% error rate.

**Capabilities**:
- **HS Code Master Data**: 200,000+ HS codes (2022 nomenclature), country-specific tariff rates
- **ML Auto-Classification**: Product description → HS code (80% accuracy, 2 min review vs 30 min manual)
- **Manual Override**: Allow users to override ML classification (store feedback for retraining)
- **Tariff Rate Lookup**: Auto-populate tariff rate (%) by HS code + country
- **HS Code Versioning**: Track HS code changes (2022 → 2027 nomenclature updates)

**ML Model**:
- **Training Data**: 100K+ product descriptions + HS codes (historical customs declarations, tariff databases)
- **Algorithm**: Text classification (BERT embeddings + XGBoost classifier)
- **Input**: Product description (name, category, material, usage)
- **Output**: Top 5 HS codes with confidence scores (e.g., 8517.12.00 @ 85%, 8517.18.00 @ 10%)

**Success Metrics**:
- ML accuracy: 80%+ (top-1 prediction correct)
- Classification time: 2 min (review ML suggestion) vs 30 min manual
- Error rate: <5% (vs 10-15% manual)

---

#### 2. Import Compliance (Customs Declarations)

**Problem**: Manual customs declarations = 30-60 min per shipment, 5-10% error rate.

**Capabilities**:
- **CBP Form 7501 (US Imports)**: Auto-generate from purchase order (supplier, HS code, value, origin country)
- **EU SAD (Single Administrative Document)**: Auto-generate for EU imports
- **Canada B3 (Customs Coding Form)**: Auto-generate for Canada imports
- **Commercial Invoice**: Auto-generate (supplier invoice + customs fields: HS code, origin, value)
- **Packing List**: Auto-generate (SKUs, quantities, weight, dimensions)
- **Customs Broker Integration**: EDI transmission to customs broker (ACE - Automated Commercial Environment)

**Workflow**:
1. **Purchase Order Created** (ADR-023) → trigger customs declaration
2. **HS Code Assigned** (ML classification or manual)
3. **Duty Calculated** (HS code + origin country → tariff rate)
4. **Declaration Generated** (CBP 7501/EU SAD/Canada B3)
5. **Review & Submit** (user review, transmit to customs broker via EDI)
6. **Customs Clearance** (customs broker files, receive clearance notice)

**Success Metrics**:
- Declaration generation time: 9 min (vs 30 min manual)
- Error rate: <2% (vs 5-10% manual)
- Clearance time: <24 hours (vs 2-3 days manual errors)

---

#### 3. Free Trade Agreements (FTA) & Certificate of Origin

**Problem**: Companies miss 5-15% duty savings by not using FTA (complex qualification rules).

**Capabilities**:
- **USMCA (US-Mexico-Canada Agreement)**: Regional Value Content (RVC) 75%, tariff shift rules, de minimis 10%
- **EU FTAs**: Pan-Euro-Med cumulation, EUR.1 certificate, supplier declarations
- **CPTPP (Comprehensive and Progressive Agreement for Trans-Pacific Partnership)**: 11 Asia-Pacific countries
- **RCEP (Regional Comprehensive Economic Partnership)**: 15 ASEAN+5 countries
- **Certificate of Origin Generation**: USMCA certificate, EUR.1, Form A (GSP)

**FTA Qualification Engine**:
- **Rules of Origin (ROO)**: Check tariff shift rule (change in HS chapter/heading/subheading)
- **Regional Value Content (RVC)**: Calculate RVC % (value of regional materials / total cost)
- **De Minimis**: Check non-originating materials <10% (USMCA), <15% (EU)
- **Material Traceability**: BOM rollup (track material origin through manufacturing)

**Example (USMCA RVC Calculation)**:
```
Product: Automotive Component (HS 8708.99)
Total Cost: $100
  - Regional Materials (US/Mexico/Canada): $80
  - Non-Regional Materials (China): $20
RVC = $80 / $100 = 80% ✅ Qualifies (RVC threshold 75%)
Duty Savings: 5% × $100 = $5 per unit
Annual Savings (100K units): $500K
```

**Success Metrics**:
- FTA qualification rate: 70-85% of eligible shipments (vs 30-50% manual)
- Duty savings: 5-15% (USMCA average 8%, EU 10%, CPTPP 12%)
- Certificate generation time: 5 min (vs 30 min manual)

---

#### 4. Export Compliance & Documentation

**Problem**: Manual export docs = 30 min per shipment, missing required fields → shipment delays.

**Capabilities**:
- **Commercial Invoice**: Auto-generate (customer, products, HS codes, origin, value, Incoterms)
- **Packing List**: Auto-generate (SKUs, quantities, weight, dimensions, pallet count)
- **Certificate of Origin**: USMCA, EUR.1, Form A (preferential origin)
- **Export License Check**: EAR (Export Administration Regulations), ITAR (International Traffic in Arms Regulations)
- **Shipper's Letter of Instruction (SLI)**: Instructions to freight forwarder

**Success Metrics**:
- Export doc generation time: 8 min (vs 30 min manual)
- Error rate: <2% (vs 8-12% manual missing fields)
- Shipment delays: -50% (correct docs first time)

---

#### 5. Duty Calculation

**Problem**: Manual duty calculation = estimates from customs broker, no visibility.

**Capabilities**:
- **Tariff Rate Database**: 200K+ HS codes × 200 countries = 40M tariff rates
- **Duty Types**: Ad valorem (%), specific ($/unit), compound (% + $/unit)
- **VAT/GST Calculation**: EU VAT (19-27%), UK VAT (20%), Canada GST (5%), Australia GST (10%)
- **Anti-Dumping Duties**: China imports (US anti-dumping 25-200%)
- **Safeguard Duties**: Steel/aluminum (Section 232: 25% steel, 10% aluminum)

**Duty Calculation Formula**:
```
Import Value: $10,000 (FOB)
HS Code: 8471.30.01 (computers)
Origin: China
Tariff Rate: 0% (MFN duty-free)
Anti-Dumping: 0% (not applicable)
VAT (if EU): 19% × ($10,000 + $0 duty) = $1,900
Total Duty + VAT: $1,900
```

**Success Metrics**:
- Duty calculation accuracy: 98%+ (vs 85-90% manual estimates)
- Visibility: Real-time duty cost in purchase order

---

#### 6. Duty Drawback

**Problem**: Manual duty drawback = 6-12 months, 80% recovery rate (vs 99% eligible).

**Capabilities**:
- **Import Entry Tracking**: Track duty paid on imports (CBP Form 7501, entry number, duty paid)
- **Export Tracking**: Track exported finished goods (HS code, quantity, export date)
- **Material Traceability**: Link imported materials → exported finished goods (BOM rollup)
- **Drawback Claim Generation**: CBP Form 7551 (unused merchandise drawback), 99% duty refund
- **Drawback Types**: Unused merchandise (same condition), manufacturing drawback (transformed), rejected merchandise

**Example (Unused Merchandise Drawback)**:
```
Imported Material: Steel coil from Germany
Duty Paid: $50,000 (10% × $500,000)
Exported Finished Good: Automotive component to Mexico
Drawback Claim: 99% × $50,000 = $49,500 refund
Claim Time: 3-6 months (vs 6-12 months manual)
```

**Success Metrics**:
- Drawback recovery rate: 99% (vs 80% manual)
- Claim cycle time: 3-6 months (vs 6-12 months manual)
- Annual drawback recovered: $500K-$2M (for $100M import/export volume)

---

#### 7. Trade Sanctions Screening

**Problem**: Shipping to sanctioned parties = $10K-$100K penalties, shipment seizure.

**Capabilities**:
- **Real-Time Screening**: Customer/vendor screening at transaction time (sales order, purchase order)
- **Sanctioned Lists**: OFAC (Office of Foreign Assets Control), BIS DPL (Denied Parties List), EU sanctions, UN sanctions
- **Entity Matching**: Name matching (fuzzy logic: 85%+ similarity = match), address matching, country matching
- **Manual Review Workflow**: Flagged entities → compliance officer review → approve/block
- **Audit Trail**: Log all screening results (timestamp, entity, match score, decision)

**Sanctioned Lists**:
- **OFAC SDN (Specially Designated Nationals)**: Russia, Iran, North Korea, Cuba, Venezuela (~10,000 entities)
- **BIS DPL (Denied Parties List)**: Export violations (~1,000 entities)
- **BIS Entity List**: National security concerns (China tech companies ~600 entities)
- **EU Sanctions**: Russia, Belarus, Syria, Iran (~2,000 entities)

**Success Metrics**:
- Screening coverage: 100% (all customers/vendors screened)
- False positive rate: <5% (fuzzy matching tuned to 85% similarity)
- Violation reduction: <1 per year (vs 2-5 manual)

---

## Technology Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Backend** | Quarkus 3.x + Kotlin | Existing ChiroERP stack |
| **HS Code Classifier** | Python 3.11 + BERT + XGBoost | Text classification (product description → HS code) |
| **Tariff Rate Database** | PostgreSQL | 40M+ tariff rates (HS code × country) |
| **FTA Rules Engine** | Kotlin + Drools (rules engine) | Complex ROO rules (tariff shift, RVC, de minimis) |
| **Sanctions Screening** | Elasticsearch | Fuzzy name matching (85%+ similarity) |
| **PDF Generation** | Apache PDFBox | Commercial invoice, packing list, certificate of origin |
| **EDI Integration** | EDI 214/856 (customs broker) | ACE (Automated Commercial Environment) transmission |

---

## Integration Points

### Existing ChiroERP Modules

| Module | Integration | Purpose |
|--------|-------------|---------|
| **Procurement (ADR-023)** | Purchase order → HS code assignment, duty calculation, customs declaration | Import compliance |
| **Sales Orders (ADR-025)** | Sales order → commercial invoice, packing list, certificate of origin | Export compliance |
| **Inventory (ADR-024)** | Product master → HS code, country of origin, BOM rollup (material traceability) | FTA qualification |
| **Finance (FI-GL)** | Duty paid (import) → drawback claim (export) → duty refund posting | Duty drawback accounting |

### External Systems

| System | Integration Method | Purpose |
|--------|-------------------|---------|
| **Customs Broker** | EDI 214/856 (shipment status) | Transmit customs declarations (CBP 7501, EU SAD) |
| **Freight Forwarder** | API/EDI | Shipper's Letter of Instruction (SLI), tracking |
| **Tariff Rate Database** | API (WCO, USITC, EU TARIC) | HS code + tariff rate updates (quarterly) |
| **OFAC/BIS Sanctions** | API (Treasury.gov, BIS.gov) | Daily sanctions list updates |
| **Certificate of Origin Services** | API (ACE, CBSA, ASEAN Single Window) | Electronic certificate submission |

---

## Success Metrics & KPIs

### Compliance & Accuracy

| Metric | Baseline (Manual) | Target (Automated) | Improvement |
|--------|-------------------|--------------------| ------------|
| **Customs Declaration Accuracy** | 90-95% (5-10% errors) | 98%+ | +3-8% |
| **HS Code Classification Accuracy** | 85-90% | 95%+ (ML 80% + manual review) | +5-10% |
| **FTA Qualification Rate** | 30-50% (eligible shipments) | 70-85% | +40% |
| **Sanctions Screening Coverage** | 50-70% (manual spot checks) | 100% (real-time) | +30-50% |
| **Duty Calculation Accuracy** | 85-90% (broker estimates) | 98%+ | +8-13% |

### Efficiency

| Metric | Baseline (Manual) | Target (Automated) | Improvement |
|--------|-------------------|--------------------|-------------|
| **Customs Declaration Time** | 30 min/shipment | 9 min/shipment | -70% |
| **HS Code Classification Time** | 30 min/SKU | 7 min/SKU (ML + review) | -77% |
| **Commercial Invoice Generation** | 20 min/shipment | 3 min/shipment | -85% |
| **Certificate of Origin Time** | 30 min/shipment | 5 min/shipment | -83% |
| **Duty Drawback Claim Time** | 6-12 months | 3-6 months | -50% |

### Business Impact

| Metric | Target | Annual Savings (Example: $100M Trade Volume) |
|--------|--------|----------------------------------------------|
| **Duty Savings (FTA)** | 5-15% | $500K-$5M (assume 10% avg, $50M dutiable) |
| **Duty Drawback Recovery** | 99% (vs 80%) | $200K-$500K (assume $1M duty paid, 20% exported) |
| **Penalty Reduction** | <1 violation/year | $10K-$50K (vs 3-5 violations @ $10K-$50K each) |
| **Customs Clearance Speed** | -50% delays | $50K-$200K (avoid shipment delays, expedite fees) |
| **Total Annual ROI** | | **$760K-$5.75M** |

---

## Cost Estimate

### Development

| Resource | Duration | Cost | Notes |
|----------|----------|------|-------|
| **2 Backend Engineers** | 7 months | $350K-$420K | Customs declarations, FTA engine, duty calculation |
| **1 Trade Compliance Specialist** | 6 months | $120K-$150K | FTA rules, ROO, sanctions screening |
| **1 ML Engineer** | 3 months | $75K-$100K | HS code classifier (BERT + XGBoost) |
| **1 Frontend Engineer** | 4 months | $80K-$100K | Customs dashboard, declaration forms, certificate generation |
| **Testing & QA** | 5 months | $50K-$80K | Compliance testing (USMCA, EU, sanctions), integration testing |
| **Total Development** | | **$675K-$850K** | |

### Data & Subscriptions (Annual)

| Resource | Cost | Notes |
|----------|------|-------|
| **HS Code Database** | $50K-$80K/year | WCO, USITC, EU TARIC (quarterly updates) |
| **Tariff Rate Database** | $20K-$30K/year | 40M+ tariff rates (HS code × country) |
| **OFAC/BIS Sanctions Lists** | $5K-$10K/year | Daily updates (Treasury.gov, BIS.gov APIs) |
| **EDI Gateway (Customs Broker)** | $5K-$10K/year | ACE transmission (EDI 214/856) |
| **Total Data/Subscriptions** | **$80K-$130K/year** | |

### Integration

| Task | Cost | Notes |
|------|------|-------|
| **Customs Broker EDI Integration** | $10K-$20K | EDI 214/856 (ACE transmission) |
| **Freight Forwarder API Integration** | $5K-$15K | Shipment tracking, SLI transmission |
| **Certificate of Origin Services** | $5K-$10K | ACE, CBSA, ASEAN Single Window APIs |
| **Total Integration** | **$20K-$45K** | |

### Total Cost

| Category | Cost | Notes |
|----------|------|-------|
| **Development** | $675K-$850K | 28 weeks (Q2-Q3 2028) |
| **Data/Subscriptions (Year 1)** | $80K-$130K | Recurring annual cost |
| **Integration** | $20K-$45K | One-time |
| **Total Year 1** | **$775K-$1.025M** | |
| **Recurring (Year 2+)** | $80K-$130K/year | Data/subscriptions only |

**Adjusted P3 Estimate**: **$800K-$1.2M** (aligns with P3 roadmap)

---

## Timeline

**Duration**: 28 weeks (Q2-Q3 2028)

### Phase 1: HS Code Classification & Master Data (8 weeks)

**Deliverables**:
- HS code master data (200K codes, tariff rates)
- ML classifier (BERT + XGBoost, 80% accuracy)
- Product master HS code assignment UI

### Phase 2: Import Compliance (8 weeks)

**Deliverables**:
- Customs declarations (CBP 7501, EU SAD, Canada B3)
- Duty calculation engine
- Commercial invoice + packing list generation
- Customs broker EDI integration

### Phase 3: FTA & Export Compliance (6 weeks)

**Deliverables**:
- FTA rules engine (USMCA, EU, CPTPP)
- Country of origin tracking (BOM rollup)
- Certificate of origin generation
- Export license check (EAR, ITAR)

### Phase 4: Sanctions Screening & Duty Drawback (4 weeks)

**Deliverables**:
- Sanctions screening (OFAC, BIS DPL, EU)
- Duty drawback tracking + claim generation
- Audit trail + compliance reporting

### Phase 5: Testing & Launch (2 weeks)

**Deliverables**:
- Compliance testing (USMCA, EU rules)
- Integration testing (procurement, sales, inventory)
- Beta launch (5-10 customers)
- Documentation (user guides, compliance manuals)

---

## Risks & Mitigation

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **ML HS code accuracy <80%** | High | Medium | Validate on 10K test set, hire trade compliance expert for training data |
| **FTA rules complexity (country-specific)** | High | High | Start with USMCA (US-focused), defer EU/CPTPP to Phase 2 |
| **Tariff rate database sync** | Medium | Low | Quarterly updates (WCO, USITC), automated sync with alerts |
| **Customs broker EDI integration** | Medium | Medium | Partner with 2-3 major brokers (CH Robinson, Kuehne+Nagel) |

### Compliance Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Sanctions violation (false negative)** | High | Low | Conservative matching (85% similarity = match), manual review workflow |
| **FTA qualification error (duty overpayment)** | Medium | Medium | Audit FTA logic with trade compliance attorney, customer acceptance testing |
| **Customs declaration error (penalty)** | High | Low | Compliance testing with customs broker, beta customers review declarations |

### Business Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Low customer adoption (<30% importers/exporters)** | High | Medium | Validate demand with 10 LOIs, highlight ROI ($500K-$5M duty savings) |
| **Competitors release similar feature** | Medium | Medium | Monitor SAP GTS, Oracle GTM roadmaps, ensure ML HS classification differentiation |

---

## Alternatives Considered

### Alternative 1: Partner with Third-Party GTM Vendor

**Approach**: Partner with Amber Road (E2open), Integration Point, or similar GTM SaaS.

**Pros**:
- Fast deployment ($100K-$200K integration)
- Lower development cost

**Cons**:
- ❌ High recurring cost ($50K-$150K/year per customer, reduces margin)
- ❌ No differentiation (same GTM as competitors)
- ❌ Integration complexity (external API, data sync)

**Rejected**: Strategic capability, need native ERP integration for competitive advantage.

---

### Alternative 2: Basic Customs Compliance Only (No FTA, Drawback)

**Approach**: Build customs declarations (CBP 7501, EU SAD) only, defer FTA/drawback.

**Pros**:
- Faster time-to-market ($300K-$500K vs $800K-$1.2M)
- Lower complexity

**Cons**:
- ❌ Misses biggest ROI (FTA duty savings $500K-$5M)
- ❌ Not competitive with SAP GTS, Oracle GTM
- ❌ Customers still need manual FTA qualification

**Rejected**: FTA qualification is highest ROI feature (5-15% duty savings).

---

## Consequences

### Positive

- ✅ **Competitive Parity**: Match SAP GTS, Oracle GTM, Microsoft D365 Trade & Logistics
- ✅ **Customer ROI**: $760K-$5.75M annual savings (duty savings, drawback, penalty reduction)
- ✅ **Compliance**: 100% sanctions screening, <1 violation/year
- ✅ **Efficiency**: -70% customs declaration time, -77% HS code classification time
- ✅ **Differentiation**: ML HS classification (80% accuracy), native ERP integration

### Negative

- ❌ **High Cost**: $800K-$1.2M development + $80K-$130K/year subscriptions
- ❌ **Complexity**: FTA rules engine (country-specific ROO), sanctions screening (fuzzy matching)
- ❌ **Resource Intensive**: Trade compliance specialist (hard-to-hire role)
- ❌ **Ongoing Maintenance**: Quarterly tariff rate updates, daily sanctions list updates

### Neutral

- ⚠️ **Phased Adoption**: Importers/exporters (30-40% customers) adopt first, domestic-only later
- ⚠️ **Regulatory Changes**: FTA rules change (e.g., USMCA renegotiation), require updates
- ⚠️ **Customer Training**: Complex rules (ROO, RVC), require customer education

---

## Related ADRs

- **ADR-023**: Procurement - Purchase orders → HS code assignment, duty calculation, customs declaration
- **ADR-025**: Sales Order Management - Sales orders → commercial invoice, certificate of origin
- **ADR-024**: Inventory Management - Product master → HS code, country of origin, BOM rollup
- **ADR-021**: Fixed Asset Accounting - Duty paid → asset cost basis
- **ADR-070**: AI Demand Forecasting - Forecast import volumes → duty cost forecasting

---

## Approval

**Status**: Planned (P3 - Q2-Q3 2028)  
**Approved By**: (Pending CTO, VP Product, Head of Supply Chain sign-off)  
**Next Review**: Q4 2027 (validate demand with customer LOIs)

**Implementation Start**: Q2 2028  
**Target Completion**: Q3 2028 (28 weeks)  
**Beta Launch**: Q4 2028 (5-10 importers/exporters)  
**General Availability**: Q1 2029 (all customers)

---

**Document Owner**: Head of Supply Chain  
**Last Updated**: 2026-02-06
