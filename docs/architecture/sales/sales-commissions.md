# Sales Commissions - ADR-025

> **Bounded Context:** `sales-commissions`
> **Port:** `9206` (logical, part of sales-core service)
> **Database:** `chiroerp_sales_core`
> **Kafka Consumer Group:** `sales-core-cg`

## Overview

Sales Commissions calculates **rep payouts based on order performance, payment timing, and commission plans**. It supports accruals, approvals, and payout handoff to Finance/AP or Payroll.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Commission plans, accruals, approvals, payouts |
| **Aggregates** | CommissionPlan, CommissionRule, CommissionStatement, CommissionAccrual |
| **Key Events** | CommissionCalculatedEvent, CommissionApprovedEvent, CommissionPayableCreatedEvent |
| **Integration** | Sales Core, Finance/AP, Finance/AR |
| **Compliance** | SoD approvals for payout release |

## Key Capabilities

- Tiered and quota-based commission plans
- Accruals at fulfillment or payment
- Approval workflow for payouts
- Clawback handling on returns or credit memos

## Domain Model

```
CommissionPlan
├── planId
├── effectiveDate
├── tiers
└── payoutSchedule

CommissionStatement
├── statementId
├── repId
├── period
└── totalAmount
```

## Workflows

1. Sales order fulfilled or paid.
2. Commission calculated per plan.
3. Manager approval (if required).
4. Payable created for Finance/AP or Payroll.

## Domain Events Published

### CommissionCalculatedEvent

**Trigger**: Commission accrued based on fulfillment or payment

```json
{
  "eventType": "CommissionCalculatedEvent",
  "eventId": "evt-5001",
  "timestamp": "2026-02-15T10:30:00Z",
  "aggregateId": "COM-ACC-10230",
  "tenantId": "tenant-001",
  "payload": {
    "accrualId": "COM-ACC-10230",
    "orderId": "SO-10230",
    "repId": "REP-88",
    "amount": 450.00,
    "basis": "FULFILLMENT",
    "planId": "PLAN-Q1-2026",
    "currency": "USD"
  }
}
```

### CommissionApprovedEvent

**Trigger**: Manager approves commission statement for payout

```json
{
  "eventType": "CommissionApprovedEvent",
  "eventId": "evt-5002",
  "timestamp": "2026-03-01T14:00:00Z",
  "aggregateId": "COM-2026-02",
  "tenantId": "tenant-001",
  "payload": {
    "statementId": "COM-2026-02",
    "repId": "REP-88",
    "totalAmount": 1850.00,
    "approvedBy": "MGR-200",
    "period": "2026-02"
  }
}
```

### CommissionPayableCreatedEvent

**Trigger**: Approved commission statement creates AP payable

```json
{
  "eventType": "CommissionPayableCreatedEvent",
  "eventId": "evt-5003",
  "timestamp": "2026-03-01T14:05:00Z",
  "aggregateId": "COM-2026-02",
  "tenantId": "tenant-001",
  "payload": {
    "statementId": "COM-2026-02",
    "repId": "REP-88",
    "totalAmount": 1850.00,
    "vendorId": "VEND-REP-88",
    "paymentTerms": "NET15",
    "currency": "USD",
    "dueDate": "2026-03-15"
  }
}
```

---

## Domain Events Consumed

```
- SalesOrderFulfilledEvent (from Sales Core) -> Calculate commission accruals
- PaymentReceivedEvent (from Finance/AR) -> Release commissions on cash receipt
- CreditMemoIssuedEvent (from Finance/AR) -> Trigger clawback adjustments
- ReturnReceivedEvent (from Sales Returns) -> Adjust commission accruals
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `sales.commissions.commission.calculated` | Sales Commissions | Analytics, Reporting | 6 |
| `sales.commissions.commission.approved` | Sales Commissions | Audit, Analytics | 6 |
| `sales.commissions.commission.payable-created` | Sales Commissions | Finance/AP | 6 |

---

## Integration Points

### Sales Core → Commissions (Accrual Trigger)

**Flow**: Order fulfillment triggers commission calculation

```
SalesOrderFulfilledEvent
  ↓
Sales Commissions Service
  ↓ Evaluate commission plan (tiers, quotas)
  ↓ Calculate accrual amount
  ↓
CommissionCalculatedEvent → Analytics
```

### Commissions → Finance/AP (Payout Flow)

**Detailed Workflow**:

1. **Monthly Statement Preparation**
   - Aggregate all approved accruals for rep/period
   - Generate `CommissionStatement` (status: DRAFT)
   - Apply clawbacks for returns or credit memos

2. **Manager Approval**
   - Statement submitted for approval
   - SoD control: Approver ≠ Rep's manager (if required)
   - On approval: `CommissionApprovedEvent` published

3. **AP Payable Creation**
   - Commissions service creates vendor invoice in AP
   - Sales rep mapped to vendor account (`VEND-REP-88`)
   - `CommissionPayableCreatedEvent` triggers AP invoice posting (see [AP Events - CommissionPayableCreatedEvent](../finance/ap/ap-events.md#commissionpayablecreatedevent)):
     ```
     DR: Commission Expense (6500)        $1,850
     CR: AP - Commissions Payable (2105)  $1,850
     ```

4. **Payment Execution**
   - AP payment run includes commission payables
   - Payment sent to rep's bank account
   - Finance/AP publishes `PaymentSentEvent`
   - Accounting:
     ```
     DR: AP - Commissions Payable (2105)  $1,850
     CR: Cash/Bank (1010)                  $1,850
     ```

5. **Accrual Liability Tracking**
   - Outstanding accruals tracked as liability until paid
   - Monthly reconciliation between accruals and AP balance
   - Audit trail: Accrual → Statement → AP Invoice → Payment

### Finance/AR → Commissions (Cash-Basis Release)

**Flow**: For plans with cash-basis commission triggers

```
PaymentReceivedEvent (from AR)
  ↓
Sales Commissions Service
  ↓ Match payment to order
  ↓ Release held commission accrual
  ↓
CommissionCalculatedEvent (basis: PAYMENT)
```

### Clawback Handling

**Flow**: Customer returns or credit memos reduce commission

```
CreditMemoIssuedEvent (from AR)
  ↓
Sales Commissions Service
  ↓ Identify related commission accrual
  ↓ Create negative adjustment
  ↓
CommissionAdjustedEvent → Reduce next statement
```

## Integration Examples

### Commission Payable → Finance/AP Invoice Posting

**Flow**: `CommissionPayableCreatedEvent` is consumed by AP to create a vendor invoice for the rep.

```json
{
  "eventType": "VendorInvoicePostedEvent",
  "eventId": "evt-ap-9101",
  "timestamp": "2026-03-01T14:06:00Z",
  "aggregateId": "AP-INV-7788",
  "tenantId": "tenant-001",
  "payload": {
    "invoiceId": "AP-INV-7788",
    "vendorId": "VEND-REP-88",
    "amount": 1850.00,
    "currency": "USD",
    "sourceEventId": "evt-5003"
  }
}
```

### Commission Payout → Finance/AP Payment

**Flow**: AP payment run issues the commission payout and publishes the payment event.

```json
{
  "eventType": "PaymentSentEvent",
  "eventId": "evt-ap-9102",
  "timestamp": "2026-03-15T09:00:00Z",
  "aggregateId": "AP-PAY-5541",
  "tenantId": "tenant-001",
  "payload": {
    "paymentId": "AP-PAY-5541",
    "vendorId": "VEND-REP-88",
    "amount": 1850.00,
    "currency": "USD",
    "sourceInvoiceId": "AP-INV-7788"
  }
}
```

## API Endpoints (Examples)

### POST /api/v1/sales/commissions/{statementId}/approve

Approve commission statement for payout

**Request**:
```http
POST /api/v1/sales/commissions/COM-2026-02/approve
Content-Type: application/json
Authorization: Bearer {token}

{
  "approvedBy": "USR-200",
  "notes": "Monthly payout approval for Feb 2026"
}
```

**Success Response (200 OK)**:
```json
{
  "statementId": "COM-2026-02",
  "status": "APPROVED",
  "approvedAt": "2026-03-01T14:00:00Z",
  "approvedBy": "USR-200"
}
```

**Error Response (403 Forbidden)** - Per ADR-010:
```json
{
  "errorCode": "AUTHORIZATION_FAILED",
  "message": "User USR-200 lacks APPROVE_COMMISSION_STATEMENT permission",
  "timestamp": "2026-03-01T14:00:00Z",
  "requestId": "req-abc-123",
  "details": {
    "requiredPermission": "APPROVE_COMMISSION_STATEMENT",
    "resourceId": "COM-2026-02"
  }
}
```

### GET /api/v1/sales/commissions/statements

List commission statements by rep and period

**Request**:
```http
GET /api/v1/sales/commissions/statements?repId=REP-88&period=2026-02
Authorization: Bearer {token}
```

**Success Response (200 OK)**:
```json
{
  "statements": [
    {
      "statementId": "COM-2026-02",
      "repId": "REP-88",
      "period": "2026-02",
      "totalAmount": 1850.00,
      "status": "APPROVED",
      "accrualCount": 14
    }
  ]
}
```

### POST /api/v1/sales/commissions/accruals/{accrualId}/adjust

Adjust commission accrual (e.g., clawback for return)

**Request**:
```http
POST /api/v1/sales/commissions/accruals/COM-ACC-10230/adjust
Content-Type: application/json
Authorization: Bearer {token}

{
  "adjustmentAmount": -450.00,
  "reason": "Customer return - RMA-5001",
  "creditMemoId": "CM-2026-101"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Accrual adjustment validation failed",
  "timestamp": "2026-03-02T10:15:00Z",
  "requestId": "req-def-456",
  "details": {
    "violations": [
      {
        "field": "adjustmentAmount",
        "constraint": "Cannot exceed original accrual amount",
        "rejectedValue": -600.00,
        "originalAmount": -450.00
      }
    ]
  }
}
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| API availability | 99.7% | < 99.5% |
| Command latency | < 1s p95 | > 3s |
| Event processing lag | < 30s p95 | > 2m |

## Compliance & Controls

- Approval workflow required for payouts
- Audit trail for accrual adjustments and clawbacks

## References

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md)
- [Finance/AP Events - CommissionPayableCreatedEvent](../finance/ap/ap-events.md#commissionpayablecreatedevent) - AP integration point for commission payouts
