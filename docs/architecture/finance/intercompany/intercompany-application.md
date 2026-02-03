# Intercompany Application Layer

> Part of [Finance - Intercompany](../finance-intercompany.md)

## Directory Structure

```
intercompany-application/
└── src/main/kotlin/com.erp.finance.intercompany.application/
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

```
port/input/command/
├── CreateIntercompanyTransactionCommand.kt
│   └── agreementId, type, amount, currency
│
├── PostIntercompanyTransactionCommand.kt
│   └── transactionId
│
├── RunNettingCommand.kt
│   └── period, counterparties[]
│
└── PostEliminationCommand.kt
    └── period
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetIntercompanyTransactionByIdQuery.kt
│   └── transactionId -> IntercompanyTransactionDto
│
├── GetNettingBatchByIdQuery.kt
│   └── batchId -> NettingBatchDto
│
└── GetEliminationEntriesQuery.kt
    └── period -> List<EliminationEntryDto>
```

---

## Output Ports (Driven Ports)

```
port/output/
├── AgreementRepository.kt
├── TransactionRepository.kt
├── NettingBatchRepository.kt
├── EliminationRepository.kt
├── GeneralLedgerPort.kt
└── EventPublisherPort.kt
```

---

## Command Handlers

```
service/command/
├── TransactionCommandHandler.kt
│   ├── handle(CreateIntercompanyTransactionCommand): TransactionId
│   └── handle(PostIntercompanyTransactionCommand): void
│
├── NettingCommandHandler.kt
│   └── handle(RunNettingCommand): NettingBatchId
│
└── EliminationCommandHandler.kt
    └── handle(PostEliminationCommand): void
```
