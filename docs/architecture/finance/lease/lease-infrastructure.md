# Lease Infrastructure Layer

> Part of [Finance - Lease Accounting](../finance-lease.md)

## Directory Structure

```
lease-infrastructure/
└── src/main/kotlin/com.erp.finance.lease.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       └── messaging/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

```
adapter/input/rest/
├── LeaseContractResource.kt
│   ├── POST /api/v1/lease/contracts          -> createLease()
│   ├── POST /api/v1/lease/contracts/{id}/activate -> activateLease()
│   └── GET  /api/v1/lease/contracts/{id}     -> getLease()
│
├── LeaseAmortizationResource.kt
│   └── POST /api/v1/lease/contracts/{id}/amortize -> postAmortization()
```

---

## Event Consumers

```
adapter/input/event/
├── PeriodCloseEventConsumer.kt
│   └── Consumes: FinancialPeriodClosedEvent -> Lock amortization
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── LeaseContractJpaAdapter.kt
│   ├── LeaseScheduleJpaAdapter.kt
│   └── ROUAssetJpaAdapter.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/
├── integration/
│   └── GLPostingAdapter.kt
│
├── messaging/
│   ├── kafka/
│   │   ├── LeaseEventPublisher.kt
│   │   └── schema/
│   │       ├── LeaseActivatedSchema.avro
│   │       └── LeaseAmortizationPostedSchema.avro
│   └── outbox/
│       └── LeaseOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── LeaseDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt

resources/
└── db/migration/
    ├── V1__create_lease_schema.sql
    ├── V2__create_lease_contract_tables.sql
    └── V3__create_lease_schedule_tables.sql
```
