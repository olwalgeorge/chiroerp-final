# Close Infrastructure Layer

> Part of [Finance - Period Close](../finance-close.md)

## Directory Structure

```
close-infrastructure/
└── src/main/kotlin/com.erp.finance.close.infrastructure/
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
├── CloseRunResource.kt
│   ├── POST /api/v1/close/runs            -> startCloseRun()
│   ├── POST /api/v1/close/runs/{id}/finalize -> finalizeCloseRun()
│   └── GET  /api/v1/close/runs/{id}       -> getCloseRun()
│
├── CloseTaskResource.kt
│   └── POST /api/v1/close/tasks/{id}/complete -> completeTask()
```

---

## Event Consumers

```
adapter/input/event/
├── SubledgerClosedConsumer.kt
│   └── Consumes: SubledgerClosedEvent -> Update checklist
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── CloseRunJpaAdapter.kt
│   ├── CloseTaskJpaAdapter.kt
│   └── ReconciliationJpaAdapter.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/
├── integration/
│   └── GLCloseAdapter.kt             # Trigger GL close
│
├── messaging/
│   ├── kafka/
│   │   ├── CloseEventPublisher.kt
│   │   └── schema/
│   │       ├── CloseRunCompletedSchema.avro
│   └── outbox/
│       └── CloseOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── CloseDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt

resources/
└── db/migration/
    ├── V1__create_close_schema.sql
    ├── V2__create_close_run_tables.sql
    └── V3__create_close_task_tables.sql
```
