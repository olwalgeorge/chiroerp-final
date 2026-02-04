# Procurement Sourcing & RFQ - ADR-023

> **Bounded Context:** `procurement-sourcing`
> **Port:** `9102`
> **Database:** `chiroerp_procurement_sourcing`
> **Kafka Consumer Group:** `procurement-sourcing-cg`

## Overview

Sourcing manages **RFQ/RFP workflows, quote evaluation, and award decisions**. It supports compliance-driven sourcing, quote comparisons, and contract awards that feed Procurement Core for PO creation.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | RFQ/RFP, quote comparison, award decisions |
| **Aggregates** | RFQ, Quote, SupplierBid, AwardDecision |
| **Key Events** | RFQIssuedEvent, QuoteSubmittedEvent, AwardGrantedEvent |
| **Integration** | Feeds PO creation in Procurement Core |
| **Compliance** | Fair sourcing, audit trail |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [sourcing-domain.md](./sourcing/sourcing-domain.md) | Aggregates, entities, value objects, domain events, exceptions, services |
| **Application Layer** | [sourcing-application.md](./sourcing/sourcing-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [sourcing-infrastructure.md](./sourcing/sourcing-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [sourcing-api.md](./sourcing/sourcing-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [sourcing-events.md](./sourcing/sourcing-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
procurement-sourcing/
├── sourcing-domain/
├── sourcing-application/
└── sourcing-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       PROCUREMENT-SOURCING SERVICE                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ IssueRFQ         │  │ EvaluateQuotes   │  │ GrantAward        │  │    │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ RFQRepo │ QuoteRepo │ AwardRepo │ EventPublisherPort            │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │    │
│  │  │ RFQ      │  │ Quote        │  │ SupplierBid  │  │ AwardDecision│  │    │
│  │  │ Aggregate│  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │    │
│  │  └──────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Evaluation Rules   │  │ Domain Services                      │    │    │
│  │  │ Scorecards         │  │ Compliance, Award Policies           │    │    │
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

### 1. RFQ/RFP Management
- RFQ issuance and supplier invitations
- Deadlines, amendments, and compliance controls

### 2. Quote Evaluation
- Price/quality/lead-time scoring
- Automated shortlisting and approvals

### 3. Award Decisions
- Award approvals and notifications
- Contract handoff to Procurement Core

## Integration Points

```
┌──────────────────┐   AwardGranted   ┌──────────────────┐
│ procurement-     │ ───────────────> │ procurement-core │
│ sourcing         │                  │ (PO creation)    │
└──────────────────┘                  └──────────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| RFQ issuance | < 2s p95 | > 5s |
| Quote evaluation | < 5s p95 | > 10s |
| Award decision | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.5% |

## Compliance & Audit

- Fair sourcing policies and audit trail
- Mandatory approvals for high-risk spend

## Related ADRs

- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md)
- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
