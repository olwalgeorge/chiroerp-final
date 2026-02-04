# Finance Treasury - ADR-026

> **Bounded Context:** `finance-treasury`
> **Port:** `8089`
> **Database:** `chiroerp_finance_treasury`
> **Kafka Consumer Group:** `finance-treasury-cg`

## Overview

Treasury manages cash positions, bank accounts, bank statement imports, and reconciliation. It provides cash visibility, supports liquidity planning, and posts reconciliation adjustments to GL.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Bank accounts, cash position, reconciliation, cash forecasts |
| **Aggregates** | BankAccount, BankStatement, CashPosition, Reconciliation |
| **Key Events** | BankStatementImportedEvent, CashPositionUpdatedEvent, BankReconciledEvent |
| **GL Integration** | Cash account reconciliations and adjustments |
| **Compliance** | SOX 404, bank reconciliation controls |

## Bounded Context

```
finance-treasury/
├── treasury-domain/
├── treasury-application/
└── treasury-infrastructure/
```

---

## Domain Layer (`treasury-domain/`)

### Aggregates & Entities

```
src/main/kotlin/com.erp.finance.treasury.domain/
├── model/
│   ├── bank/
│   │   ├── BankAccount.kt             # Aggregate Root
│   │   ├── BankAccountId.kt
│   │   └── BankAccountStatus.kt
│   ├── statement/
│   │   ├── BankStatement.kt           # Aggregate Root
│   │   ├── BankStatementId.kt
│   │   └── StatementLine.kt
│   └── reconciliation/
│       ├── BankReconciliation.kt      # Aggregate Root
│       ├── ReconciliationId.kt
│       └── ReconciliationStatus.kt
```

### Domain Events

```
├── BankStatementImportedEvent.kt
├── CashPositionUpdatedEvent.kt
└── BankReconciledEvent.kt
```

### Domain Services

```
└── ReconciliationService.kt
```

---

## Application Layer (`treasury-application/`)

### Commands

```
├── ImportBankStatementCommand.kt
├── ReconcileBankStatementCommand.kt
└── UpdateCashPositionCommand.kt
```

### Queries

```
├── GetCashPositionQuery.kt
└── GetBankReconciliationQuery.kt
```

### Output Ports

```
├── BankAccountRepository.kt
├── BankStatementRepository.kt
├── ReconciliationRepository.kt
└── GeneralLedgerPort.kt
```

---

## Infrastructure Layer (`treasury-infrastructure/`)

### REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/treasury/bank-statements` | Import bank statement |
| POST | `/api/v1/treasury/reconciliations/{id}/complete` | Complete reconciliation |
| GET | `/api/v1/treasury/cash-position` | Current cash position |

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Bank statement import validation failed",
  "timestamp": "2026-02-10T12:00:00Z",
  "requestId": "req-treasury-001",
  "details": {
    "violations": [
      {
        "field": "statementDate",
        "constraint": "Date cannot be in the future",
        "rejectedValue": "2026-12-01"
      }
    ]
  }
}
```

### Messaging
- Consumes `PaymentSentEvent` (AP) and `PaymentReceivedEvent` (AR)
- Publishes `BankStatementImportedEvent` and `BankReconciledEvent`

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.treasury.bank-statement.imported` | Finance Treasury | Finance/GL, Analytics | 6 |
| `finance.treasury.cash-position.updated` | Finance Treasury | Analytics, Reporting | 6 |
| `finance.treasury.bank-reconciliation.reconciled` | Finance Treasury | Finance/GL, Audit | 6 |

---

## GL Integration (Journal Entries)

### Bank Reconciliation Adjustment
```
Debit:  Bank Fees Expense (6200)      $50
Credit: Cash/Bank (1000)               $50
```

---

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Bank Statement Import | < 2 min | > 5 min |
| Reconciliation Latency | < 500ms p95 | > 1s |
| API Availability | 99.95% | < 99.90% |

## Compliance & Audit

- Reconciliation controls and approval workflows
- Immutable audit trail for statement imports

## Related ADRs

- [ADR-026: Treasury](../../adr/ADR-026-treasury-cash-management.md) - Treasury domain decisions
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
