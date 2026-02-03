# Inventory ATP Application Layer

> Part of [Inventory ATP & Allocation](../inventory-atp.md)

## Directory Structure

```
atp-application/
└── src/main/kotlin/com.erp.inventory.atp.application/
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

```
port/input/command/
├── CommitAllocationCommand.kt
│   └── itemId, channel, quantity, reference
│
├── ReleaseAllocationCommand.kt
│   └── allocationId, reason
│
└── UpdateSafetyStockCommand.kt
    └── itemId, locationId, quantity
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetAvailabilityQuery.kt
│   └── itemId, channel, promiseDate? -> AvailabilityDto
│
└── GetAllocationPoolQuery.kt
    └── itemId, locationId -> AllocationPoolDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── AllocationPoolRepository.kt
├── ReservationRuleRepository.kt
└── SafetyStockRepository.kt
```

### Integration Ports

```
├── InventoryCorePort.kt             # Stock and reservations
├── SalesPort.kt                     # Orders and commitments
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── AllocationCommandHandler.kt
│   └── SafetyStockCommandHandler.kt
└── query/
    ├── AvailabilityQueryHandler.kt
    └── AllocationPoolQueryHandler.kt
```
