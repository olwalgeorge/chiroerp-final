# Human Capital Management (HCM) Domain Architecture

This directory contains hexagonal architecture specifications for Human Capital Management (HCM) subdomains including **Travel & Expense Management**, **Contingent Workforce Management**, and **Workforce Scheduling & Labor Management**.

## Subdomain Index

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Travel & Expense (T&E)** | ADR-054 | 9901 | Advanced | [hr-travel-expense.md](./hr-travel-expense.md) | Travel booking, policy enforcement, expense management |
| **Expense Receipts** | ADR-054 | 9902 | Advanced | [hr-expense-receipts.md](./hr-expense-receipts.md) | OCR, receipt capture, validation |
| **Corporate Card Reconciliation** | ADR-054 | 9903 | Advanced | [hr-card-reconciliation.md](./hr-card-reconciliation.md) | Card transaction matching, reconciliation |
| **Contingent Workforce (VMS)** | ADR-052 | 9904 | Add-on | [hr-contingent-workforce.md](./hr-contingent-workforce.md) | SOW, contractor lifecycle, ATS, AI matching |
| **Workforce Scheduling (WFM)** | ADR-055 | 9905 | Add-on | [hr-workforce-scheduling.md](./hr-workforce-scheduling.md) | Shift planning, demand forecasting, compliance |
| **HCM Analytics** | ADR-034/052/054/055 | 9906 | Advanced | [hr-analytics.md](./hr-analytics.md) | Workforce KPIs, spend analytics, compliance dashboards |

## Domain Overview (New: 2026-02-03)

### Travel & Expense Management (ADR-054) - Advanced Tier
**Extends ADR-034 (HR Integration) with enterprise-grade T&E capabilities**

**Scope:**
- Travel booking integration (GDS, TMC, online booking tools)
- Pre-trip approval workflows
- Expense report management with policy enforcement
- Receipt capture (OCR, mobile app, email forwarding)
- Corporate card reconciliation
- Mileage tracking and per diem calculations
- Multi-currency expense handling
- Project/cost center allocation
- Integration with Finance (ADR-009) and Payroll (ADR-034)

**Key Features:**
- Policy enforcement (spending limits, approval routing, prohibited vendors)
- Real-time policy violations and warnings
- Automated receipt matching
- Corporate card feed integration (Visa, Mastercard, Amex)
- Travel booking approval workflows
- Expense audit trail and compliance reporting

### Contingent Workforce Management (ADR-052) - Add-on Tier
**VMS (Vendor Management System) for consultants, contractors, and professional services**

**Scope:**
- Statement of Work (SOW) management
- Contractor/consultant lifecycle (requisition → onboarding → offboarding)
- Skills-based sourcing and rate card management
- Staffing supplier governance and scorecards
- Time & expense approval for contractors
- Compliance tracking (I-9, background checks, certifications)
- Applicant Tracking System (ATS) for contingent roles
- AI-powered talent matching
- Vendor portal for staffing suppliers

**Key Features:**
- Requisition workflow (hiring manager request → approval → sourcing)
- Rate card negotiation and MSA management
- Candidate screening and interview scheduling
- Onboarding automation (contracts, system access, equipment)
- Timesheet approval and invoice reconciliation
- Supplier performance scorecards (time-to-fill, quality scores, retention)
- AI matching: Skills match scoring, candidate recommendations, bias detection
- Vendor self-service portal: Submit candidates, view requisitions, track placements

### Workforce Scheduling & Labor Management (ADR-055) - Add-on Tier
**WFM for shift-based operations (retail, hospitality, healthcare, manufacturing)**

**Scope:**
- Shift planning and schedule optimization
- Labor demand forecasting (historical trends, events, seasonality)
- Time & attendance tracking
- Schedule compliance (break rules, overtime, consecutive shifts)
- Employee self-service (shift swap, time-off requests, availability)
- Labor cost forecasting and budget tracking
- Integration with POS, PMS, EHR for demand signals

**Key Features:**
- Shift templates and rule-based scheduling
- Auto-optimization (minimize labor cost, maximize coverage)
- Real-time schedule adjustments (call-ins, no-shows)
- Compliance monitoring (labor laws, union rules, certifications)
- Mobile app for schedule access and shift swap
- Labor forecasting integration with ADR-032 (Budgeting)
- Integration with ADR-034 (Payroll) for time & attendance

## Port Allocation

| Port | Service | Database | Tier | Status |
|------|---------|----------|------|--------|
| 9901 | hr-travel-expense | chiroerp_hr_travel_expense | Advanced | New (2026-02-03) |
| 9902 | hr-expense-receipts | chiroerp_hr_travel_expense (shared) | Advanced | New (2026-02-03) |
| 9903 | hr-card-reconciliation | chiroerp_hr_travel_expense (shared) | Advanced | New (2026-02-03) |
| 9904 | hr-contingent-workforce | chiroerp_hr_contingent | Add-on | New (2026-02-03) |
| 9905 | hr-workforce-scheduling | chiroerp_hr_scheduling | Add-on | New (2026-02-03) |
| 9906 | hr-analytics | chiroerp_hr_analytics | Advanced | New (2026-02-03) |

## Integration Map

```
+------------------------------------------------------------------------------------+
|                        HUMAN CAPITAL MANAGEMENT (HCM)                               |
|------------------------------------------------------------------------------------|
|                                                                                    |
|  TRAVEL & EXPENSE MANAGEMENT (Advanced - ADR-054):                                 |
|                                                                                    |
|  Travel Request -> Booking (GDS/TMC) -> Trip -> Expense Report -> Approval         |
|        |              |                   |           |              |            |
|        v              v                   v           v              v            |
|  Policy Check    Travel Policy      Receipt OCR   Card Txn    Finance/AP          |
|                                         |           |              |              |
|                                         v           v              v              |
|                                    Match Receipts -> Reconcile -> Post GL          |
|                                                                                    |
|  CONTINGENT WORKFORCE MANAGEMENT (Add-on - ADR-052):                               |
|                                                                                    |
|  Requisition -> Sourcing -> ATS -> AI Matching -> Interview -> Offer -> Onboard    |
|       |           |          |         |             |           |         |       |
|       v           v          v         v             v           v         v       |
|  Approval    Supplier   Resume    Candidate    Scorecard    Contract   Access     |
|              Portal     Parse     Ranking                                          |
|       |           |                                |                      |       |
|       v           v                                v                      v       |
|  Rate Card    Performance                    Timesheet ->         Project/SOW     |
|  Management   Scorecard                       Approval              Management    |
|                                                   |                               |
|                                                   v                               |
|                                              Finance/AP                            |
|                                                                                    |
|  WORKFORCE SCHEDULING (Add-on - ADR-055):                                          |
|                                                                                    |
|  Demand Forecast -> Shift Template -> Schedule -> Optimization -> Publish          |
|         |               |                  |            |             |           |
|         v               v                  v            v             v           |
|  POS/PMS/EHR      Compliance         Employee     Labor Cost    Mobile App        |
|   Integration        Rules            Availability   Tracking                     |
|                        |                  |                          |           |
|                        v                  v                          v           |
|                   Labor Laws         Shift Swap              Time & Attendance    |
|                   Break Rules        Time-Off Req                   |            |
|                                                                      v            |
|                                                                 Payroll (ADR-034)  |
|                                                                                    |
+------------------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)

**Travel & Expense (ADR-054):**
- **HR (ADR-034)** -> `EmployeeHiredEvent`, `EmployeeTerminatedEvent` -> Manage T&E access
- **Finance/GL (ADR-009)** -> `ChartOfAccountsUpdatedEvent` -> Update cost center/GL account options
- **Projects (ADR-036)** -> `ProjectCreatedEvent`, `ProjectClosedEvent` -> Enable/disable project allocation

**Contingent Workforce (ADR-052):**
- **Procurement (ADR-023)** -> `PurchaseOrderApprovedEvent` -> SOW approval, rate card validation
- **Projects (ADR-036)** -> `ProjectBudgetApprovedEvent` -> Authorize contractor requisitions
- **Finance/AP (ADR-009)** -> `InvoiceApprovedEvent` -> Contractor payment processing

**Workforce Scheduling (ADR-055):**
- **HR (ADR-034)** -> `EmployeeHiredEvent`, `EmployeeTerminatedEvent`, `SkillsCertifiedEvent` -> Update shift eligibility
- **POS/PMS/EHR** (External) -> `SalesDataRecorded`, `OccupancyUpdated`, `PatientAdmitted` -> Demand signals
- **Budgeting (ADR-032)** -> `LaborBudgetApprovedEvent` -> Schedule within budget constraints

### Downstream Consumers (Publish Events)

**Travel & Expense (ADR-054):**
- **Finance/AP (ADR-009)** <- `ExpenseReportApprovedEvent`, `TravelAdvanceRequestedEvent`
- **Finance/GL (ADR-009)** <- `ExpenseReportPostedEvent` (journal entry creation)
- **Payroll (ADR-034)** <- `ExpenseReimbursementApprovedEvent` (add to payroll)
- **Controlling (ADR-028)** <- `ExpenseAllocatedEvent` (cost center/project allocation)

**Contingent Workforce (ADR-052):**
- **Finance/AP (ADR-009)** <- `ContractorTimesheetApprovedEvent`, `ContractorInvoiceApprovedEvent`
- **Controlling (ADR-028)** <- `ContractorCostAllocatedEvent` (project/cost center)
- **Procurement (ADR-023)** <- `SOWCreatedEvent`, `RateCardUpdatedEvent`
- **Projects (ADR-036)** <- `ContractorAssignedEvent`, `ContractorOffboardedEvent`

**Workforce Scheduling (ADR-055):**
- **Payroll (ADR-034)** <- `TimeAndAttendanceRecordedEvent`, `OvertimeWorkedEvent`
- **Controlling (ADR-028)** <- `LaborCostForecastUpdatedEvent`
- **Budgeting (ADR-032)** <- `ActualLaborSpendRecordedEvent` (budget vs actual tracking)
- **HR (ADR-034)** <- `ComplianceViolationDetectedEvent` (break rules, consecutive shifts)

## Related ADRs

- [ADR-034: HR Integration & Payroll Events](../../adr/ADR-034-hr-integration-payroll-events.md) - Base HR capabilities, payroll integration
- [ADR-054: Travel & Expense Management](../../adr/ADR-054-travel-expense-management.md) - **New: Advanced Tier**
- [ADR-052: Contingent Workforce & Professional Services](../../adr/ADR-052-contingent-workforce-professional-services.md) - **New: Add-on**
- [ADR-055: Workforce Scheduling & Labor Management](../../adr/ADR-055-workforce-scheduling-labor-management.md) - **New: Add-on**
- [ADR-009: Financial Accounting](../../adr/ADR-009-financial-accounting-domain.md) - GL posting, AP integration
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md) - Cost allocation
- [ADR-032: Budgeting & Planning](../../adr/ADR-032-budgeting-planning-fpa.md) - Labor budget integration
- [ADR-036: Project Accounting](../../adr/ADR-036-project-accounting.md) - Project allocation
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - SOW procurement

## Related Domains

- [Finance Architecture](../finance/README.md) - AP/GL posting integration
- [Procurement Architecture](../procurement/README.md) - SOW procurement workflows

## Implementation Roadmap

### Phase 1: Travel & Expense Foundation (Months 1-3)
- Expense report management
- Receipt capture (OCR, mobile upload)
- Policy enforcement engine
- Basic approval workflows
- Finance/GL integration

### Phase 2: Travel Booking & Card Reconciliation (Months 4-6)
- Travel request and pre-trip approval
- GDS/TMC integration (Sabre, Amadeus, Concur)
- Corporate card feed integration
- Automated card transaction matching
- Multi-currency support

### Phase 3: Contingent Workforce Foundation (Months 7-9)
- Requisition workflow
- SOW management
- Rate card negotiation
- Contractor onboarding/offboarding
- Timesheet approval
- Supplier management

### Phase 4: ATS & AI Matching (Months 10-12)
- Applicant Tracking System for contingent roles
- Resume parsing and skills extraction
- AI-powered candidate matching (skills match scoring)
- Interview scheduling and scorecards
- Vendor portal (supplier self-service)
- Supplier performance scorecards

### Phase 5: Workforce Scheduling Foundation (Months 13-15)
- Shift template management
- Schedule creation and publication
- Employee self-service (view schedule, request time-off)
- Time & attendance tracking
- Basic compliance rules (break laws, overtime)

### Phase 6: WFM Advanced Features (Months 16-18)
- Labor demand forecasting (ML-based)
- Schedule auto-optimization (minimize cost, maximize coverage)
- Real-time schedule adjustments (call-ins, no-shows)
- Advanced compliance monitoring (union rules, certifications)
- Mobile app (shift swap, availability management)
- POS/PMS/EHR integration for demand signals

### Phase 7: Analytics & Optimization (Months 19+)
- Workforce analytics dashboards
- T&E spend analytics and policy compliance reporting
- Contingent workforce spend and supplier performance
- Labor cost forecasting and budget variance
- Predictive analytics (attrition risk, optimal staffing levels)

---

## Implementation Status

### Current Status: Specification Complete (2026-02-03)

**6 modules specified:**
- Travel & Expense Management (ADR-054) - Advanced Tier
- Expense Receipts & OCR (ADR-054) - Advanced Tier
- Corporate Card Reconciliation (ADR-054) - Advanced Tier
- Contingent Workforce Management (ADR-052) - Add-on
- Workforce Scheduling & Labor Management (ADR-055) - Add-on
- HCM Analytics (cross-module) - Advanced Tier

**Next Steps:**
1. Implement Phase 1 (T&E Foundation) for Advanced tier customers
2. Pilot ADR-052 (Contingent Workforce) with professional services customers
3. Pilot ADR-055 (Workforce Scheduling) with retail/hospitality customers
4. Gather feedback and iterate on AI matching algorithms (ADR-052)
5. Build demand forecasting ML models (ADR-055)
