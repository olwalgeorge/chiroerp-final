# Procurement Supplier Management - ADR-023

> **Bounded Context:** `procurement-suppliers`
> **Port:** `9103` (logical, part of procurement-core service)
> **Database:** `chiroerp_procurement_core`
> **Kafka Consumer Group:** `procurement-core-cg`

## Overview

Supplier Management governs **vendor onboarding, compliance, and lifecycle**. It ensures KYC/AML checks, document validity, and supplier classification with audit-ready controls.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Supplier onboarding, compliance, lifecycle |
| **Aggregates** | Supplier, SupplierCompliance, SupplierScorecard |
| **Key Events** | VendorCreatedEvent, VendorUpdatedEvent, SupplierBlockedEvent |
| **Integration** | Feeds AP vendor master and procurement workflows |
| **Compliance** | KYC/AML, anti-bribery, GDPR |

## Key Capabilities

- Supplier onboarding with approvals
- Compliance document tracking (W9/W8, certificates)
- Risk classification and blocking
- Supplier scorecards and KPIs

## Domain Model

```
Supplier
├── supplierId
├── name
├── status: Active | OnHold | Blocked
└── classifications: List<String>

SupplierCompliance
├── documents
├── expiryDates
└── riskLevel

SupplierScorecard
├── onTimeDelivery
├── qualityScore
└── costVariance
```

## Workflows

1. Supplier onboarding request submitted.
2. Compliance checks and documentation review.
3. Supplier approved and activated for purchasing.

## Domain Events Published

```json
{
  "eventType": "VendorCreatedEvent",
  "payload": {
    "vendorId": "vendor-001",
    "name": "Acme Supplies",
    "status": "ACTIVE"
  }
}
```

```json
{
  "eventType": "VendorUpdatedEvent",
  "payload": {
    "vendorId": "vendor-001",
    "status": "ON_HOLD"
  }
}
```

## Domain Events Consumed

```
- VendorMasterUpdatedEvent (from MDG) -> Sync supplier master
```

## Integration Points

- **Finance/AP**: Vendor master synchronization
- **Procurement Core**: Supplier status checks

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| VendorCreatedEvent | `procurement.suppliers.vendor.created` | 6 | 30d |
| VendorUpdatedEvent | `procurement.suppliers.vendor.updated` | 6 | 30d |
| SupplierBlockedEvent | `procurement.suppliers.vendor.blocked` | 6 | 30d |
| VendorMasterUpdatedEvent (consumed) | `mdm.vendor.master.updated` | 6 | 30d |

**Consumer Group:** `procurement-core-cg`
**Partition Key:** `vendorId` (ensures all events for a vendor are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/procurement/suppliers
Content-Type: application/json

{
  "name": "Acme Supplies",
  "taxId": "12-3456789",
  "country": "US"
}
```

```http
POST /api/v1/procurement/suppliers/{supplierId}/block
Content-Type: application/json

{
  "reason": "Compliance expired"
}
```

## Error Responses

```json
{
  "errorCode": "SUPPLIER_DUPLICATE_TAX_ID",
  "message": "A supplier with tax ID '12-3456789' already exists",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "taxId": "12-3456789",
    "existingVendorId": "vendor-500"
  }
}
```

```json
{
  "errorCode": "SUPPLIER_COMPLIANCE_EXPIRED",
  "message": "Cannot activate supplier: W9 form expired",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "vendorId": "vendor-001",
    "documentType": "W9",
    "expiryDate": "2026-01-15"
  }
}
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| API availability | 99.7% | < 99.5% |
| Command latency | < 1s p95 | > 3s |
| Event processing lag | < 30s p95 | > 2m |

## Compliance & Controls

- Mandatory KYC/AML checks
- Supplier blocking and audit trail
- SoD between onboarding and approval

## References

- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md)
