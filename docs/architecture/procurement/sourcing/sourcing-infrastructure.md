# Procurement Sourcing Infrastructure Layer

> Part of [Procurement Sourcing & RFQ](../procurement-sourcing.md)

## Directory Structure

```
sourcing-infrastructure/
└── src/main/kotlin/com.erp.procurement.sourcing.infrastructure/
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
├── RFQResource.kt
│   ├── POST /api/v1/procurement/rfqs -> issueRFQ()
│   └── GET  /api/v1/procurement/rfqs/{id} -> getRFQ()

├── QuoteResource.kt
│   ├── POST /api/v1/procurement/rfqs/{id}/quotes -> submitQuote()
│   └── GET  /api/v1/procurement/rfqs/{id}/quotes -> listQuotes()
```

---

## Event Consumers

```
adapter/input/event/
├── SupplierStatusConsumer.kt
│   └── Consumes: VendorUpdatedEvent -> Update eligible suppliers
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
├── RFQJpaAdapter.kt
├── QuoteJpaAdapter.kt
└── AwardJpaAdapter.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/messaging/
├── kafka/
│   ├── SourcingEventPublisher.kt
│   └── schema/
│       ├── RFQIssuedSchema.avro
│       └── AwardGrantedSchema.avro
```

---

## Configuration & Resources

```
configuration/
├── SourcingDependencyInjection.kt
└── MessagingConfiguration.kt
```

```
resources/
└── db/migration/
    ├── V1__create_sourcing_schema.sql
    ├── V2__create_rfq_tables.sql
    └── V3__create_quote_tables.sql
```
