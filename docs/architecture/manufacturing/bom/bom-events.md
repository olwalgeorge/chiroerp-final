# Manufacturing BOM Events & Integration

> Part of [Manufacturing BOM Management](../manufacturing-bom.md)

---

## Domain Events Published

### BOMPublishedEvent

```json
{
  "eventType": "BOMPublishedEvent",
  "payload": {
    "bomId": "BOM-100",
    "itemId": "FG-100",
    "revision": "A"
  }
}
```

### RoutingUpdatedEvent

```json
{
  "eventType": "RoutingUpdatedEvent",
  "payload": {
    "routingId": "R-200",
    "itemId": "FG-100",
    "operationCount": 5
  }
}
```

---

## Domain Events Consumed

```
- ItemDiscontinuedEvent (from Inventory) -> Retire BOM
```

---

## Avro Schemas

### BomPublishedSchema.avro

```json
{
  "type": "record",
  "name": "BOMPublishedEvent",
  "namespace": "com.erp.manufacturing.bom.events",
  "fields": [
    { "name": "bomId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "revision", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `manufacturing.bom.published` | BOM Mgmt | MRP, Production, Costing | 6 |
| `manufacturing.routing.updated` | BOM Mgmt | Production, Capacity | 6 |
