# Intercompany Infrastructure Layer

> Part of [Finance - Intercompany](../finance-intercompany.md)

## Directory Structure

```
intercompany-infrastructure/
└── src/main/kotlin/com.erp.finance.intercompany.infrastructure/
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

```
adapter/input/rest/
├── IntercompanyTransactionResource.kt
│   ├── POST /api/v1/intercompany/transactions       -> createTransaction()
│   ├── POST /api/v1/intercompany/transactions/{id}/post -> postTransaction()
│   └── GET  /api/v1/intercompany/transactions/{id}  -> getTransaction()
│
├── NettingResource.kt
│   ├── POST /api/v1/intercompany/netting/run        -> runNetting()
│   └── GET  /api/v1/intercompany/netting/{id}       -> getNettingBatch()
```

---

## Event Consumers

```
adapter/input/event/
├── PeriodCloseEventConsumer.kt
│   └── Consumes: FinancialPeriodClosedEvent -> Trigger eliminations
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── AgreementJpaAdapter.kt
│   ├── TransactionJpaAdapter.kt
│   ├── NettingBatchJpaAdapter.kt
│   └── EliminationJpaAdapter.kt
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
│   │   ├── IntercompanyEventPublisher.kt
│   │   └── schema/
│   │       ├── IntercompanyPostedSchema.avro
│   │       └── NettingCompletedSchema.avro
│   └── outbox/
│       └── IntercompanyOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── IntercompanyDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt

resources/
└── db/migration/
    ├── V1__create_intercompany_schema.sql
    ├── V2__create_ic_transaction_tables.sql
    └── V3__create_netting_tables.sql
```
