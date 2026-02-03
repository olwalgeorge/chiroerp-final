# Analytics & Reporting Domain Architecture

This directory contains hexagonal architecture specifications for Analytics & Reporting subdomains.

## Subdomain Index

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Data Warehouse** | ADR-016 | 9801 | Core | [analytics-warehouse.md](./analytics-warehouse.md) | Facts, dimensions, historical BI |
| **OLAP/Cube Engine** | ADR-016 | 9802 | Core | [analytics-olap.md](./analytics-olap.md) | Cubes, aggregates, dimensional queries |
| **KPI Engine** | ADR-016 | 9803 | Core | [analytics-kpi.md](./analytics-kpi.md) | KPI definitions, calculations, alerts |
| **Dashboard Builder** | ADR-016 | 9804 | Core | [analytics-dashboard.md](./analytics-dashboard.md) | Dashboards, widgets, sharing |
| **Report Scheduler** | ADR-016 | 9805 | Core | [analytics-scheduler.md](./analytics-scheduler.md) | Scheduled reports, delivery, retention |
| **Embedded Analytics** | ADR-016 | 9806 | Core | [analytics-embedded.md](./analytics-embedded.md) | In-app KPIs and insights |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Data Warehouse** (`/warehouse/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **OLAP/Cube Engine** (`/olap/`) - 5 detailed files
- **KPI Engine** (`/kpi/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Dashboard Builder** - UI definitions and sharing
- **Report Scheduler** - Delivery and retention workflows
- **Embedded Analytics** - Contextual KPIs

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9801 | analytics-warehouse | chiroerp_analytics_warehouse | Implemented |
| 9802 | analytics-olap | chiroerp_analytics_olap | Implemented |
| 9803 | analytics-kpi | chiroerp_analytics_kpi | Implemented |
| 9804 | analytics-dashboard | chiroerp_analytics_olap (shared) | Implemented |
| 9805 | analytics-scheduler | chiroerp_analytics_warehouse (shared) | Implemented |
| 9806 | analytics-embedded | chiroerp_analytics_olap (shared) | Implemented |

**Note:** Inline modules share their parent service database and run as part of the core service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
+----------------------------------------------------------------------------+
|                           ANALYTICS & REPORTING                            |
|----------------------------------------------------------------------------|
|                                                                            |
|  Domain Events -> Warehouse -> OLAP -> KPI -> Dashboards/Embedded           |
|                    |                 |               |                     |
|                    +---- Scheduler ---+               |                     |
|                                                                            |
+----------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **All Domains** -> `DomainEventIngested` -> Warehouse loads

### Downstream Consumers (Publish Events)
- **Dashboards/Embedded** <- `KpiCalculatedEvent`, `CubeRefreshedEvent`
- **Scheduler** <- `WarehouseLoadCompletedEvent`

## References

### Related ADRs
- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
- [ADR-015: Data Lifecycle Management](../../adr/ADR-015-data-lifecycle-management.md)

### Related Domains
- [Finance Architecture](../finance/README.md)
- [Inventory Architecture](../inventory/README.md)
- [Procurement Architecture](../procurement/README.md)
- [Sales Architecture](../sales/README.md)
- [Manufacturing Architecture](../manufacturing/README.md)
- [CRM Architecture](../crm/README.md)
- [Quality Architecture](../quality/README.md)
- [Maintenance Architecture](../maintenance/README.md)
- [MDM Architecture](../mdm/README.md)

---

## Phase 1 Status: Complete

**6 modules implemented:**
- Data Warehouse
- OLAP/Cube Engine
- KPI Engine
- Dashboard Builder
- Report Scheduler
- Embedded Analytics

**Total documentation:** 22 files (3 overview + 15 detailed subfolder files + 3 inline + 1 README)
