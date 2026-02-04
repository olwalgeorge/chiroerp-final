# Sales Pricing & Promotions - ADR-025

> **Bounded Context:** `sales-pricing`
> **Port:** `9202`
> **Database:** `chiroerp_sales_pricing`
> **Kafka Consumer Group:** `sales-pricing-cg`

## Overview

Pricing & Promotions owns **price lists, discounts, promotions, and override approvals**. It provides deterministic price calculation for Sales Core and emits pricing audit events.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Price lists, promotions, discounts, overrides |
| **Aggregates** | PriceList, PriceCondition, Promotion, DiscountRule |
| **Key Events** | PriceCalculatedEvent, PromotionAppliedEvent, PriceOverrideApprovedEvent |
| **Integration** | Provides pricing to Sales Core |
| **Compliance** | Override approvals, audit trail |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [pricing-domain.md](./pricing/pricing-domain.md) | Aggregates, entities, value objects, domain events, exceptions, services |
| **Application Layer** | [pricing-application.md](./pricing/pricing-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [pricing-infrastructure.md](./pricing/pricing-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [pricing-api.md](./pricing/pricing-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [pricing-events.md](./pricing/pricing-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
sales-pricing/
├── pricing-domain/
├── pricing-application/
└── pricing-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SALES-PRICING SERVICE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ CalculatePrice   │  │ ApplyPromotion   │  │ ApproveOverride   │  │    │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ PriceRepo │ PromotionRepo │ EventPublisherPort                   │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │    │
│  │  │ PriceList    │  │ Promotion    │  │ DiscountRule │  │ Override  │ │    │
│  │  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │ Aggregate │ │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘ │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Pricing Rules       │  │ Domain Services                      │    │    │
│  │  │ Tax-inclusive prices│  │ Price calculation & approvals         │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Cache      │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Redis)    │   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Price Lists and Conditions
- Multi-currency price lists
- Customer and channel-specific pricing

### 2. Promotions and Discounts
- Stackable promotions with rules
- Time-bound campaigns

### 3. Override Approvals
- Threshold-based approvals
- Audit trail for exceptions

## Integration Points

```
┌──────────────┐   PriceRequest   ┌────────────────┐
│ sales-core   │ ───────────────> │ sales-pricing  │
└──────────────┘                  └────────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Price calculation | < 200ms p95 | > 400ms |
| Promotion evaluation | < 300ms p95 | > 600ms |
| API availability | 99.95% | < 99.90% |

## Compliance & Audit

- Approval required for pricing overrides
- Audit trail for price changes and promotions

## Related ADRs

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md)
