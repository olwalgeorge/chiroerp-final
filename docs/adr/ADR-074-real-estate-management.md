# ADR-074: Real Estate Management Solution

**Status**: Planned (P3 Optional - 2029)  
**Date**: 2026-02-06  
**Decision Makers**: CTO, VP Product  
**Consulted**: REITs, property management companies, corporate real estate  
**Informed**: Sales, customer success

---

## Context

### Business Problem

Real estate companies (REITs, property management, corporate real estate) need:

- **Property Management**: Commercial (office, retail, industrial), residential (apartments, condos, single-family)
- **Lease Administration**: CAM (Common Area Maintenance) reconciliation, rent escalations (CPI, fixed %), lease modifications
- **Tenant Billing**: Rent billing, CAM charges, utilities pass-through, late fees
- **Lease Accounting**: ASC 842 compliance (ROU right-of-use asset, lease liability), IFRS 16
- **Property Maintenance**: Work orders, vendor management, preventive maintenance schedules

### Market Opportunity

**Target Market**:
- REITs (Real Estate Investment Trusts) $100M-5B assets
- Property management companies (10K-100K units)
- Corporate real estate departments (Fortune 500)

**Customer ROI**:
- CAM reconciliation time: -60% (automated vs manual Excel)
- Lease accounting compliance: 100% ASC 842 (vs manual = audit findings)
- Tenant billing accuracy: >99% (vs 95-97% manual)

---

## Decision

### Selected Approach: Real Estate Management Module

Build **real estate management solution** with property management, lease administration, tenant billing, and ASC 842 lease accounting:

1. **Property Management**: Property portfolio (buildings, units), tenant management
2. **Lease Administration**: Lease contracts, rent escalations, CAM reconciliation
3. **Tenant Billing**: Rent billing, CAM charges, utilities, late fees
4. **Lease Accounting**: ASC 842 compliance (ROU asset, lease liability, amortization)
5. **Property Maintenance**: Work orders, vendor management

### Key Capabilities

#### 1. Property Portfolio Management

**Property Master Data**:
- Property (building): Address, property type (office/retail/industrial/residential), total square footage, year built
- Unit: Unit number, square footage, unit type (1BR/2BR/office/retail), status (vacant/occupied/under renovation)
- Amenities: Parking spaces, storage units, fitness center, pool

#### 2. Lease Administration

**Lease Contract**:
- Tenant, unit, lease start/end, base rent ($/month or $/sqft/year)
- Rent escalations: Fixed % (3% annual), CPI-based, stepped (Year 1: $10/sqft, Year 2: $11/sqft, Year 3: $12/sqft)
- CAM charges: Pro-rata share (unit sqft / building sqft × total CAM expenses)
- Security deposit, lease type (gross, modified gross, triple net NNN)

**Example (Commercial Lease)**:
```
Tenant: ABC Corp
Unit: Suite 200 (10,000 sqft)
Lease Term: 5 years (2024-2029)
Base Rent: $25/sqft/year = $250,000/year = $20,833/month
Escalation: 3% annual (Year 2: $25.75/sqft, Year 3: $26.52/sqft)
CAM: 10,000 sqft / 100,000 sqft building = 10% of CAM expenses
Estimated CAM: $5/sqft/year = $50,000/year = $4,167/month
Total Monthly: $20,833 + $4,167 = $25,000
```

#### 3. CAM Reconciliation

**Common Area Maintenance**:
- CAM expenses: Property management fees, utilities (common areas), janitorial, landscaping, snow removal, insurance, property tax
- Estimated CAM billing: Monthly (10% of estimated annual CAM)
- Actual CAM reconciliation: Annual (actual expenses - estimated = true-up)
- Tenant true-up: Bill tenant additional CAM or credit overpayment

**Example**:
```
Building: 100,000 sqft
Total CAM Expenses (Actual): $520,000
Tenant ABC: 10,000 sqft (10% share) = $52,000 actual CAM

Estimated Billing: $50,000 (12 months × $4,167)
Actual CAM: $52,000
True-Up: $52,000 - $50,000 = $2,000 (additional billing to tenant)
```

#### 4. Tenant Billing

**Rent Bill**:
- Base rent (monthly)
- CAM charges (estimated monthly, reconciled annually)
- Utilities pass-through (electric, gas, water if tenant-paid)
- Parking fees
- Late fees (5% or $50, whichever greater, if 5+ days late)
- Total due, due date (typically 1st of month)

#### 5. ASC 842 Lease Accounting

**Lessee Accounting (Corporate Real Estate)**:
- ROU (Right-of-Use) Asset = Present value of lease payments
- Lease Liability = Present value of lease payments
- Monthly journal entry:
  - Debit: Lease Expense (ROU amortization + interest expense)
  - Credit: ROU Asset (amortization)
  - Credit: Lease Liability (principal payment)
  - Credit: Cash (lease payment)

**Lessor Accounting (REIT)**:
- Operating lease: Recognize rent revenue straight-line over lease term
- Finance lease: Derecognize asset, recognize lease receivable

---

## Technology Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Backend** | Quarkus 3.x + Kotlin | Existing ChiroERP stack |
| **Lease Accounting** | Kotlin + financial math library | ASC 842 present value, amortization schedules |
| **CAM Reconciliation** | Kotlin + Drools (rules engine) | Complex CAM allocation rules |

---

## Success Metrics

| Metric | Target |
|--------|--------|
| **CAM Reconciliation Time** | -60% (automated vs manual Excel) |
| **Lease Accounting Compliance** | 100% ASC 842 (vs manual = audit findings) |
| **Tenant Billing Accuracy** | >99% (vs 95-97% manual) |

---

## Cost Estimate

| Category | Cost |
|----------|------|
| **Development** | $500K-$650K (2 backend × 6mo, 1 real estate domain expert × 4mo, frontend × 4mo, testing × 3mo) |
| **ASC 842 Compliance Tools** | $50K-$100K (lease accounting software, auditor validation) |
| **Integration** | $50K-$100K (property management systems, tenant portals) |
| **Total** | **$600K-$850K** |

**P3 Estimate**: **$600K-$800K** (Q3-Q4 2029, 24 weeks)

---

## Related ADRs

- **ADR-033**: Lease Accounting IFRS 16 / ASC 842 (lessee accounting)
- **ADR-021**: Fixed Asset Accounting (property depreciation)
- **ADR-022**: Revenue Recognition (rent revenue)

---

## Approval

**Status**: Planned (P3 Optional - Q3-Q4 2029)  
**Approved By**: (Pending CTO, VP Product sign-off)  
**Next Review**: Q4 2028 (validate demand with REIT customers)

---

**Document Owner**: VP Product  
**Last Updated**: 2026-02-06
