# Plant Maintenance Preventive Application Layer

> Part of [Plant Maintenance Preventive Maintenance](../maintenance-preventive.md)

## Directory Structure

```
preventive-application/
`-- src/main/kotlin/com.erp.maintenance.preventive.application/
    |-- port/
    |   |-- input/
    |   |   |-- command/
    |   |   `-- query/
    |   `-- output/
    `-- service/
        |-- command/
        `-- query/
```

---

## Commands (Write Operations)

```
port/input/command/
|-- CreateMaintenancePlanCommand.kt
|   `-- equipmentId, planType, interval
|
|-- GenerateScheduleCommand.kt
|   `-- planId, horizonDays
|
|-- UpdateCounterReadingCommand.kt
|   `-- equipmentId, counterValue
|
`-- ActivatePlanCommand.kt
    `-- planId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetMaintenancePlanQuery.kt
|   `-- planId -> MaintenancePlanDto
|
|-- ListMaintenanceSchedulesQuery.kt
|   `-- equipmentId -> List<ScheduleDto>
|
`-- GetCounterReadingQuery.kt
    `-- equipmentId -> CounterDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- MaintenancePlanRepository.kt
|-- ScheduleRepository.kt
|-- WorkOrderPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- MaintenancePlanCommandHandler.kt
|   `-- ScheduleCommandHandler.kt
`-- query/
    |-- MaintenancePlanQueryHandler.kt
    `-- ScheduleQueryHandler.kt
```
