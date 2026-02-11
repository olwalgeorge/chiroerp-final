package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.service.RefreshSessionRequest
import com.chiroerp.identity.core.application.service.RevokeAllSessionsRequest
import com.chiroerp.identity.core.application.service.RevokeSessionRequest
import com.chiroerp.identity.core.application.service.SessionHeartbeatRequest
import com.chiroerp.identity.core.application.service.SessionService
import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class SessionServiceTest {
    private val tenantA = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private val tenantB = UUID.fromString("30000000-0000-0000-0000-000000000001")
    private val userId = UUID.fromString("10000000-0000-0000-0000-000000000001")

    private lateinit var repository: InMemorySessionRepository
    private lateinit var service: SessionService

    @BeforeEach
    fun setup() {
        repository = InMemorySessionRepository()
        service = SessionService(repository)
    }

    @Test
    fun `refresh extends expiration for active session`() {
        val session = seedActiveSession()

        val refreshed = service.refresh(
            request = RefreshSessionRequest(
                tenantId = tenantA,
                userId = userId,
                sessionId = session.id,
                ttl = Duration.ofHours(2),
            ),
            now = Instant.parse("2026-02-11T11:00:00Z"),
        )

        assertThat(refreshed).isNotNull
        assertThat(refreshed?.expiresAt).isEqualTo(Instant.parse("2026-02-11T13:00:00Z"))
    }

    @Test
    fun `heartbeat updates last seen for active session`() {
        val session = seedActiveSession()

        val touched = service.heartbeat(
            request = SessionHeartbeatRequest(
                tenantId = tenantA,
                userId = userId,
                sessionId = session.id,
            ),
            now = Instant.parse("2026-02-11T10:30:00Z"),
        )

        assertThat(touched).isNotNull
        assertThat(touched?.lastSeenAt).isEqualTo(Instant.parse("2026-02-11T10:30:00Z"))
    }

    @Test
    fun `refresh expires stale session and returns null`() {
        val stale = seedActiveSession(expiresAt = Instant.parse("2026-02-11T10:05:00Z"))

        val refreshed = service.refresh(
            request = RefreshSessionRequest(
                tenantId = tenantA,
                userId = userId,
                sessionId = stale.id,
                ttl = Duration.ofMinutes(30),
            ),
            now = Instant.parse("2026-02-11T10:06:00Z"),
        )

        assertThat(refreshed).isNull()
        assertThat(repository.findById(stale.id)?.status).isEqualTo(SessionStatus.EXPIRED)
    }

    @Test
    fun `revoke marks session revoked and is idempotent`() {
        val session = seedActiveSession()

        val first = service.revoke(
            request = RevokeSessionRequest(
                tenantId = tenantA,
                userId = userId,
                sessionId = session.id,
                reason = "manual",
            ),
            now = Instant.parse("2026-02-11T10:15:00Z"),
        )
        val second = service.revoke(
            request = RevokeSessionRequest(
                tenantId = tenantA,
                userId = userId,
                sessionId = session.id,
                reason = "manual",
            ),
            now = Instant.parse("2026-02-11T10:16:00Z"),
        )

        assertThat(first).isTrue()
        assertThat(second).isFalse()
        assertThat(repository.findById(session.id)?.status).isEqualTo(SessionStatus.REVOKED)
    }

    @Test
    fun `revoke all returns count for tenant scoped sessions`() {
        seedActiveSession(id = UUID.randomUUID())
        seedActiveSession(id = UUID.randomUUID())

        val count = service.revokeAll(
            request = RevokeAllSessionsRequest(
                tenantId = tenantA,
                userId = userId,
                reason = "security",
            ),
            now = Instant.parse("2026-02-11T10:20:00Z"),
        )

        assertThat(count).isEqualTo(2)
        assertThat(repository.findActiveSessions(UserId(userId))).isEmpty()
    }

    @Test
    fun `tenant mismatch raises scope violation`() {
        val session = seedActiveSession()

        assertThatThrownBy {
            service.heartbeat(
                request = SessionHeartbeatRequest(
                    tenantId = tenantB,
                    userId = userId,
                    sessionId = session.id,
                ),
            )
        }.isInstanceOf(TenantScopeViolationException::class.java)
    }

    private fun seedActiveSession(
        id: UUID = UUID.randomUUID(),
        expiresAt: Instant = Instant.parse("2026-02-11T12:00:00Z"),
    ): Session {
        val session = Session(
            id = id,
            userId = UserId(userId),
            tenantId = TenantId(tenantA),
            issuedAt = Instant.parse("2026-02-11T10:00:00Z"),
            expiresAt = expiresAt,
            ipAddress = "10.0.0.1",
            userAgent = "test-agent",
            mfaVerified = false,
            status = SessionStatus.ACTIVE,
        )
        repository.save(session)
        return session
    }

    private class InMemorySessionRepository : SessionRepository {
        private val sessions = linkedMapOf<UUID, Session>()

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
}
