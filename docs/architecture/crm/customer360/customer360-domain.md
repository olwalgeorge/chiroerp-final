# CRM Customer 360 Domain Layer

> Part of [CRM Customer 360](../crm-customer360.md)

## Directory Structure

```
customer360-domain/
`-- src/main/kotlin/com.erp.crm.customer360.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Customer Profile (`model/customer/`)

```
|-- customer/
|   |-- CustomerProfile.kt          # Aggregate Root
|   |-- CustomerId.kt
|   |-- CustomerStatus.kt           # Active, Inactive, OnHold
|   |-- Relationship.kt             # Entity
|   |-- Segment.kt
|   `-- Preference.kt               # Value Object
```

### Contact (`model/contact/`)

```
|-- contact/
|   |-- Contact.kt                  # Entity
|   |-- ContactId.kt
|   |-- ContactRole.kt              # Billing, Service, Sales
|   `-- CommunicationPreference.kt
```

### Consent (`model/consent/`)

```
|-- consent/
|   |-- ConsentRecord.kt            # Aggregate Root
|   |-- ConsentType.kt              # Email, SMS, Call
|   `-- ConsentStatus.kt            # Granted, Revoked
```

---

## Domain Events

```
events/
|-- CustomerProfileCreatedEvent.kt
|-- CustomerProfileUpdatedEvent.kt
|-- CustomerMergedEvent.kt
`-- ConsentUpdatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- CustomerNotFoundException.kt
|-- ConsentViolationException.kt
`-- DuplicateCustomerException.kt
```

---

## Domain Services

```
services/
|-- CustomerMergeService.kt         # De-duplication
`-- ConsentPolicyService.kt         # GDPR enforcement
```

---

## Key Invariants

1. **Single Golden Record**: One active profile per customer.
2. **Consent Required**: Communications must respect consent.
3. **Merge Audit**: All merges must be reversible and logged.
