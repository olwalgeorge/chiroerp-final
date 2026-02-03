# Sales Core Application Layer

> Part of [Sales Core](../sales-core.md)

## Directory Structure

```
core-application/
└── src/main/kotlin/com.erp.sales.core.application/
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

### Order Commands

```
port/input/command/
├── CreateQuoteCommand.kt
│   └── customerId, currency, lines[], expiryDate
│
├── ConvertQuoteToOrderCommand.kt
│   └── quoteId
│
├── CreateSalesOrderCommand.kt
│   └── customerId, channel, lines[], pricingContext
│
├── SubmitSalesOrderCommand.kt
│   └── orderId
│
├── ApproveSalesOrderCommand.kt
│   └── orderId, approverId
│
├── AllocateSalesOrderCommand.kt
│   └── orderId
│
├── PlaceOrderHoldCommand.kt
│   └── orderId, reason
│
├── ReleaseOrderHoldCommand.kt
│   └── orderId, reason
│
└── CancelSalesOrderCommand.kt
    └── orderId, reason
```

### Fulfillment Completion Commands

```
├── MarkOrderFulfilledCommand.kt
│   └── orderId, shipmentId
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetSalesOrderByIdQuery.kt
│   └── orderId -> SalesOrderDto
│
├── GetOrdersByCustomerQuery.kt
│   └── customerId -> List<SalesOrderDto>
│
├── GetOrderStatusQuery.kt
│   └── orderId -> OrderStatusDto
│
├── GetBackordersQuery.kt
│   └── customerId? -> List<BackorderDto>
│
└── GetQuoteByIdQuery.kt
    └── quoteId -> QuoteDto
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── SalesOrderRepository.kt
├── QuoteRepository.kt
└── ApprovalRepository.kt
```

### Integration Ports

```
├── PricingPort.kt                  # Price calculation + promotions
├── InventoryReservationPort.kt     # Reserve/allocate stock
├── CreditManagementPort.kt         # Credit checks and holds
├── TaxEnginePort.kt                # Tax calculation
├── RevenueRecognitionPort.kt       # Contract linkage
├── CustomerAccountPort.kt          # Customer data
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── QuoteCommandHandler.kt
│   ├── SalesOrderCommandHandler.kt
│   ├── AllocationCommandHandler.kt
│   └── ApprovalCommandHandler.kt
└── query/
    ├── SalesOrderQueryHandler.kt
    ├── QuoteQueryHandler.kt
    └── BackorderQueryHandler.kt
```
