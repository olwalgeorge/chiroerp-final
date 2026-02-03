# AR Domain Layer

> Part of [Finance - Accounts Receivable](../finance-ar.md)

## Directory Structure

```
ar-domain/
└── src/main/kotlin/com.erp.finance.ar.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Customer Account (`model/customer/`)

```
├── customer/
│   ├── CustomerAccount.kt           # Aggregate Root
│   ├── CustomerAccountId.kt
│   ├── CustomerType.kt              # Individual, Corporate, Government
│   ├── CreditProfile.kt             # Entity
│   ├── CreditLimit.kt
│   ├── CreditStatus.kt              # Good, Warning, Hold, Blocked
│   ├── PaymentTerms.kt              # Net30, Net60, 2/10Net30
│   ├── DunningProfile.kt
│   └── CustomerSegment.kt
```

**CustomerAccount Aggregate**:
```kotlin
class CustomerAccount private constructor(
    val id: CustomerAccountId,
    val customerCode: String,
    val name: String,
    var status: CreditStatus,
    val paymentTerms: PaymentTerms,
    val creditProfile: CreditProfile,
    val segments: Set<CustomerSegment>
) {
    fun updateCreditLimit(limit: CreditLimit): CreditLimitUpdatedEvent
    fun block(reason: String): CustomerBlockedEvent
    fun unblock(): CustomerUnblockedEvent
}
```

---

### Invoice (`model/invoice/`)

```
├── invoice/
│   ├── Invoice.kt                   # Aggregate Root
│   ├── InvoiceId.kt
│   ├── InvoiceLine.kt               # Entity
│   ├── InvoiceLineId.kt
│   ├── InvoiceType.kt               # Standard, Credit, Debit, ProForma
│   ├── InvoiceStatus.kt             # Draft, Posted, PartialPaid, Paid, Cancelled
│   ├── BillingAddress.kt
│   ├── ShippingAddress.kt
│   ├── TaxDetail.kt
│   └── InvoiceReference.kt          # SO, Contract, Project link
```

**Invoice Aggregate**:
```kotlin
class Invoice private constructor(
    val id: InvoiceId,
    val invoiceNumber: String,
    val customerId: CustomerAccountId,
    var status: InvoiceStatus,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate,
    val lines: MutableList<InvoiceLine>,
    val currency: CurrencyCode
) {
    fun post(postingDate: LocalDate): InvoicePostedEvent
    fun cancel(reason: String): InvoiceCancelledEvent
    fun applyPayment(amount: Money): PaymentAppliedEvent

    val totalAmount: Money
    val taxAmount: Money
    val openBalance: Money
}
```

---

### Payment (`model/payment/`)

```
├── payment/
│   ├── Payment.kt                   # Aggregate Root
│   ├── PaymentId.kt
│   ├── PaymentAllocation.kt         # Entity (payment-to-invoice)
│   ├── PaymentMethod.kt             # Cash, Check, Wire, ACH, Card
│   ├── PaymentStatus.kt             # Pending, Applied, Reversed, Bounced
│   ├── BankReference.kt
│   ├── Remittance.kt
│   └── CashDiscount.kt
```

---

### Aging (`model/aging/`)

```
├── aging/
│   ├── AgingBucket.kt               # Value Object
│   ├── AgingSnapshot.kt             # Aggregate Root
│   ├── AgingPeriod.kt               # Current, 1-30, 31-60, 61-90, 90+
│   ├── DaysOutstanding.kt
│   └── AgingCategory.kt
```

---

### Dunning (`model/dunning/`)

```
├── dunning/
│   ├── DunningRun.kt                # Aggregate Root
│   ├── DunningRunId.kt
│   ├── DunningNotice.kt             # Entity
│   ├── DunningLevel.kt              # Reminder, Warning, Final, Legal
│   ├── DunningAction.kt             # Letter, Email, Call, Block
│   └── CollectionCase.kt
```

---

### Credit (`model/credit/`)

```
├── credit/
│   ├── CreditMemo.kt                # Aggregate Root
│   ├── CreditMemoId.kt
│   ├── CreditReason.kt              # Return, Pricing, Quality, Goodwill
│   ├── DebitMemo.kt                 # Aggregate Root
│   └── WriteOff.kt                  # Aggregate Root
```

---

### Lockbox (`model/lockbox/`)

```
└── lockbox/
    ├── LockboxBatch.kt              # Aggregate Root
    ├── LockboxItem.kt               # Entity
    ├── RemittanceMatch.kt
    └── ExceptionItem.kt
```

---

## Domain Events

```
events/
├── InvoicePostedEvent.kt
├── InvoiceCancelledEvent.kt
├── PaymentReceivedEvent.kt
├── PaymentAppliedEvent.kt
├── PaymentReversedEvent.kt
├── CreditMemoIssuedEvent.kt
├── WriteOffApprovedEvent.kt
├── DunningNoticeGeneratedEvent.kt
├── CreditLimitExceededEvent.kt
├── CustomerBlockedEvent.kt
└── AgingSnapshotCreatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── InvoiceNotFoundException.kt
├── PaymentExceedsBalanceException.kt
├── CreditLimitExceededException.kt
├── CustomerBlockedException.kt
├── DuplicateInvoiceException.kt
├── InvalidPaymentAllocationException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── InvoiceValidationService.kt          # Domain Service
├── PaymentAllocationService.kt          # FIFO, LIFO, specific allocation
├── CreditCheckService.kt                # Credit limit validation
├── AgingCalculationService.kt           # Aging bucket calculation
├── DunningSelectionService.kt           # Dunning level selection
├── CashApplicationService.kt            # Auto-match payments
└── WriteOffApprovalService.kt           # Write-off workflow
```

---

## Key Invariants

1. **Payment Balance**: Payment amount <= sum of open invoice balances.
2. **Credit Check**: New orders blocked if customer credit exceeded.
3. **Invoice Immutability**: Posted invoices cannot be modified; use credit memo.
4. **Allocation Integrity**: Sum of allocations equals payment amount.
5. **Dunning Sequence**: Dunning levels progress sequentially.
6. **Period Control**: No postings to closed periods.
