# Quality Inspection Planning Application Layer

> Part of [Quality Inspection Planning](../quality-inspection-planning.md)

## Directory Structure

```
inspection-planning-application/
`-- src/main/kotlin/com.erp.quality.inspectionplanning.application/
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
|-- CreateInspectionPlanCommand.kt
|   `-- planName, itemId, version, effectiveFrom
|
|-- AddCharacteristicCommand.kt
|   `-- planId, name, specMin, specMax, unit
|
|-- UpdateSamplingPlanCommand.kt
|   `-- planId, planType, aqlLevel, sampleSize
|
`-- ActivateTriggerRuleCommand.kt
    `-- planId, triggerType, scope
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetInspectionPlanQuery.kt
|   `-- planId -> InspectionPlanDto
|
|-- ListInspectionPlansQuery.kt
|   `-- itemId, status -> List<InspectionPlanDto>
|
`-- GetSamplingPlanQuery.kt
    `-- planId -> SamplingPlanDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- InspectionPlanRepository.kt
|-- SamplingPlanRepository.kt
|-- TriggerRuleRepository.kt
|-- ItemCatalogPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- InspectionPlanCommandHandler.kt
|   `-- SamplingPlanCommandHandler.kt
`-- query/
    |-- InspectionPlanQueryHandler.kt
    `-- SamplingPlanQueryHandler.kt
```
