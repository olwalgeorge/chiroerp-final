package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import java.util.Locale
import java.util.UUID

private const val ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN"
private const val WILDCARD = "*"

@ApplicationScoped
class AuthorizationService {
    fun canAccessTenant(principal: AuthorizationPrincipal, targetTenantId: UUID): Boolean {
        if (hasRole(principal, ROLE_SYSTEM_ADMIN)) {
            return true
        }
        return principal.tenantId == targetTenantId
    }

    fun requireTenantScope(principal: AuthorizationPrincipal, targetTenantId: UUID) {
        if (!canAccessTenant(principal, targetTenantId)) {
            throw TenantScopeViolationException(TenantId(targetTenantId))
        }
    }

    fun hasRole(principal: AuthorizationPrincipal, role: String): Boolean {
        val normalizedRole = role.trim().uppercase(Locale.ROOT)
        return principal.roles.any { it.trim().uppercase(Locale.ROOT) == normalizedRole }
    }

    fun hasPermission(
        principal: AuthorizationPrincipal,
        objectId: String,
        action: String,
        scope: String? = null,
    ): Boolean {
        if (hasRole(principal, ROLE_SYSTEM_ADMIN)) {
            return true
        }

        val targetObject = objectId.trim().uppercase(Locale.ROOT)
        val targetAction = action.trim().uppercase(Locale.ROOT)
        val targetScope = scope?.trim()?.takeIf { it.isNotEmpty() }?.uppercase(Locale.ROOT)

        return principal.permissions.any { token ->
            tokenAllows(token, targetObject, targetAction, targetScope)
        }
    }

    fun requirePermission(
        principal: AuthorizationPrincipal,
        objectId: String,
        action: String,
        scope: String? = null,
    ) {
        if (!hasPermission(principal, objectId, action, scope)) {
            throw SecurityException("Missing permission for ${objectId.trim().uppercase(Locale.ROOT)}:${action.trim().uppercase(Locale.ROOT)}")
        }
    }

    fun hasPermission(
        user: User,
        objectId: String,
        action: String,
        context: Map<String, String> = emptyMap(),
    ): Boolean = user.hasPermission(objectId, action, context)

    private fun tokenAllows(
        token: String,
        targetObject: String,
        targetAction: String,
        targetScope: String?,
    ): Boolean {
        val parts = token.split(':').map { it.trim() }
        if (parts.size !in 2..3 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false
        }

        val tokenObject = parts[0].uppercase(Locale.ROOT)
        val tokenAction = parts[1].uppercase(Locale.ROOT)
        val tokenScope = parts.getOrNull(2)
            ?.takeIf { it.isNotEmpty() }
            ?.uppercase(Locale.ROOT)

        if (tokenObject != targetObject && tokenObject != WILDCARD) {
            return false
        }
        if (tokenAction != targetAction && tokenAction != WILDCARD) {
            return false
        }

        return when {
            targetScope == null -> tokenScope == null || tokenScope == WILDCARD
            tokenScope == null -> false
            else -> tokenScope == targetScope || tokenScope == WILDCARD
        }
    }
}

data class AuthorizationPrincipal(
    val userId: UUID,
    val tenantId: UUID,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
)
