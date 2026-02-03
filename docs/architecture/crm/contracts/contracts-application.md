# CRM Contracts Application Layer

> Part of [CRM Contracts](../crm-contracts.md)

## Directory Structure

```
contracts-application/
`-- src/main/kotlin/com.erp.crm.contracts.application/
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
|-- CreateContractCommand.kt
|   `-- customerId, planId, startDate, endDate
|-- ActivateContractCommand.kt
|   `-- contractId
|-- RecordEntitlementUsageCommand.kt
|   `-- contractId, entitlementId, units
|-- RenewContractCommand.kt
|   `-- contractId, renewalTerms
`-- ExpireContractCommand.kt
    `-- contractId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetContractByIdQuery.kt
|   `-- contractId -> ContractDto
|-- ListContractsByCustomerQuery.kt
|   `-- customerId -> List<ContractDto>
`-- GetEntitlementBalanceQuery.kt
    `-- contractId -> EntitlementBalanceDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- ContractRepository.kt
|-- EntitlementRepository.kt
|-- PricingPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- ContractCommandHandler.kt
|   `-- EntitlementCommandHandler.kt
`-- query/
    |-- ContractQueryHandler.kt
    `-- EntitlementQueryHandler.kt
```
