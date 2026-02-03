# ADR-035: ESG & Sustainability Reporting

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-02  
**Deciders**: Architecture Team, Finance Team, Compliance Team  
**Priority**: P3 (Optional Add-on)  
**Tier**: Add-on  
**Tags**: esg, sustainability, carbon, csrd, tcfd, gri, scope-emissions

## Context
Environmental, Social, and Governance (ESG) reporting is mandatory in the EU (CSRD), increasingly required by investors, and becoming a competitive differentiator. A general-purpose ERP must support ESG data collection, carbon accounting, and regulatory disclosures as an optional add-on for tenants with sustainability requirements.

## Decision
Implement an **ESG & Sustainability Reporting** add-on module that captures environmental metrics, tracks carbon emissions (Scope 1/2/3), manages social and governance KPIs, and generates regulatory disclosures (CSRD, TCFD, GRI, CDP).

### Scope
- Carbon accounting (Scope 1, 2, 3 emissions).
- Environmental metrics (energy, water, waste).
- Social metrics (workforce diversity, safety, training).
- Governance metrics (board composition, ethics, compliance).
- Regulatory disclosures (CSRD, TCFD, GRI, CDP, SASB).
- Supplier sustainability assessments.

### Out of Scope (Future/External)
- Life cycle assessment (LCA) modeling.
- Carbon credit trading.
- ESG ratings optimization.

### Core Capabilities

#### Carbon Accounting
- **Scope 1**: Direct emissions from owned/controlled sources.
- **Scope 2**: Indirect emissions from purchased energy (location/market-based).
- **Scope 3**: Value chain emissions (15 categories per GHG Protocol).
- **Emission factors**: Regional emission factor databases.
- **Activity data**: Integration with procurement, fleet, facilities.
- **Carbon footprint**: Product-level and organizational-level.

#### Environmental Metrics
- **Energy**: Consumption by source (renewable vs. fossil).
- **Water**: Withdrawal, consumption, discharge by source.
- **Waste**: Generation, diversion, disposal methods.
- **Materials**: Recycled content, packaging, hazardous materials.
- **Biodiversity**: Land use, habitat impact (where applicable).

#### Social Metrics
- **Workforce**: Headcount, diversity (gender, ethnicity, age), turnover.
- **Health & Safety**: Incident rates, lost time, near misses.
- **Training**: Hours per employee, skills development.
- **Human Rights**: Supply chain assessments, due diligence.
- **Community**: Local hiring, philanthropy, engagement.

#### Governance Metrics
- **Board**: Composition, independence, diversity, tenure.
- **Ethics**: Code of conduct, whistleblower reports, anti-corruption.
- **Risk**: ESG risk assessments, climate risk (physical/transition).
- **Compliance**: Regulatory violations, fines, remediation.

#### Regulatory Frameworks
| Framework | Region | Disclosure Type |
|-----------|--------|-----------------|
| **CSRD/ESRS** | EU | Mandatory (2024+) |
| **TCFD** | Global | Climate risk recommended |
| **GRI** | Global | Comprehensive voluntary |
| **CDP** | Global | Climate/water/forests |
| **SASB** | US | Industry-specific |
| **SEC Climate** | US | Proposed mandatory |

### Data Model (Conceptual)
- `ESGReportingPeriod`, `EmissionSource`, `EmissionRecord`, `EmissionFactor`.
- `EnvironmentalMetric`, `SocialMetric`, `GovernanceMetric`.
- `CarbonFootprint`, `Scope3Category`, `SupplierAssessment`.
- `ESGTarget`, `ESGProgress`, `DisclosureReport`, `AuditTrail`.

### Key Workflows
- **Data collection**: Automated capture from operations (procurement, fleet, HR, facilities).
- **Emission calculation**: Activity data × emission factor = CO2e.
- **Scope 3 estimation**: Supplier data, spend-based, hybrid methods.
- **Target tracking**: Science-based targets (SBTi), progress monitoring.
- **Disclosure generation**: Auto-populate CSRD/GRI/TCFD templates.
- **Assurance**: Audit trail for third-party verification.

### Integration Points
- **Procurement (MM-PUR)**: Supplier emissions, spend-based Scope 3.
- **Inventory (MM-IM)**: Material flows, packaging, waste.
- **Manufacturing (PP)**: Energy consumption, process emissions.
- **Fixed Assets (FI-AA)**: Fleet vehicles, facilities, equipment.
- **HR Integration**: Workforce metrics, training, safety incidents.
- **Finance (FI)**: Carbon costs, ESG-linked financing.
- **Analytics (ADR-016)**: ESG dashboards, trend analysis.

### Non-Functional Constraints
- **Data quality**: Emission factor accuracy, source documentation.
- **Auditability**: Full trail for third-party assurance.
- **Timeliness**: Annual/quarterly reporting cycles.
- **Flexibility**: Support multiple frameworks simultaneously.

### KPIs and SLOs
| Metric | Target |
|--------|--------|
| Emission calculation accuracy | ≥ 95% vs. third-party audit |
| Data collection completeness | ≥ 90% of material sources |
| Disclosure generation time | p95 < 2 hours for annual report |
| Scope 3 coverage | ≥ 80% of material categories |
| Target tracking freshness | Monthly updates |

## Alternatives Considered
- **Spreadsheet-based tracking**: Rejected (no auditability, error-prone).
- **Standalone ESG software**: Rejected (data silos, integration cost).
- **Finance-only carbon ledger**: Rejected (insufficient scope).

## Consequences
### Positive
- Regulatory compliance readiness (CSRD, SEC).
- Investor and stakeholder confidence.
- Operational efficiency insights (energy, waste).
- Competitive differentiation.

### Negative
- Data collection complexity (especially Scope 3).
- Emission factor uncertainty.
- Requires cross-functional coordination.

### Neutral
- Module is optional; enabled per tenant.
- External assurance may still be required.

## Compliance
- **CSRD/ESRS**: EU Corporate Sustainability Reporting Directive.
- **TCFD**: Task Force on Climate-related Financial Disclosures.
- **GRI**: Global Reporting Initiative Standards.
- **GHG Protocol**: Scope 1/2/3 accounting standards.
- **SBTi**: Science Based Targets initiative alignment.

## Add-on Activation
- **Tenant feature flag**: `esg_reporting_enabled`.
- **Licensing**: Optional module, separate pricing tier.
- **Prerequisites**: Core finance, procurement, HR modules.

## Implementation Plan
- Phase 1: Scope 1 & 2 carbon accounting, emission factors.
- Phase 2: Environmental metrics (energy, water, waste).
- Phase 3: Scope 3 estimation (spend-based, supplier data).
- Phase 4: Social and governance metrics.
- Phase 5: Disclosure templates (CSRD, GRI, TCFD).
- Phase 6: Target tracking, SBTi alignment, assurance support.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-016: Analytics & Reporting Architecture
- ADR-023: Procurement (MM-PUR)
- ADR-024: Inventory Management (MM-IM)
- ADR-034: HR Integration & Payroll Events
- ADR-037: Manufacturing & Production (PP)

### External References
- GHG Protocol Corporate Standard
- CSRD/ESRS Standards (EFRAG)
- TCFD Recommendations
- GRI Standards
- Science Based Targets initiative (SBTi)
