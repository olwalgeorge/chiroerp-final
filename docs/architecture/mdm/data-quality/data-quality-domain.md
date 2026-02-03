# Master Data Quality Domain Layer

> Part of [Master Data Quality Rules](../mdm-data-quality.md)

## Directory Structure

```
data-quality-domain/
`-- src/main/kotlin/com.erp.mdm.quality.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Validation Rule (`model/rule/`)

```
|-- rule/
|   |-- ValidationRule.kt          # Aggregate Root
|   |-- RuleId.kt
|   |-- RuleType.kt                # Format, Completeness, Referential
|   `-- RuleStatus.kt              # Draft, Active, Retired
```

### Quality Score (`model/score/`)

```
|-- score/
|   |-- DataQualityScore.kt        # Entity
|   |-- DomainScore.kt
|   `-- ScoreThreshold.kt
```

---

## Domain Events

```
events/
|-- ValidationRuleActivatedEvent.kt
|-- ValidationRuleUpdatedEvent.kt
`-- DataQualityScoreUpdatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- RuleNotFoundException.kt
|-- InvalidRuleException.kt
|-- ScoreThresholdException.kt
`-- DomainNotSupportedException.kt
```

---

## Domain Services

```
services/
|-- RuleEvaluationService.kt
|-- ScoreCalculationService.kt
`-- DomainValidationService.kt
```

---

## Key Invariants

1. **Active Rules Only**: Only active rules are evaluated.
2. **Threshold Validity**: Score thresholds must be between 0 and 1.
3. **Domain Coverage**: Each domain has a minimum rule set.
4. **Auditability**: Rule changes are versioned.
