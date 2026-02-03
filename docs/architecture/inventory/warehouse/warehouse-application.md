# Warehouse Execution Application Layer

> Part of [Inventory Warehouse Execution](../inventory-warehouse.md)

## Directory Structure

```
warehouse-application/
└── src/main/kotlin/com.erp.inventory.warehouse.application/
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

### Wave and Task Commands

```
port/input/command/
├── CreateWaveCommand.kt
│   └── warehouseId, orderIds, priority
│
├── ReleaseWaveCommand.kt
│   └── waveId
│
├── CreateTaskCommand.kt
│   └── taskType, itemId, quantity, sourceBin?, targetBin?
│
└── CompleteTaskCommand.kt
    └── taskId, actualQuantity, workerId
```

### Putaway and Replenishment Commands

```
├── ConfirmPutawayCommand.kt
│   └── taskId, targetBin, quantity
│
├── ConfirmPickCommand.kt
│   └── taskId, sourceBin, quantity
│
├── TriggerReplenishmentCommand.kt
│   └── itemId, fromBin, toBin, quantity
│
└── CompleteReplenishmentCommand.kt
    └── taskId
```

### Returns and Labor Commands

```
├── RecordReturnDispositionCommand.kt
│   └── returnId, dispositionType, notes
│
└── RecordLaborCommand.kt
    └── workerId, taskId, durationMinutes
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetWaveByIdQuery.kt
│   └── waveId -> PickWaveDto
│
├── GetTaskQueueQuery.kt
│   └── warehouseId, taskType? -> List<TaskDto>
│
├── GetBinStatusQuery.kt
│   └── binId -> BinStatusDto
│
└── GetLaborMetricsQuery.kt
    └── fromDate, toDate -> LaborMetricsDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── WaveRepository.kt
├── TaskRepository.kt
├── WarehouseRepository.kt
├── PutawayRuleRepository.kt
└── LaborRepository.kt
```

### Integration Ports

```
├── InventoryCorePort.kt            # Stock confirmations
├── ProcurementPort.kt              # ASN and receipts
├── SalesPort.kt                    # Orders and priorities
├── ManufacturingPort.kt            # Staging requests
├── AutomationPort.kt               # RFID/AMR/AGV
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── WaveCommandHandler.kt
│   ├── TaskCommandHandler.kt
│   ├── PutawayCommandHandler.kt
│   ├── ReplenishmentCommandHandler.kt
│   └── ReturnsCommandHandler.kt
└── query/
    ├── WaveQueryHandler.kt
    ├── TaskQueryHandler.kt
    ├── BinQueryHandler.kt
    └── LaborQueryHandler.kt
```
