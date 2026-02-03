# Sales Pricing Application Layer

> Part of [Sales Pricing & Promotions](../sales-pricing.md)

## Directory Structure

```
pricing-application/
└── src/main/kotlin/com.erp.sales.pricing.application/
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
├── CreatePriceListCommand.kt
│   └── name, currency, items[]
│
├── ActivatePriceListCommand.kt
│   └── priceListId
│
├── RetirePriceListCommand.kt
│   └── priceListId, reason
│
├── CreatePromotionCommand.kt
│   └── code, type, rules[], effectiveDates
│
├── ActivatePromotionCommand.kt
│   └── promotionId
│
├── RequestPriceOverrideCommand.kt
│   └── orderId, lineId, requestedPrice, reason
│
└── ApprovePriceOverrideCommand.kt
    └── overrideId, approverId
```

---

## Queries (Read Operations)

```
port/input/query/
├── CalculatePriceQuery.kt
│   └── customerId, channel, items[], priceListId?, promotions[]
│
├── GetPriceListByIdQuery.kt
│   └── priceListId -> PriceListDto
│
├── GetPromotionByCodeQuery.kt
│   └── code -> PromotionDto
│
└── GetActivePriceListsQuery.kt
    └── channel, customerSegment -> List<PriceListDto>
```

---

## Output Ports (Driven Ports)

```
port/output/
├── PriceListRepository.kt
├── PromotionRepository.kt
├── OverrideRepository.kt
├── CustomerSegmentPort.kt
├── ExchangeRatePort.kt
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── PriceListCommandHandler.kt
│   ├── PromotionCommandHandler.kt
│   └── OverrideCommandHandler.kt
└── query/
    ├── PricingQueryHandler.kt
    └── PriceListQueryHandler.kt
```
