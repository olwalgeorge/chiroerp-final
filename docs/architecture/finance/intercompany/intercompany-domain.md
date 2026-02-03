# Intercompany Domain Layer

> Part of [Finance - Intercompany](../finance-intercompany.md)

## Directory Structure

```
intercompany-domain/
└── src/main/kotlin/com.erp.finance.intercompany.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Intercompany Agreement (`model/agreement/`)

```
├── agreement/
│   ├── IntercompanyAgreement.kt     # Aggregate Root
│   ├── AgreementId.kt
│   ├── Counterparty.kt
│   ├── PricingRule.kt
│   └── ValidityPeriod.kt
```

### Intercompany Transaction (`model/transaction/`)

```
├── transaction/
│   ├── IntercompanyTransaction.kt   # Aggregate Root
│   ├── TransactionId.kt
│   ├── TransactionType.kt           # Sale, Service, Loan
│   ├── TransactionStatus.kt         # Draft, Posted, Settled
│   └── CounterpartyLeg.kt
```

### Netting Batch (`model/netting/`)

```
├── netting/
│   ├── NettingBatch.kt              # Aggregate Root
│   ├── NettingBatchId.kt
│   ├── NettingLine.kt               # Entity
│   └── NettingStatus.kt
```

### Elimination Entry (`model/elimination/`)

```
├── elimination/
│   ├── EliminationEntry.kt          # Aggregate Root
│   ├── EliminationEntryId.kt
│   └── EliminationStatus.kt
```

---

## Domain Events

```
events/
├── IntercompanyPostedEvent.kt
├── NettingCompletedEvent.kt
└── EliminationPostedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── AgreementNotFoundException.kt
├── CounterpartyMismatchException.kt
├── NettingBatchNotFoundException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── NettingService.kt
├── EliminationService.kt
└── PricingService.kt
```

---

## Key Invariants

1. **Balanced IC Entry**: Counterparty legs must balance.
2. **Valid Agreement**: Transactions require active agreement.
3. **Open Period**: Postings only in open periods.
