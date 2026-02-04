# ADR-057: Dynamic Pricing & Markdown Optimization

**Status**: Proposed (Phase 3 - Year 3)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Merchandising Team, Data Science Team
**Priority**: P2 (Medium)
**Tier**: Add-on (Advanced)
**Tags**: ai, ml, pricing, markdown, clearance, optimization, retail, revenue-management

## Context

Retail chains face significant margin erosion from ineffective pricing and markdown strategies. Without AI-powered pricing optimization, retailers experience:
- **10-15% excess inventory** at end of season requiring deep clearances (50-70% off).
- **3-5% gross margin erosion** from suboptimal markdown timing and depth.
- **Lost revenue** from static pricing that doesn't respond to demand, competition, or inventory levels.

While **ADR-025 (Sales & Distribution)** supports multi-tier pricing, discounts, and promotions, pricing decisions are **manual** and **reactive** (buyers set markdowns based on intuition, not data). This creates a competitive gap versus advanced retail systems (SAP OPP, Oracle RPM, Revionics, Competera).

**ADR-056 (Demand Forecasting)** is a **prerequisite** for this ADR — dynamic pricing requires accurate demand prediction to model price elasticity and optimize markdown schedules.

## Decision

Implement a **Dynamic Pricing & Markdown Optimization** add-on module that uses AI to recommend optimal prices, markdown timing, and discount depth to maximize gross margin while clearing seasonal inventory.

### Scope

**In Scope**:
- **Price Elasticity Modeling**: Understand how demand responds to price changes (1% price drop → X% sales increase).
- **Markdown Optimization**: Recommend optimal markdown schedule (when to discount) and depth (how much to discount).
- **Clearance Acceleration**: Identify slow-moving inventory requiring aggressive markdowns to avoid end-of-season liquidation.
- **Competitive Pricing Intelligence**: Monitor competitor prices and recommend competitive responses.
- **Promotion Effectiveness Analysis**: Measure ROI of promotions (did 25% off drive enough sales to justify margin loss?).
- **A/B Testing Framework**: Test pricing strategies across store clusters (control vs. treatment groups).
- **Revenue Management**: Balance occupancy (sell-through rate) vs. average selling price (ASP).

**Out of Scope** (Phase 1 / Future):
- **Real-time dynamic pricing** (Uber-style surge pricing) — too aggressive for most retail, regulatory risk.
- **Personalized pricing** (different prices per customer) — legal/ethical concerns (price discrimination).
- **Algorithmic collusion risk** — ensure pricing AI doesn't inadvertently coordinate with competitors.
- **Supplier/MSRP constraints** — must respect Minimum Advertised Price (MAP) agreements.

### Core Capabilities

#### 1. Price Elasticity Modeling

**Price Elasticity of Demand**:
```
Elasticity = % Change in Quantity Demanded / % Change in Price

Example:
- Price drops from $100 to $90 (-10%)
- Sales increase from 100 units to 120 units (+20%)
- Elasticity = +20% / -10% = -2.0 (elastic demand)
```

**Interpretation**:
- **Elastic (|Elasticity| > 1)**: Demand is sensitive to price (luxury goods, discretionary items). Discounts drive significant sales increase.
- **Inelastic (|Elasticity| < 1)**: Demand is insensitive to price (necessities, commodities). Discounts have limited sales impact.
- **Unit Elastic (|Elasticity| = 1)**: Revenue-neutral price change (rare).

**Data Requirements**:
- **Historical Pricing**: Price points over time per SKU-store.
- **Sales Volume**: Units sold at each price point.
- **Competitor Prices**: Competitor pricing for comparable products (web scraping, third-party data).
- **Promotions**: Flag promotional periods to isolate price effect vs. promotion effect.
- **External Factors**: Weather, events, holidays (control variables).

**Modeling Approach**:
- **Log-Log Regression**: `log(Quantity) = β0 + β1 × log(Price) + ε` → β1 is elasticity.
- **XGBoost/Random Forest**: Capture non-linear elasticity (elasticity changes at different price points).
- **Hierarchical Models**: Estimate elasticity per product category, then adjust per SKU.

**Example**:
```python
import numpy as np
from sklearn.ensemble import RandomForestRegressor

# Feature engineering
df['log_price'] = np.log(df['price'])
df['log_quantity'] = np.log(df['quantity_sold'])
df['competitor_price_diff'] = df['price'] - df['competitor_price']

# Train elasticity model
X = df[['log_price', 'competitor_price_diff', 'is_promo', 'day_of_week']]
y = df['log_quantity']

elasticity_model = RandomForestRegressor(n_estimators=100)
elasticity_model.fit(X, y)

# Predict demand at different price points
price_range = np.linspace(50, 150, 20)  # Test prices from $50 to $150
predicted_demand = []
for price in price_range:
    X_test = [[np.log(price), 0, 0, 5]]  # Friday, no promo, price parity
    predicted_demand.append(np.exp(elasticity_model.predict(X_test)[0]))

# Calculate revenue at each price point
revenue = price_range * predicted_demand
optimal_price = price_range[np.argmax(revenue)]
print(f"Optimal Price: ${optimal_price:.2f}")
```

---

#### 2. Markdown Optimization

**Problem**: When to discount slow-moving seasonal inventory to maximize gross margin?

**Markdown Strategies**:
- **Early Markdown (Weeks 1-4 of season)**: Small discount (15-25% off) to accelerate sales before inventory accumulates.
- **Mid-Season Markdown (Weeks 5-8)**: Moderate discount (30-40% off) to clear excess before peak demand ends.
- **Late-Season Clearance (Weeks 9-12)**: Deep discount (50-70% off) to liquidate remaining inventory before season ends.

**Optimization Objective**:
```
Maximize: Total Gross Margin

Gross Margin = Σ (Price × Quantity Sold) - Σ (Cost × Quantity Sold) - Holding Costs

Constraints:
- Inventory must reach ≤ 5% of peak by end of season (avoid end-of-season liquidation losses).
- Price cannot drop below cost (no loss-leaders unless strategic).
- Markdown depth ≤ 70% (maintain brand perception).
```

**Markdown Schedule Algorithm**:
1. **Forecast Remaining Demand**: Use ADR-056 demand forecasting to predict sales over remaining season.
2. **Calculate Excess Inventory**: `Excess = On-Hand Stock - Forecasted Demand`.
3. **Markdown Trigger**: If `Excess > 20%`, trigger markdown.
4. **Markdown Depth Calculation**:
   - Use elasticity model to estimate sales uplift at different discount levels.
   - Choose markdown depth that maximizes gross margin (balancing reduced price vs. increased volume).
5. **Iterative Markdowns**: Re-evaluate weekly → adjust markdown depth if sell-through is slower than expected.

**Example**:
```python
# Current state
on_hand_stock = 1000
weeks_remaining = 8
forecasted_demand = 600  # Without markdown

# Calculate excess
excess_pct = (on_hand_stock - forecasted_demand) / on_hand_stock
print(f"Excess Inventory: {excess_pct*100:.0f}%")

# Test markdown scenarios
markdown_depths = [0.15, 0.25, 0.35, 0.50]
original_price = 100
cost = 40

best_margin = 0
best_markdown = 0

for markdown in markdown_depths:
    discounted_price = original_price * (1 - markdown)
    # Estimate sales uplift from elasticity model
    uplift_factor = 1 + (markdown * 2.0)  # Assume elasticity = -2.0
    forecasted_sales = forecasted_demand * uplift_factor

    # Cap sales at available inventory
    actual_sales = min(forecasted_sales, on_hand_stock)

    # Calculate gross margin
    revenue = actual_sales * discounted_price
    cogs = actual_sales * cost
    holding_cost = (on_hand_stock - actual_sales) * 2  # $2 per unit holding cost
    gross_margin = revenue - cogs - holding_cost

    if gross_margin > best_margin:
        best_margin = gross_margin
        best_markdown = markdown

print(f"Optimal Markdown: {best_markdown*100:.0f}% off → ${original_price*(1-best_markdown):.2f}")
print(f"Expected Gross Margin: ${best_margin:,.0f}")
```

---

#### 3. Clearance Acceleration

**Problem**: Identify slow-moving inventory requiring aggressive markdowns **before** it becomes end-of-season dead stock.

**Slow-Mover Detection**:
- **Sell-Through Rate (STR)**: `STR = Units Sold / Units Received × 100%`
  - Target: **≥ 80% STR** by mid-season (Week 6-8).
  - If STR < 50% by Week 6 → high-risk slow mover.
- **Weeks of Supply (WOS)**: `WOS = On-Hand Stock / Average Weekly Sales`
  - Target: **≤ 4 weeks** for fashion, ≤ 8 weeks for basics.
  - If WOS > 10 weeks → liquidation candidate.
- **Age of Inventory**: Days since first receipt.
  - If age > 90 days and WOS > 8 weeks → clearance urgently.

**Clearance Recommendations**:
- **Moderate Risk (STR 50-70%)**: 25-35% markdown to accelerate sales.
- **High Risk (STR 30-50%)**: 40-50% markdown to clear aggressively.
- **Critical Risk (STR < 30%)**: 60-70% markdown or transfer to outlet stores / liquidation partners.

**Clearance Dashboard**:
- **Slow-Mover Heatmap**: Visualize SKU-store combinations with high WOS (red = urgent, yellow = monitor, green = healthy).
- **Markdown Recommendations**: AI-suggested markdown depth per SKU-store.
- **Sell-Through Tracking**: Monitor STR week-over-week to validate markdown effectiveness.

**Example**:
```python
# Slow-mover detection
df['weeks_of_supply'] = df['on_hand_stock'] / df['avg_weekly_sales']
df['sell_through_rate'] = df['units_sold'] / df['units_received'] * 100

# Flag high-risk inventory
df['risk_level'] = 'Low'
df.loc[df['weeks_of_supply'] > 8, 'risk_level'] = 'Moderate'
df.loc[df['weeks_of_supply'] > 12, 'risk_level'] = 'High'
df.loc[(df['weeks_of_supply'] > 15) | (df['sell_through_rate'] < 30), 'risk_level'] = 'Critical'

# Recommend markdown
df['recommended_markdown'] = 0.0
df.loc[df['risk_level'] == 'Moderate', 'recommended_markdown'] = 0.30
df.loc[df['risk_level'] == 'High', 'recommended_markdown'] = 0.50
df.loc[df['risk_level'] == 'Critical', 'recommended_markdown'] = 0.65

print(df[['sku_id', 'store_id', 'weeks_of_supply', 'sell_through_rate', 'risk_level', 'recommended_markdown']])
```

---

#### 4. Competitive Pricing Intelligence

**Use Case**: Monitor competitor prices and adjust pricing to maintain competitiveness without unnecessary margin erosion.

**Data Sources**:
- **Web Scraping**: Automated scraping of competitor websites (ethical, public data only).
- **Third-Party Data Providers**: PriceSpider, Keepa, Prisync (subscription services).
- **Price Matching Policies**: If competitor drops price, match within 24 hours (configurable rules).

**Competitive Price Index (CPI)**:
```
CPI = (Your Price / Competitor Average Price) × 100

Interpretation:
- CPI = 100: Price parity with competitors
- CPI > 105: Priced 5% higher (risk of losing price-sensitive customers)
- CPI < 95: Priced 5% lower (potential to raise prices, capture margin)
```

**Competitive Response Rules**:
- **Rule 1**: If CPI > 110 for high-visibility SKUs (top 20% revenue), trigger price review.
- **Rule 2**: If competitor runs promotion (e.g., 30% off), evaluate match/beat within 48 hours.
- **Rule 3**: For commodity SKUs (low differentiation), maintain CPI 95-105 (price parity).
- **Rule 4**: For differentiated SKUs (exclusive brands), allow CPI 105-115 (premium pricing acceptable).

**Example**:
```python
# Competitive pricing analysis
df['competitor_avg_price'] = df[['competitor1_price', 'competitor2_price', 'competitor3_price']].mean(axis=1)
df['cpi'] = (df['our_price'] / df['competitor_avg_price']) * 100

# Flag overpriced SKUs
df['overpriced'] = df['cpi'] > 110

# Recommend price adjustments
df['recommended_price'] = df['our_price']
df.loc[df['overpriced'], 'recommended_price'] = df['competitor_avg_price'] * 1.05  # Match +5%

print(df[['sku_id', 'our_price', 'competitor_avg_price', 'cpi', 'recommended_price']])
```

---

#### 5. Promotion Effectiveness Analysis

**Problem**: Measure ROI of promotions (BOGO, 25% off, bundle offers) to optimize future promotional spend.

**Promotion Metrics**:
- **Incremental Sales**: Sales during promotion - baseline sales (without promotion).
- **Incremental Revenue**: Incremental sales × discounted price.
- **Incremental Margin**: Incremental revenue - incremental COGS.
- **Promotion ROI**: `ROI = Incremental Margin / Promotion Cost × 100%`
  - Promotion costs: Marketing spend, signage, staff labor for setup.

**Cannibalization Analysis**:
- Did promotion steal sales from full-price products?
- Example: BOGO on Brand A → customers switch from Brand B (cannibalization).

**Halo Effect Analysis**:
- Did promotion drive complementary product sales?
- Example: 50% off chips → increases soda sales (halo effect).

**Promotion Lifecycle**:
1. **Pre-Promotion**: Forecast baseline sales (without promotion).
2. **During Promotion**: Track actual sales, compare vs. baseline.
3. **Post-Promotion**: Measure post-promotion slump (customers stockpiled during promotion → sales drop after).
4. **Net Lift**: `Net Lift = (During Promotion Sales + Post-Promotion Sales) - (Baseline × Promotion Duration + X weeks post)`.

**Example**:
```python
# Promotion effectiveness analysis
baseline_sales = 1000  # Units per week without promotion
promo_duration_weeks = 2
promo_sales_week1 = 3000
promo_sales_week2 = 2500
post_promo_sales_week1 = 500  # Slump after promotion
post_promo_sales_week2 = 800

# Calculate incremental sales
incremental_sales = (promo_sales_week1 + promo_sales_week2) - (baseline_sales * promo_duration_weeks)
post_promo_loss = (baseline_sales * 2) - (post_promo_sales_week1 + post_promo_sales_week2)
net_incremental_sales = incremental_sales - post_promo_loss

print(f"Incremental Sales During Promo: {incremental_sales} units")
print(f"Post-Promotion Slump: {post_promo_loss} units")
print(f"Net Incremental Sales: {net_incremental_sales} units")

# Calculate ROI
original_price = 10
discounted_price = 7.50
cost = 4
promo_marketing_cost = 5000

incremental_revenue = net_incremental_sales * discounted_price
incremental_cogs = net_incremental_sales * cost
incremental_margin = incremental_revenue - incremental_cogs
promo_roi = (incremental_margin - promo_marketing_cost) / promo_marketing_cost * 100

print(f"Promotion ROI: {promo_roi:.1f}%")
```

---

#### 6. A/B Testing Framework

**Use Case**: Test pricing strategies across store clusters to validate AI recommendations before chain-wide rollout.

**A/B Test Design**:
- **Control Group**: 20-30 stores maintain current pricing strategy.
- **Treatment Group**: 20-30 stores implement AI-recommended pricing.
- **Matching**: Ensure control and treatment groups are similar (sales volume, demographics, geography).
- **Duration**: Run test for 4-8 weeks (capture at least one sales cycle).
- **Metrics**: Compare revenue, gross margin, units sold, customer traffic between groups.

**Statistical Validation**:
- **Hypothesis**: AI pricing increases gross margin vs. current pricing.
- **T-test**: Test if difference in gross margin is statistically significant (p-value < 0.05).
- **Confidence Interval**: Calculate 95% confidence interval for margin improvement.

**Rollout Decision**:
- If treatment group outperforms control by ≥ 2% gross margin with p-value < 0.05 → roll out to all stores.
- If no significant difference → investigate why (model accuracy issues? External factors?).

**Example**:
```python
from scipy.stats import ttest_ind

# A/B test results
control_margin = [42, 41, 43, 40, 42, 41, 43, 42]  # % gross margin per store
treatment_margin = [45, 44, 46, 45, 44, 46, 45, 44]  # AI pricing

# T-test
t_stat, p_value = ttest_ind(treatment_margin, control_margin)

print(f"Control Avg Margin: {np.mean(control_margin):.1f}%")
print(f"Treatment Avg Margin: {np.mean(treatment_margin):.1f}%")
print(f"Margin Improvement: +{np.mean(treatment_margin) - np.mean(control_margin):.1f}%")
print(f"P-value: {p_value:.4f}")

if p_value < 0.05:
    print("Result: Statistically significant → Recommend rollout")
else:
    print("Result: Not significant → Do not roll out")
```

---

#### 7. Revenue Management (Occupancy vs. Average Selling Price)

**Trade-off**: Higher prices → higher margin per unit, but lower sales volume. Lower prices → higher volume, but lower margin.

**Revenue Management Goal**: Maximize `Total Gross Margin = (Price - Cost) × Quantity Sold`

**Optimal Price**:
```
Optimal Price = Cost + (1 / |Elasticity|) × Price

Example:
- Cost = $40
- Elasticity = -2.0 (elastic demand)
- Optimal Price = $40 + (1 / 2.0) × Price
  → Price - 0.5 × Price = $40
  → 0.5 × Price = $40
  → Price = $80
```

**Dynamic Pricing by Inventory Level**:
- **High Inventory (>150% of forecast)**: Lower prices to accelerate sell-through (prioritize occupancy).
- **Normal Inventory (90-110% of forecast)**: Maintain target prices (balance occupancy and ASP).
- **Low Inventory (<70% of forecast)**: Raise prices to capture margin (prioritize ASP).

**Demand-Based Pricing**:
- **Peak Demand Periods** (weekends, holidays): Raise prices slightly (customers less price-sensitive).
- **Off-Peak Periods** (weekdays, slow seasons): Lower prices to drive traffic.

**Example**:
```python
# Revenue management pricing
on_hand_stock = 1200
forecasted_demand = 1000
stock_coverage = on_hand_stock / forecasted_demand

base_price = 100
cost = 40
elasticity = -2.0

# Adjust price based on inventory level
if stock_coverage > 1.5:
    # Excess inventory → lower price
    price_adjustment = -0.10  # -10%
elif stock_coverage < 0.7:
    # Low inventory → raise price
    price_adjustment = +0.10  # +10%
else:
    # Normal inventory → no adjustment
    price_adjustment = 0

recommended_price = base_price * (1 + price_adjustment)
print(f"Stock Coverage: {stock_coverage:.1%}")
print(f"Recommended Price: ${recommended_price:.2f}")
```

---

### Data Model (Conceptual)

**Core Entities**:
- `PriceElasticity`: sku_id, category_id, elasticity, confidence_interval, last_updated.
- `MarkdownRecommendation`: sku_id, store_id, current_price, recommended_price, markdown_depth, reason (slow_mover, clearance, competitive), expected_uplift, expected_margin.
- `CompetitorPrice`: competitor_id, sku_id, price, date, source (web_scrape, manual, third_party).
- `PromotionROI`: promo_id, sku_id, store_id, baseline_sales, promo_sales, post_promo_sales, net_incremental_sales, incremental_margin, roi.
- `ABTestExperiment`: experiment_id, test_name, control_stores, treatment_stores, start_date, end_date, metric (gross_margin, revenue), control_avg, treatment_avg, p_value, decision (rollout, reject).
- `PriceHistory`: sku_id, store_id, date, price, quantity_sold, competitor_avg_price, cpi.

**Relationships**:
- `MarkdownRecommendation` → `PriceElasticity` (use elasticity to estimate sales uplift).
- `MarkdownRecommendation` → `DemandForecast` (ADR-056, forecast excess inventory).
- `CompetitorPrice` → `PriceHistory` (compare our price vs. competitor).
- `ABTestExperiment` → `MarkdownRecommendation` (test AI recommendations).

---

### Key Workflows

#### Workflow 1: Weekly Markdown Recommendations
1. **Excess Inventory Detection**: Identify SKU-stores with WOS > 8 weeks or STR < 70%.
2. **Demand Forecast**: Use ADR-056 to forecast remaining sales over next 8-12 weeks.
3. **Elasticity Lookup**: Retrieve price elasticity for SKU/category.
4. **Markdown Optimization**: Calculate optimal markdown depth to maximize gross margin.
5. **Recommendation Generation**: Create `MarkdownRecommendation` records.
6. **Approval Workflow**: Route to merchandisers for review (ADR-046 Workflow Engine).
7. **Price Update**: If approved, update pricing in ADR-025 (Sales & Distribution).

#### Workflow 2: Competitive Pricing Monitoring
1. **Daily Competitor Scrape**: Fetch competitor prices via API/web scraping.
2. **Price Comparison**: Calculate CPI per SKU (our price vs. competitor average).
3. **Threshold Detection**: Flag SKUs with CPI > 110 (overpriced) or CPI < 90 (underpriced).
4. **Price Adjustment Recommendation**: Calculate recommended price (match +5% or -5%).
5. **Alert Generation**: Notify pricing team of high-priority mismatches.
6. **Rule-Based Auto-Adjustment**: If configured, automatically adjust prices within ±5% tolerance.

#### Workflow 3: Promotion Effectiveness Analysis (Post-Promotion)
1. **Promotion End**: Detect promotion end date (from ADR-025).
2. **Sales Data Collection**: Gather sales during promotion and 2-4 weeks post-promotion.
3. **Baseline Calculation**: Use ADR-056 to estimate what sales would have been without promotion.
4. **Incremental Lift**: Calculate incremental sales, revenue, margin.
5. **Cannibalization Analysis**: Check if other SKU sales dropped during promotion.
6. **ROI Calculation**: `ROI = (Incremental Margin - Promotion Cost) / Promotion Cost × 100%`.
7. **Reporting**: Generate promotion performance report for marketing/merchandising teams.

#### Workflow 4: A/B Test Execution
1. **Test Design**: Define control/treatment stores, pricing strategy, duration, success metrics.
2. **Store Assignment**: Randomly assign matched stores to control/treatment groups.
3. **Price Deployment**: Apply AI pricing to treatment stores, maintain current pricing in control.
4. **Data Collection**: Track sales, margin, traffic daily during test period.
5. **Statistical Analysis**: After 4-8 weeks, run t-test to compare groups.
6. **Decision**: If treatment significantly outperforms control → recommend rollout.
7. **Rollout**: Deploy AI pricing chain-wide, monitor for regression.

---

### Integration Points

**Core Integrations**:
- **Demand Forecasting (ADR-056)**: **Critical dependency** — pricing optimization requires accurate demand forecasts.
- **Sales & Distribution (ADR-025)**: Read current prices, promotions. Write price updates, markdown schedules.
- **Inventory (ADR-024)**: Read on-hand stock, WOS, STR. Identify slow-moving inventory.
- **Analytics (ADR-016)**: Provide pricing dashboards, elasticity reports, promotion ROI analysis.
- **Workflow Engine (ADR-046)**: Route markdown recommendations for merchandiser approval.

**External Integrations**:
- **Competitor Price Data**: Web scraping tools, third-party APIs (PriceSpider, Keepa).
- **Promotion Planning Tools**: Integration with marketing automation (Salesforce Marketing Cloud).

**Event-Driven Architecture**:
- **Consumes**:
  - `DemandForecastGeneratedEvent` (ADR-056) → Trigger markdown optimization.
  - `InventoryStockUpdatedEvent` (ADR-024) → Recalculate WOS, STR.
  - `PromotionEndedEvent` (ADR-025) → Trigger promotion effectiveness analysis.
- **Publishes**:
  - `MarkdownRecommendationCreatedEvent` → Notify merchandisers.
  - `CompetitivePriceAlertEvent` → Notify pricing team of price mismatches.
  - `PromotionROICalculatedEvent` → Notify marketing team of promotion results.

---

### Non-Functional Constraints

**Performance**:
- **Markdown Recommendations**: ≤ 10 minutes to generate recommendations for 500 stores × 5,000 SKUs = 2.5M SKU-stores.
- **Competitive Price Monitoring**: ≤ 5 minutes to scrape and analyze 10,000 SKUs across 3-5 competitors.
- **Price Elasticity Calculation**: ≤ 30 seconds per SKU/category (batch processing nightly).

**Accuracy Targets**:
- **Elasticity Model**: R² ≥ 0.70 (explains 70% of demand variance).
- **Margin Improvement**: ≥ 2% gross margin improvement vs. manual pricing.
- **Clearance Reduction**: ≥ 5-10% reduction in end-of-season liquidation inventory.

**Scalability**:
- Support 500 stores × 10,000 SKUs = 5M price points.
- Handle 10 concurrent A/B tests (treatment groups, control groups).

**Data Retention**:
- **Price History**: Retain 3 years for elasticity modeling.
- **Competitor Prices**: Retain 1 year for competitive analysis.
- **Promotion ROI**: Retain 2 years for historical comparison.

---

## Alternatives Considered

### 1. Manual Markdown Decisions (Current State)
- **Approach**: Merchandisers manually set markdowns based on intuition and past experience.
- **Pros**: Simple, merchandisers understand their products.
- **Cons**: Time-consuming, inconsistent across stores, reactive (markdowns too late → end-of-season liquidation losses).
- **Decision**: **Rejected** — Results in 3-5% margin erosion, 10-15% excess inventory.

### 2. Rule-Based Markdown (No AI)
- **Approach**: Fixed rules (e.g., "If WOS > 10 weeks, apply 30% markdown").
- **Pros**: Simple to implement, deterministic.
- **Cons**: Ignores elasticity (30% may be too much or too little), doesn't optimize margin.
- **Decision**: **Rejected** — Better than manual, but suboptimal vs. AI (leaves 1-2% margin on table).

### 3. External Pricing Platform (Revionics, Competera, Price f(x))
- **Approach**: Integrate with third-party pricing optimization SaaS.
- **Pros**: Best-in-class algorithms, proven at scale (Macy's, Kohl's use Revionics).
- **Cons**: Expensive ($300K-$1M annual licensing), complex integration, vendor lock-in.
- **Decision**: **Rejected for MVP** — Too expensive for mid-market. However, provide integration option for large enterprises.

### 4. Build In-House Pricing AI
- **Approach**: Build custom pricing optimization platform (Python, elasticity models, markdown optimization).
- **Pros**: Full control, customizable, no vendor lock-in.
- **Cons**: High development cost ($300K-$500K), 6-9 months to production, requires data science expertise.
- **Decision**: **Accepted with Phased Approach** — Start with elasticity modeling and markdown optimization (Phase 1), add advanced optimization (Phase 2).

---

## Consequences

### Positive
- **Gross Margin Improvement**: 2-3% increase from optimized markdown timing and depth.
- **Clearance Reduction**: 5-10% reduction in end-of-season liquidation inventory (less deep markdowns).
- **Competitive Responsiveness**: Faster response to competitor pricing changes (automated alerts, recommendations).
- **Promotion Optimization**: Data-driven promotion planning (invest in high-ROI promotions, cut low-ROI).
- **Merchandiser Productivity**: Automated recommendations free merchandisers to focus on assortment, vendor negotiations.

### Negative / Risks
- **Elasticity Estimation Error**: If elasticity is miscalculated, pricing recommendations may be suboptimal.
  - **Mitigation**: Validate elasticity with A/B tests, update models monthly with fresh data.
- **Competitive Reaction**: If competitors also use pricing AI, may trigger price wars (race to the bottom).
  - **Mitigation**: Set price floors (don't drop below cost + minimum margin), focus on differentiation (not just price).
- **Brand Perception Risk**: Frequent markdowns may train customers to wait for sales (devalue full-price perception).
  - **Mitigation**: Limit markdown frequency (max 2-3 markdowns per season), maintain premium positioning for key brands.
- **Regulatory Risk**: Dynamic pricing may face scrutiny (price discrimination concerns, algorithmic collusion).
  - **Mitigation**: Ensure pricing is transparent, document rationale, avoid personalized pricing.

### Neutral
- **Data Dependency**: Requires 12+ months historical pricing and sales data for accurate elasticity modeling.
- **Integration Complexity**: Deep integration with ADR-025 (pricing engine), ADR-056 (demand forecasting).
- **Change Management**: Merchandisers must trust AI recommendations (may resist initially).

---

## Success Metrics (12 Months Post-Launch)

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Gross Margin Improvement** | **+2-3%** | Compare gross margin % (before vs. after AI pricing) |
| **Clearance Inventory Reduction** | **≥ 5-10% reduction** | Measure units >90 days old at end of season |
| **Markdown Timing Improvement** | **Markdown 2-4 weeks earlier** | Compare markdown date (AI vs. manual historical) |
| **Merchandiser Adoption Rate** | **≥ 70%** | % of markdown recommendations accepted |
| **Elasticity Model Accuracy** | **R² ≥ 0.70** | Elasticity model explains ≥70% of demand variance |
| **A/B Test Success Rate** | **≥ 60% of tests show significant improvement** | T-test p-value < 0.05 |
| **Competitive Price Parity** | **CPI 95-105 for 80% of SKUs** | Monitor competitive price index |

---

## Compliance

- **Price Discrimination Laws**: Ensure pricing is uniform within a geographic region (no personalized pricing based on customer demographics).
- **Minimum Advertised Price (MAP)**: Respect supplier MAP agreements (don't drop below MAP for restricted brands).
- **Algorithmic Transparency**: Document pricing logic for regulatory audits (no "black box" AI).
- **Anti-Collusion**: Ensure pricing AI does not coordinate with competitor pricing algorithms.

---

## Implementation Plan

### Phase 1 (Months 1-3): Price Elasticity & Basic Markdown Optimization
**Scope**:
- Historical price/sales data collection (12+ months).
- Price elasticity modeling (log-log regression, XGBoost).
- Basic markdown optimization (WOS-based, rule-enhanced).
- Competitive pricing dashboard (manual data entry initially).

**Deliverables**:
- [ ] Elasticity model (per SKU/category)
- [ ] Markdown recommendation engine (weekly batch)
- [ ] Competitive price comparison dashboard
- [ ] Merchandiser approval workflow integration (ADR-046)

**Target**: 1-2% gross margin improvement (conservative baseline).

---

### Phase 2 (Months 4-6): Advanced Optimization & A/B Testing
**Scope**:
- Promotion effectiveness analysis (incremental lift, ROI).
- Dynamic markdown schedules (multi-stage markdowns optimized over season).
- A/B testing framework (control vs. treatment store groups).
- Automated competitor price scraping (web scraping, APIs).

**Deliverables**:
- [ ] Promotion ROI dashboard
- [ ] Dynamic markdown optimizer (multi-stage)
- [ ] A/B test orchestration platform
- [ ] Competitor price scraper (automated daily)

**Target**: 2-3% gross margin improvement (optimized recommendations).

---

### Phase 3 (Months 7-9): Real-Time Pricing & Advanced Analytics
**Scope**:
- Real-time pricing API (respond to competitor price changes within hours).
- Revenue management (balance occupancy vs. ASP dynamically).
- Advanced analytics (cannibalization, halo effect, cross-elasticity).
- Integration with marketing automation (promotion planning).

**Deliverables**:
- [ ] Real-time pricing engine (<1 hour response time)
- [ ] Revenue management optimizer
- [ ] Advanced promotion analytics (cannibalization, halo)
- [ ] Marketing automation integration (Salesforce Marketing Cloud)

**Target**: 3-4% gross margin improvement (real-time optimization).

---

## References

### Related ADRs
- **ADR-056: AI Demand Forecasting & Replenishment** (PREREQUISITE — demand forecasts required for pricing optimization)
- ADR-025: Sales & Distribution (pricing engine, promotions, discounts)
- ADR-024: Inventory Management (stock levels, WOS, STR)
- ADR-016: Analytics & Reporting Architecture (pricing dashboards, elasticity reports)
- ADR-046: Workflow & Approval Engine (merchandiser approval for markdown recommendations)
- ADR-027: Master Data Governance (SKU master, pricing master)

### Internal Documentation
- `docs/AI-STRATEGY.md` (ChiroERP AI strategy and phasing)
- `docs/architecture/retail/RETAIL-AI-ASSESSMENT.md` (retail AI gap analysis)

### External References
- **Dynamic Pricing Platforms**:
  - Revionics (Oracle): https://www.oracle.com/retail/price-management/
  - Competera: https://competera.ai/
  - Price f(x): https://www.pricefx.com/
- **Price Elasticity**:
  - Economics textbook: https://www.investopedia.com/terms/p/priceelasticity.asp
  - Elasticity modeling tutorial: https://towardsdatascience.com/price-elasticity-of-demand-in-python-8f5e8c6f1d3f
- **Revenue Management**:
  - "The Theory and Practice of Revenue Management" (Talluri & Van Ryzin)
  - Airline/hotel revenue management: https://en.wikipedia.org/wiki/Revenue_management
- **Retail Pricing**:
  - SAP Omnichannel Promotion Pricing: https://www.sap.com/products/retail-solutions/omnichannel-promotion-pricing.html
  - Oracle Retail Price Management: https://www.oracle.com/retail/price-management/

---

## Decision Log

| Date | Author | Decision |
|------|--------|----------|
| 2026-02-03 | Architecture Team | Initial draft created based on retail AI gap analysis |
| TBD | Merchandising Team | Review markdown optimization logic and approval workflow |
| TBD | Data Science Team | Validate elasticity modeling approach and accuracy targets |
| TBD | Legal/Compliance | Review pricing AI for regulatory compliance (price discrimination, MAP) |
| TBD | Executive Leadership | Approve Phase 1 budget ($300K-$500K) and 1-2 data scientist headcount |
