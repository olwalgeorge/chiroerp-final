# Close Domain Layer

> Part of [Finance - Period Close](../finance-close.md)

## Directory Structure

```
close-domain/
└── src/main/kotlin/com.erp.finance.close.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Close Run (`model/close/`)

```
├── close/
│   ├── CloseRun.kt                # Aggregate Root
│   ├── CloseRunId.kt
│   ├── CloseStatus.kt             # Draft, InProgress, Completed
│   ├── CloseChecklist.kt          # Entity
│   ├── CloseTask.kt               # Entity
│   └── TaskStatus.kt              # Pending, Done, Blocked
```

### Reconciliation (`model/reconciliation/`)

```
├── reconciliation/
│   ├── ReconciliationItem.kt      # Aggregate Root
│   ├── ReconciliationId.kt
│   ├── ReconciliationStatus.kt
│   └── Variance.kt
```

---

## Domain Events

```
events/
├── CloseRunStartedEvent.kt
├── CloseTaskCompletedEvent.kt
└── CloseRunCompletedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── CloseRunNotFoundException.kt
├── TaskDependencyException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── CloseChecklistService.kt
└── CloseValidationService.kt
```

---

## Key Invariants

1. **Dependency Order**: Tasks cannot complete before dependencies.
2. **Single Close**: One close run per period per ledger.
3. **Finalization Gate**: All tasks must be complete before close finalization.
