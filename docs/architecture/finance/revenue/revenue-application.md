# Revenue Application Layer

> Part of [Finance - Revenue Recognition](../finance-revenue.md)

## Directory Structure

```
revenue-application/
└── src/main/kotlin/com.erp.finance.revenue.application/
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
├── CreateContractCommand.kt
│   └── customerId, term, transactionPrice
│
├── DefinePerformanceObligationsCommand.kt
│   └── contractId, obligations[]
│
├── AllocateTransactionPriceCommand.kt
│   └── contractId
│
├── RecognizeRevenueCommand.kt
│   └── contractId, period
│
└── ModifyContractCommand.kt
    └── contractId, modification
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetContractByIdQuery.kt
│   └── contractId -> ContractDto
│
├── GetRevenueScheduleQuery.kt
│   └── contractId -> RevenueScheduleDto
│
└── GetDeferredRevenueQuery.kt
    └── asOfDate -> DeferredRevenueSummaryDto
```

---

## Output Ports (Driven Ports)

```
port/output/
├── ContractRepository.kt
├── RevenueScheduleRepository.kt
├── DeferredRevenueRepository.kt
├── GeneralLedgerPort.kt
├── BillingPort.kt
└── EventPublisherPort.kt
```

---

## Command Handlers

```
service/command/
├── ContractCommandHandler.kt
│   ├── handle(CreateContractCommand): ContractId
│   └── handle(ModifyContractCommand): void
│
├── ObligationCommandHandler.kt
│   └── handle(DefinePerformanceObligationsCommand): void
│
├── AllocationCommandHandler.kt
│   └── handle(AllocateTransactionPriceCommand): void
│
└── RecognitionCommandHandler.kt
    └── handle(RecognizeRevenueCommand): void
```

---

## Query Handlers

```
service/query/
├── ContractQueryHandler.kt
│   └── handle(GetContractByIdQuery): ContractDto
│
└── RevenueQueryHandler.kt
    ├── handle(GetRevenueScheduleQuery): RevenueScheduleDto
    └── handle(GetDeferredRevenueQuery): DeferredRevenueSummaryDto
```
