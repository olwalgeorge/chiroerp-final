# Sales Core Domain Layer

> Part of [Sales Core](../sales-core.md)

## Directory Structure

```
core-domain/
└── src/main/kotlin/com.erp.sales.core.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Sales Order (`model/order/`)

```
├── order/
│   ├── SalesOrder.kt               # Aggregate Root
│   ├── SalesOrderId.kt
│   ├── OrderType.kt                # Standard, Contract, Subscription
│   ├── OrderStatus.kt              # Draft, Submitted, Approved, Allocated, Fulfilled, Invoiced, Cancelled
│   ├── OrderLine.kt                # Entity
│   ├── OrderLineId.kt
│   ├── AllocationStatus.kt         # Pending, Allocated, Backordered
│   ├── DeliveryPlan.kt             # Entity
│   ├── PricingSnapshot.kt          # Value Object
│   ├── TaxSummary.kt               # Value Object
│   ├── OrderHold.kt                # Entity
│   └── CustomerReference.kt
```

**SalesOrder Aggregate**:
```kotlin
class SalesOrder private constructor(
    val id: SalesOrderId,
    val customerId: String,
    val type: OrderType,
    var status: OrderStatus,
    val lines: MutableList<OrderLine>,
    var pricing: PricingSnapshot,
    var taxSummary: TaxSummary,
    val deliveryPlans: MutableList<DeliveryPlan>
) {
    fun submit(): SalesOrderSubmittedEvent
    fun approve(approverId: String): SalesOrderApprovedEvent
    fun allocate(): SalesOrderAllocatedEvent
    fun placeHold(reason: String): OrderHoldPlacedEvent
    fun releaseHold(): OrderHoldReleasedEvent
    fun markFulfilled(): SalesOrderFulfilledEvent
    fun cancel(reason: String): SalesOrderCancelledEvent
}
```

---

### Quote (`model/quote/`)

```
├── quote/
│   ├── Quote.kt                    # Aggregate Root
│   ├── QuoteId.kt
│   ├── QuoteLine.kt                # Entity
│   ├── QuoteStatus.kt              # Draft, Sent, Accepted, Expired, Cancelled
│   ├── ExpiryDate.kt               # Value Object
│   └── QuoteTerms.kt               # Payment/Delivery terms
```

---

### Approval & Holds (`model/approval/`)

```
├── approval/
│   ├── ApprovalRequest.kt          # Entity
│   ├── ApprovalStatus.kt           # Pending, Approved, Rejected
│   └── ApprovalPolicy.kt           # Threshold-based rules
```

---

## Domain Events

```
events/
├── SalesOrderCreatedEvent.kt
├── SalesOrderSubmittedEvent.kt
├── SalesOrderApprovedEvent.kt
├── SalesOrderAllocatedEvent.kt
├── SalesOrderCancelledEvent.kt
├── SalesOrderFulfilledEvent.kt
├── OrderHoldPlacedEvent.kt
├── OrderHoldReleasedEvent.kt
└── QuoteConvertedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── OrderNotFoundException.kt
├── InvalidOrderStateException.kt
├── PricingExpiredException.kt
├── CreditHoldException.kt
├── AllocationFailedException.kt
├── TaxCalculationException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── OrderPricingService.kt          # Pricing snapshot + promo validation
├── AllocationService.kt            # Reservation/ATP rules
├── CreditCheckService.kt           # Credit hold/limit evaluation
├── ApprovalPolicyService.kt        # Approval thresholds and SoD checks
└── TaxCalculationService.kt        # Jurisdictional tax rules
```

---

## Key Invariants

1. **Order Validity**: Orders must contain at least one order line.
2. **Pricing Snapshot**: Pricing must be valid at submission time.
3. **Credit Holds**: Orders on hold cannot be allocated or invoiced.
4. **Allocation Integrity**: Allocated quantity cannot exceed ordered quantity.
5. **Order Fulfillment**: SalesOrderFulfilled can be emitted only after shipment confirmation.
6. **Closed Periods**: No order posting in closed financial periods.
