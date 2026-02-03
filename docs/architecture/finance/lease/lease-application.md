# Lease Application Layer

> Part of [Finance - Lease Accounting](../finance-lease.md)

## Directory Structure

```
lease-application/
└── src/main/kotlin/com.erp.finance.lease.application/
    ├── port/
    │   ├── input/
    │   │   ├── command/
    │   │   └── query/
    │   └── output/
    └── service/
        ├── command/
        └── query/
```

---

## Commands (Write Operations)

```
port/input/command/
├── CreateLeaseContractCommand.kt
│   └── vendorId, term, paymentSchedule
│
├── ActivateLeaseCommand.kt
│   └── leaseId
│
├── PostLeaseAmortizationCommand.kt
│   └── leaseId, period
│
└── RemeasureLeaseCommand.kt
    └── leaseId, newTerms
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetLeaseByIdQuery.kt
│   └── leaseId -> LeaseDto
│
└── GetLeaseScheduleQuery.kt
    └── leaseId -> LeaseScheduleDto
```

---

## Output Ports (Driven Ports)

```
port/output/
├── LeaseContractRepository.kt
├── LeaseScheduleRepository.kt
├── GeneralLedgerPort.kt
└── EventPublisherPort.kt
```

---

## Command Handlers

```
service/command/
├── LeaseCommandHandler.kt
│   ├── handle(CreateLeaseContractCommand): LeaseContractId
│   ├── handle(ActivateLeaseCommand): void
│   └── handle(RemeasureLeaseCommand): void
│
└── AmortizationCommandHandler.kt
    └── handle(PostLeaseAmortizationCommand): void
```
