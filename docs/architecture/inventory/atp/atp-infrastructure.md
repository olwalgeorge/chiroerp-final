# Inventory ATP Infrastructure Layer

> Part of [Inventory ATP & Allocation](../inventory-atp.md)

## Directory Structure

```
atp-infrastructure/
└── src/main/kotlin/com.erp.inventory.atp.infrastructure/
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

### Availability Resource

```
adapter/input/rest/
├── AvailabilityResource.kt
│   ├── GET  /api/v1/inventory/atp/availability -> getAvailability()
│   └── GET  /api/v1/inventory/atp/pools/{itemId} -> getAllocationPool()
```

### Allocation Resource

```
├── AllocationResource.kt
│   ├── POST /api/v1/inventory/atp/allocations -> commitAllocation()
│   └── POST /api/v1/inventory/atp/allocations/{id}/release -> releaseAllocation()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CommitAllocationRequest.kt
│   ├── itemId: String
│   ├── channel: String
│   ├── quantity: BigDecimal
│   └── reference: String
│
└── ReleaseAllocationRequest.kt
    └── reason: String
```

### Response DTOs

```
dto/response/
├── AvailabilityDto.kt
│   ├── itemId, channel, available, reserved
│
└── AllocationPoolDto.kt
    ├── itemId, locationId, poolQuantity
```

---

## Event Consumers

```
adapter/input/event/
├── StockAvailabilityChangedConsumer.kt
│   └── Consumes: StockAvailabilityChangedEvent -> Refresh cache
│
└── ReservationCreatedConsumer.kt
    └── Consumes: ReservationCreatedEvent -> Update pool
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── AllocationPoolJpaAdapter.kt     -> implements AllocationPoolRepository
├── ReservationRuleJpaAdapter.kt    -> implements ReservationRuleRepository
└── SafetyStockJpaAdapter.kt        -> implements SafetyStockRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── AllocationPoolEntity.kt
├── ChannelAllocationEntity.kt
└── SafetyStockEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── InventoryCoreAdapter.kt
└── SalesOrderAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── AtpEventPublisher.kt
│   └── schema/
│       ├── AvailabilityCalculatedSchema.avro
│       └── AllocationCommittedSchema.avro
└── outbox/
    └── AtpOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── AtpDependencyInjection.kt
├── PersistenceConfiguration.kt
└── CacheConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_atp_schema.sql
    ├── V2__create_allocation_pool_tables.sql
    └── V3__create_safety_stock_tables.sql
```
