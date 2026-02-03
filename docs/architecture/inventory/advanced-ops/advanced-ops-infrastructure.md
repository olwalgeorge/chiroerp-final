# Inventory Advanced Ops Infrastructure Layer

> Part of [Inventory Advanced Operations](../inventory-advanced-ops.md)

## Directory Structure

```
advanced-ops-infrastructure/
└── src/main/kotlin/com.erp.inventory.advancedops.infrastructure/
    ├── adapter/
    │   ├── input/rest/
    │   ├── output/persistence/
    │   └── output/messaging/
    ├── config/
    └── integration/
```

---

## REST Adapters

- `AdvancedOpsResource.kt`
  - `/api/v1/inventory/advanced-ops/packaging`
  - `/api/v1/inventory/advanced-ops/kits`
  - `/api/v1/inventory/advanced-ops/repack-orders`
  - `/api/v1/inventory/advanced-ops/catch-weight`

## Persistence Adapters

- PostgreSQL for master data (packaging hierarchies, kit definitions)
- Transactional records for repack orders and catch weight

## Messaging Adapters

- Kafka outbox publisher for kit/repack/catch-weight events
- Consumers for WMS `TaskCompletedEvent`

## External Integrations

- **Scale Integration**: capture actual weights from scale APIs
- **WMS Integration**: create work orders for kit assembly, repack, break-bulk
- **GL Integration**: post rollup and variance costs

## Configuration

- Topic names follow `inventory.advanced-ops.*`
- Outbox pattern to ensure reliable event publishing
- Retry policies for scale and WMS integrations
