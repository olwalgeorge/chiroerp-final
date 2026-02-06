# Commerce (SD / Omnichannel) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-025

## First-Level Modules
- `commerce-shared/` — ADR-006 COMPLIANT: Identifiers, VOs, Enums ONLY
- `commerce-ecommerce/` — Port 9301 - E-Commerce (B2C/D2C Online)
- `commerce-pos/` — Port 9302 - Point of Sale (Retail Store)
- `commerce-b2b/` — Port 9303 - B2B Commerce (Business Customers)
- `commerce-marketplace/` — Port 9304 - Marketplace (Multi-seller)
- `commerce-pricing/` — Port 9305 - Dynamic Pricing & Markdown Optimization
