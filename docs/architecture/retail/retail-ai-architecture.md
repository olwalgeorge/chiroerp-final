# Retail AI Architecture - ChiroERP

**Status**: Optional Enhancement (Post-Phase 3)
**Priority**: P2 (High Value - Competitive Differentiation)
**Last Updated**: February 3, 2026
**Target Market**: $50M-$500M revenue retailers (50-500 stores)

---

## Executive Summary

### Business Context

**Current State**: ChiroERP retail capabilities (ADR-024, ADR-025) provide strong operational foundation (multi-store inventory, POS synchronization, omnichannel orders, advanced inventory operations). However, mid-market retailers ($50M-$500M revenue, 50-500 stores) face critical challenges:

- **15-20% stockouts** → Lost sales, customer dissatisfaction
- **25-30% excess inventory** → Markdown losses, carrying costs
- **10-15% excess end-of-season inventory** → Deep markdowns, margin erosion
- **3-5% margin erosion** → Late/reactive markdown decisions

**Competitive Gap**: Enterprise retail systems (SAP IBP, Oracle RDFC, Blue Yonder Luminate, SAP OPP, Oracle RPM) provide AI-powered demand forecasting and dynamic pricing that ChiroERP currently lacks. This gap prevents ChiroERP from competing for 50-500 store retail chains.

**Solution**: Two optional AI enhancements (ADR-056, ADR-057) that close competitive gaps and generate significant ROI:

| Enhancement | Investment | Timeline | ROI | Business Impact (100-store chain) |
|-------------|------------|----------|-----|----------------------------------|
| **ADR-056: AI Demand Forecasting** | $500K-$1M | 9 months | 3-5X ($5M-$8M) | Stockouts 15% → 5-8%, Excess 25% → 10-15% |
| **ADR-057: Dynamic Pricing** | $300K-$500K | 9 months | 2-3X ($4M-$6M) | Margin +2-3%, Clearance reduction 5-10% |
| **Combined Impact** | $800K-$1.5M | 18 months | 3-4X ($10M-$16M) | **Total 3-year savings: $10M-$16M** |

### Strategic Positioning

**Before AI Enhancement**:
- ✅ Strong operational foundation (inventory, POS, omnichannel)
- ✅ Advanced inventory operations (packaging, kitting, catch weight)
- ❌ No AI demand forecasting (vs. SAP IBP, Oracle RDFC, Blue Yonder)
- ❌ No dynamic pricing (vs. SAP OPP, Oracle RPM)
- 🎯 Target: Basic retail operations, 10-50 stores

**After AI Enhancement**:
- ✅ Enterprise-grade AI demand forecasting (competitive with SAP/Oracle/Blue Yonder)
- ✅ Enterprise-grade dynamic pricing (competitive with SAP/Oracle)
- ✅ Differentiated vs. NetSuite/Odoo (no AI capabilities)
- ✅ Cost-effective vs. SAP/Oracle (1/3 price, 6-month implementation vs. 18-24 months)
- 🎯 Target: Mid-market retail chains, 50-500 stores, $50M-$500M revenue

---

## Phase AI-1: AI Demand Forecasting & Replenishment (ADR-056)

### Overview

**Purpose**: Implement AI-powered demand forecasting and automatic replenishment recommendations to minimize stockouts and excess inventory.

**ADR Reference**: ADR-056 (AI Demand Forecasting & Replenishment)
**Investment**: $500K-$1M
**Headcount**: 2-3 data scientists (time series expertise)
**Timeline**: 9 months (3 phases)
**ROI**: 3-5X in Year 1 = $5M-$8M savings (100-store chain)

### Core Capabilities

#### 1. Time Series Forecasting Engine
**Progressive Enhancement Approach**:
- **Phase 1 (Months 1-3)**: Classical time series (ARIMA, Prophet) for quick wins
- **Phase 2 (Months 4-6)**: Advanced ML (XGBoost, ensemble) for accuracy improvements
- **Phase 3 (Months 7-9)**: Deep learning (LSTM, Transformers) for complex patterns

**Models**:
- **ARIMA**: Univariate time series, linear trends
- **Prophet**: Additive model with seasonality, holidays, special events
- **XGBoost**: Tree-based ensemble with feature engineering (price, promotions, weather)
- **LSTM**: Sequential neural networks for long-term dependencies
- **Transformers**: Attention-based models for multi-variate time series

**Key Features**:
- Automatic model selection based on data characteristics
- Model comparison and ensemble (combine multiple models for accuracy)
- Forecast horizon: 1 week to 26 weeks (6 months)
- Granularity: SKU-Store-Week level

#### 2. Seasonality Detection & Modeling
**Techniques**:
- **STL Decomposition**: Seasonal-Trend-Loess decomposition (separate trend, seasonality, residuals)
- **Fourier Series**: Capture weekly, monthly, annual cycles
- **Holiday Effects**: Model spikes around holidays (Christmas, Easter, Black Friday, back-to-school)

**Patterns Detected**:
- Weekly cycles (weekend vs. weekday demand)
- Monthly cycles (paycheck cycles, month-end shopping)
- Annual cycles (seasonal merchandise, weather-driven demand)
- Event-driven spikes (sports events, concerts, local festivals)

#### 3. Promotion Impact Modeling
**Promotion Types**:
- **BOGO** (Buy One Get One): 50-150% uplift typical
- **Percentage Discounts**: 10-50% off → elasticity-based uplift
- **Dollar Discounts**: $5 off, $10 off → fixed value uplift
- **Bundle Offers**: Buy X, Get Y free → cross-item effects

**Modeling Approach**:
- Baseline demand (without promotion) vs. promoted demand
- Uplift calculation: (Promoted Demand - Baseline) / Baseline
- Elasticity curves: Demand sensitivity to discount depth
- Cannibalization effects: Promoted item steals sales from related items
- Halo effects: Promoted item increases sales of complementary items

**Output**:
- Promotion demand forecast (SKU-Store-Week)
- Stock requirement recommendations (ensure adequate stock for promotion)
- Buyer alerts (low stock warnings before promotion starts)

#### 4. External Signal Integration
**Data Sources**:
- **Weather**: Temperature, precipitation, humidity (impact on seasonal merchandise)
- **Events**: Sports games, concerts, local festivals (traffic drivers)
- **Competitor Activity**: Competitor promotions, pricing changes (market share impact)
- **Economic Indicators**: Consumer confidence, unemployment, inflation (macro trends)

**Integration Methods**:
- REST API integrations (OpenWeatherMap, Ticketmaster, Google Places)
- Web scraping for competitor intelligence
- Feature engineering: Convert raw signals into model inputs

**Use Cases**:
- Cold weather → Forecast spike in winter apparel, hot beverages
- Sports event nearby → Forecast spike in snacks, beverages, team merchandise
- Competitor promotion → Forecast demand shift, adjust pricing/promotions

#### 5. Automatic Reorder Points
**Formula**:
```
Reorder Point (ROP) = (Average Daily Demand × Lead Time) + Safety Stock
Safety Stock = Z-score × √(Lead Time) × Std Dev of Demand
```

**Service Level Configuration**:
- 90% service level → Z = 1.28 (10% stockout risk)
- 95% service level → Z = 1.65 (5% stockout risk)
- 99% service level → Z = 2.33 (1% stockout risk)

**Dynamic Adjustment**:
- ROP recalculated daily based on latest demand forecast
- Lead time updates from supplier performance data
- Safety stock adjusted based on forecast accuracy

**Output**:
- Automatic replenishment recommendations (SKU-Store-Day)
- Purchase order suggestions (quantities, timing)
- Transfer order suggestions (DC → Store allocation)

#### 6. Multi-Echelon Optimization
**Network Structure**:
- **Distribution Centers (DCs)**: 1-5 DCs serving 50-500 stores
- **Stores**: Retail locations with limited backroom space
- **Cross-Docking**: Fast-moving items bypass DC storage

**Optimization Goals**:
- Minimize total inventory (DC + stores) while meeting service levels
- Balance DC holding costs vs. store holding costs
- Minimize transportation costs (DC → Store shipments)
- Respect capacity constraints (DC space, truck capacity, store backroom)

**Allocation Logic**:
- Forecast demand at store level (SKU-Store-Week)
- Aggregate to DC level (sum of store forecasts)
- DC orders from suppliers (lead time: 2-8 weeks)
- DC allocates to stores (lead time: 1-3 days)
- Priority rules: High-velocity stores first, slow-movers last

**Output**:
- DC replenishment plan (SKU-DC-Week)
- Store allocation plan (SKU-Store-Day)
- Transfer order recommendations (DC → Store)

#### 7. Forecast Accuracy Monitoring
**Metrics Tracked**:
- **MAPE (Mean Absolute Percentage Error)**: |(Actual - Forecast) / Actual| × 100
  - Target: ≤15% (Phase 2), ≤10% (Phase 3)
- **MAE (Mean Absolute Error)**: |Actual - Forecast|
- **RMSE (Root Mean Squared Error)**: √(Mean of (Actual - Forecast)²)
- **Bias**: Mean of (Actual - Forecast) — detects systematic over/under forecasting

**Monitoring Frequency**:
- Daily accuracy checks (rolling 7-day, 30-day windows)
- Weekly accuracy reports (SKU-Store, Category, Store)
- Monthly model retraining triggers (if MAPE > 15% for 2 consecutive weeks)

**Auto-Retraining Logic**:
- If accuracy degrades → retrain model with latest data
- If new product → use category-level model until sufficient history
- If promotion → use promotion-specific model
- If seasonality shift → retrigger STL decomposition

### Implementation Plan

#### Phase 1: Foundation (Months 1-3)
**Goal**: Deliver MVP with classical time series models and basic reorder points

**Deliverables**:
- [ ] Classical time series engine (ARIMA, Prophet)
- [ ] Reorder point calculator (ROP formula, safety stock)
- [ ] Forecast dashboard (SKU-Store-Week forecasts, accuracy metrics)
- [ ] Buyer workflow integration (ADR-046: approval routing, task inbox)
- [ ] Data pipeline (ETL: sales history, inventory levels, pricing, promotions)

**Success Metrics**:
- Forecast accuracy: MAPE ≤20% (baseline)
- 50% of buyers adopt recommendations
- System uptime: ≥99%

**Technology Stack**:
- Python, pandas, statsmodels (ARIMA), Prophet
- PostgreSQL (forecast storage)
- Kafka (event publishing)
- React (forecast dashboard UI)

#### Phase 2: Enhancement (Months 4-6)
**Goal**: Improve accuracy with advanced ML and promotion modeling

**Deliverables**:
- [ ] Advanced ML models (XGBoost, ensemble)
- [ ] Promotion impact modeling (BOGO, discounts, bundles)
- [ ] External signal integration (weather, events APIs)
- [ ] What-if analysis UI (scenario planning: "What if we run 20% off promotion?")
- [ ] Model comparison dashboard (ARIMA vs. Prophet vs. XGBoost accuracy)

**Success Metrics**:
- Forecast accuracy: MAPE ≤15% (10-15% improvement vs. Phase 1)
- 75% of buyers adopt recommendations
- Promotion forecast accuracy: MAPE ≤20%

**Technology Stack**:
- XGBoost, scikit-learn (feature engineering, model training)
- OpenWeatherMap API, Ticketmaster API (external signals)
- MLflow (model versioning, experiment tracking)

#### Phase 3: Advanced (Months 7-9)
**Goal**: Achieve enterprise-grade accuracy with deep learning and multi-echelon optimization

**Deliverables**:
- [ ] Deep learning models (LSTM, Transformers)
- [ ] Multi-echelon optimization (DC → Store allocation)
- [ ] Real-time demand sensing (intraday forecast updates based on actual sales)
- [ ] MLOps pipeline (automated retraining, A/B testing, model deployment)
- [ ] Mobile app (buyer approvals, alerts)

**Success Metrics**:
- Forecast accuracy: MAPE ≤10% (enterprise-grade)
- 75%+ buyer adoption sustained
- Multi-echelon optimization: 10-15% inventory reduction

**Technology Stack**:
- TensorFlow/PyTorch (LSTM, Transformers)
- Ray/Dask (distributed training)
- Kubernetes (model serving)
- MLflow (production model registry)

### Business Impact (100-Store Apparel Retailer)

**Profile**:
- $200M annual revenue
- 5,000 SKUs
- 100 stores across 5 regions
- 70% in-store, 30% online
- 2,000 employees

**3-Year Impact**:

| Metric | Baseline (Year 0) | Year 1 (ADR-056) | Year 2 (Sustained) | Year 3 (Sustained) |
|--------|-------------------|------------------|--------------------|--------------------|
| **Stockouts** | 15% | 6-8% | 5-8% | 5-8% |
| **Excess Inventory** | 25% | 12-15% | 10-15% | 10-15% |
| **Inventory Carrying Cost** | $10M | $8.5M-$9M | $8M-$8.5M | $8M-$8.5M |
| **Lost Sales (Stockouts)** | $6M | $2.5M-$3M | $2M-$2.5M | $2M-$2.5M |
| **Markdown Losses** | $8M | $5M-$6M | $4M-$5M | $4M-$5M |
| **Buyer Productivity** | 100% | 120% | 130% | 130% |
| **Cumulative Savings** | - | **$5M-$8M** | **$7M-$10M** | **$8M-$11M** |

**ROI Calculation**:
- Investment: $500K-$1M (Year 1)
- Savings: $5M-$8M (Year 1)
- ROI: **3-5X** in Year 1
- Payback period: **2-3 months**

### Technical Architecture

**Data Model** (7 core entities):

```kotlin
// 1. DemandForecast
data class DemandForecast(
    val forecastId: UUID,
    val sku: String,
    val storeId: String,
    val forecastWeek: LocalDate,
    val forecastedQuantity: Int,
    val confidenceInterval: Pair<Int, Int>, // 95% CI: (low, high)
    val modelUsed: String, // "ARIMA", "Prophet", "XGBoost", "LSTM"
    val mape: Double,
    val generatedAt: Instant
)

// 2. ForecastModel
data class ForecastModel(
    val modelId: UUID,
    val modelType: String, // "ARIMA", "Prophet", "XGBoost", "LSTM"
    val sku: String?,
    val category: String?,
    val parameters: Map<String, Any>,
    val accuracy: Double, // MAPE
    val trainedAt: Instant,
    val version: Int
)

// 3. ReorderPoint
data class ReorderPoint(
    val ropId: UUID,
    val sku: String,
    val storeId: String,
    val reorderPoint: Int,
    val safetyStock: Int,
    val leadTime: Int, // days
    val serviceLevel: Double, // 0.95 = 95%
    val calculatedAt: Instant
)

// 4. PromotionPlan
data class PromotionPlan(
    val promotionId: UUID,
    val sku: String,
    val storeIds: List<String>,
    val promotionType: String, // "BOGO", "PERCENT_OFF", "DOLLAR_OFF"
    val discountValue: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val forecastedUplift: Double, // 0.50 = 50% uplift
    val stockRequirement: Int
)

// 5. ExternalSignal
data class ExternalSignal(
    val signalId: UUID,
    val signalType: String, // "WEATHER", "EVENT", "COMPETITOR"
    val storeId: String,
    val date: LocalDate,
    val value: Map<String, Any>, // {"temp": 75, "precipitation": 0.1}
    val source: String
)

// 6. ForecastAccuracy
data class ForecastAccuracy(
    val accuracyId: UUID,
    val sku: String,
    val storeId: String,
    val week: LocalDate,
    val forecastedQuantity: Int,
    val actualQuantity: Int,
    val mape: Double,
    val bias: Double,
    val modelUsed: String
)

// 7. ScenarioAnalysis
data class ScenarioAnalysis(
    val scenarioId: UUID,
    val scenarioName: String,
    val sku: String,
    val storeId: String,
    val assumptions: Map<String, Any>, // {"promotionDiscount": 0.20, "weatherTemp": 85}
    val forecastedQuantity: Int,
    val createdBy: String,
    val createdAt: Instant
)
```

**Key Workflows**:

1. **Daily Forecast Generation**:
   - Data collection (sales history, inventory levels, pricing, promotions)
   - Feature engineering (lag features, moving averages, seasonality indicators)
   - Model selection (pick best model per SKU-Store based on historical accuracy)
   - Forecast generation (1-26 weeks ahead)
   - Forecast storage (PostgreSQL)
   - Event publishing (DemandForecastGeneratedEvent)

2. **Automatic Replenishment Recommendations**:
   - Read demand forecasts (next 4 weeks)
   - Read current inventory levels (on-hand, on-order)
   - Calculate reorder points (ROP formula)
   - Generate replenishment recommendations (if inventory < ROP)
   - Route to buyer workflow (ADR-046: approval task)
   - If approved → generate purchase order (ADR-023)

3. **Promotion Impact Analysis**:
   - Read promotion plan (SKU, stores, dates, discount)
   - Calculate baseline demand (without promotion)
   - Calculate promoted demand (baseline × uplift factor)
   - Calculate stock requirement (promoted demand + safety buffer)
   - Generate buyer alert (if current stock < requirement)
   - Update inventory plan (reserve stock for promotion)

4. **Model Retraining**:
   - Nightly accuracy check (compare forecasts vs. actuals)
   - If MAPE > 15% for 2 consecutive weeks → trigger retraining
   - Retrain model with latest data (last 52 weeks)
   - Compare new model vs. old model accuracy
   - If new model better → deploy to production (MLflow)
   - If new model worse → keep old model, alert data science team

**Integration Points**:
- **ADR-024 (Inventory Management)**: Stock levels, lot tracking, reservations
- **ADR-025 (Sales & Distribution)**: Sales history, pricing, promotions
- **ADR-023 (Procurement)**: Purchase order generation, supplier lead times
- **ADR-046 (Workflow & Approval Engine)**: Buyer approvals, task routing
- **ADR-016 (Analytics & Reporting)**: Forecast dashboards, accuracy metrics
- **ADR-027 (Master Data Governance)**: SKU master, store master, category hierarchy

**Events Published**:
- `DemandForecastGeneratedEvent`: Daily forecast generation complete
- `ReplenishmentRecommendationCreatedEvent`: Buyer approval needed
- `ForecastAccuracyDegradedEvent`: Model retraining required

### Competitive Impact

**Before ADR-056**:
- ❌ No AI demand forecasting
- ❌ Manual reorder points (static, outdated)
- ❌ High stockouts (15-20%), high excess (25-30%)
- ❌ Not competitive for 50-500 store retail chains

**After ADR-056**:
- ✅ AI demand forecasting (ARIMA, Prophet, XGBoost, LSTM, Transformers)
- ✅ Automatic reorder points (dynamic, data-driven)
- ✅ Low stockouts (5-8%), low excess (10-15%)
- ✅ **Competitive with SAP IBP, Oracle RDFC, Blue Yonder Luminate Demand**
- ✅ **Differentiated vs. NetSuite/Odoo** (no AI forecasting)
- ✅ **Cost-effective vs. SAP/Oracle** (1/3 price, 6-month implementation)

---

## Phase AI-2: Dynamic Pricing & Markdown Optimization (ADR-057)

### Overview

**Purpose**: Implement AI-powered dynamic pricing and markdown optimization to maximize gross margin and minimize clearance inventory.

**ADR Reference**: ADR-057 (Dynamic Pricing & Markdown Optimization)
**Investment**: $300K-$500K
**Headcount**: 1-2 data scientists (pricing/elasticity expertise)
**Timeline**: 9 months (3 phases)
**ROI**: 2-3X in Year 1 = $4M-$6M margin improvement (100-store chain)
**Prerequisites**: **ADR-056 (demand forecasting) REQUIRED** — pricing optimization needs demand forecasts for elasticity modeling

### Core Capabilities

#### 1. Price Elasticity Modeling
**Purpose**: Understand how demand changes with price changes

**Elasticity Formula**:
```
Elasticity = % Change in Quantity / % Change in Price
|Elasticity| > 1 → Elastic (demand sensitive to price)
|Elasticity| < 1 → Inelastic (demand insensitive to price)
```

**Modeling Techniques**:
- **Log-Log Regression**: Simple elasticity estimation (log(Q) ~ log(P))
- **XGBoost**: Non-linear elasticity with feature interactions (price, promotions, weather, competitor prices)
- **Hierarchical Models**: Category-level → Brand-level → SKU-level elasticity

**Use Cases**:
- Elastic items (fashion, electronics) → Small price increases → large demand drops → use cautiously
- Inelastic items (essentials, staples) → Price increases → minimal demand impact → pricing power

#### 2. Markdown Optimization
**Purpose**: Determine optimal markdown schedule to maximize gross margin

**Multi-Stage Markdown Strategy**:
- **Early Season (0-25% of season)**: No markdown (full price sales)
- **Mid Season (25-60% of season)**: 15-25% off (capture willing-to-pay customers)
- **Late Season (60-85% of season)**: 30-40% off (accelerate sell-through)
- **Clearance (85-100% of season)**: 50-70% off (liquidate remaining inventory)

**Optimization Goal**:
```
Maximize: Gross Margin = Σ (Price × Quantity Sold - Cost × Quantity Sold)
Subject to:
- Inventory constraint (sell all units by season end)
- Price floor (minimum acceptable price)
- Markdown depth limits (max 70% off)
```

**Output**:
- Markdown schedule (SKU-Store-Week: markdown depth, timing)
- Revenue forecast (expected revenue by week)
- Margin forecast (expected margin by week)

#### 3. Clearance Acceleration
**Purpose**: Identify slow-moving items early and accelerate markdowns to avoid deep discounts

**Detection Metrics**:
- **Weeks of Supply (WOS)**: Current Inventory / Average Weekly Sales
  - WOS > 12 weeks → High risk (slow mover)
  - WOS 8-12 weeks → Medium risk (monitor)
  - WOS < 8 weeks → Low risk (healthy turnover)
- **Sell-Through Rate (STR)**: Units Sold / Units Received
  - STR < 40% at mid-season → High risk (accelerate markdown)
  - STR 40-60% at mid-season → Medium risk (monitor)
  - STR > 60% at mid-season → Low risk (on track)

**Risk-Based Recommendations**:
- High risk → Recommend aggressive markdown (30-40% off) NOW
- Medium risk → Recommend moderate markdown (15-25% off) in 2 weeks
- Low risk → Monitor, no markdown yet

#### 4. Competitive Pricing Intelligence
**Purpose**: Monitor competitor prices and adjust pricing to maintain competitive parity

**Data Collection**:
- Web scraping (competitor websites, Amazon, Walmart, Target)
- Price monitoring APIs (Prisync, Competera)
- Manual data entry (competitive shop visits)

**Metrics Tracked**:
- **Competitive Price Index (CPI)**: Our Price / Average Competitor Price
  - CPI > 1.05 → We're expensive (risk of losing sales)
  - CPI 0.95-1.05 → Competitive parity (good)
  - CPI < 0.95 → We're cheap (margin opportunity?)

**Alerts Generated**:
- Competitor price drop > 10% → Alert merchandiser (consider matching)
- CPI > 1.10 for key items → Alert merchandiser (high risk)
- Competitor promotion detected → Alert merchandiser (evaluate response)

#### 5. Promotion Effectiveness Analysis
**Purpose**: Measure incremental lift from promotions and calculate ROI

**Metrics Calculated**:
- **Incremental Lift**: (Promoted Sales - Baseline Sales) / Baseline Sales
  - 50%+ lift → Successful promotion
  - 20-50% lift → Moderate success
  - < 20% lift → Weak promotion (evaluate continuation)
- **Promotion ROI**: (Incremental Gross Margin - Promotion Cost) / Promotion Cost
  - ROI > 100% → Profitable promotion
  - ROI 0-100% → Break-even to slightly profitable
  - ROI < 0% → Unprofitable (discontinue)
- **Cannibalization**: Sales decrease of related items during promotion
- **Halo Effect**: Sales increase of complementary items during promotion

**Output**:
- Promotion report card (lift, ROI, cannibalization, halo effect)
- Recommendations (continue, modify, discontinue)
- Best practices (which promotion types work best)

#### 6. A/B Testing Framework
**Purpose**: Test pricing changes scientifically before chain-wide rollout

**Test Design**:
- **Control Group**: 30-40% of stores (keep current price)
- **Treatment Group**: 30-40% of stores (test new price)
- **Holdout Group**: 20-30% of stores (no changes, used for validation)

**Statistical Validation**:
- T-test: Compare treatment vs. control sales/margin
- P-value < 0.05 → Statistically significant result
- Effect size: Treatment sales - Control sales (practical significance)

**Rollout Decision**:
- If treatment > control (p < 0.05) → Roll out to all stores
- If treatment < control (p < 0.05) → Do NOT roll out (revert)
- If no significant difference → Need longer test or larger sample

#### 7. Revenue Management
**Purpose**: Balance occupancy (sell-through) vs. average selling price (ASP)

**Trade-Off**:
- High price → Low sell-through → Risk of clearance (bad)
- Low price → High sell-through → Low margin (bad)
- Optimal price → Balance sell-through + margin (good)

**Dynamic Pricing Logic**:
- High inventory level → Lower price (accelerate sell-through)
- Low inventory level → Raise price (maximize margin)
- Mid inventory level → Neutral price (maintain balance)

### Implementation Plan

#### Phase 1: Foundation (Months 10-12)
**Goal**: Deliver MVP with price elasticity modeling and basic markdown optimization

**Deliverables**:
- [ ] Price elasticity modeling (12+ months historical data, log-log regression, XGBoost)
- [ ] Basic markdown optimization (WOS-based, rule-enhanced)
- [ ] Competitive pricing dashboard (manual data entry)
- [ ] Merchandiser workflow integration (ADR-046: approval routing)
- [ ] Data pipeline (ETL: pricing history, sales, inventory, competitor prices)

**Success Metrics**:
- Elasticity model accuracy: R² ≥ 0.60 (baseline)
- 50% of merchandisers adopt recommendations
- System uptime: ≥99%

**Technology Stack**:
- Python, pandas, scikit-learn (elasticity modeling)
- PostgreSQL (price history, elasticity storage)
- React (pricing dashboard UI)

#### Phase 2: Enhancement (Months 13-15)
**Goal**: Improve accuracy with advanced optimization and A/B testing

**Deliverables**:
- [ ] Advanced markdown optimization (multi-stage, dynamic)
- [ ] A/B testing platform (test design, statistical validation, rollout decisions)
- [ ] Automated competitor scraping (web scraping, APIs)
- [ ] Promotion ROI dashboard (lift, cannibalization, halo effect)
- [ ] Clearance acceleration (slow-mover detection, risk-based recommendations)

**Success Metrics**:
- Elasticity model accuracy: R² ≥ 0.70 (10-15% improvement)
- 70% of merchandisers adopt recommendations
- A/B test success rate: ≥60%

**Technology Stack**:
- XGBoost, statsmodels (advanced elasticity, hypothesis testing)
- Scrapy, BeautifulSoup (web scraping)
- Prisync API, Competera API (competitor intelligence)

#### Phase 3: Advanced (Months 16-18)
**Goal**: Achieve enterprise-grade pricing with real-time engine and revenue management

**Deliverables**:
- [ ] Real-time pricing engine (<1 hour response time)
- [ ] Revenue management optimizer (balance sell-through vs. ASP)
- [ ] Advanced analytics (cross-elasticity, cannibalization modeling)
- [ ] Marketing automation integration (email campaigns, personalized offers)
- [ ] Mobile app (merchandiser approvals, alerts)

**Success Metrics**:
- Gross margin improvement: +2-3%
- Clearance inventory reduction: 5-10%
- 70%+ merchandiser adoption sustained

**Technology Stack**:
- Kubernetes (real-time pricing API)
- Advanced optimization (CVXPY, PuLP for linear programming)
- Marketing automation APIs (Klaviyo, Mailchimp)

### Business Impact (100-Store Apparel Retailer)

**Profile**:
- $200M annual revenue
- 5,000 SKUs (high seasonality, fashion/apparel)
- 100 stores across 5 regions
- 40% gross margin baseline

**3-Year Impact**:

| Metric | Baseline (Year 0) | Year 1 (ADR-057) | Year 2 (Sustained) | Year 3 (Sustained) |
|--------|-------------------|------------------|--------------------|--------------------|
| **Gross Margin %** | 40% | 42-43% | 42-43% | 42-43% |
| **Clearance Inventory (units >90 days)** | 10-15% | 5-10% | 5-10% | 5-10% |
| **Markdown Timing** | Manual (reactive) | 2-4 weeks earlier | 2-4 weeks earlier | 2-4 weeks earlier |
| **Competitive Price Parity (CPI)** | Variable (0.90-1.15) | 95-105 for 80% SKUs | 95-105 for 80% SKUs | 95-105 for 80% SKUs |
| **Merchandiser Productivity** | 100% | 120% | 130% | 130% |
| **Cumulative Margin Improvement** | - | **$4M-$6M** | **$6M-$9M** | **$7M-$10M** |

**ROI Calculation**:
- Investment: $300K-$500K (Year 1)
- Margin improvement: $4M-$6M (Year 1)
- ROI: **2-3X** in Year 1
- Payback period: **2-3 months**

### Technical Architecture

**Data Model** (6 core entities):

```kotlin
// 1. PriceElasticity
data class PriceElasticity(
    val elasticityId: UUID,
    val sku: String,
    val category: String,
    val elasticity: Double, // -1.5 = 10% price increase → 15% demand decrease
    val confidenceInterval: Pair<Double, Double>,
    val modelUsed: String,
    val r2: Double, // model fit
    val calculatedAt: Instant
)

// 2. MarkdownRecommendation
data class MarkdownRecommendation(
    val recommendationId: UUID,
    val sku: String,
    val storeId: String,
    val currentPrice: BigDecimal,
    val recommendedPrice: BigDecimal,
    val markdownPercent: Double,
    val reason: String, // "High WOS", "Low STR", "End of Season"
    val forecastedMargin: BigDecimal,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val createdAt: Instant
)

// 3. CompetitorPrice
data class CompetitorPrice(
    val priceId: UUID,
    val sku: String,
    val competitor: String, // "Amazon", "Walmart", "Target"
    val price: BigDecimal,
    val availability: String, // "IN_STOCK", "OUT_OF_STOCK"
    val url: String,
    val scrapedAt: Instant
)

// 4. PromotionROI
data class PromotionROI(
    val promotionId: UUID,
    val sku: String,
    val storeIds: List<String>,
    val promotionType: String,
    val discountValue: Double,
    val baselineSales: Int,
    val promotedSales: Int,
    val incrementalLift: Double, // (promoted - baseline) / baseline
    val grossMargin: BigDecimal,
    val promotionCost: BigDecimal,
    val roi: Double, // (margin - cost) / cost
    val cannibalization: Map<String, Int>, // related SKUs impacted
    val haloEffect: Map<String, Int> // complementary SKUs boosted
)

// 5. ABTestExperiment
data class ABTestExperiment(
    val experimentId: UUID,
    val experimentName: String,
    val sku: String,
    val controlStores: List<String>,
    val treatmentStores: List<String>,
    val controlPrice: BigDecimal,
    val treatmentPrice: BigDecimal,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String, // "RUNNING", "COMPLETED", "ROLLED_OUT"
    val results: ABTestResults?
)

data class ABTestResults(
    val controlSales: Int,
    val treatmentSales: Int,
    val pValue: Double,
    val effectSize: Double,
    val recommendation: String // "ROLLOUT", "DO_NOT_ROLLOUT", "EXTEND_TEST"
)

// 6. PriceHistory
data class PriceHistory(
    val historyId: UUID,
    val sku: String,
    val storeId: String,
    val price: BigDecimal,
    val effectiveDate: LocalDate,
    val reason: String, // "MARKDOWN", "PROMOTION", "COMPETITIVE_MATCH"
    val createdBy: String
)
```

**Key Workflows**:

1. **Weekly Markdown Recommendations**:
   - Read inventory levels (on-hand, age)
   - Calculate WOS, STR (slow-mover detection)
   - Read demand forecast (ADR-056 integration)
   - Read elasticity (price sensitivity)
   - Optimize markdown schedule (maximize margin, meet clearance targets)
   - Generate markdown recommendations (SKU-Store-Week)
   - Route to merchandiser workflow (ADR-046: approval task)
   - If approved → update pricing (ADR-025)

2. **Competitive Pricing Monitoring**:
   - Daily competitor price scraping (web scraping, APIs)
   - Calculate CPI (our price / avg competitor price)
   - If CPI > 1.10 → generate alert (high risk)
   - If competitor promotion detected → generate alert (evaluate response)
   - Generate price match recommendations (if appropriate)
   - Route to merchandiser workflow (auto-adjust or manual approval)

3. **Promotion Effectiveness Analysis**:
   - At promotion end → collect sales data (treatment stores)
   - Calculate baseline (control stores or pre-promotion sales)
   - Calculate incremental lift (treatment - baseline) / baseline
   - Identify cannibalization (related SKU sales decrease)
   - Identify halo effect (complementary SKU sales increase)
   - Calculate promotion ROI (margin - cost) / cost
   - Generate promotion report (lift, ROI, cannibalization, halo, recommendation)

4. **A/B Test Execution**:
   - Test design (select control, treatment, holdout stores)
   - Price deployment (update pricing for treatment stores)
   - Data collection (track sales, margin daily)
   - Statistical analysis (t-test, p-value calculation)
   - Rollout decision (if p < 0.05 and effect size > threshold → rollout)
   - If rollout → update pricing chain-wide
   - If no rollout → revert treatment stores to original price

**Integration Points**:
- **ADR-056 (Demand Forecasting) - CRITICAL**: Demand forecasts required for elasticity modeling
- **ADR-025 (Sales & Distribution)**: Pricing engine, sales history, promotions
- **ADR-024 (Inventory Management)**: Stock levels, age, WOS calculations
- **ADR-046 (Workflow & Approval Engine)**: Merchandiser approvals, task routing
- **ADR-016 (Analytics & Reporting)**: Pricing dashboards, elasticity visualizations
- **ADR-027 (Master Data Governance)**: SKU master, category hierarchy, competitor mapping

**Events Published**:
- `MarkdownRecommendationCreatedEvent`: Merchandiser approval needed
- `CompetitivePriceAlertEvent`: Competitor price drop detected
- `PromotionROICalculatedEvent`: Promotion performance analysis complete

### Competitive Impact

**Before ADR-057**:
- ❌ No dynamic pricing
- ❌ Manual markdown decisions (late, reactive)
- ❌ No competitive intelligence (blind to competitor moves)
- ❌ Margin erosion (3-5%), high clearance (10-15%)

**After ADR-057**:
- ✅ AI-powered dynamic pricing (elasticity-based, data-driven)
- ✅ Automated markdown optimization (2-4 weeks earlier, maximize margin)
- ✅ Competitive pricing intelligence (real-time monitoring, automatic alerts)
- ✅ Margin improvement (+2-3%), low clearance (5-10%)
- ✅ **Competitive with SAP OPP, Oracle RPM, Revionics, Competera**
- ✅ **Differentiated vs. NetSuite/Odoo** (no pricing optimization)
- ✅ **Cost-effective vs. SAP/Oracle** (1/3 price, 6-month implementation)

---

## Combined Business Impact

### 3-Year Cumulative Savings (100-Store Apparel Retailer)

| Year | ADR-056 Savings | ADR-057 Margin Improvement | Total Savings | Cumulative Savings |
|------|-----------------|----------------------------|---------------|--------------------|
| **Year 1** | $5M-$8M | $4M-$6M | **$9M-$14M** | **$9M-$14M** |
| **Year 2** | $7M-$10M | $6M-$9M | **$13M-$19M** | **$10M-$16M** (net of investment) |
| **Year 3** | $8M-$11M | $7M-$10M | **$15M-$21M** | **$10M-$16M** (sustained) |

**Total Investment**: $800K-$1.5M (ADR-056 + ADR-057)
**Total ROI**: **3-4X** in 18 months
**Payback Period**: **2-3 months**

### Competitive Positioning Matrix

| Capability | ChiroERP (Before AI) | ChiroERP (After AI) | SAP Retail | Oracle Retail | Blue Yonder | NetSuite | Odoo |
|------------|---------------------|---------------------|------------|---------------|-------------|----------|------|
| **Multi-Store Inventory** | A+ | A+ | A+ | A+ | A+ | A | A |
| **POS Synchronization** | A+ | A+ | A+ | A+ | A+ | A | A |
| **Omnichannel Orders** | A | A | A+ | A+ | A | B+ | B |
| **Advanced Inventory Ops** | A+ | A+ | A+ (EWM) | A+ | A+ | C | C |
| **Demand Forecasting AI** | ❌ | ✅ **A** | A+ (IBP) | A+ (RDFC) | A+ (Luminate) | ❌ | ❌ |
| **Dynamic Pricing AI** | ❌ | ✅ **A** | A (OPP) | A (RPM) | A (Pricing) | C | ❌ |
| **Labor Forecasting AI** | B+ | B+ | A | A | A+ | C | ❌ |
| **Recommendations** | ❌ | ⚠️ Integrate | A+ | A+ | A | ❌ | ❌ |
| **Customer Analytics** | ❌ | ⚠️ Integrate | A+ (CDP) | A+ (CX) | A | C | C |
| **Implementation Time** | - | **3-6 months** | 18-24 months | 18-24 months | 12-18 months | 6-9 months | 3-6 months |
| **Pricing** | - | **$20K-$200K/year** | $500K-$5M | $500K-$5M | $300K-$2M | $50K-$300K | $10K-$100K |

**Key Takeaways**:
- ✅ **ADR-056 + ADR-057 close CRITICAL gaps** with SAP/Oracle/Blue Yonder retail AI
- ✅ **Differentiated vs. NetSuite/Odoo** (no AI capabilities)
- ✅ **Cost-effective vs. SAP/Oracle** (1/3 to 1/5 the price, 1/3 implementation time)
- 🎯 **Target Market**: $50M-$500M revenue retailers (50-500 stores) — sweet spot between SMB and enterprise

---

## Implementation Strategy

### Recommended Sequencing

```
Year 1: ADR-056 (AI Demand Forecasting & Replenishment)
├── Months 1-3: Phase 1 (Classical time series, MVP)
├── Months 4-6: Phase 2 (Advanced ML, promotions)
└── Months 7-9: Phase 3 (Deep learning, multi-echelon)
    └── ROI Target: $5M-$8M (3-5X investment)

Year 2: ADR-057 (Dynamic Pricing & Markdown Optimization)  ← Depends on ADR-056
├── Months 10-12: Phase 1 (Elasticity, markdown optimization)
├── Months 13-15: Phase 2 (A/B testing, competitor intelligence)
└── Months 16-18: Phase 3 (Real-time pricing, revenue management)
    └── ROI Target: $4M-$6M (2-3X investment)

Year 3+: Integration Strategy
├── Ecommerce AI: Partner with Shopify/BigCommerce (product recommendations)
├── Marketing AI: Partner with Salesforce/HubSpot (segmentation, LTV)
└── Planogram AI: Partner with Trax/RELEX (shelf optimization)
```

### Why This Sequence?

1. **ADR-056 First** (Demand Forecasting):
   - Most critical competitive gap (all enterprise competitors have this)
   - Highest ROI (3-5X) and business impact (stockout + excess reduction)
   - Foundation for ADR-057 (pricing optimization requires demand forecasts for elasticity modeling)
   - Addresses supply chain AI (ChiroERP's core strength)

2. **ADR-057 Second** (Dynamic Pricing):
   - Requires ADR-056 as prerequisite (demand elasticity needs forecasting baseline)
   - Competitive differentiator (less critical than demand forecasting)
   - Higher complexity (requires 12+ months pricing/elasticity data)
   - More valuable for specific verticals (fashion/apparel with high markdowns)

3. **Integration Third** (Customer-Facing AI):
   - Don't build what partners do better (product recommendations, customer segmentation)
   - Focus ChiroERP investment on operational AI (back office, supply chain, workforce)
   - Provide data feeds/APIs to specialized platforms (Shopify, BigCommerce, Salesforce, HubSpot)
   - Lower risk, faster time-to-value

### Alternative Strategy: Partner Instead of Build

**Don't Build** (integrate instead):
1. **Personalized Product Recommendations**:
   - Rationale: Shopify, BigCommerce, Adobe Commerce have mature AI recommendation engines
   - ChiroERP role: Provide inventory/order data feeds to ecommerce platforms
   - Focus: Operational AI (supply chain), not customer-facing AI

2. **Customer Segmentation & LTV**:
   - Rationale: Salesforce, HubSpot, Klaviyo have advanced AI segmentation and LTV modeling
   - ChiroERP role: Provide transaction data exports to marketing platforms
   - Focus: Operational CRM (account health, AR aging), not marketing AI

3. **Planogram Optimization**:
   - Rationale: Specialized vendors exist (Trax, RELEX, Blue Yonder Space Planning) with computer vision
   - ChiroERP role: Provide integration capabilities, not core competency
   - Cost: High development cost ($1M-$2M) for limited addressable market

---

## Success Criteria (12-Month Post-Implementation)

### ADR-056 (Demand Forecasting) Success Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| **Forecast Accuracy (MAPE)** | ≤15% (Phase 2), ≤10% (Phase 3) | Compare forecasted vs. actual sales weekly |
| **Stockout Reduction** | ≥50% (15% → 7-8%) | Track frequency of zero-stock events by SKU-Store |
| **Excess Inventory Reduction** | ≥40% (25% → 15%) | Count units >90 days old / total inventory |
| **Inventory Turns Improvement** | +10-20% | COGS / Average Inventory (quarterly) |
| **Buyer Adoption Rate** | ≥75% | % of replenishment recommendations accepted |
| **Model Retraining Frequency** | ≥90% models retrained monthly | Track model freshness in MLflow registry |
| **System Uptime** | ≥99.5% | Monitor forecast generation job success rate |

### ADR-057 (Dynamic Pricing) Success Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| **Gross Margin Improvement** | +2-3% | Compare gross margin % before/after AI (quarterly) |
| **Clearance Inventory Reduction** | ≥5-10% | Count units >90 days old at season-end / total inventory |
| **Markdown Timing Improvement** | 2-4 weeks earlier | Compare markdown start date before/after AI |
| **Elasticity Model Accuracy** | R² ≥ 0.70 | Model explains ≥70% of demand variance |
| **Merchandiser Adoption Rate** | ≥70% | % of markdown recommendations accepted |
| **A/B Test Success Rate** | ≥60% | Tests showing p-value < 0.05 improvement |
| **Competitive Price Parity** | CPI 95-105 for 80% SKUs | Monitor Competitive Price Index weekly |

---

## Next Steps

### Immediate Actions (Month 0)

1. **Executive Review & Approval**:
   - [ ] Present ADR-056 & ADR-057 to executive leadership
   - [ ] Secure budget approval ($500K-$1M for ADR-056, $300K-$500K for ADR-057)
   - [ ] Approve headcount (2-3 data scientists for ADR-056, 1-2 for ADR-057)
   - [ ] Set ROI targets ($5M-$8M ADR-056, $4M-$6M ADR-057)

2. **Technical Validation**:
   - [ ] Data Science Team: Validate forecasting methods, elasticity models, accuracy targets
   - [ ] Supply Chain Team: Review reorder point logic, multi-echelon optimization
   - [ ] Merchandising Team: Review markdown optimization, approval workflows
   - [ ] Architecture Team: Validate integration points, event schemas, data models

3. **Implementation Planning**:
   - [ ] Recruit data science talent (time series expertise, pricing optimization)
   - [ ] Set up MLOps infrastructure (MLflow, model versioning, A/B testing framework)
   - [ ] Prepare historical data (12-24 months sales, pricing, promotions, inventory)
   - [ ] Design buyer/merchandiser approval workflows (ADR-046 integration)

4. **Pilot Program**:
   - [ ] Select 10-20 pilot stores for ADR-056 (demand forecasting)
   - [ ] Define success metrics and tracking dashboards
   - [ ] Run 3-6 month pilot before chain-wide rollout
   - [ ] Gather buyer/merchandiser feedback and iterate

### Documentation Maintenance

- [ ] Update competitive comparison tables as new features launch
- [ ] Track ROI metrics monthly (forecast accuracy, stockout %, excess %, margin %)
- [ ] Document lessons learned during implementation
- [ ] Update ADRs with production deployment details and refinements

---

## References

### Created ADRs
- **ADR-056**: AI Demand Forecasting & Replenishment (`docs/adr/ADR-056-ai-demand-forecasting-replenishment.md`)
- **ADR-057**: Dynamic Pricing & Markdown Optimization (`docs/adr/ADR-057-dynamic-pricing-markdown-optimization.md`)

### Related ADRs
- **ADR-024**: Inventory Management (stock ledger, POS synchronization, advanced inventory ops)
- **ADR-025**: Sales & Distribution (pricing, promotions, orders)
- **ADR-023**: Procurement (purchase orders, supplier management)
- **ADR-055**: Workforce Scheduling & Labor Management (labor forecasting patterns)
- **ADR-016**: Analytics & Reporting Architecture (dashboards, metrics, KPIs)
- **ADR-046**: Workflow & Approval Engine (buyer/merchandiser approvals, task routing)
- **ADR-027**: Master Data Governance (data quality, SKU master, store master)
- **ADR-015**: Data Lifecycle Management (data retention, archiving)

### Roadmap Integration
- **Gap-to-SAP-Grade Roadmap**: `docs/architecture/gap-to-sap-grade-roadmap.md` (Phase 3.5: Retail AI Enhancement)

### External References
- **AI Strategy**: `docs/AI-STRATEGY.md` (ChiroERP AI principles, phasing)
- **SAP IBP**: Integrated Business Planning (demand forecasting, supply planning)
- **Oracle RDFC**: Retail Demand Forecasting Cloud
- **Blue Yonder Luminate**: AI-powered supply chain planning
- **SAP OPP**: Omnichannel Promotion Pricing
- **Oracle RPM**: Retail Price Management
