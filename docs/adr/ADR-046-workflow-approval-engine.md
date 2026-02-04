# ADR-046: Workflow & Approval Engine

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Product Team
**Priority**: P0 (Critical)
**Tier**: Core
**Tags**: workflow, approvals, orchestration, human-tasks, temporal

## Context
ChiroERP includes domain services and event-driven integration for system-to-system workflows. A general-purpose ERP also requires orchestration for **human-centric processes**: multi-level approvals, task assignment, escalations, timeouts, exception handling, and process monitoring. This ADR defines a workflow & approval capability that complements domain logic and events with reliable long-running process execution.

### Design Goals
- Support long-running business processes (O2C, P2P, close, exception handling)
- Provide human tasks (inbox), multi-level approvals, delegation, and escalations
- Enable parallel execution, retries, timeouts, and compensating actions (sagas)
- Provide visibility: process history, current state, SLAs, and audit trails
- Keep workflow logic upgrade-safe and testable (code + configuration)
- Allow future “BPMN-style” modeling for BA collaboration without constraining the runtime

### Problem Statement
How do we enable long-running business processes with human tasks, approvals, parallel processing, timeouts, and exception handling without building a custom workflow engine?

## Decision
Implement a **Workflow & Approval Engine** for long-running processes using **Temporal.io** as the workflow runtime. Where a visual model is needed, represent workflows using BPMN-style concepts and (optionally) BPMN 2.0 interchange later, without requiring BPMN execution at runtime.

### Why Temporal (Not Camunda/Zeebe)

| Feature | Temporal | Camunda/Zeebe | Justification |
|---------|----------|---------------|---------------|
| **Microservices Native** | ✅ Service-per-workflow | ⚠️ Central engine | Aligns with ChiroERP architecture |
| **Code-First** | ✅ Kotlin workflows | ⚠️ XML/BPMN first | Developer-friendly |
| **Event Sourcing** | ✅ Native | ❌ External | Matches CQRS pattern (ADR-001) |
| **Compensation** | ✅ Saga support | ✅ Compensation events | Both support, Temporal simpler |
| **Observability** | ✅ Built-in UI | ✅ Cockpit | Both good |
| **Learning Curve** | ✅ Lower (code) | ⚠️ Higher (BPMN modeling) | Faster team ramp-up |
| **Vendor Lock-In** | ⚠️ Temporal Cloud | ⚠️ Camunda Cloud | Both have cloud offerings |

**Decision**: Start with **Temporal** for code-first workflows. Add visual BPMN designer later if needed.

---

## Core Workflow Patterns

### 1. Multi-Level Approval Workflow

#### Use Case: Purchase Order Approval
```kotlin
@WorkflowInterface
interface PurchaseOrderApprovalWorkflow {
    @WorkflowMethod
    fun approve(request: POApprovalRequest): POApprovalResult
}

@WorkflowImpl
class PurchaseOrderApprovalWorkflowImpl : PurchaseOrderApprovalWorkflow {

    private val approvalActivity = Workflow.newActivityStub(
        ApprovalActivity::class.java,
        ActivityOptions {
            startToCloseTimeout = Duration.ofMinutes(5)
        }
    )

    override fun approve(request: POApprovalRequest): POApprovalResult {
        val schema = approvalActivity.getApprovalSchema(
            tenantId = request.tenantId,
            documentType = "PURCHASE_ORDER",
            context = request.context // amount, vendor risk, etc.
        )

        // Execute approval levels sequentially
        for (level in schema.approvalLevels) {
            val approvers = approvalActivity.determineApprovers(level, request)

            // Parallel approval within level (if multiple approvers)
            val approvals = approvers.map { approver ->
                Async.function {
                    waitForApproval(request.poId, approver, level.levelNumber)
                }
            }

            val results = Async.allOf(approvals).get()

            // Check if any rejections
            if (results.any { it.decision == REJECTED }) {
                return POApprovalResult(status = REJECTED, rejectedBy = results.first { it.decision == REJECTED }.approver)
            }

            // Check for escalation timeout
            if (Workflow.await(Duration.ofHours(level.escalationTimeHours)) {
                allApproved(results)
            }.not()) {
                approvalActivity.escalate(request.poId, level)
            }
        }

        // All levels approved
        approvalActivity.completePOApproval(request.poId)
        return POApprovalResult(status = APPROVED)
    }

    private fun waitForApproval(poId: POId, approver: UserId, level: Int): ApprovalDecision {
        // Wait for signal (human task completion)
        val signalName = "po_approval_${poId}_${approver}_${level}"
        return Workflow.await { getApprovalDecision(signalName) }
    }
}
```

#### Human Task API
```kotlin
@Path("/workflow/tasks")
class WorkflowTaskResource {

    @Inject
    lateinit var temporalClient: WorkflowClient

    @GET
    @Path("/my-tasks")
    fun getMyTasks(@Context userId: UserId): List<WorkflowTask> {
        // Query Temporal for pending tasks assigned to user
        return temporalClient.newWorkflowStub(
            PurchaseOrderApprovalWorkflow::class.java
        ).queryPendingTasks(userId)
    }

    @POST
    @Path("/{taskId}/approve")
    fun approveTask(
        @PathParam("taskId") taskId: TaskId,
        @Valid request: ApprovalDecisionRequest
    ): Response {
        // Send signal to workflow
        val workflowId = getWorkflowIdForTask(taskId)
        temporalClient.newWorkflowStub(
            PurchaseOrderApprovalWorkflow::class.java,
            workflowId
        ).signal("approval_decision", request.decision)

        return Response.ok().build()
    }
}
```

---

### 2. Long-Running Business Process

#### Use Case: Order-to-Cash (O2C) Orchestration
```kotlin
@WorkflowInterface
interface OrderToCashWorkflow {
    @WorkflowMethod
    fun execute(orderId: OrderId): O2CResult
}

@WorkflowImpl
class OrderToCashWorkflowImpl : OrderToCashWorkflow {

    private val salesActivity = Workflow.newActivityStub(SalesActivity::class.java)
    private val inventoryActivity = Workflow.newActivityStub(InventoryActivity::class.java)
    private val financeActivity = Workflow.newActivityStub(FinanceActivity::class.java)

    override fun execute(orderId: OrderId): O2CResult {
        try {
            // Step 1: Create Sales Order (synchronous)
            val order = salesActivity.createSalesOrder(orderId)

            // Step 2: Credit Check (may require approval)
            val creditCheck = salesActivity.performCreditCheck(order.customerId, order.totalAmount)
            if (creditCheck.status == BLOCKED) {
                // Child workflow for credit approval
                val creditApprovalWorkflow = Workflow.newChildWorkflowStub(
                    CreditApprovalWorkflow::class.java
                )
                val creditApproval = creditApprovalWorkflow.approve(CreditApprovalRequest(orderId))
                if (creditApproval.decision == REJECTED) {
                    return O2CResult(status = CREDIT_REJECTED)
                }
            }

            // Step 3: Inventory Reservation (compensatable)
            val reservation = inventoryActivity.reserveStock(order)

            // Step 4: Shipment (wait for external event)
            salesActivity.createShipment(order)
            val shipmentConfirmation = Workflow.await(Duration.ofDays(7)) {
                getShipmentConfirmation(order.shipmentId)
            }

            if (!shipmentConfirmation.isPresent) {
                // Timeout: compensate reservation
                inventoryActivity.cancelReservation(reservation.reservationId)
                return O2CResult(status = SHIPMENT_TIMEOUT)
            }

            // Step 5: Invoice Generation
            val invoice = financeActivity.createInvoice(order, shipmentConfirmation.get())

            // Step 6: Payment Collection (wait for days)
            val payment = Workflow.await(Duration.ofDays(30)) {
                getPaymentReceived(invoice.invoiceId)
            }

            if (!payment.isPresent) {
                // Trigger dunning workflow (parallel)
                Workflow.newChildWorkflowStub(DunningWorkflow::class.java)
                    .execute(invoice.customerId, invoice.invoiceId)
            }

            return O2CResult(status = COMPLETED, invoiceId = invoice.invoiceId)

        } catch (e: Exception) {
            // Compensation logic
            compensate()
            throw e
        }
    }

    private fun compensate() {
        // Saga compensation pattern
        // Undo: cancel reservation, reverse invoice, etc.
    }
}
```

---

### 3. Parallel Processing with Aggregation

#### Use Case: Period Close Orchestration
```kotlin
@WorkflowInterface
interface PeriodCloseWorkflow {
    @WorkflowMethod
    fun execute(request: PeriodCloseRequest): PeriodCloseResult
}

@WorkflowImpl
class PeriodCloseWorkflowImpl : PeriodCloseWorkflow {

    override fun execute(request: PeriodCloseRequest): PeriodCloseResult {
        val tasks = listOf(
            CloseTask("AP", "Close Accounts Payable"),
            CloseTask("AR", "Close Accounts Receivable"),
            CloseTask("Inventory", "Close Inventory Valuation"),
            CloseTask("Assets", "Run Depreciation"),
            CloseTask("Controlling", "Allocate Cost Centers")
        )

        // Execute close tasks in parallel
        val futures = tasks.map { task ->
            Async.function {
                executeCloseTask(task, request.period)
            }
        }

        // Wait for all tasks to complete
        val results = Async.allOf(futures).get()

        // Check for failures
        val failures = results.filter { !it.success }
        if (failures.isNotEmpty()) {
            return PeriodCloseResult(
                status = FAILED,
                failedTasks = failures.map { it.taskName }
            )
        }

        // All tasks successful: perform GL close
        financeActivity.performGLClose(request.period)

        return PeriodCloseResult(status = COMPLETED)
    }
}
```

---

### 4. Event-Driven Workflow Triggering

#### Kafka Event → Workflow Start
```kotlin
@ApplicationScoped
class WorkflowEventListener {

    @Inject
    lateinit var temporalClient: WorkflowClient

    @Incoming("sales.order.approved")
    fun onOrderApproved(event: SalesOrderApprovedEvent) {
        // Start O2C workflow
        val workflow = temporalClient.newWorkflowStub(
            OrderToCashWorkflow::class.java,
            WorkflowOptions {
                workflowId = "o2c-${event.payload.orderId}"
                taskQueue = "order-fulfillment"
            }
        )

        WorkflowClient.start(workflow::execute, event.payload.orderId)
    }

    @Incoming("warehouse.shipment.confirmed")
    fun onShipmentConfirmed(event: ShipmentConfirmedEvent) {
        // Signal running O2C workflow
        val workflowId = "o2c-${event.payload.orderId}"
        val workflow = temporalClient.newWorkflowStub(
            OrderToCashWorkflow::class.java,
            workflowId
        )
        workflow.signal("shipment_confirmed", event.payload)
    }
}
```

---

## Workflow Administration

### Workflow Monitoring Dashboard
```kotlin
@Path("/workflow/monitoring")
class WorkflowMonitoringResource {

    @GET
    @Path("/running")
    fun getRunningWorkflows(
        @QueryParam("workflowType") workflowType: String?
    ): List<WorkflowExecution> {
        // Query Temporal for active workflows
        return temporalClient.listOpenWorkflows(
            query = "WorkflowType='$workflowType' AND ExecutionStatus='Running'"
        )
    }

    @GET
    @Path("/{workflowId}/history")
    fun getWorkflowHistory(
        @PathParam("workflowId") workflowId: String
    ): WorkflowHistory {
        // Get event history (audit trail)
        return temporalClient.getWorkflowExecutionHistory(workflowId)
    }

    @POST
    @Path("/{workflowId}/terminate")
    fun terminateWorkflow(
        @PathParam("workflowId") workflowId: String,
        @Valid request: TerminateRequest
    ): Response {
        temporalClient.newUntypedWorkflowStub(workflowId)
            .terminate(request.reason)
        return Response.ok().build()
    }
}
```

### Task Inbox UI Requirements
```
User Task Inbox
├─ My Tasks (assigned to me)
├─ Team Tasks (assigned to my team/role)
├─ Completed Tasks (audit trail)
└─ Escalated Tasks (overdue)

Task Details View
├─ Business Context (PO details, invoice details, etc.)
├─ Approval History (who approved at each level)
├─ Actions (Approve, Reject, Delegate, Comment)
└─ Related Documents (attachments, supporting docs)
```

---

## Integration with Configuration Framework (ADR-044)

### Dynamic Approval Routing
```kotlin
@ActivityImpl
class ApprovalActivity {

    @Inject
    lateinit var configRepository: ConfigurationRepository

    fun getApprovalSchema(
        tenantId: TenantId,
        documentType: String,
        context: Map<String, Any>
    ): ApprovalSchema {
        // Fetch approval schema from configuration
        val schema = configRepository.getApprovalSchema(tenantId, documentType)

        // Filter levels based on context (e.g., amount thresholds)
        return schema.copy(
            approvalLevels = schema.approvalLevels.filter { level ->
                evaluateConditions(level.conditions, context)
            }
        )
    }

    fun determineApprovers(
        level: ApprovalLevel,
        request: POApprovalRequest
    ): List<UserId> {
        return when (level.approverDetermination) {
            ROLE -> userRepository.findByRole(level.approverRole!!)
            ORG_HIERARCHY -> {
                // Determine from org model (ADR-045)
                val plant = request.plantId
                val plantManager = orgService.getPlantManager(plant)
                listOf(plantManager)
            }
            SPECIFIC_USER -> listOf(level.specificUserId!!)
        }
    }
}
```

---

## Workflow Versioning

### Handling Workflow Changes
```kotlin
@WorkflowImpl(version = 2) // New version
class PurchaseOrderApprovalWorkflowImpl_V2 : PurchaseOrderApprovalWorkflow {

    override fun approve(request: POApprovalRequest): POApprovalResult {
        // New logic: added risk assessment step
        val riskScore = assessVendorRisk(request.vendorId)
        if (riskScore > 80) {
            // Additional approval required
        }

        // Rest of original logic
    }
}

// Old workflows (started with V1) continue with V1 logic
// New workflows (started after V2 deploy) use V2 logic
```

**Temporal handles version compatibility automatically.**

---

## Performance Standards

### Workflow Execution Metrics

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Workflow Start Latency | < 100ms p95 | > 200ms |
| Activity Execution Latency | < 500ms p95 | > 1s |
| Task Assignment Latency | < 200ms | > 500ms |
| Concurrent Workflows (per tenant) | 10,000+ | Performance degradation |
| Workflow History Retention | 2 years | Configurable |

### Scaling Strategy
- **Horizontal Scaling**: Deploy multiple Temporal workers (Quarkus services)
- **Task Queue Partitioning**: Separate queues per domain (sales, finance, procurement)
- **Workflow Archival**: Move completed workflows to cold storage after 90 days

---

## Alternatives Considered

### 1. Camunda/Zeebe
**Pros**: BPMN 2.0 standard, visual designer, mature ecosystem
**Cons**: XML-heavy, central engine (not microservices-native), steeper learning curve
**Decision**: Deferred—can add BPMN designer on top of Temporal later

### 2. Custom State Machine (Spring State Machine)
**Pros**: Lightweight, full control
**Cons**: No workflow history, no human tasks, no distributed sagas
**Decision**: Rejected—insufficient for ERP complexity

### 3. Apache Airflow
**Pros**: Mature, Python-friendly
**Cons**: Batch-oriented (DAGs), not designed for long-running business processes
**Decision**: Rejected—wrong abstraction level

### 4. AWS Step Functions
**Pros**: Managed service, low ops overhead
**Cons**: Vendor lock-in, limited local dev/test, JSON-based state machines
**Decision**: Rejected—avoid AWS lock-in

### 5. No Workflow Engine (Domain Services Only)
**Pros**: Simple
**Cons**: Cannot handle human tasks, no process monitoring, no compensation logic
**Decision**: Rejected—does not meet requirements for approvals, task inboxes, and long-running orchestration

---

## Consequences

### Positive
- ✅ **Human-Centric Processes**: Approvals, task inboxes, escalations
- ✅ **Long-Running Processes**: O2C, P2P, Close workflows (days/weeks duration)
- ✅ **Saga Pattern**: Built-in compensation for distributed transactions
- ✅ **Auditability**: Full workflow history (who, what, when)
- ✅ **Observability**: Temporal UI shows running workflows, failures, retries
- ✅ **Code-First**: Kotlin workflows (type-safe, testable, refactorable)

### Negative
- ❌ **Dependency**: Temporal is a critical runtime dependency
- ❌ **Learning Curve**: Team needs to learn Temporal concepts
- ❌ **No Visual Designer**: Business analysts cannot model workflows (yet)
- ❌ **Migration**: Existing hardcoded approvals need migration to workflows

### Neutral
- Temporal Cloud vs self-hosted decision deferred
- BPMN designer can be added later (Temporal supports BPMN import)
- Some simple approvals may not need workflow (trade-off: consistency vs complexity)

---

## Compliance

### Audit Requirements
- All workflow executions logged with full event history
- Approval decisions immutable and signed (userId, timestamp, decision)
- Workflow history retained for 7 years (regulatory compliance)

### Authorization
- Task assignment respects org-based authorization (ADR-045)
- Delegation requires explicit permission
- Workflow admin role separate from task approver role

---

## Implementation Plan

### Phase 1: Foundation (Months 1-2)
- ✅ Temporal deployment (self-hosted on Kubernetes)
- ✅ Quarkus + Temporal integration
- ✅ Basic approval workflow (PO approval)
- ✅ Task inbox API + UI

### Phase 2: Core Workflows (Months 3-4)
- ✅ Order-to-Cash (O2C) workflow
- ✅ Procure-to-Pay (P2P) workflow
- ✅ Invoice approval workflow
- ✅ Integration with configuration framework (ADR-044)

### Phase 3: Advanced Features (Months 5-6)
- ✅ Period close orchestration
- ✅ Dunning workflow
- ✅ Credit management workflow
- ✅ Workflow monitoring dashboard

### Phase 4: Visual Designer (Month 7+)
- ✅ BPMN 2.0 designer integration (optional)
- ✅ Low-code workflow builder for business analysts
- ✅ Workflow templates library (industry-specific)

---

## References

### Related ADRs
- ADR-011: Saga Pattern & Compensating Transactions (workflow compensation)
- ADR-044: Configuration & Rules Framework (approval schemas)
- ADR-045: Enterprise Organizational Model (approver determination)
- ADR-014: Authorization Objects & SoD (task authorization)

### Technology References
- [Temporal.io Documentation](https://docs.temporal.io/)
- [BPMN 2.0 Specification](https://www.omg.org/spec/BPMN/2.0/)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)

### Industry References
- SAP Business Workflow (SWF)
- Oracle BPM Suite
- Salesforce Flow Builder
- Workday Business Process Framework

---

**Note**: This ADR establishes workflow, approvals, and tasking as a core platform capability for predictable orchestration, auditability, and enterprise usability.
