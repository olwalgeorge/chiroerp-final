# Procurement Core Events & Integration

> Part of [Procurement Core](../procurement-core.md)

---

## Domain Events Published

### PurchaseOrderApprovedEvent

**Trigger**: PO approved
**Consumers**: Inventory, Finance/AP

```json
{
  "eventType": "PurchaseOrderApprovedEvent",
  "payload": {
    "poId": "PO-2026-000123",
    "supplierId": "vendor-001",
    "totalAmount": 250.00,
    "currency": "USD"
  }
}
```

### PurchaseOrderIssuedEvent

**Trigger**: PO issued to supplier
**Consumers**: Supplier portal, Inventory

```json
{
  "eventType": "PurchaseOrderIssuedEvent",
  "payload": {
    "poId": "PO-2026-000123",
    "issuedAt": "2026-02-02T10:00:00Z"
  }
}
```

---

## Domain Events Consumed

```
- VendorUpdatedEvent (from Supplier Mgmt) -> Validate supplier status
```

---

## GL Posting Rules (Examples)

### PO Approval (GR/IR Setup)
```
No direct posting. AP prepares GR/IR clearing on receipt.
```

---

## Avro Schemas

### PurchaseOrderApprovedSchema.avro

```json
{
  "type": "record",
  "name": "PurchaseOrderApprovedEvent",
  "namespace": "com.erp.procurement.core.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "poId", "type": "string" },
    { "name": "supplierId", "type": "string" },
    { "name": "totalAmount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `procurement.po.approved` | Procurement Core | Inventory, Finance/AP | 6 |
| `procurement.po.issued` | Procurement Core | Supplier Portal, Inventory | 3 |
| `procurement.requisition.submitted` | Procurement Core | Approvals, Analytics | 3 |
