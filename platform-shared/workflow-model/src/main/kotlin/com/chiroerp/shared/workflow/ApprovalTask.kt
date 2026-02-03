package com.chiroerp.shared.workflow

import java.time.Instant

/**
 * Approval task assigned to a user or role.
 */
data class ApprovalTask(
    val taskId: String,
    val workflowInstanceId: String,
    val stage: String,                   // Approval stage name (e.g., "MANAGER_APPROVAL")
    val assignedTo: TaskAssignment,      // Who should approve
    val status: TaskStatus,
    val dueDate: Instant?,               // SLA deadline
    val createdAt: Instant,
    val completedAt: Instant? = null,
    val decision: ApprovalDecision? = null,
    val comments: String? = null
) {
    /**
     * Check if task is overdue.
     */
    fun isOverdue(): Boolean {
        return status == TaskStatus.PENDING && 
               dueDate != null && 
               Instant.now().isAfter(dueDate)
    }
}

/**
 * Task assignment (user, role, or group).
 */
sealed class TaskAssignment {
    data class User(val userId: String) : TaskAssignment()
    data class Role(val roleName: String) : TaskAssignment()
    data class Group(val groupName: String) : TaskAssignment()
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    ESCALATED,
    DELEGATED
}
