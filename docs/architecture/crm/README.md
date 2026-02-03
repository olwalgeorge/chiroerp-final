# CRM and Field Service Domain Architecture

This directory contains hexagonal architecture specifications for CRM and Field Service subdomains.

## Subdomain Index

### Phase 1 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Customer 360** | ADR-043 | 9401 | Add-on | [crm-customer360.md](./crm-customer360.md) | Customer profile, contacts, preferences |
| **Pipeline** | ADR-043 | 9402 | Add-on | [crm-pipeline.md](./crm-pipeline.md) | Opportunities, stages, forecasts |
| **Service Orders** | ADR-042 | 9403 | Add-on | [crm-service-orders.md](./crm-service-orders.md) | Work orders, SLAs, service billing |
| **Contracts** | ADR-043 | 9404 | Add-on | [crm-contracts.md](./crm-contracts.md) | Service contracts, entitlements, renewals |
| **Dispatch** | ADR-042 | 9405 | Add-on | [crm-dispatch.md](./crm-dispatch.md) | Technician scheduling and routing |
| **Activity** | ADR-043 | 9406 | Add-on | [crm-activity.md](./crm-activity.md) | Calls, emails, tasks, interaction history |
| **Account Health** | ADR-043 | 9407 | Add-on | [crm-account-health.md](./crm-account-health.md) | Health scoring and risk signals |
| **Parts Consumption** | ADR-042 | 9408 | Add-on | [crm-parts-consumption.md](./crm-parts-consumption.md) | Parts usage for service orders |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Customer 360** (`/customer360/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Pipeline** (`/pipeline/`) - 5 detailed files
- **Service Orders** (`/service-orders/`) - 5 detailed files
- **Contracts** (`/contracts/`) - 5 detailed files
- **Dispatch** (`/dispatch/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Activity** - Single file with interaction logging
- **Account Health** - Single file with health scoring
- **Parts Consumption** - Single file with parts usage

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9401 | crm-customer360 | chiroerp_crm_customer360 | Implemented |
| 9402 | crm-pipeline | chiroerp_crm_pipeline | Implemented |
| 9403 | crm-service-orders | chiroerp_crm_service_orders | Implemented |
| 9404 | crm-contracts | chiroerp_crm_contracts | Implemented |
| 9405 | crm-dispatch | chiroerp_crm_dispatch | Implemented |
| 9406 | crm-activity | chiroerp_crm_service_orders (shared) | Implemented |
| 9407 | crm-account-health | chiroerp_crm_service_orders (shared) | Implemented |
| 9408 | crm-parts-consumption | chiroerp_crm_service_orders (shared) | Implemented |

**Note:** Inline modules (activity, account health, parts consumption) share the `chiroerp_crm_service_orders` database and run as part of the Service Orders service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
+------------------------------------------------------------------------------+
|                         CRM AND FIELD SERVICE DOMAIN                         |
+------------------------------------------------------------------------------+
|                                                                              |
|   +----------------+       +----------------+       +---------------------+  |
|   | Customer 360   | <---- | Pipeline       | ----> | Sales               |  |
|   +----------------+       +----------------+       +---------------------+  |
|            |                          |                                |      |
|            | Contracts                | Opportunities                  |      |
|            v                          v                                v      |
|   +----------------+       +----------------+       +---------------------+  |
|   | Contracts      | ----> | Service Orders | <---- | Dispatch            |  |
|   +----------------+       +----------------+       +---------------------+  |
|            |                          |                                |      |
|            | Entitlements             | Parts                          |      |
|            v                          v                                v      |
|   +----------------+       +----------------+       +---------------------+  |
|   | Account Health | <---- | Parts Consump. | ----> | Inventory           |  |
|   +----------------+       +----------------+       +---------------------+  |
|                                                                              |
+------------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **Sales** -> `SalesOrderFulfilledEvent` -> Install/activation services
- **Finance/AR** -> `CustomerBlockedEvent` -> Credit/service holds
- **Inventory** -> `StockIssuedEvent` -> Parts consumption
- **Procurement** -> `PurchaseOrderApprovedEvent` -> Subcontracted service parts

### Downstream Consumers (Publish Events)
- **Sales** <- `OpportunityClosedWonEvent` -> Convert pipeline to order
- **Finance/AR** <- `ServiceOrderBilledEvent`, `CreditMemoRequestedEvent`
- **Inventory** <- `PartsConsumedEvent`, `PartsReturnedEvent`
- **Analytics** <- `AccountHealthUpdatedEvent`, `ServiceSlaBreachedEvent`

## References

### Related ADRs
- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md) - CRM decisions
- [ADR-042: Field Service Operations](../../adr/ADR-042-field-service-operations.md) - Service order and dispatch
- [ADR-025: Sales and Distribution](../../adr/ADR-025-sales-distribution.md) - Pipeline conversion
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Parts usage
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md) - Billing and credit

### Related Domains
- [Sales Architecture](../sales/README.md) - Pipeline conversion and billing
- [Inventory Architecture](../inventory/README.md) - Parts and stock movements
- [Finance Architecture](../finance/README.md) - AR integration

---

## Phase 1 Status: Complete

**8 modules implemented:**
- Customer 360
- Pipeline
- Service Orders
- Contracts
- Dispatch
- Activity
- Account Health
- Parts Consumption

**Total documentation:** 34 files (5 overview + 25 detailed subfolder files + 3 inline + 1 README)

**Next steps:**
- Integration: Extend analytics and quality workflows
- Phase 2: Advanced SLA optimization and route planning
