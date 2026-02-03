# Sales Core Infrastructure Layer

> Part of [Sales Core](../sales-core.md)

## Directory Structure

```
core-infrastructure/
└── src/main/kotlin/com.erp.sales.core.infrastructure/
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

### Sales Order Resource

```
adapter/input/rest/
├── SalesOrderResource.kt
│   ├── POST   /api/v1/sales/orders                 -> createOrder()
│   ├── GET    /api/v1/sales/orders/{id}            -> getOrder()
│   ├── POST   /api/v1/sales/orders/{id}/submit     -> submitOrder()
│   ├── POST   /api/v1/sales/orders/{id}/approve    -> approveOrder()
│   ├── POST   /api/v1/sales/orders/{id}/allocate   -> allocateOrder()
│   └── POST   /api/v1/sales/orders/{id}/cancel     -> cancelOrder()
```

### Quote Resource

```
├── QuoteResource.kt
│   ├── POST   /api/v1/sales/quotes                 -> createQuote()
│   ├── GET    /api/v1/sales/quotes/{id}            -> getQuote()
│   └── POST   /api/v1/sales/quotes/{id}/convert    -> convertQuote()
```

### Holds & Approvals Resource

```
├── OrderHoldResource.kt
│   ├── POST   /api/v1/sales/orders/{id}/hold       -> placeHold()
│   └── POST   /api/v1/sales/orders/{id}/release    -> releaseHold()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateSalesOrderRequest.kt
│   ├── customerId: String
│   ├── channel: String
│   ├── currency: String
│   ├── lines: List<OrderLineRequest>
│   └── pricingContext: PricingContextRequest
│
├── CreateQuoteRequest.kt
│   ├── customerId: String
│   ├── currency: String
│   ├── lines: List<QuoteLineRequest>
│   └── expiryDate: String
│
└── ApproveOrderRequest.kt
    ├── approverId: String
    └── reason: String?
```

### Response DTOs

```
dto/response/
├── SalesOrderDto.kt
│   ├── id, status, customerId, totalAmount, currency
│   └── lines: List<OrderLineDto>
│
├── QuoteDto.kt
│   ├── id, status, customerId, totalAmount, currency
│
└── OrderStatusDto.kt
    ├── id, status, holds, allocationStatus
```

---

## Event Consumers

```
adapter/input/event/
├── ShipmentConfirmedEventConsumer.kt
│   └── Consumes: ShipmentConfirmedEvent -> Mark order fulfilled
│
├── CreditLimitExceededEventConsumer.kt
│   └── Consumes: CreditLimitExceededEvent -> Place order hold
│
├── CustomerBlockedEventConsumer.kt
│   └── Consumes: CustomerBlockedEvent -> Block orders
│
├── CreditHoldReleasedEventConsumer.kt
│   └── Consumes: CreditReleasedEvent -> Release order hold
│
└── InventoryReservationCreatedConsumer.kt
    └── Consumes: ReservationCreatedEvent -> Mark allocation
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── SalesOrderJpaAdapter.kt         -> implements SalesOrderRepository
├── QuoteJpaAdapter.kt              -> implements QuoteRepository
└── ApprovalJpaAdapter.kt           -> implements ApprovalRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── SalesOrderEntity.kt
├── OrderLineEntity.kt
├── QuoteEntity.kt
└── ApprovalEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── PricingClientAdapter.kt
├── InventoryReservationAdapter.kt
├── CreditManagementAdapter.kt
├── TaxEngineAdapter.kt
├── RevenueRecognitionAdapter.kt
└── CustomerAccountAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── SalesEventPublisher.kt
│   └── schema/
│       ├── SalesOrderCreatedSchema.avro
│       └── SalesOrderFulfilledSchema.avro
└── outbox/
    └── SalesOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── SalesDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
```
