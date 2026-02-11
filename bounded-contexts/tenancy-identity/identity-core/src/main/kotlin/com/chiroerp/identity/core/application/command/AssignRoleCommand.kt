package com.chiroerp.identity.core.application.command

import java.util.UUID

/**
 * Adds or updates a user role together with its permissions.
 */
data class AssignRoleCommand(
    val userId: UUID,
    val tenantId: UUID,
    val role: RoleAssignment,
)
