# ADR-025: Commerce (Omnichannel Sales & Distribution)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-05
**Deciders**: Architecture Team, Sales Ops
**Priority**: P1 (High)
**Tier**: Core
**Tags**: commerce, sales, orders, pricing, fulfillment, omnichannel, ecommerce, pos, b2b, marketplace, hexagonal-architecture

## Context
Sales order management, pricing, taxation, and fulfillment are core ERP capabilities. SAP-grade parity requires an end-to-end Order-to-Cash (O2C) flow with credit management, multi-channel orders (POS, ecommerce, marketplace, wholesale), returns, and audit-ready billing.

Modern commerce requires unified omnichannel capabilities spanning B2C e-commerce, retail POS, B2B wholesale, and marketplace operations with shared pricing, inventory, and customer experiences.

## Decision
Implement a **Commerce** bounded context with 5 subdomains covering all sales channels, unified pricing, and order management following **hexagonal architecture** principles.

### Subdomain Architecture
Commerce is implemented as 5 subdomains within the `commerce/` bounded context, each following **hexagonal architecture** with clean separation of concerns:

```
commerce/                                        # COMMERCE BOUNDED CONTEXT (ADR-025) - Omnichannel
├── commerce-shared/                             # Shared types across commerce subdomains
├── commerce-ecommerce/                          # Port 9301 - E-Commerce (B2C/D2C Online)
├── commerce-pos/                                # Port 9302 - Point of Sale (Retail Store)
├── commerce-b2b/                                # Port 9303 - B2B Commerce (Business Customers)
├── commerce-marketplace/                        # Port 9304 - Marketplace (Multi-seller)
└── commerce-pricing/                            # Port 9305 - Dynamic Pricing & Markdown Optimization
```

#### 1. E-Commerce (Port 9301)
**Package**: `com.chiroerp.commerce.ecommerce`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `OnlineStore`, `Storefront`, `ProductListing`, `ShoppingCart`, `CartItem`, `Wishlist`, `Checkout`, `OnlineOrder`, `OrderLine`, `Promotion`, `CouponCode`, `ProductReview`, `OnlineCustomer`, `CustomerAccount` | E-commerce entities |
| **Domain Events** | `CartAbandonedEvent`, `OrderPlacedOnlineEvent`, `CheckoutStartedEvent`, `PaymentFailedEvent`, `ReviewSubmittedEvent`, `WishlistUpdatedEvent`, `ShipmentDispatchedEvent` | E-commerce events |
| **Input Ports** | `CartUseCase`, `CheckoutUseCase`, `OrderUseCase`, `PromotionUseCase`, `ReviewUseCase` | E-commerce use cases |
| **Output Ports** | `CartRepositoryPort`, `CheckoutRepositoryPort`, `OrderRepositoryPort`, `PromotionRepositoryPort`, `PaymentGatewayPort`, `InventoryPort`, `EcommerceEventPublisherPort` | Persistence and integration |
| **Domain Services** | `PersonalizationService`, `RecommendationEngine`, `SEOOptimizationService`, `PricingEngine`, `InventoryCheckService` | Business logic |
| **REST Controllers** | `StorefrontResource`, `CartResource`, `CheckoutResource`, `OrderResource`, `ReviewResource` | API endpoints |

#### 2. Point of Sale (Port 9302)
**Package**: `com.chiroerp.commerce.pos`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `POSTerminal`, `TerminalSession`, `CashDrawer`, `RetailTransaction`, `TransactionLine`, `ScanItem`, `POSPayment`, `PaymentTender`, `CashPayment`, `CardPayment`, `Receipt`, `RetailReturn`, `EndOfDay`, `CashReconciliation` | POS entities |
| **Domain Events** | `TerminalOpenedEvent`, `TransactionCompletedEvent`, `PaymentProcessedEvent`, `VoidTransactionEvent`, `ReturnProcessedEvent`, `EndOfDayCompletedEvent`, `DrawerReconciliationEvent` | POS events |
| **Input Ports** | `TerminalUseCase`, `TransactionUseCase`, `PaymentUseCase`, `ReceiptUseCase`, `EODUseCase` | POS use cases |
| **Output Ports** | `TerminalRepositoryPort`, `TransactionRepositoryPort`, `ReceiptRepositoryPort`, `PaymentTerminalPort`, `BarcodeScannerPort`, `ReceiptPrinterPort`, `CashDrawerPort`, `POSEventPublisherPort` | Persistence and hardware |
| **Domain Services** | `TransactionService`, `PaymentProcessingService`, `ReceiptService`, `EODService`, `CashReconciliationService` | Business logic |
| **REST Controllers** | `TerminalResource`, `TransactionResource`, `PaymentResource`, `EODResource` | API endpoints |

#### 3. B2B Commerce (Port 9303)
**Package**: `com.chiroerp.commerce.b2b`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `BusinessAccount`, `CompanyProfile`, `TaxExemption`, `CreditLimit`, `PaymentTerms`, `Buyer`, `BuyerRole`, `ApprovalWorkflow`, `Contract`, `PricingAgreement`, `VolumeDiscount`, `Quote`, `QuoteLine`, `B2BOrder`, `BlanketOrder`, `B2BCatalog`, `CustomPricing`, `TierPricing` | B2B entities |
| **Domain Events** | `QuoteRequestedEvent`, `QuoteApprovedEvent`, `OrderApprovedEvent`, `OrderRejectedEvent`, `ContractSignedEvent`, `CreditLimitExceededEvent`, `B2BOrderPlacedEvent` | B2B events |
| **Input Ports** | `AccountUseCase`, `QuoteUseCase`, `B2BOrderUseCase`, `ContractUseCase`, `CreditUseCase` | B2B use cases |
| **Output Ports** | `AccountRepositoryPort`, `QuoteRepositoryPort`, `B2BOrderRepositoryPort`, `ContractRepositoryPort`, `CreditBureauPort`, `ApprovalWorkflowPort`, `B2BEventPublisherPort` | Persistence and integration |
| **Domain Services** | `ApprovalWorkflowService`, `ContractPricingService`, `CreditCheckService`, `QuoteNegotiationService` | Business logic |
| **REST Controllers** | `AccountResource`, `QuoteResource`, `B2BOrderResource`, `ContractResource` | API endpoints |

#### 4. Marketplace (Port 9304)
**Package**: `com.chiroerp.commerce.marketplace`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `Seller`, `SellerProfile`, `SellerRating`, `SellerPerformance`, `VerificationStatus`, `ProductListing`, `ListingApproval`, `Commission`, `CommissionRule`, `CommissionTier`, `CommissionPayout`, `FulfillmentOption`, `SellerFulfillment`, `PlatformFulfillment` | Marketplace entities |
| **Domain Events** | `SellerOnboardedEvent`, `SellerVerifiedEvent`, `ListingCreatedEvent`, `ListingApprovedEvent`, `ListingRejectedEvent`, `CommissionCalculatedEvent`, `PayoutProcessedEvent` | Marketplace events |
| **Input Ports** | `SellerUseCase`, `ListingUseCase`, `CommissionUseCase`, `PayoutUseCase` | Marketplace use cases |
| **Output Ports** | `SellerRepositoryPort`, `ListingRepositoryPort`, `CommissionRepositoryPort`, `PayoutPort`, `MarketplaceEventPublisherPort` | Persistence and integration |
| **Domain Services** | `CommissionCalculationService`, `SellerVerificationService`, `ListingModerationService`, `PayoutService` | Business logic |
| **REST Controllers** | `SellerResource`, `ListingResource`, `CommissionResource`, `PayoutResource` | API endpoints |

#### 5. Dynamic Pricing (Port 9305)
**Package**: `com.chiroerp.commerce.pricing`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `PriceList`, `PriceCondition`, `DynamicPrice`, `MarkdownSchedule`, `CompetitorPrice`, `PriceOptimization`, `ElasticityModel`, `PromotionRule`, `BundlePrice`, `ChannelPrice`, `CustomerSegmentPrice` | Pricing entities |
| **Domain Events** | `PriceUpdatedEvent`, `MarkdownAppliedEvent`, `CompetitorPriceChangedEvent`, `PromotionActivatedEvent`, `PriceOptimizationRunEvent` | Pricing events |
| **Input Ports** | `PriceListUseCase`, `DynamicPricingUseCase`, `MarkdownUseCase`, `PromotionUseCase` | Pricing use cases |
| **Output Ports** | `PriceListRepositoryPort`, `PricingRuleRepositoryPort`, `CompetitorPricePort`, `PricingEventPublisherPort` | Persistence and integration |
| **Domain Services** | `PriceCalculationService`, `DynamicPricingEngine`, `MarkdownOptimizationService`, `CompetitorAnalysisService` | Business logic |
| **REST Controllers** | `PriceListResource`, `DynamicPricingResource`, `MarkdownResource` | API endpoints |

### Inter-Subdomain Communication

| Source Subdomain | Target Subdomain | Communication | Purpose |
|------------------|------------------|---------------|---------|
| E-Commerce (9301) | Pricing (9305) | Query | Get product prices for storefront |
| E-Commerce (9301) | Marketplace (9304) | Query | Display marketplace listings |
| POS (9302) | Pricing (9305) | Query | Real-time price lookup |
| B2B (9303) | Pricing (9305) | Query | Contract and tier pricing |
| B2B (9303) | E-Commerce (9301) | Event | Sync B2B customer orders |
| Marketplace (9304) | E-Commerce (9301) | Event | Marketplace orders to fulfillment |
| Marketplace (9304) | Pricing (9305) | Query | Seller pricing rules |
| Pricing (9305) | All Channels | Event | Price change notifications |

### Port Assignments

| Subdomain | Port | Package |
|-----------|------|---------|
| E-Commerce | 9301 | `com.chiroerp.commerce.ecommerce` |
| Point of Sale | 9302 | `com.chiroerp.commerce.pos` |
| B2B Commerce | 9303 | `com.chiroerp.commerce.b2b` |
| Marketplace | 9304 | `com.chiroerp.commerce.marketplace` |
| Dynamic Pricing | 9305 | `com.chiroerp.commerce.pricing` |

### Scope
- Direct sales, wholesale, and POS orders.
- Multi-channel order capture and allocation.
- Pricing, promotions, and tax determination.
- Returns, refunds, and credit memo handling.

### Feature Tiering (Core vs Advanced)
**Core**
- Sales order capture, invoicing, basic pricing, and basic returns.

**Advanced**
- Credit management, rebates/commissions, omnichannel orchestration, advanced ATP.

### Core Capabilities
- **Order lifecycle**: quote -> order -> delivery -> invoice -> payment.
- **Pricing engine**: multi-currency price lists, discounts, promotions, rebates, and approvals.
- **Tax integration**: VAT/GST/Sales Tax, exemptions, and jurisdiction rules.
- **Credit management**: customer limits, holds, and release workflow.
- **Returns management**: RMA, restock, refunds, and credit notes.
- **Channel orchestration**: POS, ecommerce, marketplace, and wholesale.
- **ATP and backorder** management with substitution rules.
- **Commissions and rebates**: sales incentives and rebate settlement workflows.

### Data Model (Conceptual)
- `Customer`, `SalesOrder`, `OrderLine`, `PriceCondition`, `Promotion`, `Delivery`, `Shipment`, `Invoice`, `CreditMemo`, `ReturnOrder`, `RMA`.

### Key Workflows
- **O2C**: order -> allocate -> pick/pack/ship -> invoice -> cash.
- **POS flow**: scan -> payment -> receipt -> inventory deduction.
- **Returns**: RMA approval -> receipt -> restock/write-off -> refund/credit.
- **Credit management**: automatic checks with override approvals.

### Integration Points

#### Internal Subdomain Integration
The 5 Commerce subdomains integrate through domain events and queries:

| Source → Target | Integration Pattern | Purpose |
|-----------------|---------------------|---------|
| E-Commerce (9301) → Pricing (9305) | Query | Get product prices for storefront |
| E-Commerce (9301) → Marketplace (9304) | Query | Display marketplace listings |
| POS (9302) → Pricing (9305) | Query | Real-time price lookup |
| B2B (9303) → Pricing (9305) | Query | Contract and tier pricing |
| B2B (9303) → E-Commerce (9301) | Event | Sync B2B customer orders |
| Marketplace (9304) → E-Commerce (9301) | Event | Marketplace orders to fulfillment |
| Marketplace (9304) → Pricing (9305) | Query | Seller pricing rules |
| Pricing (9305) → All Channels | Event | Price change notifications |

#### External Module Integration

##### Inventory (ADR-024)
- **All channels** → ATP, reservations, and shipment confirmation.
- **POS (9302)** → Real-time stock updates on transaction.
- **E-Commerce (9301)** → Inventory availability display.

##### Finance/AR (ADR-020)
- **All channels** → Invoice posting, revenue and receivables.
- **B2B (9303)** → Credit limit checks and payment terms.
- **Marketplace (9304)** → Seller payout accounting.

##### Revenue Recognition (ADR-022)
- **E-Commerce (9301)** → Contract and billing linkages.
- **B2B (9303)** → Multi-element arrangement recognition.

##### Tax Engines
- **Pricing (9305)** → External VAT/GST/Sales Tax services.
- **POS (9302)** → Real-time tax calculation.

##### Payment Gateways
- **E-Commerce (9301)** → Online payment processing.
- **POS (9302)** → Card terminal integration.
- **Marketplace (9304)** → Seller payout processing.

### Non-Functional Constraints
- **Latency**: channel orders processed within 2 seconds p95.
- **Consistency**: price and promotion evaluation deterministic.
- **Idempotency**: order submissions safe for retries.

## Alternatives Considered
- **Standalone CRM**: rejected (no fulfillment integration).
- **Minimal order tracking**: rejected (no pricing/tax controls).
- **External billing system**: rejected (audit and reconciliation complexity).
- **Single-channel only**: rejected (insufficient for omnichannel).

## Consequences
### Positive
- Full O2C traceability with audit-ready controls.
- Unified pricing and promotions across channels.
- Improved cash flow forecasting and credit control.

### Negative
- Complex configuration for pricing, tax, and credit policies.
- Requires strong master data governance for customers and products.

### Neutral
- Advanced SD capabilities (rebates, ATP optimization) can be phased.

## Compliance
- **SOX**: approvals for pricing overrides and credit notes.
- **GDPR**: customer PII handling controls.
- **Tax compliance**: jurisdictional tax rules and exemptions.

## Implementation Plan
Implementation follows the subdomain architecture within `commerce/`:

### Phase 1: Foundation
- **commerce-shared**: Shared catalog, pricing, and order types.
- **commerce-pricing** (Port 9305): Price lists, basic discounts, tax calculation.
- Integration with Inventory (ADR-024) and Finance/AR.

### Phase 2: E-Commerce
- **commerce-ecommerce** (Port 9301): Storefront, cart, checkout, online orders.
- Payment gateway integration.
- Basic promotions and coupon codes.
- Order tracking and shipment notifications.

### Phase 3: Point of Sale
- **commerce-pos** (Port 9302): Terminal management, transactions, payments.
- Hardware integration (barcode scanner, receipt printer, cash drawer).
- End-of-day reconciliation.
- Returns and refunds processing.

### Phase 4: B2B Commerce
- **commerce-b2b** (Port 9303): Business accounts, quotes, contracts.
- Credit management and approval workflows.
- Volume discounts and tier pricing.
- Blanket orders and scheduled deliveries.

### Phase 5: Marketplace
- **commerce-marketplace** (Port 9304): Seller onboarding, listings, commissions.
- Multi-seller fulfillment options.
- Seller performance tracking.
- Payout processing.

### Phase 6: Advanced Pricing
- **commerce-pricing** (Port 9305): Dynamic pricing, markdown optimization.
- Competitor price monitoring.
- AI-driven price elasticity modeling.
- Channel-specific pricing strategies.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-022: Revenue Recognition
- ADR-024: Inventory Management

### Internal Documentation
- `docs/sales/sd_requirements.md`

### External References
- SAP SD order-to-cash processes
- IFRS 15 / ASC 606 sales contract considerations
