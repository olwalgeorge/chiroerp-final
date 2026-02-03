# Inventory Core Application Layer

> Part of [Inventory Core](../inventory-core.md)

## Directory Structure

```
core-application/
└── src/main/kotlin/com.erp.inventory.core.application/
    ├── port/
    │   ├── input/
    │   │   ├── command/
    │   │   └── query/
    │   └── output/
    └── service/
        ├── command/
        └── query/
```

---

## Commands (Write Operations)

### Stock Movement Commands

```
port/input/command/
├── ReceiveStockCommand.kt
│   └── itemId, locationId, quantity, uom, lot?, serials?, cost
│
├── IssueStockCommand.kt
│   └── itemId, locationId, quantity, uom, reservationId?
│
├── TransferStockCommand.kt
│   └── itemId, fromLocationId, toLocationId, quantity, uom
│
└── AdjustStockCommand.kt
    └── itemId, locationId, quantityDelta, reasonCode
```

### Reservation Commands

```
├── CreateReservationCommand.kt
│   └── itemId, locationId, quantity, channel, reference
│
├── ReleaseReservationCommand.kt
│   └── reservationId, reason
│
└── ConfirmReservationCommand.kt
    └── reservationId
```

### Item and Master Data Commands

```
├── CreateItemCommand.kt
│   └── sku, description, baseUom, attributes
│
├── AddVariantCommand.kt
│   └── itemId, variantCode, barcodes
│
└── UpdateUoMConversionCommand.kt
    └── itemId, fromUom, toUom, factor
```

### Counting and Valuation Commands

```
├── StartCycleCountCommand.kt
│   └── locationId, itemIds?, scheduledDate
│
├── PostCycleCountCommand.kt
│   └── cycleCountId, lines[]
│
└── RunValuationCommand.kt
    └── period, method
```

---

## Queries (Read Operations)

### Stock Queries

```
port/input/query/
├── GetStockOnHandQuery.kt
│   └── itemId, locationId -> StockOnHandDto
│
├── GetInventoryByLocationQuery.kt
│   └── locationId -> LocationInventoryDto
│
└── GetStockLedgerQuery.kt
    └── itemId, dateRange -> List<StockMovementDto>
```

### Reservation and Availability Queries

```
├── GetAvailabilityQuery.kt
│   └── itemId, channel -> AvailabilityDto
│
└── GetReservationByIdQuery.kt
    └── reservationId -> ReservationDto
```

### Valuation and Reporting Queries

```
├── GetCostLayersQuery.kt
│   └── itemId -> List<CostLayerDto>
│
└── GetInventoryValuationQuery.kt
    └── period -> InventoryValuationDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── ItemRepository.kt
├── LocationRepository.kt
├── StockLedgerRepository.kt
├── ReservationRepository.kt
├── CostLayerRepository.kt
└── CycleCountRepository.kt
```

### Integration Ports

```
├── GeneralLedgerPort.kt             # Inventory postings
├── ProcurementPort.kt               # Goods receipt events
├── SalesPort.kt                     # Sales allocations
├── ManufacturingPort.kt             # Material issues/receipts
├── WmsPort.kt                       # WMS confirmations
├── FxRatePort.kt                    # Multi-currency valuation
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── StockCommandHandler.kt
│   ├── ReservationCommandHandler.kt
│   ├── ItemCommandHandler.kt
│   ├── CycleCountCommandHandler.kt
│   └── ValuationCommandHandler.kt
└── query/
    ├── StockQueryHandler.kt
    ├── ReservationQueryHandler.kt
    ├── AvailabilityQueryHandler.kt
    └── ValuationQueryHandler.kt
```
