package com.chiroerp.shared.workflow

/**
 * Approval decision made by approver.
 */
data class ApprovalDecision(
    val action: ApprovalAction,
    val comments: String? = null,
    val delegateTo: String? = null       // User ID if delegating
)

enum class ApprovalAction {
    /**
     * Approve the request.
     */
    APPROVE,
    
    /**
     * Reject the request.
     */
    REJECT,
    
    /**
     * Return to submitter for corrections.
     */
    RETURN_FOR_REVISION,
    
    /**
     * Delegate to another user.
     */
    DELEGATE,
    
    /**
     * Escalate to higher authority.
     */
    ESCALATE
}
