# Close Application Layer

> Part of [Finance - Period Close](../finance-close.md)

## Directory Structure

```
close-application/
└── src/main/kotlin/com.erp.finance.close.application/
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
├── StartCloseRunCommand.kt
│   └── ledgerId, periodId
│
├── CompleteCloseTaskCommand.kt
│   └── runId, taskId
│
└── FinalizeCloseRunCommand.kt
    └── runId
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetCloseRunByIdQuery.kt
│   └── runId -> CloseRunDto
│
└── GetCloseChecklistQuery.kt
    └── periodId -> CloseChecklistDto
```

---

## Output Ports (Driven Ports)

```
port/output/
├── CloseRunRepository.kt
├── ReconciliationRepository.kt
├── GeneralLedgerPort.kt           # Trigger period close in GL
└── EventPublisherPort.kt
```

---

## Command Handlers

```
service/command/
├── CloseRunCommandHandler.kt
│   ├── handle(StartCloseRunCommand): CloseRunId
│   ├── handle(CompleteCloseTaskCommand): void
│   └── handle(FinalizeCloseRunCommand): void
```
