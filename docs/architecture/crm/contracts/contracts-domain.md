# CRM Contracts Domain Layer

> Part of [CRM Contracts](../crm-contracts.md)

## Directory Structure

```
contracts-domain/
`-- src/main/kotlin/com.erp.crm.contracts.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Service Contract (`model/contract/`)

```
|-- contract/
|   |-- ServiceContract.kt          # Aggregate Root
|   |-- ContractId.kt
|   |-- ContractStatus.kt           # Draft, Active, Suspended, Expired
|   |-- CoveragePlan.kt             # Value Object
|   `-- RenewalTerms.kt
```

### Entitlement (`model/entitlement/`)

```
|-- entitlement/
|   |-- Entitlement.kt              # Aggregate Root
|   |-- EntitlementId.kt
|   `-- EntitlementBalance.kt       # Value Object
```

### Renewal (`model/renewal/`)

```
|-- renewal/
|   |-- RenewalQuote.kt             # Aggregate Root
|   |-- RenewalId.kt
|   `-- RenewalStatus.kt            # Proposed, Accepted, Rejected
```

---

## Domain Events

```
events/
|-- ContractCreatedEvent.kt
|-- ContractActivatedEvent.kt
|-- ContractRenewedEvent.kt
|-- ContractExpiredEvent.kt
`-- EntitlementConsumedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- ContractNotActiveException.kt
|-- EntitlementExceededException.kt
`-- RenewalWindowClosedException.kt
```

---

## Domain Services

```
services/
|-- EntitlementPolicyService.kt     # Usage rules
|-- RenewalPricingService.kt        # Renewal quote logic
`-- CoverageEligibilityService.kt   # Coverage checks
```

---

## Key Invariants

1. **Active Contract Required**: Entitlements apply only to active contracts.
2. **No Negative Balance**: Entitlement balance cannot go below zero.
3. **Single Active Renewal**: Only one renewal quote can be active at a time.
4. **Expiry Enforcement**: Expired contracts cannot be billed.
