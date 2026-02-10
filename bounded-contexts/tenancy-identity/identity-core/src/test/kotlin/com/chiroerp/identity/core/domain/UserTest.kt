package com.chiroerp.identity.core.domain

import com.chiroerp.identity.core.domain.event.MfaEnabledEvent
import com.chiroerp.identity.core.domain.event.UserActivatedEvent
import com.chiroerp.identity.core.domain.event.UserCreatedEvent
import com.chiroerp.identity.core.domain.event.UserPasswordChangedEvent
import com.chiroerp.identity.core.domain.model.MfaConfiguration
import com.chiroerp.identity.core.domain.model.MfaMethod
import com.chiroerp.identity.core.domain.model.Permission
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.domain.model.UserRole
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

class UserTest {
    private val tenantId = TenantId(UUID.fromString("30000000-0000-0000-0000-000000000001"))

    @Test
    fun `register emits created event and pending status`() {
        val user = user()

        assertThat(user.status).isEqualTo(UserStatus.PENDING)
        val events = user.pullDomainEvents()
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(UserCreatedEvent::class.java)
    }

    @Test
    fun `activate transitions user to ACTIVE and emits event`() {
        val user = user()
        user.pullDomainEvents()

        user.activate(Instant.parse("2026-02-10T12:00:00Z"))

        assertThat(user.status).isEqualTo(UserStatus.ACTIVE)
        val events = user.pullDomainEvents()
        assertThat(events.filterIsInstance<UserActivatedEvent>()).hasSize(1)
    }

    @Test
    fun `changePassword increments version and enforces history`() {
        val user = user()
        user.pullDomainEvents()

        user.changePassword("hashed-2", Instant.parse("2026-02-10T12:05:00Z"), historySize = 2, rotationTtl = Duration.ofDays(90))
        val events = user.pullDomainEvents()
        assertThat(events.filterIsInstance<UserPasswordChangedEvent>()).hasSize(1)
        assertThat(user.credentialsSnapshot.passwordVersion).isEqualTo(2)

        assertThatThrownBy {
            user.changePassword("hashed-2", Instant.parse("2026-02-10T12:06:00Z"))
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `hasPermission evaluates role and direct permissions`() {
        val rolePermission = Permission(objectId = "FI_POSTING", actions = setOf("CREATE", "APPROVE"))
        val user = user(initialRoles = setOf(UserRole(code = "FI_ACCOUNTANT", permissions = setOf(rolePermission))))
        user.pullDomainEvents()

        assertThat(user.hasPermission("fi_posting", "approve")).isTrue()
        assertThat(user.hasPermission("FI_POSTING", "delete")).isFalse()

        val scopedPermission = Permission(objectId = "USER_ADMIN", actions = setOf("INVITE"), constraints = mapOf("region" to "EU"))
        user.grantPermission(scopedPermission)

        assertThat(user.hasPermission("USER_ADMIN", "invite", context = mapOf("region" to "EU"))).isTrue()
        assertThat(user.hasPermission("USER_ADMIN", "invite", context = mapOf("region" to "US"))).isFalse()
    }

    @Test
    fun `enableMfa records configuration event`() {
        val user = user()
        user.pullDomainEvents()

        user.enableMfa(
            MfaConfiguration(
                methods = setOf(MfaMethod.TOTP),
                sharedSecret = "SECRET",
                backupCodes = setOf("code-1", "code-2"),
            ),
            occurredAt = Instant.parse("2026-02-10T13:00:00Z"),
        )

        val events = user.pullDomainEvents()
        assertThat(events.filterIsInstance<MfaEnabledEvent>()).hasSize(1)
        assertThat(user.isMfaEnabled).isTrue()
    }

    private fun user(initialRoles: Set<UserRole> = emptySet()): User = User.register(
        tenantId = tenantId,
        profile = UserProfile(
            firstName = "Jane",
            lastName = "Doe",
            email = "JANE.DOE@example.com",
            phoneNumber = null,
            locale = Locale.US,
            timeZone = ZoneId.of("UTC"),
        ),
        passwordHash = "hashed-1",
        initialRoles = initialRoles,
        now = Instant.parse("2026-02-10T11:00:00Z"),
        userId = UserId(UUID.fromString("40000000-0000-0000-0000-000000000001")),
    )
}
