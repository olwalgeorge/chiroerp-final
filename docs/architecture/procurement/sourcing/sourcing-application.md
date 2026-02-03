# Procurement Sourcing Application Layer

> Part of [Procurement Sourcing & RFQ](../procurement-sourcing.md)

## Directory Structure

```
sourcing-application/
└── src/main/kotlin/com.erp.procurement.sourcing.application/
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
├── IssueRFQCommand.kt
│   └── rfqId, suppliers[], dueDate
│
├── SubmitQuoteCommand.kt
│   └── rfqId, supplierId, lines[]
│
└── GrantAwardCommand.kt
    └── rfqId, supplierId, reason
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetRFQByIdQuery.kt
│   └── rfqId -> RFQDto
│
└── GetQuoteComparisonQuery.kt
    └── rfqId -> QuoteComparisonDto
```

---

## Output Ports (Driven Ports)

```
port/output/
├── RFQRepository.kt
├── QuoteRepository.kt
└── AwardRepository.kt
```

### Integration Ports

```
├── SupplierPort.kt
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── RFQCommandHandler.kt
│   ├── QuoteCommandHandler.kt
│   └── AwardCommandHandler.kt
└── query/
    ├── RFQQueryHandler.kt
    └── QuoteComparisonQueryHandler.kt
```
