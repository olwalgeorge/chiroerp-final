# Sales Rebates & Incentives - ADR-025

> **Bounded Context:** `sales-rebates`  
> **Port:** `9207` (logical, part of sales-core service)  
> **Database:** `chiroerp_sales_core`  
> **Kafka Consumer Group:** `sales-core-cg`

## Overview

Sales Rebates manages **customer incentive programs, accruals, and rebate settlements**. It supports volume-based rebates, promotional incentives, and claim validation, integrating with Finance/AR for credit memo issuance.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Rebates, incentives, accruals, claims |
| **Aggregates** | RebateProgram, RebateAgreement, RebateAccrual, RebateClaim |
| **Key Events** | RebateAccruedEvent, RebateApprovedEvent, RebateIssuedEvent |
| **Integration** | Sales Core, Pricing, Finance/AR |
| **Compliance** | Audit controls for incentive approvals |

## Key Capabilities

- Program setup (volume, growth, tiered incentives)
- Accrual calculations per order
- Claim validation and approval workflows
- Settlement via credit memo or payment

## Domain Model

```
RebateProgram
├── programId
├── effectivePeriod
├── tiers
└── calculationMethod

RebateClaim
├── claimId
├── customerId
├── period
└── status: Submitted | Approved | Issued
```

## Workflows

1. Program configured with tiers and eligibility rules.
2. Orders fulfilled and rebates accrued.
3. Customer submits claim or auto-approval triggered.
4. Rebate issued via credit memo or payment.

## Domain Events Published

### RebateAccruedEvent

**Trigger**: Order fulfillment triggers rebate accrual

```json
{
  "eventType": "RebateAccruedEvent",
  "eventId": "evt-6001",
  "timestamp": "2026-02-10T11:20:00Z",
  "aggregateId": "REB-ACC-10310",
  "tenantId": "tenant-001",
  "payload": {
    "accrualId": "REB-ACC-10310",
    "orderId": "SO-10310",
    "customerId": "CUST-205",
    "amount": 75.00,
    "programId": "REBATE-Q1",
    "tier": "VOLUME-TIER-2",
    "currency": "USD"
  }
}
```

### RebateApprovedEvent

**Trigger**: Rebate claim approved for settlement

```json
{
  "eventType": "RebateApprovedEvent",
  "eventId": "evt-6002",
  "timestamp": "2026-03-05T09:30:00Z",
  "aggregateId": "REB-2026-02",
  "tenantId": "tenant-001",
  "payload": {
    "claimId": "REB-2026-02",
    "customerId": "CUST-205",
    "amount": 420.00,
    "approvedBy": "USR-300",
    "period": "2026-02",
    "programId": "REBATE-Q1"
  }
}
```

### RebateIssuedEvent

**Trigger**: Rebate settled via credit memo or payment

```json
{
  "eventType": "RebateIssuedEvent",
  "eventId": "evt-6003",
  "timestamp": "2026-03-05T09:35:00Z",
  "aggregateId": "REB-2026-02",
  "tenantId": "tenant-001",
  "payload": {
    "claimId": "REB-2026-02",
    "customerId": "CUST-205",
    "amount": 420.00,
    "settlementMethod": "CREDIT_MEMO",
    "creditMemoId": "CM-2026-REB-205",
    "currency": "USD"
  }
}
```

---

## Domain Events Consumed

```
- SalesOrderFulfilledEvent (from Sales Core) -> Accrue rebate
- PriceCalculatedEvent (from Pricing) -> Validate eligibility
- ReturnReceivedEvent (from Returns) -> Adjust accruals
- CustomerBlockedEvent (from Finance/AR) -> Suspend rebate eligibility
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `sales.rebates.rebate.accrued` | Sales Rebates | Analytics, Reporting | 6 |
| `sales.rebates.rebate.approved` | Sales Rebates | Finance/AR, Audit | 6 |
| `sales.rebates.rebate.issued` | Sales Rebates | Finance/AR, Analytics | 6 |

---

## Integration Points

### Sales Core → Rebates (Accrual Trigger)

**Flow**: Order fulfillment triggers rebate evaluation

```
SalesOrderFulfilledEvent
  ↓
Sales Rebates Service
  ↓ Evaluate program eligibility (customer, product, period)
  ↓ Calculate tier-based rebate amount
  ↓
RebateAccruedEvent → Analytics
```

### Rebates → Finance/AR (Credit Memo Settlement Flow)

**Detailed Workflow**:

1. **Accrual Accumulation**
   - Each fulfilled order triggers rebate accrual
   - Accruals tracked per customer/program/period
   - Volume or value thresholds determine tier
   - `RebateAccruedEvent` published for each accrual

2. **Claim Submission**
   - Customer submits claim (manual or auto-triggered)
   - System validates:
     - Customer eligibility (not blocked, active program)
     - Accrual balance matches claim amount
     - Period end date passed (for retrospective programs)
   - `RebateClaim` created with status: SUBMITTED

3. **Approval Workflow**
   - High-value claims (> $5,000) require manager approval
   - SoD control: Approver ≠ Program administrator
   - On approval: `RebateApprovedEvent` published
   - Claim status: SUBMITTED → APPROVED

4. **AR Credit Memo Issuance**
   - Rebates service creates credit memo in AR
   - `RebateIssuedEvent` triggers AR posting (see [AR Events - RebateIssuedEvent](../finance/ar/ar-events.md#rebateissuedevent)):
     ```
     DR: Rebate Expense (6600)              $420
     CR: Accounts Receivable (1200)         $420
     ```
   - Credit memo reduces customer balance
   - Customer can use credit against future invoices

5. **Alternative: Cash Payment Settlement**
   - For programs with cash payout option:
     ```
     DR: Rebate Expense (6600)              $420
     CR: Cash/Bank (1010)                    $420
     ```
   - Payment sent to customer bank account
   - `PaymentSentEvent` published by Finance/AP

6. **Accrual Liability Tracking**
   - Outstanding accruals tracked as liability until settled
   - Monthly reconciliation between accruals and AR credits
   - Audit trail: Accrual → Claim → Credit Memo → Customer Credit

### Sales Pricing → Rebates (Eligibility Check)

**Flow**: Price calculation checks for rebate program eligibility

```
PriceCalculatedEvent (from Pricing)
  ↓
Sales Rebates Service
  ↓ Check customer enrolled in active programs
  ↓ Validate product eligibility
  ↓
Rebate program info cached for order processing
```

## Integration Examples

### Rebate Settlement → Finance/AR Credit Memo

**Flow**: `RebateIssuedEvent` is consumed by AR to create and post a credit memo.

```json
{
  "eventType": "CreditMemoIssuedEvent",
  "eventId": "evt-ar-3101",
  "timestamp": "2026-03-05T09:36:00Z",
  "aggregateId": "CM-2026-REB-205",
  "tenantId": "tenant-001",
  "payload": {
    "creditMemoId": "CM-2026-REB-205",
    "customerId": "CUST-205",
    "amount": 420.00,
    "currency": "USD",
    "sourceClaimId": "REB-2026-02"
  }
}
```

### Rebate Settlement → Cash Payment (Optional)

**Flow**: For cash settlements, AP issues payment and publishes the payment event.

```json
{
  "eventType": "PaymentSentEvent",
  "eventId": "evt-ap-9203",
  "timestamp": "2026-03-06T10:15:00Z",
  "aggregateId": "AP-PAY-5599",
  "tenantId": "tenant-001",
  "payload": {
    "paymentId": "AP-PAY-5599",
    "vendorId": "VEND-CUST-205",
    "amount": 420.00,
    "currency": "USD",
    "sourceClaimId": "REB-2026-02"
  }
}
```

### Adjustment Handling

**Flow**: Returns or cancellations reduce rebate accruals

```
ReturnReceivedEvent (from Sales Returns)
  ↓
Sales Rebates Service
  ↓ Identify related rebate accrual
  ↓ Create negative adjustment
  ↓ Recalculate tier if volume threshold impacted
  ↓
RebateAdjustedEvent → Update accrual balance
```

### Multi-Tier Rebate Example

**Scenario**: Volume-based quarterly rebate program

```
Program: REBATE-Q1
- Tier 1: $0-$10K      → 2% rebate
- Tier 2: $10K-$50K    → 3% rebate
- Tier 3: $50K+        → 5% rebate

Customer CUST-205 Quarterly Orders:
- Jan: $15K → 3% = $450 accrued
- Feb: $18K → 3% = $540 accrued (cumulative $33K)
- Mar: $22K → 3% = $660 accrued (cumulative $55K, crosses Tier 3!)

End of Q1:
- Recalculate entire quarter at Tier 3 (5%)
- Total rebate: $55K × 5% = $2,750
- Already accrued: $1,650
- Adjustment: +$1,100 (true-up to Tier 3)
- Final claim: $2,750
```

## API Endpoints (Examples)

### POST /api/v1/sales/rebates/claims

Submit rebate claim for approval and settlement

**Request**:
```http
POST /api/v1/sales/rebates/claims
Content-Type: application/json
Authorization: Bearer {token}

{
  "customerId": "CUST-205",
  "period": "2026-02",
  "programId": "REBATE-Q1",
  "claimAmount": 420.00,
  "settlementMethod": "CREDIT_MEMO"
}
```

**Success Response (201 Created)**:
```json
{
  "claimId": "REB-2026-02",
  "customerId": "CUST-205",
  "status": "SUBMITTED",
  "claimAmount": 420.00,
  "accruedBalance": 420.00,
  "submittedAt": "2026-03-05T09:00:00Z"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Rebate claim validation failed",
  "timestamp": "2026-03-05T09:00:00Z",
  "requestId": "req-xyz-789",
  "details": {
    "violations": [
      {
        "field": "claimAmount",
        "constraint": "Claim amount exceeds accrued balance",
        "rejectedValue": 600.00,
        "accruedBalance": 420.00
      }
    ]
  }
}
```

### GET /api/v1/sales/rebates/accruals

Query rebate accruals by customer and program

**Request**:
```http
GET /api/v1/sales/rebates/accruals?customerId=CUST-205&programId=REBATE-Q1&period=2026-Q1
Authorization: Bearer {token}
```

**Success Response (200 OK)**:
```json
{
  "accruals": [
    {
      "accrualId": "REB-ACC-10310",
      "orderId": "SO-10310",
      "customerId": "CUST-205",
      "amount": 75.00,
      "tier": "VOLUME-TIER-2",
      "accruedAt": "2026-02-10T11:20:00Z"
    }
  ],
  "summary": {
    "totalAccrued": 420.00,
    "totalClaimed": 0.00,
    "availableBalance": 420.00,
    "currentTier": "VOLUME-TIER-2"
  }
}
```

### POST /api/v1/sales/rebates/claims/{claimId}/approve

Approve rebate claim for settlement

**Request**:
```http
POST /api/v1/sales/rebates/claims/REB-2026-02/approve
Content-Type: application/json
Authorization: Bearer {token}

{
  "approvedBy": "USR-300",
  "notes": "Q1 volume rebate approved"
}
```

**Success Response (200 OK)**:
```json
{
  "claimId": "REB-2026-02",
  "status": "APPROVED",
  "approvedAt": "2026-03-05T09:30:00Z",
  "approvedBy": "USR-300"
}
```

**Error Response (403 Forbidden)** - Per ADR-010:
```json
{
  "errorCode": "AUTHORIZATION_FAILED",
  "message": "User USR-300 lacks APPROVE_REBATE_CLAIM permission",
  "timestamp": "2026-03-05T09:30:00Z",
  "requestId": "req-ghi-321",
  "details": {
    "requiredPermission": "APPROVE_REBATE_CLAIM",
    "resourceId": "REB-2026-02",
    "claimAmount": 420.00,
    "approvalThreshold": 5000.00
  }
}
```

### GET /api/v1/sales/rebates/programs/{programId}

Retrieve rebate program details and tier structure

**Request**:
```http
GET /api/v1/sales/rebates/programs/REBATE-Q1
Authorization: Bearer {token}
```

**Success Response (200 OK)**:
```json
{
  "programId": "REBATE-Q1",
  "name": "Q1 2026 Volume Rebate",
  "effectivePeriod": {
    "start": "2026-01-01",
    "end": "2026-03-31"
  },
  "calculationMethod": "VOLUME_TIERED",
  "tiers": [
    {
      "tier": "VOLUME-TIER-1",
      "thresholdMin": 0,
      "thresholdMax": 10000,
      "rebatePercent": 2.0
    },
    {
      "tier": "VOLUME-TIER-2",
      "thresholdMin": 10000,
      "thresholdMax": 50000,
      "rebatePercent": 3.0
    },
    {
      "tier": "VOLUME-TIER-3",
      "thresholdMin": 50000,
      "thresholdMax": null,
      "rebatePercent": 5.0
    }
  ],
  "settlementOptions": ["CREDIT_MEMO", "CASH_PAYMENT"],
  "status": "ACTIVE"
}
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| API availability | 99.7% | < 99.5% |
| Command latency | < 1s p95 | > 3s |
| Event processing lag | < 30s p95 | > 2m |

## Compliance & Controls

- Approval required for high-value rebates
- Audit trail for program changes and claims

## References

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
- [Finance/AR Events - RebateIssuedEvent](../finance/ar/ar-events.md#rebateissuedevent) - AR integration point for rebate credit memos
