# Quality Inspection Planning Events & Integration

> Part of [Quality Inspection Planning](../quality-inspection-planning.md)

---

## Domain Events Published

### InspectionPlanCreatedEvent

```json
{
  "eventType": "InspectionPlanCreatedEvent",
  "payload": {
    "planId": "PLAN-100",
    "itemId": "ITEM-100",
    "version": "v1",
    "effectiveFrom": "2026-02-01"
  }
}
```

### TriggerRuleActivatedEvent

```json
{
  "eventType": "TriggerRuleActivatedEvent",
  "payload": {
    "planId": "PLAN-100",
    "triggerType": "INCOMING",
    "scope": "ITEM"
  }
}
```

---

## Domain Events Consumed

```
- GoodsReceivedEvent (from Procurement) -> Evaluate incoming triggers
- ProductionOrderReleasedEvent (from Manufacturing) -> Evaluate in-process triggers
- ReturnReceivedEvent (from Sales) -> Evaluate return triggers
```

---

## Avro Schemas

### InspectionPlanCreatedSchema.avro

```json
{
  "type": "record",
  "name": "InspectionPlanCreatedEvent",
  "namespace": "com.erp.quality.inspectionplanning.events",
  "fields": [
    { "name": "planId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "version", "type": "string" },
    { "name": "effectiveFrom", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `quality.planning.plan.created` | Inspection Planning | Quality Execution | 6 |
| `quality.planning.trigger.activated` | Inspection Planning | Quality Execution | 6 |
