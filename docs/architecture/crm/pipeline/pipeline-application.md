# CRM Pipeline Application Layer

> Part of [CRM Pipeline](../crm-pipeline.md)

## Directory Structure

```
pipeline-application/
`-- src/main/kotlin/com.erp.crm.pipeline.application/
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
|-- CreateOpportunityCommand.kt
|   `-- customerId, name, amount, currency, expectedCloseDate
|-- UpdateOpportunityCommand.kt
|   `-- opportunityId, changes
|-- ChangeOpportunityStageCommand.kt
|   `-- opportunityId, stage
|-- CloseOpportunityCommand.kt
|   `-- opportunityId, outcome, reason
`-- CreateForecastSnapshotCommand.kt
    `-- period, ownerId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetOpportunityByIdQuery.kt
|   `-- opportunityId -> OpportunityDto
|-- ListPipelineByOwnerQuery.kt
|   `-- ownerId -> List<OpportunityDto>
`-- GetForecastSnapshotQuery.kt
    `-- forecastId -> ForecastDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- OpportunityRepository.kt
|-- StageDefinitionRepository.kt
|-- ForecastRepository.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- OpportunityCommandHandler.kt
|   `-- ForecastCommandHandler.kt
`-- query/
    |-- OpportunityQueryHandler.kt
    `-- ForecastQueryHandler.kt
```
