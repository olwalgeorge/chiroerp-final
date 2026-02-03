# Sales Pricing Domain Layer

> Part of [Sales Pricing & Promotions](../sales-pricing.md)

## Directory Structure

```
pricing-domain/
└── src/main/kotlin/com.erp.sales.pricing.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Price List (`model/pricelist/`)

```
├── pricelist/
│   ├── PriceList.kt                # Aggregate Root
│   ├── PriceListId.kt
│   ├── PriceListStatus.kt           # Draft, Active, Retired
│   ├── PriceListItem.kt             # Entity
│   ├── PricingTier.kt               # Value Object
│   ├── EffectiveDateRange.kt        # Value Object
│   └── CurrencyCode.kt
```

**PriceList Aggregate**:
```kotlin
class PriceList private constructor(
    val id: PriceListId,
    val name: String,
    var status: PriceListStatus,
    val currency: CurrencyCode,
    val items: MutableList<PriceListItem>
) {
    fun activate(): PriceListActivatedEvent
    fun retire(reason: String): PriceListRetiredEvent
    fun addItem(item: PriceListItem): PriceListItemAddedEvent
}
```

---

### Promotion & Discount (`model/promo/`)

```
├── promo/
│   ├── Promotion.kt                # Aggregate Root
│   ├── PromotionId.kt
│   ├── PromotionType.kt             # Percentage, FixedAmount, BOGO
│   ├── PromotionStatus.kt           # Scheduled, Active, Expired
│   ├── DiscountRule.kt              # Entity
│   ├── CustomerSegment.kt           # Value Object
│   └── ChannelScope.kt              # Value Object
```

---

### Overrides (`model/override/`)

```
├── override/
│   ├── PriceOverride.kt             # Aggregate Root
│   ├── OverrideId.kt
│   ├── OverrideStatus.kt            # Requested, Approved, Rejected
│   └── ApprovalThreshold.kt         # Value Object
```

---

## Domain Events

```
events/
├── PriceCalculatedEvent.kt
├── PromotionAppliedEvent.kt
├── PriceOverrideRequestedEvent.kt
├── PriceOverrideApprovedEvent.kt
├── PriceListActivatedEvent.kt
└── PriceListRetiredEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── PriceListNotFoundException.kt
├── PromotionExpiredException.kt
├── OverrideNotAuthorizedException.kt
├── InvalidCurrencyException.kt
└── PricingConfigurationException.kt
```

---

## Domain Services

```
services/
├── PricingEngine.kt                # Deterministic price calculation
├── PromotionEngine.kt              # Rule evaluation and stacking
├── OverrideApprovalPolicy.kt       # Threshold checks and SoD rules
└── PriceListSelector.kt            # Customer/channel-based selection
```

---

## Key Invariants

1. **Active Price List**: Prices must come from an active price list.
2. **Currency Consistency**: Price currency must match order currency.
3. **Promotion Validity**: Promotions must be within effective date range.
4. **Override Approval**: Overrides above threshold require approval.
5. **Deterministic Pricing**: Same inputs must yield the same price output.
