package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.handler.PermissionView
import com.chiroerp.identity.core.application.handler.SessionView
import com.chiroerp.identity.core.application.handler.UserPermissionsView
import com.chiroerp.identity.core.application.handler.UserRoleView
import com.chiroerp.identity.core.application.handler.UserView
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.UserStatus
import java.time.Instant
import java.util.UUID

data class UserResponse(
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
    val passwordVersion: Int,
    val mustChangePassword: Boolean,
    val passwordRotationRequired: Boolean,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(view: UserView): UserResponse = UserResponse(
            userId = view.userId,
            tenantId = view.tenantId,
            firstName = view.firstName,
            lastName = view.lastName,
            email = view.email,
            phoneNumber = view.phoneNumber,
            locale = view.locale,
            timeZoneId = view.timeZoneId,
            status = view.status,
            roles = view.roles,
            directPermissions = view.directPermissions,
            mfaEnabled = view.mfaEnabled,
            passwordVersion = view.passwordVersion,
            mustChangePassword = view.mustChangePassword,
            passwordRotationRequired = view.passwordRotationRequired,
            lastLoginAt = view.lastLoginAt,
            createdAt = view.createdAt,
            updatedAt = view.updatedAt,
        )
    }
}

data class UserListResponse(
    val items: List<UserResponse>,
    val offset: Int,
    val limit: Int,
    val count: Int,
)

data class UserPermissionsResponse(
    val userId: UUID,
    val tenantId: UUID,
    val directPermissions: Set<PermissionView>,
    val rolePermissions: Set<PermissionView>,
    val effectivePermissions: Set<PermissionView>,
) {
    companion object {
        fun from(view: UserPermissionsView): UserPermissionsResponse = UserPermissionsResponse(
            userId = view.userId,
            tenantId = view.tenantId,
            directPermissions = view.directPermissions,
            rolePermissions = view.rolePermissions,
            effectivePermissions = view.effectivePermissions,
        )
    }
}

data class ActiveSessionResponse(
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
) {
    companion object {
        fun from(view: SessionView): ActiveSessionResponse = ActiveSessionResponse(
            sessionId = view.sessionId,
            userId = view.userId,
            tenantId = view.tenantId,
            issuedAt = view.issuedAt,
            expiresAt = view.expiresAt,
            status = view.status,
            mfaVerified = view.mfaVerified,
            ipAddress = view.ipAddress,
            userAgent = view.userAgent,
            lastSeenAt = view.lastSeenAt,
            revokedAt = view.revokedAt,
            revocationReason = view.revocationReason,
        )
    }
}
