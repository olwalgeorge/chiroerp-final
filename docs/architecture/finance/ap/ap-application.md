# AP Application Layer

> Part of [Finance - Accounts Payable](../finance-ap.md)

## Directory Structure

```
ap-application/
└── src/main/kotlin/com.erp.finance.ap.application/
    ├── port/
    │   ├── input/
    │   │   ├── command/
    │   │   └── query/
    │   └── output/
    └── service/
        ├── command/
        └── query/
```

---

## Commands (Write Operations)

### Invoice Commands

```
port/input/command/
├── CreateVendorInvoiceCommand.kt
│   └── vendorId, invoiceNumber, invoiceDate, dueDate, lines[], poReferences[]
│
├── PostVendorInvoiceCommand.kt
│   └── invoiceId, postingDate, overrideMatchCheck: Boolean
│
├── CancelVendorInvoiceCommand.kt
│   └── invoiceId, reason
│
├── PerformThreeWayMatchCommand.kt
│   └── invoiceId, poId, grnId
│
├── ResolveMatchExceptionCommand.kt
│   └── exceptionId, resolution (Accept/Reject/Adjust), adjustedAmount?
```

### Payment Commands

```
├── RecordPaymentCommand.kt
│   └── vendorId, amount, paymentMethod, bankReference, invoiceAllocations[]
│
├── ApplyPaymentCommand.kt
│   └── paymentId, allocations: List<InvoiceId, Amount>
│
├── ReversePaymentCommand.kt
│   └── paymentId, reason
│
├── VoidCheckCommand.kt
│   └── paymentId, checkNumber, voidReason
```

### Payment Run Commands

```
├── CreatePaymentRunCommand.kt
│   └── runDate, selectionCriteria (dueDate, vendors[], minAmount)
│
├── GeneratePaymentProposalsCommand.kt
│   └── paymentRunId
│
├── ModifyPaymentProposalCommand.kt
│   └── paymentRunId, invoiceId, action (Add/Remove/Adjust)
│
├── ApprovePaymentRunCommand.kt
│   └── paymentRunId, approverId
│
├── ExecutePaymentRunCommand.kt
│   └── paymentRunId
│
├── CancelPaymentRunCommand.kt
│   └── paymentRunId, reason
```

### Vendor Commands

```
├── BlockVendorCommand.kt
│   └── vendorId, reason, blockDate
│
├── UnblockVendorCommand.kt
│   └── vendorId
│
├── UpdateVendorBankDetailsCommand.kt
│   └── vendorId, bankDetails
│
└── GenerateAPAgingSnapshotCommand.kt
    └── asOfDate
```

---

## Queries (Read Operations)

### Invoice Queries

```
port/input/query/
├── GetVendorInvoiceByIdQuery.kt
│   └── invoiceId → VendorInvoiceDto
│
├── GetVendorInvoicesByVendorQuery.kt
│   └── vendorId, status?, dateRange? → List<VendorInvoiceSummaryDto>
│
├── GetOpenInvoicesQuery.kt
│   └── vendorId?, dueDate?, minAmount? → List<OpenInvoiceDto>
│
├── GetInvoicesForMatchingQuery.kt
│   └── poId → List<UnmatchedInvoiceDto>
│
├── GetMatchExceptionsQuery.kt
│   └── status?, vendorId? → List<MatchExceptionDto>
```

### Payment Queries

```
├── GetPaymentByIdQuery.kt
│   └── paymentId → PaymentDto
│
├── GetPaymentHistoryQuery.kt
│   └── vendorId, dateRange? → List<PaymentSummaryDto>
│
├── GetPendingPaymentsQuery.kt
│   └── bankAccount?, paymentMethod? → List<PendingPaymentDto>
│
├── GetPaymentRunByIdQuery.kt
│   └── paymentRunId → PaymentRunDto
│
├── GetPaymentRunsQuery.kt
│   └── status?, dateRange? → List<PaymentRunSummaryDto>
```

### Reporting Queries

```
├── GetVendorBalanceQuery.kt
│   └── vendorId → VendorBalanceDto
│
├── GetAPAgingReportQuery.kt
│   └── asOfDate, vendorId? → APAgingReportDto
│
├── GetCashRequirementsQuery.kt
│   └── fromDate, toDate → CashRequirementsDto
│
├── GetVendorStatementQuery.kt
│   └── vendorId, dateRange → VendorStatementDto
│
├── GetDPOMetricsQuery.kt
│   └── period → DaysPayableOutstandingDto
│
└── Get1099ReportQuery.kt
    └── taxYear, vendorId? → Form1099ReportDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── VendorInvoiceRepository.kt
│   ├── save(invoice: VendorInvoice)
│   ├── findById(id: VendorInvoiceId): VendorInvoice?
│   ├── findByVendorAndNumber(vendorId, invoiceNumber): VendorInvoice?
│   └── findOpenInvoices(criteria: InvoiceSearchCriteria): List<VendorInvoice>
│
├── PaymentRepository.kt
│   ├── save(payment: Payment)
│   ├── findById(id: PaymentId): Payment?
│   └── findByVendor(vendorId: VendorAccountId): List<Payment>
│
├── PaymentRunRepository.kt
│   ├── save(paymentRun: PaymentRun)
│   ├── findById(id: PaymentRunId): PaymentRun?
│   └── findByStatus(status: PaymentRunStatus): List<PaymentRun>
│
├── VendorAccountRepository.kt
│   ├── findById(id: VendorAccountId): VendorAccount?
│   └── findByVendorCode(code: String): VendorAccount?
│
├── MatchingRepository.kt
│   └── findExceptions(criteria: ExceptionSearchCriteria): List<MatchException>
│
└── APAgingSnapshotRepository.kt
    └── save(snapshot: APAgingSnapshot)
```

### Read Model Ports

```
├── VendorInvoiceReadRepository.kt       # Denormalized read model
├── APAgingReadRepository.kt             # Aging report projections
├── VendorStatementReadRepository.kt     # Statement generation
└── CashRequirementsReadRepository.kt    # Cash forecast projections
```

### Integration Ports

```
├── GeneralLedgerPort.kt                 # GL journal posting
│   └── postJournal(entry: JournalEntry): JournalEntryId
│
├── ProcurementPort.kt                   # PO/GRN integration
│   ├── getPurchaseOrder(poId: PurchaseOrderId): PurchaseOrder
│   └── getGoodsReceipt(grnId: GoodsReceiptId): GoodsReceipt
│
├── TaxServicePort.kt                    # Tax/withholding calculation
│   └── calculateWithholding(invoice: VendorInvoice): WithholdingResult
│
├── BankIntegrationPort.kt               # Bank file generation, positive pay
│   ├── generatePaymentFile(batch: PaymentBatch): BankFile
│   └── generatePositivePayFile(checks: List<Check>): PositivePayFile
│
├── TreasuryPort.kt                      # Cash position updates
│   └── notifyCashOutflow(payment: Payment)
│
├── NotificationPort.kt                  # Remittance advice
│   └── sendRemittanceAdvice(payment: Payment, vendor: VendorAccount)
│
├── AuditTrailPort.kt
│   └── log(event: AuditEvent)
│
└── EventPublisherPort.kt
    └── publish(event: DomainEvent)
```

---

## Command Handlers

```
service/command/
├── VendorInvoiceCommandHandler.kt
│   ├── handle(CreateVendorInvoiceCommand): VendorInvoiceId
│   ├── handle(PostVendorInvoiceCommand): void
│   ├── handle(CancelVendorInvoiceCommand): void
│   └── handle(PerformThreeWayMatchCommand): MatchResult
│
├── PaymentCommandHandler.kt
│   ├── handle(RecordPaymentCommand): PaymentId
│   ├── handle(ApplyPaymentCommand): void
│   ├── handle(ReversePaymentCommand): void
│   └── handle(VoidCheckCommand): void
│
├── PaymentRunCommandHandler.kt
│   ├── handle(CreatePaymentRunCommand): PaymentRunId
│   ├── handle(GeneratePaymentProposalsCommand): List<PaymentProposal>
│   ├── handle(ApprovePaymentRunCommand): void
│   └── handle(ExecutePaymentRunCommand): void
│
├── MatchingCommandHandler.kt
│   └── handle(ResolveMatchExceptionCommand): void
│
└── VendorCommandHandler.kt
    ├── handle(BlockVendorCommand): void
    └── handle(UnblockVendorCommand): void
```

---

## Query Handlers

```
service/query/
├── VendorInvoiceQueryHandler.kt
│   ├── handle(GetVendorInvoiceByIdQuery): VendorInvoiceDto
│   ├── handle(GetOpenInvoicesQuery): List<OpenInvoiceDto>
│   └── handle(GetMatchExceptionsQuery): List<MatchExceptionDto>
│
├── PaymentQueryHandler.kt
│   ├── handle(GetPaymentByIdQuery): PaymentDto
│   ├── handle(GetPaymentHistoryQuery): List<PaymentSummaryDto>
│   └── handle(GetPendingPaymentsQuery): List<PendingPaymentDto>
│
├── PaymentRunQueryHandler.kt
│   ├── handle(GetPaymentRunByIdQuery): PaymentRunDto
│   └── handle(GetPaymentRunsQuery): List<PaymentRunSummaryDto>
│
├── APReportingQueryHandler.kt
│   ├── handle(GetAPAgingReportQuery): APAgingReportDto
│   ├── handle(GetCashRequirementsQuery): CashRequirementsDto
│   ├── handle(GetVendorStatementQuery): VendorStatementDto
│   └── handle(GetDPOMetricsQuery): DaysPayableOutstandingDto
│
└── VendorQueryHandler.kt
    └── handle(GetVendorBalanceQuery): VendorBalanceDto
```
