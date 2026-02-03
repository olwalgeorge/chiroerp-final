# Manufacturing BOM Application Layer

> Part of [Manufacturing BOM Management](../manufacturing-bom.md)

## Directory Structure

```
bom-application/
`-- src/main/kotlin/com.erp.manufacturing.bom.application/
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
|-- CreateBomCommand.kt
|   `-- itemId, items[], revision
|
|-- UpdateBomCommand.kt
|   `-- bomId, items[]
|
|-- PublishBomCommand.kt
|   `-- bomId
|
|-- CreateRoutingCommand.kt
|   `-- itemId, operations[]
|
`-- UpdateRoutingCommand.kt
    `-- routingId, operations[]
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetBomByIdQuery.kt
|   `-- bomId -> BomDto
|
|-- GetEffectiveBomQuery.kt
|   `-- itemId, date -> BomDto
|
`-- GetRoutingByIdQuery.kt
    `-- routingId -> RoutingDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- BomRepository.kt
|-- RoutingRepository.kt
|-- ItemMasterPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- BomCommandHandler.kt
|   `-- RoutingCommandHandler.kt
`-- query/
    |-- BomQueryHandler.kt
    `-- RoutingQueryHandler.kt
```
