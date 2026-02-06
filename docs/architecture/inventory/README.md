# Inventory (MM-IM) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-024

## First-Level Modules
- `inventory-shared/` — Shared types across inventory subdomains
- `inventory-core/` — Port 9001 - Stock Ledger, Valuation, Reservations
- `inventory-warehouse/` — Port 9002 - WMS: Wave Planning, Tasks, Putaway, Pick
- `inventory-valuation/` — Port 9005 - Cost Layers, Landed Cost, FX Revaluation
- `inventory-atp/` — Port 9006 - ATP Calculation, Channel Allocation, Safety Stock
- `inventory-traceability/` — Port 9007 - Lot/Serial Tracking, FEFO, Recall Management
- `inventory-advanced-ops/` — Port 9008 - Packaging, Kitting, Repack, Catch Weight
- `inventory-forecasting/` — Port 9009 - AI Demand Forecasting & Replenishment
