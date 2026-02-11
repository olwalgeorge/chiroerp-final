package com.chiroerp.identity.core.application.handler

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import com.chiroerp.identity.core.application.query.GetActiveSessionsQuery
import com.chiroerp.identity.core.application.query.GetUserByEmailQuery
import com.chiroerp.identity.core.application.query.GetUserPermissionsQuery
import com.chiroerp.identity.core.application.query.GetUserQuery
import com.chiroerp.identity.core.application.query.ListUsersQuery
import com.chiroerp.identity.core.domain.model.ExternalIdentity
import com.chiroerp.identity.core.domain.model.Permission
import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.util.Locale
import java.util.UUID

@ApplicationScoped
/**
 * Read-side identity queries with tenant isolation checks (ADR-005) and
 * service-level scope enforcement (ADR-007).
 */
class UserQueryHandler(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) {
    fun handle(query: GetUserQuery): UserView =
        resolveUser(UserId(query.userId), TenantId(query.tenantId)).toView()

    fun handle(query: GetUserByEmailQuery): UserView? {
        val tenantId = TenantId(query.tenantId)
        val normalizedEmail = query.email.trim().lowercase(Locale.ROOT)
        if (normalizedEmail.isBlank()) {
            return null
        }
        return userRepository.findByEmail(tenantId, normalizedEmail)?.toView()
    }

    fun handle(query: GetUserPermissionsQuery): UserPermissionsView {
        val user = resolveUser(UserId(query.userId), TenantId(query.tenantId))

        val directPermissions = user.directPermissions.map { it.toView() }.toSet()
        val rolePermissions = user.assignedRoles
            .flatMap { it.permissions }
            .map { it.toView() }
            .toSet()

        return UserPermissionsView(
            userId = user.id.value,
            tenantId = user.tenantId.value,
            directPermissions = directPermissions,
            rolePermissions = rolePermissions,
            effectivePermissions = directPermissions + rolePermissions,
        )
    }

    fun handle(query: GetActiveSessionsQuery): List<SessionView> {
        val user = resolveUser(UserId(query.userId), TenantId(query.tenantId))

        return sessionRepository.findActiveSessions(user.id)
            .asSequence()
            .filter { it.tenantId == user.tenantId && it.status == SessionStatus.ACTIVE }
            .sortedByDescending { it.lastSeenAt ?: it.issuedAt }
            .map { it.toView() }
            .toList()
    }

    fun handle(query: ListUsersQuery): List<UserView> {
        require(query.limit in 1..500) { "Query limit must be between 1 and 500" }
        require(query.offset >= 0) { "Query offset cannot be negative" }

        val tenantId = TenantId(query.tenantId)
        return userRepository.listByTenant(tenantId, query.limit, query.offset, query.status)
            .asSequence()
            .filter { it.tenantId == tenantId }
            .map { it.toView() }
            .toList()
    }

    private fun resolveUser(userId: UserId, tenantId: TenantId): User {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)

        if (user.tenantId != tenantId) {
            throw TenantScopeViolationException(tenantId)
        }

        return user
    }
}

data class UserView(
    val userId: UUID,
    val tenantId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val locale: String,
    val timeZoneId: String,
    val status: UserStatus,
    val roles: Set<UserRoleView>,
    val directPermissions: Set<PermissionView>,
    val mfaEnabled: Boolean,
    val externalIdentities: Set<ExternalIdentityView>,
    val passwordVersion: Int,
    val mustChangePassword: Boolean,
    val passwordRotationRequired: Boolean,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class UserRoleView(
    val code: String,
    val description: String,
    val sodGroup: String?,
    val permissions: Set<PermissionView>,
)

data class PermissionView(
    val objectId: String,
    val actions: Set<String>,
    val constraints: Map<String, String>,
)

data class ExternalIdentityView(
    val provider: String,
    val subject: String,
    val linkedAt: Instant,
)

data class UserPermissionsView(
    val userId: UUID,
    val tenantId: UUID,
    val directPermissions: Set<PermissionView>,
    val rolePermissions: Set<PermissionView>,
    val effectivePermissions: Set<PermissionView>,
)

data class SessionView(
    val sessionId: UUID,
    val userId: UUID,
    val tenantId: UUID,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val status: SessionStatus,
    val mfaVerified: Boolean,
    val ipAddress: String?,
    val userAgent: String?,
    val lastSeenAt: Instant?,
    val revokedAt: Instant?,
    val revocationReason: String?,
)

private fun User.toView(now: Instant = Instant.now()): UserView = UserView(
    userId = id.value,
    tenantId = tenantId.value,
    firstName = profile.firstName,
    lastName = profile.lastName,
    email = profile.normalizedEmail,
    phoneNumber = profile.phoneNumber,
    locale = profile.locale.toLanguageTag(),
    timeZoneId = profile.timeZone.id,
    status = status,
    roles = assignedRoles.map {
        UserRoleView(
            code = it.normalizedCode,
            description = it.description,
            sodGroup = it.sodGroup,
            permissions = it.permissions.map { permission -> permission.toView() }.toSet(),
        )
    }.toSet(),
    directPermissions = directPermissions.map { it.toView() }.toSet(),
    mfaEnabled = isMfaEnabled,
    externalIdentities = linkedIdentities.map { it.toView() }.toSet(),
    passwordVersion = credentialsSnapshot.passwordVersion,
    mustChangePassword = credentialsSnapshot.mustChangePassword,
    passwordRotationRequired = credentialsSnapshot.requiresRotation(now),
    lastLoginAt = lastLoginAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun Permission.toView(): PermissionView = PermissionView(
    objectId = objectId,
    actions = actions,
    constraints = constraints,
)

private fun ExternalIdentity.toView(): ExternalIdentityView = ExternalIdentityView(
    provider = provider.name,
    subject = subject,
    linkedAt = linkedAt,
)

private fun Session.toView(): SessionView = SessionView(
    sessionId = id,
    userId = userId.value,
    tenantId = tenantId.value,
    issuedAt = issuedAt,
    expiresAt = expiresAt,
    status = status,
    mfaVerified = mfaVerified,
    ipAddress = ipAddress,
    userAgent = userAgent,
    lastSeenAt = lastSeenAt,
    revokedAt = revokedAt,
    revocationReason = revocationReason,
)
