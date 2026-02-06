# Master Data Governance (MDG) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-027

## First-Level Modules
- `mdm-shared/` — Shared types for MDM context
- `mdm-hub/` — Port 9701 - Golden Record Management
- `mdm-data-quality/` — Port 9702 - Validation & Quality Scoring
- `mdm-stewardship/` — Port 9703 - Approval Workflows & SoD
- `mdm-match-merge/` — Port 9704 - Duplicate Detection & Survivorship
- `mdm-analytics/` — Port 9705 - Quality KPIs & Dashboards
