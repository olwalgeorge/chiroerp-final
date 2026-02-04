# ChiroERP AI Strategy: Pragmatic, ROI-Driven Approach

**Date**: 2026-02-03
**Status**: Approved
**Assessment Grade**: 8/10 — Pragmatic, Not Overengineered

## Executive Summary

ChiroERP applies AI/ML strategically in areas with clear ROI and measurable business impact, avoiding gratuitous "AI washing." Our AI strategy is phased, starting with proven, commodity technologies in Phase 1 (MVP) and adding more sophisticated ML capabilities in Phase 2 after data accumulation and customer validation.

## Core Principles

1. **AI where it solves real problems** — Not as marketing buzzwords
2. **Deterministic logic first** — Use AI only when rules-based approaches fall short
3. **Phased deployment** — MVP with proven tech, Phase 2 with advanced ML
4. **Measurable KPIs** — Every AI feature has success metrics
5. **Explainability** — Especially for hiring, compliance, and financial decisions

---

## Phase 1 (MVP): High-Value, Proven AI

### ✅ ADR-054: Receipt OCR (Travel & Expense)
- **Technology**: Optical Character Recognition (OCR)
- **Use Case**: Extract vendor, date, amount, category from receipt photos
- **Why High-Value**:
  - Saves 2-3 minutes per receipt × 1000s of receipts/month
  - Industry standard (Concur, Expensify, SAP Concur all use OCR)
  - Mature, commodity technology (Google Vision API, AWS Textract, Azure AI Document Intelligence)
- **KPI**: ≥95% OCR accuracy, <5 seconds processing time
- **Risk**: 2/10 — Proven technology
- **Recommendation**: ✅ **PHASE 1 CORE**

---

### ✅ ADR-052: Resume Parsing & Skills Matching (Contingent Workforce)
- **Technology**: Natural Language Processing (NLP), Machine Learning
- **Use Case**:
  - NLP-based resume parsing (extract skills, certifications, work history)
  - Semantic search ("senior Java developer with AWS experience")
  - ML-based candidate-job matching (skills match score, success prediction)
- **Why High-Value**:
  - Directly addresses VMS pain point: matching 100s of candidates to 100s of roles
  - Competitive necessity: SmartRecruiters, Bullhorn, Fieldglass all have AI matching
  - Solves unstructured data problem (resumes in various formats)
- **KPI**: ≥80% AI matching accuracy (top-3 recommendations hired)
- **Risk**: 4/10 — Requires training data, but well-established NLP techniques
- **Recommendation**: ✅ **PHASE 1 CORE** — Core VMS differentiator

---

### ✅ ADR-052: Bias Mitigation in Hiring
- **Technology**: Fairness constraints, explainability (SHAP, LIME), audit trails
- **Use Case**: Ensure AI candidate recommendations are free from demographic bias
- **Why High-Value**:
  - Legal compliance: EEOC regulations, EU AI Act requirements
  - Competitive differentiator: Responsible AI is a procurement criterion
  - Not just "nice to have" — increasingly mandatory for enterprise procurement
- **KPI**: Pass fairness audits (disparate impact ratio ≥ 0.8), explainability for all recommendations
- **Risk**: 3/10 — Well-established fairness libraries (AI Fairness 360, Fairlearn)
- **Recommendation**: ✅ **PHASE 1 CORE** — Regulatory necessity

---

### ✅ ADR-054: Rules-Based Fraud Detection (Travel & Expense)
- **Technology**: Deterministic rules + statistical outlier detection (Z-score)
- **Use Case**: Flag suspicious expenses for audit review
- **Why Deterministic is Better**:
  - ✅ Duplicate detection (same receipt submitted twice)
  - ✅ Policy violations (hotel over $300/night limit)
  - ✅ Missing receipts over threshold ($75)
  - ✅ Personal charges (gambling, cash advances merchant categories)
  - ✅ Z-score outliers (>2 standard deviations from average)
  - ❌ ML anomaly detection has high false-positive rates
- **Implementation**: Rules engine + statistical analysis, **NOT** machine learning
- **KPI**: <2% false-positive rate, ≥95% duplicate detection accuracy
- **Risk**: 2/10 — Deterministic logic is reliable and explainable
- **Recommendation**: ✅ **PHASE 1 CORE** — Rules-based approach (Note: "fraud detection" clarified as rules-based, not ML)

---

### ✅ ADR-040: Deterministic Health Scoring (Plant Maintenance)
- **Technology**: Multi-dimensional scoring model (NOT AI/ML)
- **Use Case**: Asset health scoring (5 dimensions: condition, performance, reliability, cost, criticality)
- **Why Deterministic is Better**:
  - ✅ Transparent scoring formula (weighted average)
  - ✅ Configurable by asset class
  - ✅ No training data required
  - ❌ True "predictive maintenance" requires IoT sensors (vibration, temperature) that most customers lack
- **Implementation**: Scoring model + condition inspections, **NOT** machine learning
- **KPI**: ≥90% critical assets scored monthly, ≥85% predictive accuracy for failures
- **Risk**: 1/10 — Simple, effective, no AI complexity
- **Recommendation**: ✅ **PHASE 1 CORE** — Deterministic model (Note: "predictive maintenance" clarified as health scoring, not AI/ML)

---

## Phase 2 (Future): Advanced ML After Data Accumulation

### ⚠️ ADR-052: Predictive Analytics (Contingent Workforce)
- **Use Case**: Time-to-fill prediction, attrition risk, rate optimization, supplier performance forecasting
- **Why Phase 2**:
  - Requires 6-12 months of historical requisition and assignment data
  - Strategic insights, but not MVP-blocking
  - Can launch VMS with basic reports, add predictions later
- **Recommendation**: ⚠️ **PHASE 2** — After data accumulates

---

### ⚠️ ADR-055: ML-Based Labor Demand Forecasting (WFM)
- **Use Case**: Time series forecasting (ARIMA, Prophet) for staffing needs
- **Why Phase 2**:
  - Phase 1 MVP: Historical averaging + linear regression (sufficient for <500 employees)
  - Phase 2: ML forecasting for large-scale operations (1000+ employees, 24/7 shifts)
  - Requires 6-12 months of historical labor and demand data
- **Recommendation**: ⚠️ **PHASE 2** — Start with regression, add ML for enterprises
- **Positioning**: ChiroERP WFM is **lightweight and integration-friendly** vs. Kronos/UKG Pro (which require dedicated implementation teams)

---

### ⚠️ ADR-055: Auto-Scheduling Optimization (WFM)
- **Use Case**: Integer Linear Programming (ILP) / constraint solvers for shift optimization
- **Why Phase 2**:
  - Phase 1 MVP: Manual drag-and-drop scheduling with conflict detection (sufficient for <100 employees)
  - Phase 2: Auto-scheduler for complex operations (100+ employees, rotating shifts, union rules)
  - This is **operations research**, not AI/ML — it's deterministic optimization
- **Recommendation**: ⚠️ **PHASE 2** — Manual scheduling first, auto-scheduler later
- **Note**: Most SMBs use manual scheduling; enterprises already have Kronos/ADP

---

### ❌ ADR-040: AI/ML Predictive Maintenance on IoT Sensors
- **Use Case**: Machine learning on IoT sensor data (vibration, temperature, oil analysis)
- **Why Avoid**:
  - Requires expensive IoT infrastructure + data science team
  - Most ChiroERP customers (95%) use preventive maintenance schedules, not IoT sensors
  - True predictive maintenance is for large manufacturing plants using specialized CMMS (IBM Maximo, Infor EAM)
- **Integration Strategy**: For customers with existing IoT platforms (Azure IoT Hub, AWS IoT Core), ChiroERP will **integrate sensor data** into health scores and alerts, but will **not build IoT infrastructure or train ML models from scratch**
- **Recommendation**: ❌ **AVOID BUILDING** — Integrate customer's IoT platform (future), don't build from scratch

---

## AI Strategy Summary Table

| ADR | AI Feature | Technology | Phase | Value Tier | Risk | Status |
|-----|-----------|-----------|-------|-----------|------|--------|
| **ADR-054** | Receipt OCR | OCR (commodity) | 1 | ⭐⭐⭐ HIGH | 2/10 | ✅ Core |
| **ADR-052** | Resume Parsing & Matching | NLP, ML | 1 | ⭐⭐⭐ HIGH | 4/10 | ✅ Core |
| **ADR-052** | Bias Mitigation | Fairness libraries | 1 | ⭐⭐⭐ HIGH | 3/10 | ✅ Core |
| **ADR-054** | Fraud Detection | Rules + Z-score | 1 | ⭐⭐⭐ HIGH | 2/10 | ✅ Core (deterministic) |
| **ADR-040** | Health Scoring | Multi-dim scoring | 1 | ⭐⭐⭐ HIGH | 1/10 | ✅ Core (deterministic) |
| **ADR-052** | Predictive Analytics (VMS) | ML regression/classification | 2 | ⭐⭐ MEDIUM | 4/10 | ⚠️ After data |
| **ADR-055** | Labor Demand Forecasting | Time series ML | 2 | ⭐⭐ MEDIUM | 5/10 | ⚠️ Enterprise |
| **ADR-055** | Schedule Optimization | ILP/CSP solvers | 2 | ⭐⭐ MEDIUM | 6/10 | ⚠️ Enterprise |
| **ADR-040** | Predictive Maintenance (IoT) | ML on sensor data | Never | ⭐ LOW | 8/10 | ❌ Integrate, don't build |

---

## Competitive Positioning

### SAP S/4HANA
- **Strengths**: Deep ML in procurement (SAP Ariba), finance (cash flow prediction), IoT (SAP Leonardo)
- **Weaknesses**: Overly complex for mid-market, expensive ML modules
- **ChiroERP Advantage**: Pragmatic AI for mid-market (OCR, NLP matching), not overkill

### Oracle ERP Cloud
- **Strengths**: AI-powered GL reconciliation, adaptive planning, procurement recommendations
- **Weaknesses**: AI features gated behind expensive add-ons
- **ChiroERP Advantage**: AI included in core tiers, not separate SKUs

### NetSuite
- **Strengths**: Simple, no AI overengineering
- **Weaknesses**: Limited AI capabilities (basic demand forecasting)
- **ChiroERP Advantage**: Modern AI (OCR, NLP, bias mitigation) without complexity

### Dynamics 365
- **Strengths**: Copilot integration (GPT-based chat), sales insights
- **Weaknesses**: AI focused on CRM, weak in finance/HR
- **ChiroERP Advantage**: AI across T&E, VMS, maintenance (broader coverage)

---

## Success Metrics

### Phase 1 (Year 1)
- **Receipt OCR**: ≥95% accuracy, <5 sec processing, used by ≥80% of T&E users
- **Resume Parsing**: ≥90% field extraction accuracy, saves ≥5 min per resume
- **Skills Matching**: ≥80% top-3 hire rate (customer hires one of top 3 recommendations)
- **Bias Audits**: Pass all fairness audits (disparate impact ratio ≥0.8)
- **Health Scoring**: ≥90% critical assets scored monthly

### Phase 2 (Year 2+)
- **Time-to-Fill Prediction**: ≥75% accuracy (±3 days), MAPE <20%
- **Labor Demand Forecasting**: ≥85% accuracy vs actual labor hours
- **Auto-Scheduler Adoption**: ≥50% of WFM customers with >100 employees use auto-scheduler

---

## Governance & Ethics

### Responsible AI Principles
1. **Explainability**: All AI hiring decisions include feature importance (SHAP values)
2. **Fairness**: Quarterly bias audits, disparate impact analysis
3. **Privacy**: PII anonymization for ML training, GDPR/CCPA compliance
4. **Human-in-the-Loop**: AI recommends, humans decide (especially hiring, fraud)
5. **Transparency**: Customers can disable AI features (except OCR, which is core UX)

### Compliance
- **EU AI Act**: Bias mitigation, explainability, audit trails for hiring AI
- **EEOC**: Adverse impact testing, fairness constraints
- **GDPR/CCPA**: Right to explanation, data deletion for ML models

---

## Conclusion

ChiroERP's AI strategy is **pragmatic and ROI-driven**, focusing on proven technologies (OCR, NLP) with clear business value while avoiding overengineering (no IoT predictive maintenance, no ML fraud detection where rules work better). This positions ChiroERP as **modern but not bleeding-edge**, competitive with SAP/Oracle but leaner, and far ahead of NetSuite without unnecessary complexity.

**Assessment**: 8/10 — Excellent strategic balance. Minor adjustments made to clarify phasing (WFM forecasting, VMS predictive analytics) and implementation approach (rules-based fraud detection, deterministic health scoring).

---

## References
- ADR-040: Plant Maintenance (Health Scoring)
- ADR-052: Contingent Workforce & Professional Services (AI-Powered Talent Matching)
- ADR-054: Travel & Expense Management (Receipt OCR, Fraud Detection)
- ADR-055: Workforce Scheduling & Labor Management (Demand Forecasting, Schedule Optimization)
