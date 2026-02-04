# Close Events & Integration

> Part of [Finance - Period Close](../finance-close.md)

---

## Domain Events Published

### CloseRunCompletedEvent

**Trigger**: Close run finalized
**Consumers**: General Ledger, Reporting

```json
{
  "eventType": "CloseRunCompletedEvent",
  "payload": {
    "runId": "close-001",
    "periodId": "per-2026-02",
    "ledgerId": "ledger-001",
    "completedAt": "2026-03-02T01:00:00Z"
  }
}
```

---

## Domain Events Consumed

```
- SubledgerClosedEvent       (from AP/AR/Assets) -> Update checklist
```

---

## GL Integration

### Period Close Trigger
- Finalize close run -> invoke GL period close

---

## Avro Schemas

### CloseRunCompletedSchema.avro

```json
{
  "type": "record",
  "name": "CloseRunCompletedEvent",
  "namespace": "com.erp.finance.close.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "runId", "type": "string" },
    { "name": "periodId", "type": "string" },
    { "name": "ledgerId", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.close.run.completed` | Close | GL, Reporting | 3 |
