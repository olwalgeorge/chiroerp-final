# ADR-003: Event-Driven Integration Between Contexts

**Status**: Accepted (In Progress)
**Date**: 2025-11-05
**Deciders**: Architecture Team
**Tier**: Core
**Tags**: events, integration, messaging, bounded-contexts
**Updated**: 2026-02-12 (AsyncAPI governance and event contract automation)

## Context
Bounded contexts in our ERP platform need to share data and coordinate workflows without creating tight coupling. We must choose an integration strategy that supports eventual consistency, scalability, and maintains bounded context autonomy.

We also need to decide on the specific message broker implementation: which Kafka distribution/vendor provides the best balance of cost, operational simplicity, developer experience, and production readiness for our East African healthcare ERP deployment.

## Decision
We will use **asynchronous event-driven integration** as the primary communication pattern between bounded contexts, with **synchronous REST APIs** reserved for read-heavy, request-response scenarios.

### Implementation Status: In Progress (as of 2026-02-12)

**Implementation Notes:** This ADR defines the event-driven integration standard. Adoption will proceed incrementally as bounded contexts are implemented and integrated.

### Event-Driven Integration Strategy

1. **Domain Events as Integration Mechanism**
   - Each context publishes domain events for state changes
   - Events represent facts that have occurred
   - Published to `platform-shared/common-messaging` abstraction
   - Other contexts subscribe to relevant events

2. **Event Schema Ownership**
   - Publishing context owns event schema
   - Events published in `*-shared/` modules
   - Versioned contracts with backward compatibility
   - No breaking changes without migration path

### Event Contract Documentation & Governance (Implemented Baseline)

- Async event contracts are now documented using AsyncAPI 3.0 specs under `config/asyncapi/`:
  - `config/asyncapi/asyncapi-tenancy.yaml`
  - `config/asyncapi/asyncapi-identity.yaml`
- CI enforces event-contract quality via `.github/workflows/asyncapi-lint.yml`:
  - spec validation with `@asyncapi/cli@2.3.0`
  - linting with `@stoplight/spectral-cli@6.11.1`
  - PR breaking-change checks against base branch
  - documentation artifact generation on `main`
- Local automation is available through Gradle tasks:
  - `validateAsyncApiSpecs`
  - `generateAsyncApiDocs`
  - `eventGovernance`
  - `allApiGovernance` (combined REST + event governance)

These controls establish contract governance, but domain-by-domain event catalog expansion remains ongoing.

3. **Message Broker Strategy**

   **Decision**: Hybrid approach - **Redpanda for local development**, **Apache Kafka for production**

   **Local Development** (Redpanda):
   - Single container vs 3 containers (Kafka + ZooKeeper + Schema Registry)
   - 2-second startup vs 30+ seconds
   - 500 MB RAM vs 2+ GB
   - Built-in Schema Registry (no separate container)
   - 100% Kafka API compatible (no code changes)
   - Better developer experience

   **Production** (Apache Kafka 3.7.0 + Strimzi Operator):
   - Kubernetes deployment on DigitalOcean/AWS
   - No vendor lock-in (can deploy anywhere)
   - Proven at scale (LinkedIn, Uber, Netflix)
   - $55K 5-year TCO vs $190K for Confluent Cloud
   - Strimzi operator for automated management
   - Apicurio Schema Registry

   **Alternatives Evaluated**:

   | Option | 5-Year TCO | Pros | Cons | Decision |
   |--------|-----------|------|------|----------|
   | **Apache Kafka OSS** | **$55K** | No vendor lock-in, deploy anywhere, proven at scale | Manual operations (mitigated by Strimzi) | ✅ **CHOSEN for production** |
   | **Confluent Cloud** | $190K | Managed, full ecosystem | 3.5x more expensive, vendor lock-in, cloud-only | ❌ Rejected - cost |
   | **Redpanda Cloud** | $115K | Simpler operations, fast | Smaller ecosystem, less battle-tested | ⚠️ Deferred - use for dev only |
   | **AWS MSK** | $140K | Managed, AWS integration | AWS lock-in, regional constraints (no Kenya/Tanzania) | ❌ Rejected - lock-in |

   **TCO Breakdown** (5 years, 3 brokers, 50TB storage, 200MB/s throughput):

   **Apache Kafka (OSS)**:
   - Compute: 3x c2-standard-8 @ $219/month = $39K
   - Storage: 15TB SSD @ $51/month = $9K
   - Operations: 20% time for 1 engineer @ $1,500/month = $9K
   - **Total: $57K** (rounded to $55K)

   **Confluent Cloud**:
   - Platform fees: $500/month = $30K
   - Compute: $2,000/month = $120K
   - Storage: $1,000/month = $60K
   - **Total: $210K** (rounded to $190K after discounts)

   **Why Hybrid Approach?**:
   - 💰 **Cost savings**: $135K saved over 5 years vs Confluent
   - 🚀 **Developer productivity**: Redpanda 10x faster startup, 75% less RAM
   - 🔓 **No vendor lock-in**: Can deploy Apache Kafka anywhere (on-prem, any cloud)
   - 🌍 **Regional flexibility**: Kenya/Tanzania deployments possible (AWS MSK not available)
   - ✅ **API compatibility**: 100% Kafka API compatible - zero code changes between environments

   **Configuration**:
   - Durable, ordered, replayable event log
   - Topic per bounded context (see "Kafka Topics Strategy" below)
   - Consumer groups for parallel processing
   - See "Implementation Plan" section for Kubernetes manifests

4. **Event Store (Optional per Context)**
   - Contexts can optionally persist events
   - Enables event sourcing, audit trails, temporal queries
   - To be implemented in `platform-infrastructure/eventing`

### Rationale

### Why Event-Driven?
- ✅ **Loose Coupling**: Contexts don't know about subscribers
- ✅ **Scalability**: Asynchronous processing, parallel consumers
- ✅ **Resilience**: Retry logic, dead letter queues, fault tolerance
- ✅ **Audit Trail**: Complete history of system changes
- ✅ **Temporal Decoupling**: Producer/consumer can be offline

### When to Use Synchronous APIs?
- 🔄 **Real-time queries** (e.g., "Get current stock level")
- 🔄 **User-facing operations** requiring immediate response
- 🔄 **Reference data lookups** (cached on consumer side)

### When to Use Events?
- 📢 **State change notifications** (OrderPlaced, AccountCreated)
- 📢 **Workflow orchestration** (multi-step sagas)
- 📢 **Data synchronization** across contexts
- 📢 **Analytics and reporting** (BI context consumes all events)

### Message Broker Configuration

### Kafka Topics Strategy

**Option 1: Topic per Event Type**
```
commerce.order.placed
commerce.order.cancelled
inventory.stock.reserved
financial.payment.completed
```

**Option 2: Topic per Bounded Context** (Chosen)
```
commerce-events (all commerce events)
inventory-events
financial-events
```

**Rationale**: Option 2 provides better ordering guarantees within a context and simpler topic management.

### Consumer Groups
- One consumer group per subscribing bounded context
- Enables parallel processing within a context
- Independent consumption rates

### Kafka Configuration
```yaml
# In application.properties
kafka.bootstrap.servers=localhost:9092

# Producer config
mp.messaging.outgoing.commerce-events.connector=smallrye-kafka
mp.messaging.outgoing.commerce-events.topic=commerce-events
mp.messaging.outgoing.commerce-events.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

# Consumer config
mp.messaging.incoming.commerce-events.connector=smallrye-kafka
mp.messaging.incoming.commerce-events.topic=commerce-events
mp.messaging.incoming.commerce-events.value.deserializer=io.quarkus.kafka.client.serialization.JsonbDeserializer
mp.messaging.incoming.commerce-events.group.id=inventory-consumer
```

KRaft mode eliminates ZooKeeper. Our broker configuration uses combined broker/controller roles:

```properties
process.roles=broker,controller
controller.listener.names=CONTROLLER
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
controller.quorum.voters=1@localhost:9093
```

### Idempotency

All event handlers MUST be idempotent:

```kotlin
@ConsumeEvent("commerce.OrderPlaced")
@Transactional
fun onOrderPlaced(event: OrderPlacedEvent) {
    // Check if already processed
    if (processedEvents.exists(event.eventId)) {
        logger.debug("Event ${event.eventId} already processed, skipping")
        return
    }

    // Process event
    inventoryService.reserveStock(event.orderId, event.orderLines)

    // Mark as processed
    processedEvents.record(event.eventId)
}
```

### Saga Pattern for Distributed Transactions

For multi-step workflows:

```kotlin
// Choreography-based saga
OrderPlaced -> ReserveInventory -> ProcessPayment -> ShipOrder

// Each step publishes success/failure events
// Compensating transactions on failure
PaymentFailed -> ReleaseInventory -> CancelOrder
```

### Monitoring & Observability

- **Event Publishing Metrics**: Events published per context, failures
- **Consumer Lag**: How far behind consumers are
- **Dead Letter Queue**: Failed events requiring manual intervention
- **Processing Time**: P50, P95, P99 per event type
- **Event Replay**: Track replayed events separately

### Testing Strategy

- **Unit Tests**: Mock `EventPublisher` interface
- **Integration Tests**: Use Testcontainers for Redpanda (Kafka-compatible `KafkaContainer`)
- **Contract Tests**: Verify event schema compatibility
- **E2E Tests**: Full workflow testing with real broker

## Production Kafka Operations

### Deployment (Strimzi on Kubernetes)

```yaml
# strimzi-kafka-cluster.yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: chiroerp-kafka
  namespace: kafka
spec:
  kafka:
    version: 3.7.0
    replicas: 3
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
    storage:
      type: persistent-claim
      size: 5Ti
      class: fast-ssd
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      log.retention.hours: 168
      min.insync.replicas: 2
      default.replication.factor: 3
  zookeeper:
    replicas: 3
    storage:
      type: persistent-claim
      size: 100Gi
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

### Monitoring (Prometheus + Grafana)

**Key Metrics**:
- **Broker metrics**: CPU, memory, disk I/O, network throughput
- **Topic metrics**: Message rate, byte rate, partition count
- **Consumer metrics**: Lag, processing rate, errors
- **Producer metrics**: Send rate, error rate, latency

**Alerting Rules**:
```yaml
# kafka-alerts.yaml
groups:
  - name: kafka
    interval: 30s
    rules:
      - alert: KafkaBrokerDown
        expr: up{job="kafka-broker"} == 0
        for: 2m
        annotations:
          summary: "Kafka broker {{ $labels.instance }} is down"

      - alert: UnderReplicatedPartitions
        expr: kafka_server_replicamanager_underreplicatedpartitions > 0
        for: 5m
        annotations:
          summary: "Kafka has under-replicated partitions"

      - alert: ConsumerLag
        expr: kafka_consumergroup_lag > 1000
        for: 10m
        annotations:
          summary: "Consumer {{ $labels.consumergroup }} lagging behind"
```

### Schema Registry (Apicurio)

```yaml
# apicurio-registry.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: apicurio-registry
  namespace: kafka
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: apicurio
        image: apicurio/apicurio-registry-mem:2.5.0
        env:
        - name: REGISTRY_KAFKASQL_BOOTSTRAP_SERVERS
          value: chiroerp-kafka-bootstrap:9092
        ports:
        - containerPort: 8080
```

### High Availability Configuration

**Production Kafka Settings**:
- **Replication factor**: 3 (data replicated across 3 brokers)
- **Min in-sync replicas**: 2 (must have 2 replicas synchronized before write acknowledged)
- **Retention**: 7 days (168 hours) for standard events, 7 years for financial events
- **Partitions**: 12 per topic (1 partition per bounded context for ordering)

**Disaster Recovery**:
- **Cross-region replication**: Mirror topics to secondary region (Kenya → Tanzania)
- **Backup**: Daily snapshots of ZooKeeper state and Kafka data directories
- **Recovery time objective (RTO)**: 1 hour
- **Recovery point objective (RPO)**: 5 minutes

## Alternatives Considered

### 1. Synchronous REST Only
**Rejected**: Creates tight coupling, cascading failures, difficult to scale independently.

### 2. Shared Database Integration
**Rejected**: Violates bounded context autonomy, creates coupling at data level.

### 3. RabbitMQ Instead of Kafka
**Deferred**: RabbitMQ excellent for queuing, but Kafka's log-based approach better for event sourcing and replay. May use RabbitMQ for specific use cases (e.g., workflow task queues).

### 4. Confluent Cloud (Fully Managed Kafka)
**Rejected for production** despite excellent features:
- **Cost**: $190K over 5 years vs $55K for Apache Kafka OSS (3.5x more expensive)
- **Vendor lock-in**: Locked into Confluent's pricing and cloud infrastructure
- **Cloud-only**: Cannot deploy on-premises in Tanzania clinics with intermittent connectivity
- **Regional constraints**: Limited Africa coverage, high latency from Kenya/Tanzania
- **Features we'd pay for but not use**: ksqlDB, advanced connectors, tiered storage

**When Confluent makes sense**: Large enterprises with >$10M budgets, cloud-native only, need managed Schema Registry + ksqlDB.

### 5. Redpanda for Production
**Deferred** (using for local dev only):
- **Pros**: Simpler operations (no ZooKeeper), 10x faster, 75% less memory
- **Cons**: Smaller community/ecosystem, less battle-tested at scale, newer (founded 2019)
- **Decision**: Use for local development (excellent DX), defer production adoption until more mature
- **Re-evaluation**: Consider after 2+ years of production use at scale by major companies

### 6. AWS MSK (Managed Kafka)
**Rejected** for production:
- **Cost**: $140K over 5 years (2.5x more expensive than self-hosted)
- **AWS lock-in**: Locked into AWS regions, cannot deploy on-prem or other clouds
- **Regional availability**: Not available in Kenya or Tanzania (closest: South Africa)
- **Network latency**: 50-100ms from East Africa to South Africa
- **Compliance**: Data residency requirements conflict with regional availability

**When MSK makes sense**: AWS-only deployments in regions where MSK available, teams with limited Kubernetes expertise.

### 7. Azure Event Hubs (Kafka-compatible)
**Rejected**: Similar to AWS MSK - vendor lock-in, regional constraints, higher cost than self-hosted.

### 8. Apache Pulsar
**Rejected**: Excellent technology but smaller ecosystem than Kafka, steeper learning curve, not Kafka API compatible (would require code changes).

## Consequences

### Positive
- ✅ Bounded contexts remain independent
- ✅ Easy to add new event subscribers
- ✅ Natural audit log and debugging trail
- ✅ Supports event sourcing and CQRS
- ✅ Can replay events for new read models
- ✅ Resilient to temporary service outages
- ✅ **$135K cost savings** over 5 years vs Confluent Cloud
- ✅ **No vendor lock-in** - can deploy anywhere (on-prem, any cloud)
- ✅ **Excellent developer experience** - Redpanda 10x faster startup for local dev
- ✅ **100% Kafka API compatible** - zero code changes between dev (Redpanda) and prod (Apache Kafka)

### Negative
- ❌ Eventual consistency (not immediate)
- ❌ Debugging distributed workflows is harder
- ❌ Need monitoring for event processing delays
- ❌ Dead letter queue management required
- ❌ Event schema evolution complexity
- ❌ Duplicate message handling required (idempotency)
- ❌ **Operational overhead** - managing Kafka cluster (mitigated by Strimzi operator)
- ❌ **Different environments** - Redpanda (dev) vs Apache Kafka (prod) - must test both
- ❌ **Manual operations** - no managed service for production (deliberate trade-off for cost/flexibility)

### Neutral
- ⚖️ Requires message broker infrastructure (Redpanda for dev, Apache Kafka for prod)
- ⚖️ Developers must understand async patterns
- ⚖️ Testing integration flows more complex
- ⚖️ Kubernetes expertise required for production deployment (Strimzi operator simplifies)

## Compliance

### Data Privacy & Residency
**Requirement**: Events containing PII/PHI must comply with GDPR, HIPAA data residency rules.

**How We Comply**:
- **Event Encryption**: Sensitive data in events encrypted at rest and in transit (TLS 1.3)
- **Tenant Isolation**: Events tagged with `tenantId`; consumers filter by tenant
- **Data Minimization**: Events contain minimal PII; use entity IDs instead of full records
- **Right to Erasure**: Event store supports tombstone events for GDPR erasure requests
- **Audit Trail**: All event publications and consumptions logged for compliance audits

### SOX Compliance
**Requirement**: Financial events must be immutable and auditable.

**How We Comply**:
- **Event Immutability**: Events are append-only in Redpanda; no updates/deletes
- **Event Ordering**: Guaranteed ordering within partition (by tenantId or entityId)
- **Audit Log**: Complete log of all financial events (OrderPlacedEvent, PaymentProcessedEvent, etc.)
- **Retention**: Financial events retained for 7 years per SOX requirements

### Service Level Objectives
- **Event Publication**: < 100ms (P95)
- **Event Delivery**: < 500ms end-to-end (P95)
- **Event Durability**: 99.99% (Redpanda replication factor 3)
- **Event Ordering**: 100% within partition

## Implementation Plan
### Implementation Details

### Event Definition
```kotlin
// In commerce-shared/order-shared/
package com.erp.commerce.order.events

import java.time.Instant
import java.util.UUID

data class OrderPlacedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val occurredAt: Instant = Instant.now(),
    val orderId: OrderId,
    val customerId: CustomerId,
    val orderLines: List<OrderLineDTO>,
    val totalAmount: Money,
    val version: Int = 1
) : DomainEvent
```

### Publishing Events
```kotlin
// In commerce-ecommerce/application layer
@ApplicationScoped
class PlaceOrderHandler(
    private val orderRepository: OrderRepository,
    private val eventPublisher: EventPublisher
) : CommandHandler<PlaceOrderCommand, OrderId> {

    @Transactional
    override fun handle(command: PlaceOrderCommand): OrderId {
        val order = Order.create(command.customerId, command.lines)
        orderRepository.save(order)

        // Publish event
        eventPublisher.publish(
            OrderPlacedEvent(
                orderId = order.id,
                customerId = order.customerId,
                orderLines = order.lines.map { OrderLineDTO.from(it) },
                totalAmount = order.totalAmount
            )
        )

        return order.id
    }
}
```

### Consuming Events
```kotlin
// In inventory-stock/application layer
@ApplicationScoped
class OrderEventConsumer(
    private val inventoryService: InventoryService
) {

    @ConsumeEvent("commerce.OrderPlaced")
    @Transactional
    fun onOrderPlaced(event: OrderPlacedEvent) {
        logger.info("Reserving inventory for order ${event.orderId}")

        try {
            inventoryService.reserveStock(
                orderId = event.orderId,
                items = event.orderLines.map {
                    StockReservation(it.productId, it.quantity)
                }
            )
        } catch (e: InsufficientStockException) {
            // Publish compensating event
            eventPublisher.publish(
                StockReservationFailedEvent(event.orderId, e.message)
            )
        }
    }
}
```

### Event Versioning
```kotlin
// Version 1
data class OrderPlacedEvent(
    val orderId: OrderId,
    val customerId: CustomerId,
    val totalAmount: Money,
    val version: Int = 1
)

// Version 2 - added field
data class OrderPlacedEvent(
    val orderId: OrderId,
    val customerId: CustomerId,
    val totalAmount: Money,
    val shippingAddress: Address?, // New field, nullable for backward compatibility
    val version: Int = 2
)

// Consumer handles both versions
when (event.version) {
    1 -> handleV1(event)
    2 -> handleV2(event)
}
```

### Transactional Outbox Policy

- Every bounded context that emits integration events **must** enqueue those events in its own transactional outbox table inside the same database transaction that mutates domain state. Direct broker calls from handlers (e.g., calling a Kafka emitter inside `AccountingCommandHandler`) are prohibited because they risk dropping events when the producer or broker is unavailable.
- Each outbox entry records event type, payload, channel/topic, occurrence timestamp, and publication status (`PENDING`, `PUBLISHED`, `FAILED`). Background schedulers drain the queue and publish to Kafka via context-specific publishers (see `tenancy-identity` and `financial-accounting` implementations).
- New event publishers must provide replay-safe delivery: retry with exponential backoff up to a bounded number of attempts, move irrecoverable messages to a failed state, and expose `pending` gauges/metrics for observability.
- Integration tests for every context must include a Testcontainers-backed suite (Postgres + Kafka) that exercises the full path: REST/service call → handler → outbox row persisted → scheduler invocation → Kafka topic receive. Tests are gated behind the `-PwithContainers=true` flag to keep CI fast while ensuring production parity coverage is available on demand.
- Migrations for future contexts must create `<context>_outbox_events` tables before enabling the outbox publisher, and ADR updates must document the policy so regression back to direct broker calls is caught during review.

### Review Date

- **After Phase 4**: Review event schema versioning approach
- **After Phase 6**: Assess if saga orchestration needed
- **Quarterly**: Review Kafka vs alternatives based on operational experience

## References

### Related ADRs
- ADR-001: Modular CQRS Implementation
- ADR-002: Database Per Bounded Context
- ADR-004: API Gateway Pattern (to be written)
