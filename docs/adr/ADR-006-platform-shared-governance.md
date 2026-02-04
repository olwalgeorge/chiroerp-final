# ADR-006: Platform-Shared Governance Rules

**Status**: Amended (Phase 0 Implementation)
**Date**: 2025-11-06
**Updated**: 2026-02-03
**Tier**: Core
**Context**: Phase 0 - Platform Foundation + Preventing Shared Kernel Anti-Pattern

## Context
The platform uses multiple bounded contexts and a small set of shared technical primitives. To preserve bounded-context autonomy and avoid shared-kernel drift, we define enforceable governance rules that keep platform-shared modules small, technical, and dependency-safe.

**Phase 0 Update (2026-02-03):** Added configuration/organizational/workflow abstraction modules to support the "SAP-grade configurability" strategy (ADR-044, ADR-045, ADR-046). These enable domains to depend on **interfaces** while implementations evolve from hardcoded (Phase 0) → config-driven (Phase 1) → AI-powered (Phase 2+) without changing domain code.

## Decision
Establish strict governance rules for `platform-shared` modules to prevent distributed monolith anti-pattern and maintain bounded context autonomy.

### Allowed in platform-shared

### 1. Technical Primitives ONLY
```kotlin
// ✅ Pure abstractions with no business semantics
sealed class Result<out T>
interface Command
interface Query<out R>
interface DomainEvent
data class DomainError(code: String, message: String)
```

### 2. Framework Integration Contracts
```kotlin
// ✅ CQRS infrastructure
interface CommandHandler<in C : Command, out R>
interface QueryHandler<in Q : Query<R>, out R>

// ✅ Event abstractions
interface EventPublisher
interface EventSubscriber<in E : DomainEvent>
```

### 3. Observability Infrastructure
```kotlin
// ✅ Logging, metrics, tracing
data class CorrelationId(val value: String)
interface StructuredLogger
object MetricsCollector
```

### 4. Security Primitives
```kotlin
// ✅ Authentication/authorization infrastructure
interface AuthenticationPrincipal
sealed class SecurityContext
```

### 5. Platform Abstraction Interfaces (Phase 0 Addition)
```kotlin
// ✅ Configuration engine abstractions (ADR-044)
interface PricingRulesEngine
interface PostingRulesEngine
interface TaxRulesEngine

// ✅ Organizational model abstractions (ADR-045)
interface OrgHierarchyService
data class OrgUnit(val id: UUID, val code: String, val type: OrgUnitType)

// ✅ Workflow engine abstractions (ADR-046)
interface WorkflowEngine
data class ApprovalContext(val documentType: String, val amount: BigDecimal)

// ✅ Event messaging abstractions (ADR-003)
interface DomainEventPublisher
interface DomainEvent
```

**Rationale:** These are **pure technical contracts** that enable the configurability strategy. Domains depend on interfaces, implementations are swapped without code changes. This is different from shared domain models (which are forbidden).

### Forbidden in platform-shared

### 1. Domain Models
```kotlin
// ❌ Business domain concepts belong in bounded contexts
data class CustomerAddress  // → customer-relation context
enum class OrderStatus      // → commerce context
data class InvoiceLineItem  // → financial-management context
```

### 2. Business Logic
```kotlin
// ❌ Business rules belong in domain layer of specific context
class TaxCalculator         // → financial-management context
object DiscountPolicy       // → commerce context
fun validatePassword()      // → tenancy-identity context
```

### 3. Shared DTOs
```kotlin
// ❌ API contracts are context-specific
data class CreateOrderRequest    // → commerce-ecommerce/application
data class CustomerResponse      // → customer-relation/application
```

### 4. Utility Classes
```kotlin
// ❌ Avoid "utils" dumping ground
object StringUtils
object DateUtils
object CollectionUtils
```

### Duplication vs. Sharing Decision Matrix

| Scenario | Decision | Rationale |
|----------|----------|-----------|
| `Result<T>` for error handling | **SHARE** | Pure technical contract, identical semantics |
| `Email` value object | **DUPLICATE** | Identity validation ≠ Marketing campaign validation |
| `Address` data structure | **DUPLICATE** | Tax address ≠ Shipping address ≠ User profile address |
| `Currency` enum | **SHARE (carefully)** | ISO 4217 standard, but context-specific formatting |
| `CommandHandler` interface | **SHARE** | Pure CQRS infrastructure pattern |
| `AuditInfo` metadata | **SHARE** | Technical audit trail, consistent semantics |
| `PhoneNumber` validation | **DUPLICATE** | Customer contact ≠ HR emergency contact ≠ Supplier phone |
| `Money` value object | **DUPLICATE** | Finance precision ≠ Commerce display rounding |

## Alternatives Considered
### Alternative 1: Shared Domain Module
Create `platform-shared/common-domain` with reusable domain models.

**Rejected because:**
- Violates bounded context autonomy
- Creates tight coupling across all contexts
- Single change requires redeploying all services
- Semantic differences hidden behind shared types

### Alternative 2: No Shared Modules
Force each context to implement everything from scratch.

**Rejected because:**
- Duplicates technical infrastructure (Result, Command, Event patterns)
- Inconsistent observability and security implementations
- Wastes effort on non-differentiating technical code

### Alternative 3: Shared Libraries via Maven/Gradle Publishing
Publish shared modules as versioned dependencies.

**Deferred because:**
- Adds complexity during rapid development phase
- Version management overhead
- Consider for Phase 7 (Production deployment)

## Consequences
### Positive
- ✅ Bounded contexts remain autonomous
- ✅ Teams can evolve independently
- ✅ Deploy contexts without coordinating changes
- ✅ Clear ownership and responsibility
- ✅ Prevents distributed monolith

### Negative
- ❌ Some code duplication across contexts
- ❌ Requires discipline during code reviews
- ❌ Need to educate team on bounded context principles

### Neutral
- 🔄 Periodic audits required to prevent drift
- 🔄 ArchUnit tests need maintenance as contexts grow

## Compliance
### Enforcement (Planned)

The following architecture suites are enforced in CI and block PRs:

- Platform-Shared Governance (this ADR): `PlatformSharedGovernanceRules`
- Layering rules: `LayeringRules`
- Hexagonal architecture rules: `HexagonalArchitectureRules`

CI configuration (summary):

- Main workflow runs (blocking):
  - `./gradlew :tests:arch:test --tests "*PlatformSharedGovernanceRules*"`
  - `./gradlew :tests:arch:test --tests "*LayeringRules*"`
  - `./gradlew :tests:arch:test --tests "*HexagonalArchitectureRules*"`
- Scheduled governance workflow mirrors the same steps weekly.

Local developer workflow:

- `./gradlew :tests:arch:test --tests "*PlatformSharedGovernanceRules*"`
- `./gradlew :tests:arch:test --tests "*LayeringRules*"`
- `./gradlew :tests:arch:test --tests "*HexagonalArchitectureRules*"`

See `docs/ARCHITECTURE_TESTING_GUIDE.md` for wiring details, scope expansion, and troubleshooting.

### Governance Mechanisms

### 1. Module Size Limit
- **Maximum (Original):** 4 modules in `platform-shared/`
- **Updated (Phase 0):** 7 modules in `platform-shared/`
- **Current Modules:**
  1. `common-types` - Shared value objects (Money, Email, UUID extensions)
  2. `common-messaging` - Event publishing/consuming interfaces (Kafka abstractions)
  3. `common-observability` - Logging, metrics, tracing contracts
  4. `common-security` - Authentication/authorization primitives
  5. `config-model` - Configuration engine domain model (PricingRule, PostingRule, TaxRule) ⭐ NEW
  6. `org-model` - Organizational hierarchy value objects (OrgUnit, AuthorizationContext) ⭐ NEW
  7. `workflow-model` - Workflow definitions (WorkflowDefinition, ApprovalRoute) ⭐ NEW
- **Adding 8th module requires:** Architecture review + team consensus + ADR update

### 2. File Count Alert
- **Warning threshold:** 25 files per module
- **Critical threshold:** 50 files per module
- **Action:** Trigger refactoring review

### 3. Dependency Rules (ArchUnit)
```kotlin
// Enforce with architecture tests
@Test
fun `platform-shared must not depend on bounded contexts`() {
    noClasses()
        .that().resideInAPackage("com.erp.shared..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "com.erp.identity..",
            "com.erp.finance..",
            "com.erp.commerce.."
            // ... all bounded contexts
        )
}

@Test
fun `bounded contexts must not depend on each other`() {
    noClasses()
        .that().resideInAPackage("com.erp.identity..")
        .should().dependOnClassesThat().resideInAPackage("com.erp.finance..")
}
```

### 4. Code Review Checklist
Every PR touching `platform-shared` must answer:
- [ ] Is this a pure technical primitive or abstraction interface?
- [ ] Does it contain zero business logic/implementation?
- [ ] Would 2+ contexts use the EXACT same behavior?
- [ ] Is coupling cost < duplication cost?
- [ ] Could this belong in a specific context instead?
- [ ] **NEW:** If interface, can implementations be swapped without domain code changes?

**Review Frequency:** Every Sprint (2 weeks)
**Enforcement (Planned):**
- ⬜ **Automated:** ArchUnit tests in CI pipeline (`.github/workflows/ci.yml`) - **PLANNED**
- ⬜ **Weekly Audit:** GitHub Actions workflow (`.github/workflows/arch-governance.yml`) - **PLANNED**
- ⬜ **Local Audit:** PowerShell script (`scripts/audit-platform-shared.ps1`)
- ⬜ **Code Review:** Manual checklist for platform-shared PRs

**Owner:** Lead Architect / Senior Engineer
**Escalation:** Team consensus required to add new shared module

### Enforcement Status

**Status:** ❌ **NOT IMPLEMENTED** (planning phase)

### CI Integration (Planned)

**Main CI Pipeline** (`.github/workflows/ci.yml`):
```yaml
# Planned: build job will block PRs
- name: Enforce platform-shared governance (ADR-006)
  run: ./gradlew :tests:arch:test --tests "*PlatformSharedGovernanceRules*" --no-daemon --stacktrace

# Planned: architecture-tests job for enforcement
- name: Enforce platform-shared governance (ADR-006)
  run: ./gradlew :tests:arch:test --tests "*PlatformSharedGovernanceRules*" --no-daemon --stacktrace
```

**Weekly Governance Audit** (`.github/workflows/arch-governance.yml`):
```yaml
# Planned: runs every Monday at 09:00 UTC (blocking)
- name: Run ArchUnit tests (planned)
  run: ./gradlew :tests:arch:test --tests "*PlatformSharedGovernanceRules*" --no-daemon --stacktrace
```

### Coverage

**Planned coverage (not yet implemented):**
- ⬜ platform-shared (7 modules) ⭐ UPDATED from 4
- ⬜ tenancy-identity (3 modules)
- ⬜ financial-management (10 modules)
- ⬜ commerce (12 modules)
- ⬜ business-intelligence (3 modules)
- ⬜ communication-hub (3 modules)
- ⬜ corporate-services (6 modules)
- ⬜ customer-relation (9 modules)
- ⬜ inventory-management (6 modules)
- ⬜ manufacturing-execution (9 modules)
- ⬜ operations-service (3 modules)
- ⬜ procurement (6 modules)

**Target:** 77 modules under governance (updated from 74)

### Local Validation

**Run tests locally:**
```bash
./gradlew :tests:arch:test --tests "*PlatformSharedGovernanceRules*"
```

**Run with full test suite:**
```bash
./gradlew :tests:arch:test
```

### Rollout Timeline (Planned Targets)

- **2025-11-06:** ADR drafted, ArchUnit infrastructure created (planned)
- **2025-11-07:** Opt-in advisory mode enabled, identity + platform-shared wired (planned)
- **2025-11-08:** Sprint 3 expansion completed (all 12 contexts wired) (planned)
- **2025-11-09:** Enforcement enabled target (planned)

### Compliance Verification

**Last Verified:** Not yet verified
**Verification Method:** ArchUnit automated tests (planned)
**Result:** ❌ Not implemented

**Next Audit:** After initial implementation

## Implementation Plan
- Phase 1: Define governance rules and create ArchUnit test suite skeletons.
- Phase 2: Wire ArchUnit governance tests into CI and a scheduled audit workflow.
- Phase 3: Add a local audit script and code review checklist.
- Phase 4: Train the team on platform-shared boundaries and escalation paths.

## Phase 0 Amendment (2026-02-03)

### New Modules Added

Added 3 platform abstraction modules to support SAP-grade configurability:

1. **`config-model`** - Configuration engine abstractions
   - **Why:** Enables 85%+ of variation via configuration, not code (ADR-044)
   - **Interfaces:** `PricingRulesEngine`, `PostingRulesEngine`, `TaxRulesEngine`
   - **Pattern:** Domains depend on interface → implementations swapped (hardcoded → Drools → AI)
   - **Zero business logic:** Only interfaces and context data classes

2. **`org-model`** - Organizational hierarchy abstractions
   - **Why:** Dynamic org structures, matrix organizations, data visibility (ADR-045)
   - **Interfaces:** `OrgHierarchyService`, value objects like `OrgUnit`
   - **Pattern:** Authorization/visibility checks via interface, not hardcoded
   - **Zero business logic:** Pure structural contracts

3. **`workflow-model`** - Workflow engine abstractions
   - **Why:** Configurable approval routes, escalations, delegation (ADR-046)
   - **Interfaces:** `WorkflowEngine`, context classes like `ApprovalContext`
   - **Pattern:** Approval logic via interface, not domain code
   - **Zero business logic:** Only workflow primitives

### Rationale for Exception

**Question:** Why add 3 modules when ADR-006 limits to 4?

**Answer:** These modules are **critical infrastructure** for the platform strategy:

1. **Not domain models** - Pure technical contracts (interfaces + value objects)
2. **Enable autonomy** - Domains depend on abstractions, not implementations
3. **Strategic goal** - Without these, cannot achieve SAP-grade configurability
4. **Referenced by ADRs** - ADR-044, ADR-045, ADR-046 explicitly require these
5. **Swappable implementations** - Phase 0 hardcoded → Phase 1 config → Phase 2 AI

**This is consistent with ADR-006 principle:** "Technical primitives and framework contracts are allowed."

### Updated Governance

- **Module limit:** 4 → **7** (increased to accommodate platform abstractions)
- **Next limit:** 8th module requires architecture review + ADR amendment
- **Enforcement:** Same ArchUnit rules apply (no business logic, no domain models)
- **Code review:** Enhanced checklist includes "can implementations be swapped?"

## References

### Related ADRs
- ADR-001: Modular CQRS Implementation
- ADR-003: Event-Driven Integration Between Contexts
- ADR-005: Multi-Tenancy Data Isolation Strategy
- **ADR-044: Configuration Rules Framework** ⭐ NEW (config-model rationale)
- **ADR-045: Enterprise Organizational Model** ⭐ NEW (org-model rationale)
- **ADR-046: Workflow & Approval Engine** ⭐ NEW (workflow-model rationale)

### Internal Documentation
- *Domain-Driven Design* by Eric Evans (Ch. 14: Maintaining Model Integrity)
- *Building Microservices* by Sam Newman (Ch. 1: Microservices at Scale)
- *Implementing Domain-Driven Design* by Vaughn Vernon (Ch. 3: Context Mapping)
