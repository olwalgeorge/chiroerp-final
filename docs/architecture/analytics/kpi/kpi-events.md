# Analytics KPI Events & Integration

> Part of [Analytics KPI Engine](../analytics-kpi.md)

---

## Domain Events Published

### KpiCalculatedEvent

```json
{
  "eventType": "KpiCalculatedEvent",
  "payload": {
    "kpiId": "KPI-100",
    "period": "2026-02",
    "value": 4.8
  }
}
```

### KpiThresholdBreachedEvent

```json
{
  "eventType": "KpiThresholdBreachedEvent",
  "payload": {
    "kpiId": "KPI-100",
    "threshold": "WARNING",
    "value": 2.5
  }
}
```

---

## Domain Events Consumed

```
- WarehouseLoadCompletedEvent (from Data Warehouse) -> Refresh KPIs
- CubeRefreshedEvent (from OLAP) -> Refresh KPIs
```

---

## Avro Schemas

### KpiCalculatedSchema.avro

```json
{
  "type": "record",
  "name": "KpiCalculatedEvent",
  "namespace": "com.erp.analytics.kpi.events",
  "fields": [
    { "name": "kpiId", "type": "string" },
    { "name": "period", "type": "string" },
    { "name": "value", "type": "double" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `analytics.kpi.calculated` | KPI Engine | Dashboards, Embedded | 6 |
| `analytics.kpi.threshold.breached` | KPI Engine | Alerts, Embedded | 3 |
