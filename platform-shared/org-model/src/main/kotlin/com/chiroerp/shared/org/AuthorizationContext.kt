package com.chiroerp.shared.org

/**
 * Authorization context combining user identity and organizational scope.
 * Used for data filtering and authorization checks.
 * 
 * Phase 0: Minimal context with single tenant
 * Phase 1: Full multi-tenant context from JWT
 * 
 * Related ADRs: ADR-006, ADR-007, ADR-014, ADR-045
 */
data class AuthorizationContext(
    val userId: String,
    val tenantId: String,
    val orgUnitId: String,               // Primary org unit for user
    val accessibleOrgUnits: Set<String>, // All org units user can access
    val roles: Set<String>,              // User roles (e.g., "FINANCE_MANAGER")
    val permissions: Set<String>         // Fine-grained permissions (e.g., "invoices:approve")
) {
    /**
     * Check if user can access data from given org unit.
     */
    fun canAccessOrgUnit(orgUnitId: String): Boolean {
        return orgUnitId in accessibleOrgUnits
    }
    
    /**
     * Check if user has specific role.
     */
    fun hasRole(role: String): Boolean {
        return role in roles
    }
    
    /**
     * Check if user has specific permission.
     */
    fun hasPermission(permission: String): Boolean {
        return permission in permissions
    }
    
    companion object {
        /**
         * Phase 0: Development context with full access.
         */
        val SINGLE_TENANT_DEV = AuthorizationContext(
            userId = "dev-user",
            tenantId = "00000000-0000-0000-0000-000000000001",
            orgUnitId = "1000",
            accessibleOrgUnits = setOf("1000"),
            roles = setOf("DEVELOPER", "ADMIN", "SYSTEM_ADMIN"),
            permissions = setOf("*:*")  // Wildcard all permissions
        )
    }
}
