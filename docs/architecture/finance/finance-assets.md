# Finance Assets (Fixed Assets) - ADR-021

> **Bounded Context:** `finance-assets`
> **Port:** `8084`
> **Database:** `chiroerp_finance_assets`
> **Kafka Consumer Group:** `finance-assets-cg`

## Overview

The Fixed Assets subdomain manages the complete lifecycle of capitalized assets—from acquisition through depreciation, revaluation, impairment, and disposal. It handles complex multi-book depreciation, asset hierarchies, and generates GL journal entries for all asset movements.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Fixed asset lifecycle, depreciation, disposal |
| **Aggregates** | Asset, AssetClass, DepreciationBook, AssetTransfer, PhysicalInventory |
| **Key Events** | AssetCapitalizedEvent, DepreciationPostedEvent, AssetDisposedEvent, AssetTransferredEvent |
| **GL Integration** | Asset acquisition, depreciation expense, accumulated depreciation, disposal gain/loss |
| **Compliance** | IFRS 16, ASC 842, IAS 16, IAS 36 (Impairment) |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [assets-domain.md](./assets/assets-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [assets-application.md](./assets/assets-application.md) | Commands, queries, ports, command/query handlers |
| **Infrastructure Layer** | [assets-infrastructure.md](./assets/assets-infrastructure.md) | REST resources, JPA adapters, Kafka messaging, configuration |
| **REST API** | [assets-api.md](./assets/assets-api.md) | Complete REST endpoint reference with examples |
| **Events & GL Integration** | [assets-events.md](./assets/assets-events.md) | Domain events, consumed events, GL journal entries, Avro schemas |

## Bounded Context

```
finance-assets/
├── assets-domain/
├── assets-application/
└── assets-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FINANCE-ASSETS SERVICE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │   │
│  │  │ CapitalizeAsset │  │ PostDepreciation│  │ DisposeAsset        │  │   │
│  │  │ CommandHandler  │  │ CommandHandler  │  │ CommandHandler      │  │   │
│  │  └────────┬────────┘  └────────┬────────┘  └──────────┬──────────┘  │   │
│  │           │                    │                      │              │   │
│  │  ┌────────┴────────────────────┴──────────────────────┴──────────┐  │   │
│  │  │                    OUTPUT PORTS (Interfaces)                   │  │   │
│  │  │  AssetRepository │ DepreciationBookRepo │ GLIntegrationPort   │  │   │
│  │  └───────────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌──────────┐  ┌────────────┐  ┌───────────────┐  ┌──────────────┐  │   │
│  │  │  Asset   │  │ AssetClass │  │Depreciation   │  │AssetTransfer │  │   │
│  │  │Aggregate │  │ Aggregate  │  │Book Aggregate │  │  Aggregate   │  │   │
│  │  └──────────┘  └────────────┘  └───────────────┘  └──────────────┘  │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ PhysicalInventory  │  │ Domain Services                      │   │   │
│  │  │    Aggregate       │  │ DepreciationCalculator, Revaluation │   │   │
│  │  └────────────────────┘  └─────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                    INFRASTRUCTURE LAYER                              │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐  │   │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Scheduler  │  │   │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Batch)    │  │   │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Asset Lifecycle Management
- Asset acquisition (purchase, construction, donation)
- Capital vs. expense threshold enforcement
- Asset categorization and classification
- Sub-asset and component tracking
- Asset retirement and disposal

### 2. Multi-Book Depreciation
- Corporate book (GAAP/IFRS)
- Tax book (local tax regulations)
- Management book (internal reporting)
- Parallel depreciation methods per book

### 3. Depreciation Methods
- Straight-line
- Declining balance (single/double)
- Sum-of-years-digits
- Units of production
- Custom depreciation schedules

### 4. Asset Movements
- Inter-company transfers
- Location transfers
- Cost center reassignment
- Partial transfers

### 5. Physical Inventory
- Barcode/RFID tracking
- Physical count reconciliation
- Missing asset detection
- Condition assessment

## Integration Points

```
┌──────────────┐     AssetCapitalized      ┌──────────────┐
│   finance-   │ ──────────────────────►   │  finance-gl  │
│    assets    │     DepreciationPosted    │              │
│              │ ──────────────────────►   │   (Journal   │
│              │     AssetDisposed         │   Entries)   │
│              │ ──────────────────────►   └──────────────┘
└──────────────┘
       ▲
       │  PurchaseOrderApprovedEvent
       │  ProjectCapitalized
┌──────┴───────┐
│ procurement  │
│   /project   │
└──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Asset Capitalization Latency | < 200ms p99 | > 500ms |
| Depreciation Batch (10,000 assets) | < 30 min p95 | > 60 min |
| Asset Query Latency | < 50ms p95 | > 100ms |
| Event Publishing Latency | < 100ms p99 | > 200ms |
| Monthly Close Depreciation Run | < 5 min | > 10 min |

## Compliance & Audit

- **IAS 16** - Property, Plant and Equipment
- **IAS 36** - Impairment of Assets
- **IFRS 16** - Leases (for ROU assets)
- **ASC 360** - Property, Plant, and Equipment (US GAAP)
- **SOX 404** - Internal controls over financial reporting
- Full audit trail for all asset transactions
- Segregation of duties enforcement

## Related ADRs

- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-021: Fixed Assets Subdomain](../../adr/ADR-021-fixed-asset-accounting.md) - Asset-specific decisions
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
