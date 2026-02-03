# CRM Dispatch Application Layer

> Part of [CRM Dispatch](../crm-dispatch.md)

## Directory Structure

```
dispatch-application/
`-- src/main/kotlin/com.erp.crm.dispatch.application/
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
|-- CreateDispatchRequestCommand.kt
|   `-- serviceOrderId, locationId, requiredSkills
|-- AssignTechnicianCommand.kt
|   `-- dispatchId, technicianId
|-- ConfirmDispatchCommand.kt
|   `-- dispatchId
|-- RejectDispatchCommand.kt
|   `-- dispatchId, reason
|-- OptimizeRouteCommand.kt
|   `-- dispatchId
`-- UpdateTechnicianStatusCommand.kt
    `-- technicianId, status
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetDispatchByIdQuery.kt
|   `-- dispatchId -> DispatchDto
|-- ListAvailableTechniciansQuery.kt
|   `-- locationId -> List<TechnicianDto>
`-- GetRoutePlanQuery.kt
    `-- dispatchId -> RoutePlanDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- DispatchRepository.kt
|-- TechnicianRepository.kt
|-- MapRoutingPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- DispatchCommandHandler.kt
|   `-- TechnicianCommandHandler.kt
`-- query/
    |-- DispatchQueryHandler.kt
    `-- TechnicianQueryHandler.kt
```
