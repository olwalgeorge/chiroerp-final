# Revenue Infrastructure Layer

> Part of [Finance - Revenue Recognition](../finance-revenue.md)

## Directory Structure

```
revenue-infrastructure/
└── src/main/kotlin/com.erp.finance.revenue.infrastructure/
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

### Contract Resource

```
adapter/input/rest/
├── ContractResource.kt
│   ├── POST   /api/v1/revenue/contracts        -> createContract()
│   ├── GET    /api/v1/revenue/contracts/{id}   -> getContract()
│   └── POST   /api/v1/revenue/contracts/{id}/allocate -> allocatePrice()
```

### Recognition Resource

```
├── RecognitionResource.kt
│   ├── POST   /api/v1/revenue/recognize        -> recognizeRevenue()
│   └── GET    /api/v1/revenue/schedules/{id}   -> getSchedule()
```

---

## Event Consumers

```
adapter/input/event/
├── SalesOrderEventConsumer.kt
│   └── Consumes: SalesOrderFulfilledEvent -> Contract activation
│
├── BillingEventConsumer.kt
│   └── Consumes: InvoicePostedEvent -> Trigger recognition
│
└── PeriodCloseEventConsumer.kt
    └── Consumes: FinancialPeriodClosedEvent -> Lock recognition
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── ContractJpaAdapter.kt
│   ├── RevenueScheduleJpaAdapter.kt
│   ├── DeferredRevenueJpaAdapter.kt
│   ├── entity/
│   │   ├── ContractEntity.kt
│   │   ├── ObligationEntity.kt
│   │   ├── RevenueScheduleEntity.kt
│   │   └── DeferredRevenueEntity.kt
│   └── repository/
│       ├── ContractJpaRepository.kt
│       ├── RevenueScheduleJpaRepository.kt
│       └── DeferredRevenueJpaRepository.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/
├── integration/
│   ├── GLPostingAdapter.kt           # Post revenue entries
│   └── BillingAdapter.kt             # Billing/contract data
│
├── messaging/
│   ├── kafka/
│   │   ├── RevenueEventPublisher.kt
│   │   ├── RevenueEventConsumer.kt
│   │   └── schema/
│   │       ├── RevenueRecognizedSchema.avro
│   │       └── RevenueDeferredSchema.avro
│   └── outbox/
│       └── RevenueOutboxEventPublisher.kt
│
└── reporting/
    └── RevenueDisclosureReadAdapter.kt
```

---

## Configuration & Resources

```
configuration/
├── RevenueDependencyInjection.kt
├── PersistenceConfiguration.kt
├── MessagingConfiguration.kt
└── SchedulerConfiguration.kt

resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_revenue_schema.sql
    ├── V2__create_contract_tables.sql
    └── V3__create_schedule_tables.sql
```
