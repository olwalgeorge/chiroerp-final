# ADR-054: Travel & Expense Management (T&E)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Finance Team, HR Team
**Priority**: P2 (High Value)
**Tier**: Advanced
**Tags**: travel, expense, reimbursement, per-diem, compliance, spend-management

## Context
While **ADR-034 (HR Integration)** provides basic T&E capabilities (expense approval, reimbursement, GL posting), it lacks comprehensive travel management features required by enterprises: travel booking integration, policy enforcement, per diem rules, receipt capture, corporate card reconciliation, mileage tracking, advance requests, and detailed spend analytics.

Employees across sales, consulting, field service, and executive teams require seamless travel booking, expense submission, and reimbursement workflows that enforce corporate policies, integrate with travel management companies (TMCs), and provide CFOs with spend visibility and control.

## Decision
Implement a comprehensive **Travel & Expense Management (T&E)** module that extends ADR-034 with travel booking, policy enforcement, receipt capture, corporate card reconciliation, and advanced expense workflows integrated with HR, Finance, Procurement, and Project Accounting.

### Scope
- **Travel Booking**: Integration with TMCs (Concur Travel, Egencia, TripActions).
- **Expense Submission**: Mobile/web expense capture with receipt OCR.
- **Policy Enforcement**: Real-time policy checks and approvals.
- **Per Diem & Mileage**: Automated calculation based on location and rates.
- **Corporate Cards**: Transaction import and reconciliation (Amex, Visa, Mastercard).
- **Advance Requests**: Travel advance and settlement workflows.
- **Multi-Currency**: Foreign exchange rates and currency conversion.
- **Audit & Compliance**: VAT recovery, audit trails, rules-based fraud detection.

### Out of Scope (Future/External)
- Travel content aggregation (flights, hotels, car rentals) - use TMC partners.
- Loyalty program management - managed by TMC.
- Travel risk management (traveler tracking, safety alerts) - integrate with external TRM platforms.

## Core Capabilities

### Travel Request & Booking

#### Travel Request
- **Trip Planning**:
  - Employee initiates travel request (destination, dates, purpose, budget).
  - Business justification and cost estimate.
  - Approval workflow (manager → travel coordinator → finance).

- **Travel Policy Checks**:
  - Booking window (e.g., flights must be booked 14+ days in advance).
  - Class of service restrictions (economy for domestic, business for international > 6 hours).
  - Preferred suppliers and negotiated rates.
  - Advance approval for exceptions.

- **Budget Control**:
  - Check against travel budget (cost center, project, department).
  - Hard stop or soft warning for over-budget requests.
  - Budget reservation during approval process.

#### Travel Booking Integration
- **TMC Integration**:
  - SSO to Concur Travel, Egencia, TripActions, or corporate TMC portal.
  - Pass travel request details to TMC (pre-filled itinerary).
  - Import booked itinerary back to ERP (flights, hotels, car rentals).

- **Booking Data Capture**:
  - Itinerary details (flights, hotels, car rentals).
  - Booking references (PNR, confirmation numbers).
  - Costs and payment method (corporate card, central billing).
  - Traveler details and travel policy compliance.

- **Direct Booking**:
  - For users without TMC access, manual itinerary entry.
  - Upload booking confirmations and receipts.

#### Travel Itinerary Management
- **Trip Dashboard**:
  - Upcoming trips, current trips, past trips.
  - Itinerary details (flight times, hotel addresses, car rental pickups).
  - Export to calendar (Outlook, Google Calendar).

- **Trip Modifications**:
  - Change requests (flight changes, hotel cancellations).
  - Approval for change fees and fare differences.
  - Updated itinerary sync.

### Expense Capture & Submission

#### Receipt Capture
- **Mobile App**:
  - Snap photo of receipt (OCR extraction: vendor, date, amount, category).
  - Attach to expense line item.
  - Offline mode (sync when online).

- **Email Import**:
  - Forward e-receipts to dedicated email address.
  - Auto-parse and create draft expense.

- **Web Upload**:
  - Drag-and-drop PDF/image receipts.
  - Bulk upload for multiple receipts.

- **OCR Validation**:
  - Extract merchant, date, total, tax, payment method.
  - Auto-categorize based on merchant (e.g., Starbucks → Meals, Shell → Fuel).
  - Flag discrepancies for manual review.

#### Expense Line Items
- **Expense Categories**:
  - Airfare, hotel, meals, ground transportation, car rental, fuel, parking, tolls.
  - Entertainment, client gifts, office supplies, conference fees.
  - Mileage, per diem, communication (phone, internet).

- **Expense Attributes**:
  - Expense date, merchant, amount, currency, payment method.
  - Business purpose and attendees (for meals/entertainment).
  - Project/customer allocation (billable vs non-billable).
  - Cost center, GL account, tax code.

- **Split Transactions**:
  - Split single receipt across multiple projects or attendees.
  - Allocate percentages (e.g., 60% Project A, 40% Project B).

#### Mileage Tracking
- **Mileage Entry**:
  - Start/end locations or total distance.
  - Date, purpose, vehicle type (personal, company, rental).
  - Automatic rate application (IRS standard mileage rate or company policy).

- **GPS Integration**:
  - Import mileage from GPS/telematics (company vehicles).
  - Mobile app GPS tracking (opt-in for personal vehicles).

- **Calculation**:
  - Total mileage × rate = reimbursement amount.
  - Daily/monthly mileage caps if applicable.

#### Per Diem
- **Per Diem Types**:
  - Meals & Incidentals (M&IE) per diem.
  - Lodging per diem (if no hotel receipt).
  - Full per diem (meals + lodging).

- **Rate Tables**:
  - GSA rates (US federal government standard).
  - Custom corporate rates by location.
  - International rates by city/country.

- **Automatic Calculation**:
  - Based on travel destination and dates from itinerary.
  - Pro-rated for partial days (e.g., 75% for departure/arrival days).
  - Reduced per diem if meals provided (conference, client dinner).

- **Per Diem vs Actual**:
  - Option to claim actual expenses (with receipts) or per diem (no receipts).
  - Policy rules (e.g., per diem for domestic, actual for international).

### Policy Enforcement & Approval

#### Policy Engine
- **Policy Rules**:
  - Expense limits by category (e.g., max $75 for meals, $200 for hotel).
  - Receipt requirements (mandatory for expenses > $25).
  - Advance approval requirements (entertainment, gifts, high-value items).
  - Restricted categories (alcohol, personal items).
  - Spending caps (daily, trip, monthly).

- **Real-Time Validation**:
  - Check expense against policy during entry.
  - Flag violations (out-of-policy) with explanation.
  - Allow exceptions with business justification.

- **Multi-Level Policies**:
  - Global corporate policy.
  - Country/region-specific policies.
  - Department or job level policies (executives vs standard employees).

#### Approval Workflows
- **Approval Hierarchy**:
  - **Level 1: Manager** - Validates business purpose and necessity.
  - **Level 2: Finance** - Reviews policy compliance and coding.
  - **Level 3: Executive** - For high-value or out-of-policy expenses.

- **Approval Rules**:
  - Auto-approve for in-policy expenses < threshold (e.g., $500).
  - Manager approval for standard expenses.
  - Additional approval for out-of-policy or high-value.
  - Delegation during manager absence.

- **Approval Actions**:
  - Approve, reject, send back for clarification.
  - Add comments and request additional documentation.
  - Bulk approval for multiple reports.

#### Expense Report Lifecycle
1. **Draft**: Employee creates expense report and adds line items.
2. **Submit**: Employee submits for approval (validates completeness).
3. **In Approval**: Routed through approval chain.
4. **Approved**: All approvers have approved.
5. **In Payment**: Sent to AP for reimbursement processing.
6. **Paid**: Payment issued to employee.
7. **Posted**: GL posting completed.
8. **Rejected/Recalled**: Sent back to employee for corrections.

### Corporate Card Integration

#### Card Transaction Import
- **Supported Providers**:
  - American Express Corporate, Visa Commercial, Mastercard Corporate.
  - Direct integration (Amex Global Business Travel) or feed aggregators (Yodlee, Plaid).

- **Transaction Feed**:
  - Daily import of card transactions (merchant, date, amount, card holder).
  - Map transactions to employees by card number.
  - Create pending expense items for reconciliation.

- **Transaction Matching**:
  - Auto-match imported transactions to submitted expense line items.
  - Match criteria: amount, date, merchant, cardholder.
  - Flag unmatched transactions for employee review.

#### Card Reconciliation
- **Expense Linking**:
  - Employee matches card transaction to expense line item (attaches receipt, adds details).
  - For itemized expenses (hotel with multiple charges), split transaction.

- **Personal Charges**:
  - Flag personal charges on corporate card.
  - Employee repays via payroll deduction or direct payment.
  - Track personal charge balances and aging.

- **Disputed Charges**:
  - Employee disputes fraudulent or incorrect charges.
  - Dispute workflow (submit to card issuer, track resolution).
  - Adjust expense report when resolved.

- **Reconciliation Status**:
  - **Reconciled**: Transaction matched to expense with receipt.
  - **Pending**: Transaction imported, awaiting employee action.
  - **Personal**: Flagged as personal, repayment pending.
  - **Disputed**: Under dispute with card issuer.

#### Corporate Card Compliance
- **Missing Receipts**:
  - Alert employees for card transactions without receipts (> threshold).
  - Escalate to manager for unresolved items.
  - Policy for missing receipt affidavits.

- **Aging Reports**:
  - Unreconciled transactions report (30, 60, 90+ days old).
  - Personal charge aging and collections.

- **Card Controls**:
  - Spending limits by employee level.
  - Merchant category restrictions (block cash advances, gambling).
  - Cardholder liability and risk management.

### Advance Requests & Settlement

#### Travel Advance
- **Advance Request**:
  - Employee requests cash advance before trip (amount, justification).
  - Approval workflow (manager → finance).
  - Payment via wire transfer, check, or payroll.

- **Advance Tracking**:
  - Record advance as employee liability (due from employee).
  - Link advance to travel request and expense report.

- **Advance Settlement**:
  - Deduct advance from expense reimbursement.
  - If expenses < advance, employee repays difference.
  - If expenses > advance, pay net difference to employee.
  - Clear advance liability in GL.

### Reimbursement Processing

#### Payment Calculation
- **Reimbursement Amount**:
  - Total approved out-of-pocket expenses.
  - Minus: Travel advance, personal charges.
  - Plus: Mileage, per diem.
  - Currency conversion for foreign expenses.

- **Multi-Currency**:
  - Convert foreign currency expenses to home currency.
  - Use corporate exchange rate or daily rate at transaction date.
  - Display original and converted amounts.

#### Payment Execution
- **Payment Methods**:
  - Direct deposit (ACH, SEPA, BACS) to employee bank account.
  - Payroll integration (add to next paycheck).
  - Check or wire transfer for exceptions.

- **Payment Batching**:
  - Batch approved expense reports for payment run.
  - AP integration (ADR-009) for payment processing.
  - Payment file generation (NACHA, SEPA XML).

- **Payment Confirmation**:
  - Notify employee when payment issued.
  - Payment reference and expected deposit date.
  - Payment history and reimbursement tracking.

### GL Posting & Accounting

#### Expense Posting
- **Posting Logic**:
  - **Debit**: Expense account (by category and cost center).
  - **Credit**: AP clearing account (employee payable).
  - When paid: **Debit** AP clearing, **Credit** Cash.

- **Cost Allocation**:
  - Allocate expense to cost center, project, customer, grant.
  - Multiple dimensions (legal entity, department, product, region).
  - Integration with Controlling (ADR-028) for internal orders.

- **Project Accounting**:
  - Billable vs non-billable expense tracking.
  - Mark-up on billable expenses for customer invoicing.
  - Integration with Project Accounting (ADR-036).

#### Corporate Card Posting
- **Posting Timing**:
  - **Option 1: Post when reconciled** (expense report approved).
  - **Option 2: Post at card statement** (accrual, then adjust when reconciled).

- **Posting Logic**:
  - **Debit**: Expense account.
  - **Credit**: Corporate card liability (Amex payable).
  - When statement paid: **Debit** Card liability, **Credit** Cash.

#### VAT/GST Recovery
- **Tax Capture**:
  - Extract VAT/GST from receipts (OCR or manual entry).
  - VAT rate and country identification.

- **Tax Recovery**:
  - Post recoverable VAT to tax receivable account.
  - Export for VAT return filing.
  - Integration with Tax Engine (ADR-030).

### Audit & Compliance

#### Audit Trail
- **Immutable History**:
  - All expense report changes logged (who, when, what).
  - Approval history and comments.
  - Receipt version control (original vs corrected).

- **Supporting Documentation**:
  - Receipt images/PDFs linked to expense lines.
  - Travel justification and approvals.
  - Policy exception rationale.

#### Fraud Detection
- **Deterministic Rules**:
  - **Duplicate Detection**: Same receipt (amount, merchant, date) submitted multiple times.
  - **Policy Violations**: Hotel over $300/night limit, meals over per diem, unauthorized merchant categories.
  - **Missing Receipts**: Transactions over $75 without receipt attachment.
  - **Personal Charges**: Corporate card transactions at blocked merchant categories (gambling, cash advances, personal shopping).
  - **Temporal Anomalies**: Expenses submitted from non-travel dates or outside trip duration.

- **Statistical Outlier Detection**:
  - **Z-Score Analysis**: Flag expenses > 2 standard deviations from employee/department average.
  - **Round-Number Flagging**: Expenses with suspiciously round amounts ($100.00, $500.00) without itemized receipts.
  - **Velocity Checks**: Multiple submissions in short time window (possible duplicate errors).

- **Audit Workflow**:
  - High-risk expenses flagged for audit review before approval.
  - Quarterly audit sampling (5-10% of all expenses).
  - Manager investigation workflow for flagged items.

> **Implementation Note**: Fraud detection uses **rules-based policy enforcement** and **statistical outlier analysis** (Z-score, velocity checks), not machine learning. Deterministic rules are more explainable, easier to tune, and have lower false-positive rates than ML anomaly detection. Future enhancement could add ML-based anomaly detection after accumulating 12+ months of historical expense data.

#### Compliance Reporting
- **Spend Analytics**:
  - T&E spend by employee, department, cost center, project.
  - Category analysis (airfare, hotel, meals).
  - Policy compliance rate and violation trends.
  - Vendor spend concentration.

- **Tax Compliance**:
  - Per diem vs actual tracking for tax reporting.
  - Accountable plan compliance (IRS requirements).
  - Fringe benefit reporting for personal use.

## Data Model (Conceptual)

### Core Entities
- **TravelRequest**: request_id, employee_id, destination, start_date, end_date, purpose, estimated_cost, approval_status, budget_id.
- **TravelItinerary**: itinerary_id, travel_request_id, booking_reference, flight_details, hotel_details, car_rental_details, total_cost.
- **ExpenseReport**: report_id, employee_id, report_date, status (draft, submitted, approved, paid), total_amount, reimbursement_amount, advance_amount.
- **ExpenseLineItem**: line_id, report_id, expense_date, category, merchant, amount, currency, payment_method, receipt_url, project_id, cost_center_id, billable_flag.
- **Receipt**: receipt_id, line_id, image_url, ocr_data (merchant, date, amount, tax), upload_date.
- **MileageEntry**: mileage_id, report_id, date, start_location, end_location, distance, rate, amount, vehicle_type.
- **PerDiemEntry**: per_diem_id, report_id, location, date, rate_type (meals, lodging, full), rate, amount, days.
- **CorporateCardTransaction**: transaction_id, card_number, employee_id, merchant, transaction_date, amount, currency, reconciliation_status, linked_expense_line_id.
- **TravelAdvance**: advance_id, employee_id, travel_request_id, amount, payment_date, settlement_status, outstanding_balance.
- **ApprovalHistory**: approval_id, report_id, approver_id, approval_level, action (approve, reject, send_back), comments, approval_date.
- **PolicyViolation**: violation_id, report_id, line_id, policy_rule, violation_type, justification, exception_approved.

### Relationships
- ExpenseReport → ExpenseLineItem (one-to-many)
- ExpenseLineItem → Receipt (one-to-many)
- ExpenseReport → TravelRequest (many-to-one, optional)
- ExpenseReport → ApprovalHistory (one-to-many)
- ExpenseLineItem → CorporateCardTransaction (one-to-one)
- TravelRequest → TravelItinerary (one-to-one)
- ExpenseReport → TravelAdvance (many-to-one)

## Key Workflows

### Travel & Expense Lifecycle
1. **Travel Request**: Employee submits travel request → Manager approves → Travel booked.
2. **Trip Execution**: Employee travels and incurs expenses.
3. **Expense Capture**: Employee captures receipts via mobile app.
4. **Expense Submission**: Employee creates expense report, adds line items, submits.
5. **Approval**: Manager → Finance → Executive (if needed).
6. **Card Reconciliation**: Match corporate card transactions to expense lines.
7. **Reimbursement**: AP processes payment to employee.
8. **GL Posting**: Post expenses to appropriate accounts and cost centers.

## Integration Points

### HR Integration (ADR-034)
- Employee master data (name, bank account, manager, cost center).
- Payroll integration for reimbursements via paycheck.
- Employee hierarchy for approval routing.

### Finance / GL (ADR-009)
- Expense GL posting (expense accounts, cost centers).
- AP integration for employee reimbursements.
- Corporate card liability accounts.

### Accounts Payable (ADR-009)
- Reimbursement payment processing.
- Payment file generation (ACH, wire).
- Payment status tracking.

### Controlling (ADR-028)
- Cost center and internal order allocation.
- Budget checking and reservations.
- Variance analysis (budget vs actual T&E spend).

### Project Accounting (ADR-036)
- Billable vs non-billable expense tracking.
- Project cost allocation and mark-ups.
- Customer billing for reimbursable expenses.

### Procurement (ADR-023)
- Preferred vendor and negotiated rates.
- Travel policy compliance (preferred airlines, hotels).

### Tax Engine (ADR-030)
- VAT/GST calculation and recovery.
- Tax jurisdiction determination.
- Tax reporting and filing.

### Treasury (ADR-026)
- Corporate card payment processing.
- Travel advance disbursements.
- Cash flow forecasting (T&E spend projections).

### Analytics (ADR-016)
- T&E spend dashboards and analytics.
- Policy compliance reporting.
- Employee spending patterns and benchmarking.

### Master Data Governance (ADR-027)
- Employee data quality and validation.
- Vendor/merchant master data.
- Cost center and GL account mapping.

### Workflow Engine (ADR-046)
- Travel request and expense report approval workflows.
- Policy exception routing.
- Escalations and delegation.

## Non-Functional Requirements

### Performance
- **Expense submission latency**: p95 < 5 seconds (including OCR).
- **Approval workflow latency**: p95 < 10 seconds per approval action.
- **Reimbursement processing**: p95 < 3 business days from approval to payment.

### Scalability
- Support 100K+ employees submitting expenses.
- Handle 10M+ expense line items per year.
- 1M+ corporate card transactions per month.

### Reliability
- **System availability**: 99.9% uptime during business hours.
- **Data accuracy**: 99.95% OCR accuracy for receipt extraction.
- **Payment accuracy**: 100% correct reimbursement amounts.

### Security
- Employee bank account PII protection (encryption, access controls).
- Receipt images encrypted at rest.
- Role-based access control (employees, managers, finance, auditors).
- Audit logging for all expense modifications.

### Compliance
- **Tax compliance**: IRS accountable plan requirements.
- **Data retention**: 7-year retention for audit and tax.
- **Privacy**: GDPR/CCPA compliance for employee PII.

## KPIs and SLOs

### Operational KPIs
| KPI | Target | Measurement |
|-----|--------|-------------|
| **Expense submission time** | <= 10 days post-trip | Average days from trip end to submission |
| **Approval cycle time** | <= 3 business days | Submission to final approval |
| **Reimbursement cycle time** | <= 5 business days | Submission to payment |
| **Policy compliance rate** | >= 95% | In-policy expenses / Total expenses |
| **Receipt compliance** | >= 98% | Expenses with receipts (where required) |
| **Card reconciliation rate** | >= 95% within 30 days | Reconciled transactions / Total transactions |
| **Advance settlement rate** | >= 90% within 60 days | Settled advances / Total advances |

### Employee Satisfaction
| Metric | Target |
|--------|--------|
| **Mobile app rating** | >= 4.5/5 stars |
| **Help desk tickets** | <= 2% of submissions |
| **Reimbursement satisfaction** | >= 90% (survey) |

### Technical SLOs
| Service | SLO | Measurement |
|---------|-----|-------------|
| **OCR processing** | p95 < 5s | Receipt upload to data extraction |
| **Expense report submission** | p95 < 3s | Submit button to confirmation |
| **Approval action** | p95 < 2s | Approve/reject action to status update |
| **Mobile app sync** | p95 < 10s | Offline to online data sync |

## Alternatives Considered

### 1. Extend ADR-034 (Basic T&E)
- **Rejected**: ADR-034 provides only basic reimbursement. Enterprises need travel booking, policy enforcement, card reconciliation, and advanced workflows.

### 2. Use Standalone T&E System (Concur, Expensify, Coupa)
- **Rejected**: Weak integration with ERP financials and project accounting. Requires duplicate GL account mapping and reconciliation overhead.

### 3. Manual Expense Processing
- **Rejected**: Not scalable, error-prone, slow reimbursements, poor policy enforcement, limited audit trail.

### 4. Embed in Procurement (ADR-023)
- **Rejected**: T&E is employee-centric (reimbursements, personal cards), not vendor-centric (POs, invoices).

## Consequences

### Positive
- Streamlines travel booking and expense submission (mobile-first).
- Enforces policy compliance in real-time (reduces out-of-policy spend).
- Automates corporate card reconciliation (reduces manual effort by 80%).
- Accelerates reimbursements (improves employee satisfaction).
- Provides CFO visibility into T&E spend and trends.
- Integrates with ERP financials (single source of truth for expenses).

### Negative / Risks
- Requires TMC integration (Concur, Egencia) for travel booking.
- OCR accuracy dependent on receipt quality (handwritten receipts fail).
- Corporate card integration complexity (multiple card issuers, data formats).
- Employee training required for policy changes and system adoption.

### Neutral
- Advanced tier feature (core T&E in ADR-034, full T&E in ADR-054).
- Can be phased: Expense Management → Travel Booking → Card Reconciliation → Advanced Analytics.

## Implementation Phases

### Phase 1: Expense Management (Core)
- Expense report creation and submission.
- Receipt upload and OCR.
- Basic approval workflows.
- Mileage and per diem calculations.
- Reimbursement processing and GL posting.

### Phase 2: Travel Booking (Advanced)
- Travel request and approval.
- TMC integration (Concur, Egencia).
- Itinerary management.
- Travel policy enforcement.

### Phase 3: Corporate Card (Advanced)
- Card transaction import (Amex, Visa, Mastercard).
- Transaction matching and reconciliation.
- Personal charge tracking.
- Compliance reporting.

### Phase 4: Analytics & Optimization (Advanced)
- T&E spend dashboards.
- Policy compliance analytics.
- Fraud detection and anomaly alerts.
- Vendor spend optimization.

## References
- **ADR-034**: HR Integration & Payroll Events (basic T&E workflows).
- **ADR-009**: Financial Accounting Domain (GL posting, AP integration).
- **ADR-028**: Controlling (cost center allocation, budget management).
- **ADR-036**: Project Accounting (billable expenses, customer billing).
- **ADR-030**: Tax Engine & Compliance (VAT recovery).
- **ADR-046**: Workflow & Approval Engine (approval routing).
- **ADR-016**: Analytics & Reporting Architecture (T&E dashboards).

## Decision Log
- **2026-02-03**: Initial draft created to extend ADR-034 with comprehensive T&E capabilities filling identified 30% gap in travel and expense management.
