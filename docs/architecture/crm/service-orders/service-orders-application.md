# CRM Service Orders Application Layer

> Part of [CRM Service Orders](../crm-service-orders.md)

## Directory Structure

```
service-orders-application/
`-- src/main/kotlin/com.erp.crm.serviceorders.application/
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
|-- CreateServiceOrderCommand.kt
|   `-- customerId, locationId, priority, description
|-- ScheduleServiceOrderCommand.kt
|   `-- serviceOrderId, scheduleWindow
|-- RecordWorkLogCommand.kt
|   `-- serviceOrderId, technicianId, workPerformed
|-- CompleteServiceOrderCommand.kt
|   `-- serviceOrderId, resolutionCode
`-- ApproveServiceBillingCommand.kt
    `-- serviceOrderId, approverId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetServiceOrderByIdQuery.kt
|   `-- serviceOrderId -> ServiceOrderDto
|-- ListOpenServiceOrdersQuery.kt
|   `-- status -> List<ServiceOrderDto>
`-- GetSlaStatusQuery.kt
    `-- serviceOrderId -> SlaStatusDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- ServiceOrderRepository.kt
|-- SlaRepository.kt
|-- InventoryReservationPort.kt
|-- DispatchPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- ServiceOrderCommandHandler.kt
|   `-- WorkLogCommandHandler.kt
`-- query/
    |-- ServiceOrderQueryHandler.kt
    `-- SlaQueryHandler.kt
```
