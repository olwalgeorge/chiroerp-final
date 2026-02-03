# Inventory Valuation Events & Integration

> Part of [Inventory Valuation & Costing](../inventory-valuation.md)

---

## Domain Events Published

### ValuationRunCompletedEvent

**Trigger**: Valuation run completed  
**Consumers**: Finance/GL, Analytics

```json
{
  "eventType": "ValuationRunCompletedEvent",
  "payload": {
    "runId": "val-001",
    "period": "2026-02",
    "method": "WAC",
    "totalValue": 250000.00,
    "currency": "USD"
  }
}
```

### LandedCostAllocatedEvent

**Trigger**: Landed cost allocation completed  
**Consumers**: Finance/GL

```json
{
  "eventType": "LandedCostAllocatedEvent",
  "payload": {
    "landedCostId": "lc-001",
    "allocationRule": "BY_VALUE",
    "totalCharge": 325.00
  }
}
```

### FxRevaluationPostedEvent

**Trigger**: FX revaluation posted  
**Consumers**: Finance/GL

```json
{
  "eventType": "FxRevaluationPostedEvent",
  "payload": {
    "revaluationId": "fx-001",
    "period": "2026-02",
    "gainLoss": -1250.00,
    "currency": "USD"
  }
}
```

---

## Domain Events Consumed

```
- StockReceivedEvent (from Inventory Core) -> Create cost layers
- StockIssuedEvent (from Inventory Core) -> Consume cost layers
- FinancialPeriodClosedEvent (from Finance Close) -> Lock valuation
```

---

## GL Posting Rules (Examples)

### Valuation Posting
```
Debit:  Inventory (1400)             $10,000
Credit: Inventory Adjustment (5300)  $10,000
```

### FX Revaluation
```
Debit:  FX Loss (7990)               $1,250
Credit: Inventory (1400)             $1,250
```

---

## Avro Schemas

### ValuationRunCompletedSchema.avro

```json
{
  "type": "record",
  "name": "ValuationRunCompletedEvent",
  "namespace": "com.erp.inventory.valuation.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "runId", "type": "string" },
    { "name": "period", "type": "string" },
    { "name": "method", "type": "string" },
    { "name": "totalValue", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" }
  ]
}
```

### LandedCostAllocatedSchema.avro

```json
{
  "type": "record",
  "name": "LandedCostAllocatedEvent",
  "namespace": "com.erp.inventory.valuation.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "landedCostId", "type": "string" },
    { "name": "allocationRule", "type": "string" },
    { "name": "totalCharge", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.valuation.run.completed` | Inventory Valuation | Finance/GL, Analytics | 6 |
| `inventory.valuation.landedcost.allocated` | Inventory Valuation | Finance/GL | 6 |
| `inventory.valuation.fx.revaluation.posted` | Inventory Valuation | Finance/GL | 3 |
