package com.chiroerp.identity.core.domain.model

import com.chiroerp.identity.core.domain.event.MfaEnabledEvent
import com.chiroerp.identity.core.domain.event.UserActivatedEvent
import com.chiroerp.identity.core.domain.event.UserCreatedEvent
import com.chiroerp.identity.core.domain.event.UserDomainEvent
import com.chiroerp.identity.core.domain.event.UserLockedEvent
import com.chiroerp.identity.core.domain.event.UserLoggedInEvent
import com.chiroerp.identity.core.domain.event.UserLoggedOutEvent
import com.chiroerp.identity.core.domain.event.UserPasswordChangedEvent
import com.chiroerp.identity.core.domain.event.UserRoleAssignedEvent
import com.chiroerp.tenancy.shared.TenantId
import java.time.Duration
import java.time.Instant
import java.util.UUID

class User private constructor(
    val id: UserId,
    val tenantId: TenantId,
    var profile: UserProfile,
    private var credentials: UserCredentials,
    private val roles: MutableMap<String, UserRole>,
    private val permissions: MutableSet<Permission>,
    private var mfaConfiguration: MfaConfiguration?,
    private val externalIdentities: MutableSet<ExternalIdentity>,
    var status: UserStatus,
    val createdAt: Instant,
    var updatedAt: Instant,
    var lastLoginAt: Instant?,
) {
    private val pendingEvents = mutableListOf<UserDomainEvent>()

    val assignedRoles: Set<UserRole>
        get() = roles.values.toSet()

    val directPermissions: Set<Permission>
        get() = permissions.toSet()

    val linkedIdentities: Set<ExternalIdentity>
        get() = externalIdentities.toSet()

    val credentialsSnapshot: UserCredentials
        get() = credentials

    val isMfaEnabled: Boolean
        get() = mfaConfiguration != null

    val mfaConfigurationSnapshot: MfaConfiguration?
        get() = mfaConfiguration

    fun activate(occurredAt: Instant = Instant.now()) {
        if (status == UserStatus.ACTIVE) return
        check(status == UserStatus.PENDING || status == UserStatus.LOCKED) {
            "User cannot be activated from $status"
        }
        status = UserStatus.ACTIVE
        updatedAt = occurredAt
        record(UserActivatedEvent(id, tenantId, occurredAt))
    }

    fun lock(reason: String, occurredAt: Instant = Instant.now()) {
        require(reason.isNotBlank()) { "Lock reason is required" }
        if (status == UserStatus.DISABLED) {
            throw IllegalStateException("Disabled users are already locked")
        }
        if (status == UserStatus.LOCKED) {
            return
        }
        status = UserStatus.LOCKED
        updatedAt = occurredAt
        record(UserLockedEvent(id, tenantId, reason.trim(), occurredAt))
    }

    fun changePassword(
        newHash: String,
        changedAt: Instant = Instant.now(),
        historySize: Int = 5,
        rotationTtl: Duration? = null,
    ) {
        credentials = credentials.rotate(newHash, changedAt, historySize, forceChange = false, ttl = rotationTtl)
        updatedAt = changedAt
        record(UserPasswordChangedEvent(id, tenantId, credentials.passwordVersion, changedAt))
    }

    fun forcePasswordReset() {
        credentials = credentials.withForceChange()
        updatedAt = Instant.now()
    }

    fun assignRole(role: UserRole, occurredAt: Instant = Instant.now()) {
        val normalized = role.normalizedCode
        if (roles[normalized] == role) {
            return
        }
        roles[normalized] = role
        updatedAt = occurredAt
        record(UserRoleAssignedEvent(id, tenantId, normalized, occurredAt))
    }

    fun revokeRole(roleCode: String) {
        val normalized = roleCode.trim().uppercase()
        if (roles.remove(normalized) != null) {
            updatedAt = Instant.now()
        }
    }

    fun grantPermission(permission: Permission) {
        if (permissions.add(permission)) {
            updatedAt = Instant.now()
        }
    }

    fun revokePermission(permission: Permission) {
        if (permissions.remove(permission)) {
            updatedAt = Instant.now()
        }
    }

    fun enableMfa(configuration: MfaConfiguration, occurredAt: Instant = Instant.now()) {
        mfaConfiguration = configuration
        updatedAt = occurredAt
        record(MfaEnabledEvent(id, tenantId, configuration.methods, occurredAt))
    }

    fun disableMfa() {
        mfaConfiguration = null
        updatedAt = Instant.now()
    }

    fun recordSuccessfulLogin(
        sessionId: UUID,
        ipAddress: String?,
        userAgent: String?,
        mfaVerified: Boolean,
        occurredAt: Instant = Instant.now(),
    ) {
        lastLoginAt = occurredAt
        updatedAt = occurredAt
        record(UserLoggedInEvent(id, tenantId, sessionId, ipAddress, userAgent, mfaVerified, occurredAt))
    }

    fun recordLogout(sessionId: UUID, reason: String?, occurredAt: Instant = Instant.now()) {
        record(UserLoggedOutEvent(id, tenantId, sessionId, reason, occurredAt))
        updatedAt = occurredAt
    }

    fun linkExternalIdentity(identity: ExternalIdentity) {
        val exists = externalIdentities.any {
            it.provider == identity.provider && it.subject == identity.subject
        }
        if (!exists) {
            externalIdentities += identity
            updatedAt = identity.linkedAt
        }
    }

    fun hasPermission(objectId: String, action: String, context: Map<String, String> = emptyMap()): Boolean =
        currentPermissions().any { it.matchesObject(objectId) && it.allows(action, context) }

    fun pullDomainEvents(): List<UserDomainEvent> {
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return events
    }

    private fun currentPermissions(): Set<Permission> =
        permissions + roles.values.flatMap { it.permissions }

    private fun record(event: UserDomainEvent) {
        pendingEvents += event
    }

    companion object {
        fun register(
            tenantId: TenantId,
            profile: UserProfile,
            passwordHash: String,
            initialRoles: Set<UserRole> = emptySet(),
            initialPermissions: Set<Permission> = emptySet(),
            now: Instant = Instant.now(),
            userId: UserId = UserId.random(),
        ): User {
            val user = User(
                id = userId,
                tenantId = tenantId,
                profile = profile,
                credentials = UserCredentials(passwordHash, passwordVersion = 1, passwordChangedAt = now),
                roles = initialRoles.associateBy { it.normalizedCode }.toMutableMap(),
                permissions = initialPermissions.toMutableSet(),
                mfaConfiguration = null,
                externalIdentities = mutableSetOf(),
                status = UserStatus.PENDING,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
            )
            user.record(
                UserCreatedEvent(
                    userId = user.id,
                    tenantId = tenantId,
                    email = profile.normalizedEmail,
                    status = user.status,
                    roles = user.roles.keys.toSet(),
                    occurredAt = now,
                ),
            )
            return user
        }

        fun rehydrate(
            id: UserId,
            tenantId: TenantId,
            profile: UserProfile,
            credentials: UserCredentials,
            roles: Set<UserRole>,
            permissions: Set<Permission>,
            mfaConfiguration: MfaConfiguration?,
            externalIdentities: Set<ExternalIdentity>,
            status: UserStatus,
            createdAt: Instant,
            updatedAt: Instant,
            lastLoginAt: Instant?,
        ): User = User(
            id = id,
            tenantId = tenantId,
            profile = profile,
            credentials = credentials,
            roles = roles.associateBy { it.normalizedCode }.toMutableMap(),
            permissions = permissions.toMutableSet(),
            mfaConfiguration = mfaConfiguration,
            externalIdentities = externalIdentities.toMutableSet(),
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastLoginAt = lastLoginAt,
        )
    }
}
