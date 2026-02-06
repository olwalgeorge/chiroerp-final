# Procurement (MM-PUR) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-023

## First-Level Modules
- `procurement-shared/` — Shared procurement types
- `procurement-core/` — Port 9201 - Requisitions, PO lifecycle, approvals
- `procurement-sourcing/` — Port 9202 - RFQ/RFP, quotes, awards
- `procurement-suppliers/` — Port 9203 - Vendor onboarding, compliance, lifecycle
- `procurement-receiving/` — Port 9204 - Goods receipt, service entry, inspection
- `procurement-invoice-match/` — Port 9205 - 2/3-way match, GR/IR reconciliation
