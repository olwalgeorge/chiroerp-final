# Procurement Core Domain Layer

> Part of [Procurement Core](../procurement-core.md)

## Directory Structure

```
core-domain/
└── src/main/kotlin/com.erp.procurement.core.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Requisition (`model/requisition/`)

```
├── requisition/
│   ├── Requisition.kt              # Aggregate Root
│   ├── RequisitionId.kt
│   ├── RequisitionLine.kt           # Entity
│   ├── RequisitionStatus.kt         # Draft, Submitted, Approved, Rejected
│   └── BudgetCheck.kt
```

---

### Purchase Order (`model/po/`)

```
├── po/
│   ├── PurchaseOrder.kt             # Aggregate Root
│   ├── PurchaseOrderId.kt
│   ├── POLine.kt                    # Entity
│   ├── POStatus.kt                  # Draft, Approved, Issued, Closed, Cancelled
│   ├── POType.kt                    # Standard, Blanket, Contract
│   └── POChange.kt                  # Entity (revision)
```

---

### Approval Workflow (`model/approval/`)

```
├── approval/
│   ├── ApprovalWorkflow.kt          # Aggregate Root
│   ├── ApprovalStep.kt              # Entity
│   ├── ApprovalStatus.kt            # Pending, Approved, Rejected
│   └── ApprovalThreshold.kt
```

---

## Domain Events

```
events/
├── RequisitionSubmittedEvent.kt
├── RequisitionApprovedEvent.kt
├── PurchaseOrderApprovedEvent.kt
├── PurchaseOrderIssuedEvent.kt
├── PurchaseOrderCancelledEvent.kt
└── PurchaseOrderChangedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── BudgetExceededException.kt
├── ApprovalRequiredException.kt
├── InvalidPOStatusException.kt
└── SupplierBlockedException.kt
```

---

## Domain Services

```
services/
├── ApprovalRoutingService.kt        # SoD and threshold logic
├── BudgetValidationService.kt       # Budget checks
└── POChangeControlService.kt        # Revision rules
```

---

## Key Invariants

1. **Approval Required**: POs cannot be issued without approvals.
2. **Budget Control**: Requisitions must pass budget checks.
3. **Change Control**: PO changes create revisions and audit entries.
4. **Supplier Status**: Blocked suppliers cannot receive new POs.
