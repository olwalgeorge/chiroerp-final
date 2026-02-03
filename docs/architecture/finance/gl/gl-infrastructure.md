# GL Infrastructure Layer

> Part of [Finance - General Ledger](../finance-gl.md)

## Directory Structure

```
gl-infrastructure/
└── src/main/kotlin/com.erp.finance.gl.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       ├── messaging/
    │       └── reporting/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

### Ledger Resource

```
adapter/input/rest/
├── LedgerResource.kt
│   ├── POST   /api/v1/gl/ledgers                 -> createLedger()
│   ├── GET    /api/v1/gl/ledgers/{id}            -> getLedger()
│   └── POST   /api/v1/gl/ledgers/{id}/periods    -> openPeriod()
```

### Chart of Accounts Resource

```
├── ChartOfAccountsResource.kt
│   ├── POST   /api/v1/gl/chart-of-accounts       -> createChart()
│   ├── GET    /api/v1/gl/chart-of-accounts/{id}  -> getChart()
│   ├── POST   /api/v1/gl/chart-of-accounts/{id}/accounts -> addAccount()
│   └── PUT    /api/v1/gl/accounts/{id}/deactivate -> deactivateAccount()
```

### Journal Entry Resource

```
├── JournalEntryResource.kt
│   ├── POST   /api/v1/gl/journal-entries         -> createJournalEntry()
│   ├── POST   /api/v1/gl/journal-entries/{id}/post -> postJournalEntry()
│   ├── POST   /api/v1/gl/journal-entries/{id}/reverse -> reverseJournalEntry()
│   └── GET    /api/v1/gl/journal-entries/{id}    -> getJournalEntry()
```

### Period & Reporting Resource

```
├── PeriodResource.kt
│   ├── POST   /api/v1/gl/periods/{id}/close      -> closePeriod()
│   └── GET    /api/v1/gl/periods/{id}            -> getPeriod()
│
├── ReportingResource.kt
│   ├── GET    /api/v1/gl/trial-balance           -> getTrialBalance()
│   └── GET    /api/v1/gl/reports/financials      -> getFinancialStatements()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateLedgerRequest.kt
│   ├── ledgerCode: String
│   ├── name: String
│   ├── baseCurrency: String
│   └── fiscalCalendar: String
│
├── CreateChartOfAccountsRequest.kt
│   └── name: String
│
├── AddAccountRequest.kt
│   ├── accountNumber: String
│   ├── name: String
│   ├── type: String
│   ├── parentAccountId: String?
│   └── postingRule: String
│
├── CreateJournalEntryRequest.kt
│   ├── ledgerId: String
│   ├── postingDate: LocalDate
│   ├── description: String
│   └── lines: List<JournalLineRequest>
│
├── JournalLineRequest.kt
│   ├── accountId: String
│   ├── debit: BigDecimal
│   ├── credit: BigDecimal
│   ├── costCenter: String?
│   └── description: String?
│
└── ClosePeriodRequest.kt
    └── closeType: String           # SOFT, HARD
```

### Response DTOs

```
dto/response/
├── LedgerDto.kt
│   ├── id, ledgerCode, name, baseCurrency
│   └── status, fiscalCalendar
│
├── ChartOfAccountsDto.kt
│   ├── id, name
│   └── accounts: List<AccountDto>
│
├── JournalEntryDto.kt
│   ├── id, status, postingDate, description
│   ├── totalDebits, totalCredits
│   └── lines: List<JournalLineDto>
│
├── TrialBalanceDto.kt
│   ├── asOfDate, totalDebits, totalCredits
│   └── lines: List<TrialBalanceLineDto>
│
└── FinancialStatementDto.kt
    ├── statementType
    ├── periodId
    └── sections: List<StatementSectionDto>
```

---

## Event Consumers

```
adapter/input/event/
├── APEventConsumer.kt
│   └── Consumes: VendorInvoicePostedEvent, PaymentSentEvent -> Create journal entries
│
├── AREventConsumer.kt
│   └── Consumes: InvoicePostedEvent, PaymentReceivedEvent -> Create journal entries
│
├── AssetsEventConsumer.kt
│   └── Consumes: AssetCapitalizedEvent, DepreciationPostedEvent -> Create journal entries
│
├── TaxEventConsumer.kt
│   └── Consumes: TaxCalculatedEvent -> Create tax journal entries
│
└── TreasuryEventConsumer.kt
    └── Consumes: BankStatementImportedEvent -> Reconcile cash accounts
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── LedgerJpaAdapter.kt
│   ├── ChartOfAccountsJpaAdapter.kt
│   ├── AccountJpaAdapter.kt
│   ├── JournalEntryJpaAdapter.kt
│   ├── AccountingPeriodJpaAdapter.kt
│   ├── entity/
│   │   ├── LedgerEntity.kt
│   │   ├── ChartOfAccountsEntity.kt
│   │   ├── AccountEntity.kt
│   │   ├── JournalEntryEntity.kt
│   │   ├── JournalLineEntity.kt
│   │   └── AccountingPeriodEntity.kt
│   └── repository/
│       ├── LedgerJpaRepository.kt
│       ├── ChartOfAccountsJpaRepository.kt
│       ├── AccountJpaRepository.kt
│       ├── JournalEntryJpaRepository.kt
│       └── AccountingPeriodJpaRepository.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/
├── integration/
│   ├── ExchangeRateAdapter.kt         # FX conversions
│   └── AuditTrailAdapter.kt           # Immutable audit logs
│
├── messaging/
│   ├── kafka/
│   │   ├── GLEventPublisher.kt
│   │   ├── GLEventConsumer.kt
│   │   └── schema/
│   │       ├── JournalEntryPostedSchema.avro
│   │       └── FinancialPeriodClosedSchema.avro
│   └── outbox/
│       └── GLOutboxEventPublisher.kt
│
└── reporting/
    ├── TrialBalanceReadAdapter.kt
    └── FinancialStatementReadAdapter.kt
```

---

## Configuration & Resources

```
configuration/
├── GLDependencyInjection.kt
├── PersistenceConfiguration.kt
├── MessagingConfiguration.kt
└── ReportingConfiguration.kt

resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_gl_schema.sql
    ├── V2__create_chart_of_accounts.sql
    ├── V3__create_journal_entry_tables.sql
    └── V4__create_accounting_period_tables.sql
```
