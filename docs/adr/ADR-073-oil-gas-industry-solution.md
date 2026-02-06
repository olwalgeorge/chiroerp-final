# ADR-073: Oil & Gas Industry Solution

**Status**: Planned (P3 - 2029)  
**Date**: 2026-02-06  
**Decision Makers**: CTO, VP Product, Head of Industry Solutions  
**Consulted**: Oil & gas executives, petroleum accountants, reserves engineers  
**Informed**: Sales, customer success, implementation teams

---

## Context

### Business Problem

Oil & gas companies (exploration & production, midstream, downstream) require specialized capabilities:

- **Upstream (E&P)**: Drilling AFE (Authorization for Expenditure $2M-20M/well), joint venture accounting (JIB - monthly partner billing), production accounting (daily volumes, depletion), SEC reserves reporting (PV-10, standardized measure)
- **Midstream (Pipelines)**: Nominations (capacity allocation), tariff billing ($/barrel, $/Mcf), FERC compliance (Form 6/2), imbalance tracking
- **Downstream (Refining)**: Refining runs (crude → gasoline/diesel/jet fuel), blending (octane, cetane, RVP), rack pricing, distribution

Without specialized oil & gas ERP, companies face:
- **Manual AFE tracking** (Excel, 7-10 days approval cycle)
- **JIB errors** (manual Excel calculations = 5% error rate, partner disputes)
- **Slow production accounting** (5-7 days month-end close vs <2 hours automated)
- **Complex reserves reporting** (manual PV-10 calculations, SEC audit risk)

### Market Opportunity

**Target Market**:
- Independent E&P companies $50M-1B revenue
- Midstream pipeline operators
- Downstream refiners/marketers

**US Oil & Gas Market**:
- 9,000+ operators ($400B revenue: upstream $250B, midstream $80B, downstream $70B)
- **SAP IS-Oil**: $15M-100M implementations (36-60 months)
- **Oracle E&P**: $10M-50M
- **P2 BOLO**: $5M-20M (energy accounting software)

**Customer ROI**:
- AFE cycle time: <3 days (vs 7-10 days manual)
- JIB accuracy: 100% automated (vs 95% manual Excel)
- Production accounting: <2 hours month-end close (vs 5-7 days)
- SEC reserves: 100% compliance (vs manual = audit risk)

---

## Decision

### Selected Approach: Comprehensive Oil & Gas Industry Solution

Build **full-scope oil & gas solution** targeting upstream (E&P), midstream (pipelines), and downstream (refining):

1. **Upstream (E&P)**: AFE management, JIB (joint interest billing), production accounting, SEC reserves (PV-10)
2. **Midstream**: Pipeline nominations, tariff billing, FERC Form 6/2, imbalance tracking
3. **Downstream**: Refining runs, blending (octane/cetane), rack pricing, distribution

### Key Capabilities

#### 1. Upstream - Drilling AFE Management

**Authorization for Expenditure (AFE)**:
- Multi-well AFE (budget $2M-20M per well: drilling $1M-10M, completion $500K-5M, equipping $300K-2M)
- Working interest partners (50% operator, 25% partner A, 25% partner B)
- Cost categories (drilling, completion, equipping, facilities)
- Approval workflow (geologist → engineer → finance → partner approval)

**Example**:
```
Well: ABC-123H (Horizontal, Permian Basin)
AFE Budget: $8M
  - Drilling: $4M
  - Completion (hydraulic fracturing): $3M
  - Equipping (wellhead, flowlines): $1M
Working Interest:
  - Operator (us): 50% ($4M)
  - Partner A: 25% ($2M)
  - Partner B: 25% ($2M)
Approval: 3 days (vs 7-10 days manual)
```

#### 2. Joint Venture Accounting (JIB)

**Joint Interest Billing**:
- Monthly JIB invoices (LOE - Lease Operating Expenses + capital costs × working interest %)
- Revenue distribution (oil/gas sales × net revenue interest NRI %)
- Automated partner invoicing (generate invoices for 5-20 partners per well)
- Reconciliation (partner payments, disputes, adjustments)

**Example**:
```
Well: ABC-123H
Monthly LOE: $50K (pumping, chemicals, electricity, labor)
Oil Sales: 15,000 barrels @ $75/bbl = $1,125,000
Gas Sales: 30,000 Mcf @ $3/Mcf = $90,000
Total Revenue: $1,215,000

Partner A (25% WI, 20% NRI):
  - JIB Invoice (LOE): 25% × $50K = $12,500 (owed to operator)
  - Revenue Distribution: 20% × $1,215,000 = $243,000 (pay to partner A)
  - Net: $243,000 - $12,500 = $230,500 (wire to partner A)
```

#### 3. Production Accounting

**Daily Production**:
- Import SCADA/well meters (barrels oil, Mcf gas)
- Revenue calculation (volumes × posted prices)
- Depletion (unit-of-production: cost basis / proved reserves × current period production)
- Month-end close: <2 hours (vs 5-7 days manual)

#### 4. SEC Reserves Reporting

**Proved Reserves**:
- PV-10 calculation (future net revenue discounted @ 10%)
- Standardized measure (PV-10 - income tax)
- Reserve categories: PDP (proved developed producing), PDNP (proved developed non-producing), PUD (proved undeveloped)
- SEC filing (Form 10-K, annual reserves report)

#### 5. Midstream - Pipeline Nominations

**Nominations**:
- Shipper nominations (request pipeline capacity for month: receipt point, delivery point, volume)
- Capacity allocation (if total nominations > capacity, prorate)
- Scheduling (daily/hourly flow rates)
- Imbalance tracking (nominated vs actual)

#### 6. Tariff Billing

**Pipeline Tariffs**:
- Tariff rates ($/barrel for oil, $/Mcf for gas)
- Volumetric billing (actual flow × tariff rate)
- Fuel retention (deduct 2-5% gas volume as transportation fee)
- FERC Form 6 (oil pipelines), Form 2 (gas pipelines)

#### 7. Downstream - Refining & Blending

**Refining Runs**:
- Crude oil input (volume, API gravity, sulfur %)
- Refining yields (gasoline 45%, diesel 25%, jet fuel 10%, residual 10%, other 10%)
- Yield variance (actual vs theoretical)

**Blending**:
- Gasoline blending (octane rating 87/89/93, RVP Reid Vapor Pressure, ethanol 10%)
- Diesel blending (cetane number 40-55, sulfur <15ppm ULSD)
- Optimization (minimize cost, meet specifications)

---

## Technology Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Backend** | Quarkus 3.x + Kotlin | Existing ChiroERP stack |
| **AFE Workflow** | Camunda BPMN | Approval workflow (geologist → engineer → finance → partners) |
| **JIB Calculation** | Kotlin + Drools (rules engine) | Complex partner revenue/expense allocation |
| **Production Data Import** | SCADA integration (OPC UA, Modbus) | Well meters, tank gauges |
| **Reserves Calculation** | Python 3.11 + NumPy | PV-10 discounted cash flow (DCF) |
| **Blending Optimization** | Python + PuLP (linear programming) | Gasoline/diesel blending optimization |

---

## Success Metrics & KPIs

| Metric | Baseline (Manual) | Target (Automated) | Improvement |
|--------|-------------------|--------------------| ------------|
| **AFE Approval Time** | 7-10 days | <3 days | -60-70% |
| **JIB Accuracy** | 95% (5% Excel errors) | 100% | +5% |
| **Production Accounting Close** | 5-7 days | <2 hours | -98% |
| **Reserves Reporting Time** | 40-80 hours/quarter | <8 hours | -80-90% |
| **Pipeline Nominations** | 50% automated | 100% automated | +50% |

---

## Cost Estimate

| Category | Cost | Notes |
|----------|------|-------|
| **Development** | $850K-$1.25M | 3 backend × 8mo, 1 oil & gas domain expert × 8mo, 1 reserves specialist × 3mo, frontend × 6mo, testing × 6mo |
| **SCADA Integration** | $80K-$120K | Well production data, pipeline flow meters |
| **SEC Reserves Tools** | $40K-$60K | Reserve software, PV-10 calculators |
| **FERC Reporting** | $30K-$40K | FERC Form 6/2 templates |
| **Total** | **$1.0M-$1.47M** | |

**P3 Estimate**: **$1.0M-$1.5M** (Q1-Q2 2029, 32 weeks)

---

## Related ADRs

- **ADR-036**: Project Accounting - AFE cost tracking
- **ADR-021**: Fixed Asset Accounting - Well depreciation (depletion, unit-of-production)
- **ADR-024**: Inventory Management - Crude oil inventory, refined products
- **ADR-037**: Manufacturing Production - Refining runs, blending

---

## Approval

**Status**: Planned (P3 - Q1-Q2 2029)  
**Approved By**: (Pending CTO, VP Product sign-off)  
**Next Review**: Q2 2028 (validate demand with oil & gas customer LOIs)

**Implementation Start**: Q1 2029  
**Target Completion**: Q2 2029 (32 weeks)  
**Beta Launch**: Q3 2029 (5-10 E&P/midstream/downstream companies)

---

**Document Owner**: Head of Industry Solutions  
**Last Updated**: 2026-02-06
