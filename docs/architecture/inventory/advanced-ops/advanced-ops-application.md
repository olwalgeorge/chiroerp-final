# Inventory Advanced Ops Application Layer

> Part of [Inventory Advanced Operations](../inventory-advanced-ops.md)

## Directory Structure

```
advanced-ops-application/
└── src/main/kotlin/com.erp.inventory.advancedops.application/
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

### Packaging Commands

```
├── CreatePackagingHierarchyCommand.kt
│   └── itemId, levels[], gtins[], tiHi
│
├── UpdatePackagingLevelCommand.kt
│   └── hierarchyId, levelId, dimensions, weight
│
└── ValidatePackagingHierarchyCommand.kt
    └── hierarchyId
```

### Kitting Commands

```
├── CreateKitCommand.kt
│   └── kitSku, kitType, components[], substitutions?
│
├── CreateKitAssemblyOrderCommand.kt
│   └── kitId, quantity, warehouseId, priority
│
├── CompleteKitAssemblyCommand.kt
│   └── assemblyOrderId, actualComponents[], variance
│
└── DisassembleKitCommand.kt
    └── kitId, quantity
```

### Repack / VAS Commands

```
├── CreateRepackOrderCommand.kt
│   └── sourceItemId, targetItemId, quantity, reason
│
├── CompleteRepackCommand.kt
│   └── repackOrderId, outputLines[], variance
│
└── CreateVasOrderCommand.kt
    └── referenceId, operations[]
```

### Catch Weight Commands

```
├── RegisterCatchWeightItemCommand.kt
│   └── itemId, nominalWeight, tolerance
│
└── RecordCatchWeightCommand.kt
    └── itemId, lotId?, actualWeight, tareWeight?
```

---

## Queries (Read Operations)

```
├── GetPackagingHierarchyQuery.kt
│   └── itemId -> PackagingHierarchyDto
│
├── GetKitDefinitionQuery.kt
│   └── kitId -> KitDto
│
├── GetKitAvailabilityQuery.kt
│   └── kitId, locationId -> KitAvailabilityDto
│
├── GetRepackOrderQuery.kt
│   └── repackOrderId -> RepackOrderDto
│
└── GetCatchWeightHistoryQuery.kt
    └── itemId, dateRange -> List<CatchWeightRecordDto>
```

---

## Output Ports (Driven Ports)

```
port/output/
├── PackagingRepository.kt
├── KitRepository.kt
├── RepackRepository.kt
├── CatchWeightRepository.kt
├── CoreInventoryPort.kt           # Reservations, stock movements
├── WmsPort.kt                     # Task execution (kit/repack/break-bulk)
├── GeneralLedgerPort.kt           # Cost rollups and variance posting
├── ScaleIntegrationPort.kt        # Actual weight capture
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── PackagingCommandHandler.kt
│   ├── KitCommandHandler.kt
│   ├── RepackCommandHandler.kt
│   └── CatchWeightCommandHandler.kt
└── query/
    ├── PackagingQueryHandler.kt
    ├── KitQueryHandler.kt
    ├── RepackQueryHandler.kt
    └── CatchWeightQueryHandler.kt
```
