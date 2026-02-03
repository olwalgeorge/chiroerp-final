# Procurement Core Application Layer

> Part of [Procurement Core](../procurement-core.md)

## Directory Structure

```
core-application/
└── src/main/kotlin/com.erp.procurement.core.application/
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
├── SubmitRequisitionCommand.kt
│   └── requesterId, lines[], costCenter
│
├── ApproveRequisitionCommand.kt
│   └── requisitionId, approverId
│
├── CreatePurchaseOrderCommand.kt
│   └── requisitionId, supplierId, lines[]
│
├── ApprovePurchaseOrderCommand.kt
│   └── poId, approverId
│
└── ChangePurchaseOrderCommand.kt
    └── poId, changeReason, updatedLines[]
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetRequisitionByIdQuery.kt
│   └── requisitionId -> RequisitionDto
│
├── GetPurchaseOrderByIdQuery.kt
│   └── poId -> PurchaseOrderDto
│
└── ListPurchaseOrdersQuery.kt
    └── status?, supplierId?, dateRange?
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── RequisitionRepository.kt
├── PurchaseOrderRepository.kt
└── ApprovalWorkflowRepository.kt
```

### Integration Ports

```
├── SupplierPort.kt                  # Supplier status
├── BudgetPort.kt                    # Budget checks
├── InventoryPort.kt                 # Expected receipts
├── FinancePort.kt                   # AP/GRIR coordination
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── RequisitionCommandHandler.kt
│   ├── PurchaseOrderCommandHandler.kt
│   └── ApprovalCommandHandler.kt
└── query/
    ├── RequisitionQueryHandler.kt
    └── PurchaseOrderQueryHandler.kt
```
