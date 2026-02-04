# ADR-020: Event-Driven Architecture Hybrid Policy

**Status**: Draft (Not Implemented)
**Date**: 2025-11-09
**Deciders**: Architecture Team
**Tier**: Core
**Tags**: events, integration, hybrid, messaging, governance

## Context
The ERP platform consists of 12 bounded contexts that need to integrate and share data. We must define clear patterns for inter-context communication that balance:

- **Autonomy**: Each context can evolve independently
- **Consistency**: Data changes propagate reliably across contexts
- **Performance**: Integration doesn't introduce unacceptable latency
- **Complexity**: Patterns are understandable and maintainable
- **Flexibility**: Different contexts can adopt patterns suited to their needs

### Research Foundation

This policy is informed by:

1. **Domain Analysis**: Reviewed CONTEXT_MAP.md identifying 12 contexts with specific upstream/downstream relationships
2. **Implementation Phase**: Architecture baseline and integration policy definition (as of 2026-02-01)
3. **Codebase Audit**: Initial implementation may start from scaffolds while patterns are established and validated
4. **ERP Integration Patterns**: Research from industry best practices:
   - SAP's event-driven architecture (Business Events in S/4HANA)
   - Oracle's SOA Suite patterns for ERP integration
   - Microsoft Dynamics 365's dataverse events
   - NetSuite's SuiteFlow event-based workflows

5. **Key ERP Integration Characteristics**:
   - **Order-to-Cash**: High event volume, requires async (Commerce → Inventory → Financial)
   - **Procure-to-Pay**: Async beneficial for AP/AR workflows (Procurement → Inventory → Financial)
   - **Make-to-Order**: Manufacturing needs real-time queries + async updates
   - **Master Data**: Customer/Product need hybrid (sync queries + async updates)
   - **Analytics**: Pure consumer pattern for BI aggregation
   - **Transactional Consistency**: Outbox pattern mandatory for financial accuracy

6. **Anti-Patterns to Avoid**:
   - Synchronous REST chains across > 2 contexts (cascading failures)
   - Polling for state changes (prefer events)
   - Publishing events without transactional outbox (data loss risk)
   - Forcing pure EDA where hybrid is more appropriate
   - No event versioning strategy (breaking changes inevitable)

## Decision
### Implementation Notes
This ADR defines the integration policy for bounded contexts and the recommended default infrastructure patterns (hybrid events + REST where appropriate, transactional outbox, schema versioning). It is intended to guide incremental adoption as contexts are implemented.

We adopt a **flexible hybrid integration policy** that allows bounded contexts to choose from three integration patterns based on their specific needs:

### Pattern 1: Pure Event-Driven Architecture (Pure EDA)

**When to Use**:
- Context publishes state changes that many other contexts need to react to
- Eventual consistency is acceptable (no real-time guarantees needed)
- High decoupling is priority (publisher doesn't know consumers)
- Context serves as authoritative source for domain data

**Characteristics**:
- All outbound integration via domain events
- No synchronous APIs exposed for data access
- Other contexts maintain local read models built from events
- Event versioning and backward compatibility mandatory

**Example Use Cases**:
- Inventory stock level changes → notify Commerce, Manufacturing, Procurement
- Financial period close → notify all contexts for reporting snapshots

### Pattern 2: Hybrid (Events + REST APIs) ⭐ RECOMMENDED DEFAULT

**When to Use**:
- Context needs both real-time queries and eventual consistency updates
- Some operations require immediate response (authentication, validation)
- Context publishes events for state changes but also exposes query APIs
- Balance between decoupling and performance needed
- **MOST ERP CONTEXTS FIT THIS PATTERN**

**Characteristics**:
- Commands/writes trigger domain events (async)
- Queries exposed via REST APIs (sync)
- CQRS separation: write operations publish events, reads via APIs
- Events for eventual consistency, APIs for immediate reads

**Why Hybrid is Best for ERP**:
1. **Real-Time Queries**: Users need instant product availability, account balances, order status
2. **Async Workflows**: Order-to-cash, procure-to-pay benefit from event choreography
3. **Loose Coupling**: Events prevent cascading failures across context chains
4. **Performance**: Query APIs avoid event replay overhead for simple reads
5. **Pragmatic**: Easier to implement and reason about than pure EDA

**Example Use Cases**:
- **Identity**: Auth queries via REST, user lifecycle events via Kafka
- **Commerce**: Order placement events async, product catalog queries sync
- **Inventory**: Stock availability REST API, stock change events async
- **Financial**: Account balance queries sync, GL posting events async
- **Customer**: Profile lookup REST API, profile update events async

**Trade-offs**:
- ✅ Best of both worlds (sync speed + async decoupling)
- ✅ Easier to test than pure EDA
- ✅ Gradual migration path from REST-only
- ⚠️ More infrastructure than REST-only (Kafka required)
- ⚠️ Need to decide which operations are sync vs async

### Pattern 3: Synchronous (REST-Only)

**When to Use**:
- Context primarily serves queries/reads
- Real-time consistency required (no eventual consistency acceptable)
- Low integration volume (not many consumers)
- Context is internal utility (not core domain)

**Characteristics**:
- All integration via REST APIs
- Request-response pattern only
- No event publishing infrastructure needed
- Tighter coupling acceptable for specific use cases

**Example Use Cases**:
- API Gateway: Route requests, rate limiting (operational, not domain)
- Communication Hub: Send email/SMS on demand
- Corporate Assets: Query asset details for forms

### Pattern Selection Decision Matrix

| Criteria | Pure EDA | Hybrid | REST-Only |
|----------|----------|--------|-----------|
| **Real-time reads required** | ❌ No | ✅ Yes | ✅ Yes |
| **Eventual consistency acceptable** | ✅ Yes | ✅ Yes | ❌ No |
| **High decoupling priority** | ✅ High | ⚠️ Medium | ❌ Low |
| **Many consumers** | ✅ Yes | ✅ Yes | ❌ Few |
| **State change notifications** | ✅ Always | ✅ Yes | ❌ No |
| **Query performance** | ⚠️ Local cache | ✅ Direct | ✅ Direct |
| **Implementation complexity** | ⚠️ High | ⚠️ Medium | ✅ Low |
| **Infrastructure cost** | ⚠️ High (Kafka) | ⚠️ Medium | ✅ Low |

### Recommended Pattern by Context

### Current Implementation Status

**NOTE**: This ADR provides integration policy guidance during the initial architecture phase. Some bounded contexts may begin as skeletons while patterns are established. The recommendations below are based on:
1. Analysis of the CONTEXT_MAP.md integration requirements
2. Domain-Driven Design principles for each domain
3. Planned tenancy-identity hybrid pattern
4. Industry best practices for ERP integration patterns

### Pattern Recommendations with Rationale

| Bounded Context | Pattern | Implementation Status | Detailed Rationale |
|----------------|---------|----------------------|-------------------|
| **Tenancy-Identity** | **Hybrid** | 📋 Placeholder | **AUTH**: Real-time validation queries (REST)<br>**USER LIFECYCLE**: Async propagation to all contexts (Events)<br>**TENANT PROVISIONING**: Notify contexts of new tenants (Events)<br>***Planned reference implementation*** |
| **Commerce** (4 subdomains) | **Hybrid** ⭐ | 📋 Placeholder | **ORDER PLACEMENT**: Async to Inventory/Finance (Events)<br>**PRODUCT CATALOG**: Real-time queries for storefront (REST)<br>**PRICING**: Real-time calculations (REST)<br>**High Event Volume**: Orders trigger multiple downstream actions |
| **Inventory** (2 subdomains) | **Hybrid** ⭐ | 📋 Placeholder | **STOCK CHANGES**: Critical async notifications (Events)<br>**AVAILABILITY QUERIES**: Real-time for Commerce (REST)<br>**WAREHOUSE MOVEMENTS**: Async to Manufacturing (Events)<br>**High Coupling**: Central to Commerce/Manufacturing/Procurement |
| **Financial** (3 subdomains) | **Hybrid** | 📋 Placeholder | **GL POSTINGS**: Async from Commerce/Procurement (Events)<br>**ACCOUNT QUERIES**: Real-time balance checks (REST)<br>**PAYMENT STATUS**: Async notifications (Events)<br>**Audit Requirements**: Event sourcing beneficial |
| **Manufacturing** (3 subdomains) | **Hybrid** | 📋 Placeholder | **PRODUCTION COMPLETION**: Async to Inventory (Events)<br>**WO STATUS QUERIES**: Real-time for planning (REST)<br>**QUALITY EVENTS**: Async to multiple contexts (Events)<br>**Batch Processing**: Events fit production workflows |
| **Procurement** (2 subdomains) | **Hybrid** | 📋 Placeholder | **PO RECEIVING**: Async to Inventory/Finance (Events)<br>**PO QUERIES**: Real-time for approval flows (REST)<br>**SUPPLIER EVENTS**: Async notifications (Events)<br>**Cross-Context**: High integration with Inventory/Finance |
| **Customer Relation** (3 subdomains) | **Hybrid** | 📋 Placeholder | **PROFILE UPDATES**: Async to Commerce/BI (Events)<br>**CUSTOMER LOOKUP**: Real-time for sales (REST)<br>**SUPPORT TICKETS**: Async to Operations (Events)<br>**Master Data**: Customer is shared entity |
| **Operations Service** | **Hybrid** | 📋 Placeholder | **WORK COMPLETION**: Async to Manufacturing (Events)<br>**DISPATCH QUERIES**: Real-time scheduling (REST)<br>**SLA EVENTS**: Async monitoring (Events)<br>**Field Coordination**: Events for mobile workforce |
| **Business Intelligence** | **Pure EDA** (Consumer Only) | 📋 Placeholder | **READ-ONLY**: No state to publish<br>**EVENT SINK**: Consumes from all contexts<br>**ANALYTICS**: Build read models from events<br>**No Real-Time**: Eventual consistency acceptable |
| **Communication Hub** | **REST-Only** ⭐ | 📋 Placeholder | **ON-DEMAND**: Triggered by other contexts<br>**NO STATE**: Transient operations only<br>**EXTERNAL**: Integrates with SendGrid/Twilio<br>**LOW VOLUME**: Not a domain bottleneck |
| **Corporate Services** (2 subdomains) | **REST-Only** | 📋 Placeholder | **INTERNAL UTILITY**: HR/Asset queries<br>**LOW COUPLING**: Minimal cross-context needs<br>**INFREQUENT**: Not in critical path<br>**SIMPLE**: REST adequate for use cases |

**Legend**:
- ⭐ = **Priority for next implementation** (high integration needs)
- 📋 = Placeholder code only

### ERP Workflow Examples (Event Choreography)

### Workflow 1: Order-to-Cash (Commerce → Inventory → Financial)

**Pattern**: Event Choreography (Hybrid contexts)

```
1. Customer places order in Commerce
   ├─ Commerce: REST API → Create Order (sync response to user)
   └─ Commerce: Publish OrderPlacedEvent

2. Inventory receives OrderPlacedEvent
   ├─ Check stock availability
   ├─ Reserve items
   └─ Publish ItemsReservedEvent

3. Financial receives OrderPlacedEvent
   ├─ Create AR invoice
   └─ Publish InvoiceGeneratedEvent

4. Commerce receives ItemsReservedEvent + InvoiceGeneratedEvent
   ├─ Update order status to "Ready for Fulfillment"
   └─ Publish OrderConfirmedEvent

5. Inventory fulfills order
   ├─ Pick items, pack, ship
   └─ Publish OrderShippedEvent

6. Financial receives OrderShippedEvent
   ├─ Recognize revenue (GL posting)
   └─ Publish RevenueRecognizedEvent
```

**Why Events**: Loose coupling, each step can fail/retry independently, audit trail

**Why REST**: Customer needs immediate order confirmation (can't wait for async processing)

### Workflow 2: Procure-to-Pay (Procurement → Inventory → Financial)

```
1. Procurement: PO issued
   └─ Publish POIssuedEvent

2. Inventory receives POIssuedEvent
   ├─ Create expected receipt
   └─ Wait for physical arrival

3. Procurement: Goods received
   └─ Publish ReceivingCompletedEvent

4. Inventory receives ReceivingCompletedEvent
   ├─ Update stock levels
   └─ Publish StockAdjustedEvent

5. Financial receives ReceivingCompletedEvent
   ├─ Match to PO (3-way match: PO + Receipt + Invoice)
   ├─ Create AP liability
   └─ Publish InvoiceMatchedEvent

6. Financial: Payment due
   └─ Publish PaymentProcessedEvent
```

**Why Events**: Long-running process (days/weeks), allows for approval workflows

### Workflow 3: Make-to-Order (Commerce → Manufacturing → Inventory)

```
1. Commerce: Custom order placed
   └─ Publish CustomOrderPlacedEvent

2. Manufacturing receives CustomOrderPlacedEvent
   ├─ Create work order
   ├─ Query Inventory REST API for BOM availability (sync)
   └─ Publish WorkOrderCreatedEvent

3. Manufacturing: Production starts
   └─ Publish ProductionStartedEvent

4. Manufacturing: Production complete
   ├─ Quality check (may take time)
   └─ Publish ProductionCompletedEvent

5. Inventory receives ProductionCompletedEvent
   ├─ Add finished goods to stock
   └─ Publish StockAdjustedEvent

6. Commerce receives StockAdjustedEvent
   └─ Notify customer order ready for shipment
```

**Why Hybrid**: BOM check needs immediate response (can't start if materials unavailable)

### Workflow 4: Customer Support Ticket (Customer → Operations → Communication)

```
1. Customer: Support ticket created
   └─ Publish TicketOpenedEvent

2. Operations receives TicketOpenedEvent
   ├─ Assign to technician
   ├─ Schedule field service
   └─ Publish ServiceScheduledEvent

3. Communication receives ServiceScheduledEvent
   └─ Send SMS/Email to customer (REST call to external API)

4. Operations: Work completed
   └─ Publish WorkCompletedEvent

5. Customer receives WorkCompletedEvent
   ├─ Update ticket status
   └─ Publish TicketClosedEvent
```

**Why Communication is REST-Only**: No domain state to publish, just external API calls

### Migration Playbook

### Phase 1: REST-Only → Hybrid

**When**: Context needs to notify other contexts of state changes

**Steps**:
1. Add `quarkus-messaging-kafka` dependency to infrastructure module
2. Create domain events in `{context}-domain/events/`
3. Implement `EventPublisherPort` in application layer
4. Create outbox infrastructure (entity, repository, publisher, scheduler)
5. Wire `OutboxEventPublisher` to use cases
6. Configure Kafka channel in `application.yml`
7. Update use cases to publish events after state changes
8. Keep existing REST APIs unchanged (backward compatible)

**Testing**:
- Verify events appear in outbox table
- Confirm Kafka topic receives messages
- Test retry logic with Kafka unavailable
- Monitor metrics and logs

### Phase 2: Hybrid → Pure EDA

**When**: Context no longer needs synchronous queries (rare)

**Steps**:
1. Identify all REST API consumers
2. Ensure consumers have event handlers for read model updates
3. Migrate consumers to event-based read models
4. Deprecate REST endpoints (version with sunset headers)
5. Monitor usage to confirm zero traffic
6. Remove REST endpoints after deprecation period

**Note**: Pure EDA migration is complex and rarely needed. Most contexts benefit from hybrid.

### Phase 3: Pure EDA → Hybrid

**When**: Need to add real-time query capabilities

**Steps**:
1. Implement query services in application layer
2. Create REST endpoints for queries only
3. Keep event publishing unchanged
4. Document API contracts
5. Register APIs in API gateway

### Schema Evolution

| Version | Date | Changes | Migration Notes |
|---------|------|---------|-----------------|
| 1.0.0 | 2025-01-15 | Initial event | N/A |
| 1.1.0 | 2025-06-10 | Added `phoneNumber` | Optional field |
| 2.0.0 | 2025-09-01 | Deprecated `emailAddress` | Migrate to `email` by v3.0.0 |
| 2.1.0 | 2025-11-16 | Added `preferredLanguage` | Optional field |

### Current Schema (v2.1.0)

```kotlin
data class UserCreatedEvent(
    val tenantId: TenantId,           // v1.0.0
    val userId: UserId,                // v1.0.0
    val username: String,              // v1.0.0
    val email: String,                 // v1.0.0

    @Since("1.1.0")
    val phoneNumber: String? = null,

    @Since("2.1.0")
    val preferredLanguage: String? = null,

    @DeprecatedSince(version = "2.0.0", replaceWith = "Use 'email'", removeIn = "3.0.0")
    val emailAddress: String? = null
)
```
```

### SAP S/4HANA Comparison

| Feature | SAP Approach | Our Implementation | Advantage |
|---------|-------------|-------------------|-----------|
| **Field Versioning** | Release notes | `@Since` annotation | ✅ Compile-time tracking |
| **Deprecation** | SAP Notes | `@DeprecatedSince` | ✅ Enforced migration docs |
| **Compatibility** | Manual testing | ArchUnit auto-checks | ✅ Build-time verification |
| **Breaking Changes** | Requires SAP upgrade | New event versions | ✅ Independent migration |
| **Consumer Safety** | Best practices | Nullable enforcement | ✅ Guaranteed compatibility |

### Benefits

1. **Zero-Downtime Deployments**: Services update independently
2. **No Coordination Required**: Producer and consumer deploy separately
3. **Clear Migration Path**: `@DeprecatedSince` guides consumers
4. **Automated Safety**: ArchUnit prevents breaking changes
5. **Version Tracking**: Know exactly when each field was added
6. **Debugging**: Version annotations help trace schema evolution

### Anti-Patterns to Avoid

❌ **Adding non-nullable fields without versioning**
```kotlin
data class OrderEvent(val orderId: OrderId, val amount: Money)  // Breaks old consumers!
```

❌ **Removing fields directly**
```kotlin
// Removed customerId - BREAKS EVERYTHING!
```

❌ **Invalid version format**
```kotlin
@Since("v2")  // Must be "2.0.0"
```

❌ **Empty deprecation guidance**
```kotlin
@DeprecatedSince(version = "2.0.0", replaceWith = "")  // No help!
```

❌ **Breaking changes without new event type**
```kotlin
val email: Email  // Changed from String to Email type - BREAKING!
```

## Alternatives Considered
- **Pure REST-only integration** across contexts. Rejected due to tight coupling and cascading failures.
- **Pure event-driven everywhere**. Rejected because many workflows require real-time query semantics.
- **Shared database across contexts**. Rejected due to loss of autonomy and migration risk.

## Consequences
### Positive

1. **Flexibility**: Each context chooses the pattern that fits its needs
2. **Pragmatic**: Not forcing pure EDA where hybrid makes more sense
3. **Proven Pattern**: Tenancy-Identity shows hybrid works well
4. **Clear Guidelines**: Teams know when to use which pattern
5. **Gradual Adoption**: Can migrate from REST-Only → Hybrid → Pure EDA incrementally
6. **Reliability**: Transactional outbox guarantees exactly-once delivery
7. **Observability**: Standard metrics and logging across all contexts

### Negative

1. **Complexity**: Multiple patterns increase cognitive load
2. **Infrastructure**: Kafka adds operational overhead
3. **Consistency**: Eventual consistency model requires careful design
4. **Testing**: Event-driven systems harder to test than synchronous
5. **Debugging**: Distributed event flows harder to trace than REST calls
6. **Learning Curve**: Team needs education on EDA patterns

#### Mitigations

1. **Documentation**: This ADR + implementation examples (tenancy-identity)
2. **Templates**: Provide starter code for outbox pattern
3. **Governance**: ArchUnit tests enforce patterns
4. **Training**: Run workshops on event-driven design
5. **Observability**: Invest in tracing and monitoring tools
6. **Defaults**: Recommend hybrid as safe default choice

### Neutral
- Some workflows will remain REST-first while event-driven maturity grows.

## Compliance
### Governance Rules

### Architecture Tests (ArchUnit)

```kotlin
// File: tests/arch/src/test/kotlin/.../EventDrivenArchitectureRules.kt

@ArchTest
val eventPublishersMustUseOutboxPattern = classes()
    .that().implement(EventPublisherPort::class.java)
    .and().resideInAPackage("..infrastructure..")
    .should().dependOnClassesThat().haveSimpleName("OutboxRepository")
    .because("All event publishers must use transactional outbox pattern")

@ArchTest
val domainEventsMustBeImmutable = classes()
    .that().implement(DomainEvent::class.java)
    .should().beAnnotatedWith(kotlin.annotation.Target::class.java)
    .orShould().haveModifier(KModifier.DATA)
    .because("Domain events must be immutable data classes")

@ArchTest
val eventPublisherPortsMustResideInApplicationLayer = classes()
    .that().haveSimpleNameEndingWith("EventPublisherPort")
    .should().resideInAPackage("..application.port.output..")
    .because("Port abstractions belong in application layer")

@ArchTest
val kafkaAdaptersMustNotBeAccessedDirectly = noClasses()
    .that().resideInAPackage("..application..")
    .should().dependOnClassesThat().haveSimpleNameContaining("Kafka")
    .because("Application layer must depend on ports, not Kafka adapters")
```

### Event Naming Conventions

- **Format**: `{Aggregate}{Action}Event` (e.g., `OrderPlacedEvent`, `PaymentProcessed Event`)
- **Tense**: Past tense (events describe what happened)
- **Package**: `{context}.domain.events`
- **Properties**: Include aggregate ID, tenant ID, timestamp, version

### Kafka Topic Naming

- **Format**: `{context}.domain.events.v{version}`
- **Examples**:
  - `identity.domain.events.v1`
  - `commerce.domain.events.v1`
  - `financial.domain.events.v1`

### Monitoring Requirements

Each event-driven context must expose:

1. **Metrics**:
   - `{context}.outbox.events.published` (counter)
   - `{context}.outbox.publish.duration` (timer)
   - `{context}.outbox.events.pending` (gauge)
   - `{context}.outbox.events.failed` (counter)

2. **Logs**:
   - Event publishing success/failure
   - Event consumption success/failure
   - Retry attempts
   - DLQ forwarding

3. **Health Checks**:
   - Kafka connectivity
   - Outbox processing lag (time since oldest pending event)

### Event Versioning and Adaptability Policy

**Last Updated**: 2025-11-16
**Rationale**: Prevent breaking changes in production when events evolve

### The Problem: Breaking Changes in Event-Driven Systems

In event-driven architectures, events are contracts between producers and consumers. When you modify an event:
- Old consumers may crash on unknown fields
- Missing required fields break new consumers
- Removing fields causes deserialization errors
- No version tracking makes debugging impossible

**Real-world scenario:**
```
Jan 2025: Deploy OrderPlacedEvent with orderId, customerId
Jun 2025: Add shippingAddress field (non-nullable)
Result: All services deployed before June CRASH! 💥
```

### Solution: Mandatory Event Versioning

#### Rule 1: New Fields Must Be Versioned

All fields added after initial event creation MUST use `@Since` annotation:

```kotlin
data class UserCreatedEvent(
    val tenantId: TenantId,
    val userId: UserId,
    val username: String,
    val email: String,  // ✅ Original fields - no annotation needed

    @Since("2.1.0")  // ✅ REQUIRED for new fields
    val phoneNumber: String? = null,

    @Since("2.2.0")
    val preferredLanguage: String? = null
)
```

**Enforcement:**
- Pre-commit hook WARNS if new fields lack `@Since`
- ArchUnit test FAILS build if `@Since` field is not nullable
- Semantic versioning (X.Y.Z) required

#### Rule 2: New Fields Must Be Nullable or Have Defaults

To maintain backward compatibility, new fields MUST be optional:

```kotlin
// ❌ WRONG - Breaks old consumers
@Since("2.1.0")
val phoneNumber: String  // NOT nullable!

// ✅ CORRECT - Old consumers can omit it
@Since("2.1.0")
val phoneNumber: String? = null

// ✅ ALSO CORRECT - Default value
@Since("2.1.0")
val phoneNumber: String = "Not provided"
```

**Why:** Old event consumers (microservices deployed before v2.1.0) don't include `phoneNumber` in their serialization. If the field is non-nullable, deserialization fails.

#### Rule 3: Deprecation Requires Migration Path

When deprecating fields, use `@DeprecatedSince` with clear guidance:

```kotlin
data class UserCreatedEvent(
    val tenantId: TenantId,
    val userId: UserId,

    @DeprecatedSince(
        version = "2.0.0",
        replaceWith = "Use 'email' field instead. The 'emailAddress' field " +
                      "will be removed in version 3.0.0.",
        removeIn = "3.0.0"
    )
    val emailAddress: String? = null,  // ✅ Keep for backward compatibility

    @Since("2.0.0")
    val email: String? = null  // ✅ Replacement field
)
```

**Migration Timeline:**
1. **v2.0.0** - Add `email`, deprecate `emailAddress` (both present)
2. **v2.1.0 - v2.9.0** - Consumers migrate to `email` field
3. **v3.0.0** - Create `UserCreatedEventV2` without `emailAddress`

#### Rule 4: Breaking Changes Require New Event Type

Never remove or change existing fields directly. Instead:

```kotlin
// ❌ WRONG - Breaks existing consumers
data class OrderPlacedEvent(
    val orderId: OrderId,
    // Removed customerId field - BREAKING CHANGE!
    val customerInfo: CustomerDetails  // Changed structure - BREAKING!
)

// ✅ CORRECT - Create new event version
data class OrderPlacedEventV2(
    val orderId: OrderId,
    val customerInfo: CustomerDetails  // New structure
)

// Keep OrderPlacedEvent for backward compatibility until consumers migrate
data class OrderPlacedEvent(
    val orderId: OrderId,
    val customerId: CustomerId
)
```

### Consumer Implementation Pattern

Event consumers must handle versioned fields gracefully:

```kotlin
@EventHandler
fun handleUserCreated(event: UserCreatedEvent) {
    // Create user with original fields (always present)
    val user = createUser(event.tenantId, event.userId, event.email)

    // Handle optional fields added in v2.1.0
    event.phoneNumber?.let { phone ->
        userService.addPhoneNumber(event.userId, phone)
    }

    // Handle optional fields added in v2.2.0
    event.preferredLanguage?.let { lang ->
        userService.setLanguage(event.userId, lang)
    }

    // Handle deprecated field (for backward compatibility)
    val email = event.email ?: event.emailAddress ?: "unknown@example.com"
}
```

### Automated Enforcement

#### Pre-Commit Hook (Warnings)

`scripts/hooks/pre-commit-resilient.ps1` checks:
- ✅ New fields in modified events have `@Since` annotation
- ✅ `@Since` fields are nullable (String? not String)
- ✅ `@DeprecatedSince` has non-empty `replaceWith` guidance

**Example output:**
```
[Event/Bean Versioning] WARNING

  ⚠️  Event/Bean versioning issues:
      • UserCreatedEvent.kt: Field with @Since is not nullable

  ADR-020: New fields must have @Since annotation and be nullable
  Example: @Since("2.1.0") val phoneNumber: String? = null

  This ensures backward compatibility with existing consumers!
```

#### ArchUnit Tests (Build Enforcement)

`tests/arch/src/test/kotlin/com/erp/tests/arch/EventBeanAdaptabilityRules.kt`:

1. **`Events should support backward compatibility with nullable new fields`**
   - Verifies `@Since` fields are nullable or have defaults
   - FAILS build if non-nullable `@Since` field found

2. **`Events with @Since annotation must follow semantic versioning`**
   - Validates version format is X.Y.Z (e.g., "2.1.0")
   - FAILS build if invalid format (e.g., "v2" or "2.1")

3. **`Deprecated event fields must have migration path documented`**
   - Checks `@DeprecatedSince` has meaningful `replaceWith` (min 10 chars)
   - FAILS build if migration guidance missing

4. **`Events should not remove fields without version bump`**
   - Detects field removal without creating new event type
   - FAILS build if `removeIn` specified but event not versioned (e.g., `EventV2`)

### Version Strategy

#### Semantic Versioning for Events

- **Major (X.0.0)**: Breaking changes (use new event type instead)
- **Minor (x.Y.0)**: New optional fields added (backward compatible)
- **Patch (x.y.Z)**: Bug fixes in event handlers (no schema changes)

**Example:**
```
v1.0.0 - Initial UserCreatedEvent (tenantId, userId, email)
v1.1.0 - Add phoneNumber (optional) → @Since("1.1.0")
v1.2.0 - Add preferredLanguage (optional) → @Since("1.2.0")
v2.0.0 - Deprecate emailAddress, add email → @DeprecatedSince
v3.0.0 - Remove emailAddress → Create UserCreatedEventV2
```

#### Event Catalog Documentation

Maintain event catalog in `docs/events/`:
```markdown
# UserCreatedEvent

**Current Version**: 2.2.0
**Created**: 2025-01-15
**Last Modified**: 2025-11-16

Include: description, owning team, schema link, version history, compatibility notes, and deprecation timeline.

## Implementation Plan
### Implementation Guidelines

### Event Publishing (Required for Pure EDA and Hybrid)

#### 1. Domain Events

**Location**: `{context}-domain/src/main/kotlin/.../events/`

```kotlin
// Example: bounded-contexts/commerce/commerce-domain/src/main/kotlin/com/erp/commerce/domain/events/
package com.erp.commerce.domain.events

import com.erp.shared.types.events.DomainEvent
import com.erp.shared.types.events.EventVersion
import java.time.Instant
import java.util.UUID

data class OrderPlacedEvent(
    val orderId: OrderId,
    val customerId: CustomerId,
    val tenantId: TenantId,
    val totalAmount: Money,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: EventVersion = EventVersion.initial(),
) : DomainEvent
```

**Rules**:
- All events implement `com.erp.shared.types.events.DomainEvent`
- Event names use past tense: `OrderPlaced`, `UserCreated`, `PaymentProcessed`
- Events are immutable data classes
- Include `tenantId` for multi-tenant isolation
- Include aggregate identifier (e.g., `orderId`, `userId`)

#### 2. Port Abstraction

**Location**: `{context}-application/src/main/kotlin/.../port/output/EventPublisherPort.kt`

```kotlin
package com.erp.commerce.application.port.output

import com.erp.shared.types.events.DomainEvent

interface EventPublisherPort {
    fun publish(event: DomainEvent)
    fun publish(events: Collection<DomainEvent>)
}
```

**Rules**:
- Single responsibility: event publishing only
- Domain-agnostic interface (uses shared types)
- No infrastructure concerns (Kafka, HTTP) in application layer

#### 3. Transactional Outbox Pattern (MANDATORY)

**Outbox Entity**: `{context}-infrastructure/.../outbox/OutboxEventEntity.kt`

```kotlin
@Entity
@Table(name = "{context}_outbox_events")
data class OutboxEventEntity(
    @Id @GeneratedValue
    val id: Long? = null,

    @Column(nullable = false)
    val eventId: UUID,

    @Column(nullable = false)
    val eventType: String,

    @Column(nullable = false)
    val aggregateId: String,

    @Column(columnDefinition = "TEXT")
    val payload: String,  // JSON serialized event

    @Column(nullable = false)
    val occurredAt: Instant,

    @Column(nullable = false)
    val recordedAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OutboxEventStatus = OutboxEventStatus.PENDING,

    @Column
    val publishedAt: Instant? = null,

    @Column(nullable = false)
    val failureCount: Int = 0,

    @Column(length = 2000)
    val lastError: String? = null,

    @Column
    val tenantId: String? = null
)

enum class OutboxEventStatus {
    PENDING, PUBLISHED, FAILED
}
```

**Why Mandatory**: Guarantees exactly-once delivery and transactional consistency between domain changes and event publishing.

#### 4. Outbox Publisher

**Location**: `{context}-infrastructure/.../outbox/OutboxEventPublisher.kt`

```kotlin
@ApplicationScoped
@Transactional(TxType.MANDATORY)
class OutboxEventPublisher(
    private val objectMapper: ObjectMapper,
    private val outboxRepository: OutboxRepository,
) : EventPublisherPort {

    override fun publish(event: DomainEvent) {
        val entity = OutboxEventEntity.from(event, objectMapper)
        outboxRepository.save(entity)
    }
}
```

**Rules**:
- Runs in same transaction as domain changes
- Serializes event to JSON and stores in outbox table
- No external calls (Kafka) in this component

#### 5. Outbox Scheduler

**Location**: `{context}-infrastructure/.../outbox/OutboxEventScheduler.kt`

```kotlin
@ApplicationScoped
class OutboxEventScheduler(
    private val outboxRepository: OutboxRepository,
    private val messagePublisher: OutboxMessagePublisher,  // Kafka adapter
    private val meterRegistry: MeterRegistry,
) {
    @Scheduled(every = "5s", concurrentExecution = SKIP)
    @Transactional(TxType.REQUIRES_NEW)
    fun publishPendingEvents() {
        val events = outboxRepository.fetchPending(batchSize = 100, maxAttempts = 3)

        events.forEach { event ->
            when (val result = messagePublisher.publish(event)) {
                is Success -> outboxRepository.markPublished(event)
                is Failure -> outboxRepository.markFailed(event, result.error)
            }
        }
    }
}
```

**Rules**:
- Separate transaction from domain operations
- Configurable polling interval (default: 5 seconds)
- Batch processing for efficiency
- Retry logic with exponential backoff
- Metrics for observability

#### 6. Kafka Message Publisher

**Location**: `{context}-infrastructure/.../outbox/KafkaOutboxMessagePublisher.kt`

```kotlin
@ApplicationScoped
class KafkaOutboxMessagePublisher(
    @Channel("{context}-events-out")
    private val emitter: Emitter<String>,
    private val meterRegistry: MeterRegistry,
) : OutboxMessagePublisher {

    @Counted(value = "{context}.outbox.events.published")
    @Timed(value = "{context}.outbox.publish.duration")
    override fun publish(eventType: String, aggregateId: String, payload: String): Result<Unit> {
        val message = Message.of(payload)
            .addMetadata(OutgoingKafkaRecordMetadata.builder<String>()
                .withKey(aggregateId)  // Partition by aggregate for ordering
                .withHeaders(RecordHeaders()
                    .add("event-type", eventType.toByteArray())
                    .add("correlation-id", MDC.get("correlationId")?.toByteArray())
                    .add("tenant-id", MDC.get("tenantId")?.toByteArray())
                )
                .build()
            )

        return emitter.send(message)
            .onFailure().invoke { cause ->
                LOGGER.error("Failed to publish event", cause)
            }
            .subscribeAsCompletionStage()
            .toCompletableFuture()
            .thenApply { success(Unit) }
            .exceptionally { failure("KAFKA_PUBLISH_FAILED", it.message) }
            .get()
    }
}
```

**Rules**:
- Partition by aggregate ID for event ordering guarantees
- Include metadata headers (event-type, tenant-id, correlation-id)
- Emit metrics for monitoring
- Handle ACK/NACK callbacks

### Event Consumption (Required for Pure EDA and Hybrid)

#### 1. Event Consumer

**Location**: `{context}-infrastructure/.../adapter/input/event/{SourceContext}EventConsumer.kt`

```kotlin
@ApplicationScoped
class IdentityEventConsumer(
    private val userService: UserSyncService,
) {

    @Incoming("identity-events-in")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    fun consumeIdentityEvents(message: Message<String>): CompletionStage<Void> {
        val eventType = message.getMetadata(IncomingKafkaRecordMetadata::class.java)
            .map { it.getHeaders().lastHeader("event-type").value().decodeToString() }
            .orElse("unknown")

        return when (eventType) {
            "UserCreatedEvent" -> handleUserCreated(message.payload)
            "UserUpdatedEvent" -> handleUserUpdated(message.payload)
            else -> {
                LOGGER.warn("Unknown event type: $eventType")
                CompletableFuture.completedFuture(null)
            }
        }.handle { _, throwable ->
            if (throwable != null) {
                LOGGER.error("Failed to process event: $eventType", throwable)
                message.nack(throwable)
            } else {
                message.ack()
            }
            null
        }
    }

    private fun handleUserCreated(payload: String): CompletionStage<Void> {
        val event = objectMapper.readValue(payload, UserCreatedEvent::class.java)
        return userService.syncUser(event).subscribeAsCompletionStage()
    }
}
```

**Rules**:
- Idempotent event handling (safe to process same event multiple times)
- Explicit ACK/NACK for message acknowledgment
- Error handling with DLQ (dead letter queue) fallback
- Event type routing based on headers
- Structured logging with correlation IDs

#### 2. Application Configuration

**Location**: `{context}-infrastructure/src/main/resources/application.yml`

```yaml
# Kafka consumer configuration
mp:
  messaging:
    incoming:
      identity-events-in:
        connector: smallrye-kafka
        topic: identity.domain.events.v1
        group.id: commerce-identity-consumer
        key.deserializer: org.apache.kafka.common.serialization.StringDeserializer
        value.deserializer: org.apache.kafka.common.serialization.StringDeserializer
        enable.auto.commit: false
        auto.offset.reset: earliest

    outgoing:
      commerce-events-out:
        connector: smallrye-kafka
        topic: commerce.domain.events.v1
        key.serializer: org.apache.kafka.common.serialization.StringSerializer
        value.serializer: org.apache.kafka.common.serialization.StringSerializer
        acks: all
        retries: 3
```

### Synchronous REST APIs (Required for Hybrid and REST-Only)

#### Design Principles

1. **Query Operations Only**: REST APIs should primarily serve queries/reads
2. **No State Changes via REST**: Use events for writes across contexts
3. **Versioning**: Include version in URL path (`/api/v1/...`)
4. **Pagination**: All list endpoints must support pagination
5. **Error Responses**: Use standard Result<T> pattern with HTTP status codes

#### Example REST Endpoint

```kotlin
@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
class CustomerResource(
    private val customerQuery: CustomerQueryService,
) {

    @GET
    @Path("/{customerId}")
    fun getCustomer(@PathParam("customerId") id: String): Response {
        return customerQuery.findById(CustomerId(id))
            .fold(
                onSuccess = { customer -> Response.ok(customer).build() },
                onFailure = { error ->
                    when (error.code) {
                        "CUSTOMER_NOT_FOUND" -> Response.status(404).entity(error).build()
                        else -> Response.status(500).entity(error).build()
                    }
                }
            )
    }
}
```

### Implementation Priority Matrix

### Phase-Based Rollout (Based on Context Map Analysis)

Given that no bounded contexts are implemented, we recommend a phased approach:

#### Phase 1: Core Domain Events (Highest ROI)

**Priority 1: Commerce Context** (4 subdomains)
- **Why First**: Central hub connecting Customer, Inventory, Financial
- **Event Volume**: High (every order triggers multiple events)
- **Dependencies**: Blocks Inventory and Financial implementation
- **Key Events**: `OrderPlaced`, `OrderFulfilled`, `OrderCancelled`, `PaymentReceived`
- **Estimated Effort**: 2-3 weeks
- **Pattern**: Hybrid (order events + product catalog REST APIs)

**Priority 2: Inventory Context** (2 subdomains)
- **Why Second**: Upstream from Commerce, downstream to Manufacturing
- **Event Volume**: Very High (stock changes frequent)
- **Dependencies**: Enables Manufacturing and Procurement
- **Key Events**: `StockAdjusted`, `ItemReserved`, `ItemReceived`, `WarehouseTransfer`
- **Estimated Effort**: 2 weeks
- **Pattern**: Hybrid (stock events + availability REST APIs)

**Priority 3: Financial Management** (3 subdomains)
- **Why Third**: Consumes events from Commerce and Procurement
- **Event Volume**: Medium-High
- **Dependencies**: Needed for BI and reporting
- **Key Events**: `InvoiceGenerated`, `PaymentPosted`, `GLEntryCreated`, `PeriodClosed`
- **Estimated Effort**: 3 weeks (complex domain)
- **Pattern**: Hybrid (financial events + account query APIs)

#### Phase 2: Manufacturing & Supply Chain

**Priority 4: Manufacturing Execution** (3 subdomains)
- **Dependencies**: Requires Inventory events
- **Key Events**: `WorkOrderStarted`, `ProductionCompleted`, `QualityCheckFailed`, `MaintenanceRequired`
- **Estimated Effort**: 3 weeks
- **Pattern**: Hybrid

**Priority 5: Procurement** (2 subdomains)
- **Dependencies**: Requires Inventory and Financial
- **Key Events**: `POIssued`, `ReceivingCompleted`, `SupplierRated`, `InvoiceReceived`
- **Estimated Effort**: 2 weeks
- **Pattern**: Hybrid

#### Phase 3: Customer & Operations

**Priority 6: Customer Relation** (3 subdomains)
- **Dependencies**: Consumes Commerce events
- **Key Events**: `CustomerCreated`, `ProfileUpdated`, `TicketOpened`, `CampaignExecuted`
- **Estimated Effort**: 2 weeks
- **Pattern**: Hybrid

**Priority 7: Operations Service**
- **Dependencies**: Consumes Customer and Manufacturing events
- **Key Events**: `ServiceRequestCreated`, `WorkCompleted`, `TechnicianDispatched`
- **Estimated Effort**: 1.5 weeks
- **Pattern**: Hybrid

#### Phase 4: Analytics & Utilities

**Priority 8: Business Intelligence**
- **Dependencies**: Requires events from all Phase 1-3 contexts
- **Implementation**: Event consumers only (no publishing)
- **Estimated Effort**: 2-3 weeks (complex aggregations)
- **Pattern**: Pure EDA (Consumer Only)

**Priority 9: Communication Hub**
- **Dependencies**: Triggered by events from other contexts
- **Implementation**: REST-only (no domain events to publish)
- **Estimated Effort**: 1 week
- **Pattern**: REST-Only

**Priority 10: Corporate Services** (2 subdomains)
- **Low Priority**: Internal utilities, minimal integration
- **Estimated Effort**: 1 week
- **Pattern**: REST-Only

### Total Estimated Timeline
- **Phase 1**: 7-8 weeks (Commerce, Inventory, Financial)
- **Phase 2**: 5 weeks (Manufacturing, Procurement)
- **Phase 3**: 3.5 weeks (Customer, Operations)
- **Phase 4**: 3-4 weeks (BI, Communication, Corporate)
- **Total**: 18-20 weeks for full platform EDA adoption

### Review and Updates

- **Next Review**: 2025-12-16 (30 days)
- **Owner**: Architecture Team
- **Status Changes**: Will move to ACCEPTED after team review and consensus
- **Last Updated**: 2025-11-16 (Added Event Versioning and Adaptability Policy)

## References

### Related ADRs
- ADR-001: Modular CQRS Implementation (Pragmatic CQRS with validation)
- ADR-006: Platform Shared Governance
- ADR-010: REST Validation Standard

### Internal Documentation
- CQRS Infrastructure: platform-infrastructure/cqrs/
- Event Store: platform-infrastructure/eventing/
- Implementation Example: bounded-contexts/tenancy-identity/identity-infrastructure/src/main/kotlin/.../outbox/
- Pre-Commit Hook: scripts/hooks/pre-commit-resilient.ps1
- Shared Types: platform-shared/common-types/src/main/kotlin/.../events/DomainEvent.kt
- Versioning Annotations: platform-shared/common-types/src/main/kotlin/.../annotations/Since.kt

### External References
- ArchUnit Tests: tests/arch/src/test/kotlin/com/erp/tests/arch/EventBeanAdaptabilityRules.kt
- Related ADRs:
