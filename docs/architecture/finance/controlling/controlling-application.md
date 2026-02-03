# Controlling Application Layer

> Part of [Finance - Controlling](../finance-controlling.md)

## Directory Structure

```
controlling-application/
└── src/main/kotlin/com.erp.finance.controlling.application/
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

### Cost/Profit Center Commands

```
port/input/command/
├── CreateCostCenterCommand.kt
│   └── code, name, managerId, hierarchyParent?
│
├── BlockCostCenterCommand.kt
│   └── costCenterId, reason
│
├── CreateProfitCenterCommand.kt
│   └── code, name, parent?
│
└── CreateInternalOrderCommand.kt
    └── orderType, description, budget?
```

### Allocation & Budget Commands

```
├── DefineAllocationCycleCommand.kt
│   └── name, basis, rules[]
│
├── RunAllocationCommand.kt
│   └── cycleId, period, postingDate
│
├── ReverseAllocationRunCommand.kt
│   └── runId, reason
│
├── SetActivityRateCommand.kt
│   └── activityTypeId, rate, validFrom
│
└── ApproveBudgetCommand.kt
    └── budgetId, approverId
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetCostCenterByIdQuery.kt
│   └── costCenterId -> CostCenterDto
│
├── GetProfitCenterByIdQuery.kt
│   └── profitCenterId -> ProfitCenterDto
│
├── GetInternalOrderByIdQuery.kt
│   └── orderId -> InternalOrderDto
│
├── GetAllocationRunByIdQuery.kt
│   └── runId -> AllocationRunDto
│
└── GetCostCenterReportQuery.kt
    └── costCenterId, period -> CostCenterReportDto
```

---

## Output Ports (Driven Ports)

```
port/output/
├── CostCenterRepository.kt
├── ProfitCenterRepository.kt
├── InternalOrderRepository.kt
├── AllocationCycleRepository.kt
├── AllocationRunRepository.kt
├── ActivityRateRepository.kt
├── BudgetRepository.kt
├── GeneralLedgerPort.kt           # Post allocation journal entries
├── AuditTrailPort.kt
└── EventPublisherPort.kt
```

---

## Command Handlers

```
service/command/
├── CostCenterCommandHandler.kt
│   ├── handle(CreateCostCenterCommand): CostCenterId
│   └── handle(BlockCostCenterCommand): void
│
├── ProfitCenterCommandHandler.kt
│   └── handle(CreateProfitCenterCommand): ProfitCenterId
│
├── InternalOrderCommandHandler.kt
│   └── handle(CreateInternalOrderCommand): InternalOrderId
│
├── AllocationCommandHandler.kt
│   ├── handle(DefineAllocationCycleCommand): AllocationCycleId
│   ├── handle(RunAllocationCommand): AllocationRunId
│   └── handle(ReverseAllocationRunCommand): void
│
└── BudgetCommandHandler.kt
    └── handle(ApproveBudgetCommand): void
```

---

## Query Handlers

```
service/query/
├── CostCenterQueryHandler.kt
│   └── handle(GetCostCenterByIdQuery): CostCenterDto
│
├── ProfitCenterQueryHandler.kt
│   └── handle(GetProfitCenterByIdQuery): ProfitCenterDto
│
├── InternalOrderQueryHandler.kt
│   └── handle(GetInternalOrderByIdQuery): InternalOrderDto
│
└── ReportingQueryHandler.kt
    └── handle(GetCostCenterReportQuery): CostCenterReportDto
```
