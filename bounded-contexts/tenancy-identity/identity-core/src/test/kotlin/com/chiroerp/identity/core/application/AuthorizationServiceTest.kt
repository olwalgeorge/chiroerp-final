package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.service.AuthorizationPrincipal
import com.chiroerp.identity.core.application.service.AuthorizationService
import com.chiroerp.identity.core.domain.model.Permission
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.domain.model.UserRole
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

class AuthorizationServiceTest {
    private val tenantA = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private val tenantB = UUID.fromString("30000000-0000-0000-0000-000000000001")

    private lateinit var service: AuthorizationService

    @BeforeEach
    fun setup() {
        service = AuthorizationService()
    }

    @Test
    fun `tenant scope allows same tenant and system admin bypass`() {
        val tenantUser = AuthorizationPrincipal(
            userId = UUID.randomUUID(),
            tenantId = tenantA,
            roles = setOf("TENANT_ADMIN"),
        )
        val systemAdmin = AuthorizationPrincipal(
            userId = UUID.randomUUID(),
            tenantId = tenantA,
            roles = setOf("system_admin"),
        )

        assertThat(service.canAccessTenant(tenantUser, tenantA)).isTrue()
        assertThat(service.canAccessTenant(tenantUser, tenantB)).isFalse()
        assertThat(service.canAccessTenant(systemAdmin, tenantB)).isTrue()
    }

    @Test
    fun `require tenant scope throws for cross tenant non-admin`() {
        val principal = AuthorizationPrincipal(
            userId = UUID.randomUUID(),
            tenantId = tenantA,
            roles = setOf("TENANT_ADMIN"),
        )

        assertThatThrownBy {
            service.requireTenantScope(principal, tenantB)
        }.isInstanceOf(TenantScopeViolationException::class.java)
    }

    @Test
    fun `permission tokens support object-action and scoped wildcard`() {
        val principal = AuthorizationPrincipal(
            userId = UUID.randomUUID(),
            tenantId = tenantA,
            permissions = setOf(
                "USER_ADMIN:READ",
                "FI_POSTING:CREATE:1000",
                "AR_INVOICE:APPROVE:*",
            ),
        )

        assertThat(service.hasPermission(principal, "USER_ADMIN", "READ")).isTrue()
        assertThat(service.hasPermission(principal, "FI_POSTING", "CREATE", scope = "1000")).isTrue()
        assertThat(service.hasPermission(principal, "AR_INVOICE", "APPROVE", scope = "2000")).isTrue()
        assertThat(service.hasPermission(principal, "FI_POSTING", "CREATE", scope = "2000")).isFalse()
        assertThat(service.hasPermission(principal, "USER_ADMIN", "DELETE")).isFalse()
    }

    @Test
    fun `requirePermission throws on denied access`() {
        val principal = AuthorizationPrincipal(
            userId = UUID.randomUUID(),
            tenantId = tenantA,
            permissions = setOf("USER_ADMIN:READ"),
        )

        assertThatThrownBy {
            service.requirePermission(principal, "USER_ADMIN", "DELETE")
        }.isInstanceOf(SecurityException::class.java)
    }

    @Test
    fun `domain user permission evaluation includes role and direct grants`() {
        val user = User.register(
            tenantId = TenantId(tenantA),
            profile = UserProfile(
                firstName = "Jane",
                lastName = "Doe",
                email = "jane.doe@example.com",
                phoneNumber = null,
                locale = Locale.US,
                timeZone = ZoneId.of("UTC"),
            ),
            passwordHash = "hash-1",
            initialRoles = setOf(
                UserRole(
                    code = "fi_poster",
                    permissions = setOf(Permission("FI_POSTING", setOf("CREATE"))),
                ),
            ),
            initialPermissions = setOf(Permission("USER_ADMIN", setOf("READ"))),
            now = Instant.parse("2026-02-11T10:00:00Z"),
        )

        assertThat(service.hasPermission(user, "FI_POSTING", "CREATE")).isTrue()
        assertThat(service.hasPermission(user, "USER_ADMIN", "READ")).isTrue()
        assertThat(service.hasPermission(user, "USER_ADMIN", "DELETE")).isFalse()
    }
}
