# Master Data Quality Events & Integration

> Part of [Master Data Quality Rules](../mdm-data-quality.md)

---

## Domain Events Published

### ValidationRuleActivatedEvent

```json
{
  "eventType": "ValidationRuleActivatedEvent",
  "payload": {
    "ruleId": "RULE-10",
    "domain": "CUSTOMER",
    "ruleType": "COMPLETENESS"
  }
}
```

### DataQualityScoreUpdatedEvent

```json
{
  "eventType": "DataQualityScoreUpdatedEvent",
  "payload": {
    "masterId": "M-100",
    "domain": "CUSTOMER",
    "score": 0.96
  }
}
```

---

## Domain Events Consumed

```
- MasterRecordPublishedEvent (from MDM Hub) -> Recalculate scores
```

---

## Avro Schemas

### DataQualityScoreUpdatedSchema.avro

```json
{
  "type": "record",
  "name": "DataQualityScoreUpdatedEvent",
  "namespace": "com.erp.mdm.quality.events",
  "fields": [
    { "name": "masterId", "type": "string" },
    { "name": "domain", "type": "string" },
    { "name": "score", "type": "double" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `mdm.quality.rule.activated` | Data Quality | MDM Hub | 3 |
| `mdm.quality.score.updated` | Data Quality | MDM Hub, Analytics | 6 |
