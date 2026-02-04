# ADR-036: Project Accounting (PS)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-02
**Deciders**: Architecture Team, Finance Team, Operations Team
**Priority**: P3 (Optional Add-on)
**Tier**: Add-on
**Tags**: project-accounting, wbs, cost-tracking, milestone-billing, psa, construction

## Context
Project-based businesses (professional services, construction, engineering, R&D) require project accounting capabilities beyond standard cost center accounting. A general-purpose ERP must support project costing, WBS, milestone billing, revenue recognition, and resource management as an optional add-on for project-intensive tenants.

## Decision
Implement a **Project Accounting (PS)** add-on module that provides Work Breakdown Structure (WBS), project budgeting, cost collection, milestone/progress billing, revenue recognition, and resource planning integrated with finance, procurement, HR, and controlling.

### Scope
- Work Breakdown Structure (WBS) and project hierarchy.
- Project budgeting and cost planning.
- Actual cost collection (labor, materials, expenses, subcontractors).
- Project billing (milestone, progress, time & materials, fixed price).
- Project revenue recognition (ASC 606 / IFRS 15).
- Resource planning and capacity management.
- Project profitability and variance analysis.

### Out of Scope (Future/External)
- Full PSA (Professional Services Automation) suite.
- Project portfolio management (PPM).
- Advanced resource optimization (AI-based).
- Gantt/scheduling tools (integrate with MS Project, Primavera).

### Core Capabilities

#### Work Breakdown Structure (WBS)
- **Project hierarchy**: Project → Phase → Task → Activity.
- **WBS elements**: Cost collectors, billing elements, profit centers.
- **Templates**: Reusable WBS templates by project type.
- **Status tracking**: Planning, active, on hold, closed, cancelled.
- **Multi-company projects**: Intercompany cost sharing.

#### Project Budgeting
- **Original budget**: Baseline cost plan by WBS element.
- **Budget versions**: Revisions with approval workflow.
- **Cost types**: Labor, materials, expenses, subcontractors, overhead.
- **Rate tables**: Standard labor rates, material costs, burden rates.
- **Budget controls**: Soft/hard limits, tolerance thresholds.

#### Cost Collection
- **Labor**: Timesheet integration, labor cost allocation.
- **Materials**: PO receipts, inventory issues to project.
- **Expenses**: T&E integration, project-coded expenses.
- **Subcontractors**: Vendor invoices allocated to WBS.
- **Overhead**: Burden allocation (percentage or flat).
- **Commitments**: Encumbrances for open POs/contracts.

#### Project Billing
| Billing Method | Description | Use Case |
|----------------|-------------|----------|
| **Milestone** | Invoice at defined milestones | Construction, deliverables |
| **Progress** | Percentage of completion | Long-term contracts |
| **Time & Materials** | Actual hours × rate + expenses | Consulting, support |
| **Fixed Price** | Agreed contract value | Product delivery |
| **Cost Plus** | Actual cost + margin | Government, R&D |
| **Retainage** | Hold % until completion | Construction |

#### Revenue Recognition (ASC 606 / IFRS 15)
- **Performance obligations**: Identify distinct deliverables.
- **Transaction price**: Contract value, variable consideration.
- **Allocation**: Standalone selling price per obligation.
- **Recognition**: Over time (input/output) or point in time.
- **Contract modifications**: Prospective or cumulative catch-up.
- **Integration**: Link to ADR-022 Revenue Recognition.

#### Resource Planning
- **Resource pool**: Skills, availability, cost rates.
- **Project staffing**: Assignments, planned vs. actual hours.
- **Capacity planning**: Utilization forecasting.
- **Skill matching**: Resource recommendations.
- **Rate cards**: Bill rates by role, client, project.

### Data Model (Conceptual)
- `Project`, `WBSElement`, `ProjectBudget`, `BudgetLine`, `CostType`.
- `TimeEntry`, `ExpenseEntry`, `MaterialIssue`, `SubcontractorCost`.
- `ProjectBillingPlan`, `Milestone`, `BillingEvent`, `ProjectInvoice`.
- `Resource`, `ResourceAssignment`, `RateCard`, `CapacityPlan`.
- `ContractObligation`, `RevenueSchedule`, `CommitmentRegister`.

### Key Workflows
- **Project setup**: Create project → define WBS → set budget → staff resources.
- **Cost capture**: Timesheets/expenses/POs → validate → post to WBS.
- **Billing**: Milestone achieved → create billing request → invoice → AR.
- **Revenue recognition**: Calculate % complete → recognize revenue → GL posting.
- **Close**: Final billing → reconcile costs → close WBS → archive.
- **Variance analysis**: Budget vs. actual vs. forecast → corrective action.

### Integration Points
- **Finance/GL (ADR-009)**: Project cost postings, revenue recognition.
- **Controlling/CO (ADR-028)**: Profitability analysis, allocation.
- **Procurement (ADR-023)**: POs to project, commitment tracking.
- **Inventory (ADR-024)**: Material issues to WBS.
- **HR Integration (ADR-034)**: Timesheets, labor rates, resource data.
- **Revenue Recognition (ADR-022)**: ASC 606 contract accounting.
- **Accounts Receivable**: Project invoices, milestone billing.
- **Analytics (ADR-016)**: Project dashboards, profitability reports.

### Non-Functional Constraints
- **Accuracy**: Cost allocations reconcile to GL within 0.01%.
- **Timeliness**: Weekly cost updates, monthly billing cycles.
- **Auditability**: Full trail from timesheet to GL to invoice.
- **Scalability**: Support 10,000+ active WBS elements per tenant.

### KPIs and SLOs
| Metric | Target |
|--------|--------|
| Cost posting latency | p95 < 24 hours from entry |
| Budget vs. actual variance visibility | Within 1 business day |
| Milestone billing accuracy | 100% match to contract terms |
| Revenue recognition compliance | 100% ASC 606/IFRS 15 |
| Resource utilization accuracy | ≥ 95% vs. timesheets |
| Project close cycle | p95 < 5 business days |

## Alternatives Considered
- **Cost center only**: Rejected (no WBS, no project billing).
- **Standalone project software**: Rejected (data silos, reconciliation).
- **Spreadsheet tracking**: Rejected (no controls, no audit trail).

## Consequences
### Positive
- End-to-end project financial visibility.
- Accurate project profitability analysis.
- Compliant revenue recognition.
- Improved resource utilization.

### Negative
- Configuration complexity (WBS structures, billing rules).
- Requires disciplined timesheet and expense capture.
- Change management for project managers.

### Neutral
- Module is optional; enabled per tenant.
- Can integrate with external PM tools for scheduling.

## Compliance
- **ASC 606 / IFRS 15**: Contract revenue recognition.
- **SOX**: Project cost controls, billing approvals.
- **Government contracting**: Cost plus, DCAA compliance (if applicable).
- **Construction**: Progress billing, retainage, lien waivers.

## Add-on Activation
- **Tenant feature flag**: `project_accounting_enabled`.
- **Licensing**: Optional module, separate pricing tier.
- **Prerequisites**: Core finance, procurement, HR integration.

## Implementation Plan
- Phase 1: Project and WBS hierarchy, basic cost collection.
- Phase 2: Project budgeting, commitment tracking.
- Phase 3: Milestone and T&M billing.
- Phase 4: Progress billing, revenue recognition integration.
- Phase 5: Resource planning, capacity management.
- Phase 6: Advanced analytics, profitability dashboards.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-016: Analytics & Reporting Architecture
- ADR-022: Revenue Recognition (ASC 606 / IFRS 15)
- ADR-023: Procurement (MM-PUR)
- ADR-024: Inventory Management (MM-IM)
- ADR-028: Controlling / Management Accounting (CO)
- ADR-034: HR Integration & Payroll Events

### External References
- SAP PS (Project System) module overview
- ASC 606 / IFRS 15 contract accounting
- PMBOK Guide (Project Management Institute)
