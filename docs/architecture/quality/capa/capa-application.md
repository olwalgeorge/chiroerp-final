# Quality CAPA Application Layer

> Part of [Quality CAPA Management](../quality-capa.md)

## Directory Structure

```
capa-application/
`-- src/main/kotlin/com.erp.quality.capa.application/
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
|-- InitiateCAPACommand.kt
|   `-- ncId, capaType, description
|
|-- RecordRootCauseCommand.kt
|   `-- capaId, method, statement
|
|-- CreateActionPlanCommand.kt
|   `-- capaId, actions[]
|
|-- VerifyEffectivenessCommand.kt
|   `-- capaId, result
|
`-- CloseCAPACommand.kt
    `-- capaId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetCAPAQuery.kt
|   `-- capaId -> CAPADto
|
|-- ListCAPAQuery.kt
|   `-- status -> List<CAPADto>
|
`-- GetCAPAEffectivenessQuery.kt
    `-- capaId -> EffectivenessDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- CAPARepository.kt
|-- ActionTaskPort.kt
|-- InspectionPlanPort.kt
|-- ManufacturingProcessPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- CAPACommandHandler.kt
|   `-- ActionPlanCommandHandler.kt
`-- query/
    |-- CAPAQueryHandler.kt
    `-- CAPAEffectivenessQueryHandler.kt
```
