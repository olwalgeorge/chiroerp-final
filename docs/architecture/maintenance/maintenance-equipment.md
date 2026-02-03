# Plant Maintenance Equipment Master - ADR-040

> **Bounded Context:** `maintenance-equipment`  
> **Port:** `9601`  
> **Database:** `chiroerp_maintenance_equipment`  
> **Kafka Consumer Group:** `maintenance-equipment-cg`

## Overview

Equipment Master owns the **equipment hierarchy, functional locations, and technical attributes** required for maintenance planning and execution. It links equipment to fixed assets and tracks installation, warranty, and documentation metadata.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Equipment hierarchy, technical attributes, classifications |
| **Aggregates** | Equipment, FunctionalLocation, EquipmentClass |
| **Key Events** | EquipmentCreatedEvent, EquipmentAssignedEvent |
| **Integration** | Fixed Assets, Manufacturing, Quality |
| **Compliance** | ISO 55000 asset records |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [equipment-domain.md](./equipment/equipment-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [equipment-application.md](./equipment/equipment-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [equipment-infrastructure.md](./equipment/equipment-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [equipment-api.md](./equipment/equipment-api.md) | Endpoints and DTOs |
| **Events & Integration** | [equipment-events.md](./equipment/equipment-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
maintenance-equipment/
|-- equipment-domain/
|-- equipment-application/
`-- equipment-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                      EQUIPMENT MASTER                                  |
|-----------------------------------------------------------------------|
|  Hierarchy + Attributes + Documents -> Equipment Records -> Maintenance |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Equipment hierarchy (plant, location, system, equipment).
2. Technical attributes and classification by criticality.
3. Installation history and warranty tracking.
4. Document links to manuals and schematics.

## Integration Points

- **Fixed Assets**: equipment-to-asset linkage.
- **Manufacturing**: work center/equipment references.
- **Quality**: calibration requirements.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Equipment lookup | < 200ms p95 | > 500ms |
| Equipment creation | < 1s p95 | > 3s |
| API availability | 99.7% | < 99.5% |

## Compliance & Audit

- Equipment change history retained.
- Classification changes require approval.

## Related ADRs

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
- [ADR-021: Fixed Asset Accounting](../../adr/ADR-021-fixed-asset-accounting.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
