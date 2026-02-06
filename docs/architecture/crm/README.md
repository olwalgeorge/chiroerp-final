# CRM Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-043

## First-Level Modules
- `crm-shared/` — Shared kernel for CRM domain
- `crm-customer360/` — Customer 360 Subdomain (Port 9451)
- `crm-pipeline/` — Sales Pipeline Subdomain (Port 9452)
- `crm-contracts/` — Service Contracts Subdomain (Port 9453)
- `crm-activity/` — Activity Tracking Subdomain (Port 9454)
- `crm-account-health/` — Account Health Subdomain (Port 9455)
