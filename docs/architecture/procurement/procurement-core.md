# Procurement Core - ADR-023

> **Bounded Context:** `procurement-core`  
> **Port:** `9101`  
> **Database:** `chiroerp_procurement_core`  
> **Kafka Consumer Group:** `procurement-core-cg`

## Overview

Procurement Core owns **requisitioning, approvals, and purchase order lifecycle**. It enforces approval workflows, change control, and auditability, and publishes PO/approval events to Inventory and Finance/AP.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Requisitions, PO lifecycle, approvals, change control |
| **Aggregates** | Requisition, PurchaseOrder, ApprovalWorkflow, POChange |
| **Key Events** | PurchaseRequisitionSubmittedEvent, PurchaseOrderApprovedEvent, PurchaseOrderIssuedEvent |
| **GL Integration** | PO approval triggers GR/IR setup in Finance/AP |
| **Compliance** | SOX approvals, audit trail, SoD enforcement |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [core-domain.md](./core/core-domain.md) | Aggregates, entities, value objects, domain events, exceptions, services |
| **Application Layer** | [core-application.md](./core/core-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [core-infrastructure.md](./core/core-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [core-api.md](./core/core-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [core-events.md](./core/core-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Bounded Context

```
procurement-core/
├── core-domain/
├── core-application/
└── core-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        PROCUREMENT-CORE SERVICE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ SubmitRequisition│  │ ApprovePO        │  │ ChangePO          │  │    │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ RequisitionRepo │ PORepo │ ApprovalPort │ EventPublisherPort   │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │    │
│  │  │ Requisition  │  │ PurchaseOrder│  │ ApprovalFlow │  │ POChange  │ │    │
│  │  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │ Aggregate │ │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘ │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Approval Rules      │  │ Domain Services                      │    │    │
│  │  │ Budget & Policy      │  │ SoD, Compliance, Change Control     │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Scheduler  │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Batch)    │   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Requisitioning
- Role-based requisition entry and approvals
- Budget checks and policy enforcement

### 2. Purchase Order Lifecycle
- PO creation, approval, and issuance
- Change control and revision tracking

### 3. Compliance & Audit
- Segregation of duties and approval thresholds
- Immutable audit trail for all PO changes

## Integration Points

```
┌──────────────┐   PO Approved      ┌──────────────┐
│ procurement- │ ─────────────────> │ inventory    │
│   core       │                    │ (receiving)  │
└──────────────┘                    └──────────────┘
       │ PO Approved
       ▼
┌──────────────┐
│ finance / AP │
└──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| PO approval latency | < 2s p95 | > 5s |
| Requisition submission | < 1s p95 | > 2s |
| PO change processing | < 2s p95 | > 5s |
| API availability | 99.95% | < 99.90% |

## Compliance & Audit

- SOX approval controls and change logs
- Anti-bribery controls for vendor selection
- Segregation of duties enforced

## Related ADRs

- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
