# AP Domain Layer

> Part of [Finance - Accounts Payable](../finance-ap.md)

## Directory Structure

```
ap-domain/
└── src/main/kotlin/com.erp.finance.ap.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Vendor Account (`model/vendor/`)

```
├── vendor/
│   ├── VendorAccount.kt             # Aggregate Root
│   ├── VendorAccountId.kt
│   ├── VendorType.kt                # Supplier, Contractor, ServiceProvider
│   ├── VendorStatus.kt              # Active, OnHold, Blocked, Inactive
│   ├── PaymentTerms.kt              # Net30, Net60, 2/10Net30, Immediate
│   ├── PaymentMethod.kt             # Check, ACH, Wire, Card
│   ├── BankDetails.kt               # Entity - bank account info
│   ├── TaxInfo.kt                   # W9/W8, VAT registration
│   ├── VendorAddress.kt             # Remittance address
│   └── VendorContact.kt
```

**VendorAccount Aggregate**:
```kotlin
class VendorAccount private constructor(
    val id: VendorAccountId,
    val vendorCode: String,
    val name: String,
    val type: VendorType,
    var status: VendorStatus,
    val paymentTerms: PaymentTerms,
    val defaultPaymentMethod: PaymentMethod,
    val bankDetails: List<BankDetails>,
    val taxInfo: TaxInfo,
    val addresses: List<VendorAddress>,
    val creditLimit: Money?,
    val currency: CurrencyCode
) {
    fun block(reason: String): VendorBlockedEvent
    fun unblock(): VendorUnblockedEvent
    fun updateBankDetails(bankDetails: BankDetails): BankDetailsUpdatedEvent
}
```

---

### Vendor Invoice (`model/invoice/`)

```
├── invoice/
│   ├── VendorInvoice.kt             # Aggregate Root
│   ├── VendorInvoiceId.kt
│   ├── InvoiceLine.kt               # Entity
│   ├── InvoiceLineId.kt
│   ├── InvoiceType.kt               # Standard, Credit, Debit, Prepayment
│   ├── InvoiceStatus.kt             # Draft, PendingMatch, Matched, Posted, Paid, Cancelled
│   ├── MatchStatus.kt               # Unmatched, PartialMatch, FullMatch, Exception
│   ├── MatchType.kt                 # TwoWay (PO-Invoice), ThreeWay (PO-GRN-Invoice)
│   ├── MatchTolerance.kt            # Price/Qty variance tolerance
│   ├── InvoiceReference.kt          # PO, GRN, Contract references
│   ├── TaxDetail.kt
│   └── WithholdingTax.kt
```

**VendorInvoice Aggregate**:
```kotlin
class VendorInvoice private constructor(
    val id: VendorInvoiceId,
    val invoiceNumber: String,
    val vendorId: VendorAccountId,
    val type: InvoiceType,
    var status: InvoiceStatus,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate,
    val lines: MutableList<InvoiceLine>,
    var matchStatus: MatchStatus,
    val matchType: MatchType,
    val poReferences: List<PurchaseOrderReference>,
    val grnReferences: List<GoodsReceiptReference>,
    val currency: CurrencyCode,
    val exchangeRate: ExchangeRate?
) {
    // Invariant: Cannot post if not fully matched (when 3-way match required)
    fun post(): VendorInvoicePostedEvent
    fun performThreeWayMatch(po: PurchaseOrder, grn: GoodsReceipt): MatchResult
    fun applyPayment(amount: Money): PaymentAppliedEvent
    fun cancel(reason: String): VendorInvoiceCancelledEvent

    val totalAmount: Money
    val taxAmount: Money
    val withholdingAmount: Money
    val netPayable: Money
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
│   ├── PaymentMethod.kt             # Check, ACH, Wire, Card
│   ├── PaymentStatus.kt             # Proposed, Approved, Sent, Cleared, Returned
│   ├── BankReference.kt
│   ├── CheckDetails.kt              # Check number, void info
│   ├── RemittanceAdvice.kt
│   └── PaymentDiscount.kt           # Early payment discount
```

---

### Payment Run (`model/paymentrun/`)

```
├── paymentrun/
│   ├── PaymentRun.kt                # Aggregate Root
│   ├── PaymentRunId.kt
│   ├── PaymentProposal.kt           # Entity - proposed payments
│   ├── PaymentRunStatus.kt          # Draft, Proposed, Approved, Executing, Completed
│   ├── PaymentSelection.kt          # Criteria for invoice selection
│   ├── PaymentRunSchedule.kt        # Scheduled execution
│   └── PaymentBatch.kt              # Grouped by bank/method
```

**PaymentRun Aggregate**:
```kotlin
class PaymentRun private constructor(
    val id: PaymentRunId,
    val runDate: LocalDate,
    var status: PaymentRunStatus,
    val selection: PaymentSelection,
    val proposals: MutableList<PaymentProposal>,
    val batches: MutableList<PaymentBatch>
) {
    fun generateProposals(dueInvoices: List<VendorInvoice>): List<PaymentProposal>
    fun approve(approverId: UserId): PaymentRunApprovedEvent
    fun execute(): PaymentRunExecutedEvent
    fun addToProposal(invoice: VendorInvoice)
    fun removeFromProposal(invoiceId: VendorInvoiceId)
}
```

---

### Matching (`model/matching/`)

```
├── matching/
│   ├── MatchingDocument.kt          # Aggregate Root
│   ├── MatchingDocumentId.kt
│   ├── MatchLine.kt                 # Entity
│   ├── MatchResult.kt               # Value Object
│   ├── PriceVariance.kt
│   ├── QuantityVariance.kt
│   ├── MatchException.kt            # Entity - variance exceptions
│   └── MatchResolution.kt           # Accept, Reject, Adjust
```

---

### Aging (`model/aging/`)

```
├── aging/
│   ├── APAgingSnapshot.kt           # Aggregate Root
│   ├── AgingBucket.kt               # Current, 1-30, 31-60, 61-90, 90+
│   ├── VendorAgingDetail.kt         # Entity
│   └── AgingPeriod.kt
```

---

## Domain Events

```
events/
├── VendorInvoicePostedEvent.kt
├── VendorInvoiceCancelledEvent.kt
├── VendorInvoiceMatchedEvent.kt
├── MatchExceptionCreatedEvent.kt
├── PaymentSentEvent.kt
├── PaymentClearedEvent.kt
├── PaymentReturnedEvent.kt
├── PaymentRunApprovedEvent.kt
├── PaymentRunExecutedEvent.kt
├── VendorBlockedEvent.kt
├── VendorUnblockedEvent.kt
├── EarlyPaymentDiscountTakenEvent.kt
└── APAgingSnapshotCreatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── VendorInvoiceNotFoundException.kt
├── InvoiceNotMatchedException.kt
├── MatchToleranceExceededException.kt
├── VendorBlockedException.kt
├── DuplicateInvoiceException.kt
├── PaymentExceedsBalanceException.kt
├── InsufficientFundsException.kt
├── PaymentRunAlreadyExecutedException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── InvoiceValidationService.kt          # Duplicate check, vendor validation
├── ThreeWayMatchService.kt              # PO-GRN-Invoice matching
├── VarianceCalculationService.kt        # Price/qty variance calculation
├── PaymentAllocationService.kt          # FIFO, oldest first allocation
├── EarlyPaymentDiscountService.kt       # Discount calculation
├── PaymentProposalService.kt            # Generate payment proposals
├── AgingCalculationService.kt           # Aging bucket calculation
└── WithholdingTaxService.kt             # 1099/withholding calculation
```

---

## Key Invariants

1. **3-Way Match Required**: Invoice cannot be posted if match status ≠ FullMatch (when configured)
2. **Variance Tolerance**: Match exceptions created if price/qty variance exceeds tolerance
3. **Payment Balance**: Payment allocation ≤ invoice open balance
4. **Vendor Block**: No payments to blocked vendors
5. **Duplicate Prevention**: Same vendor + invoice number = duplicate
6. **Period Control**: No postings to closed periods
7. **Approval Required**: Payment runs require approval before execution
