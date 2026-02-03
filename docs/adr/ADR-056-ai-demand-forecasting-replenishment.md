# ADR-056: AI Demand Forecasting & Replenishment

**Status**: Proposed (Phase 2 - Year 2)  
**Date**: 2026-02-03  
**Deciders**: Architecture Team, Supply Chain Team, Data Science Team  
**Priority**: P1 (High)  
**Tier**: Add-on (Advanced)  
**Tags**: ai, ml, demand-forecasting, replenishment, inventory-optimization, retail, time-series

## Context

Sophisticated multi-branch retail chains require AI-powered demand forecasting to optimize inventory replenishment, reduce stockouts, and minimize excess inventory. Without demand forecasting AI, retailers experience 15-20% stockouts (lost sales) and 25-30% excess inventory (markdown losses). 

While **ADR-024 (Inventory Management)** provides real-time stock visibility and **ADR-055 (Workforce Scheduling)** includes labor demand forecasting, there is **no AI capability for product-level demand forecasting and automatic replenishment recommendations**. This creates a competitive gap versus enterprise retail systems (SAP IBP, Oracle RDFC, Blue Yonder).

## Decision

Implement an **AI Demand Forecasting & Replenishment** add-on module that provides SKU-store level demand prediction, automatic reorder point recommendations, and multi-echelon inventory optimization using time series forecasting and machine learning.

### Scope

**In Scope**:
- **Product-Level Demand Forecasting**: Predict daily/weekly sales per SKU per store.
- **Seasonality Detection**: Weekly, monthly, annual cycles (e.g., ice cream peaks in summer).
- **Promotion Impact Modeling**: Forecasted sales uplift for promotions (BOGO, discounts).
- **External Signal Integration**: Weather, events, holidays, competitor activity.
- **Automatic Reorder Points**: AI-recommended min/max stock levels per SKU-store.
- **Multi-Echelon Optimization**: Distribution center → store network optimization.
- **Safety Stock Calculations**: Based on demand volatility and lead time variability.
- **Forecast Accuracy Monitoring**: MAPE, MAE, RMSE tracking with model retraining.
- **What-If Analysis**: Simulate promotion impact, new store openings, supply disruptions.

**Out of Scope** (Phase 1 / Future):
- Real-time demand sensing (POS → immediate forecast updates) — Phase 3 enhancement.
- Assortment optimization (which SKUs to carry per store) — separate ADR.
- Supplier collaboration (CPFR - Collaborative Planning, Forecasting, Replenishment).
- Advanced optimization (genetic algorithms, reinforcement learning) — Phase 3.

### Core Capabilities

#### 1. Time Series Forecasting Engine

**Forecasting Methods** (Progressive Enhancement):

**Phase 1 (MVP)**: Classical Time Series
- **ARIMA** (AutoRegressive Integrated Moving Average): Handles trend and seasonality
- **Prophet** (Facebook): Robust to missing data, multiple seasonality (daily, weekly, annual)
- **Exponential Smoothing**: Simple baseline (Holt-Winters for trend/seasonality)

**Phase 2 (Advanced)**: Machine Learning
- **XGBoost/LightGBM**: Gradient boosting with feature engineering (day of week, month, promo flags)
- **LSTM** (Long Short-Term Memory): Deep learning for complex patterns
- **Transformer Models**: Attention-based forecasting (TimesFM, Temporal Fusion Transformer)

**Model Selection Logic**:
- **Short history (<6 months)**: Use exponential smoothing or Prophet (robust to limited data)
- **Medium history (6-24 months)**: Use ARIMA or XGBoost (balance accuracy and interpretability)
- **Long history (>24 months)**: Use LSTM or Transformer (capture complex patterns)

**Ensemble Approach**:
- Combine multiple models (ARIMA + Prophet + XGBoost) with weighted averaging
- Use recent performance (last 4 weeks) to adjust weights dynamically

---

#### 2. Seasonality Detection & Modeling

**Seasonality Types**:
- **Weekly Seasonality**: Weekend vs. weekday patterns (retail stores)
- **Monthly Seasonality**: Paycheck cycles (1st and 15th of month)
- **Annual Seasonality**: Holiday peaks (Christmas, Black Friday, back-to-school)
- **Event-Based Seasonality**: Sports events, concerts, local festivals

**Implementation**:
- **STL Decomposition** (Seasonal and Trend decomposition using Loess): Separate trend, seasonality, noise
- **Fourier Series**: Capture multiple seasonality periods (daily + weekly + annual)
- **Calendar Effects**: Model specific dates (Black Friday, Cyber Monday, Easter)

**Example**:
```python
# Detect seasonality using STL decomposition
from statsmodels.tsa.seasonal import STL

stl = STL(sales_data, seasonal=7)  # Weekly seasonality
result = stl.fit()

trend = result.trend
seasonal = result.seasonal
residual = result.resid
```

---

#### 3. Promotion Impact Modeling

**Promotion Types**:
- **Price Discounts**: 10% off, 25% off, 50% clearance
- **BOGO**: Buy one get one free (2x sales uplift typical)
- **Bundle Offers**: Buy 3 get 1 free (cross-product impact)
- **Seasonal Promotions**: Back-to-school, Black Friday, Valentine's Day

**Promotion Features**:
- `is_promo` (binary flag: yes/no)
- `promo_type` (discount, BOGO, bundle)
- `promo_depth` (percentage: 10%, 25%, 50%)
- `promo_duration` (days: 1-day flash sale vs. 2-week campaign)

**Uplift Modeling**:
- **Historical Uplift**: Calculate average sales increase during past promotions
- **Elasticity Curves**: Model price sensitivity (1% price drop → X% sales increase)
- **Halo Effect**: Capture impact on complementary products (chips sales increase with soda promotions)

**Example**:
```python
# Promotion impact feature engineering
forecast_df['is_promo'] = forecast_df['date'].isin(promo_dates)
forecast_df['promo_uplift'] = forecast_df['is_promo'] * 2.5  # 2.5x baseline sales during BOGO

# Train model with promotion features
model = XGBRegressor()
model.fit(X_train[['day_of_week', 'month', 'is_promo', 'promo_depth']], y_train)
```

---

#### 4. External Signal Integration

**Weather Signals**:
- **Temperature**: Ice cream sales correlate with heat (>25°C)
- **Precipitation**: Umbrella/raincoat sales spike on rainy days
- **Snow**: Winter gear, shovels, salt sales increase during snowstorms

**Event Signals**:
- **Sports Events**: Beer, snacks sales increase near stadiums on game days
- **Concerts/Festivals**: Local sales uplift (hotels, restaurants, retail)
- **Holidays**: Valentine's Day (flowers, chocolates), Halloween (costumes, candy)

**Competitor Signals**:
- **Competitor Promotions**: If competitor has 30% off sale, our sales may drop
- **New Store Openings**: Competitor opens nearby → cannibalization risk

**Data Sources**:
- Weather: OpenWeather API, Weather.com API
- Events: Eventbrite API, Google Calendar API, local event databases
- Competitors: Web scraping (ethical, public data only), third-party competitive intelligence

**Example**:
```python
# Weather feature engineering
forecast_df['temp_celsius'] = weather_api.get_temperature(store_location, date)
forecast_df['is_rainy'] = weather_api.get_precipitation(store_location, date) > 0

# Event feature engineering
forecast_df['is_game_day'] = check_sports_events(store_location, date)
forecast_df['is_holiday'] = check_holidays(date, country='US')

# Train model with external signals
model.fit(X_train[['temp_celsius', 'is_rainy', 'is_game_day', 'is_holiday']], y_train)
```

---

#### 5. Automatic Reorder Point Recommendations

**Reorder Point Formula**:
```
Reorder Point (ROP) = (Average Daily Demand × Lead Time Days) + Safety Stock
```

**Safety Stock Calculation**:
```
Safety Stock = Z-score × StdDev(Daily Demand) × √Lead Time Days

Where:
- Z-score = Service level (e.g., 1.65 for 95% in-stock, 2.33 for 99%)
- StdDev(Daily Demand) = Standard deviation of forecasted demand
- Lead Time Days = Supplier lead time + internal processing time
```

**Dynamic Reorder Points**:
- **Seasonal Adjustment**: Higher ROP before peak season (e.g., increase ice cream ROP in May)
- **Promotion Adjustment**: Increase ROP before planned promotions (BOGO requires 2x stock)
- **Lead Time Variability**: If supplier is unreliable, increase safety stock

**Min/Max Inventory Policies**:
- **Min (Reorder Point)**: Trigger replenishment when stock drops below ROP
- **Max (Order-Up-To Level)**: Target stock level after replenishment
  - `Max = ROP + Economic Order Quantity (EOQ)`
  - EOQ balances ordering costs and holding costs

**Example**:
```python
# Calculate reorder point
avg_daily_demand = forecast_df['predicted_sales'].mean()
std_daily_demand = forecast_df['predicted_sales'].std()
lead_time_days = 7  # Supplier lead time
service_level_z = 1.65  # 95% in-stock target

reorder_point = (avg_daily_demand * lead_time_days) + \
                (service_level_z * std_daily_demand * math.sqrt(lead_time_days))

print(f"Reorder Point: {reorder_point:.0f} units")
```

---

#### 6. Multi-Echelon Optimization

**Problem**: How to allocate inventory across distribution center (DC) and stores?

**Constraints**:
- DC capacity limits (pallet positions, cubic feet)
- Store capacity limits (shelf space, backroom space)
- Transportation costs (DC → store shipping costs)
- Service level targets (95% in-stock at store level)

**Optimization Objective**:
```
Minimize: Total Inventory Holding Cost + Stockout Cost + Transportation Cost

Subject to:
- DC stock ≥ Sum(Store Replenishments)
- Store stock ≥ Reorder Point (safety stock constraint)
- DC capacity ≤ Max Capacity
- Store capacity ≤ Max Capacity
```

**Allocation Logic** (Heuristic):
1. **Forecast Aggregation**: Sum store-level forecasts → DC demand forecast
2. **DC Stock Positioning**: DC holds 1-2 weeks of aggregate demand + safety stock
3. **Store Replenishment**: Push inventory to stores based on forecasted demand (pull-based replenishment)
4. **Emergency Transfers**: Allow store-to-store transfers for fast-moving items

**Advanced** (Phase 2):
- **Risk Pooling**: Centralize slow-movers at DC, distribute fast-movers to stores
- **Postponement**: Hold generic inventory at DC, customize at store (e.g., gift wrapping)

---

#### 7. Forecast Accuracy Monitoring & Model Retraining

**Forecast Accuracy Metrics**:

**MAPE (Mean Absolute Percentage Error)**:
```
MAPE = (1/n) × Σ |Actual - Forecast| / Actual × 100%

Target: ≤ 15% (good), ≤ 10% (excellent)
```

**MAE (Mean Absolute Error)**:
```
MAE = (1/n) × Σ |Actual - Forecast|

Units: Same as sales (e.g., units sold)
```

**RMSE (Root Mean Squared Error)**:
```
RMSE = √[(1/n) × Σ (Actual - Forecast)²]

Penalizes large errors more than MAE
```

**Monitoring Dashboard**:
- **Overall Accuracy**: MAPE across all SKU-stores
- **Per-SKU Accuracy**: Identify problematic SKUs (high forecast error)
- **Per-Store Accuracy**: Identify problematic stores (new store, unusual patterns)
- **Time-Based Accuracy**: Track accuracy over time (model degradation?)

**Model Retraining Triggers**:
- **Scheduled**: Retrain weekly (fast-moving items) or monthly (slow-moving items)
- **Accuracy Degradation**: If MAPE increases >5% over baseline, retrain immediately
- **Data Drift**: If recent sales patterns change significantly (e.g., new competitor opens)
- **New Promotions**: Retrain after major promotions to capture updated uplift factors

**Example**:
```python
# Calculate MAPE
def calculate_mape(actual, forecast):
    return np.mean(np.abs((actual - forecast) / actual)) * 100

mape = calculate_mape(actual_sales, forecasted_sales)
print(f"Forecast MAPE: {mape:.2f}%")

# Retraining logic
if mape > 20:  # Accuracy degraded
    print("Triggering model retraining...")
    retrain_model(sku_id, store_id)
```

---

#### 8. What-If Analysis & Scenario Planning

**Use Cases**:
- **Promotion Planning**: "What if we run 25% off promotion for 2 weeks?"
- **New Store Opening**: "What if we open 5 new stores in this region?"
- **Supply Disruption**: "What if supplier lead time increases from 7 to 14 days?"
- **Competitor Activity**: "What if competitor launches aggressive pricing?"

**Implementation**:
- **Scenario Inputs**: User adjusts assumptions (promotion depth, lead time, external events)
- **Forecast Simulation**: Re-run forecasting model with adjusted inputs
- **Impact Analysis**: Compare baseline forecast vs. scenario forecast
  - **Demand Impact**: Sales increase/decrease
  - **Inventory Impact**: Stock requirements change
  - **Financial Impact**: Revenue, margin, inventory holding costs

**Example**:
```python
# Baseline forecast
baseline_forecast = forecast_model.predict(X_baseline)

# Scenario: 30% off promotion for 2 weeks
scenario_df = X_baseline.copy()
scenario_df['is_promo'] = True
scenario_df['promo_depth'] = 0.30
scenario_df['promo_duration'] = 14

scenario_forecast = forecast_model.predict(scenario_df)

# Compare impact
demand_uplift = scenario_forecast.sum() - baseline_forecast.sum()
print(f"Promotion drives +{demand_uplift:.0f} unit sales (+{demand_uplift/baseline_forecast.sum()*100:.1f}%)")
```

---

### Data Model (Conceptual)

**Core Entities**:
- `DemandForecast`: forecast_id, sku_id, store_id, date, predicted_sales, confidence_interval, model_version.
- `ForecastModel`: model_id, model_type (ARIMA, Prophet, XGBoost, LSTM), hyperparameters, training_date, accuracy_metrics.
- `ReorderPoint`: sku_id, store_id, reorder_point, order_up_to_level, safety_stock, lead_time_days, service_level_target.
- `PromotionPlan`: promo_id, sku_id, store_id, start_date, end_date, promo_type, promo_depth, forecasted_uplift.
- `ExternalSignal`: signal_id, signal_type (weather, event, competitor), date, location, value (e.g., temperature, event name).
- `ForecastAccuracy`: sku_id, store_id, forecast_date, actual_sales, forecasted_sales, mape, mae, rmse.
- `ScenarioAnalysis`: scenario_id, scenario_name, assumptions (JSON), baseline_forecast, scenario_forecast, impact_summary.

**Relationships**:
- `DemandForecast` → `ForecastModel` (which model generated this forecast?)
- `DemandForecast` → `PromotionPlan` (forecasted with or without promotion?)
- `ReorderPoint` → `DemandForecast` (based on forecasted demand)
- `ForecastAccuracy` → `DemandForecast` (compare forecast vs. actual)

---

### Key Workflows

#### Workflow 1: Daily Forecast Generation
1. **Data Collection**: Gather historical sales (ADR-024), promotions (ADR-025), weather, events.
2. **Feature Engineering**: Create time features (day of week, month), lag features (sales last week), external signals.
3. **Model Selection**: Choose appropriate model per SKU-store (based on data availability, accuracy history).
4. **Forecast Generation**: Run forecasting model → predict next 30-90 days demand per SKU-store.
5. **Post-Processing**: Apply business rules (minimum sales floor, maximum cap), adjust for known stockouts.
6. **Storage**: Save forecasts to `DemandForecast` table.
7. **Event Publishing**: `DemandForecastGeneratedEvent` → trigger replenishment recommendations.

#### Workflow 2: Automatic Replenishment Recommendations
1. **Forecast Consumption**: Read latest forecasts from `DemandForecast`.
2. **Current Inventory**: Read on-hand stock from ADR-024 (stock ledger).
3. **Reorder Point Calculation**: Calculate ROP = (Avg Daily Demand × Lead Time) + Safety Stock.
4. **Recommendation Logic**:
   - If `On-Hand Stock < Reorder Point` → Generate replenishment order
   - Order Quantity = `Order-Up-To Level - On-Hand Stock - In-Transit Stock`
5. **Approval Workflow**: Route recommendations to buyers for approval (ADR-046 Workflow Engine).
6. **PO Generation**: If approved, create purchase order (ADR-023 Procurement).

#### Workflow 3: Promotion Impact Analysis
1. **Promotion Plan**: Marketing team creates promotion in ADR-025 (Sales & Distribution).
2. **Baseline Forecast**: Generate forecast without promotion (normal demand).
3. **Promotion Forecast**: Re-run forecast with promotion features (is_promo=True, promo_depth=0.30).
4. **Uplift Calculation**: `Uplift = Promotion Forecast - Baseline Forecast`.
5. **Stock Requirement**: `Required Stock = Baseline On-Hand + Uplift + Safety Buffer (20%)`.
6. **Buyer Alert**: If current stock insufficient, alert buyers to increase order quantities.

#### Workflow 4: Model Retraining & Accuracy Monitoring
1. **Nightly Accuracy Check**: Compare yesterday's forecast vs. actual sales (from POS).
2. **Accuracy Metrics**: Calculate MAPE, MAE, RMSE per SKU-store.
3. **Degradation Detection**: If MAPE > 20% or increased >5% vs. last week, flag for retraining.
4. **Retraining Pipeline**:
   - Fetch latest 12-24 months historical data
   - Re-run model training with updated data
   - Evaluate new model accuracy (holdout test set)
   - If new model better (lower MAPE), deploy to production
   - Update `ForecastModel` table with new version
5. **Dashboard Update**: Refresh accuracy dashboard for users (buyers, planners).

---

### Integration Points

**Core Integrations**:
- **Inventory (ADR-024)**: Read stock levels, historical sales, POS data. Write replenishment recommendations.
- **Sales & Distribution (ADR-025)**: Read promotion plans, pricing changes. Forecast promotion impact.
- **Procurement (ADR-023)**: Generate purchase orders based on replenishment recommendations.
- **Analytics (ADR-016)**: Provide forecast accuracy dashboards, demand trends, what-if analysis.
- **Workflow Engine (ADR-046)**: Route replenishment recommendations for buyer approval.

**External Integrations**:
- **Weather APIs**: OpenWeather, Weather.com (temperature, precipitation forecasts).
- **Event APIs**: Eventbrite, Google Calendar (concerts, sports, holidays).
- **Competitor Intelligence**: Third-party services (price monitoring, store openings).

**Event-Driven Architecture**:
- **Consumes**:
  - `POSSalePostedEvent` (ADR-024) → Update historical sales data
  - `PromotionCreatedEvent` (ADR-025) → Trigger promotion impact forecast
  - `NewStoreOpenedEvent` (ADR-027 MDG) → Forecast new store demand
- **Publishes**:
  - `DemandForecastGeneratedEvent` → Notify replenishment engine
  - `ReplenishmentRecommendationCreatedEvent` → Notify buyers
  - `ForecastAccuracyDegradedEvent` → Trigger model retraining

---

### Non-Functional Constraints

**Performance**:
- **Forecast Generation**: ≤ 5 minutes for 100 stores × 5,000 SKUs = 500K forecasts (batch processing overnight).
- **Reorder Point Calculation**: ≤ 1 second per SKU-store (real-time API).
- **What-If Analysis**: ≤ 30 seconds for scenario simulation (interactive user experience).

**Accuracy Targets**:
- **Phase 1 (Year 1)**: MAPE ≤ 20% (baseline, acceptable for most retail).
- **Phase 2 (Year 2)**: MAPE ≤ 15% (good accuracy, industry standard).
- **Phase 3 (Year 3+)**: MAPE ≤ 10% (excellent accuracy, competitive with best-in-class).

**Scalability**:
- Support 500 stores × 10,000 SKUs = 5M forecasts per day.
- Handle 10 concurrent model training jobs (parallel processing).

**Data Retention**:
- **Historical Sales**: Retain 3 years for trend analysis.
- **Forecasts**: Retain 1 year for accuracy validation.
- **Model Versions**: Retain last 10 versions per SKU-store.

---

## Alternatives Considered

### 1. Manual Forecasting (Excel-based)
- **Approach**: Buyers manually forecast demand using Excel spreadsheets and historical averages.
- **Pros**: Simple, no ML complexity, buyers understand the logic.
- **Cons**: Time-consuming (40+ hours/month for 100 stores), error-prone, cannot handle seasonality/promotions.
- **Decision**: **Rejected** — Scales poorly, 15-20% forecast error typical (vs. 10-15% with AI).

### 2. Simple Moving Average
- **Approach**: Use 4-week or 8-week moving average as forecast (no seasonality modeling).
- **Pros**: Simple to implement, interpretable.
- **Cons**: Lags behind trends, cannot predict promotion impact or seasonality.
- **Decision**: **Rejected** — Insufficient accuracy for sophisticated retail (MAPE 25-30%).

### 3. External Demand Planning Platform (Blue Yonder, o9 Solutions)
- **Approach**: Integrate with third-party demand planning SaaS (Blue Yonder, o9, Kinaxis).
- **Pros**: Best-in-class forecasting, proven at scale (Walmart, Target use Blue Yonder).
- **Cons**: Expensive ($500K-$2M annual licensing), complex integration, vendor lock-in.
- **Decision**: **Rejected for MVP** — Too expensive for ChiroERP's mid-market target. However, provide integration option for large enterprises.

### 4. Build In-House AI Platform
- **Approach**: Build custom AI forecasting platform from scratch (Python, TensorFlow, MLflow).
- **Pros**: Full control, customizable, no vendor lock-in.
- **Cons**: High development cost ($1M-$2M), 12-18 months to production, requires data science team.
- **Decision**: **Accepted with Phased Approach** — Start with open-source models (Prophet, ARIMA), add advanced ML (LSTM) in Phase 2.

---

## Consequences

### Positive
- **Stockout Reduction**: 15% → 5-8% (50% improvement) → recover lost sales.
- **Excess Inventory Reduction**: 25% → 10-15% (40-60% improvement) → reduce markdown losses.
- **Inventory Carrying Cost Savings**: 10-15% reduction in working capital tied up in inventory.
- **Buyer Productivity**: Automated recommendations free buyers to focus on strategic decisions (assortment, vendor negotiations).
- **Competitive Differentiation**: Closes gap with SAP, Oracle, Blue Yonder for enterprise retail customers.

### Negative / Risks
- **Model Accuracy Risk**: If forecasts are inaccurate (MAPE >20%), buyers will lose trust and revert to manual forecasting.
  - **Mitigation**: Start with conservative forecasts (wider safety stock), gradually tighten as accuracy improves.
- **Data Quality Dependency**: Forecasting requires clean historical sales data. Missing data, incorrect timestamps, POS errors will degrade accuracy.
  - **Mitigation**: Implement data quality checks (ADR-027 Master Data Governance), flag suspicious data patterns.
- **Change Management**: Buyers may resist AI recommendations ("I know my products better than a machine").
  - **Mitigation**: Position AI as **decision support**, not replacement. Allow buyers to override recommendations with justification.
- **Cold Start Problem**: New stores or new SKUs lack historical data → cannot forecast accurately.
  - **Mitigation**: Use similar store/similar product approach (cluster-based forecasting), bootstrap with 3-6 months manual forecasts.

### Neutral
- **ML Infrastructure Required**: Requires MLOps platform (MLflow, Kubeflow) for model training, versioning, deployment.
- **Data Science Talent**: Requires 2-3 data scientists with time series forecasting expertise.
- **Ongoing Maintenance**: Models degrade over time, require continuous monitoring and retraining.

---

## Success Metrics (12 Months Post-Launch)

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Forecast Accuracy (MAPE)** | **≤ 15%** | Compare forecasted vs. actual sales across all SKU-stores |
| **Stockout Reduction** | **≥ 50% reduction** | Compare stockout frequency (before vs. after AI) |
| **Excess Inventory Reduction** | **≥ 40% reduction** | Compare excess inventory (units >90 days old) |
| **Inventory Turns Improvement** | **+10-20%** | Inventory Turns = COGS / Avg Inventory |
| **Buyer Adoption Rate** | **≥ 75%** | % of replenishment recommendations accepted by buyers |
| **Model Retraining Frequency** | **≥ 90% models retrained monthly** | Ensure models stay current |
| **System Uptime** | **≥ 99.5%** | Forecast generation runs complete successfully |

---

## Compliance

- **Data Privacy (GDPR, CCPA)**: Forecasting uses aggregated sales data (no customer PII). Comply with data retention policies (ADR-015).
- **Model Explainability**: Provide feature importance (which factors drive forecast?) for buyer trust and audit trail.
- **Audit Trail**: Log all forecast versions, model changes, buyer overrides for compliance and root cause analysis.

---

## Implementation Plan

### Phase 1 (Months 1-3): MVP — Classical Time Series Forecasting
**Scope**:
- ARIMA and Prophet forecasting per SKU-store
- Basic seasonality detection (weekly, annual)
- Automatic reorder point recommendations
- Forecast accuracy dashboard (MAPE, MAE)

**Deliverables**:
- [ ] Forecasting engine (Python, Prophet, ARIMA)
- [ ] Reorder point calculator (safety stock, lead time)
- [ ] Daily forecast batch job (500K forecasts overnight)
- [ ] Forecast accuracy monitoring dashboard
- [ ] Replenishment recommendation API (REST)
- [ ] Buyer approval workflow integration (ADR-046)

**Target Accuracy**: MAPE ≤ 20% (baseline)

---

### Phase 2 (Months 4-6): Advanced ML & Promotion Modeling
**Scope**:
- XGBoost/LightGBM forecasting (feature engineering: day of week, month, lag features)
- Promotion impact modeling (BOGO, discounts)
- Ensemble forecasting (ARIMA + Prophet + XGBoost)
- External signal integration (weather, events)

**Deliverables**:
- [ ] ML forecasting models (XGBoost, LightGBM)
- [ ] Promotion uplift calculator
- [ ] Weather/event API integration
- [ ] Ensemble model orchestration
- [ ] What-if analysis UI (scenario planning)

**Target Accuracy**: MAPE ≤ 15% (good)

---

### Phase 3 (Months 7-9): Multi-Echelon Optimization & Advanced ML
**Scope**:
- LSTM/Transformer forecasting (deep learning for complex patterns)
- Multi-echelon optimization (DC → store allocation)
- Real-time demand sensing (POS → immediate forecast updates)
- Automatic model retraining pipeline

**Deliverables**:
- [ ] LSTM/Transformer models (TensorFlow, PyTorch)
- [ ] Multi-echelon optimizer (DC-store allocation)
- [ ] Real-time forecast API (<1 second response)
- [ ] MLOps pipeline (MLflow, model versioning, A/B testing)
- [ ] Advanced analytics (cohort analysis, demand drivers)

**Target Accuracy**: MAPE ≤ 10% (excellent)

---

## References

### Related ADRs
- ADR-024: Inventory Management (stock ledger, POS synchronization, historical sales data)
- ADR-025: Sales & Distribution (promotion planning, pricing, order orchestration)
- ADR-023: Procurement (purchase order generation, supplier lead times)
- ADR-055: Workforce Scheduling & Labor Management (labor demand forecasting patterns)
- ADR-016: Analytics & Reporting Architecture (forecast accuracy dashboards)
- ADR-046: Workflow & Approval Engine (buyer approval for replenishment recommendations)
- ADR-027: Master Data Governance (data quality, SKU master, store master)
- ADR-015: Data Lifecycle Management (historical data retention, archiving)

### Internal Documentation
- `docs/AI-STRATEGY.md` (ChiroERP AI strategy and phasing)
- `docs/architecture/retail/RETAIL-AI-ASSESSMENT.md` (retail AI gap analysis)

### External References
- **Forecasting**:
  - Prophet documentation: https://facebook.github.io/prophet/
  - ARIMA tutorial: https://otexts.com/fpp3/arima.html
  - Time series forecasting with ML: https://www.tensorflow.org/tutorials/structured_data/time_series
- **Retail Demand Forecasting**:
  - Blue Yonder Luminate Demand: https://blueyonder.com/solutions/luminate-demand
  - Oracle Retail Demand Forecasting: https://www.oracle.com/retail/demand-forecasting/
  - SAP Integrated Business Planning (IBP): https://www.sap.com/products/supply-chain-management/integrated-business-planning.html
- **MLOps**:
  - MLflow: https://mlflow.org/
  - Kubeflow: https://www.kubeflow.org/
- **Research Papers**:
  - "Forecasting at Scale" (Prophet paper): https://peerj.com/preprints/3190/
  - "Temporal Fusion Transformers for Interpretable Multi-horizon Time Series Forecasting": https://arxiv.org/abs/1912.09363

---

## Decision Log

| Date | Author | Decision |
|------|--------|----------|
| 2026-02-03 | Architecture Team | Initial draft created based on retail AI gap analysis |
| TBD | Supply Chain Team | Review forecasting methods and reorder point logic |
| TBD | Data Science Team | Validate ML model selection and accuracy targets |
| TBD | Buyer Representatives | Review approval workflow and override capabilities |
| TBD | Executive Leadership | Approve Phase 1 budget ($500K) and 2-3 data scientist headcount |
