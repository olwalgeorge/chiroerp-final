# GL Domain Layer

> Part of [Finance - General Ledger](../finance-gl.md)

## Directory Structure

```
gl-domain/
└── src/main/kotlin/com.erp.finance.gl.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Ledger (`model/ledger/`)

```
├── ledger/
│   ├── Ledger.kt                    # Aggregate Root
│   ├── LedgerId.kt
│   ├── LedgerCode.kt
│   ├── LedgerStatus.kt              # Draft, Active, Suspended
│   ├── BaseCurrency.kt
│   ├── FiscalCalendar.kt
│   └── LedgerSettings.kt
```

---

### Chart of Accounts (`model/coa/`)

```
├── coa/
│   ├── ChartOfAccounts.kt           # Aggregate Root
│   ├── ChartOfAccountsId.kt
│   ├── Account.kt                   # Entity
│   ├── AccountId.kt
│   ├── AccountNumber.kt
│   ├── AccountType.kt               # Asset, Liability, Equity, Revenue, Expense
│   ├── AccountStatus.kt             # Active, Inactive, Blocked
│   ├── AccountHierarchy.kt          # Parent/child structure
│   ├── AccountSegment.kt            # Segment value object
│   └── AccountPostingRule.kt
```

**ChartOfAccounts Aggregate**:
```kotlin
class ChartOfAccounts private constructor(
    val id: ChartOfAccountsId,
    val name: String,
    val accounts: MutableList<Account>
) {
    fun addAccount(account: Account): AccountCreatedEvent
    fun deactivateAccount(accountId: AccountId): AccountDeactivatedEvent
}
```

---

### Journal Entry (`model/journal/`)

```
├── journal/
│   ├── JournalEntry.kt              # Aggregate Root
│   ├── JournalEntryId.kt
│   ├── JournalLine.kt               # Entity
│   ├── JournalLineId.kt
│   ├── JournalEntryStatus.kt        # Draft, Posted, Reversed
│   ├── PostingDate.kt
│   ├── SourceDocument.kt            # AP/AR/Assets reference
│   ├── PostingBatchId.kt
│   └── ReversalReason.kt
```

**JournalEntry Aggregate**:
```kotlin
class JournalEntry private constructor(
    val id: JournalEntryId,
    val ledgerId: LedgerId,
    val postingDate: LocalDate,
    val lines: MutableList<JournalLine>,
    var status: JournalEntryStatus
) {
    fun post(): JournalEntryPostedEvent
    fun reverse(reason: ReversalReason): JournalEntryReversedEvent

    val totalDebits: Money
    val totalCredits: Money
}
```

---

### Accounting Period (`model/period/`)

```
├── period/
│   ├── AccountingPeriod.kt          # Aggregate Root
│   ├── AccountingPeriodId.kt
│   ├── PeriodStatus.kt              # Open, SoftClosed, Closed
│   ├── PeriodType.kt                # Month, Quarter, Year
│   ├── PeriodRange.kt
│   └── PeriodLock.kt
```

---

### Posting Batch (`model/batch/`)

```
├── batch/
│   ├── PostingBatch.kt              # Aggregate Root
│   ├── PostingBatchId.kt
│   ├── BatchStatus.kt               # Draft, Posted, Failed
│   ├── BatchSource.kt               # AP, AR, Assets, Manual
│   └── BatchTotals.kt
```

---

## Domain Events

```
events/
├── LedgerCreatedEvent.kt
├── AccountCreatedEvent.kt
├── AccountDeactivatedEvent.kt
├── JournalEntryPostedEvent.kt
├── JournalEntryReversedEvent.kt
├── PostingBatchPostedEvent.kt
├── FinancialPeriodClosedEvent.kt
└── TrialBalanceGeneratedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── LedgerNotFoundException.kt
├── AccountNotFoundException.kt
├── AccountInactiveException.kt
├── UnbalancedJournalEntryException.kt
├── PeriodClosedException.kt
├── InvalidPostingDateException.kt
├── DuplicateJournalEntryException.kt
└── PostingBatchFailedException.kt
```

---

## Domain Services

```
services/
├── JournalBalancingService.kt        # Debits = credits validation
├── AccountValidationService.kt       # Account status and type rules
├── PeriodControlService.kt           # Open/close enforcement
├── ExchangeRateService.kt            # Multi-currency support
└── TrialBalanceService.kt            # Snapshot generation
```

---

## Key Invariants

1. **Balanced Entry**: Total debits must equal total credits.
2. **Open Period**: Journal entries can only post to open periods.
3. **Active Accounts**: Posting only allowed to active accounts.
4. **Immutable Posting**: Posted entries cannot be edited; only reversed.
5. **Batch Integrity**: Posting batch totals must reconcile to entries.
6. **Period Close**: Close prevents new postings in the period.
