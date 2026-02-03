# AR Infrastructure Layer

> Part of [Finance - Accounts Receivable](../finance-ar.md)

## Directory Structure

```
ar-infrastructure/
└── src/main/kotlin/com.erp.finance.ar.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       ├── messaging/
    │       ├── notification/
    │       └── audit/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

### Invoice Resource

```
adapter/input/rest/
├── InvoiceResource.kt
│   ├── POST   /api/v1/ar/invoices           -> createInvoice()
│   ├── GET    /api/v1/ar/invoices/{id}      -> getInvoice()
│   ├── GET    /api/v1/ar/invoices           -> listInvoices()
│   ├── POST   /api/v1/ar/invoices/{id}/post -> postInvoice()
│   └── POST   /api/v1/ar/invoices/{id}/cancel -> cancelInvoice()
```

### Payment Resource

```
├── PaymentResource.kt
│   ├── POST   /api/v1/ar/payments           -> recordPayment()
│   ├── GET    /api/v1/ar/payments/{id}      -> getPayment()
│   ├── POST   /api/v1/ar/payments/{id}/apply -> applyPayment()
│   └── POST   /api/v1/ar/payments/{id}/reverse -> reversePayment()
```

### Credit & Dunning Resources

```
├── CreditMemoResource.kt
│   └── POST   /api/v1/ar/credit-memos       -> issueCreditMemo()
│
├── DunningResource.kt
│   └── POST   /api/v1/ar/dunning-runs       -> runDunning()
```

### Reporting Resources

```
├── AgingResource.kt
│   └── GET    /api/v1/ar/reports/aging            -> getAgingReport()
│
├── CustomerStatementResource.kt
│   └── GET    /api/v1/ar/reports/customer-statement -> getCustomerStatement()
│
└── ARReportingResource.kt
    ├── GET    /api/v1/ar/reports/dso              -> getDSOMetrics()
    └── GET    /api/v1/ar/reports/cash-forecast    -> getCashForecast()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateInvoiceRequest.kt
│   ├── customerId: String
│   ├── invoiceNumber: String
│   ├── invoiceDate: LocalDate
│   ├── dueDate: LocalDate
│   ├── currency: String
│   ├── lines: List<InvoiceLineRequest>
│   └── taxDetails: List<TaxDetailRequest>?
│
├── InvoiceLineRequest.kt
│   ├── description: String
│   ├── quantity: BigDecimal
│   ├── unitPrice: BigDecimal
│   ├── accountId: String
│   ├── taxCode: String?
│
├── RecordPaymentRequest.kt
│   ├── customerId: String
│   ├── amount: BigDecimal
│   ├── currency: String
│   ├── paymentMethod: String
│   ├── bankReference: String?
│   └── allocations: List<PaymentAllocationRequest>?
│
├── PaymentAllocationRequest.kt
│   ├── invoiceId: String
│   └── amount: BigDecimal
│
├── IssueCreditMemoRequest.kt
│   ├── invoiceId: String
│   ├── amount: BigDecimal
│   └── reason: String
│
└── RunDunningRequest.kt
    ├── asOfDate: LocalDate
    ├── dunningLevel: String?
    └── customerIds: List<String>?
```

### Response DTOs

```
dto/response/
├── InvoiceDto.kt
│   ├── id, invoiceNumber, customerId, customerName
│   ├── status, invoiceDate, dueDate, postingDate
│   ├── totalAmount, taxAmount, openBalance, currency
│   └── lines: List<InvoiceLineDto>
│
├── InvoiceLineDto.kt
│   ├── id, lineNumber, description
│   ├── quantity, unitPrice, lineTotal
│   ├── accountId, taxCode, taxAmount
│
├── PaymentDto.kt
│   ├── id, paymentNumber, customerId
│   ├── amount, currency, paymentMethod
│   ├── status, paymentDate, bankReference
│   └── allocations: List<PaymentAllocationDto>
│
├── AgingReportDto.kt
│   ├── asOfDate, totalBalance
│   ├── buckets: Map<String, BigDecimal>
│   └── customerDetails: List<CustomerAgingDto>
│
├── CustomerBalanceDto.kt
│   ├── customerId, currentBalance, pastDueBalance
│
└── CustomerStatementDto.kt
    ├── customerId, fromDate, toDate
    ├── openingBalance, closingBalance
    └── lines: List<StatementLineDto>
```

---

## Event Consumers

```
adapter/input/event/
├── SalesOrderEventConsumer.kt
│   └── Consumes: SalesOrderFulfilledEvent -> Create invoice
│
├── ContractEventConsumer.kt
│   └── Consumes: ContractRenewalEvent -> Subscription billing
│
└── BankStatementEventConsumer.kt
    └── Consumes: BankStatementImportedEvent -> Auto-apply payments
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── InvoiceJpaAdapter.kt          -> implements InvoiceRepository
├── PaymentJpaAdapter.kt          -> implements PaymentRepository
├── CustomerAccountJpaAdapter.kt  -> implements CustomerAccountRepository
├── DunningJpaAdapter.kt          -> implements DunningRepository
└── AgingSnapshotJpaAdapter.kt    -> implements AgingSnapshotRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── InvoiceEntity.kt
├── InvoiceLineEntity.kt
├── PaymentEntity.kt
├── PaymentAllocationEntity.kt
├── CustomerAccountEntity.kt
├── CreditMemoEntity.kt
├── DunningRunEntity.kt
└── AgingSnapshotEntity.kt
```

### Read Model Adapters

```
adapter/output/persistence/reporting/
├── AgingReadAdapter.kt
├── CustomerStatementReadAdapter.kt
├── CashForecastReadAdapter.kt
└── document/
    ├── InvoiceDocument.kt
    ├── AgingReportDocument.kt
    ├── CustomerStatementDocument.kt
    └── DSOMetricsDocument.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── GLPostingAdapter.kt
├── TaxServiceAdapter.kt
├── SalesOrderAdapter.kt
└── BankIntegrationAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── AREventPublisher.kt
│   ├── AREventConsumer.kt
│   └── schema/
│       ├── InvoicePostedSchema.avro
│       ├── PaymentReceivedSchema.avro
│       └── DunningGeneratedSchema.avro
└── outbox/
    └── AROutboxEventPublisher.kt
```

```
adapter/output/notification/
├── DunningEmailAdapter.kt
└── InvoiceEmailAdapter.kt
```

```
adapter/output/audit/
└── ARImmutableAuditLogAdapter.kt
```

---

## Configuration & Resources

```
configuration/
├── ARDependencyInjection.kt
├── CQRSConfiguration.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_ar_schema.sql
    ├── V2__create_invoice_tables.sql
    ├── V3__create_payment_tables.sql
    ├── V4__create_customer_account_tables.sql
    ├── V5__create_dunning_tables.sql
    ├── V6__create_aging_tables.sql
    └── V7__create_ar_audit_tables.sql
```
