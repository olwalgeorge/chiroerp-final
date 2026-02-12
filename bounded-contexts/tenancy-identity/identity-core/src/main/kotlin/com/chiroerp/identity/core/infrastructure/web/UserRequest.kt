package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.command.ActivateUserCommand
import com.chiroerp.identity.core.application.command.AssignRoleCommand
import com.chiroerp.identity.core.application.command.ChangePasswordCommand
import com.chiroerp.identity.core.application.command.CreateUserCommand
import com.chiroerp.identity.core.application.command.LinkExternalIdentityCommand
import com.chiroerp.identity.core.application.command.LockUserCommand
import com.chiroerp.identity.core.application.command.PermissionGrant
import com.chiroerp.identity.core.application.command.RoleAssignment
import com.chiroerp.identity.core.domain.model.IdentityProvider
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.UUID

data class CreateUserRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:NotBlank
    @field:Size(max = 120)
    val firstName: String,
    @field:NotBlank
    @field:Size(max = 120)
    val lastName: String,
    @field:NotBlank
    @field:Size(max = 320)
    val email: String,
    @field:NotBlank
    val password: String,
    val locale: String = "en-US",
    val timeZoneId: String = "UTC",
    val phoneNumber: String? = null,
    val directPermissions: Set<@Valid PermissionGrantRequest> = emptySet(),
    val roles: Set<@Valid RoleAssignmentRequest> = emptySet(),
) {
    fun toCommand(passwordHash: String): CreateUserCommand = CreateUserCommand(
        tenantId = tenantId,
        firstName = firstName,
        lastName = lastName,
        email = email,
        passwordHash = passwordHash,
        locale = Locale.forLanguageTag(locale.replace('_', '-')).takeIf { it.language.isNotBlank() } ?: Locale.US,
        timeZoneId = timeZoneId,
        phoneNumber = phoneNumber,
        directPermissions = directPermissions.map { it.toCommandPermission() }.toSet(),
        roles = roles.map { it.toCommandRole() }.toSet(),
    )
}

data class PermissionGrantRequest(
    @field:NotBlank
    val objectId: String,
    val actions: Set<String>,
    val constraints: Map<String, String> = emptyMap(),
) {
    fun toCommandPermission(): PermissionGrant = PermissionGrant(
        objectId = objectId,
        actions = actions,
        constraints = constraints,
    )
}

data class RoleAssignmentRequest(
    @field:NotBlank
    val code: String,
    val description: String? = null,
    val permissions: Set<PermissionGrantRequest> = emptySet(),
    val sodGroup: String? = null,
) {
    fun toCommandRole(): RoleAssignment = RoleAssignment(
        code = code,
        description = description,
        permissions = permissions.map { it.toCommandPermission() }.toSet(),
        sodGroup = sodGroup,
    )
}

data class ActivateUserRequest(
    @field:NotNull
    val tenantId: UUID,
) {
    fun toCommand(userId: UUID): ActivateUserCommand = ActivateUserCommand(
        tenantId = tenantId,
        userId = userId,
    )
}

data class LockUserRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:NotBlank
    val reason: String,
) {
    fun toCommand(userId: UUID): LockUserCommand = LockUserCommand(
        tenantId = tenantId,
        userId = userId,
        reason = reason,
    )
}

data class AssignRoleRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:Valid
    val role: RoleAssignmentRequest,
) {
    fun toCommand(userId: UUID): AssignRoleCommand = AssignRoleCommand(
        tenantId = tenantId,
        userId = userId,
        role = role.toCommandRole(),
    )
}

data class ChangePasswordRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:NotBlank
    val newPassword: String,
    val historySize: Int? = null,
    val ttlDays: Long? = null,
    val forceChangeOnNextLogin: Boolean = false,
) {
    fun toCommand(userId: UUID, passwordHash: String): ChangePasswordCommand = ChangePasswordCommand(
        tenantId = tenantId,
        userId = userId,
        newPasswordHash = passwordHash,
        historySize = historySize,
        newTtl = ttlDays?.let(Duration::ofDays),
        forceChangeOnNextLogin = forceChangeOnNextLogin,
    )
}

data class LinkExternalIdentityRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:NotNull
    val provider: IdentityProvider,
    @field:NotBlank
    val subject: String,
    val claims: Map<String, String> = emptyMap(),
    val linkedAt: Instant = Instant.now(),
) {
    fun toCommand(userId: UUID): LinkExternalIdentityCommand = LinkExternalIdentityCommand(
        tenantId = tenantId,
        userId = userId,
        provider = provider,
        subject = subject,
        claims = claims,
        linkedAt = linkedAt,
    )
}
