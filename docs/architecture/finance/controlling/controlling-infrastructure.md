# Controlling Infrastructure Layer

> Part of [Finance - Controlling](../finance-controlling.md)

## Directory Structure

```
controlling-infrastructure/
└── src/main/kotlin/com.erp.finance.controlling.infrastructure/
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

### Cost Center Resource

```
adapter/input/rest/
├── CostCenterResource.kt
│   ├── POST   /api/v1/controlling/cost-centers       -> createCostCenter()
│   ├── GET    /api/v1/controlling/cost-centers/{id}  -> getCostCenter()
│   └── POST   /api/v1/controlling/cost-centers/{id}/block -> blockCostCenter()
```

### Internal Orders & Allocations

```
├── InternalOrderResource.kt
│   ├── POST   /api/v1/controlling/internal-orders    -> createOrder()
│   └── GET    /api/v1/controlling/internal-orders/{id} -> getOrder()
│
├── AllocationResource.kt
│   ├── POST   /api/v1/controlling/allocations/run    -> runAllocation()
│   └── POST   /api/v1/controlling/allocations/{id}/reverse -> reverseAllocation()
```

### Reporting Resource

```
├── ControllingReportResource.kt
│   ├── GET    /api/v1/controlling/reports/cost-center -> getCostCenterReport()
│   └── GET    /api/v1/controlling/reports/profitability -> getProfitabilityReport()
```

---

## Event Consumers

```
adapter/input/event/
├── GLPostingEventConsumer.kt
│   └── Consumes: JournalEntryPostedEvent -> Update actuals
│
├── AssetsEventConsumer.kt
│   └── Consumes: AssetTransferredEvent -> Update cost center mapping
│
└── PeriodCloseEventConsumer.kt
    └── Consumes: FinancialPeriodClosedEvent -> Lock allocations
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── CostCenterJpaAdapter.kt
│   ├── ProfitCenterJpaAdapter.kt
│   ├── InternalOrderJpaAdapter.kt
│   ├── AllocationCycleJpaAdapter.kt
│   ├── AllocationRunJpaAdapter.kt
│   ├── entity/
│   │   ├── CostCenterEntity.kt
│   │   ├── ProfitCenterEntity.kt
│   │   ├── InternalOrderEntity.kt
│   │   ├── AllocationCycleEntity.kt
│   │   └── AllocationRunEntity.kt
│   └── repository/
│       ├── CostCenterJpaRepository.kt
│       ├── ProfitCenterJpaRepository.kt
│       ├── InternalOrderJpaRepository.kt
│       ├── AllocationCycleJpaRepository.kt
│       └── AllocationRunJpaRepository.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/
├── integration/
│   ├── GLPostingAdapter.kt           # Post allocation entries to GL
│   └── HRAdapter.kt                  # Org unit mappings
│
├── messaging/
│   ├── kafka/
│   │   ├── ControllingEventPublisher.kt
│   │   ├── ControllingEventConsumer.kt
│   │   └── schema/
│   │       ├── AllocationRunPostedSchema.avro
│   │       └── CostCenterCreatedSchema.avro
│   └── outbox/
│       └── ControllingOutboxEventPublisher.kt
│
└── reporting/
    ├── CostCenterReportReadAdapter.kt
    └── ProfitabilityReportReadAdapter.kt
```

---

## Configuration & Resources

```
configuration/
├── ControllingDependencyInjection.kt
├── PersistenceConfiguration.kt
├── MessagingConfiguration.kt
└── ReportingConfiguration.kt

resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_controlling_schema.sql
    ├── V2__create_cost_center_tables.sql
    └── V3__create_allocation_tables.sql
```
