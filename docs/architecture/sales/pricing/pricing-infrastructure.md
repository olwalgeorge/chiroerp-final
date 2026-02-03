# Sales Pricing Infrastructure Layer

> Part of [Sales Pricing & Promotions](../sales-pricing.md)

## Directory Structure

```
pricing-infrastructure/
└── src/main/kotlin/com.erp.sales.pricing.infrastructure/
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

### Pricing Resource

```
adapter/input/rest/
├── PricingResource.kt
│   ├── POST   /api/v1/sales/pricing/calculate  -> calculatePrice()
```

### Price List Resource

```
├── PriceListResource.kt
│   ├── POST   /api/v1/sales/price-lists        -> createPriceList()
│   ├── POST   /api/v1/sales/price-lists/{id}/activate -> activatePriceList()
│   └── GET    /api/v1/sales/price-lists/{id}   -> getPriceList()
```

### Promotion Resource

```
├── PromotionResource.kt
│   ├── POST   /api/v1/sales/promotions         -> createPromotion()
│   ├── POST   /api/v1/sales/promotions/{id}/activate -> activatePromotion()
│   └── GET    /api/v1/sales/promotions/{code}  -> getPromotion()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CalculatePriceRequest.kt
│   ├── customerId: String
│   ├── channel: String
│   ├── currency: String
│   ├── items: List<PricedItemRequest>
│   └── promotionCodes: List<String>?
│
├── CreatePriceListRequest.kt
│   ├── name: String
│   ├── currency: String
│   └── items: List<PriceListItemRequest>
│
└── CreatePromotionRequest.kt
    ├── code: String
    ├── type: String
    ├── rules: List<DiscountRuleRequest>
    └── effectiveDates: DateRangeRequest
```

### Response DTOs

```
dto/response/
├── CalculatedPriceDto.kt
│   ├── totalAmount, currency, breakdown
│
├── PriceListDto.kt
│   ├── id, name, status, currency, items
│
└── PromotionDto.kt
    ├── id, code, status, type, rules
```

---

## Event Consumers

```
adapter/input/event/
├── CustomerTierUpdatedEventConsumer.kt
│   └── Consumes: CustomerTierUpdatedEvent -> Refresh segments
│
└── ExchangeRateUpdatedEventConsumer.kt
    └── Consumes: ExchangeRateUpdatedEvent -> Update FX cache
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters

```
adapter/output/persistence/jpa/
├── PriceListJpaAdapter.kt          -> implements PriceListRepository
├── PromotionJpaAdapter.kt          -> implements PromotionRepository
└── OverrideJpaAdapter.kt           -> implements OverrideRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── PriceListEntity.kt
├── PriceListItemEntity.kt
├── PromotionEntity.kt
└── OverrideEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── CustomerSegmentAdapter.kt
└── ExchangeRateAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── PricingEventPublisher.kt
│   └── schema/
│       ├── PriceCalculatedSchema.avro
│       └── PriceOverrideApprovedSchema.avro
└── outbox/
    └── PricingOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── PricingDependencyInjection.kt
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
