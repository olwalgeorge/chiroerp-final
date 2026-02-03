# Quality CAPA Events & Integration

> Part of [Quality CAPA Management](../quality-capa.md)

---

## Domain Events Published

### CAPAInitiatedEvent

```json
{
  "eventType": "CAPAInitiatedEvent",
  "payload": {
    "capaId": "CAPA-500",
    "ncId": "NC-1000",
    "capaType": "CORRECTIVE"
  }
}
```

### CAPAClosedEvent

```json
{
  "eventType": "CAPAClosedEvent",
  "payload": {
    "capaId": "CAPA-500",
    "effectiveness": "VERIFIED"
  }
}
```

---

## Domain Events Consumed

```
- NonconformanceCreatedEvent (from Nonconformance) -> Initiate CAPA
```

---

## Avro Schemas

### CAPAInitiatedSchema.avro

```json
{
  "type": "record",
  "name": "CAPAInitiatedEvent",
  "namespace": "com.erp.quality.capa.events",
  "fields": [
    { "name": "capaId", "type": "string" },
    { "name": "ncId", "type": "string" },
    { "name": "capaType", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `quality.capa.initiated` | CAPA | Analytics | 3 |
| `quality.capa.closed` | CAPA | Analytics, Inspection Planning | 3 |
