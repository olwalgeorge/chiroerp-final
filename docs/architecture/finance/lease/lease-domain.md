# Lease Domain Layer

> Part of [Finance - Lease Accounting](../finance-lease.md)

## Directory Structure

```
lease-domain/
└── src/main/kotlin/com.erp.finance.lease.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Lease Contract (`model/contract/`)

```
├── contract/
│   ├── LeaseContract.kt            # Aggregate Root
│   ├── LeaseContractId.kt
│   ├── LeaseType.kt                # Operating, Finance
│   ├── LeaseStatus.kt              # Draft, Active, Terminated
│   ├── LeaseTerm.kt
│   └── PaymentSchedule.kt
```

### ROU Asset (`model/asset/`)

```
├── asset/
│   ├── ROUAsset.kt                 # Aggregate Root
│   ├── ROUAssetId.kt
│   ├── AmortizationSchedule.kt
│   └── ROUStatus.kt
```

### Lease Liability (`model/liability/`)

```
├── liability/
│   ├── LeaseLiability.kt           # Aggregate Root
│   ├── LeaseLiabilityId.kt
│   └── InterestSchedule.kt
```

---

## Domain Events

```
events/
├── LeaseActivatedEvent.kt
├── LeaseAmortizationPostedEvent.kt
└── LeaseRemeasuredEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── LeaseNotFoundException.kt
├── InvalidLeaseTermException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── AmortizationService.kt
└── LeaseRemeasurementService.kt
```

---

## Key Invariants

1. **Active Lease**: Only active leases amortize.
2. **Schedule Balance**: Liability roll-forward must reconcile.
3. **Open Period**: Posting only in open periods.
