# AP Infrastructure Layer

> Part of [Finance - Accounts Payable](../finance-ap.md)

## Directory Structure

```
ap-infrastructure/
└── src/main/kotlin/com.erp.finance.ap.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       ├── messaging/
    │       └── notification/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

### Invoice Resource

```
adapter/input/rest/
├── VendorInvoiceResource.kt
│   ├── POST   /api/v1/ap/invoices           → createInvoice()
│   ├── GET    /api/v1/ap/invoices/{id}      → getInvoice()
│   ├── GET    /api/v1/ap/invoices           → listInvoices()
│   ├── POST   /api/v1/ap/invoices/{id}/post → postInvoice()
│   ├── POST   /api/v1/ap/invoices/{id}/cancel → cancelInvoice()
│   └── POST   /api/v1/ap/invoices/{id}/match → performMatch()
```

### Payment Resource

```
├── PaymentResource.kt
│   ├── POST   /api/v1/ap/payments           → recordPayment()
│   ├── GET    /api/v1/ap/payments/{id}      → getPayment()
│   ├── GET    /api/v1/ap/payments           → listPayments()
│   ├── POST   /api/v1/ap/payments/{id}/apply → applyPayment()
│   ├── POST   /api/v1/ap/payments/{id}/reverse → reversePayment()
│   └── POST   /api/v1/ap/payments/{id}/void → voidCheck()
```

### Payment Run Resource

```
├── PaymentRunResource.kt
│   ├── POST   /api/v1/ap/payment-runs                      → createPaymentRun()
│   ├── GET    /api/v1/ap/payment-runs/{id}                 → getPaymentRun()
│   ├── GET    /api/v1/ap/payment-runs                      → listPaymentRuns()
│   ├── POST   /api/v1/ap/payment-runs/{id}/proposals       → generateProposals()
│   ├── PUT    /api/v1/ap/payment-runs/{id}/proposals       → modifyProposal()
│   ├── POST   /api/v1/ap/payment-runs/{id}/approve         → approvePaymentRun()
│   ├── POST   /api/v1/ap/payment-runs/{id}/execute         → executePaymentRun()
│   └── POST   /api/v1/ap/payment-runs/{id}/cancel          → cancelPaymentRun()
```

### Matching Resource

```
├── MatchingResource.kt
│   ├── GET    /api/v1/ap/matching/exceptions               → getMatchExceptions()
│   ├── GET    /api/v1/ap/matching/exceptions/{id}          → getException()
│   └── POST   /api/v1/ap/matching/exceptions/{id}/resolve  → resolveException()
```

### Reporting Resource

```
├── APReportingResource.kt
│   ├── GET    /api/v1/ap/reports/aging                → getAgingReport()
│   ├── GET    /api/v1/ap/reports/vendor-statement     → getVendorStatement()
│   ├── GET    /api/v1/ap/reports/cash-requirements    → getCashRequirements()
│   ├── GET    /api/v1/ap/reports/dpo                  → getDPOMetrics()
│   └── GET    /api/v1/ap/reports/1099                 → get1099Report()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateVendorInvoiceRequest.kt
│   ├── vendorId: String
│   ├── invoiceNumber: String
│   ├── invoiceDate: LocalDate
│   ├── dueDate: LocalDate
│   ├── currency: String
│   ├── lines: List<InvoiceLineRequest>
│   └── poReferences: List<String>?
│
├── InvoiceLineRequest.kt
│   ├── description: String
│   ├── quantity: BigDecimal
│   ├── unitPrice: BigDecimal
│   ├── accountId: String
│   ├── costCenter: String?
│   └── taxCode: String?
│
├── RecordPaymentRequest.kt
│   ├── vendorId: String
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
├── CreatePaymentRunRequest.kt
│   ├── runDate: LocalDate
│   ├── paymentMethod: String?
│   ├── vendorIds: List<String>?
│   ├── maxDueDate: LocalDate?
│   └── minAmount: BigDecimal?
│
└── ResolveMatchExceptionRequest.kt
    ├── resolution: String          # ACCEPT, REJECT, ADJUST
    └── adjustedAmount: BigDecimal?
```

### Response DTOs

```
dto/response/
├── VendorInvoiceDto.kt
│   ├── id, invoiceNumber, vendorId, vendorName
│   ├── type, status, matchStatus
│   ├── invoiceDate, dueDate, postingDate
│   ├── totalAmount, taxAmount, withholdingAmount, netPayable
│   ├── openBalance, currency
│   ├── lines: List<InvoiceLineDto>
│   └── poReferences, grnReferences
│
├── InvoiceLineDto.kt
│   ├── id, lineNumber, description
│   ├── quantity, unitPrice, lineTotal
│   ├── accountId, accountName
│   └── costCenter, taxCode, taxAmount
│
├── PaymentDto.kt
│   ├── id, paymentNumber, vendorId, vendorName
│   ├── amount, currency, paymentMethod
│   ├── status, paymentDate, clearDate
│   ├── bankReference, checkNumber
│   └── allocations: List<PaymentAllocationDto>
│
├── PaymentRunDto.kt
│   ├── id, runDate, status
│   ├── totalAmount, invoiceCount, vendorCount
│   ├── proposals: List<PaymentProposalDto>
│   └── batches: List<PaymentBatchDto>
│
├── APAgingReportDto.kt
│   ├── asOfDate, totalBalance
│   ├── buckets: Map<String, BigDecimal>  # Current, 1-30, etc.
│   └── vendorDetails: List<VendorAgingDto>
│
├── MatchExceptionDto.kt
│   ├── id, invoiceId, poId, grnId
│   ├── exceptionType, varianceAmount
│   ├── status, createdDate
│   └── resolution, resolvedBy, resolvedDate
│
└── CashRequirementsDto.kt
    ├── fromDate, toDate
    ├── totalRequired: BigDecimal
    └── byDate: List<DailyCashRequirementDto>
```

---

## Event Consumers

```
adapter/input/event/
├── PurchaseOrderEventConsumer.kt
│   └── Consumes: PurchaseOrderApprovedEvent → Enable invoice matching
│
├── GoodsReceiptEventConsumer.kt
│   └── Consumes: GoodsReceivedEvent → Enable 3-way matching
│
├── BankStatementEventConsumer.kt
│   └── Consumes: BankStatementImportedEvent → Auto-clear payments
│
└── VendorMasterEventConsumer.kt
    └── Consumes: VendorCreatedEvent, VendorUpdatedEvent → Sync vendor data
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── VendorInvoiceJpaAdapter.kt      → implements VendorInvoiceRepository
├── PaymentJpaAdapter.kt            → implements PaymentRepository
├── PaymentRunJpaAdapter.kt         → implements PaymentRunRepository
├── VendorAccountJpaAdapter.kt      → implements VendorAccountRepository
└── MatchingJpaAdapter.kt           → implements MatchingRepository
```

### JPA Entities

```
├── entity/
│   ├── VendorInvoiceEntity.kt
│   │   ├── @Table(name = "ap_vendor_invoices")
│   │   ├── @OneToMany lines: List<InvoiceLineEntity>
│   │   └── @ManyToOne vendor: VendorAccountEntity
│   │
│   ├── InvoiceLineEntity.kt
│   │   └── @Table(name = "ap_invoice_lines")
│   │
│   ├── PaymentEntity.kt
│   │   ├── @Table(name = "ap_payments")
│   │   └── @OneToMany allocations: List<PaymentAllocationEntity>
│   │
│   ├── PaymentAllocationEntity.kt
│   │   └── @Table(name = "ap_payment_allocations")
│   │
│   ├── PaymentRunEntity.kt
│   │   ├── @Table(name = "ap_payment_runs")
│   │   └── @OneToMany proposals: List<PaymentProposalEntity>
│   │
│   ├── VendorAccountEntity.kt
│   │   └── @Table(name = "ap_vendor_accounts")
│   │
│   ├── MatchExceptionEntity.kt
│   │   └── @Table(name = "ap_match_exceptions")
│   │
│   └── APAgingSnapshotEntity.kt
│       └── @Table(name = "ap_aging_snapshots")
```

### JPA Repositories

```
├── repository/
│   ├── VendorInvoiceJpaRepository.kt    : JpaRepository<VendorInvoiceEntity, UUID>
│   ├── PaymentJpaRepository.kt          : JpaRepository<PaymentEntity, UUID>
│   ├── PaymentRunJpaRepository.kt       : JpaRepository<PaymentRunEntity, UUID>
│   ├── VendorAccountJpaRepository.kt    : JpaRepository<VendorAccountEntity, UUID>
│   └── MatchExceptionJpaRepository.kt   : JpaRepository<MatchExceptionEntity, UUID>
```

### Reporting Adapters (Read Model)

```
├── reporting/
│   ├── APAgingReadAdapter.kt           → implements APAgingReadRepository
│   ├── VendorStatementReadAdapter.kt   → implements VendorStatementReadRepository
│   ├── CashRequirementsReadAdapter.kt  → implements CashRequirementsReadRepository
│   └── document/
│       ├── VendorInvoiceDocument.kt     # MongoDB document
│       ├── APAgingDocument.kt
│       ├── VendorStatementDocument.kt
│       └── DPOMetricsDocument.kt
```

---

## Integration Adapters

```
adapter/output/integration/
├── GLPostingAdapter.kt              → implements GeneralLedgerPort
│   └── Posts journal entries to GL service
│
├── ProcurementAdapter.kt            → implements ProcurementPort
│   └── Fetches PO/GRN data for matching
│
├── TaxServiceAdapter.kt             → implements TaxServicePort
│   └── Calculates withholding tax
│
├── TreasuryAdapter.kt               → implements TreasuryPort
│   └── Notifies Treasury of cash outflows
│
└── BankIntegrationAdapter.kt        → implements BankIntegrationPort
    ├── generateACHFile()            # NACHA format
    ├── generateWireFile()           # SWIFT MT103
    ├── generateCheckFile()          # Positive pay
    └── importBankStatement()        # BAI2 format
```

---

## Messaging Adapters

```
adapter/output/messaging/
├── kafka/
│   ├── APEventPublisher.kt
│   │   └── Publishes: VendorInvoicePostedEvent, PaymentSentEvent, etc.
│   │
│   ├── APEventConsumer.kt
│   │   └── Consumes: PO/GRN events, bank statements
│   │
│   └── schema/
│       ├── VendorInvoicePostedSchema.avro
│       ├── PaymentSentSchema.avro
│       ├── PaymentClearedSchema.avro
│       └── MatchExceptionSchema.avro
│
└── outbox/
    └── APOutboxEventPublisher.kt
        └── Transactional outbox pattern
```

---

## Notification Adapters

```
adapter/output/notification/
├── RemittanceAdviceAdapter.kt
│   ├── sendEmail(payment, vendor)
│   └── generatePDF(payment): ByteArray
│
└── PaymentNotificationAdapter.kt
    └── notifyApprover(paymentRun)
```

---

## Configuration

```
configuration/
├── APDependencyInjection.kt
│   └── Bean definitions for all adapters
│
├── APCQRSConfiguration.kt
│   └── Command/Query bus configuration
│
├── APPersistenceConfiguration.kt
│   └── DataSource, JPA, MongoDB config
│
├── APMessagingConfiguration.kt
│   └── Kafka producers/consumers
│
└── APBankConfiguration.kt
    └── Bank file format configurations
```

---

## Database Migrations

```
resources/db/migration/
├── V1__create_ap_schema.sql
│   └── CREATE SCHEMA ap;
│
├── V2__create_vendor_tables.sql
│   ├── ap_vendor_accounts
│   └── ap_vendor_bank_details
│
├── V3__create_invoice_tables.sql
│   ├── ap_vendor_invoices
│   ├── ap_invoice_lines
│   └── ap_invoice_references
│
├── V4__create_payment_tables.sql
│   ├── ap_payments
│   ├── ap_payment_allocations
│   └── ap_check_details
│
├── V5__create_payment_run_tables.sql
│   ├── ap_payment_runs
│   ├── ap_payment_proposals
│   └── ap_payment_batches
│
├── V6__create_matching_tables.sql
│   ├── ap_matching_documents
│   ├── ap_match_lines
│   └── ap_match_exceptions
│
├── V7__create_aging_tables.sql
│   └── ap_aging_snapshots
│
└── V8__create_ap_audit_tables.sql
    └── ap_audit_log
```

---

## Application Configuration

```yaml
# resources/application.yml
quarkus:
  datasource:
    ap:
      db-kind: postgresql
      jdbc:
        url: jdbc:postgresql://${DB_HOST}:5432/chiroerp_ap

  kafka:
    ap-events:
      bootstrap-servers: ${KAFKA_BOOTSTRAP}

ap:
  matching:
    price-tolerance-percent: 2.0
    quantity-tolerance-percent: 1.0
    auto-match-enabled: true

  payment-run:
    require-approval: true
    min-approval-amount: 10000

  bank:
    ach-format: NACHA
    wire-format: SWIFT_MT103
    positive-pay-enabled: true
```
