# Analytics OLAP Events & Integration

> Part of [Analytics OLAP & Cube Engine](../analytics-olap.md)

---

## Domain Events Published

### CubeRefreshedEvent

```json
{
  "eventType": "CubeRefreshedEvent",
  "payload": {
    "cubeId": "CUBE-100",
    "refreshedAt": "2026-02-02T10:00:00Z"
  }
}
```

### AggregateSnapshotPublishedEvent

```json
{
  "eventType": "AggregateSnapshotPublishedEvent",
  "payload": {
    "cubeId": "CUBE-100",
    "window": "2026-02"
  }
}
```

---

## Domain Events Consumed

```
- WarehouseLoadCompletedEvent (from Data Warehouse) -> Refresh cube
```

---

## Avro Schemas

### CubeRefreshedSchema.avro

```json
{
  "type": "record",
  "name": "CubeRefreshedEvent",
  "namespace": "com.erp.analytics.olap.events",
  "fields": [
    { "name": "cubeId", "type": "string" },
    { "name": "refreshedAt", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `analytics.olap.cube.refreshed` | OLAP | KPI, Dashboards | 3 |
| `analytics.olap.snapshot.published` | OLAP | KPI | 3 |
