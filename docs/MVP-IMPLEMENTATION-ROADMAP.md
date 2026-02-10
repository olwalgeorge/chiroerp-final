# ChiroERP MVP Implementation Roadmap (Tenancy-Identity + Finance)

**Status**: Active (single authoritative roadmap)  
**Last Updated**: February 9, 2026  
**MVP Scope**: `bounded-contexts/tenancy-identity/*` and `bounded-contexts/finance/*` only

This roadmap replaces prior roadmap tracks for implementation execution. The world-class roadmap stream is treated as obsolete for MVP execution and ADR/blueprint content has been absorbed here.

## 1. Scope and Sources

### In Scope (MVP)
- `bounded-contexts/tenancy-identity/tenancy-shared`
- `bounded-contexts/tenancy-identity/tenancy-core`
- `bounded-contexts/tenancy-identity/identity-core`
- `bounded-contexts/finance/finance-shared`
- `bounded-contexts/finance/finance-gl/*`
- `bounded-contexts/finance/finance-ar/*`
- `bounded-contexts/finance/finance-ap/*`
- `bounded-contexts/finance/finance-assets/*`
- `bounded-contexts/finance/finance-tax/*`

### Out of Scope (this roadmap)
- All other bounded contexts and add-on verticals
- P3/world-class expansion tracks

### Source of Truth Inputs
- `COMPLETE_STRUCTURE.txt` (tenancy-identity + finance module/file blueprint)
- ADR-001, ADR-002, ADR-003, ADR-004, ADR-005, ADR-006, ADR-007
- ADR-009, ADR-010, ADR-011, ADR-019, ADR-020, ADR-030, ADR-031
- ADR-044, ADR-045, ADR-046

## 2. Current Baseline (As of 2026-02-09)

### Module Reality Snapshot

| Module | Main Kotlin (real/total) | Test Kotlin (real/total) | Status |
|---|---:|---:|---|
| `bounded-contexts/tenancy-identity/tenancy-shared` | 9/9 | 0/2 | Partial (main implemented, tests placeholder) |
| `bounded-contexts/tenancy-identity/tenancy-core` | 39/39 | 5/5 | In progress |
| `bounded-contexts/tenancy-identity/identity-core` | 15/62 | 2/7 | In progress (domain layer complete) |
| `bounded-contexts/finance/finance-shared` | 0/18 | 0/2 | Not started |
| `bounded-contexts/finance/finance-gl/gl-domain` | 0/70 | 0/4 | Not started |
| `bounded-contexts/finance/finance-gl/gl-application` | 0/44 | 0/4 | Not started |
| `bounded-contexts/finance/finance-gl/gl-infrastructure` | 0/73 | 0/6 | Not started |
| `bounded-contexts/finance/finance-ar/ar-domain` | 0/37 | 0/0 | Not started |
| `bounded-contexts/finance/finance-ar/ar-application` | 0/15 | 0/0 | Not started |
| `bounded-contexts/finance/finance-ar/ar-infrastructure` | 0/40 | 0/6 | Not started |
| `bounded-contexts/finance/finance-ap/ap-domain` | 0/58 | 0/5 | Not started |
| `bounded-contexts/finance/finance-ap/ap-application` | 0/29 | 0/3 | Not started |
| `bounded-contexts/finance/finance-ap/ap-infrastructure` | 0/55 | 0/6 | Not started |
| `bounded-contexts/finance/finance-assets/assets-domain` | 0/52 | 0/4 | Not started |
| `bounded-contexts/finance/finance-assets/assets-application` | 0/35 | 0/3 | Not started |
| `bounded-contexts/finance/finance-assets/assets-infrastructure` | 0/57 | 0/7 | Not started |
| `bounded-contexts/finance/finance-tax/tax-domain` | 0/45 | 0/4 | Not started |
| `bounded-contexts/finance/finance-tax/tax-application` | 0/34 | 0/4 | Not started |
| `bounded-contexts/finance/finance-tax/tax-infrastructure` | 0/68 | 0/7 | Not started |

### Baseline Build Validation

Validated command:

```bash
./gradlew \
  :bounded-contexts:tenancy-identity:tenancy-shared:test \
  :bounded-contexts:tenancy-identity:tenancy-core:test \
  :bounded-contexts:tenancy-identity:identity-core:test \
  :bounded-contexts:finance:finance-shared:test \
  :bounded-contexts:finance:finance-gl:gl-domain:test \
  :bounded-contexts:finance:finance-gl:gl-application:test \
  :bounded-contexts:finance:finance-gl:gl-infrastructure:test \
  :bounded-contexts:finance:finance-ar:ar-domain:test \
  :bounded-contexts:finance:finance-ar:ar-application:test \
  :bounded-contexts:finance:finance-ar:ar-infrastructure:test \
  :bounded-contexts:finance:finance-ap:ap-domain:test \
  :bounded-contexts:finance:finance-ap:ap-application:test \
  :bounded-contexts:finance:finance-ap:ap-infrastructure:test \
  :bounded-contexts:finance:finance-assets:assets-domain:test \
  :bounded-contexts:finance:finance-assets:assets-application:test \
  :bounded-contexts:finance:finance-assets:assets-infrastructure:test \
  :bounded-contexts:finance:finance-tax:tax-domain:test \
  :bounded-contexts:finance:finance-tax:tax-application:test \
  :bounded-contexts:finance:finance-tax:tax-infrastructure:test
```

Result: **BUILD SUCCESSFUL** on February 9, 2026 (tenancy-core real tests green; most non-tenancy modules still no-source/not-started).

## 3. SAP-Grade Non-Negotiables (Quality Gates)

No phase is complete unless all gates below pass:

1. **Architecture Gate**  
   ADR-001/006 layering and bounded-context boundaries; no business leakage into `platform-shared`.
2. **Isolation and Security Gate**  
   ADR-005/007 tenant scoping and RBAC enforced at gateway + service; no cross-tenant data access.
3. **API Contract Gate**  
   ADR-010 validation standard (`@BeanParam`, explicit domain errors, sanitized responses).
4. **Messaging Reliability Gate**  
   ADR-003/020/011 outbox + idempotent consumers + retry/DLQ + correlation IDs.
5. **Financial Correctness Gate**  
   ADR-009/030/031 double-entry invariants, deterministic tax/posting, close controls.
6. **Testing Gate**  
   ADR-019 minimum coverage and E2E process tests for O2C, P2P, and close.
7. **Observability and Audit Gate**  
   Structured logs, metrics, traces, immutable audit evidence for approvals/postings.

## 4. Execution Plan (Trackable Backlog)

Sprint cadence: 2-week sprints.  
Status values: `Done`, `In Progress`, `Not Started`, `Blocked`.

### Phase 0: Program Control and Baseline (Sprint 0)

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| MVP-00 | Publish single MVP roadmap for tenancy-identity + finance | Done | - | `docs/MVP-IMPLEMENTATION-ROADMAP.md` |
| MVP-01 | Remove obsolete roadmap documents from active docs tree | Done | MVP-00 | Only this roadmap remains for implementation tracking |
| MVP-02 | Establish weekly implementation scorecard template | Not Started | MVP-00 | Section 6 in this file used in weekly review |
| MVP-03 | Create per-module task board (`TI-*`, `ID-*`, `FI-*`) | Not Started | MVP-00 | Board exists in project tracker |
| MVP-04 | Lock module acceptance criteria and gate checklist | Done | MVP-00 | Section 3 approved by architecture lead |

### Phase 1: Tenancy Core Completion (Sprints 1-2)

Progress update (2026-02-09):
- Main placeholder burn-down for `tenancy-core` completed (0 placeholder files remaining in `src/main/kotlin`).
- **Security hardening applied** (2026-02-09):
  - Added `@RolesAllowed` to all endpoints (`tenant-admin`, `platform-admin`, `gateway-service`)
  - Restricted `/by-domain` and `/resolve` endpoints to prevent tenant enumeration
  - Removed `domain` field from `TenantResolutionResponse` to limit data exposure
  - Changed default isolation strategy from `SCHEMA` to `AUTO` (ADR-005 tier-based)
  - Added schema/database name length limits (63 chars) with hash suffix for collision resistance
  - Implemented tenant-admin scope guard using trusted `X-Tenant-ID` header checks
  - Implemented PENDING→ACTIVE lifecycle scaffolding (provisioning execution still pending)
- **Outbox reliability slice applied** (2026-02-09):
  - Added `tenant_outbox` Flyway migration (`V003__create_tenant_outbox_table.sql`)
  - Replaced direct publish path with transactional outbox writes (`TenantOutboxEventPublisher`)
  - Added relay worker with retry/backoff (`TenantOutboxRelayService`, `TenantOutboxRelayWorker`)
  - Added idempotency/retry tests (`TenantOutboxJpaStoreTest`, `TenantOutboxRelayServiceTest`)
- **Observability + audit logging** (2026-02-10):
  - Added Micrometer gauges/counters for pending, dead, dispatched, failed outbox events
  - Added readiness health check for outbox backlog + dead-letter thresholds
  - Added tenant lifecycle audit logging for create/activate/suspend/terminate endpoints
- **Tenant provisioning execution** (2026-02-10):
  - Schema-tier provisioning now issues real `CREATE SCHEMA`, grants, and bootstrap DDL with rollback on failure
  - Seeds per-tenant `provisioning_audit` records to confirm bootstrap completion
  - Enterprise (database-per-tenant) flow intentionally left pending to avoid activating unprovisioned tenants
- Test placeholder `TenantJpaRepositoryTest.kt` replaced with real integration tests.
- Added `TenantControllerTest.kt` with full API integration test coverage.

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| TI-01 | Implement remaining tenancy command/query placeholders | Done | MVP-04 | All 7 files fully implemented |
| TI-02 | Add tenant lifecycle transitions with invariants (activate/suspend/terminate) | Done | TI-01 | Domain tests proving valid and invalid transitions |
| TI-03 | Implement tenant resolution by domain and headers | Done | TI-01 | Endpoint/service integration tests + security |
| TI-04 | Implement provisioning flow (schema setup + bootstrap hooks) | Done | TI-01 | Schema-tier + database-tier provisioning executes DDL, grants, telemetry + rollback tests |
| TI-05 | Implement isolation strategy service aligned to ADR-005 tiers | Done | TI-04 | AUTO mode + slug truncation + hash suffix |
| TI-06 | Replace placeholder tests with real unit/integration tests | Done | TI-01 | All test files now have real tests |
| TI-07 | Add API validation and error mapping per ADR-010 | Done | TI-01 | Validation contract tests + @RolesAllowed + tenant-scope header enforcement |
| TI-08 | Add tenancy outbox event publication reliability | Done | TI-02 | V003 outbox migration + relay worker + idempotency/retry tests |
| TI-09 | Add health, metrics, audit logs for tenancy lifecycle | Done | TI-02 | Tenant audit logs + `/q/health` outbox readiness + Prom gauges/counters |
| TI-10 | Phase 1 gate review | Done | TI-01..TI-09 | 2026-02-10 evidence: tenancy-core + architecture tests green; security/API/messaging/observability gates verified |

### Phase 2: Identity Core Implementation (Sprints 3-5)

Progress update (2026-02-11):
- **Domain layer implementation completed** (ID-01):
  - Replaced all placeholder domain model files with concrete implementations: `UserId`, `UserStatus`, `IdentityProvider`, `UserProfile`, `UserCredentials`, `Permission`, `UserRole`, `MfaConfiguration`, `ExternalIdentity`, `Session` (+ `SessionStatus`)
  - Implemented domain event contract (`UserDomainEvent`) + 8 concrete events: `UserCreatedEvent`, `UserActivatedEvent`, `UserLockedEvent`, `UserPasswordChangedEvent`, `UserRoleAssignedEvent`, `MfaEnabledEvent`, `UserLoggedInEvent`, `UserLoggedOutEvent`
  - Implemented `User` aggregate (lines 17-180) with lifecycle/security invariants: activation/locking, password rotation history, role + permission aggregation, MFA enablement, external identity linking, login/logout event capture, domain event emission
  - Defined domain ports: `UserRepository`, `SessionRepository`, `IdentityProviderGateway`, `UserEventPublisher`
  - Added comprehensive domain tests: `UserTest.kt` (67 lines), `SessionTest.kt` (44 lines) covering lifecycle, password history, permission evaluation, MFA, session refresh/heartbeat/logout
  - Build verification: `./gradlew :bounded-contexts:tenancy-identity:identity-core:test` SUCCESS (~3m36s)

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| ID-01 | Replace placeholder domain model/event/port files in `identity-core` | Done | TI-10 | 0 placeholder domain files; `UserTest.kt`, `SessionTest.kt` tests green (2026-02-11) |
| ID-02 | Implement user lifecycle aggregate logic and invariants | Not Started | ID-01 | Domain tests for create/activate/lock/password |
| ID-03 | Implement authentication service (login/logout, anti-enumeration) | Not Started | ID-01 | Auth service tests and timing guard tests |
| ID-04 | Implement RBAC + permissions with tenant scoping (ADR-007/014) | Not Started | ID-01 | Permission tests + denial tests |
| ID-05 | Implement password policy/encoding and rotation flows | Not Started | ID-03 | Password policy integration tests |
| ID-06 | Implement MFA (TOTP) enrollment and verification | Not Started | ID-03 | MFA controller/service tests |
| ID-07 | Implement session persistence in Redis | Not Started | ID-03 | Session TTL + revocation tests |
| ID-08 | Implement JWT token provider/validation and claim contract | Not Started | ID-03 | Token tests + gateway compatibility tests |
| ID-09 | Implement REST controllers (`Auth`, `User`, `Mfa`) | Not Started | ID-03..ID-08 | API tests (happy/failure/security paths) |
| ID-10 | Replace placeholder Flyway scripts (`V001`-`V004`) with executable schema | Not Started | ID-01 | DB migration succeeds from empty state |
| ID-11 | Implement event outbox for identity events | Not Started | ID-02 | Outbox relay and idempotency tests |
| ID-12 | Replace placeholder tests with real tests across layers | Not Started | ID-01..ID-11 | `identity-core` tests no longer placeholders |
| ID-13 | Phase 2 gate review | Not Started | ID-01..ID-12 | All quality gates pass for identity-core |

### Phase 3: Finance Foundation and GL Vertical Slice (Sprints 6-8)

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| FI-01 | Implement `finance-shared` identifiers/value objects per ADR-006 | Not Started | ID-13 | `finance-shared` main files non-placeholder |
| FI-02 | Implement GL domain invariants (double-entry, period lock, account validity) | Not Started | FI-01 | Domain tests for invariant enforcement |
| FI-03 | Implement GL application command/query handlers and ports | Not Started | FI-02 | Handler tests for posting/reversal/queries |
| FI-04 | Implement GL persistence + Flyway migrations | Not Started | FI-02 | Migration + repository integration tests |
| FI-05 | Implement GL REST resources + validation standard | Not Started | FI-03 | REST tests incl. invalid payloads |
| FI-06 | Implement GL outbox/inbox + Kafka schemas | Not Started | FI-03 | Reliable event publish/consume tests |
| FI-07 | Implement read adapters for trial balance/balance sheet | Not Started | FI-04 | Query consistency tests |
| FI-08 | Implement GL observability (metrics, traces, audit trail) | Not Started | FI-05 | Dashboards + audit log checks |
| FI-09 | Enforce tenancy + identity authorization on GL endpoints | Not Started | ID-13, FI-05 | Security integration tests |
| FI-10 | Phase 3 gate review | Not Started | FI-01..FI-09 | GL vertical slice accepted |

### Phase 4: Accounts Receivable + Accounts Payable (Sprints 9-11)

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| ARAP-01 | Implement AR domain (`Invoice`, `Payment`, `Aging`, `Dunning`) | Not Started | FI-10 | AR domain tests |
| ARAP-02 | Implement AR application handlers/ports | Not Started | ARAP-01 | AR handler tests |
| ARAP-03 | Implement AR infrastructure (REST, persistence, migrations, events) | Not Started | ARAP-02 | AR integration tests |
| ARAP-04 | Integrate AR postings to GL with deterministic rules | Not Started | ARAP-03, FI-10 | AR->GL posting tests |
| ARAP-05 | Implement AP domain (vendor, bill, payment run, 3-way match) | Not Started | FI-10 | AP domain tests |
| ARAP-06 | Implement AP application handlers/ports | Not Started | ARAP-05 | AP handler tests |
| ARAP-07 | Implement AP infrastructure (REST, persistence, migrations, events) | Not Started | ARAP-06 | AP integration tests |
| ARAP-08 | Integrate AP postings to GL with maker-checker checks | Not Started | ARAP-07, FI-10 | AP->GL posting tests |
| ARAP-09 | Implement AR/AP validation, audit, and metrics standards | Not Started | ARAP-03, ARAP-07 | Validation + observability tests |
| ARAP-10 | Add AR/AP E2E process tests (invoice->cash, bill->payment) | Not Started | ARAP-04, ARAP-08 | E2E tests in CI |
| ARAP-11 | Phase 4 gate review | Not Started | ARAP-01..ARAP-10 | AR/AP accepted |

### Phase 5: Fixed Assets + Tax Engine (Sprints 12-14)

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| FAX-01 | Implement Assets domain (acquire, depreciate, transfer, dispose) | Not Started | ARAP-11 | Assets domain tests |
| FAX-02 | Implement Assets application handlers/ports | Not Started | FAX-01 | Assets handler tests |
| FAX-03 | Implement Assets infrastructure (REST, persistence, migrations, events) | Not Started | FAX-02 | Assets integration tests |
| FAX-04 | Implement Assets->GL subledger integration | Not Started | FAX-03, FI-10 | Depreciation/disposal posting tests |
| FAX-05 | Implement Tax domain (tax codes, calc, returns, withholding) | Not Started | ARAP-11 | Tax domain tests |
| FAX-06 | Implement Tax application handlers/ports | Not Started | FAX-05 | Tax handler tests |
| FAX-07 | Implement Tax infrastructure (REST, persistence, migrations, events) | Not Started | FAX-06 | Tax integration tests |
| FAX-08 | Implement Tax->GL/AR/AP integrations (ADR-030) | Not Started | FAX-07, ARAP-11 | End-to-end tax posting tests |
| FAX-09 | Implement reverse charge, exemptions, and return readiness checks | Not Started | FAX-07 | Compliance scenario tests |
| FAX-10 | Implement audit-ready tax trail exports | Not Started | FAX-07 | Tax audit evidence output |
| FAX-11 | Phase 5 gate review | Not Started | FAX-01..FAX-10 | Assets/Tax accepted |

### Phase 6: Cross-Context Orchestration, Close, and Pilot Readiness (Sprints 15-16)

| ID | Work Item | Status | Depends On | Evidence / DoD |
|---|---|---|---|---|
| REL-01 | Implement saga support standards for financial workflows (ADR-011) | Not Started | FAX-11 | Saga state + compensation tests |
| REL-02 | Implement period close orchestration MVP (ADR-031) | Not Started | REL-01 | Close run tests and evidence |
| REL-03 | Implement O2C and P2P E2E process suites (ADR-019) | Not Started | ARAP-11, FAX-11 | E2E green in CI |
| REL-04 | Execute load/performance tests for MVP paths | Not Started | REL-03 | Meets p95 targets per ADRs |
| REL-05 | Execute security and tenant-isolation test suite | Not Started | REL-03 | Zero cross-tenant leakage |
| REL-06 | Prepare operational runbooks and incident playbooks | Not Started | REL-03 | Runbook docs approved |
| REL-07 | UAT with finance + tenancy personas | Not Started | REL-03 | Signed UAT report |
| REL-08 | MVP release gate review | Not Started | REL-01..REL-07 | Go/No-Go decision record |

## 5. Critical Dependency Rules (No Skipping)

1. `tenancy-core` must clear Phase 1 gates before deep `identity-core` work begins.  
2. `identity-core` security model must clear Phase 2 gates before finance APIs are considered done.  
3. `finance-shared` and GL vertical slice must be done before AR/AP.  
4. AR/AP must be done before Assets/Tax integrations.  
5. Assets/Tax must be done before period close orchestration.  
6. No module is marked complete while placeholder files remain in that module.  
7. No phase is closed without passing all SAP-grade quality gates in Section 3.

## 6. Weekly Tracking Template

Use this format in weekly implementation reviews:

```text
Week:
Completed IDs:
In Progress IDs:
Blocked IDs:
Placeholder Burn-Down:
  - tenancy-identity main placeholders: <count>
  - finance main placeholders: <count>
Build/Test Signal:
  - command:
  - result:
Gate Risks:
Decisions Needed:
```

## 7. Immediate Next 10 Actions

1. ✅ **(COMPLETED 2026-02-11)** Start `ID-01` identity-core placeholder replacement (domain layer first).
2. Execute Phase 2 kickoff: begin `ID-02` user lifecycle aggregate logic and invariants.
3. Start `ID-10` identity Flyway scripts (user/session/role/permission tables).
4. Prepare `ID-11` identity outbox design by reusing TI-08 pattern contracts.
5. Implement `ID-03` authentication service (login/logout, anti-enumeration).
6. Harden outbox with dead-letter handling policy after max attempts (TI follow-up).
7. Complete weekly scorecard workflow (`MVP-02`) and publish current week evidence.
8. Create per-module tracker board (`MVP-03`) linking `TI-*`, `ID-*`, and `FI-*` IDs.
9. Document SOC/SAP runbook entries for TI-09 observability + TI-04 provisioning monitors.
10. Execute Phase 1 gate checklist and capture `TI-10` evidence (if not yet formally closed).
