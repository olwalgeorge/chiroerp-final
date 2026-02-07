# Gap-to-SAP-Grade Implementation Plan - ChiroERP

**Status**: Implementation Roadmap
**Priority**: P0 (Critical - Product-Market Fit)
**Last Updated**: February 3, 2026
**Target**: Transform architecture into SAP-grade product for African market

---

## Executive Summary

### Current State
- **Architecture Maturity**: ✅ SAP-grade (70+ modules, 49 ADRs, event-driven, CQRS)
- **Functional Breadth**: ✅ SAP-grade (FI/CO/MM/PP/SD/QM/PM/CRM/MDG)
- **Product Maturity**: ⚠️ Pre-MVP (no production proof, missing platform semantics)

### The Gap
**Architecture ≠ Product**. We have world-class architecture but missing:
1. **Configuration Engine** - SAP wins by making 80-90% variation configurable, not coded
2. **Operational Org Model** - Propagation rules, authorization derivation, scope inheritance
3. **Localization Content Delivery** - Country packs with real regulatory content at scale
4. **Extensibility Hooks** - Partner-safe customization without breaking upgrades
5. **Workflow UX** - Unified task inbox, approval routing, process guidance

### The Solution
**18-Month Product Roadmap** in 6 phases:
- **Phase 1-2 (Months 1-6)**: Shared Platform + Tenancy + Finance MVP (Config/Org/Workflow + multi-tenant GL/AP/AR + core O2C/P2P)
- **Phase 3 (Months 7-9)**: Scale Foundation + Advanced Inventory Operations (3-country readiness, 10K users, retail depth)
- **Phase 4 (Months 10-12)**: Enterprise Features (enterprise deal, SoD, intercompany, period close)
- **Phase 5-6 (Months 13-18)**: Localization Scale + Compliance (15 country packs, partners, ISO/SOC2)

**Optional Retail AI Enhancement** (can run parallel to Phase 4-6):
- **Phase AI-1 (Months 1-9)**: AI Demand Forecasting & Replenishment (ADR-056) — $500K-$1M, 3-5X ROI
- **Phase AI-2 (Months 10-18)**: Dynamic Pricing & Markdown Optimization (ADR-057) — $300K-$500K, 2-3X ROI

**Total Investment**: $5.48M over 18 months (core roadmap) + $800K-$1.5M (optional retail AI)

### Success Metrics
- ✅ **Configuration Coverage**: 85%+ variation without custom code
- ✅ **Localization**: 3 country packs by Phase 3 (reference + 2) → 15 by Phase 5
- ✅ **Scale Proof**: 10,000 concurrent users, 1M transactions/month
- ✅ **Enterprise Deal**: $500K+ ARR contract with bank/telecom/manufacturing company
- ✅ **Partner Ecosystem**: 5+ implementation partners certified

---

## Adaptability Assessment: Excellent (Cloud-Native Advantage)

### ✅ Architecture Advantages Over SAP

| Dimension | SAP Baseline | ChiroERP Baseline | Adaptability Win |
|-----------|--------------|------------------|------------------|
| **Microservices Modularity** | Monolithic core with limited microservices | Pure microservices from day 1 (ADR-001) | Add/remove domains without cross-impact |
| **Event-Driven Integration** | Batch EDI + selective real-time (IDOC-heavy) | Event-first with Kafka (ADR-003, ADR-020) | New integrations subscribe to events (no point-to-point) |
| **Database-Per-Context** | Shared HANA database | PostgreSQL per bounded context (ADR-002) | Scale domains independently, polyglot persistence possible |
| **API-First Design** | ABAP APIs + partial REST | REST/GraphQL first (ADR-010) | Easier mobile + partner integrations |
| **Configuration Framework** | IMG customizing (T-codes) | Dynamic config engine (ADR-044) | 85%+ variation without custom code |
| **Multi-Tenancy Native** | Single-tenant on-prem; multi-tenant S/4HANA Cloud | Row-level multi-tenancy (ADR-005) | One deployment serves 1000+ tenants |

### 🌍 Industry Adaptability Matrix

| Fit | Industries | Gaps / Notes |
|-----|------------|--------------|
| ✅ **Excellent Fit (No Gaps)** | Distribution & Wholesale; **Retail (AI-Enhanced)**; Discrete Manufacturing; Professional Services; Healthcare; Telecommunications; Public Sector; Insurance | POS integration (ADR-024), **AI demand forecasting & dynamic pricing (ADR-056, ADR-057 - optional)**, project accounting (ADR-036), revenue recognition (ADR-022), CRM (ADR-043); **Enhanced**: Fund accounting (ADR-050), policy/claims (ADR-051) |
| ⚠️ **Good Fit (Minor Gaps)** | Banking & Finance; Process Manufacturing; Utilities; Real Estate | **Enhanced**: Advanced treasury (ADR-026 ext.), recipes/batch genealogy (ADR-037 ext.), meter-to-cash (ADR-042 ext.), property management (ADR-033 ext.) |
| ✅ **Strong Fit (Phase 3+)** | Oil & Gas | Joint venture accounting, upstream/midstream/downstream operations (future ADR-052) |

**Industry Coverage**: 8/13 excellent + 4/13 good = **92% market coverage** (up from 77% post-enhancements).

**Recent Enhancements (February 2026)**:
- **ADR-024 Extended**: Advanced Inventory Operations (packaging hierarchies, kitting, repack, catch weight) for retail/distribution/food & beverage
- **ADR-056 Created** (Optional): AI Demand Forecasting & Replenishment for retail (ARIMA, Prophet, XGBoost, LSTM, Transformers) — **closes CRITICAL gap with SAP IBP, Oracle RDFC, Blue Yonder** — $5M-$8M ROI for 100-store chain
- **ADR-057 Created** (Optional): Dynamic Pricing & Markdown Optimization for retail (price elasticity, markdown optimization, competitive intelligence, A/B testing) — **closes gap with SAP OPP, Oracle RPM** — $4M-$6M ROI for 100-store chain
- **ADR-026 Extended**: Advanced Treasury (derivatives, hedge accounting, cash pooling, investment management) for banking/finance
- **ADR-033 Extended**: Real Estate Property Management (lessor accounting, tenant management, property operations)
- **ADR-037 Extended**: Process Manufacturing (recipes, batch genealogy, co-products, continuous production) for food/beverage/pharma/chemicals
- **ADR-042 Extended**: Utilities (meter-to-cash, network assets, outage management, workforce dispatch)
- **ADR-050 Created**: Public Sector & Government (fund accounting, encumbrances, grants management, appropriations)
- **ADR-051 Created**: Insurance (policy administration, claims management, underwriting, actuarial, reinsurance)

### 🚀 Extensibility & Customization Adaptability

**Built-In Extensibility (ADR-049)**
- Hook points (pre/post hooks on 50+ business processes)
- Custom fields without schema changes
- Custom workflows (Temporal-based approval routing, ADR-046)
- Custom pricing rules (rule engine)
- Custom reports (SQL-based report builder, ADR-016)
- Custom integrations (event-driven API for 3rd-party systems)

**Localization Adaptability (ADR-047)**
- Country pack model: pluggable tax, accounting, and regulatory rules
- Kenya: KRA eTIMS, M-Pesa, VAT 16%, IFRS
- Tanzania: TRA VFD, M-Pesa, VAT 18%, IFRS
- Nigeria: FIRS, CBN, VAT 7.5%, IFRS
- South Africa: SARS eFiling, VAT 15%, IFRS
- US: Avalara, multi-state tax, US GAAP
- Target: 15 country packs by Phase 5 (Month 15)

**Deployment Adaptability**
- SMB mode: Docker Compose bundled (Phase 3)
- Mid-market: Kubernetes 3-5 nodes
- Enterprise: Kubernetes multi-region auto-scaling
- Cloud: AWS/GCP/Azure support
- On-premise: air-gapped deployment ready

### 🎯 Final Verdict
- **Domain Coverage**: A+ (98%) - **up from 95% post-industry enhancements**
- **Strengths**: Complete Finance/CO/MM/PP/SD/QM/PM domains + Advanced Inventory Operations + 7 industry-specific extensions
- **Gaps**: Joint venture accounting (Oil & Gas - future ADR-052), full HCM (intentional - partner ecosystem)
- **Comparison**: Matches SAP breadth, exceeds in analytics, cloud-native architecture, and industry-specific depth
- **Adaptability**: A+ (Excellent)

### Strategic Positioning
- **vs SAP**: win on cloud-native, 3-6 month implementation, $20K-$200K pricing (vs SAP's 12-24 months, $500K-$5M), **advanced inventory ops (kitting, catch weight) match SAP EWM depth**, **optional retail AI (ADR-056, ADR-057) competitive with SAP IBP/OPP at 1/3 the price**
- **vs Odoo**: win on enterprise depth (CO, intercompany, close, SoD) + **advanced inventory ops** (Odoo lacks packaging hierarchies, catch weight, kit ATP explosion) + **optional retail AI** (Odoo has NO AI demand forecasting or dynamic pricing)
- **vs NetSuite**: win on manufacturing (PP + QM + PM modules) + **optional retail AI** (NetSuite lacks AI demand forecasting, limited pricing optimization)
- **vs Dynamics 365**: win on African localization (Kenya/Tanzania/Nigeria packs) + **optional retail AI competitive with D365 Commerce**

### Roadmap Impact
The 18-month roadmap adds missing **product layer** (config, org model, workflow, localization, compliance). Post-roadmap, we reach **98% domain coverage + 100% product readiness** for the African mid-market, with deep vertical capabilities for retail, distribution, food & beverage, banking, utilities, public sector, insurance, and real estate. **Optional retail AI enhancement (ADR-056, ADR-057) closes competitive gap with enterprise retail systems (SAP, Oracle, Blue Yonder) for $50M-$500M retailers.**

### 💡 Bottom Line
ChiroERP already has SAP-grade domain coverage with **7 industry-specific extensions** (Banking, Process Mfg, Utilities, Public Sector, Insurance, Real Estate, Advanced Inventory). The gap isn't breadth — it's operationalization. After Phase 6: **12 customers, ~$645K ARR, 15 country packs, ISO 27001 + SOC 2, and 5+ certified partners**. That marks the shift from **"SAP-grade architecture" → "SAP-grade product."**

**For retail customers**: Optional **AI enhancement (ADR-056 + ADR-057)** generates **$10M-$16M ROI** (3 years, 100-store chain) and closes competitive gap with SAP/Oracle/Blue Yonder retail AI capabilities, positioning ChiroERP for $50M-$500M retail segment (50-500 stores).

## Phase Gate Framework

### Phase Gate Overview

Each phase has **explicit exit criteria** that must be met before proceeding. This prevents "90% done" syndrome and ensures production readiness.

| Phase | Duration | Investment | Exit Criteria | Business Outcome |
|-------|----------|------------|---------------|------------------|
| **Phase 1** | 3 months | $880K | Config + Org + Workflow engines operational | Platform foundation validated |
| **Phase 2** | 3 months | $880K | Tenancy + Finance MVP live (pilot customer, 1000+ tx/month) | Product-market fit proven |
| **Phase 3** | 3 months | $1,080K | 3 countries, 10K users, advanced inventory ops | Scale model validated + retail depth |
| **Phase 4** | 3 months | $880K | Enterprise deal ($500K+ ARR) signed | Enterprise viability proven |
| **Phase 5** | 3 months | $880K | 15 country packs, partner ecosystem | Geographic scale achieved |
| **Phase 6** | 3 months | $880K | ISO 27001 + SOC 2 certified | Enterprise-grade compliance |
| **Total** | 18 months | $5.48M | 12 customers, ~$645K ARR | Trajectory to profitability |

---

## Risk Register

### Critical Risks (Red - Phase Blockers)

| Risk | Probability | Impact | Mitigation | Owner |
|------|-------------|--------|------------|-------|
| **R1: Tenant isolation breach** | 30% | 🔴 HIGH | Mandatory tenant context enforcement, automated isolation tests, security review | Platform Lead |
| **R2: Config engine adoption < 80%** | 30% | 🔴 HIGH | Reference implementation as proof, config UI training | Product Manager |
| **R3: First customer churn (< 6 months)** | 25% | 🔴 HIGH | Dedicated customer success manager, weekly check-ins | Customer Success |
| **R4: Performance targets missed (< 10K users)** | 35% | 🔴 HIGH | Early load testing (Month 4), database optimization budget | Platform Lead |
| **R5: Localization content delivery delay** | 50% | 🔴 HIGH | Country pack template + partner model (Phase 5) | Localization Lead |

### High Risks (Orange - Timeline Impact)

| Risk | Probability | Impact | Mitigation | Owner |
|------|-------------|--------|------------|-------|
| **R6: Workflow UX adoption low** | 40% | 🟡 MEDIUM | User research (Month 2), iterative UI/UX design | Product Manager |
| **R7: Partner ecosystem ramp slow** | 60% | 🟡 MEDIUM | Partner portal + certification program (Phase 6) | VP Sales |
| **R7a: Advanced Inventory Ops complexity (Phase 3)** | 45% | 🟡 MEDIUM | Phased rollout (packaging → kitting → repack → catch weight), mock scale integration | Platform Lead |
| **R8: Enterprise deal cycle > 6 months** | 70% | 🟡 MEDIUM | POC kit (pre-built demos), reference customer program | VP Sales |
| **R9: Microservices complexity for SMB** | 50% | 🟡 MEDIUM | SMB bundled mode (docker-compose), Phase 3 | Platform Lead |
| **R10: ISO 27001 audit failure** | 20% | 🟡 MEDIUM | External consultant (Month 12), mock audit (Month 15) | Security Lead |

### Medium Risks (Yellow - Cost Impact)

| Risk | Probability | Impact | Mitigation | Owner |
|------|-------------|--------|------------|-------|
| **R11: Cloud costs exceed budget (> $15K/month)** | 40% | 🟢 LOW | Reserved instances, cost monitoring alerts | DevOps Lead |
| **R12: Team attrition (> 20%)** | 30% | 🟢 LOW | Competitive comp, equity, remote-first culture | VP Engineering |
| **R13: Scope creep (> 70 modules)** | 50% | 🟢 LOW | Strict phase gate reviews, "not now" backlog | CTO |

---

## Dependency Map (Critical Path)

### Phase 1 Dependencies
```
Config Engine (Week 1-6)
    ├─→ Pricing Rules (Week 7) [blocks O2C]
    ├─→ Tax Rules (Week 8) [blocks AR/AP invoicing]
    └─→ Posting Rules (Week 9) [blocks GL]

Org Model (Week 1-6)
    ├─→ Authorization (Week 7) [blocks multi-user]
    └─→ Scope Filtering (Week 8) [blocks sales/inventory]

Workflow Engine (Week 1-6)
    └─→ P2P Approval (Week 10) [blocks purchase orders]
```

### Phase 2 Dependencies (Tenancy + Finance MVP)
```
Tenant Provisioning (Week 13-14)
    ├─→ Tenant context enforcement [BLOCKING]
    └─→ Org + Auth + Config [BLOCKING]

Finance Core (Week 13-18)
    ├─→ GL Posting Rules [BLOCKING]
    ├─→ Tax Rules [BLOCKING]
    └─→ Workflow Engine (approvals) [BLOCKING]

Core O2C + P2P (Week 17-20)
    ├─→ Finance Core [BLOCKING]
    └─→ Config + Org + Workflow [BLOCKING]
```

### Phase 3 Dependencies (Scale)
```
Tanzania Country Pack (Week 25-28)
    └─→ Kenya Country Pack (template) [BLOCKING]

Performance Testing (Week 29-32)
    ├─→ Database Optimization [BLOCKING]
    └─→ Kafka Partitioning [BLOCKING]
```

**Critical Path**: Config Engine → Tenancy → Finance MVP → Pilot Customer → Scale Validation

---

## Gap Analysis: Architecture vs Product

### ✅ What We Have (SAP-Grade Already)

| Capability | Status | Evidence |
|------------|--------|----------|
| **Domain Breadth** | ✅ Complete | 10 domains (FI/CO/MM/PP/SD/QM/PM/CRM/MDG/Analytics) |
| **Architecture Modularity** | ✅ Best-in-class | Microservices + CQRS + Event-driven + Database-per-context |
| **Enterprise Finance** | ✅ SAP-grade | CO, Intercompany, Close, Leases, Revenue Recognition, Treasury |
| **Process Coverage** | ✅ Complete | P2P, O2C, Plan-to-Produce, Quality-to-Inventory, Maintenance-to-Cost |
| **Governance Standards** | ✅ Documented | ADR-010 (REST), ADR-014 (SoD), ADR-017 (Performance), ADR-019 (Testing) |
| **Multi-Tenancy** | ✅ Architected | ADR-005 (Row-level isolation + Tenant ID propagation) |
| **Configuration Framework** | ✅ Designed | ADR-044 (Dynamic config with versioning + audit) |
| **Org Model** | ✅ Designed | ADR-045 (Hierarchical with matrix support) |
| **Workflow Engine** | ✅ Designed | ADR-046 (Temporal-based with approval routing) |
| **Localization Model** | ✅ Designed | ADR-047 (Country packs with tax/CoA/formats) |
| **Extensibility** | ✅ Designed | ADR-049 (Pre/post hooks + event subscriptions) |

**Assessment**: Architecture is SAP-grade. Now we need to **operationalize** it.

---

### ⚠️ What We're Missing (Product Gaps)

| Gap | Impact | SAP's Advantage | Our Risk |
|-----|--------|-----------------|----------|
| **1. Config-First Operating Model** | 🔴 CRITICAL | 80-90% variation via config, not code | Custom code per customer → no scale |
| **2. Operational Org Model** | 🔴 CRITICAL | Consistent authorization + scope inheritance | Manual permissions → security gaps |
| **3. Localization Content Delivery** | 🔴 CRITICAL | 100+ country packs, regulatory updates | Cannot enter new markets fast |
| **4. Extensibility Catalog** | 🟡 HIGH | 1000+ documented hooks, partner certification | Breaking changes on upgrades |
| **5. Workflow UX** | 🟡 HIGH | Unified inbox, process guidance, mobile app | Poor user adoption |
| **6. Deployment Modes** | 🟡 HIGH | SMB (bundled) vs Enterprise (distributed) | Microservices overhead for SMB |
| **7. Production Proof** | 🔴 CRITICAL | 50+ years of edge cases, battle-tested | No real-world validation |
| **8. Partner Ecosystem** | 🟢 MEDIUM | 10,000+ consultants, pre-built templates | Implementation bottleneck |
| **9. UI/UX Maturity** | 🟢 MEDIUM | Fiori (consistent across all modules) | API-first (no UI yet) |
| **10. Upgrade Safety** | 🟡 HIGH | Zero-downtime upgrades, backward compat | Breaking changes possible |

---

## 18-Month Roadmap: Architecture → Product

### Phase 1: Platform Foundation (Months 1-3)
**Goal**: Build the "platform layer" that makes ChiroERP a product, not just APIs

**Investment**: $880K (3 months × $293K/month incl. team + infra + compliance)

#### Exit Criteria (Phase Gate 1)

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **Config Engine Operational** | Pricing + tax + posting rules drive 1 end-to-end O2C flow | 85%+ variation without custom code |
| **Org Model Operational** | Authorization scope filters sales orders by user's org unit | 100% of queries org-filtered |
| **Workflow Engine Operational** | P2P approval routes purchase orders through 3-level approval | 100% of POs use workflow |
| **Audit Trail Complete** | All config changes logged with who/what/when | Zero config changes without audit |
| **Version Management** | Config rollback tested (revert bad pricing rule) | < 5 min rollback time |
| **Reference Implementation** | 1 complete O2C flow (order → invoice → payment → GL) with config | Zero hardcoded rules |

**Definition of Done**:
- [ ] Configuration UI deployed (React admin panel)
- [ ] 10+ pricing rules configured (volume discount, customer segment)
- [ ] 5+ tax rules configured (reference VAT/GST, withholding rules)
- [ ] 20+ posting rules configured (IFRS accounts)
- [ ] 5+ approval rules configured (PO < $1K auto-approve, > $10K CEO approval)
- [ ] Org hierarchy loaded (3 levels: company → department → team)
- [ ] Authorization tested (user can only see own org's data)
- [ ] Workflow tested (approve/reject PO, escalation on timeout)
- [ ] Performance tested (1000 config rule evaluations/sec)
- [ ] Documentation complete (config admin guide, API docs)

**Business Outcome**: Platform foundation validated, ready for finance MVP pilot customer

---

#### 1.1 Configuration Engine MVP (ADR-044 Operationalization)

**Why Critical**: SAP wins because 85%+ variation is configuration, not custom code.

**What to Build**:

```kotlin
// Configuration Engine Core
@Service
class ConfigurationEngine(
    private val configRepository: ConfigRepository,
    private val versionManager: VersionManager,
    private val auditService: AuditService
) {
    // 1. Pricing Configuration (replace hardcoded pricing logic)
    fun evaluatePricingRule(
        productId: UUID,
        customerId: UUID,
        quantity: Int,
        orderDate: LocalDate,
        tenantId: UUID
    ): PricingResult {
        // Load active pricing rules for tenant
        val rules = configRepository.findActiveRules(
            tenantId = tenantId,
            configType = ConfigType.PRICING,
            effectiveDate = orderDate
        )

        // Evaluate rules in priority order
        return rules
            .sortedBy { it.priority }
            .firstNotNullOfOrNull { rule ->
                when (rule) {
                    is QuantityDiscountRule -> {
                        if (quantity >= rule.minQuantity) {
                            PricingResult(
                                basePrice = rule.unitPrice,
                                discount = rule.discountPercent,
                                finalPrice = rule.unitPrice * (1 - rule.discountPercent / 100)
                            )
                        } else null
                    }
                    is CustomerSegmentRule -> {
                        if (customerId in rule.customerIds) {
                            PricingResult(
                                basePrice = rule.unitPrice,
                                discount = rule.discountPercent,
                                finalPrice = rule.unitPrice * (1 - rule.discountPercent / 100)
                            )
                        } else null
                    }
                    else -> null
                }
            } ?: throw PricingException("No pricing rule found")
    }

    // 2. Tax Configuration (replace hardcoded tax logic)
    fun calculateTax(
        netAmount: BigDecimal,
        countryCode: String,
        taxCategory: TaxCategory,
        transactionDate: LocalDate,
        tenantId: UUID
    ): TaxResult {
        val taxRules = configRepository.findTaxRules(
            tenantId = tenantId,
            countryCode = countryCode,
            taxCategory = taxCategory,
            effectiveDate = transactionDate
        )

        val taxAmount = taxRules.fold(BigDecimal.ZERO) { acc, rule ->
            acc + netAmount * rule.rate / 100
        }

        return TaxResult(
            netAmount = netAmount,
            taxAmount = taxAmount,
            grossAmount = netAmount + taxAmount,
            appliedRules = taxRules.map { it.id }
        )
    }

    // 3. Posting Rule Configuration (replace hardcoded GL accounts)
    fun determineGLAccounts(
        transactionType: TransactionType,
        countryCode: String,
        businessArea: String,
        tenantId: UUID
    ): PostingRule {
        return configRepository.findPostingRule(
            tenantId = tenantId,
            transactionType = transactionType,
            countryCode = countryCode,
            businessArea = businessArea
        ) ?: throw PostingException("No posting rule found")
    }

    // 4. Approval Configuration (replace hardcoded approval limits)
    fun determineApprovers(
        documentType: DocumentType,
        amount: BigDecimal,
        requesterId: UUID,
        orgUnitId: UUID,
        tenantId: UUID
    ): List<ApproverId> {
        val approvalMatrix = configRepository.findApprovalMatrix(
            tenantId = tenantId,
            documentType = documentType,
            orgUnitId = orgUnitId
        )

        return approvalMatrix
            .filter { amount >= it.minAmount && amount < it.maxAmount }
            .sortedBy { it.level }
            .map { it.approverId }
    }

    // 5. Number Range Configuration (replace hardcoded sequences)
    fun getNextNumber(
        documentType: DocumentType,
        fiscalYear: Int,
        countryCode: String,
        tenantId: UUID
    ): String {
        val numberRange = configRepository.findNumberRange(
            tenantId = tenantId,
            documentType = documentType,
            fiscalYear = fiscalYear,
            countryCode = countryCode
        )

        val nextNumber = numberRange.currentNumber + 1

        // Update current number (atomic)
        configRepository.updateNumberRange(numberRange.id, nextNumber)

        return numberRange.format.format(nextNumber)  // e.g., "INV-2026-00001"
    }
}
```

**Configuration UI** (React Admin):
```typescript
// Configuration Management UI
const ConfigurationManager = () => {
  const [configType, setConfigType] = useState<ConfigType>('PRICING');

  return (
    <div className="config-manager">
      <Tabs value={configType} onChange={setConfigType}>
        <Tab value="PRICING">Pricing Rules</Tab>
        <Tab value="TAX">Tax Rules</Tab>
        <Tab value="POSTING">Posting Rules</Tab>
        <Tab value="APPROVAL">Approval Matrix</Tab>
        <Tab value="NUMBER_RANGE">Number Ranges</Tab>
      </Tabs>

      {configType === 'PRICING' && (
        <PricingRuleEditor
          onSave={(rule) => saveConfig(rule)}
          onTest={(rule) => testPricingRule(rule)}
        />
      )}

      {configType === 'TAX' && (
        <TaxRuleEditor
          countries={['KE', 'TZ', 'US', 'DE']}
          onSave={(rule) => saveConfig(rule)}
        />
      )}

      {/* ... other config types ... */}
    </div>
  );
};
```

**Deliverables**:
- [ ] Configuration Engine API (pricing, tax, posting, approval, number ranges)
- [ ] Configuration UI (React admin panel)
- [ ] Version management (config history + rollback)
- [ ] Audit logging (who changed what when)
- [ ] Testing framework (simulate config changes before production)

**Success Metric**: 85%+ of finance O2C variation handled via config (no code changes)

---

#### 1.2 Org Model Operationalization (ADR-045)

**Why Critical**: Enterprise authorization, scope inheritance, reporting hierarchy.

**What to Build**:

```kotlin
// Org Model Service
@Service
class OrgModelService(
    private val orgRepository: OrgRepository,
    private val authService: AuthorizationService
) {
    // 1. Org Hierarchy Traversal
    fun getOrgHierarchy(orgUnitId: UUID): OrgNode {
        val unit = orgRepository.findById(orgUnitId)
        val parent = unit.parentId?.let { getOrgHierarchy(it) }
        val children = orgRepository.findByParentId(orgUnitId).map { getOrgHierarchy(it.id) }

        return OrgNode(
            id = unit.id,
            name = unit.name,
            type = unit.type,
            parent = parent,
            children = children
        )
    }

    // 2. Authorization Scope Derivation
    fun deriveUserScope(userId: UUID, permission: Permission): Set<UUID> {
        val userAssignments = orgRepository.findUserAssignments(userId)

        return userAssignments.flatMap { assignment ->
            when (assignment.scopeType) {
                ScopeType.SELF -> setOf(assignment.orgUnitId)
                ScopeType.CHILDREN -> getDescendants(assignment.orgUnitId)
                ScopeType.ALL -> getAllOrgUnits(assignment.tenantId)
            }
        }.toSet()
    }

    // 3. Org-Based Data Filtering
    fun <T> filterByOrgScope(
        entities: List<T>,
        userId: UUID,
        permission: Permission,
        orgExtractor: (T) -> UUID
    ): List<T> {
        val allowedOrgs = deriveUserScope(userId, permission)
        return entities.filter { orgExtractor(it) in allowedOrgs }
    }

    // 4. Reporting Line Traversal
    fun getReportingChain(userId: UUID): List<UserId> {
        val user = orgRepository.findUserById(userId)
        val manager = user.managerId

        return if (manager != null) {
            listOf(manager) + getReportingChain(manager)
        } else {
            emptyList()
        }
    }

    // 5. Cost Center Inheritance
    fun getCostCenter(orgUnitId: UUID): CostCenter {
        val unit = orgRepository.findById(orgUnitId)

        return unit.costCenterId?.let { costCenterId ->
            costCenterRepository.findById(costCenterId)
        } ?: run {
            // Inherit from parent
            unit.parentId?.let { getCostCenter(it) }
        } ?: throw OrgException("No cost center found in hierarchy")
    }
}
```

**Org Model UI**:
```typescript
// Org Chart Visualization
const OrgChartViewer = () => {
  const { data: orgTree } = useOrgHierarchy();

  return (
    <OrgChart
      data={orgTree}
      onNodeClick={(node) => showOrgDetails(node)}
      renderNode={(node) => (
        <OrgNodeCard
          name={node.name}
          type={node.type}
          headCount={node.headCount}
          costCenter={node.costCenter}
        />
      )}
    />
  );
};
```

**Deliverables**:
- [ ] Org hierarchy API (CRUD + traversal)
- [ ] Authorization scope derivation (user → allowed org units)
- [ ] Org-based data filtering (sales orders, inventory, invoices)
- [ ] Reporting line API (manager chain)
- [ ] Org chart UI (visualization + drill-down)

**Success Metric**: 100% of Finance/Sales/Inventory queries filtered by user's org scope

---

#### 1.3 Workflow Engine MVP (ADR-046 Operationalization)

**Why Critical**: P2P approval routing, O2C credit checks, multi-level approvals.

**What to Build**:

```kotlin
// Workflow Engine (Temporal-based)
@Service
class WorkflowEngine(
    private val temporalClient: TemporalClient,
    private val configEngine: ConfigurationEngine
) {
    // 1. Start Workflow
    fun startApprovalWorkflow(
        documentId: UUID,
        documentType: DocumentType,
        amount: BigDecimal,
        requesterId: UUID,
        orgUnitId: UUID,
        tenantId: UUID
    ): WorkflowId {
        // Determine approvers from config
        val approvers = configEngine.determineApprovers(
            documentType = documentType,
            amount = amount,
            requesterId = requesterId,
            orgUnitId = orgUnitId,
            tenantId = tenantId
        )

        // Start Temporal workflow
        val workflow = temporalClient.newWorkflowStub(
            ApprovalWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue("approval-queue")
                .setWorkflowId("approval-${documentId}")
                .build()
        )

        WorkflowClient.start(workflow::processApproval, ApprovalRequest(
            documentId = documentId,
            documentType = documentType,
            approvers = approvers,
            requesterId = requesterId,
            tenantId = tenantId
        ))

        return workflow.workflowId
    }

    // 2. Approve/Reject
    fun processApprovalDecision(
        workflowId: WorkflowId,
        approverId: UUID,
        decision: ApprovalDecision,
        comments: String?
    ) {
        val workflow = temporalClient.newWorkflowStub(
            ApprovalWorkflow::class.java,
            workflowId
        )

        workflow.submitDecision(approverId, decision, comments)
    }

    // 3. Query Workflow State
    fun getWorkflowState(workflowId: WorkflowId): WorkflowState {
        val workflow = temporalClient.newWorkflowStub(
            ApprovalWorkflow::class.java,
            workflowId
        )

        return workflow.getState()
    }
}

// Temporal Workflow Definition
@WorkflowInterface
interface ApprovalWorkflow {
    @WorkflowMethod
    fun processApproval(request: ApprovalRequest): ApprovalResult

    @SignalMethod
    fun submitDecision(approverId: UUID, decision: ApprovalDecision, comments: String?)

    @QueryMethod
    fun getState(): WorkflowState
}

@Component
class ApprovalWorkflowImpl : ApprovalWorkflow {
    override fun processApproval(request: ApprovalRequest): ApprovalResult {
        val decisions = mutableListOf<ApprovalDecision>()

        // Sequential approval (Level 1 → Level 2 → Level 3)
        for ((level, approverId) in request.approvers.withIndex()) {
            // Wait for approval decision (signal)
            val decision = Workflow.await { decisions.size > level }

            if (decision == ApprovalDecision.REJECTED) {
                return ApprovalResult.REJECTED
            }
        }

        return ApprovalResult.APPROVED
    }

    override fun submitDecision(approverId: UUID, decision: ApprovalDecision, comments: String?) {
        // Store decision (Temporal will resume workflow)
        decisions.add(decision)
    }

    override fun getState(): WorkflowState {
        return WorkflowState(
            currentLevel = decisions.size + 1,
            totalLevels = request.approvers.size,
            decisions = decisions
        )
    }
}
```

**Workflow UI (Task Inbox)**:
```typescript
// Unified Task Inbox
const TaskInbox = () => {
  const { data: tasks } = usePendingTasks();

  return (
    <TaskList>
      {tasks.map((task) => (
        <TaskCard key={task.id}>
          <TaskHeader>
            {task.documentType} Approval - {task.documentNumber}
          </TaskHeader>
          <TaskDetails>
            Amount: {formatCurrency(task.amount)}
            Requester: {task.requesterName}
            Date: {formatDate(task.requestDate)}
          </TaskDetails>
          <TaskActions>
            <Button onClick={() => approve(task.id)}>Approve</Button>
            <Button onClick={() => reject(task.id)}>Reject</Button>
            <Button onClick={() => viewDetails(task.id)}>View</Button>
          </TaskActions>
        </TaskCard>
      ))}
    </TaskList>
  );
};
```

**Deliverables**:
- [ ] Workflow engine API (start, approve, reject, query)
- [ ] Temporal workflow implementations (P2P approval, O2C credit check)
- [ ] Task inbox UI (pending approvals)
- [ ] Workflow monitoring (stuck workflows, SLA breaches)
- [ ] Mobile app (approve on the go)

**Success Metric**: 100% of P2P purchase orders routed via workflow (no manual approvals)

---

### Phase 2: Tenancy + Finance MVP (Months 4-6)
**Goal**: Prove multi-tenant finance core works in production with a pilot customer

**Investment**: $880K (3 months × $293K/month incl. team + infra + compliance)

#### Exit Criteria (Phase Gate 2)

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **Tenant Isolation Verified** | Cross-tenant access tests pass | Zero data leakage in automated suite |
| **Finance Core Live** | GL/AP/AR posting + close | < 10 days month-end close |
| **Pilot Customer Live** | 10+ users, 1000+ transactions/month | Customer NPS > 30 |
| **O2C Flow Complete** | Order → Invoice → Payment → GL | < 3 P0 bugs/month |
| **P2P Flow Complete** | PR → PO (workflow) → Receipt → Payment | < 3 P0 bugs/month |
| **Performance Validated** | 1000 transactions/month processed | < 2s API response (p95) |
| **Configuration Coverage** | 80%+ finance variation via config | Zero customer-specific code |

**Definition of Done**:
- [ ] Tenant provisioning API + admin UI
- [ ] Tenant context enforced across services (filters + tests)
- [ ] GL/AP/AR live (posting rules, journals, invoices, bills, payments)
- [ ] Bank import/reconciliation (CSV or API)
- [ ] O2C and P2P flows wired end-to-end
- [ ] Finance team can close month-end (< 10 days)
- [ ] Pilot customer signed ($20K-$35K ARR)
- [ ] Customer onboarded (10+ users trained)
- [ ] 1000+ transactions processed (orders + invoices + payments)
- [ ] Customer success playbook created (weekly check-ins, escalation)
- [ ] Case study published (customer testimonial, metrics)
- [ ] Sales deck updated (live customer proof)

**Business Outcome**: Finance MVP validated, referenceable customer

---

#### 2.1 Tenant Provisioning & Isolation (Production)

**Deliverables**:
- [ ] Automated tenant creation (ID, schema/row-level partitioning)
- [ ] Tenant context propagation in API + messaging
- [ ] Cross-tenant access tests (negative test suite)
- [ ] Tenant-level quotas and limits

**Success Metric**: 0 cross-tenant data access in automated tests

---

#### 2.2 Finance Core (GL/AP/AR) (Production)

**Deliverables**:
- [ ] Chart of accounts + posting rules
- [ ] Journal entries + period close
- [ ] AR invoices + customer payments
- [ ] AP bills + vendor payments
- [ ] Reconciliation reports (bank + sub-ledgers)

**Success Metric**: 1,000+ finance postings/day with < 1% exception rate

---

#### 2.3 Core O2C Flow (Production)

**Deliverables**:
- [ ] Sales order entry (pricing from config)
- [ ] Credit check (workflow-based)
- [ ] Invoice generation
- [ ] Payment receipt
- [ ] GL posting (accounts from config)

**Success Metric**: 1,000 orders/month processed end-to-end

---

#### 2.4 Core P2P Flow (Production)

**Deliverables**:
- [ ] Purchase requisition
- [ ] PO approval (workflow-based, 3 levels)
- [ ] Goods receipt
- [ ] Invoice verification
- [ ] Payment + withholding handling from config

**Success Metric**: 500 POs/month processed end-to-end

---

#### 2.5 First Production Customer (Finance MVP)

**Target Customer Profile**:
- **Industry**: Distribution/Wholesale (fast O2C + P2P cycles)
- **Size**: 50-200 employees, $4M-$15M revenue
- **Pain Point**: Fragmented finance + manual approvals
- **Deal Size**: $20K-$35K annual license

**Customer Success Criteria**:
- [ ] 10+ users onboarded (sales + procurement + finance)
- [ ] 1,000+ transactions/month (orders + invoices + payments)
- [ ] < 3 P0 bugs/month (after go-live)

---

### Phase 3: Scale Foundation (Months 7-9)
**Goal**: Prove ChiroERP scales to 10K users and 3 countries + deliver advanced inventory operations for retail/distribution

**Investment**: $1,080K (3 months × $293K/month + $200K Advanced Inventory Operations)

#### Exit Criteria (Phase Gate 3)

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **3 Country Packs Operational** | Kenya + Tanzania + US customers live | 3 customers, 300+ users total |
| **10K User Load Test Passed** | k6 load test with 10K concurrent users | < 2s API response (p95) |
| **Database Performance** | 10,000 GL postings/hour sustained | < 100ms query time (p95) |
| **Kafka Performance** | 10,000 events/minute sustained | < 5s consumer lag |
| **Multi-Region Deployment** | Kenya (Africa) + US (Americas) regions | < 200ms cross-region latency |
| **Monitoring Operational** | Grafana dashboards, Prometheus alerts | 99.9% uptime achieved |
| **Advanced Inventory Ops Live** | Packaging hierarchy + kitting + repack + catch weight | 2+ customers using advanced features |

**Definition of Done**:
- [ ] Tanzania customer live (100+ users, TRA VFD certified)
- [ ] US customer live (50+ users, Avalara sales tax integrated)
- [ ] Load testing completed (10K users, all scenarios passed)
- [ ] Database optimized (partitioning, read replicas, Redis caching)
- [ ] Kafka optimized (10 partitions per topic, consumer lag < 5s)
- [ ] Multi-region tested (Kenya + US data centers)
- [ ] Disaster recovery tested (RPO 1 hour, RTO 4 hours)
- [ ] Monitoring dashboards live (KRA eTIMS, M-Pesa, API, DB, Kafka)
- [ ] On-call rotation established (24/7 support)
- [ ] Runbook created (incident response, escalation)
- [ ] **Advanced Inventory Operations operational (4 modules live)**
- [ ] **1+ retail/distribution customer using advanced inventory features**
- [ ] **Advanced Inventory performance SLOs met (< 200ms kit ATP, < 100ms catch weight)**

**Business Outcome**: Scale model validated, ready for enterprise

---

#### 3.1 Tanzania Country Pack

**Deliverables**:
- [ ] TRA VFD integration (verification codes)
- [ ] Tanzania tax rules (VAT 18%, WHT)
- [ ] M-Pesa Tanzania (Vodacom API)
- [ ] EAC customs documentation

**Success Metric**: 1 Tanzania customer live (100+ users, 500 transactions/month)

---

#### 3.2 US Country Pack (Global Proof)

**Deliverables**:
- [ ] US-GAAP accounting (different from IFRS)
- [ ] Sales tax (state-level, Avalara integration)
- [ ] ACH payments (Plaid integration)
- [ ] 1099 reporting (contractor payments)

**Success Metric**: 1 US customer live (50+ users, 200 transactions/month)

---

#### 3.3 Performance Validation

**Deliverables**:
- [ ] Load testing (10K concurrent users)
- [ ] Database optimization (partitioning, read replicas)
- [ ] Kafka optimization (10+ partitions)
- [ ] API response time < 2s (p95)

**Success Metric**: Pass all performance tests from performance-testing-guide.md

---

#### 3.4 Advanced Inventory Operations (ADR-024 Extension)

**Rationale**: Retail and distribution customers require sophisticated inventory operations (packaging hierarchies, kitting, repack, catch weight) that go beyond basic stock ledger. This extension is critical for winning deals in:
- **Retail**: Multi-level packaging (pallet → case → each), catch weight (meat, produce), gift basket kitting
- **Distribution/Wholesale**: Break bulk operations, vendor compliance (Ti/Hi, GTIN), master pack creation
- **Food & Beverage**: Recipe kits, catch weight ingredients, portion control, promotional bundles
- **Manufacturing**: Component kitting (assemble-to-order), subassembly management

**Strategic Timing**: Phase 3 is optimal because:
1. Kenya/Tanzania customers (grocery chains, distributors) need these features
2. US customer proof-of-concept benefits from retail depth
3. Differentiates from Odoo/NetSuite (which lack this depth)
4. Foundation for Phase 4 enterprise deals (multi-location retailers, CPG companies)

**Deliverables**:
- [ ] **Packaging Hierarchy Management** (Month 7)
  - Multi-level packaging structures (pallet → case → inner pack → each)
  - GTIN/barcode per level (GTIN-14, GTIN-13, GTIN-12, UPC)
  - Dimensions and weight per level (cube/freight calculations)
  - Ti/Hi configuration (cases per layer, layers per pallet)
  - UOM conversion validation across packaging levels
  - REST API: 10 endpoints for packaging hierarchy CRUD

- [ ] **Kitting & Bundling Operations** (Month 7-8)
  - Kit types: Static (pre-built), Dynamic (build-to-order), Virtual (logical grouping), Configurable (customer-selected)
  - Kit BOM management with substitution rules
  - Kit ATP explosion (min of component availability)
  - Kit assembly/disassembly workflows with variance tracking
  - Component reservation (atomic, all-or-nothing)
  - Kit cost rollup (sum of component costs)
  - REST API: 15 endpoints for kit operations

- [ ] **Repackaging & VAS (Value-Added Services)** (Month 8)
  - Break bulk operations (split larger units into smaller: pallet → cases → each)
  - Repackaging workflows (change packaging without changing SKU)
  - Master pack creation (build display-ready packs)
  - Deconsolidation (split mixed pallets by SKU)
  - VAS operations: labeling, gift wrapping, customization
  - Repack work orders with labor/material cost capture
  - REST API: 12 endpoints for repack operations

- [ ] **Catch Weight / Variable Weight** (Month 9)
  - Nominal vs actual weight tracking (1 lb nominal = 1.03 actual)
  - Dual UOM (count for stocking, weight for sales)
  - Scale integration for receipt and POS (REST API for scale devices)
  - Average weight calculation for planning (forecasting, MRP)
  - Pricing by actual weight with tare weight handling
  - USDA/FDA compliance for food products
  - Catch weight variance and shrink tracking
  - REST API: 8 endpoints for catch weight operations

**Database Schema**:
- `packaging_level` table (sku, level, gtin, dimensions, weight, ti, hi)
- `kit` table (kit_sku, kit_type, assembly_location, pricing_method)
- `kit_component` table (kit_sku, component_sku, quantity, substitution_group)
- `kit_assembly_order` table (assembly_order_number, kit_sku, status, variance)
- `repack_order` table (repack_order_number, source_packaging, target_packaging, status)
- `vas_order` table (vas_order_number, service_type, labor_hours, material_cost)
- `catch_weight_item` table (sku, nominal_weight, average_actual_weight)
- `catch_weight_lot` table (lot_number, nominal_weight, actual_weight, receipt_date)
- `catch_weight_transaction` table (transaction_id, nominal_weight, actual_weight, variance)

**Integration Points**:
- **Core Inventory (ADR-024)**: Stock movements, reservations, valuation
- **WMS (ADR-038)**: Physical execution of repack/assembly work orders
- **Procurement (ADR-023)**: Receive by pallet, catch weight receiving
- **Sales (ADR-025)**: Kit order processing, catch weight pricing, sell by case
- **Controlling (ADR-028)**: Kit cost rollup, repack labor/material costs
- **Quality (ADR-039)**: Repack quality checks, catch weight variance monitoring

**KPIs / SLOs**:
- Kit ATP calculation latency: < 200ms p99
- Kit assembly cycle time: < 4 hours p95 (dynamic kits)
- Repack completion time: < 2 hours p95
- Catch weight recording latency: < 100ms p99
- Packaging hierarchy accuracy: >= 99.9% (correct UOM conversions)
- Kit component variance: < 0.5% (assembly accuracy)
- Repack accuracy: >= 99.5% (variance within tolerance)
- Catch weight accuracy: >= 99.9% (scale integration reliability)

**Testing & Validation**:
- [ ] Unit tests: Kit ATP explosion, UOM conversion, catch weight variance
- [ ] Integration tests: End-to-end kit assembly flow, break bulk flow, catch weight flow
- [ ] Performance tests: 1000 kit ATP checks/second, 10,000 catch weight records/day
- [ ] Compliance tests: GTIN uniqueness, FDA/USDA catch weight accuracy, lot traceability

**Resource Requirements**:
- **Team**: 2 senior engineers (backend), 1 senior engineer (frontend), 1 QA engineer
- **Duration**: 3 months (parallel with country packs and performance validation)
- **Cost**: $200K (included in Phase 3 $880K budget)

**Success Metrics**:
- Advanced Inventory Operations operational (all 4 modules live)
- 1+ retail/distribution customer using packaging hierarchy + kitting
- 1+ food & beverage customer using catch weight
- Performance SLOs met (< 200ms kit ATP, < 100ms catch weight recording)
- Integration validated with Core Inventory, WMS, Sales, Procurement

**Risk Mitigation**:
- **Risk**: Complexity delay (3 months aggressive for 4 modules)
  - **Mitigation**: Phased rollout (packaging hierarchy first, then kitting, then repack, then catch weight)
- **Risk**: Scale integration complexity (multiple systems: WMS, POS scales, Core Inventory)
  - **Mitigation**: REST API abstraction layer, mock scale integration for testing
- **Risk**: Customer adoption (advanced features require training)
  - **Mitigation**: Reference implementation with Kenya grocery chain, training videos, UI wizards

**Documentation**:
- Implementation architecture: [inventory-advanced-ops.md](../inventory/inventory-advanced-ops.md)
- API specification: Advanced Ops API Reference (45 REST endpoints)
- User guide: Advanced Inventory Operations Training (packaging setup, kit BOM, repack workflows, catch weight configuration)

---

#### 3.5 Retail AI Enhancement (Optional - Post-Phase 3)

**Rationale**: With advanced inventory operations in place (Phase 3), retail customers can benefit from AI-powered demand forecasting and dynamic pricing capabilities that leverage the operational foundation. This enhancement closes competitive gaps with enterprise retail systems (SAP IBP, Oracle RDFC, Blue Yonder, SAP OPP, Oracle RPM).

**Strategic Value**:
- Closes **CRITICAL** competitive gap with SAP/Oracle/Blue Yonder retail AI capabilities
- Improves retail AI maturity from **6/10 → 8.5/10**
- Generates **$10M-$16M cumulative ROI** (3 years, 100-store chain)
- Differentiates ChiroERP in $50M-$500M retail segment (50-500 stores)

**Prerequisites**:
- ✅ Phase 3 Advanced Inventory Operations complete (packaging, kitting, catch weight)
- ✅ ADR-024 (Inventory Management) operational
- ✅ ADR-025 (Sales & Distribution) operational
- ✅ ADR-046 (Workflow & Approval Engine) operational
- ✅ ADR-016 (Analytics & Reporting) operational

**Two-Phase AI Implementation** (18 months total, can run parallel to Phase 4-6):

##### Phase AI-1: AI Demand Forecasting & Replenishment (Months 1-9)
**ADR**: ADR-056 (AI Demand Forecasting & Replenishment)
**Investment**: $500K-$1M (2-3 data scientists, 9 months)
**ROI**: 3-5X in Year 1 = $5M-$8M savings (100-store chain)

**Core Capabilities** (7 major components):
1. **Time Series Forecasting Engine**: ARIMA, Prophet, XGBoost, LSTM, Transformers (progressive enhancement)
2. **Seasonality Detection**: Weekly, monthly, annual cycles (STL decomposition, Fourier series)
3. **Promotion Impact Modeling**: BOGO, discounts, bundles with uplift calculations
4. **External Signal Integration**: Weather, events, competitor activity (API integration)
5. **Automatic Reorder Points**: Dynamic ROP = (Avg Daily Demand × Lead Time) + Safety Stock with Z-scores
6. **Multi-Echelon Optimization**: DC → store allocation with capacity constraints
7. **Forecast Accuracy Monitoring**: MAPE, MAE, RMSE tracking with auto-retraining triggers

**Implementation Phases**:
- **Phase 1 (Months 1-3)**: Classical time series (ARIMA, Prophet), reorder point calculator, forecast dashboard, buyer workflow
- **Phase 2 (Months 4-6)**: Advanced ML (XGBoost, ensemble), promotion modeling, weather/event APIs, what-if analysis
- **Phase 3 (Months 7-9)**: Deep learning (LSTM, Transformers), multi-echelon optimizer, real-time demand sensing, MLOps pipeline (MLflow)

**Success Metrics**:
- Forecast accuracy: MAPE ≤15% (Phase 2), ≤10% (Phase 3)
- Stockout reduction: 15% → 5-8% (≥50% improvement)
- Excess inventory reduction: 25% → 10-15% (≥40% improvement)
- Buyer adoption rate: ≥75%
- System uptime: ≥99.5%

**Business Impact** (100-store chain):
- Without AI: 15-20% stockouts (lost sales) + 25-30% excess inventory (markdown losses)
- With AI: 5-8% stockouts + 10-15% excess → **$5M-$8M ROI**
- Inventory carrying cost reduction: 10-15%
- Sales uplift from improved availability: 5-10%
- Buyer productivity: Automated recommendations free buyers for strategic work

**Technical Architecture**:
- **Data Model**: 7 entities (DemandForecast, ForecastModel, ReorderPoint, PromotionPlan, ExternalSignal, ForecastAccuracy, ScenarioAnalysis)
- **Workflows**: Daily forecast generation, automatic replenishment recommendations, promotion impact analysis, model retraining
- **Integration**: ADR-024 (Inventory), ADR-025 (Sales), ADR-023 (Procurement), ADR-046 (Workflow)
- **Events**: DemandForecastGeneratedEvent, ReplenishmentRecommendationCreatedEvent, ForecastAccuracyDegradedEvent
- **Technology**: Python, Prophet, XGBoost, TensorFlow/PyTorch, MLflow

**Competitive Impact**: ✅ **CLOSES CRITICAL GAP** with SAP IBP, Oracle RDFC, Blue Yonder Luminate Demand

##### Phase AI-2: Dynamic Pricing & Markdown Optimization (Months 10-18)
**ADR**: ADR-057 (Dynamic Pricing & Markdown Optimization)
**Investment**: $300K-$500K (1-2 data scientists, 9 months)
**ROI**: 2-3X in Year 1 = $4M-$6M margin improvement (100-store chain)
**Prerequisites**: ADR-056 (demand forecasting) REQUIRED

**Core Capabilities** (7 major components):
1. **Price Elasticity Modeling**: Log-log regression, XGBoost, hierarchical models (|Elasticity| > 1 = elastic, < 1 = inelastic)
2. **Markdown Optimization**: Multi-stage schedules (15-25% early → 30-40% mid → 50-70% late season) to maximize gross margin
3. **Clearance Acceleration**: Slow-mover detection via Weeks of Supply (WOS) and Sell-Through Rate (STR)
4. **Competitive Pricing Intelligence**: Web scraping, price monitoring, Competitive Price Index (CPI) tracking
5. **Promotion Effectiveness Analysis**: Incremental lift calculation, cannibalization + halo effect measurement
6. **A/B Testing Framework**: Control vs. treatment groups, statistical validation (t-tests), rollout decisions
7. **Revenue Management**: Balance sell-through vs. average selling price (ASP) with dynamic pricing

**Implementation Phases**:
- **Phase 1 (Months 10-12)**: Price elasticity modeling (12+ months historical data), basic markdown optimization (WOS-based), competitive dashboard, merchandiser workflow
- **Phase 2 (Months 13-15)**: Advanced optimization (multi-stage markdowns), A/B testing platform, automated competitor scraping, promotion ROI dashboard
- **Phase 3 (Months 16-18)**: Real-time pricing engine (<1 hour response), revenue management optimizer, advanced analytics (cannibalization, cross-elasticity)

**Success Metrics**:
- Elasticity model accuracy: R² ≥ 0.70
- Gross margin improvement: +2-3%
- Clearance inventory reduction: 5-10% (units >90 days old at season-end)
- Markdown timing: 2-4 weeks earlier vs. manual
- Merchandiser adoption: ≥70%
- Competitive price parity: CPI 95-105 for 80% of SKUs
- A/B test success rate: ≥60% show significant improvement

**Business Impact** (100-store chain):
- Without AI: 10-15% excess + 3-5% margin erosion from late/deep markdowns
- With AI: <5% excess + 1-2% margin improvement → **$4M-$6M ROI**
- Particularly valuable for fashion/apparel retailers with high seasonality
- Competitive responsiveness: Automated alerts, faster price adjustments

**Technical Architecture**:
- **Data Model**: 6 entities (PriceElasticity, MarkdownRecommendation, CompetitorPrice, PromotionROI, ABTestExperiment, PriceHistory)
- **Workflows**: Weekly markdown recommendations, competitive pricing monitoring, promotion effectiveness analysis, A/B test execution
- **Integration**: ADR-056 (Demand Forecasting - CRITICAL), ADR-025 (Sales), ADR-024 (Inventory), ADR-046 (Workflow)
- **Events**: MarkdownRecommendationCreatedEvent, CompetitivePriceAlertEvent, PromotionROICalculatedEvent
- **Technology**: Python, XGBoost, statistical modeling, web scraping tools

**Competitive Impact**: ✅ **CLOSES GAP** with SAP OPP, Oracle RPM, Revionics, Competera

**Combined Business Impact Summary** (3-Year, 100-Store Apparel Retailer):

| Metric | Baseline (Year 0) | +ADR-056 (Year 1) | +ADR-057 (Year 2) | Year 3 (Sustained) |
|--------|-------------------|-------------------|-------------------|---------------------|
| **Stockouts** | 15% | 6-8% | 5-8% | 5-8% |
| **Excess Inventory** | 25% | 12-15% | 10-15% | 10-15% |
| **Gross Margin** | 40% | 40-41% | 42-43% | 42-43% |
| **Cumulative Savings** | - | $5M-$8M | $9M-$14M | $10M-$16M |

**Recommended Sequencing**:
1. **ADR-056 First** (Months 1-9): Most critical competitive gap, highest ROI (3-5X), foundation for pricing optimization
2. **ADR-057 Second** (Months 10-18): Requires ADR-056 as prerequisite (demand elasticity needs forecasting baseline)
3. **Integration Third** (Year 3+): Partner with Shopify/BigCommerce (recommendations), Salesforce/HubSpot (segmentation, LTV)

**Alternative Strategy (Lower Priority AI)**:
- **Don't Build**: Personalized product recommendations (integrate with ecommerce platforms like Shopify/BigCommerce)
- **Don't Build**: Customer segmentation & LTV (integrate with marketing platforms like Salesforce/HubSpot/Klaviyo)
- **Don't Build**: Planogram optimization (partner with specialized vendors like Trax/RELEX/Blue Yonder Space Planning)
- **Rationale**: ChiroERP focuses on **operational AI** (supply chain, inventory, workforce) not customer-facing AI — partner ecosystem for ecommerce/marketing AI

**Competitive Positioning After AI Implementation**:

| Capability | ChiroERP (Before) | ChiroERP (After AI) | SAP Retail | Oracle Retail | Blue Yonder |
|------------|-------------------|---------------------|------------|---------------|-------------|
| **Multi-Store Inventory** | A+ | A+ | A+ | A+ | A+ |
| **POS Synchronization** | A+ | A+ | A+ | A+ | A+ |
| **Omnichannel Orders** | A | A | A+ | A+ | A |
| **Demand Forecasting AI** | ❌ | ✅ **A (ADR-056)** | A+ (IBP) | A+ (RDFC) | A+ (Luminate) |
| **Dynamic Pricing AI** | ❌ | ✅ **A (ADR-057)** | A (OPP) | A (RPM) | A (Pricing) |
| **Labor Forecasting AI** | B+ | B+ | A | A | A+ |
| **Recommendations** | ❌ | ⚠️ **Integrate** | A+ | A+ | A |
| **Customer Analytics** | ❌ | ⚠️ **Integrate** | A+ (CDP) | A+ (CX) | A |

**Target Market**: $50M-$500M revenue retailers (50-500 stores) — **differentiated vs. NetSuite/Odoo** (no AI), **cost-effective vs. SAP/Oracle** (1/3 price, 6-month implementation vs. 18-24 months)

**Documentation**:
- ADR-056: AI Demand Forecasting & Replenishment (docs/adr/ADR-056-ai-demand-forecasting-replenishment.md)
- ADR-057: Dynamic Pricing & Markdown Optimization (docs/adr/ADR-057-dynamic-pricing-markdown-optimization.md)

---

### Phase 4: Enterprise Features (Months 10-12)
**Goal**: Win $500K+ enterprise deal (bank/telecom/manufacturing)

**Investment**: $880K (3 months × $293K/month incl. team + infra + compliance)

#### Exit Criteria (Phase Gate 4)

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **Enterprise Deal Signed** | $500K+ ARR contract with bank/telecom/manufacturing | LOI signed, 50% upfront |
| **SoD Operational** | Segregation of Duties rules enforced | Zero SoD violations |
| **Intercompany Accounting** | 3+ legal entities with intercompany transactions | Automated elimination entries |
| **Period Close Orchestration** | Month-end close in < 5 days (down from 10-15 days) | Finance team approval |
| **Security Audit Passed** | Penetration test by Big 4 firm | Zero critical vulnerabilities |
| **Multi-Tenant Isolation** | 100+ tenants, zero data leakage | Security audit validated |

**Definition of Done**:
- [ ] Enterprise customer signed ($500K+ ARR)
- [ ] SoD rules configured (create PO ≠ approve PO ≠ pay PO)
- [ ] SoD violations detected (real-time alerts)
- [ ] Intercompany tested (3 legal entities, 100+ transactions)
- [ ] Elimination entries automated (consolidated reporting)
- [ ] Period close workflow operational (10+ steps, parallel execution)
- [ ] Close monitoring dashboard (bottleneck identification)
- [ ] Security audit completed (Big 4 firm)
- [ ] Penetration test passed (OWASP Top 10)
- [ ] Multi-tenant isolation tested (100+ tenants, no data leakage)

**Business Outcome**: Enterprise viability proven, ready for scale

---

#### 4.1 Advanced Authorization (SoD)

**Deliverables**:
- [ ] Segregation of Duties rules (ADR-014)
- [ ] Role-based access control (RBAC)
- [ ] Audit logging (all financial transactions)

**Success Metric**: Pass security audit from Big 4 firm

---

#### 4.2 Intercompany Accounting

**Deliverables**:
- [ ] Intercompany transactions (ADR-029)
- [ ] Automated elimination entries
- [ ] Consolidated reporting

**Success Metric**: Multi-entity customer live (3+ legal entities)

---

#### 4.3 Period Close Orchestration

**Deliverables**:
- [ ] Month-end close workflow (ADR-031)
- [ ] Close monitoring dashboard
- [ ] Automated reconciliations

**Success Metric**: Month-end close in < 5 days (down from 10-15 days manual)

---

### Phase 5: Localization Scale (Months 13-15)
**Goal**: 10+ country packs (Africa expansion)

**Investment**: $880K (3 months × $293K/month incl. team + infra + compliance)

#### Exit Criteria (Phase Gate 5)

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **15 Country Packs Operational** | 15 countries installed with tax + CoA + integrations | 15 customers (1 per country) |
| **Country Pack Factory** | Add 1 new country pack in < 2 weeks (not 3 months) | Template-driven creation |
| **Partner Ecosystem** | 5+ certified implementation partners | 30% of deals via partners |
| **Regulatory Content Pipeline** | Quarterly tax/CoA updates automated | Zero manual updates |
| **Localization Documentation** | Country pack admin guide per country | Partner self-serve enabled |

**Definition of Done**:
- [ ] 15 country packs installed (Kenya, Tanzania, US, Nigeria, South Africa, Ghana, Uganda, Rwanda, Ethiopia, Zambia, Botswana, Namibia, Malawi, Zimbabwe, Mozambique)
- [ ] Country pack template created (tax rules, CoA, formats, integrations)
- [ ] Regulatory content pipeline operational (quarterly updates)
- [ ] 5 partners certified (Kenya, Tanzania, Nigeria, South Africa, US)
- [ ] Partner portal live (training, certification, deal registration)
- [ ] Implementation methodology documented (project templates)
- [ ] 30% of new deals through partners (not direct sales)
- [ ] Localization playbook created (per-country checklist)
- [ ] Translation support (English, Swahili, French - 3 languages)
- [ ] Multi-currency tested (15 currencies, real-time FX rates)

**Business Outcome**: Geographic scale achieved, partner-led growth model

---

#### 5.1 Country Pack Factory

**Deliverables**:
- [ ] Country pack template (tax + CoA + formats + integrations)
- [ ] Regulatory content pipeline (quarterly updates)
- [ ] Partner certification (implementation partners per country)

**Success Metric**: Add 1 new country pack/month

---

#### 5.2 Target Countries (Africa Priority)

**Q1 2027**:
- [ ] Nigeria (VAT 7.5%, FIRS e-invoicing)
- [ ] South Africa (VAT 15%, SARS eFiling)
- [ ] Ghana (VAT 12.5%, GRA integration)

**Q2 2027**:
- [ ] Uganda (VAT 18%, URA e-invoicing)
- [ ] Rwanda (VAT 18%, RRA integration)
- [ ] Ethiopia (VAT 15%, ERCA integration)

**Success Metric**: 15 country packs by end of Phase 5

---

### Phase 6: Enterprise-Grade (Months 16-18)
**Goal**: Win $500K+ enterprise deal, pass ISO 27001 audit

**Investment**: $880K (3 months × $293K/month incl. team + infra + compliance)

#### Exit Criteria (Phase Gate 6)

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **ISO 27001 Certified** | Certificate issued by accredited body | Stage 1 + 2 audits passed |
| **SOC 2 Type II Report** | Report issued by Big 4 auditor | 6-month observation complete |
| **$500K+ Enterprise Deal** | Contract signed with Bank / Telecom / Manufacturing | LOI signed |
| **50% Partner-Led Deals** | 50% of new deals closed by partners (not direct) | Partner ecosystem mature |
| **12 Customers, ~$645K ARR** | 12 production customers, ~$645K ARR (cumulative) | Trajectory to profitability validated |
| **Churn Rate < 10%** | Annual churn rate below 10% | Customer success validated |

**Definition of Done**:
- [ ] ISO 27001 certificate issued (valid 3 years)
- [ ] SOC 2 Type II report published (6-month observation)
- [ ] $500K+ enterprise deal signed (Bank / Telecom / Manufacturing)
- [ ] 12 customers live (3 Kenya, 2 Tanzania, 1 US, 6 other Africa)
- [ ] ~$645K ARR achieved (cumulative)
- [ ] 50% of deals via partners (not direct sales)
- [ ] Churn rate < 10% (2-3 churned customers max)
- [ ] NPS > 50 (customer satisfaction high)
- [ ] Expansion revenue > 20% (upsell/cross-sell working)
- [ ] Product roadmap v2 (next 18 months planned)

**Business Outcome**: Enterprise-grade compliance, trajectory to profitability

---

#### 6.1 ISO 27001 Certification

**Deliverables**:
- [ ] ISMS implementation (6 months)
- [ ] Stage 1 audit (documentation)
- [ ] Stage 2 audit (on-site)
- [ ] Certification issued

**Success Metric**: ISO 27001 certificate by Month 18

---

#### 6.2 SOC 2 Type II (US Customers)

**Deliverables**:
- [ ] Control implementation (security, availability, integrity)
- [ ] 6-month observation period
- [ ] Audit by Big 4 firm

**Success Metric**: SOC 2 Type II report by Month 18

---

#### 6.3 Partner Ecosystem

**Deliverables**:
- [ ] Partner portal (training, certification, deal registration)
- [ ] Implementation methodology (project templates)
- [ ] 5+ certified partners (Kenya, Tanzania, Nigeria, South Africa, US)

**Success Metric**: 50% of new deals through partners (by end of Phase 6)

---

## Deployment Strategy: SMB vs Enterprise

### Problem
Microservices = overhead for SMB (< 100 users).

### Solution: Tiered Deployment Modes

#### Mode 1: SMB Bundled (< 100 users, < $100K revenue)
```yaml
# docker-compose-smb.yml
services:
  chiroerp-all-in-one:
    image: chiroerp/all-in-one:latest
    environment:
      - DEPLOYMENT_MODE=SMB
      - POSTGRES_HOST=db
      - KAFKA_ENABLED=false  # Use in-memory event bus
    ports:
      - "8080:8080"

  db:
    image: postgres:15

  redis:
    image: redis:7
```

**Characteristics**:
- Single JVM (all services co-located)
- Shared database (multi-schema, not multi-database)
- In-memory event bus (no Kafka overhead)
- Vertical scaling only

**Pricing**: $500-$2,000/month

---

#### Mode 2: Mid-Market Distributed (100-1000 users, $100K-$1M revenue)
```yaml
# docker-compose-midmarket.yml
services:
  api-gateway:
    image: chiroerp/api-gateway:latest

  finance-service:
    image: chiroerp/finance:latest
    replicas: 2

  sales-service:
    image: chiroerp/sales:latest
    replicas: 2

  inventory-service:
    image: chiroerp/inventory:latest
    replicas: 2

  postgres-finance:
    image: postgres:15

  postgres-sales:
    image: postgres:15

  kafka:
    image: confluentinc/cp-kafka:latest
```

**Characteristics**:
- Service decomposition (3-5 core services)
- Database-per-service
- Kafka for events
- Horizontal scaling (2-4 replicas per service)

**Pricing**: $5,000-$20,000/month

---

#### Mode 3: Enterprise Kubernetes (1000+ users, $1M+ revenue)
```yaml
# kubernetes-enterprise.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: finance-service
spec:
  replicas: 10
  template:
    spec:
      containers:
      - name: finance
        image: chiroerp/finance:latest
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: finance-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: finance-service
  minReplicas: 10
  maxReplicas: 50
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

**Characteristics**:
- Full microservices decomposition (15+ services)
- Multi-region deployment
- Auto-scaling (10-50 replicas per service)
- Observability (Grafana, Prometheus, Jaeger)
- Multi-tenancy (1000+ tenants)

**Pricing**: $50,000-$200,000/month

---

## Competitive Positioning (Post-Roadmap)

### vs SAP

| Dimension | ChiroERP (Post-Roadmap) | SAP | Winner |
|-----------|-------------------------|-----|--------|
| **Architecture** | Cloud-native, event-driven, CQRS | Monolith (S/4HANA modernizing) | 🟢 ChiroERP |
| **Functional Breadth** | 10 domains, 70+ modules | 15+ domains, 1000+ transactions | 🔴 SAP (but 80% overlap) |
| **Configuration** | 85%+ via config (Phase 1) | 90%+ via config | 🟡 Tie |
| **Localization** | 15 country packs (Phase 5) | 100+ country packs | 🔴 SAP |
| **Implementation Time** | 3-6 months | 12-24 months | 🟢 ChiroERP |
| **Cost** | $20K-$200K/year | $500K-$5M/year | 🟢 ChiroERP |
| **Extensibility** | Partner-safe hooks (Phase 1) | Modification-heavy (breaking) | 🟢 ChiroERP |
| **UX** | Modern React (Phase 3) | Fiori (improving) | 🟡 Tie |
| **Partner Ecosystem** | 5-10 partners (Phase 6) | 10,000+ partners | 🔴 SAP |
| **Production Proof** | 3 years (post-roadmap) | 50+ years | 🔴 SAP |

**Target Market**: SAP replacement for mid-market (500-5000 employees) in Africa + emerging markets.

---

### vs Odoo

| Dimension | ChiroERP (Post-Roadmap) | Odoo | Winner |
|-----------|-------------------------|-----|--------|
| **Architecture** | Microservices, CQRS, Kafka | Modular monolith (Python) | 🟢 ChiroERP |
| **Enterprise Finance** | Full CO, Intercompany, Close | Basic accounting | 🟢 ChiroERP |
| **Governance (SoD, Audit)** | ADR-014, full audit trail | Limited | 🟢 ChiroERP |
| **Localization** | 15 country packs (Phase 5) | 80+ country packs | 🔴 Odoo |
| **Configuration** | 85%+ via config | 60% via config (Python code) | 🟢 ChiroERP |
| **Deployment Flexibility** | SMB/Enterprise modes | Single mode (monolith) | 🟢 ChiroERP |
| **Cost** | $20K-$200K/year | $10K-$100K/year | 🔴 Odoo |
| **Implementation Time** | 3-6 months | 2-4 months | 🔴 Odoo |
| **Partner Ecosystem** | 5-10 partners | 500+ partners | 🔴 Odoo |

**Target Market**: Odoo replacement for enterprises (500+ employees) needing CO/Intercompany/SoD.

---

## Investment Requirements (Revised)

### Team (18 months)

| Role | Headcount | Cost/Month | Total (18 mo) | Rationale |
|------|-----------|------------|---------------|-----------|
| **Engineering** | 12 | $120K | $2.16M | 3 teams: Platform (4), Product (4), Infra (4) |
| **Product** | 2 | $20K | $360K | Product Manager + UX Designer |
| **QA** | 4 | $30K | $540K | Manual + Automation (1:3 ratio) |
| **DevOps** | 2 | $20K | $360K | Kubernetes + CI/CD + Monitoring |
| **Localization** | 3 | $20K | $360K | Country pack specialists (Kenya, Tanzania, Nigeria) |
| **Sales** | 3 | $25K | $450K | Regional AEs (Africa, US, Global) |
| **Implementation** | 4 | $30K | $540K | Customer success + Professional services |
| **Total** | 30 | $265K/mo | **$4.77M** | |

---

### Infrastructure (18 months)

| Item | Cost/Month | Total (18 mo) | Rationale |
|------|------------|---------------|-----------|
| **Cloud (AWS/GCP)** | $15K | $270K | Kubernetes (EKS/GKE), 3 regions, auto-scaling |
| **Kafka (Confluent Cloud)** | $5K | $90K | 10 partitions per topic, 7-day retention |
| **Monitoring (Datadog)** | $2K | $36K | APM + Logs + Infrastructure monitoring |
| **Testing (k6, JMeter)** | $1K | $18K | Load testing, performance regression |
| **Total** | $23K/mo | **$414K** | |

---

### Certifications & Compliance

| Item | Timeline | Cost | Rationale |
|------|----------|------|-----------|
| **KRA eTIMS (Kenya)** | Month 8 | $2K | Device registration, production approval (Phase 3 localization) |
| **M-Pesa Daraja (Kenya)** | Month 9 | Free | Safaricom certification (Phase 3 localization) |
| **TRA VFD (Tanzania)** | Month 8 | $1.5K | VFD license, TRA approval |
| **ISO 27001** | Month 12-18 | $30K | ISMS implementation + Stage 1/2 audits |
| **SOC 2 Type II** | Month 12-18 | $50K | 6-month observation + Big 4 audit |
| **Penetration Testing** | Month 10 | $15K | OWASP Top 10, vulnerability scan |
| **Total** | 18 months | **$98.5K** | |

---

### Total Investment: **$5.28M** over 18 months

| Category | Amount | % of Total |
|----------|--------|------------|
| **Team** | $4.77M | 90.3% |
| **Infrastructure** | $414K | 7.8% |
| **Certifications** | $98.5K | 1.9% |
| **Total** | **$5.28M** | 100% |

**Funding Strategy** (18-Month Roadmap):
- **Seed Round**: $2M (cover first 6 months, Phase 1-2 = $1.76M + $240K buffer)
- **Series A**: $4M (cover remaining 12 months, Phase 3-6 = $3.52M + $480K buffer)
- **Total Raise**: $6M (18-month execution $5.28M + $720K buffer)

**Note**: 3-year financial model below assumes continued $293K/month burn → $10.548M total investment over 36 months, requiring $11M total funding ($6M for 18-month roadmap + $5M for Year 3 expansion).

---

## RACI Matrix (Ownership per Workstream)

### Phase 1-2: Platform + Tenancy + Finance MVP

| Workstream | Responsible | Accountable | Consulted | Informed |
|------------|-------------|-------------|-----------|----------|
| **Config Engine** | Platform Lead | CTO | Product Manager | VP Engineering |
| **Org Model** | Platform Lead | CTO | Security Lead | VP Engineering |
| **Workflow Engine** | Platform Lead | CTO | Product Manager | VP Engineering |
| **Tenant Provisioning** | Platform Lead | CTO | Security Lead | VP Engineering |
| **Finance Core (GL/AP/AR)** | Finance Domain Lead | VP Engineering | Product Manager | CTO |
| **Pilot Customer (Finance MVP)** | Implementation Lead | VP Sales | Customer Success | CEO |

---

### Phase 3-4: Scale + Enterprise

| Workstream | Responsible | Accountable | Consulted | Informed |
|------------|-------------|-------------|-----------|----------|
| **Tanzania Country Pack** | Localization Lead (Tanzania) | VP Engineering | Backend Lead | CTO |
| **US Country Pack** | Localization Lead (US) | VP Engineering | Backend Lead | CTO |
| **Performance Testing** | QA Lead | VP Engineering | DevOps Lead | CTO |
| **Database Optimization** | DevOps Lead | VP Engineering | Platform Lead | CTO |
| **SoD Implementation** | Security Lead | CTO | Product Manager | VP Engineering |
| **Intercompany Accounting** | Finance Domain Lead | VP Engineering | Product Manager | CTO |
| **Enterprise Deal** | VP Sales | CEO | Implementation Lead | Board |

---

### Phase 5-6: Localization Scale + Compliance

| Workstream | Responsible | Accountable | Consulted | Informed |
|------------|-------------|-------------|-----------|----------|
| **Country Pack Factory** | Localization Lead | VP Engineering | Platform Lead | CTO |
| **15 Country Packs** | Localization Team (3) | Localization Lead | Regional Partners | VP Engineering |
| **Partner Program** | VP Sales | CEO | Marketing | Board |
| **ISO 27001** | Security Lead | CTO | External Consultant | CEO |
| **SOC 2 Type II** | Security Lead | CTO | Big 4 Auditor | CEO |
| **$500K+ Enterprise Deal** | VP Sales | CEO | Implementation Lead | Board |

---

## Revenue Projections (Revised - Realistic)

**Context**: The 18-month roadmap focuses on building the platform and achieving initial traction (12 customers, ~$645K ARR by Month 18). The 3-year financial model below extends this trajectory to profitability, assuming continued investment at $293K/month to support sales scale, customer implementation, and infrastructure growth from 12 → 50 customers.

- **18-Month Roadmap** (Phase 1-6): $5.28M investment → 12 customers, $645K ARR (Month 18)
- **Year 2 Continuation** (Months 13-24): $3.516M additional investment → 25 customers, $2.015M ARR
- **Year 3 Scale** (Months 25-36): $3.516M additional investment → 50 customers, $8.155M ARR, **profitability achieved**

### Assumptions

| Assumption | Value | Rationale |
|------------|-------|-----------|
| **Pipeline Multiplier** | 3x | Need $1.5M pipeline to close $500K ARR |
| **Conversion Rate** | 33% | 1 in 3 qualified leads converts to customer |
| **Sales Cycle** | 3-6 months | Mid-market: 3 months, Enterprise: 6 months |
| **Avg Deal Size (Year 1)** | $30K | SMB/Mid-market deals only |
| **Avg Deal Size (Year 2)** | $80K | Mix of mid-market ($50K) + enterprise ($200K) |
| **Avg Deal Size (Year 3)** | $150K | 70% enterprise deals |
| **Annual Churn** | 10% | Industry standard for SMB/mid-market ERP |
| **Expansion Revenue** | 20% | Upsell (more users) + cross-sell (more modules) |
| **Partner Mix (Year 3)** | 50% | 50% deals via partners, 50% direct |

---

### Year 1 (Months 1-12) - Prove Product-Market Fit

| Quarter | New Customers | Lost Customers | Net Customers | Avg ARR | New ARR | Churned ARR | Net ARR | Cumulative ARR |
|---------|---------------|----------------|---------------|---------|---------|-------------|---------|----------------|
| **Q1** | 0 | 0 | 0 | $0 | $0 | $0 | $0 | **$0** |
| **Q2** | 1 (Pilot customer) | 0 | 1 | $30K | $30K | $0 | $30K | **$30K** |
| **Q3** | 2 (Pilot + Regional) | 0 | 3 | $30K | $60K | $0 | $60K | **$90K** |
| **Q4** | 2 (Tanzania, US) | 0 | 5 | $30K | $60K | $0 | $60K | **$150K** |

**Year 1 Total**: 5 customers, $150K ARR
**Investment**: $3.516M (12 months × $293K aligned with 18-month roadmap)
**Burn Multiple**: 23.4 (NOT GOOD - expected in Year 1)

---

### Year 2 (Months 13-24) - Scale to 25 Customers

| Quarter | New Customers | Lost Customers | Net Customers | Avg ARR | New ARR | Churned ARR | Expansion ARR | Net ARR | Cumulative ARR |
|---------|---------------|----------------|---------------|---------|---------|-------------|---------------|---------|----------------|
| **Q1** | 3 (Nigeria, South Africa, Ghana) | 0 | 8 | $60K | $180K | $0 | $15K | $195K | **$345K** |
| **Q2** | 4 (Uganda, Rwanda, US) | 0 | 12 | $70K | $280K | $0 | $20K | $300K | **$645K** |
| **Q3** | 6 (Ethiopia, Zambia, Botswana, etc.) | 1 (churn) | 17 | $80K | $480K | -$30K | $30K | $480K | **$1.125M** |
| **Q4** | 9 (Enterprise deals start) | 1 (churn) | 25 | $100K | $900K | -$60K | $50K | $890K | **$2.015M** |

**Year 2 Total**: 25 customers (20 net new), $2.015M ARR
**Investment**: $3.516M (12 months × $293K aligned with 18-month roadmap)
**Burn Multiple**: 1.75 (IMPROVING - path to profitability)

---

### Year 3 (Months 25-36) - Scale to 50 Customers + Profitability

| Quarter | New Customers | Lost Customers | Net Customers | Avg ARR | New ARR | Churned ARR | Expansion ARR | Net ARR | Cumulative ARR |
|---------|---------------|----------------|---------------|---------|---------|-------------|---------------|---------|----------------|
| **Q1** | 8 (50% via partners) | 2 (churn) | 31 | $120K | $960K | -$120K | $100K | $940K | **$2.955M** |
| **Q2** | 10 (Enterprise focus) | 3 (churn) | 38 | $150K | $1.5M | -$180K | $150K | $1.47M | **$4.425M** |
| **Q3** | 12 (Geographic expansion) | 4 (churn) | 46 | $180K | $2.16M | -$240K | $200K | $2.12M | **$6.545M** |
| **Q4** | 8 (Slowing growth) | 4 (churn) | 50 | $200K | $1.6M | -$240K | $250K | $1.61M | **$8.155M** |

**Year 3 Total**: 50 customers (25 net new), $8.155M ARR
**Investment**: $3.516M (12 months × $293K aligned with 18-month roadmap)
**Burn Multiple**: 0.43 (PROFITABLE - revenue > costs)
**Gross Margin**: 75% (typical SaaS)
**Gross Profit**: $6.1M
**Operating Costs**: $3.516M
**EBITDA**: **+$2.584M (32% margin)** ← **PROFITABILITY ACHIEVED**

---

### 3-Year Summary

| Metric | Year 1 | Year 2 | Year 3 | Total |
|--------|--------|--------|--------|-------|
| **New Customers** | 5 | 20 | 25 | 50 |
| **Churned Customers** | 0 | 2 | 13 | 15 |
| **Net Customers** | 5 | 25 | 50 | 50 |
| **ARR** | $150K | $2.015M | $8.155M | $8.155M |
| **Investment** | $3.516M | $3.516M | $3.516M | $10.548M |
| **Burn Multiple** | 23.4 | 1.75 | 0.43 | 1.29 |
| **EBITDA** | -$3.366M | -$1.501M | +$2.584M | -$2.283M |

**Cumulative Cash Burn**: $2.283M (offset by $2.584M Year 3 profit → Net positive $301K)
**Break-Even**: Month 32 (Q3 Year 3)
**Funding Requirement**: $11M Series A (to cover $10.548M investment + $500K buffer)

---

### 18-Month Roadmap Target (End of Phase 6)
- **Investment**: $5.28M over 18 months ($293K/month burn rate)
- **Customers**: 12 customers by Month 18 (Year 2 Q2)
- **ARR**: ~$645K by Month 18
- **Business Outcome**: Platform proven, enterprise-grade compliance achieved (ISO 27001 + SOC 2), trajectory to profitability validated

### 3-Year Financial Target (Conservative)
- **Year 3 ARR**: $8.155M (50 customers)
- **Total Investment**: $10.548M over 36 months (assumes continued $293K/month burn)
- **Profitability**: Month 32 (Q3 Year 3), EBITDA +$2.584M (32% margin)
- **Funding**: $11M total ($6M for 18-month roadmap + $5M for Year 3 expansion)
- **Valuation (Exit)**: $40M-$80M at 5-10x ARR (Year 3)

**Note**: The 3-year model assumes continued investment post-roadmap at the same burn rate ($293K/month) to support sales, implementation, and infrastructure scale from 12 → 50 customers.

---

## Success Metrics (18-Month)

### Product Metrics (Technical Excellence)

| Metric | Target | Current | Phase Gate |
|--------|--------|---------|------------|
| **Configuration Coverage** | 85%+ variation without custom code | 0% (not built) | Phase 1 exit |
| **Localization** | 15 country packs operational | 0 (not built) | Phase 5 exit |
| **Performance** | 10,000 concurrent users, < 2s API (p95) | Not tested | Phase 3 exit |
| **Uptime** | 99.9% (43 min downtime/month) | N/A | Phase 3 exit |
| **Test Coverage** | 80%+ code coverage (unit + integration) | 0% (not built) | Phase 1 exit |
| **API Response Time** | < 2s p95, < 5s p99 | Not measured | Phase 3 exit |
| **Database Query Time** | < 100ms p95 | Not measured | Phase 3 exit |
| **Kafka Consumer Lag** | < 5s during peak load | Not measured | Phase 3 exit |
| **Security Vulnerabilities** | Zero critical (OWASP Top 10) | Not audited | Phase 4 exit |

---

### Business Metrics (Market Validation)

| Metric | Year 1 | Year 2 | Year 3 | Rationale |
|--------|--------|--------|--------|-----------|
| **Customers** | 5 | 25 | 50 | Realistic growth: 5 → 25 → 50 |
| **ARR** | $150K | $2.015M | $8.155M | Conservative deal sizes |
| **Avg Deal Size** | $30K | $80K | $163K | Mid-market → Enterprise mix |
| **Enterprise Deals (> $500K)** | 0 | 1 | 5 | Enterprise viability by Year 2 |
| **Churn Rate** | 0% | 8% | 10% | Industry standard SMB/mid-market |
| **Expansion Revenue** | 0% | 20% | 20% | Upsell/cross-sell working |
| **Partner Mix** | 0% | 20% | 50% | Partner ecosystem maturing |
| **NPS** | N/A | 30+ | 50+ | Customer satisfaction |
| **Gross Margin** | 50% | 70% | 75% | Typical SaaS trajectory |
| **EBITDA Margin** | -2013% | -58% | +36% | **Profitability Month 32** |

---

### Operational Metrics (Execution Discipline)

| Metric | Target | Measurement | Phase Gate |
|--------|--------|-------------|------------|
| **Implementation Time** | 8-12 weeks | Time from contract to go-live | Phase 2 exit |
| **P0 Bugs** | < 3/month per customer | Production incidents (post go-live) | Phase 2 exit |
| **Month-End Close** | < 5 days (down from 10-15 days) | Finance team feedback | Phase 4 exit |
| **Config Rule Evaluation** | 1000 rules/sec | Load testing benchmark | Phase 1 exit |
| **KRA eTIMS Cu Retrieval** | 95%+ success rate, < 2s latency | Production monitoring | Phase 2 exit |
| **M-Pesa Payment Success** | 98%+ success rate | Production monitoring | Phase 2 exit |
| **Security Audit** | Pass Big 4 audit (zero critical) | External audit report | Phase 4 exit |
| **ISO 27001** | Certificate issued | Certification body | Phase 6 exit |
| **SOC 2 Type II** | Report issued | Big 4 auditor | Phase 6 exit |

---

### Leading Indicators (Early Warning System)

| Metric | Target | Red Flag | Action |
|--------|--------|----------|--------|
| **Pipeline (3x ARR)** | $450K (Q2 Year 1) | < $300K | Increase marketing spend |
| **Lead Conversion** | 33% | < 20% | Sales training, demo improvements |
| **Sales Cycle** | 3-6 months | > 9 months | POC kit, reference customers |
| **Trial-to-Paid** | 50% | < 30% | Onboarding improvements, pricing |
| **Daily Active Users** | 80% of licenses | < 50% | UX improvements, training |
| **Config Coverage** | 85%+ | < 70% | Expand config engine capabilities |
| **Support Ticket Volume** | < 10/customer/month | > 20 | Product quality issues, documentation |
| **Engineering Velocity** | 40 story points/sprint | < 25 | Team health, technical debt |
| **Deployment Frequency** | 2x/week | < 1x/week | CI/CD pipeline issues |
| **Mean Time to Recovery** | < 1 hour | > 4 hours | On-call process, runbooks |

---

## Risk Mitigation

### Risk 1: Implementation Complexity (70+ modules)
**Mitigation**: Tiered deployment modes (SMB bundled vs Enterprise distributed)

### Risk 2: Localization Content Delivery
**Mitigation**: Country pack factory + partner certification per country

### Risk 3: No Production Proof
**Mitigation**: Finance MVP (Phase 2) validates architecture with real customer

### Risk 4: SAP Ecosystem Advantage (10K+ partners)
**Mitigation**: Target SAP-dissatisfied mid-market (500-5000 employees) in Africa

### Risk 5: Microservices Overhead for SMB
**Mitigation**: All-in-one bundled mode (< 100 users)

---

## Next Actions (Month 1)

### Week 1-2: Configuration Engine Foundation
- [ ] Design config schema (pricing, tax, posting, approval, number range)
- [ ] Build config repository (PostgreSQL + versioning)
- [ ] Implement config API (CRUD + query)

### Week 3-4: Pricing Config MVP
- [ ] Build pricing evaluation engine
- [ ] Create pricing rule UI (React admin)
- [ ] Test with reference pricing rules (volume discount, customer segment)

### Month 2: Tax + Posting Config
- [ ] Build tax calculation engine (reference VAT/GST, withholding rules)
- [ ] Build posting rule engine (IFRS accounts)
- [ ] Test with core O2C flow (sales order → invoice → GL posting)

### Month 3: Approval Config + Workflow
- [ ] Build approval matrix config
- [ ] Integrate with Temporal workflow engine
- [ ] Test with core P2P flow (PR → PO approval → payment)

---

**Ready to execute?** Let me know if you want me to:
1. **Create detailed sprint plans** (2-week sprints for Phase 1)
2. **Build configuration engine code** (Kotlin + React)
3. **Set up pilot finance environment** (tenant provisioning + bank sandbox)

---

## Appendix A: Phase Gate Checklist Template

### Phase Gate Review Template

**Phase**: [Phase 1 / 2 / 3 / 4 / 5 / 6]
**Review Date**: [YYYY-MM-DD]
**Attendees**: CTO, VP Engineering, VP Sales, CEO, Board Observer
**Decision**: [ ] GO / [ ] NO-GO / [ ] CONDITIONAL GO

---

#### Exit Criteria Status

| Criterion | Status | Evidence | Blocker? |
|-----------|--------|----------|----------|
| [Criterion 1] | 🟢 PASS / 🟡 PARTIAL / 🔴 FAIL | [Link to test results] | Yes/No |
| [Criterion 2] | 🟢 PASS / 🟡 PARTIAL / 🔴 FAIL | [Link to customer data] | Yes/No |
| [Criterion 3] | 🟢 PASS / 🟡 PARTIAL / 🔴 FAIL | [Link to audit report] | Yes/No |
| [Criterion 4] | 🟢 PASS / 🟡 PARTIAL / 🔴 FAIL | [Link to metrics] | Yes/No |
| [Criterion 5] | 🟢 PASS / 🟡 PARTIAL / 🔴 FAIL | [Link to documentation] | Yes/No |
| [Criterion 6] | 🟢 PASS / 🟡 PARTIAL / 🔴 FAIL | [Link to benchmark] | Yes/No |

---

#### Definition of Done Status

| Deliverable | Status | Owner | Completion Date |
|-------------|--------|-------|-----------------|
| [Deliverable 1] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 2] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 3] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 4] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 5] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 6] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 7] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 8] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 9] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |
| [Deliverable 10] | ✅ DONE / 🟡 IN PROGRESS / ❌ NOT STARTED | [Name] | [YYYY-MM-DD] |

---

#### Risk Assessment

| Risk ID | Status | Impact | Mitigation Effectiveness |
|---------|--------|--------|--------------------------|
| R1 | 🟢 RESOLVED / 🟡 MITIGATED / 🔴 ACTIVE | HIGH/MED/LOW | [Comments] |
| R2 | 🟢 RESOLVED / 🟡 MITIGATED / 🔴 ACTIVE | HIGH/MED/LOW | [Comments] |
| R3 | 🟢 RESOLVED / 🟡 MITIGATED / 🔴 ACTIVE | HIGH/MED/LOW | [Comments] |
| R4 | 🟢 RESOLVED / 🟡 MITIGATED / 🔴 ACTIVE | HIGH/MED/LOW | [Comments] |
| R5 | 🟢 RESOLVED / 🟡 MITIGATED / 🔴 ACTIVE | HIGH/MED/LOW | [Comments] |

---

#### Financial Status

| Metric | Target | Actual | Variance |
|--------|--------|--------|----------|
| **Investment (Phase)** | $795K | $[Actual] | [+/- %] |
| **ARR (Cumulative)** | $[Target] | $[Actual] | [+/- %] |
| **Burn Multiple** | [Target] | [Actual] | [+/- %] |
| **Customer Count** | [Target] | [Actual] | [+/- %] |

---

#### Decision Criteria

**GO Conditions**:
- [ ] All critical exit criteria passed (🟢)
- [ ] All Definition of Done items completed (✅)
- [ ] No unmitigated red risks (🔴)
- [ ] Budget variance < 20%
- [ ] ARR variance < 30%
- [ ] Customer churn < 20%

**NO-GO Triggers**:
- [ ] Any critical exit criterion failed (🔴)
- [ ] Unmitigated red risk with HIGH impact
- [ ] Budget overrun > 50%
- [ ] Customer churn > 20%
- [ ] Production P0 incidents > 5/month

**CONDITIONAL GO**:
- [ ] Partial completion (🟡) with clear remediation plan within 2 weeks
- [ ] Budget variance 20-50% with cost reduction plan approved by CFO
- [ ] ARR variance 30-50% with pipeline recovery plan (3x coverage)
- [ ] One red risk with mitigation plan + executive sponsor

---

**Decision**: [GO / NO-GO / CONDITIONAL GO]

**Next Phase Start Date**: [YYYY-MM-DD]
**Review Cadence**: Weekly status meetings (team), Monthly steering committee (execs), Quarterly board review

**Action Items**:
1. [Action item 1] - Owner: [Name] - Due: [Date]
2. [Action item 2] - Owner: [Name] - Due: [Date]
3. [Action item 3] - Owner: [Name] - Due: [Date]

---

## Appendix B: Reference Implementation Checklists

### B.1 Order-to-Cash (O2C) Flow

**Scope**: Sales Order → Pricing → Credit Check → Invoice (KRA eTIMS) → Payment (M-Pesa) → GL Posting

| Step | Config Required | Code Required | Test Scenario | Success Criteria | Status |
|------|-----------------|---------------|---------------|------------------|--------|
| **1. Sales Order Entry** | Pricing rules (volume discount) | Order API | 100 orders with different quantities | Order created with correct pricing | ❌ |
| **2. Pricing Evaluation** | Customer segment rules | Pricing engine | VIP customer gets 10% discount | Discount applied automatically | ❌ |
| **3. Credit Check** | Credit limit per customer | Workflow engine | Order > credit limit → approval | Approval workflow triggered | ❌ |
| **4. Invoice Generation** | Tax rules (VAT 16%) | Invoice API | Invoice with line items + tax | Invoice has correct tax | ❌ |
| **5. KRA eTIMS Submission** | Device serial, org code | eTIMS integration | Cu number retrieved < 2s | 95%+ Cu retrieval success | ❌ |
| **6. M-Pesa Payment** | Paybill shortcode | M-Pesa C2B callback | C2B payment received | 98%+ payment success | ❌ |
| **7. Payment Application** | Payment matching rules | Payment API | Payment applied to invoice | Invoice marked paid | ❌ |
| **8. GL Posting** | IFRS accounts, posting rules | GL API | Dr: Bank, Cr: Revenue + VAT | Correct GL entries | ❌ |
| **9. Reconciliation** | Daily M-Pesa statement | Reconciliation engine | All payments matched | Zero unmatched payments | ❌ |

**Success Criteria**:
- 100 orders processed end-to-end with **zero manual intervention**
- 85%+ variation driven by configuration (not code)
- < 3 P0 bugs/month in production
- **Phase 1 Exit**: 1 complete O2C flow with zero hardcoded rules

---

### B.2 Procure-to-Pay (P2P) Flow

**Scope**: Purchase Requisition → PO Approval → Goods Receipt → Invoice Verification (KRA eTIMS) → Payment (M-Pesa/RTGS) → GL Posting

| Step | Config Required | Code Required | Test Scenario | Success Criteria | Status |
|------|-----------------|---------------|---------------|------------------|--------|
| **1. Purchase Requisition** | Approval matrix (amount-based) | PR API | PR < $1K auto-approve | Auto-approval works | ❌ |
| **2. PO Approval** | 3-level approval workflow | Temporal workflow | PO > $10K → CEO approval | Workflow escalates correctly | ❌ |
| **3. PO Creation** | Vendor master, tax rules | PO API | PO with line items + VAT | PO created with tax | ❌ |
| **4. Goods Receipt** | Tolerance (qty variance) | GR API | GR 98 units (PO 100 units) | Tolerance accepted | ❌ |
| **5. Invoice Verification** | 3-way match (PO-GR-Invoice) | Invoice verification | Invoice matches PO + GR | 3-way match passes | ❌ |
| **6. KRA Cu Validation** | eTIMS API | Cu number check | Supplier invoice has valid Cu | Cu validation passes | ❌ |
| **7. Withholding VAT** | Withholding VAT 6% | Tax calculation | Non-registered supplier → 6% WHT | WHT calculated correctly | ❌ |
| **8. Payment (M-Pesa)** | M-Pesa B2B API | Payment API | Small supplier < $1K → M-Pesa | M-Pesa payment successful | ❌ |
| **9. Payment (RTGS)** | Bank integration (RTGS Kenya) | Payment API | Large supplier > $10K → RTGS | RTGS payment initiated | ❌ |
| **10. GL Posting** | IFRS accounts, posting rules | GL API | Dr: Expense, Cr: Bank + WHT | Correct GL entries | ❌ |

**Success Criteria**:
- 50 POs processed end-to-end with **3-way match + withholding tax**
- 100% of POs use workflow (no manual routing)
- < 3 P0 bugs/month in production
- **Phase 1 Exit**: 1 complete P2P flow with 3-level approval

---

### B.3 Month-End Close Orchestration

**Scope**: Period Lock → GL Balances → Reconciliations → Eliminations → Financial Statements → Unlock

| Step | Config Required | Code Required | Test Scenario | Success Criteria | Status |
|------|-----------------|---------------|---------------|------------------|--------|
| **1. Period Lock** | Close calendar | Period lock API | Lock Month 1 | Users can't post to closed period | ❌ |
| **2. GL Balance Check** | Account hierarchy | GL balance query | All accounts balanced | Dr = Cr for all postings | ❌ |
| **3. Bank Reconciliation** | Bank accounts | Reconciliation engine | Match bank statement | All transactions matched | ❌ |
| **4. Intercompany Elimination** | IC accounts | Elimination engine | 3 legal entities | IC balances zeroed out | ❌ |
| **5. Trial Balance** | Report layout | Financial reporting | Generate trial balance | Balanced + drill-down works | ❌ |
| **6. P&L Statement** | IFRS mapping | Financial reporting | Generate P&L | Revenue - Expenses = Net Income | ❌ |
| **7. Balance Sheet** | IFRS mapping | Financial reporting | Generate balance sheet | Assets = Liabilities + Equity | ❌ |
| **8. Cash Flow Statement** | Cash flow categories | Financial reporting | Generate cash flow | Operating + Investing + Financing | ❌ |
| **9. Audit Trail** | Change log | Audit report | All GL changes logged | Who/What/When captured | ❌ |
| **10. Period Unlock** | Close calendar | Period lock API | Unlock for adjustments | Users can post adjustments | ❌ |

**Success Criteria**:
- Month-end close in **< 5 days** (down from 10-15 days)
- Zero manual elimination entries
- Audit trail complete (100% of GL changes logged)
- **Phase 4 Exit**: Close orchestration operational with finance team approval

---

### B.4 KRA eTIMS Integration (Kenya)

**Scope**: Device Registration → Cu Number Retrieval → Invoice Submission → Receipt Printing

| Step | Config Required | Code Required | Test Scenario | Success Criteria | Status |
|------|-----------------|---------------|---------------|------------------|--------|
| **1. Device Registration** | Device serial, org code | eTIMS device API | Register 5 virtual devices | All devices registered | ❌ |
| **2. Sandbox Testing** | Sandbox credentials | eTIMS test API | Submit 100 test invoices | 95%+ Cu retrieval | ❌ |
| **3. Production Approval** | Tax compliance letter | KRA application | Apply for production | Production API key received | ❌ |
| **4. Invoice Submission** | Sales tax rules | eTIMS submit API | Submit 1000 invoices | 95%+ Cu retrieval < 2s | ❌ |
| **5. Error Handling** | Retry logic | eTIMS error handling | Simulate network failure | Auto-retry works | ❌ |
| **6. Receipt Printing** | Thermal printer config | Receipt API | Print 100 receipts | QR code scannable | ❌ |
| **7. Daily Sales Report** | Report format | eTIMS report API | Submit daily sales | Report accepted by KRA | ❌ |
| **8. Monthly VAT Return** | VAT return format | VAT return API | File monthly return | Return filed successfully | ❌ |

**Success Criteria**:
- **95%+ Cu retrieval success rate** in production
- **< 2s latency** p95
- Zero data loss (invoice submission idempotent)
- **Phase 2 Exit**: 1000+ invoices submitted with Cu numbers

---

### B.5 M-Pesa Integration (Kenya)

**Scope**: Daraja API Registration → C2B Payments → B2B Payments → Reconciliation

| Step | Config Required | Code Required | Test Scenario | Success Criteria | Status |
|------|-----------------|---------------|---------------|------------------|--------|
| **1. Daraja Registration** | Paybill shortcode | OAuth2 authentication | Register for Daraja API | Consumer key/secret received | ❌ |
| **2. C2B Setup** | Validation/Confirmation URLs | C2B callback handler | Register C2B URLs | Callbacks working | ❌ |
| **3. C2B Testing** | Test paybill 600000 | C2B payment flow | Receive 100 test payments | 98%+ payment success | ❌ |
| **4. B2B Setup** | Organization shortcode | B2B API | Register for B2B | B2B credentials received | ❌ |
| **5. B2B Testing** | Test B2B credentials | B2B payment flow | Send 50 test payments | 98%+ payment success | ❌ |
| **6. Production Migration** | Production paybill | Production OAuth2 | Migrate to production | Production callbacks working | ❌ |
| **7. Payment Reconciliation** | Daily statement | Reconciliation engine | Match 500 payments | All payments matched | ❌ |
| **8. Failed Payment Retry** | Retry logic | Payment retry handler | Simulate failure | Auto-retry works | ❌ |

**Success Criteria**:
- **98%+ payment success rate** in production
- Zero duplicate payments (idempotency keys)
- Daily reconciliation (all payments matched)
- **Phase 2 Exit**: 500+ payments received via C2B callback

---

### B.6 Performance Testing (10K Users)

**Scope**: Load Testing → Database Optimization → Kafka Optimization → Multi-Region Testing

| Scenario | Target Load | Success Criteria | Current | Status |
|----------|-------------|------------------|---------|--------|
| **Sales Order Entry** | 1000 orders/hour | < 2s API p95 | Not tested | ❌ |
| **Invoice Generation** | 500 invoices/hour | < 2s API p95 | Not tested | ❌ |
| **KRA eTIMS Cu Retrieval** | 5 req/sec sustained | < 2s latency p95 | Not tested | ❌ |
| **M-Pesa C2B Callbacks** | 100 req/sec sustained | < 500ms processing | Not tested | ❌ |
| **GL Posting** | 10,000 postings/hour | < 100ms DB query p95 | Not tested | ❌ |
| **Kafka Events** | 10,000 events/min | < 5s consumer lag | Not tested | ❌ |
| **API Gateway** | 10K concurrent users | < 2s API p95 | Not tested | ❌ |
| **Database** | 10K concurrent queries | < 100ms query p95 | Not tested | ❌ |
| **Multi-Region Latency** | Kenya ↔ US | < 200ms cross-region | Not tested | ❌ |

**Success Criteria**:
- **10,000 concurrent users** with < 2s API response p95
- Database optimized (partitioning + read replicas + caching)
- Kafka optimized (10 partitions per topic, < 5s consumer lag)
- **Phase 3 Exit**: All performance targets met

---

**Reference Implementation Target**: Phase 1 exit criterion (Config + Org + Workflow validated with 1 O2C + 1 P2P flow)
