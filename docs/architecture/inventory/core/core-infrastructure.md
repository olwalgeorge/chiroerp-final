# Inventory Core Infrastructure Layer

> Part of [Inventory Core](../inventory-core.md)

## Directory Structure

```
core-infrastructure/
└── src/main/kotlin/com.erp.inventory.core.infrastructure/
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

### Item Resource

```
adapter/input/rest/
├── ItemResource.kt
│   ├── POST   /api/v1/inventory/items           -> createItem()
│   ├── GET    /api/v1/inventory/items/{id}      -> getItem()
│   └── GET    /api/v1/inventory/items           -> listItems()
```

### Stock Movement Resource

```
├── StockMovementResource.kt
│   ├── POST   /api/v1/inventory/stock-movements -> postMovement()
│   ├── GET    /api/v1/inventory/stock-ledger    -> getLedger()
│   └── GET    /api/v1/inventory/availability    -> getAvailability()
```

### Reservation Resource

```
├── ReservationResource.kt
│   ├── POST   /api/v1/inventory/reservations           -> createReservation()
│   ├── POST   /api/v1/inventory/reservations/{id}/confirm -> confirmReservation()
│   └── POST   /api/v1/inventory/reservations/{id}/release -> releaseReservation()
```

### Cycle Count Resource

```
├── CycleCountResource.kt
│   ├── POST   /api/v1/inventory/cycle-counts       -> startCycleCount()
│   └── POST   /api/v1/inventory/cycle-counts/{id}/post -> postCycleCount()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateItemRequest.kt
│   ├── sku: String
│   ├── description: String
│   ├── baseUom: String
│   └── attributes: Map<String, String>?
│
├── StockMovementRequest.kt
│   ├── itemId: String
│   ├── locationId: String
│   ├── quantity: BigDecimal
│   ├── uom: String
│   ├── movementType: String
│   ├── lotNumber: String?
│   ├── serialNumbers: List<String>?
│   └── cost: BigDecimal?
│
├── CreateReservationRequest.kt
│   ├── itemId: String
│   ├── locationId: String
│   ├── quantity: BigDecimal
│   ├── channel: String
│   └── reference: String
│
└── PostCycleCountRequest.kt
    ├── lines: List<CycleCountLineRequest>
    └── varianceReason: String?
```

### Response DTOs

```
dto/response/
├── ItemDto.kt
│   ├── id, sku, description, baseUom, status
│   └── variants: List<ItemVariantDto>
│
├── StockOnHandDto.kt
│   ├── itemId, locationId, quantity, available, reserved
│
├── ReservationDto.kt
│   ├── id, itemId, locationId, quantity, status, channel
│
└── CycleCountDto.kt
    ├── id, locationId, status, totalVariance
```

---

## Event Consumers

```
adapter/input/event/
├── GoodsReceivedEventConsumer.kt
│   └── Consumes: GoodsReceivedEvent -> Receive stock
│
├── SalesOrderAllocatedEventConsumer.kt
│   └── Consumes: SalesOrderAllocatedEvent -> Create reservation
│
└── ProductionReceiptEventConsumer.kt
    └── Consumes: ProductionReceiptEvent -> Receive manufactured stock
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── ItemJpaAdapter.kt           -> implements ItemRepository
├── StockLedgerJpaAdapter.kt    -> implements StockLedgerRepository
├── ReservationJpaAdapter.kt    -> implements ReservationRepository
├── CostLayerJpaAdapter.kt      -> implements CostLayerRepository
└── CycleCountJpaAdapter.kt     -> implements CycleCountRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── ItemEntity.kt
├── ItemVariantEntity.kt
├── StockMovementEntity.kt
├── ReservationEntity.kt
├── CostLayerEntity.kt
└── CycleCountEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── GLPostingAdapter.kt
├── ProcurementAdapter.kt
├── SalesOrderAdapter.kt
├── ManufacturingAdapter.kt
└── WmsAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── InventoryEventPublisher.kt
│   └── schema/
│       ├── StockReceivedSchema.avro
│       ├── StockIssuedSchema.avro
│       └── ReservationCreatedSchema.avro
└── outbox/
    └── InventoryOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── InventoryDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_inventory_schema.sql
    ├── V2__create_item_tables.sql
    ├── V3__create_stock_ledger_tables.sql
    ├── V4__create_reservation_tables.sql
    ├── V5__create_cost_layer_tables.sql
    └── V6__create_cycle_count_tables.sql
```
