# ADR-011: Saga Pattern & Compensating Transactions

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team  
**Tier**: Core  
**Tags**: saga, transactions, reliability, orchestration, choreography, compensating-actions  

## Context
Cross-context business processes (Order-to-Cash, Procure-to-Pay, Make-to-Order, Period Close) span multiple bounded contexts. Two-phase commit is not feasible across services and brokers. We need a consistent pattern for long-running, distributed workflows with clear failure handling, compensation, and observability that meets SAP-grade ERP reliability expectations.

## Decision
Adopt a **Saga Pattern** with explicit **compensating transactions** and choose orchestration vs. choreography per use case. Implementation is **not started**; this ADR defines the standard.

### Pattern Selection
- **Default**: **Orchestrated saga** for complex, multi-step business workflows needing global visibility, deadlines, and business semantics (aligns with SAP workflow/BPM patterns).
- **Choreography**: Allowed for simple 2-3 step integrations with low blast radius and clear idempotent handlers.

### Core Principles
- **Idempotency first**: All commands/events must be idempotent; include idempotency keys and deduplication storage per context.
- **Exactly-once effect via outbox/inbox**: Use transactional outbox + inbox (or idempotent consumer store) for all saga steps.
- **Time-bounded steps**: Every step defines SLA and timeout; orchestrator enforces retry/backoff and escalation.
- **Compensation required**: Each step that mutates state must declare a compensating action (where business-legal).
- **Deterministic state machine**: Saga transitions are explicit, persisted, and replayable.
- **Isolation by tenant**: All saga state and messages carry tenant context; compensations honor tenant isolation tier (row/schema/DB).
- **Observability**: Correlation IDs, saga IDs, metrics, structured logs, traces on every hop.
- **Security**: Propagate authz context (user, roles, scopes); compensations respect SoD (no same actor for commit and rollback where policy requires).

### Saga Orchestrator Standard (Recommended)
- **Location**: Per bounded context needing cross-context workflows; orchestrator lives in the initiating context’s application layer.
- **State store**: Durable saga table (PostgreSQL) with states: `PENDING`, `IN_PROGRESS`, `WAITING`, `COMPLETED`, `COMPENSATING`, `COMPENSATED`, `FAILED`, `ABORTED`, `TIMED_OUT`.
- **Timers**: Use scheduler (e.g., Quarkus @Scheduled) or broker-based delays for timeouts and retries; backoff with jitter.
- **Commands vs. events**: Orchestrator issues commands (sync or async) and listens to events to advance the state machine.
- **Ordering**: Partition saga messages by `sagaId` to preserve step order.

### Choreography Standard (Allowed for Simple Cases)
- Keep to ≤3 steps; otherwise upgrade to orchestration.
- Publish well-versioned domain events; consumers execute local transactions + compensations.
- Dead-letter and retry policies per event type; idempotent handlers mandatory.

### Compensation Rules
- Every mutating step must declare: **compensate()** semantics, idempotency, and side effects.
- If business rules forbid rollback (e.g., financial postings after close), define **forward recovery** (new reversing journal) instead of delete.
- Compensations must be **authorization-safe** (respect SoD) and **tenant-safe**.

### Reliability & Delivery
- **Outbox** for producers, **Inbox/Idempotency store** for consumers.
- **At-least-once transport + idempotent handlers** as baseline.
- **Poison message** handling → DLQ with alerting; manual replay supported via admin tooling.

### Observability & Operations
- Metrics: `saga_active`, `saga_duration_ms` (p50/p95/p99), `saga_failures_total`, `saga_compensations_total`, `saga_timeouts_total` tagged by saga type, tenant, context.
- Tracing: Span per step with `saga.id`, `tenant.id`, `correlation.id`.
- Logging: Structured logs per transition; audit log for compensations and escalations.
- Dashboards: Standard Grafana boards for durations, failure rates, DLQ depth.

### Security & Compliance
- Enforce SoD where required (e.g., maker/checker on financial reversals).
- Preserve auth context (user, roles, scopes) through saga; compensations cannot escalate privileges.
- GDPR/PII: Avoid embedding PII in saga logs/messages; reference IDs instead.

### Data & Consistency
- Prefer **append-only** compensations for financial data (reversal entries) over destructive rollback.
- For inventory and orders, compensations must reconcile stock and reservations deterministically.
- Prevent partial visibility: read models should be updated only after local commit per step; consumer idempotency protects replays.

### Failure Handling & Escalation
- Retry with capped backoff; move to DLQ after N attempts; emit alert.
- Timeouts transition to `TIMED_OUT` then to `COMPENSATING` if safe, else `ABORTED` with manual intervention required.
- Manual replay tooling required for DLQ and aborted sagas.

### Versioning & Evolution
- Version saga definitions; include `sagaVersion` in state and messages.
- Rolling upgrades: orchestrators must handle in-flight older versions until drain completes.

## Alternatives Considered

### 1. Two-Phase Commit (2PC) - Rejected
**Approach**: Use distributed transactions with XA protocol for atomicity across services.

**Pros**:
- ACID guarantees across distributed services
- No compensation logic needed
- Familiar to database developers

**Cons**:
- Blocking protocol (locks held during commit)
- Coordinator single point of failure
- Poor performance at scale (>3 participants)
- Not suitable for long-running workflows
- Kafka/Redpanda don't support XA

**Why Rejected**: Incompatible with event-driven architecture; poor performance and availability characteristics.

### 2. Try-Confirm/Cancel (TCC) - Considered
**Approach**: Three-phase protocol with tentative reservation (Try), confirmation (Confirm), or cancellation (Cancel).

**Pros**:
- Non-blocking (no locks)
- Good fit for inventory/payment reservations
- Clear semantic phases

**Cons**:
- Requires all participants to implement Try/Confirm/Cancel
- More complex than choreography
- Timeout handling tricky (Try expires?)
- Not suitable for non-reversible operations

**Decision**: **Partially Adopted** - Use TCC pattern for specific use cases (inventory reservation, payment holds) but not as primary saga pattern. Saga orchestration more flexible.

### 3. Event Choreography Only - Rejected
**Approach**: Use only choreographed sagas without orchestrators.

**Pros**:
- Simple to implement
- No central coordinator
- Services remain loosely coupled

**Cons**:
- No global workflow visibility
- Difficult to reason about compensations
- Hard to debug failures
- Cyclic dependencies between contexts
- No guaranteed completion

**Why Rejected**: Insufficient for complex SAP-grade workflows (Order-to-Cash, Period Close). Reserve for simple flows.

### 4. Process Manager (BPMN) - Deferred
**Approach**: Use BPMN engine (Camunda, Temporal) for workflow orchestration.

**Pros**:
- Visual workflow designer
- Battle-tested engines
- Rich ecosystem (monitoring, debugging)
- Human tasks support

**Cons**:
- Additional infrastructure dependency
- Opinionated workflow model
- Learning curve for BPMN
- Overhead for simple sagas

**Decision**: **Deferred** - Evaluate in Phase 5+ if workflow complexity justifies dedicated engine. Current Kotlin-based orchestration sufficient for 90% of use cases.

### 5. Outbox Pattern Without Saga - Insufficient
**Approach**: Use only transactional outbox (ADR-003) without saga coordination.

**Pros**:
- Simpler than saga (no orchestrator)
- Guarantees at-least-once delivery
- Good for fire-and-forget events

**Cons**:
- No compensation logic
- No guaranteed workflow completion
- No failure recovery beyond retries
- Can't handle partial failures

**Why Rejected**: Outbox is necessary but insufficient; sagas provide recovery and compensation on top of reliable event delivery.

## Consequences
### Positive
- Clear, repeatable pattern for long-running, cross-context workflows
- Deterministic recovery with compensations
- Observability and auditability suitable for SOX/IFRS-grade flows
- Alignment with SAP-grade reliability (similar to LUW + update tasks + workflow)

### Negative / Risks
- Added complexity (state store, orchestrator, compensation code)
- More operational surface (DLQ, replay tooling, dashboards)
- Requires strict idempotency discipline and contract governance

### Neutral
- Choreography remains available for simple flows; orchestration preferred for complex/critical flows

## Compliance

### SOX (Sarbanes-Oxley) Compliance
**Requirement**: Financial workflows must be auditable with compensation trails.

**How We Comply**:
- **Audit Trail**: All saga steps (forward and compensation) logged with immutable state transitions
- **Idempotency Keys**: Prevent duplicate financial transactions during retries
- **Compensation Records**: All compensating transactions logged separately (e.g., reversal journal entries)
- **Workflow Traceability**: Saga correlation IDs link all related transactions for audit review

**Evidence**:
- Saga state store with 7-year retention
- Compensation audit logs
- Quarterly saga completion/compensation reports

### GDPR Compliance
**Requirement**: Personal data in failed sagas must be erasable.

**How We Comply**:
- **Data Minimization**: Saga state stores only entity IDs, not full PII
- **Right to Erasure**: Saga state can be purged after workflow completion (tombstone events)
- **Purpose Limitation**: Saga state used only for workflow coordination, not analytics

### Regulatory Reporting (IFRS, GAAP)
**Requirement**: Financial period close must be reliable and recoverable.

**How We Comply**:
- **Period Close Saga**: Multi-step orchestration with compensation for partial failures
- **Idempotent Steps**: Period close can be retried safely after infrastructure failures
- **Rollback Safety**: Compensations reverse all GL entries if period close fails mid-way
- **Audit Evidence**: Complete saga log proves period close integrity for auditors

### Service Level Objectives
- **Saga Completion**: 99.9% of sagas complete successfully (including compensations)
- **Saga Latency**: < 5 seconds for simple sagas (P95), < 60 seconds for complex sagas
- **Compensation Latency**: < 10 seconds to initiate compensation after failure detection
- **State Store Reliability**: 99.99% availability (PostgreSQL with replication)
- **Idempotency Accuracy**: 100% duplicate prevention (zero double-transactions)

## Implementation Plan
### Implementation Plan (Not Started)

- Phase 1: Define saga SDK: state model, repository, outbox/inbox helpers, idempotency keys, metrics/tracing interceptors.
- Phase 2: Provide starter templates for orchestration and choreography; sample saga in a non-critical context.
- Phase 3: Add admin/replay tooling, DLQ viewer, and Grafana dashboard.
- Phase 4: Roll out to first critical flows (Order-to-Cash, Procure-to-Pay, Period Close) with SoD-aware compensations.

## References

### Related ADRs
- ADR-003 (Event-Driven Integration), ADR-020 (Event-Driven Architecture Policy), ADR-010 (Validation), ADR-009 (Financial), ADR-008 (CI/CD resilience)

### External References
- SAP analogs: LUW/update task, SAP Workflow/BPM, Application Log, tRFC/qRFC patterns
