package com.chiroerp.identity.core.application.command

import java.util.UUID

/**
 * Command that requests activation of a user within a tenant boundary.
 * Both identifiers are required to enforce tenant isolation (ADR-007).
 */
data class ActivateUserCommand(
    val userId: UUID,
    val tenantId: UUID,
)
