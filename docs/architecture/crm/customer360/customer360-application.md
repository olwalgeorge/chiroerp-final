# CRM Customer 360 Application Layer

> Part of [CRM Customer 360](../crm-customer360.md)

## Directory Structure

```
customer360-application/
`-- src/main/kotlin/com.erp.crm.customer360.application/
    |-- port/
    |   |-- input/
    |   |   |-- command/
    |   |   `-- query/
    |   `-- output/
    `-- service/
        |-- command/
        `-- query/
```

---

## Commands (Write Operations)

```
port/input/command/
|-- CreateCustomerProfileCommand.kt
|   `-- name, status, contacts[], preferences
|-- UpdateCustomerProfileCommand.kt
|   `-- customerId, changes
|-- MergeCustomerProfilesCommand.kt
|   `-- sourceId, targetId
`-- UpdateConsentCommand.kt
    `-- customerId, consentType, status
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetCustomerProfileByIdQuery.kt
|   `-- customerId -> CustomerProfileDto
|-- SearchCustomersQuery.kt
|   `-- name, email, phone -> List<CustomerProfileDto>
`-- GetConsentHistoryQuery.kt
    `-- customerId -> List<ConsentDto>
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- CustomerRepository.kt
|-- ConsentRepository.kt
|-- MdgPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- CustomerProfileCommandHandler.kt
|   `-- ConsentCommandHandler.kt
`-- query/
    |-- CustomerProfileQueryHandler.kt
    `-- ConsentQueryHandler.kt
```
