# Quality Nonconformance Events & Integration

> Part of [Quality Nonconformance](../quality-nonconformance.md)

---

## Domain Events Published

### NonconformanceCreatedEvent

```json
{
  "eventType": "NonconformanceCreatedEvent",
  "payload": {
    "ncId": "NC-1000",
    "severity": "MAJOR",
    "sourceLotId": "LOT-900"
  }
}
```

### DispositionDeterminedEvent

```json
{
  "eventType": "DispositionDeterminedEvent",
  "payload": {
    "ncId": "NC-1000",
    "dispositionType": "REWORK"
  }
}
```

### QualityCostPostedEvent

```json
{
  "eventType": "QualityCostPostedEvent",
  "payload": {
    "ncId": "NC-1000",
    "costCategory": "INTERNAL",
    "amount": 250.00
  }
}
```

---

## Domain Events Consumed

```
- DefectDetectedEvent (from Quality Execution) -> Create NC
- ReturnReceivedEvent (from Sales) -> Customer complaint NC
```

---

## Avro Schemas

### NonconformanceCreatedSchema.avro

```json
{
  "type": "record",
  "name": "NonconformanceCreatedEvent",
  "namespace": "com.erp.quality.nonconformance.events",
  "fields": [
    { "name": "ncId", "type": "string" },
    { "name": "severity", "type": "string" },
    { "name": "sourceLotId", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `quality.nonconformance.created` | Nonconformance | CAPA, Analytics | 6 |
| `quality.nonconformance.disposition` | Nonconformance | Inventory | 6 |
| `quality.nonconformance.cost.posted` | Nonconformance | Finance/CO | 3 |
