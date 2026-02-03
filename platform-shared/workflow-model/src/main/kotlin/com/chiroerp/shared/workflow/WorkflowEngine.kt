package com.chiroerp.shared.workflow

import java.time.Instant

/**
 * Interface for workflow and approval routing.
 * 
 * Phase 0: Hardcoded approval rules (e.g., >$10K needs manager approval)
 * Phase 1: Configuration-driven workflow definitions (BPMN-style)
 * Phase 2: AI-powered workflow optimization and intelligent routing
 * 
 * Related ADRs: ADR-006, ADR-046
 */
interface WorkflowEngine {
    /**
     * Start a new workflow instance.
     * 
     * @param context Workflow initiation context
     * @return Workflow instance ID and initial tasks
     */
    suspend fun startWorkflow(context: WorkflowContext): Result<WorkflowInstance>
    
    /**
     * Get current approval tasks for a workflow instance.
     * 
     * @param instanceId Workflow instance ID
     * @return List of pending approval tasks
     */
    suspend fun getApprovalTasks(instanceId: String): Result<List<ApprovalTask>>
    
    /**
     * Complete an approval task (approve/reject).
     * 
     * @param taskId Task identifier
     * @param decision Approval decision
     * @param userId User making the decision
     * @return Updated workflow state
     */
    suspend fun completeTask(taskId: String, decision: ApprovalDecision, userId: String): Result<WorkflowInstance>
    
    /**
     * Get workflow instance status.
     * 
     * @param instanceId Workflow instance ID
     * @return Current workflow state
     */
    suspend fun getWorkflowInstance(instanceId: String): Result<WorkflowInstance?>
    
    /**
     * Cancel a workflow instance.
     * 
     * @param instanceId Workflow instance ID
     * @param userId User requesting cancellation
     * @param reason Cancellation reason
     * @return Cancelled workflow state
     */
    suspend fun cancelWorkflow(instanceId: String, userId: String, reason: String): Result<WorkflowInstance>
}

/**
 * Context for workflow initiation.
 */
data class WorkflowContext(
    val workflowType: String,            // e.g., "INVOICE_APPROVAL", "PURCHASE_ORDER_APPROVAL"
    val entityId: String,                // ID of entity being approved (invoice, PO, etc.)
    val entityType: String,              // Type of entity
    val initiatorUserId: String,         // User who started workflow
    val orgUnitId: String,               // Organizational context
    val priority: WorkflowPriority = WorkflowPriority.NORMAL,
    val attributes: Map<String, Any> = emptyMap()  // Context-specific attributes
)

/**
 * Workflow instance state.
 */
data class WorkflowInstance(
    val instanceId: String,
    val workflowType: String,
    val entityId: String,
    val entityType: String,
    val status: WorkflowStatus,
    val currentStage: String,            // Current approval stage
    val pendingTasks: List<ApprovalTask>,
    val completedTasks: List<ApprovalTask>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant? = null
)

enum class WorkflowStatus {
    INITIATED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    CANCELLED,
    ESCALATED
}

enum class WorkflowPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
