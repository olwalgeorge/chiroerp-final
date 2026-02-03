# GL Application Layer

> Part of [Finance - General Ledger](../finance-gl.md)

## Directory Structure

```
gl-application/
└── src/main/kotlin/com.erp.finance.gl.application/
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

### Ledger & COA Commands

```
port/input/command/
├── CreateLedgerCommand.kt
│   └── ledgerCode, name, baseCurrency, fiscalCalendar
│
├── CreateChartOfAccountsCommand.kt
│   └── ledgerId, name
│
├── AddAccountCommand.kt
│   └── coaId, accountNumber, name, type, parentAccountId?
│
└── DeactivateAccountCommand.kt
    └── accountId, reason
```

### Journal Entry Commands

```
├── CreateJournalEntryCommand.kt
│   └── ledgerId, postingDate, lines[], sourceDocument?
│
├── PostJournalEntryCommand.kt
│   └── journalEntryId
│
└── ReverseJournalEntryCommand.kt
    └── journalEntryId, reason
```

### Period & Batch Commands

```
├── OpenAccountingPeriodCommand.kt
│   └── ledgerId, periodType, startDate, endDate
│
├── CloseAccountingPeriodCommand.kt
│   └── periodId, closeType (Soft/Hard)
│
├── CreatePostingBatchCommand.kt
│   └── ledgerId, source, entryIds[]
│
└── PostPostingBatchCommand.kt
    └── batchId
```

---

## Queries (Read Operations)

### Ledger & COA Queries

```
port/input/query/
├── GetLedgerByIdQuery.kt
│   └── ledgerId -> LedgerDto
│
├── GetChartOfAccountsQuery.kt
│   └── ledgerId -> ChartOfAccountsDto
│
└── GetAccountByIdQuery.kt
    └── accountId -> AccountDto
```

### Journal & Period Queries

```
├── GetJournalEntryByIdQuery.kt
│   └── journalEntryId -> JournalEntryDto
│
├── GetJournalEntriesQuery.kt
│   └── ledgerId, dateRange?, status? -> List<JournalEntrySummaryDto>
│
├── GetAccountingPeriodStatusQuery.kt
│   └── periodId -> AccountingPeriodDto
│
└── GetTrialBalanceQuery.kt
    └── ledgerId, asOfDate -> TrialBalanceDto
```

### Reporting Queries

```
├── GetAccountBalanceQuery.kt
│   └── accountId, asOfDate -> AccountBalanceDto
│
└── GetFinancialStatementQuery.kt
    └── ledgerId, periodId, statementType -> FinancialStatementDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── LedgerRepository.kt
│   ├── save(ledger: Ledger)
│   └── findById(id: LedgerId): Ledger?
│
├── ChartOfAccountsRepository.kt
│   ├── save(coa: ChartOfAccounts)
│   └── findByLedgerId(ledgerId: LedgerId): ChartOfAccounts?
│
├── AccountRepository.kt
│   ├── save(account: Account)
│   └── findById(id: AccountId): Account?
│
├── JournalEntryRepository.kt
│   ├── save(entry: JournalEntry)
│   └── findById(id: JournalEntryId): JournalEntry?
│
├── AccountingPeriodRepository.kt
│   └── findById(id: AccountingPeriodId): AccountingPeriod?
│
└── PostingBatchRepository.kt
    └── save(batch: PostingBatch)
```

### Read Model Ports

```
├── TrialBalanceReadRepository.kt
├── AccountBalanceReadRepository.kt
└── FinancialStatementReadRepository.kt
```

### Integration Ports

```
├── EventPublisherPort.kt
│   └── publish(event: DomainEvent)
│
├── AuditTrailPort.kt
│   └── log(event: AuditEvent)
│
└── ExchangeRatePort.kt
    └── getRate(currency: CurrencyCode, date: LocalDate): ExchangeRate
```

---

## Command Handlers

```
service/command/
├── LedgerCommandHandler.kt
│   ├── handle(CreateLedgerCommand): LedgerId
│   └── handle(CreateChartOfAccountsCommand): ChartOfAccountsId
│
├── AccountCommandHandler.kt
│   ├── handle(AddAccountCommand): AccountId
│   └── handle(DeactivateAccountCommand): void
│
├── JournalEntryCommandHandler.kt
│   ├── handle(CreateJournalEntryCommand): JournalEntryId
│   ├── handle(PostJournalEntryCommand): void
│   └── handle(ReverseJournalEntryCommand): void
│
├── PeriodCommandHandler.kt
│   ├── handle(OpenAccountingPeriodCommand): AccountingPeriodId
│   └── handle(CloseAccountingPeriodCommand): void
│
└── PostingBatchCommandHandler.kt
    ├── handle(CreatePostingBatchCommand): PostingBatchId
    └── handle(PostPostingBatchCommand): void
```

---

## Query Handlers

```
service/query/
├── LedgerQueryHandler.kt
│   ├── handle(GetLedgerByIdQuery): LedgerDto
│   └── handle(GetChartOfAccountsQuery): ChartOfAccountsDto
│
├── JournalEntryQueryHandler.kt
│   ├── handle(GetJournalEntryByIdQuery): JournalEntryDto
│   └── handle(GetJournalEntriesQuery): List<JournalEntrySummaryDto>
│
├── PeriodQueryHandler.kt
│   └── handle(GetAccountingPeriodStatusQuery): AccountingPeriodDto
│
└── ReportingQueryHandler.kt
    ├── handle(GetTrialBalanceQuery): TrialBalanceDto
    └── handle(GetFinancialStatementQuery): FinancialStatementDto
```
