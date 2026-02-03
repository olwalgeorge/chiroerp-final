# Procurement Core Infrastructure Layer

> Part of [Procurement Core](../procurement-core.md)

## Directory Structure

```
core-infrastructure/
└── src/main/kotlin/com.erp.procurement.core.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       └── messaging/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

### Requisition Resource

```
adapter/input/rest/
├── RequisitionResource.kt
│   ├── POST /api/v1/procurement/requisitions -> submitRequisition()
│   └── GET  /api/v1/procurement/requisitions/{id} -> getRequisition()
```

### Purchase Order Resource

```
├── PurchaseOrderResource.kt
│   ├── POST /api/v1/procurement/purchase-orders -> createPO()
│   ├── POST /api/v1/procurement/purchase-orders/{id}/approve -> approvePO()
│   └── PUT  /api/v1/procurement/purchase-orders/{id} -> changePO()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── SubmitRequisitionRequest.kt
│   ├── requesterId: String
│   ├── costCenter: String
│   └── lines: List<RequisitionLineRequest>
│
└── CreatePurchaseOrderRequest.kt
    ├── supplierId: String
    └── lines: List<POLineRequest>
```

### Response DTOs

```
dto/response/
├── RequisitionDto.kt
│   ├── id, status, requesterId
│   └── lines: List<RequisitionLineDto>
│
└── PurchaseOrderDto.kt
    ├── id, status, supplierId
    └── lines: List<POLineDto>
```

---

## Event Consumers

```
adapter/input/event/
├── SupplierStatusEventConsumer.kt
│   └── Consumes: VendorUpdatedEvent -> Update supplier status
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── RequisitionJpaAdapter.kt      -> implements RequisitionRepository
├── PurchaseOrderJpaAdapter.kt    -> implements PurchaseOrderRepository
└── ApprovalWorkflowJpaAdapter.kt -> implements ApprovalWorkflowRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── RequisitionEntity.kt
├── PurchaseOrderEntity.kt
└── ApprovalWorkflowEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── SupplierAdapter.kt
├── BudgetAdapter.kt
├── InventoryAdapter.kt
└── FinanceAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── ProcurementEventPublisher.kt
│   └── schema/
│       ├── PurchaseOrderApprovedSchema.avro
│       └── RequisitionSubmittedSchema.avro
└── outbox/
    └── ProcurementOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── ProcurementDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_procurement_schema.sql
    ├── V2__create_requisition_tables.sql
    ├── V3__create_purchase_order_tables.sql
    └── V4__create_approval_tables.sql
```
