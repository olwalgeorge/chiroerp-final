# Inventory ATP Domain Layer

> Part of [Inventory ATP & Allocation](../inventory-atp.md)

## Directory Structure

```
atp-domain/
└── src/main/kotlin/com.erp.inventory.atp.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Allocation Pool (`model/allocation/`)

```
├── allocation/
│   ├── AllocationPool.kt           # Aggregate Root
│   ├── AllocationPoolId.kt
│   ├── ChannelAllocation.kt        # Entity
│   ├── ChannelType.kt              # POS, Ecommerce, Wholesale
│   ├── AllocationStatus.kt         # Active, Frozen
│   └── SafetyStock.kt
```

---

### Reservation Rules (`model/rules/`)

```
├── rules/
│   ├── ReservationRule.kt          # Aggregate Root
│   ├── PriorityRule.kt
│   ├── BackorderRule.kt
│   └── PromiseDateRule.kt
```

---

## Domain Events

```
events/
├── AvailabilityCalculatedEvent.kt
├── AllocationCommittedEvent.kt
├── ReservationDeniedEvent.kt
└── AllocationReleasedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── AllocationExceededException.kt
├── SafetyStockViolationException.kt
└── InvalidPriorityRuleException.kt
```

---

## Domain Services

```
services/
├── AtpEngine.kt                    # Real-time availability
├── AllocationPolicyService.kt      # Channel allocation rules
└── ReservationPriorityService.kt   # SLA-based prioritization
```

---

## Key Invariants

1. **Safety Stock**: Allocations cannot consume safety stock below threshold.
2. **Channel Limits**: Channel allocations cannot exceed pool limits.
3. **Priority Order**: Higher SLA priority cannot be overridden by lower.
