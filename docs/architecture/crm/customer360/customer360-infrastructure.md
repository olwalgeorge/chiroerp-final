# CRM Customer 360 Infrastructure Layer

> Part of [CRM Customer 360](../crm-customer360.md)

## Directory Structure

```
customer360-infrastructure/
`-- src/main/kotlin/com.erp.crm.customer360.infrastructure/
    |-- adapter/
    |   |-- input/
    |   |   |-- rest/
    |   |   `-- event/
    |   `-- output/
    |       |-- persistence/
    |       |-- integration/
    |       `-- messaging/
    |-- configuration/
    `-- resources/
```

---

## REST Adapters (Primary/Driving)

```
adapter/input/rest/
|-- CustomerProfileResource.kt
|   |-- POST /api/v1/crm/customers         -> createCustomer()
|   |-- GET  /api/v1/crm/customers/{id}    -> getCustomer()
|   `-- POST /api/v1/crm/customers/{id}/merge -> mergeCustomer()
|-- ConsentResource.kt
|   `-- POST /api/v1/crm/customers/{id}/consent -> updateConsent()
```

---

## Event Consumers

```
adapter/input/event/
`-- CustomerMasterUpdatedEventConsumer.kt
    `-- Consumes: CustomerMasterUpdatedEvent -> Sync profile
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- CustomerProfileJpaAdapter.kt
`-- ConsentJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- CustomerProfileEntity.kt
|-- ContactEntity.kt
`-- ConsentEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- MdgAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- Customer360EventPublisher.kt
|   `-- schema/
|       |-- CustomerProfileCreatedSchema.avro
|       `-- CustomerMergedSchema.avro
`-- outbox/
    `-- Customer360OutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- Customer360DependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
