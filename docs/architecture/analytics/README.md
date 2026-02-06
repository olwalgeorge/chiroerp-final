# Analytics & Reporting Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-016

## First-Level Modules
- `analytics-shared/` — Shared types across analytics subdomains
- `analytics-warehouse/` — Port 9801 - Data Warehouse, Facts, Dimensions, ETL
- `analytics-olap/` — Port 9802 - OLAP/Cube Engine, Aggregations
- `analytics-kpi/` — Port 9803 - KPI Engine, Thresholds, Alerts
- `analytics-dashboard/` — Port 9804 (inline) - Dashboard Builder, Widgets
- `analytics-scheduler/` — Port 9805 (inline) - Report Scheduler, Delivery
- `analytics-embedded/` — Port 9806 (inline) - Embedded Analytics, Contextual KPIs
