# CRM Customer 360 - ADR-043

> **Bounded Context:** `crm-customer360`  
> **Port:** `9401`  
> **Database:** `chiroerp_crm_customer360`  
> **Kafka Consumer Group:** `crm-customer360-cg`

## Overview

Customer 360 provides a **single customer profile** with contacts, preferences, relationships, and consent history. It is the canonical CRM view used by Pipeline, Service Orders, and Finance interactions.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Customer profile, contacts, preferences, consent |
| **Aggregates** | CustomerProfile, Contact, ConsentRecord |
| **Key Events** | CustomerProfileCreatedEvent, CustomerProfileUpdatedEvent, CustomerMergedEvent |
| **Integration** | Sales, Service Orders, Finance/AR |
| **Compliance** | GDPR consent, audit trail |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [customer360-domain.md](./customer360/customer360-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [customer360-application.md](./customer360/customer360-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [customer360-infrastructure.md](./customer360/customer360-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [customer360-api.md](./customer360/customer360-api.md) | Endpoints and DTOs |
| **Events & Integration** | [customer360-events.md](./customer360/customer360-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
crm-customer360/
|-- customer360-domain/
|-- customer360-application/
`-- customer360-infrastructure/
```

## Architecture Diagram

```
Customer Profile -> Contact Updates -> Consent -> Publish Events
```

## Key Business Capabilities

1. Unified customer profile and relationships.
2. Consent and communication preferences.
3. Merge and de-duplication workflow.
4. Golden record for CRM and service.

## Integration Points

- **Pipeline**: account and contact enrichment
- **Service Orders**: entitlement and SLA lookup
- **Finance/AR**: credit and billing contacts

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Profile lookup latency | < 200ms p95 | > 500ms |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- GDPR consent tracking
- Audit log for profile merges

## Related ADRs

- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
- [ADR-025: Sales and Distribution](../../adr/ADR-025-sales-distribution.md)
