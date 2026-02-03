# Finance Tax Engine - ADR-030

> **Bounded Context:** `finance-tax`  
> **Port:** `8087`  
> **Database:** `chiroerp_finance_tax`  
> **Kafka Consumer Group:** `finance-tax-cg`

## Overview

The Tax Engine subdomain centralizes tax calculation and reporting for sales tax, VAT/GST, and withholding. It provides tax code management, jurisdiction rules, calculation services, and tax posting instructions to GL.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Tax codes, jurisdictions, tax rules, calculations, returns |
| **Aggregates** | TaxCode, TaxJurisdiction, TaxRate, TaxRule, TaxCalculation |
| **Key Events** | TaxCalculatedEvent, TaxRateUpdatedEvent, TaxReturnFiledEvent |
| **GL Integration** | Tax liability postings and adjustments |
| **Compliance** | VAT/GST, sales tax, withholding, audit retention |

## Bounded Context

```
finance-tax/
├── tax-domain/
├── tax-application/
└── tax-infrastructure/
```

---

## Domain Layer (`tax-domain/`)

### Aggregates & Entities

```
src/main/kotlin/com.erp.finance.tax.domain/
├── model/
│   ├── code/
│   │   ├── TaxCode.kt               # Aggregate Root
│   │   ├── TaxCodeId.kt
│   │   └── TaxCodeStatus.kt
│   ├── jurisdiction/
│   │   ├── TaxJurisdiction.kt       # Aggregate Root
│   │   ├── TaxJurisdictionId.kt
│   │   └── JurisdictionType.kt
│   ├── rate/
│   │   ├── TaxRate.kt               # Aggregate Root
│   │   ├── TaxRateId.kt
│   │   └── EffectivePeriod.kt
│   └── calculation/
│       ├── TaxCalculation.kt        # Aggregate Root
│       ├── TaxCalculationId.kt
│       └── TaxLine.kt               # Entity
```

### Domain Events

```
├── TaxCalculatedEvent.kt
├── TaxRateUpdatedEvent.kt
└── TaxReturnFiledEvent.kt
```

### Domain Exceptions

```
├── TaxCodeNotFoundException.kt
├── JurisdictionNotFoundException.kt
├── InvalidTaxRateException.kt
└── PeriodClosedException.kt
```

### Domain Services

```
└── TaxCalculationService.kt         # Rate selection + rounding
```

---

## Application Layer (`tax-application/`)

### Commands

```
├── CalculateTaxCommand.kt
├── UpdateTaxRateCommand.kt
├── CreateTaxCodeCommand.kt
└── FileTaxReturnCommand.kt
```

### Queries

```
├── GetTaxCodeQuery.kt
├── GetTaxRatesQuery.kt
└── GetTaxSummaryQuery.kt
```

### Output Ports

```
├── TaxCodeRepository.kt
├── TaxRateRepository.kt
├── TaxCalculationRepository.kt
└── GeneralLedgerPort.kt
```

---

## Infrastructure Layer (`tax-infrastructure/`)

### REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/tax/calculate` | Calculate tax for a transaction |
| GET | `/api/v1/tax/codes` | List tax codes |
| POST | `/api/v1/tax/rates` | Create or update tax rate |
| POST | `/api/v1/tax/returns` | File tax return |

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Tax calculation validation failed",
  "timestamp": "2026-02-10T12:05:00Z",
  "requestId": "req-tax-001",
  "details": {
    "violations": [
      {
        "field": "jurisdictionCode",
        "constraint": "Unknown tax jurisdiction",
        "rejectedValue": "US-XX"
      }
    ]
  }
}
```

### Messaging
- Publishes `TaxCalculatedEvent` and `TaxRateUpdatedEvent`
- Consumes `FinancialPeriodClosedEvent` to lock tax periods

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.tax.tax-calculation.calculated` | Finance Tax | Sales, Finance/GL, Analytics | 6 |
| `finance.tax.tax-rate.updated` | Finance Tax | Sales, Pricing, Reporting | 6 |
| `finance.tax.tax-return.filed` | Finance Tax | Finance/GL, Compliance | 6 |

---

## GL Integration (Journal Entries)

### Tax Posting
```
Debit:  Tax Expense (6105)            $100
Credit: Tax Payable (2100)             $100
```

---

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Tax Calculation Latency | < 50ms p95 | > 100ms |
| Rate Lookup Latency | < 20ms p95 | > 50ms |
| API Availability | 99.95% | < 99.90% |

## Compliance & Audit

- VAT/GST and sales tax retention requirements
- Withholding compliance reporting
- Audit trail for rate changes and filings

## Related ADRs

- [ADR-030: Tax Engine](../../adr/ADR-030-tax-engine-compliance.md) - Tax domain decisions
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
