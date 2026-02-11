package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.command.ActivateUserCommand
import com.chiroerp.identity.core.application.command.AssignRoleCommand
import com.chiroerp.identity.core.application.command.ChangePasswordCommand
import com.chiroerp.identity.core.application.command.CreateUserCommand
import com.chiroerp.identity.core.application.command.EnableMfaCommand
import com.chiroerp.identity.core.application.command.LinkExternalIdentityCommand
import com.chiroerp.identity.core.application.command.LockUserCommand
import com.chiroerp.identity.core.application.command.PermissionGrant
import com.chiroerp.identity.core.application.command.RoleAssignment
import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserAlreadyExistsException
import com.chiroerp.identity.core.application.handler.UserCommandHandler
import com.chiroerp.identity.core.domain.event.UserDomainEvent
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.MfaMethod
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.UserEventPublisher
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.Locale
import java.util.UUID

class UserCommandHandlerTest {
    private val tenantId = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private lateinit var repository: InMemoryUserRepository
    private lateinit var publisher: CapturingUserEventPublisher
    private lateinit var handler: UserCommandHandler

    @BeforeEach
    fun setup() {
        repository = InMemoryUserRepository()
        publisher = CapturingUserEventPublisher()
        handler = UserCommandHandler(repository, publisher)
    }

    @Test
    fun `create user persists aggregate and emits events`() {
        val command = CreateUserCommand(
            tenantId = tenantId,
            firstName = "Jane",
            lastName = "Doe",
            email = "Jane.Doe@example.com",
            passwordHash = "hashed",
            locale = Locale.US,
            timeZoneId = "UTC",
            roles = setOf(
                RoleAssignment(
                    code = "fi_accountant",
                    permissions = setOf(
                        PermissionGrant("FI_POSTING", setOf("CREATE"))
                    ),
                ),
            ),
        )

        val created = handler.handle(command)

        assertThat(created.profile.fullName).isEqualTo("Jane Doe")
        assertThat(repository.findByEmail(TenantId(tenantId), "jane.doe@example.com")).isNotNull
        assertThat(publisher.publishedEvents).isNotEmpty
    }

    @Test
    fun `creating duplicate email throws`() {
        val command = CreateUserCommand(
            tenantId = tenantId,
            firstName = "Jane",
            lastName = "Doe",
            email = "Jane.Doe@example.com",
            passwordHash = "hashed",
        )
        handler.handle(command)

        assertThatThrownBy { handler.handle(command) }
            .isInstanceOf(UserAlreadyExistsException::class.java)
    }

    @Test
    fun `activate enforces tenant scope`() {
        val user = seedUser()

        handler.handle(
            ActivateUserCommand(
                userId = user.id.value,
                tenantId = tenantId,
            ),
        )

        assertThat(repository.findById(user.id)?.status?.canLogin()).isTrue()

        assertThatThrownBy {
            handler.handle(
                ActivateUserCommand(
                    userId = user.id.value,
                    tenantId = UUID.fromString("30000000-0000-0000-0000-000000000001"),
                ),
            )
        }.isInstanceOf(TenantScopeViolationException::class.java)
    }

    @Test
    fun `assign role adds normalized role`() {
        val user = seedUser()

        handler.handle(
            AssignRoleCommand(
                userId = user.id.value,
                tenantId = tenantId,
                role = RoleAssignment(
                    code = "auditor",
                    description = "Audit reviewer",
                    permissions = setOf(PermissionGrant("USER_ADMIN", setOf("INVITE"))),
                ),
            ),
        )

        val saved = repository.findById(user.id)
        assertThat(saved?.assignedRoles)
            .extracting<String> { it.normalizedCode }
            .contains("AUDITOR")
    }

    @Test
    fun `change password rotates hash and honors force flag`() {
        val user = seedUser()

        handler.handle(
            ChangePasswordCommand(
                userId = user.id.value,
                tenantId = tenantId,
                newPasswordHash = "hashed-2",
                historySize = 2,
                newTtl = Duration.ofDays(30),
                forceChangeOnNextLogin = true,
            ),
        )

        val saved = repository.findById(user.id)!!
        assertThat(saved.credentialsSnapshot.passwordVersion).isEqualTo(2)
        assertThat(saved.credentialsSnapshot.mustChangePassword).isTrue()
    }

    @Test
    fun `lock user changes status to locked`() {
        val user = seedUser()

        val saved = handler.handle(
            LockUserCommand(
                userId = user.id.value,
                tenantId = tenantId,
                reason = "manual lock",
            ),
        )

        assertThat(saved.status.canLogin()).isFalse()
    }

    @Test
    fun `enable mfa stores configuration and emits event`() {
        val user = seedUser()

        val saved = handler.handle(
            EnableMfaCommand(
                userId = user.id.value,
                tenantId = tenantId,
                methods = setOf(MfaMethod.TOTP),
                sharedSecret = "base32-secret",
                backupCodes = setOf("A1", "B2"),
            ),
        )

        assertThat(saved.isMfaEnabled).isTrue()
        assertThat(publisher.publishedEvents).isNotEmpty
    }

    @Test
    fun `link external identity appends provider identity`() {
        val user = seedUser()

        val saved = handler.handle(
            LinkExternalIdentityCommand(
                userId = user.id.value,
                tenantId = tenantId,
                provider = IdentityProvider.OIDC,
                subject = "oidc-subject-123",
                claims = mapOf("email_verified" to "true"),
            ),
        )

        assertThat(saved.linkedIdentities).hasSize(1)
        assertThat(saved.linkedIdentities.first().provider).isEqualTo(IdentityProvider.OIDC)
        assertThat(saved.linkedIdentities.first().subject).isEqualTo("oidc-subject-123")
    }

    private fun seedUser(): User = handler.handle(
        CreateUserCommand(
            tenantId = tenantId,
            firstName = "Seed",
            lastName = "User",
            email = "seed@example.com",
            passwordHash = "hashed-1",
        ),
    )

    private class InMemoryUserRepository : UserRepository {
        private val users = mutableMapOf<UserId, User>()

        override fun save(user: User): User {
            users[user.id] = user
            return user
        }

        override fun findById(id: UserId): User? = users[id]

        override fun findByEmail(tenantId: TenantId, email: String): User? =
            users.values.firstOrNull { it.tenantId == tenantId && it.profile.normalizedEmail == email }

        override fun findByExternalIdentity(
            tenantId: TenantId,
            provider: com.chiroerp.identity.core.domain.model.IdentityProvider,
            subject: String,
        ): User? = users.values.firstOrNull { user ->
            user.tenantId == tenantId && user.linkedIdentities.any {
                it.provider == provider && it.subject == subject
            }
        }
    }

    private class CapturingUserEventPublisher : UserEventPublisher {
        val publishedEvents = mutableListOf<UserDomainEvent>()

        override fun publish(events: Collection<UserDomainEvent>) {
            publishedEvents += events
        }
    }
}
