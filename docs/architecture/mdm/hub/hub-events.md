# Master Data Hub Events & Integration

> Part of [Master Data Hub](../mdm-hub.md)

---

## Domain Events Published

### MasterRecordPublishedEvent

```json
{
  "eventType": "MasterRecordPublishedEvent",
  "payload": {
    "masterId": "M-100",
    "domain": "CUSTOMER",
    "version": 3
  }
}
```

### ChangeRequestApprovedEvent

```json
{
  "eventType": "ChangeRequestApprovedEvent",
  "payload": {
    "changeRequestId": "CR-100",
    "masterId": "M-100"
  }
}
```

---

## Domain Events Consumed

```
- DuplicateDetectedEvent (from Match & Merge) -> Review merge proposal
- DataQualityScoreUpdatedEvent (from Data Quality) -> Flag low quality
```

---

## Avro Schemas

### MasterRecordPublishedSchema.avro

```json
{
  "type": "record",
  "name": "MasterRecordPublishedEvent",
  "namespace": "com.erp.mdm.hub.events",
  "fields": [
    { "name": "masterId", "type": "string" },
    { "name": "domain", "type": "string" },
    { "name": "version", "type": "int" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `mdm.hub.master.published` | MDM Hub | All domains | 12 |
| `mdm.hub.change.approved` | MDM Hub | Stewardship, Analytics | 6 |
