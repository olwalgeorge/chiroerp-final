# ADR-072: Utilities Industry Solution (Electric, Gas, Water)

**Status**: Planned (P3 - 2028)  
**Date**: 2026-02-06  
**Decision Makers**: CTO, VP Product, Head of Industry Solutions  
**Consulted**: Utility company executives, CIO/CFOs, regulatory compliance experts  
**Informed**: Sales, customer success, implementation teams

---

## Context

### Business Problem

Electric, gas, and water utilities (3,300+ US utilities, $500B revenue 2024) require specialized capabilities not present in general ERPs:

- **Meter-to-Cash**: Meter reading (AMI/AMR smart meters, 15-min interval data) → billing (tiered rates, time-of-use, demand charges) → collections (payment processing, delinquency, disconnection)
- **Asset Management**: Physical assets (poles, wires, transformers, substations, pipelines, valves, pumps) with 40-year lifecycles, GIS integration (geospatial location), predictive maintenance (ML failure probability)
- **Work Management**: Work orders (planned maintenance, emergency repairs, customer requests), crew dispatch (GPS routing, skills, equipment), outage management (last gasp detection, estimated restoration time, customer notifications)
- **Customer Information System (CIS)**: Customer accounts, service addresses, meter serial numbers, billing history, payment history, customer portals (self-service billing, usage analytics)
- **Regulatory Reporting**: FERC Form 1 (electric), FERC Form 2 (gas), PUC rate cases (cost of service studies), RUS (Rural Utilities Service)

Without specialized utilities ERP, companies face:
- **Manual meter reading** (AMR integration missing, estimated bills 20-30% of customers)
- **Billing errors** (complex rate structures, manual calculations = 2-5% error rate)
- **Long outage response** (3-4 hours average restoration vs 2 hours with OMS)
- **Aging asset failures** (reactive maintenance vs predictive = 2x emergency costs)
- **Regulatory compliance risk** (manual FERC reporting, audit findings)

### Current State (2026)

**Existing Capabilities**:
- **ADR-042 (FSM)**: Basic field service with utilities extension (meter reading, work orders, asset tracking)
- **ADR-021 (Fixed Assets)**: Asset depreciation (no GIS, no predictive maintenance, no 40-year utility lifecycles)
- **ADR-022 (Revenue Recognition)**: Basic billing (no tiered rates, no time-of-use, no demand charges)

**Limitations**:
- No AMI/AMR smart meter integration (15-min interval data)
- No meter-to-cash workflow (meter data → bill calculation → collections)
- No tiered rate engine (0-500 kWh @ $0.10, 500-1000 kWh @ $0.12, >1000 kWh @ $0.15)
- No GIS integration (asset location, service territory mapping)
- No outage management system (last gasp detection, crew dispatch, customer notifications)
- No Customer Information System (CIS) (customer accounts, billing history, portals)
- No FERC/PUC regulatory reporting

### Market Opportunity

**Target Market**:
- **Mid-market utilities** 50K-500K customers (investor-owned utilities, municipal utilities, cooperatives)
- **Electric utilities**: 3,000+ US utilities ($420B revenue)
- **Gas utilities**: 1,200+ US utilities ($60B revenue)
- **Water utilities**: 52,000+ US utilities ($20B revenue, mostly small municipal)

**Competitive Landscape**:
| Vendor | Product | Investment | Capabilities | Gap vs ChiroERP |
|--------|---------|------------|--------------|-----------------|
| **Oracle** | Oracle Utilities Customer Care & Billing (CC&B) | $10M-30M | Meter-to-cash, CIS, work mgmt, OMS | ✅ Feature parity target |
| **SAP** | SAP IS-U (Industry Solution - Utilities) | $15M-50M | Billing, device mgmt, CIS, FICA (contract accounts) | ✅ 60-70% cost advantage |
| **Itron** | Itron CIS (acquired Silver Spring Networks) | $5M-20M | AMI integration, billing, analytics | ✅ Native ERP integration |
| **Harris/Itineris** | Enterprise ERP for Utilities | $3M-10M | Mid-market focus, meter-to-cash, asset mgmt | ✅ Cloud-native advantage |
| **Legacy** | Customer-built systems (mainframe, AS/400) | $20M-100M maintenance | 20-40 year old, high maintenance | ✅ Modern UX, cloud-native |

**Customer ROI**:
- **Meter-to-cash cycle time**: <5 days (vs 7-10 days manual/AMR without integration)
- **Billing accuracy**: >99.5% (vs 97-98% manual, 2-5% error rate = customer complaints)
- **Outage response time**: <2 hours average restoration (vs 3-4 hours manual dispatch)
- **Asset utilization**: +10% (predictive maintenance reduces emergency failures by 30-50%)
- **Regulatory compliance**: 100% FERC/PUC reporting accuracy (vs 95% manual, audit findings)
- **Total Annual ROI**: $1M-$5M (for 100K-customer utility, $50M-$200M revenue)

---

## Decision

### Selected Approach: Comprehensive Utilities Industry Solution

Build **full-scope utilities industry solution** with meter-to-cash, asset management, work management, CIS, and regulatory reporting, targeting mid-market utilities (50K-500K customers):

1. **Meter-to-Cash**: AMI/AMR integration (15-min interval data), billing engine (tiered rates, TOU, demand charges), collections (delinquency, disconnection)
2. **Asset Management**: GIS integration (Esri ArcGIS), asset lifecycle (40-year), predictive maintenance (ML failure probability), capital planning
3. **Work Management**: Work orders, crew dispatch (GPS routing), outage management (last gasp, ETR, customer notifications)
4. **Customer Information System (CIS)**: Customer accounts, service addresses, billing/payment history, customer portals
5. **Regulatory Reporting**: FERC Form 1/2, PUC rate cases, RUS reports, FERC USofA (Uniform System of Accounts)

### Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│             Utilities Industry Solution (New)                     │
├──────────────────────────────────────────────────────────────────┤
│  Meter-to-      Asset           Work          CIS         Regula- │
│  Cash           Management      Management    (Customer) tory     │
│  (AMI/AMR,      (GIS,           (Crew         Info        Report- │
│   billing,      predictive      Dispatch,     System)     ing     │
│   collections)  maintenance)    OMS)                      (FERC)  │
└────────────┬───────────────────────────────────────────────────┬─┘
             │                                                   │
    ┌────────▼────────┬────────────┬────────────┐       ┌───────▼─────┐
    │                 │            │            │       │             │
┌───▼───┐      ┌──────▼─────┐ ┌───▼────┐  ┌───▼────┐  │   Finance   │
│Meter  │      │   GIS      │ │Work    │  │Customer│  │   (FERC     │
│ Data  │      │  (Esri     │ │ Order  │  │ Account│  │   USofA)    │
│(AMI/  │      │  ArcGIS)   │ │(ADR-42)│  │        │  │             │
│ AMR)  │      │            │ │        │  │        │  │             │
└───────┘      └────────────┘ └────────┘  └────────┘  └─────────────┘
```

---

## Implementation Details

### Domain Models (New Bounded Context: `utilities`)

#### Core Entities

| Entity | Attributes | Purpose |
|--------|-----------|---------|
| **Meter** | meterId, meterType (electric/gas/water), serialNumber, make/model, installDate, meterMultiplier, location (lat/long), serviceAccountId, AMI/AMR enabled | Meter master data |
| **MeterReading** | readingId, meterId, readingDate, readingValue, readingType (actual/estimated/AMI), intervalData (15-min), readerId | Meter reading (manual, AMR, AMI) |
| **RateSchedule** | scheduleId, customerClass (residential/commercial/industrial), effectiveDate, rateStructure (tiered/TOU/demand), tiers[], seasonalAdjustment | Rate schedule master data |
| **RateTier** | tierId, scheduleId, tierNumber, fromQuantity, toQuantity, ratePerUnit ($), tierType (energy/demand/fixed) | Tiered rate structure |
| **UtilityBill** | billId, serviceAccountId, billPeriod, meterReadings[], consumption (kWh/therms/gallons), rateSchedule, charges[], totalAmount, dueDate, status | Utility bill |
| **ServiceAccount** | accountId, customerId, serviceAddress, meterId, accountStatus (active/inactive/suspended), serviceType (electric/gas/water), connectDate | Customer service account |
| **UtilityAsset** | assetId, assetType (pole/transformer/line/substation/pipeline/valve), gisCoordinates (lat/long), voltageLevel, capacity, installDate, condition (1-5), maintenanceSchedule | Utility asset (GIS-enabled) |
| **Outage** | outageId, startTime, affectedAssets[], estimatedCustomers, assignedCrew, estimatedRestorationTime (ETR), actualRestorationTime, cause | Outage event |
| **OutageNotification** | notificationId, outageId, customerId, notificationType (SMS/email), sentTime, message (ETR, restoration update) | Customer outage notification |

#### Domain Events

| Event | Payload | Purpose |
|-------|---------|---------|
| `MeterReadingReceivedEvent` | meterId, readingDate, readingValue, intervalData (AMI 15-min) | Meter reading imported (AMI/AMR/manual) |
| `UtilityBillGeneratedEvent` | billId, serviceAccountId, totalAmount, dueDate | Bill generated, ready for customer |
| `PaymentReceivedEvent` | billId, paymentAmount, paymentDate, paymentMethod | Payment posted to bill |
| `DelinquencyDetectedEvent` | serviceAccountId, daysOverdue, balanceOwed, disconnectionEligible | Delinquent account (trigger collections) |
| `OutageDetectedEvent` | outageId, affectedAssets[], estimatedCustomers, detectionSource (last gasp/SCADA/customer call) | Outage started |
| `CrewDispatchedEvent` | outageId, crewId, dispatchTime, estimatedArrival | Crew assigned to outage |
| `OutageRestoredEvent` | outageId, restorationTime, cause, affectedCustomers | Outage resolved |
| `AssetMaintenanceScheduledEvent` | assetId, maintenanceType (preventive/predictive), scheduledDate | Asset maintenance due |
| `AssetFailurePredictedEvent` | assetId, failureProbability (%), recommendedAction (replace/inspect), predictionModel | ML predicted asset failure |

---

### Utilities Capabilities

#### 1. Meter-to-Cash

**Problem**: Manual meter reading → billing → collections = 7-10 days, 2-5% error rate.

**Capabilities**:

**1a. Meter Data Management (MDM)**:
- **Meter Master Data**: Meter ID, type (electric/gas/water), serial number, make/model, install date, multiplier, location (GPS)
- **AMI/AMR Integration**: Smart meters (Itron, Landis+Gyr, Aclara), 15-minute interval data, daily/hourly reads
- **Manual Readings**: Mobile app (field meter readers), photo capture (meter display), offline support
- **Estimated Bills**: When meter not accessible, estimate based on historical consumption (last 12 months average)
- **Meter Exchange**: Track meter replacements (old meter final read, new meter initial read)

**1b. Billing Engine**:
- **Rate Schedules**: Residential, commercial, industrial, agricultural
- **Tiered Rates**: 0-500 kWh @ $0.10, 500-1000 kWh @ $0.12, >1000 kWh @ $0.15
- **Time-of-Use (TOU)**: On-peak $0.20 (2pm-7pm), off-peak $0.08 (11pm-6am), shoulder $0.12
- **Demand Charges**: Commercial/industrial ($/kW based on max 15-min demand)
- **Fixed Charges**: Customer charge ($15/month), meter charge ($5/month)
- **Seasonal Rates**: Summer (Jun-Sep) vs Winter (Oct-May)
- **Taxes/Fees**: State/local sales tax, franchise fees, regulatory surcharges
- **Bill Calculation**: (Consumption × rate tiers) + demand charges + fixed charges + taxes

**Example Bill Calculation (Residential Tiered Rate)**:
```
Customer: John Smith (Residential)
Meter Reading: Previous 10,000 kWh → Current 10,850 kWh
Consumption: 850 kWh
Rate Schedule: Residential Tiered (Tier 1: 0-500 kWh @ $0.10, Tier 2: 500+ kWh @ $0.12)

Bill Calculation:
  Tier 1 (0-500 kWh): 500 kWh × $0.10 = $50.00
  Tier 2 (500-850 kWh): 350 kWh × $0.12 = $42.00
  Customer Charge (fixed): $15.00
  Subtotal: $107.00
  Sales Tax (6%): $6.42
  Total: $113.42
  Due Date: 15 days from bill date
```

**1c. Collections Management**:
- **Payment Processing**: Credit card, ACH, check, cash, customer portal (online payment)
- **Budget Billing**: Average monthly payment plan (smooth seasonal spikes)
- **Payment Plans**: Installment agreements (spread past-due balance over 6-12 months)
- **Delinquency Management**: Automated dunning letters (30d, 60d, 90d overdue), disconnect notices
- **Disconnect/Reconnect**: Workflow (field order → disconnect service → reconnect upon payment + reconnect fee)
- **Late Fees**: 1.5% per month on overdue balance (or state-regulated rate)

**Success Metrics**:
- Meter-to-cash cycle time: <5 days (read → bill → mail/email)
- Billing accuracy: >99.5% (vs 97-98% manual)
- Estimated bill rate: <5% (vs 20-30% manual AMR)
- Collection rate: 98%+ within 90 days

---

#### 2. Asset Management

**Problem**: Aging utility infrastructure (40+ year assets), reactive maintenance = 2x cost vs predictive.

**Capabilities**:

**2a. Asset Registry**:
- **Asset Hierarchy**: Substation → Feeder → Transformer → Service Line → Meter (electric)
- **Asset Types**: Poles, wires (overhead/underground), transformers (distribution/substation), substations, circuit breakers, reclosers, capacitor banks, pipelines, valves, pumps, treatment plants
- **Asset Attributes**: Install date, manufacturer, model, serial number, capacity (kVA, voltage, flow rate), condition (1-5: new → end-of-life), criticality (high/medium/low)
- **GIS Coordinates**: Latitude/longitude for every asset (poles, transformers, lines)

**2b. GIS Integration**:
- **Esri ArcGIS Integration**: Display assets on map (service territory, asset locations, customer service addresses)
- **Service Territory Mapping**: Display utility service area, substations, feeders, outage zones
- **Outage Visualization**: Display affected customers on map (color-coded by outage duration)
- **Work Order Routing**: GPS-based crew dispatch (nearest available crew with skills/equipment)

**2c. Asset Lifecycle Management**:
- **Install → Inspect → Maintain → Replace** (40-year lifecycle for transformers, 50-year for poles)
- **Depreciation**: Group depreciation (pool all transformers, depreciate over 40 years), unit-of-production (gas pipelines by throughput)
- **Condition Scoring**: 1 (new) → 2 (good) → 3 (fair) → 4 (poor) → 5 (critical/end-of-life)
- **Capital Planning**: Replacement forecasting (assets reaching condition 4-5 in next 5-10 years)

**2d. Predictive Maintenance**:
- **ML Failure Prediction**: Train ML model on historical failure data (age, condition, maintenance history, weather, load)
- **Failure Probability**: Predict failure probability (%) for next 1-3 years (e.g., Transformer #12345: 35% failure probability next year)
- **Recommended Actions**: Replace (failure probability >50%), inspect (25-50%), routine maintenance (<25%)
- **Prioritization**: High-criticality assets (substation transformers) + high failure probability = top priority

**Example (Predictive Maintenance)**:
```
Asset: Transformer #12345 (Distribution, 500 kVA)
Install Date: 1985 (39 years old)
Condition Score: 4 (poor)
Maintenance History: Last inspected 2022, oil sample: dissolved gas analysis (DGA) shows elevated acetylene (arcing)
Load: 450 kVA (90% capacity)
Failure Probability (ML Model): 45% next year
Recommendation: Replace in next 6 months (before summer peak load)
Cost Avoidance: $50K emergency replacement + $100K outage cost (1,200 customers × 4 hours)
```

**Success Metrics**:
- Asset utilization: +10% (predictive maintenance extends life 5-10 years)
- Emergency failures: -30-50% (vs reactive maintenance)
- Capital planning accuracy: 90%+ (forecast asset replacements 5 years out)

---

#### 3. Work Management & Outage Management System (OMS)

**Problem**: Manual outage response = 3-4 hours average restoration (no last gasp detection, no crew optimization).

**Capabilities**:

**3a. Work Order Management** (extends ADR-042 FSM):
- **Work Order Types**: Planned maintenance (transformer inspection, line patrol), emergency repairs (downed line, gas leak), customer requests (new service, meter exchange)
- **Work Order Attributes**: Priority (emergency/high/medium/low), required skills (lineman, electrician, plumber), equipment (bucket truck, backhoe, vacuum truck), estimated duration
- **Mobile App**: iOS/Android app for field crews (view work orders, update status, capture photos, time tracking)

**3b. Crew Dispatch**:
- **Crew Availability**: Track crew shifts, lunch breaks, current location (GPS tracking)
- **Skills Matrix**: Lineman (overhead, underground), electrician, plumber, welder, CDL (commercial driver)
- **Equipment Assignment**: Bucket truck, digger derrick, vacuum truck, backhoe, welding truck
- **GPS Routing**: Calculate nearest available crew with required skills/equipment (Google Maps API, Esri ArcGIS)
- **Dispatch Optimization**: Assign multiple work orders to crew (route optimization, minimize drive time)

**3c. Outage Management System (OMS)**:
- **Outage Detection**:
  - **AMI Last Gasp**: Smart meter sends "last gasp" signal when power lost (within 1-2 seconds)
  - **SCADA Alarms**: Supervisory Control and Data Acquisition (substation breaker trips, feeder outages)
  - **Customer Calls**: Customer service reps enter outage reports (phone, web, mobile app)
- **Outage Prediction**: Weather-based outage prediction (storms, high winds, ice, heat waves)
- **Affected Customer Estimation**: Count meters downstream of affected asset (transformer, feeder)
- **Crew Assignment**: Dispatch nearest available crew (lineman, bucket truck, required skills)
- **Estimated Time to Restore (ETR)**: Calculate ETR based on outage type (average 2 hours for distribution transformer, 4 hours for substation)
- **Customer Notifications**: SMS/email to affected customers (outage start, ETR, restoration update)
- **Outage Analytics**: Track SAIDI (System Average Interruption Duration Index), SAIFI (System Average Interruption Frequency Index), CAIDI (Customer Average Interruption Duration Index)

**Example (Outage Management)**:
```
Outage Detected: 2:15 PM (AMI last gasp from 450 meters)
Affected Asset: Distribution Transformer #T-5678 (serving 450 customers)
Root Cause: Blown fuse (detected via SCADA remote monitoring)
Crew Dispatched: Crew #12 (2 linemen, bucket truck), 2:18 PM (3 minutes from detection)
ETR: 2 hours (fuse replacement + line patrol)
Customer Notifications: SMS sent to 450 customers at 2:20 PM ("Outage detected, ETR 4:15 PM")
Actual Restoration: 3:45 PM (1.5 hours, ahead of ETR)
Final Notification: SMS sent at 3:47 PM ("Power restored, thank you for your patience")
```

**Success Metrics**:
- Outage detection time: <2 minutes (AMI last gasp vs 15-30 min customer calls)
- Crew dispatch time: <15 minutes (vs 30-60 min manual)
- Average restoration time: <2 hours (vs 3-4 hours manual)
- Customer notification rate: 100% (SMS/email to all affected customers)
- SAIDI/SAIFI: <regulatory targets (e.g., SAIDI <200 min/customer/year)

---

#### 4. Customer Information System (CIS)

**Problem**: No centralized customer account management (billing, payment history, service requests).

**Capabilities**:

**4a. Customer Account Management**:
- **Customer Master**: Customer ID, name, contact info (phone, email), mailing address, communication preferences (email/SMS/mail)
- **Service Account**: Link customer → service address → meter (one customer can have multiple service accounts: home electric + gas)
- **Account Status**: Active, inactive (disconnected), suspended (non-payment), transferred (change of occupancy)
- **Credit Management**: Credit score, deposit required (high-risk customers), payment history (on-time %, late payments)

**4b. Billing & Payment History**:
- **Billing History**: Last 24 months bills (bill date, amount, due date, paid date, balance)
- **Payment History**: Payment date, amount, method (credit card/ACH/check), confirmation number
- **Balance Aging**: Current balance, 0-30d, 31-60d, 61-90d, 90+d overdue

**4c. Customer Portal (Self-Service)**:
- **View Bills**: Download PDF bills, view billing history
- **Make Payment**: Credit card, ACH, one-time or auto-pay (recurring)
- **Usage Analytics**: Daily/monthly usage charts (kWh, therms, gallons), compare to prior year, neighbor comparison
- **Service Requests**: Submit meter exchange request, report outage, request new service
- **Outage Map**: View current outages in area (GIS-based map)
- **Energy Efficiency Tips**: Personalized recommendations (insulation, LED bulbs, thermostat settings)

**4d. Customer Service Rep (CSR) Tools**:
- **360° Customer View**: Single screen (customer info, service accounts, billing history, payment history, open work orders, recent calls)
- **Payment Plans**: Set up installment agreements (6-12 months)
- **Disconnect/Reconnect**: Schedule disconnect (non-payment), reconnect (payment received + reconnect fee)
- **Service Orders**: Create new service, transfer service (change of occupancy), meter exchange

**Success Metrics**:
- Customer portal adoption: 40-60% customers (pay online, view bills)
- Call center volume: -20-30% (self-service deflects calls)
- CSR handle time: -25% (360° view, no screen toggling)

---

#### 5. Regulatory Reporting

**Problem**: Manual FERC/PUC reporting = 40-80 hours per quarter, audit findings.

**Capabilities**:

**5a. FERC Form 1 (Electric Utilities)**:
- **Annual Report**: Revenue, expenses, assets, liabilities, capital structure
- **Plant in Service**: FERC Accounts 300-399 (generation, transmission, distribution assets)
- **Depreciation**: Group depreciation by asset class (transformers, poles, lines)
- **Operating Expenses**: Accounts 500-599 (operations, maintenance, customer service)
- **AFUDC (Allowance for Funds Used During Construction)**: Capitalize interest during construction

**5b. FERC Form 2 (Gas Utilities)**:
- **Annual Report**: Revenue, expenses, gas supply, pipeline capacity
- **Plant in Service**: FERC Accounts 300-399 (transmission, distribution, storage)
- **Gas Supply**: Purchase contracts, pipeline capacity, storage inventory

**5c. PUC Rate Cases (Public Utility Commission)**:
- **Cost of Service Study**: Calculate revenue requirement (operating expenses + depreciation + return on equity)
- **Rate Design**: Design rates to recover revenue requirement (tiered, TOU, demand charges)
- **Rate Case Filing**: Prepare exhibits (cost of service, rate design, depreciation study)

**5d. RUS (Rural Utilities Service)**:
- **Annual Report**: Cooperatives receiving RUS loans/grants
- **Form 7**: Financial statements (balance sheet, income statement, cash flow)

**5e. FERC USofA (Uniform System of Accounts)**:
- **Chart of Accounts**: FERC-mandated account structure (300-399 plant, 400-499 depreciation, 500-599 operations)
- **Account Mapping**: Map ChiroERP GL accounts → FERC accounts

**Success Metrics**:
- FERC reporting time: <8 hours (vs 40-80 hours manual)
- Reporting accuracy: 100% (vs 95% manual, audit findings)
- Audit findings: 0 (vs 2-5 per audit)

---

## Technology Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Backend** | Quarkus 3.x + Kotlin | Existing ChiroERP stack |
| **Meter Data Management** | PostgreSQL + TimescaleDB | Time-series data (15-min AMI intervals) |
| **AMI/AMR Integration** | Itron API, Landis+Gyr API, Aclara API | Smart meter vendors |
| **Billing Engine** | Kotlin + Drools (rules engine) | Complex rate structures (tiered, TOU, demand) |
| **GIS Integration** | Esri ArcGIS API, Mapbox | Asset location, outage visualization |
| **Outage Management** | Kafka (event streaming), Redis (real-time crew status) | Real-time outage detection, crew dispatch |
| **Customer Portal** | React + Next.js | Self-service billing, usage analytics |
| **Predictive Maintenance** | Python 3.11 + scikit-learn (Random Forest, XGBoost) | ML asset failure prediction |
| **Regulatory Reporting** | Jasper Reports | FERC Form 1/2, PUC reports |

---

## Integration Points

### Existing ChiroERP Modules

| Module | Integration | Purpose |
|--------|-------------|---------|
| **Field Service (ADR-042)** | Work orders, crew dispatch, mobile app | Extend FSM for utilities work orders |
| **Fixed Assets (ADR-021)** | Utility assets (poles, transformers, lines), depreciation (group, unit-of-production) | Asset lifecycle, capital planning |
| **Finance (FI-GL)** | FERC USofA chart of accounts, FERC Form 1/2 reporting | Regulatory accounting |
| **Revenue Recognition (ADR-022)** | Utility billing, revenue posting | Meter-to-cash revenue |
| **Inventory (ADR-024)** | Meter inventory, transformer spare parts, emergency materials | Utilities-specific inventory |

### External Systems

| System | Integration Method | Purpose |
|--------|-------------------|---------|
| **AMI/AMR Systems** | API (Itron, Landis+Gyr, Aclara) | 15-min interval meter data, last gasp (outage detection), remote connect/disconnect |
| **SCADA** | OPC UA, Modbus, DNP3 | Substation alarms, breaker status, feeder outages |
| **GIS** | Esri ArcGIS REST API | Asset location, service territory, outage visualization |
| **Weather Services** | REST API (NOAA, Weather.com) | Storm prediction, outage forecasting |
| **Payment Gateway** | Stripe, Authorize.net | Credit card, ACH payment processing |
| **SMS Gateway** | Twilio | Outage notifications, payment reminders |

---

## Success Metrics & KPIs

### Meter-to-Cash

| Metric | Baseline (Manual/Legacy) | Target (ChiroERP) | Improvement |
|--------|--------------------------|-------------------| ------------|
| **Meter-to-cash cycle time** | 7-10 days | <5 days | -40-50% |
| **Billing accuracy** | 97-98% (2-5% errors) | >99.5% | +1.5-2.5% |
| **Estimated bill rate** | 20-30% (manual AMR) | <5% (AMI integration) | -75-85% |
| **Collection rate (90d)** | 95-97% | 98%+ | +1-3% |

### Asset Management

| Metric | Baseline (Reactive) | Target (Predictive) | Improvement |
|--------|---------------------|---------------------| ------------|
| **Emergency failures** | 100% (reactive) | 50-70% (predictive maintenance) | -30-50% |
| **Asset utilization** | 85% | 95%+ (extend life 5-10 years) | +10% |
| **Capital planning accuracy** | 70-80% (5-year forecast) | 90%+ | +10-20% |
| **Maintenance cost** | $1M/year (reactive) | $600K-800K/year (predictive) | -20-40% |

### Work Management & OMS

| Metric | Baseline (Manual) | Target (Automated) | Improvement |
|--------|-------------------|--------------------| ------------|
| **Outage detection time** | 15-30 min (customer calls) | <2 min (AMI last gasp) | -87-93% |
| **Crew dispatch time** | 30-60 min | <15 min (GPS routing) | -50-75% |
| **Average restoration time** | 3-4 hours | <2 hours | -33-50% |
| **Customer notification rate** | 20-40% (manual calls) | 100% (SMS/email) | +60-80% |
| **SAIDI (interruption duration)** | 250 min/customer/year | <200 min/customer/year | -20% |

### Customer Experience

| Metric | Baseline | Target | Improvement |
|--------|----------|--------| ------------|
| **Customer portal adoption** | 10-20% | 40-60% | +30-40% |
| **Call center volume** | 100K calls/year (100K customers) | 70K-80K calls/year | -20-30% |
| **CSR handle time** | 8 min/call | 6 min/call | -25% |
| **Customer satisfaction (NPS)** | 30-40 | 50-60 | +20 points |

---

## Cost Estimate

### Development

| Resource | Duration | Cost | Notes |
|----------|----------|------|-------|
| **3 Backend Engineers** | 8 months | $600K-$720K | Meter-to-cash, billing engine, OMS, CIS |
| **1 Utilities Domain Expert** | 8 months | $160K-$200K | Rate structures, FERC reporting, utility operations |
| **1 GIS Specialist** | 4 months | $80K-$100K | Esri ArcGIS integration, asset mapping |
| **1 ML Engineer** | 3 months | $75K-$100K | Predictive maintenance (asset failure probability) |
| **1 Frontend Engineer** | 6 months | $120K-$150K | Customer portal, CSR tools, outage map |
| **Testing & QA** | 6 months | $60K-$90K | Billing accuracy, AMI integration, OMS testing |
| **Total Development** | | **$1.095M-$1.36M** | |

### AMI/GIS Integration

| Resource | Cost | Notes |
|----------|------|-------|
| **AMI Integration** | $80K-$120K | Itron API, Landis+Gyr API, Aclara API (3 vendors) |
| **GIS Integration** | $30K-$40K | Esri ArcGIS REST API, Mapbox |
| **SCADA Integration** | $10K-$20K | OPC UA, Modbus (substation alarms) |
| **Total Integration** | **$120K-$180K** | |

### Software Licenses (Annual)

| License | Cost | Notes |
|---------|------|-------|
| **Esri ArcGIS** | $40K-$60K/year | GIS platform (asset mapping, service territory) |
| **TimescaleDB** | $10K-$20K/year | Time-series DB (15-min AMI interval data) |
| **SMS Gateway (Twilio)** | $5K-$10K/year | Outage notifications (estimate 10K messages/month) |
| **Total Licenses** | **$55K-$90K/year** | |

### Total Cost

| Category | Cost | Notes |
|----------|------|-------|
| **Development** | $1.095M-$1.36M | 32 weeks (Q3-Q4 2028) |
| **Integration** | $120K-$180K | One-time |
| **Licenses (Year 1)** | $55K-$90K | Recurring annual cost |
| **Total Year 1** | **$1.27M-$1.63M** | |
| **Recurring (Year 2+)** | $55K-$90K/year | Licenses only |

**Adjusted P3 Estimate**: **$1.0M-$1.5M** (aligns with P3 roadmap, assumes some cost optimization)

---

## Timeline

**Duration**: 32 weeks (Q3-Q4 2028)

### Phase 1: Meter-to-Cash (12 weeks)

**Deliverables**:
- Meter master data, AMI/AMR integration (Itron, Landis+Gyr, Aclara)
- Billing engine (tiered rates, TOU, demand charges)
- Collections management (payment processing, delinquency, disconnect/reconnect)

### Phase 2: Asset Management & GIS (8 weeks)

**Deliverables**:
- Utility asset registry (poles, transformers, lines, substations)
- Esri ArcGIS integration (asset mapping, service territory)
- Predictive maintenance (ML asset failure probability)

### Phase 3: Work Management & OMS (8 weeks)

**Deliverables**:
- Work order management (planned, emergency, customer requests)
- Crew dispatch (GPS routing, skills, equipment)
- Outage management (last gasp, ETR, customer notifications SMS/email)

### Phase 4: CIS & Regulatory Reporting (4 weeks)

**Deliverables**:
- Customer portal (view bills, make payment, usage analytics)
- CSR tools (360° customer view, payment plans)
- FERC Form 1/2, PUC reports, RUS reports

---

## Risks & Mitigation

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **AMI integration complexity (3 vendors)** | High | Medium | Start with Itron (largest share), defer Landis+Gyr/Aclara to Phase 2 |
| **Billing engine complexity (rate structures)** | High | High | Hire utilities domain expert, validate with 3-5 utilities |
| **GIS integration performance (large datasets)** | Medium | Medium | Use spatial indexing, cache asset locations, defer real-time updates |
| **Predictive maintenance accuracy <70%** | Medium | Low | Validate ML model on historical failure data (5+ years), tune threshold |

### Business Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Low utility adoption (<10 customers)** | High | Medium | Validate demand with 5-10 LOIs (mid-market utilities 50K-500K customers) |
| **Incumbent lock-in (Oracle/SAP contracts)** | High | High | Target utilities with aging systems (20+ years), highlight 60-70% cost savings |
| **Regulatory compliance risk (FERC)** | High | Low | Hire utilities accounting expert, validate FERC reporting with external auditor |

### Execution Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Utilities domain expert hiring delays** | High | Medium | Start recruiting Q2 2028 (4 months early), contractor fallback |
| **Scope creep (water utilities different from electric/gas)** | Medium | High | Limit Phase 1 to electric/gas, defer water to Phase 2 (2029) |
| **AMI vendor partnership delays** | Medium | Medium | Partner with Itron (market leader), defer other vendors if needed |

---

## Alternatives Considered

### Alternative 1: Partner with Utilities Software Vendor

**Approach**: Partner with Itron CIS, Harris, or similar utilities software vendor (white-label or API integration).

**Pros**:
- Fast deployment ($200K-$400K integration)
- Lower development cost

**Cons**:
- ❌ High recurring cost ($50K-$200K/year per customer, reduces margin)
- ❌ No differentiation (same software as competitors)
- ❌ Integration complexity (external API, data sync)

**Rejected**: Strategic capability for $500B utilities market, need native ERP.

---

### Alternative 2: Basic Meter-to-Cash Only (No OMS, CIS)

**Approach**: Build meter-to-cash (AMI integration, billing) only, defer OMS/CIS.

**Pros**:
- Faster time-to-market ($400K-$600K vs $1M-$1.5M)
- Lower complexity

**Cons**:
- ❌ Not competitive with Oracle Utilities/SAP IS-U (full suite required)
- ❌ Utilities need OMS (outage management is critical)
- ❌ Missing customer portal (40-60% adoption = call deflection)

**Rejected**: Full suite required for mid-market utilities (50K-500K customers).

---

## Consequences

### Positive

- ✅ **New Vertical**: $500B utilities market (3,300+ US utilities)
- ✅ **Competitive Advantage**: 60-70% cost savings vs Oracle Utilities ($10M-30M), SAP IS-U ($15M-50M)
- ✅ **Customer ROI**: $1M-$5M annual (meter-to-cash <5 days, billing accuracy >99.5%, outage response <2 hours)
- ✅ **Differentiation**: Cloud-native, modern UX, AMI integration, predictive maintenance (ML)
- ✅ **Recurring Revenue**: Utilities are sticky customers (10-20 year relationships)

### Negative

- ❌ **High Cost**: $1M-$1.5M development + $55K-$90K/year licenses
- ❌ **Complexity**: Meter-to-cash, OMS, CIS, FERC reporting (4 major capabilities)
- ❌ **Resource Intensive**: Utilities domain expert (hard-to-hire role), GIS specialist
- ❌ **Long Sales Cycle**: Utilities 12-24 months (RFP process, regulatory approval)

### Neutral

- ⚠️ **Phased Adoption**: Mid-market utilities (50K-500K) first, large investor-owned utilities (1M+) later
- ⚠️ **Regulatory Burden**: FERC/PUC reporting changes require updates
- ⚠️ **AMI Vendor Dependency**: Itron, Landis+Gyr, Aclara APIs (vendor lock-in risk)

---

## Related ADRs

- **ADR-042**: Field Service Management - Work orders, crew dispatch, mobile app (extend for utilities)
- **ADR-021**: Fixed Asset Accounting - Utility assets (poles, transformers), depreciation (group, unit-of-production)
- **ADR-022**: Revenue Recognition - Utility billing, revenue posting
- **ADR-024**: Inventory Management - Meter inventory, transformer spare parts
- **ADR-070**: AI Demand Forecasting - Predictive maintenance (asset failure probability)

---

## Approval

**Status**: Planned (P3 - Q3-Q4 2028)  
**Approved By**: (Pending CTO, VP Product, Head of Industry Solutions sign-off)  
**Next Review**: Q2 2028 (validate demand with utility LOIs, Itron partnership)

**Implementation Start**: Q3 2028  
**Target Completion**: Q4 2028 (32 weeks)  
**Beta Launch**: Q1 2029 (3-5 mid-market utilities 50K-500K customers)  
**General Availability**: Q2 2029 (all utilities)

---

**Document Owner**: Head of Industry Solutions  
**Last Updated**: 2026-02-06
