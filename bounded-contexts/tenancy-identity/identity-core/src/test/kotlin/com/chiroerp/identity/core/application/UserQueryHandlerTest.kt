package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import com.chiroerp.identity.core.application.handler.UserQueryHandler
import com.chiroerp.identity.core.application.query.GetActiveSessionsQuery
import com.chiroerp.identity.core.application.query.GetUserByEmailQuery
import com.chiroerp.identity.core.application.query.GetUserPermissionsQuery
import com.chiroerp.identity.core.application.query.GetUserQuery
import com.chiroerp.identity.core.application.query.ListUsersQuery
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.Permission
import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.domain.model.UserRole
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

class UserQueryHandlerTest {
    private val tenantA = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private val tenantB = UUID.fromString("30000000-0000-0000-0000-000000000001")

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var sessionRepository: InMemorySessionRepository
    private lateinit var handler: UserQueryHandler

    @BeforeEach
    fun setup() {
        userRepository = InMemoryUserRepository()
        sessionRepository = InMemorySessionRepository()
        handler = UserQueryHandler(userRepository, sessionRepository)
    }

    @Test
    fun `get user query returns user for tenant`() {
        val user = seedUser(tenantA, "jane.doe@example.com")

        val view = handler.handle(GetUserQuery(tenantId = tenantA, userId = user.id.value))

        assertThat(view.userId).isEqualTo(user.id.value)
        assertThat(view.tenantId).isEqualTo(tenantA)
        assertThat(view.email).isEqualTo("jane.doe@example.com")
        assertThat(view.roles).extracting<String> { it.code }.contains("FINANCE_REVIEWER")
    }

    @Test
    fun `get user query rejects cross tenant access`() {
        val user = seedUser(tenantA, "jane.doe@example.com")

        assertThatThrownBy {
            handler.handle(GetUserQuery(tenantId = tenantB, userId = user.id.value))
        }.isInstanceOf(TenantScopeViolationException::class.java)
    }

    @Test
    fun `get user by email query normalizes email`() {
        seedUser(tenantA, "jane.doe@example.com")

        val view = handler.handle(
            GetUserByEmailQuery(
                tenantId = tenantA,
                email = "  JANE.DOE@EXAMPLE.COM ",
            ),
        )

        assertThat(view).isNotNull
        assertThat(view?.email).isEqualTo("jane.doe@example.com")
    }

    @Test
    fun `get user permissions query aggregates direct and role permissions`() {
        val user = seedUser(tenantA, "jane.doe@example.com")

        val permissions = handler.handle(
            GetUserPermissionsQuery(
                tenantId = tenantA,
                userId = user.id.value,
            ),
        )

        assertThat(permissions.directPermissions)
            .extracting<String> { it.objectId }
            .contains("USER_ADMIN")
        assertThat(permissions.rolePermissions)
            .extracting<String> { it.objectId }
            .contains("FI_DOC")
        assertThat(permissions.effectivePermissions)
            .extracting<String> { it.objectId }
            .contains("USER_ADMIN", "FI_DOC")
    }

    @Test
    fun `get active sessions query filters to same tenant and active status`() {
        val user = seedUser(tenantA, "jane.doe@example.com")

        val activeSession = Session(
            id = UUID.randomUUID(),
            userId = user.id,
            tenantId = TenantId(tenantA),
            issuedAt = Instant.parse("2026-02-11T10:00:00Z"),
            expiresAt = Instant.parse("2026-02-11T12:00:00Z"),
            ipAddress = "10.0.0.1",
            userAgent = "test-agent",
            mfaVerified = true,
            status = SessionStatus.ACTIVE,
        )

        val revokedSession = activeSession
            .copy(id = UUID.randomUUID())
            .markLogout("manual")

        val crossTenantSession = activeSession.copy(
            id = UUID.randomUUID(),
            tenantId = TenantId(tenantB),
        )

        sessionRepository.save(activeSession)
        sessionRepository.save(revokedSession)
        sessionRepository.save(crossTenantSession)

        val sessions = handler.handle(
            GetActiveSessionsQuery(
                tenantId = tenantA,
                userId = user.id.value,
            ),
        )

        assertThat(sessions).hasSize(1)
        assertThat(sessions.single().sessionId).isEqualTo(activeSession.id)
    }

    @Test
    fun `list users query applies tenant status and pagination`() {
        seedUser(tenantA, "pending.one@example.com", status = UserStatus.PENDING)
        seedUser(tenantA, "active.two@example.com", status = UserStatus.ACTIVE)
        seedUser(tenantA, "pending.three@example.com", status = UserStatus.PENDING)
        seedUser(tenantB, "other.tenant@example.com", status = UserStatus.PENDING)

        val users = handler.handle(
            ListUsersQuery(
                tenantId = tenantA,
                status = UserStatus.PENDING,
                limit = 1,
                offset = 1,
            ),
        )

        assertThat(users).hasSize(1)
        assertThat(users.single().tenantId).isEqualTo(tenantA)
        assertThat(users.single().status).isEqualTo(UserStatus.PENDING)
    }

    @Test
    fun `list users query rejects invalid pagination arguments`() {
        assertThatThrownBy {
            handler.handle(ListUsersQuery(tenantId = tenantA, limit = 0))
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            handler.handle(ListUsersQuery(tenantId = tenantA, offset = -1))
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `get user query throws when user does not exist`() {
        assertThatThrownBy {
            handler.handle(GetUserQuery(tenantId = tenantA, userId = UUID.randomUUID()))
        }.isInstanceOf(UserNotFoundException::class.java)
    }

    private fun seedUser(
        tenantId: UUID,
        email: String,
        status: UserStatus = UserStatus.ACTIVE,
    ): User {
        val now = Instant.parse("2026-02-11T10:00:00Z")
        val user = User.register(
            tenantId = TenantId(tenantId),
            profile = UserProfile(
                firstName = "Jane",
                lastName = "Doe",
                email = email,
                phoneNumber = "+1-555-0101",
                locale = Locale.US,
                timeZone = ZoneId.of("UTC"),
            ),
            passwordHash = "hash-${UUID.randomUUID()}",
            initialRoles = setOf(
                UserRole(
                    code = "finance_reviewer",
                    description = "Finance reviewer",
                    permissions = setOf(Permission("FI_DOC", setOf("POST", "READ"))),
                    sodGroup = "FI",
                ),
            ),
            initialPermissions = setOf(Permission("USER_ADMIN", setOf("READ"))),
            now = now,
        )

        if (status == UserStatus.ACTIVE) {
            user.activate(now.plusSeconds(60))
        }

        user.pullDomainEvents()
        return userRepository.save(user)
    }

    private class InMemoryUserRepository : UserRepository {
        private val users = linkedMapOf<UserId, User>()

        override fun save(user: User): User {
            users[user.id] = user
            return user
        }

        override fun findById(id: UserId): User? = users[id]

        override fun findByEmail(tenantId: TenantId, email: String): User? {
            val normalized = email.trim().lowercase(Locale.ROOT)
            return users.values.firstOrNull { it.tenantId == tenantId && it.profile.normalizedEmail == normalized }
        }

        override fun findByExternalIdentity(
            tenantId: TenantId,
            provider: IdentityProvider,
            subject: String,
        ): User? = users.values.firstOrNull { user ->
            user.tenantId == tenantId && user.linkedIdentities.any {
                it.provider == provider && it.subject == subject
            }
        }

        override fun listByTenant(
            tenantId: TenantId,
            limit: Int,
            offset: Int,
            status: UserStatus?,
        ): List<User> = users.values
            .asSequence()
            .filter { it.tenantId == tenantId }
            .filter { status == null || it.status == status }
            .sortedWith(compareBy<User> { it.createdAt }.thenBy { it.id.value })
            .drop(offset)
            .take(limit)
            .toList()
    }

    private class InMemorySessionRepository : SessionRepository {
        private val sessions = mutableMapOf<UUID, Session>()

        override fun save(session: Session) {
            sessions[session.id] = session
        }

        override fun findById(sessionId: UUID): Session? = sessions[sessionId]

        override fun findActiveSessions(userId: UserId): List<Session> =
            sessions.values.filter { it.userId == userId && it.status == SessionStatus.ACTIVE }

        override fun revokeAll(userId: UserId, reason: String, revokedAt: Instant): Int {
            var count = 0
            sessions.replaceAll { _, session ->
                if (session.userId == userId && session.status == SessionStatus.ACTIVE) {
                    count += 1
                    session.markLogout(reason, revokedAt)
                } else {
                    session
                }
            }
            return count
        }

        override fun findTenantSessions(tenantId: TenantId, limit: Int): List<Session> = sessions.values
            .asSequence()
            .filter { it.tenantId == tenantId }
            .take(limit)
            .toList()
    }
}
