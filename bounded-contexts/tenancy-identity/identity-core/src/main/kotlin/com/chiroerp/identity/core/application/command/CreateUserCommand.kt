package com.chiroerp.identity.core.application.command

import java.util.Locale
import java.util.UUID

/**
 * Command for creating a new user inside a tenant boundary.
 * The password value is already hashed upstream (see ADR-007).
 */
data class CreateUserCommand(
    val tenantId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val locale: Locale = Locale.US,
    val timeZoneId: String = "UTC",
    val phoneNumber: String? = null,
    val directPermissions: Set<PermissionGrant> = emptySet(),
    val roles: Set<RoleAssignment> = emptySet(),
)

/** Describes a direct permission grant for the target user. */
data class PermissionGrant(
    val objectId: String,
    val actions: Set<String>,
    val constraints: Map<String, String> = emptyMap(),
)

/** Describes a role assignment bundled with the create command payload. */
data class RoleAssignment(
    val code: String,
    val description: String? = null,
    val permissions: Set<PermissionGrant> = emptySet(),
    val sodGroup: String? = null,
)
