# AR Application Layer

> Part of [Finance - Accounts Receivable](../finance-ar.md)

## Directory Structure

```
ar-application/
└── src/main/kotlin/com.erp.finance.ar.application/
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
├── CreateInvoiceCommand.kt
│   └── customerId, invoiceNumber, invoiceDate, dueDate, currency, lines[]
│
├── PostInvoiceCommand.kt
│   └── invoiceId, postingDate
│
└── CancelInvoiceCommand.kt
    └── invoiceId, reason
```

### Payment Commands

```
├── RecordPaymentCommand.kt
│   └── customerId, amount, currency, paymentMethod, bankReference, allocations[]
│
├── ApplyPaymentCommand.kt
│   └── paymentId, allocations: List<InvoiceId, Amount>
│
└── ReversePaymentCommand.kt
    └── paymentId, reason
```

### Credit & Dunning Commands

```
├── IssueCreditMemoCommand.kt
│   └── invoiceId, amount, reason
│
├── IssueDebitMemoCommand.kt
│   └── invoiceId, amount, reason
│
├── ApproveWriteOffCommand.kt
│   └── invoiceId, amount, reason
│
└── RunDunningCommand.kt
    └── asOfDate, dunningLevel?, customerIds?
```

### Credit Management Commands

```
├── UpdateCreditLimitCommand.kt
│   └── customerId, newLimit
│
├── BlockCustomerCommand.kt
│   └── customerId, reason
│
└── UnblockCustomerCommand.kt
    └── customerId
```

### Lockbox & Aging Commands

```
├── ProcessLockboxCommand.kt
│   └── lockboxBatchId, items[]
│
└── GenerateAgingSnapshotCommand.kt
    └── asOfDate
```

---

## Queries (Read Operations)

### Invoice Queries

```
port/input/query/
├── GetInvoiceByIdQuery.kt
│   └── invoiceId -> InvoiceDto
│
├── GetInvoicesByCustomerQuery.kt
│   └── customerId, status?, dateRange? -> List<InvoiceSummaryDto>
│
└── GetOpenInvoicesQuery.kt
    └── customerId?, dueDate?, minAmount? -> List<OpenInvoiceDto>
```

### Payment Queries

```
├── GetPaymentHistoryQuery.kt
│   └── customerId, dateRange? -> List<PaymentSummaryDto>
│
└── GetPaymentByIdQuery.kt
    └── paymentId -> PaymentDto
```

### Customer & Credit Queries

```
├── GetCustomerBalanceQuery.kt
│   └── customerId -> CustomerBalanceDto
│
├── GetCreditStatusQuery.kt
│   └── customerId -> CreditStatusDto
│
└── GetDunningHistoryQuery.kt
    └── customerId -> List<DunningNoticeDto>
```

### Reporting Queries

```
├── GetAgingReportQuery.kt
│   └── asOfDate, customerId? -> AgingReportDto
│
├── GetCustomerStatementQuery.kt
│   └── customerId, dateRange -> CustomerStatementDto
│
├── GetCashForecastQuery.kt
│   └── fromDate, toDate -> CashForecastDto
│
└── GetDSOMetricsQuery.kt
    └── period -> DaysSalesOutstandingDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── InvoiceRepository.kt
├── PaymentRepository.kt
├── CustomerAccountRepository.kt
├── DunningRepository.kt
├── AgingSnapshotRepository.kt
├── InvoiceReadRepository.kt           # Read model
├── AgingReadRepository.kt             # Read model
└── CustomerStatementReadRepository.kt
```

### Integration Ports

```
├── GeneralLedgerPort.kt               # GL posting
├── TaxServicePort.kt                  # Tax calculation
├── SalesOrderPort.kt                  # Sales order integration
├── BankIntegrationPort.kt             # Lockbox and bank feeds
├── NotificationPort.kt                # Dunning notifications
├── AuditTrailPort.kt
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── InvoiceCommandHandler.kt
│   ├── PaymentCommandHandler.kt
│   ├── CreditMemoCommandHandler.kt
│   ├── DunningCommandHandler.kt
│   ├── CreditManagementCommandHandler.kt
│   └── LockboxCommandHandler.kt
└── query/
    ├── InvoiceQueryHandler.kt
    ├── PaymentQueryHandler.kt
    ├── AgingQueryHandler.kt
    ├── CreditQueryHandler.kt
    └── CustomerStatementQueryHandler.kt
```
