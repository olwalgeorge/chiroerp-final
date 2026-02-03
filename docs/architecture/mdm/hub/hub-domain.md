# Master Data Hub Domain Layer

> Part of [Master Data Hub](../mdm-hub.md)

## Directory Structure

```
hub-domain/
`-- src/main/kotlin/com.erp.mdm.hub.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Master Record (`model/master/`)

```
|-- master/
|   |-- MasterRecord.kt            # Aggregate Root
|   |-- MasterRecordId.kt
|   |-- MasterDomain.kt            # Customer, Vendor, Product, COA, CostCenter
|   `-- MasterStatus.kt            # Draft, Active, Retired
```

### Change Request (`model/change/`)

```
|-- change/
|   |-- ChangeRequest.kt           # Aggregate Root
|   |-- ChangeRequestId.kt
|   |-- ChangeType.kt              # Create, Update, Retire, Merge
|   `-- ChangeStatus.kt            # Submitted, Approved, Rejected
```

### Approval Workflow (`model/approval/`)

```
|-- approval/
|   |-- ApprovalWorkflow.kt        # Entity
|   |-- ApprovalStep.kt
|   `-- ApprovalStatus.kt
```

---

## Domain Events

```
events/
|-- MasterRecordPublishedEvent.kt
|-- ChangeRequestSubmittedEvent.kt
|-- ChangeRequestApprovedEvent.kt
`-- MasterRecordRetiredEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- MasterRecordNotFoundException.kt
|-- ChangeRequestNotFoundException.kt
|-- ApprovalRequiredException.kt
`-- MasterDomainInvalidException.kt
```

---

## Domain Services

```
services/
|-- MasterRecordService.kt         # Publishing and retirement
|-- ChangeRequestService.kt        # Validation and routing
`-- ApprovalService.kt             # SoD enforcement
```

---

## Key Invariants

1. **SoD Enforcement**: Requester cannot approve their own changes.
2. **Single Active Master**: One active record per master key.
3. **Approval Required**: Changes must be approved before publish.
4. **Retention**: Retired records remain for audit.
