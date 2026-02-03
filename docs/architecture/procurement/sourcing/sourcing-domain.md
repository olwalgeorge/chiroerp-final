# Procurement Sourcing Domain Layer

> Part of [Procurement Sourcing & RFQ](../procurement-sourcing.md)

## Directory Structure

```
sourcing-domain/
└── src/main/kotlin/com.erp.procurement.sourcing.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

```
rfq/
├── RFQ.kt                      # Aggregate Root
├── RFQId.kt
├── RFQLine.kt                  # Entity
├── RFQStatus.kt                # Draft, Issued, Closed

quote/
├── Quote.kt                    # Aggregate Root
├── QuoteId.kt
├── QuoteLine.kt                # Entity
├── QuoteStatus.kt              # Submitted, Evaluated, Rejected

award/
├── AwardDecision.kt            # Aggregate Root
├── AwardDecisionId.kt
├── AwardStatus.kt              # Pending, Approved, Granted
```

---

## Domain Events

```
events/
├── RFQIssuedEvent.kt
├── QuoteSubmittedEvent.kt
├── AwardGrantedEvent.kt
└── AwardRejectedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── RFQClosedException.kt
├── QuoteLateException.kt
└── AwardApprovalRequiredException.kt
```

---

## Domain Services

```
services/
├── QuoteEvaluationService.kt   # Scoring and comparison
└── AwardPolicyService.kt       # Compliance and approvals
```

---

## Key Invariants

1. **RFQ Closed**: Quotes cannot be submitted after RFQ is closed.
2. **Award Approval**: Award requires approval above threshold.
