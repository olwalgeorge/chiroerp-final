# CRM Contracts Infrastructure Layer

> Part of [CRM Contracts](../crm-contracts.md)

## Directory Structure

```
contracts-infrastructure/
`-- src/main/kotlin/com.erp.crm.contracts.infrastructure/
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
|-- ContractResource.kt
|   |-- POST /api/v1/crm/contracts               -> createContract()
|   |-- GET  /api/v1/crm/contracts/{id}          -> getContract()
|   |-- POST /api/v1/crm/contracts/{id}/activate -> activateContract()
|   `-- POST /api/v1/crm/contracts/{id}/renew    -> renewContract()
`-- EntitlementResource.kt
    `-- POST /api/v1/crm/contracts/{id}/entitlements/consume -> consumeEntitlement()
```

---

## Event Consumers

```
adapter/input/event/
|-- ServiceOrderCreatedEventConsumer.kt
|   `-- Consumes: ServiceOrderCreatedEvent -> validate coverage
`-- OpportunityClosedWonEventConsumer.kt
    `-- Consumes: OpportunityClosedWonEvent -> initiate contract
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- ContractJpaAdapter.kt
`-- EntitlementJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- ContractEntity.kt
|-- EntitlementEntity.kt
`-- RenewalQuoteEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- PricingAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- ContractsEventPublisher.kt
|   `-- schema/
|       |-- ContractActivatedSchema.avro
|       `-- EntitlementConsumedSchema.avro
`-- outbox/
    `-- ContractsOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- ContractsDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
