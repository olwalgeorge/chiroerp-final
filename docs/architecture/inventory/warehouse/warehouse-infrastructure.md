# Warehouse Execution Infrastructure Layer

> Part of [Inventory Warehouse Execution](../inventory-warehouse.md)

## Directory Structure

```
warehouse-infrastructure/
└── src/main/kotlin/com.erp.inventory.warehouse.infrastructure/
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

### Wave Resource

```
adapter/input/rest/
├── WaveResource.kt
│   ├── POST   /api/v1/inventory/warehouse/waves          -> createWave()
│   ├── POST   /api/v1/inventory/warehouse/waves/{id}/release -> releaseWave()
│   └── GET    /api/v1/inventory/warehouse/waves/{id}     -> getWave()
```

### Task Resource

```
├── TaskResource.kt
│   ├── POST   /api/v1/inventory/warehouse/tasks          -> createTask()
│   ├── POST   /api/v1/inventory/warehouse/tasks/{id}/complete -> completeTask()
│   └── GET    /api/v1/inventory/warehouse/tasks         -> listTasks()
```

### Putaway and Replenishment Resources

```
├── PutawayResource.kt
│   └── POST   /api/v1/inventory/warehouse/putaway/confirm -> confirmPutaway()
│
└── ReplenishmentResource.kt
    └── POST   /api/v1/inventory/warehouse/replenishments -> triggerReplenishment()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateWaveRequest.kt
│   ├── warehouseId: String
│   ├── orderIds: List<String>
│   └── priority: String
│
├── CreateTaskRequest.kt
│   ├── taskType: String
│   ├── itemId: String
│   ├── quantity: BigDecimal
│   ├── sourceBin: String?
│   └── targetBin: String?
│
├── CompleteTaskRequest.kt
│   ├── actualQuantity: BigDecimal
│   └── workerId: String
│
└── ConfirmPutawayRequest.kt
    ├── taskId: String
    ├── targetBin: String
    └── quantity: BigDecimal
```

### Response DTOs

```
dto/response/
├── PickWaveDto.kt
│   ├── id, status, priority, orderCount
│
├── TaskDto.kt
│   ├── id, type, status, itemId, quantity, sourceBin, targetBin
│
└── BinStatusDto.kt
    ├── binId, capacity, usedCapacity
```

---

## Event Consumers

```
adapter/input/event/
├── ReservationEventConsumer.kt
│   └── Consumes: ReservationCreatedEvent -> Create pick tasks
│
├── InventorySnapshotConsumer.kt
│   └── Consumes: InventorySnapshotEvent -> Refresh bin visibility
│
└── GoodsReceiptNoticeConsumer.kt
    └── Consumes: GoodsReceiptNoticeEvent -> Create putaway tasks
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── WaveJpaAdapter.kt            -> implements WaveRepository
├── TaskJpaAdapter.kt            -> implements TaskRepository
├── WarehouseJpaAdapter.kt       -> implements WarehouseRepository
└── PutawayRuleJpaAdapter.kt     -> implements PutawayRuleRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── WaveEntity.kt
├── TaskEntity.kt
├── WarehouseZoneEntity.kt
└── BinEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── InventoryCoreAdapter.kt      # Movement confirmations
├── ProcurementAdapter.kt        # ASN receipts
├── SalesOrderAdapter.kt         # Order priorities
└── ManufacturingAdapter.kt      # Staging requests
```

```
adapter/output/messaging/
├── kafka/
│   ├── WarehouseEventPublisher.kt
│   └── schema/
│       ├── TaskCompletedSchema.avro
│       └── WaveReleasedSchema.avro
└── outbox/
    └── WarehouseOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── WarehouseDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_warehouse_schema.sql
    ├── V2__create_zone_bin_tables.sql
    ├── V3__create_task_tables.sql
    └── V4__create_wave_tables.sql
```
