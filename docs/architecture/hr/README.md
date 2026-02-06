# Human Capital Management (HCM) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-034, ADR-052, ADR-054, ADR-055

## First-Level Modules
- `hr-shared/` — ADR-006 COMPLIANT: Identifiers and value objects only
- `hr-core/` — Port 9101 - Core HR (Employee, Organization, Payroll Integration)
- `hr-travel-expense/` — Port 9901 - Travel & Expense Management (ADR-054)
- `hr-contingent-workforce/` — Port 9904 - Contingent Workforce / VMS (ADR-052)
- `hr-professional-services/` — Port 9907 - Professional Services / SOW Management (ADR-052)
- `hr-workforce-scheduling/` — Port 9905 - Workforce Scheduling / WFM (ADR-055)
- `hr-analytics/` — Port 9906 - HCM Analytics
