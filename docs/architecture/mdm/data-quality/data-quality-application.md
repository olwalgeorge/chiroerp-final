# Master Data Quality Application Layer

> Part of [Master Data Quality Rules](../mdm-data-quality.md)

## Directory Structure

```
data-quality-application/
`-- src/main/kotlin/com.erp.mdm.quality.application/
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
|-- CreateValidationRuleCommand.kt
|   `-- domain, ruleType, expression
|
|-- ActivateValidationRuleCommand.kt
|   `-- ruleId
|
`-- EvaluateDataQualityCommand.kt
    `-- domain, masterId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetValidationRuleQuery.kt
|   `-- ruleId -> ValidationRuleDto
|
|-- ListValidationRulesQuery.kt
|   `-- domain -> List<ValidationRuleDto>
|
`-- GetDataQualityScoreQuery.kt
    `-- masterId -> DataQualityScoreDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- ValidationRuleRepository.kt
|-- DataQualityScoreRepository.kt
|-- MasterDataPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- ValidationRuleCommandHandler.kt
|   `-- DataQualityEvaluationHandler.kt
`-- query/
    |-- ValidationRuleQueryHandler.kt
    `-- DataQualityScoreQueryHandler.kt
```
