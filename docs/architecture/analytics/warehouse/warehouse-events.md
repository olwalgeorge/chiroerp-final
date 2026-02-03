# Analytics Warehouse Events & Integration

> Part of [Analytics Data Warehouse](../analytics-warehouse.md)

---

## Domain Events Published

### WarehouseLoadCompletedEvent

```json
{
  "eventType": "WarehouseLoadCompletedEvent",
  "payload": {
    "batchId": "LOAD-100",
    "sourceSystem": "inventory",
    "recordCount": 120000
  }
}
```

### DimensionUpdatedEvent

```json
{
  "eventType": "DimensionUpdatedEvent",
  "payload": {
    "dimension": "dim_customer",
    "version": 5
  }
}
```

---

## Domain Events Consumed

```
- DomainEventIngested (from CDC/Event Bus) -> Stage load
```

---

## Avro Schemas

### WarehouseLoadCompletedSchema.avro

```json
{
  "type": "record",
  "name": "WarehouseLoadCompletedEvent",
  "namespace": "com.erp.analytics.warehouse.events",
  "fields": [
    { "name": "batchId", "type": "string" },
    { "name": "sourceSystem", "type": "string" },
    { "name": "recordCount", "type": "long" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `analytics.warehouse.load.completed` | Data Warehouse | KPI, OLAP | 6 |
| `analytics.warehouse.dimension.updated` | Data Warehouse | OLAP | 3 |
