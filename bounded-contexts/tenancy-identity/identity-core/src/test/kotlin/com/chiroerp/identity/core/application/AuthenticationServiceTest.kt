package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.service.AuthenticationService
import com.chiroerp.identity.core.application.service.LoginAttempt
import com.chiroerp.identity.core.application.service.LoginResult
import com.chiroerp.identity.core.application.service.LogoutRequest
import com.chiroerp.identity.core.application.service.PasswordPolicy
import com.chiroerp.identity.core.application.service.PasswordService
import com.chiroerp.identity.core.domain.event.UserDomainEvent
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.identity.core.domain.port.UserEventPublisher
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.identity.core.infrastructure.security.PasswordEncoder
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

class AuthenticationServiceTest {
    private val tenantId = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private val otherTenantId = UUID.fromString("30000000-0000-0000-0000-000000000001")

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var sessionRepository: InMemorySessionRepository
    private lateinit var eventPublisher: CapturingEventPublisher
    private lateinit var passwordService: PasswordService
    private lateinit var service: AuthenticationService

    @BeforeEach
    fun setup() {
        userRepository = InMemoryUserRepository()
        sessionRepository = InMemorySessionRepository()
        eventPublisher = CapturingEventPublisher()

        val policy = PasswordPolicy(
            minLength = 8,
            maxLength = 72,
            requireUppercase = true,
            requireLowercase = true,
            requireDigit = true,
            requireSymbol = true,
            forbidWhitespace = true,
            denylist = emptySet(),
            bcryptCost = 4,
        )
        val encoder = PasswordEncoder(policy)
        passwordService = PasswordService(encoder, policy)

        service = AuthenticationService(
            userRepository = userRepository,
            sessionRepository = sessionRepository,
            passwordService = passwordService,
            userEventPublisher = eventPublisher,
            sessionTtlSeconds = 60 * 60,
            minimumResponseMs = 25,
        )
    }

    @Test
    fun `login succeeds and creates active session`() {
        val rawPassword = "ValidPass1!"
        val user = seedUser(rawPassword)

        val result = service.login(
            LoginAttempt(
                tenantId = tenantId,
                email = "jane.doe@example.com",
                password = rawPassword,
                ipAddress = "10.10.10.1",
                userAgent = "test-agent",
                mfaVerified = true,
            ),
        )

        assertThat(result).isInstanceOf(LoginResult.Success::class.java)
        val success = result as LoginResult.Success
        assertThat(success.userId).isEqualTo(user.id.value)
        assertThat(success.tenantId).isEqualTo(tenantId)

        val session = sessionRepository.findById(success.sessionId)
        assertThat(session).isNotNull
        assertThat(session?.status).isEqualTo(SessionStatus.ACTIVE)
        assertThat(eventPublisher.events).isNotEmpty
    }

    @Test
    fun `login failure is generic and enforces minimum response time`() {
        val startedAtNanos = System.nanoTime()

        val result = service.login(
            LoginAttempt(
                tenantId = tenantId,
                email = "missing.user@example.com",
                password = "irrelevant",
            ),
        )

        val elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000

        assertThat(result).isEqualTo(LoginResult.Failure())
        assertThat(elapsedMs).isGreaterThanOrEqualTo(20)
        assertThat(sessionRepository.sessions).isEmpty()
    }

    @Test
    fun `login fails for wrong password without revealing account existence`() {
        seedUser("ValidPass1!")

        val result = service.login(
            LoginAttempt(
                tenantId = tenantId,
                email = "jane.doe@example.com",
                password = "WrongPass1!",
            ),
        )

        assertThat(result).isEqualTo(LoginResult.Failure())
        assertThat(sessionRepository.sessions).isEmpty()
    }

    @Test
    fun `logout revokes session and records user logout event`() {
        val user = seedUser("ValidPass1!")
        val sessionId = loginAndExtractSessionId("ValidPass1!")

        val loggedOut = service.logout(
            LogoutRequest(
                tenantId = tenantId,
                userId = user.id.value,
                sessionId = sessionId,
                reason = "manual",
            ),
        )

        assertThat(loggedOut).isTrue()
        assertThat(sessionRepository.findById(sessionId)?.status).isEqualTo(SessionStatus.REVOKED)
        assertThat(eventPublisher.events).isNotEmpty
    }

    @Test
    fun `logout rejects cross tenant access`() {
        val user = seedUser("ValidPass1!")
        val sessionId = loginAndExtractSessionId("ValidPass1!")

        assertThatThrownBy {
            service.logout(
                LogoutRequest(
                    tenantId = otherTenantId,
                    userId = user.id.value,
                    sessionId = sessionId,
                ),
            )
        }.isInstanceOf(TenantScopeViolationException::class.java)
    }

    private fun loginAndExtractSessionId(rawPassword: String): UUID {
        val result = service.login(
            LoginAttempt(
                tenantId = tenantId,
                email = "jane.doe@example.com",
                password = rawPassword,
            ),
        )
        return (result as LoginResult.Success).sessionId
    }

    private fun seedUser(rawPassword: String): User {
        val now = Instant.parse("2026-02-11T10:00:00Z")
        val user = User.register(
            tenantId = TenantId(tenantId),
            profile = UserProfile(
                firstName = "Jane",
                lastName = "Doe",
                email = "jane.doe@example.com",
                phoneNumber = null,
                locale = Locale.US,
                timeZone = ZoneId.of("UTC"),
            ),
            passwordHash = passwordService.hash(rawPassword),
            now = now,
        )

        user.activate(now.plusSeconds(60))
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
    }

    private class InMemorySessionRepository : SessionRepository {
        val sessions = linkedMapOf<UUID, Session>()

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

        override fun findTenantSessions(tenantId: TenantId, limit: Int): List<Session> =
            sessions.values.filter { it.tenantId == tenantId }.take(limit)
    }

    private class CapturingEventPublisher : UserEventPublisher {
        val events = mutableListOf<UserDomainEvent>()

        override fun publish(events: Collection<UserDomainEvent>) {
            this.events += events
        }
    }
}
