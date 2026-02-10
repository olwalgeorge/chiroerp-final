package com.chiroerp.identity.core.domain

import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class SessionTest {
    private val tenantId = TenantId(UUID.fromString("50000000-0000-0000-0000-000000000001"))
    private val userId = UserId(UUID.fromString("60000000-0000-0000-0000-000000000001"))

    @Test
    fun `refresh extends expiration and updates last seen`() {
        val issued = Instant.parse("2026-02-10T10:00:00Z")
        val session = session(issuedAt = issued, expiresAt = issued.plus(Duration.ofHours(2)))

        val refreshed = session.refresh(Duration.ofHours(1), now = Instant.parse("2026-02-10T11:00:00Z"))

        assertThat(refreshed.expiresAt).isEqualTo(Instant.parse("2026-02-10T12:00:00Z"))
        assertThat(refreshed.lastSeenAt).isEqualTo(Instant.parse("2026-02-10T11:00:00Z"))
    }

    @Test
    fun `refresh fails for non-active sessions`() {
        val revoked = session(status = SessionStatus.REVOKED)

        assertThatThrownBy {
            revoked.refresh(Duration.ofHours(1))
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `logout marks session revoked with reason`() {
        val session = session()

        val revoked = session.markLogout(reason = "manual", at = Instant.parse("2026-02-10T11:30:00Z"))

        assertThat(revoked.status).isEqualTo(SessionStatus.REVOKED)
        assertThat(revoked.revokedAt).isEqualTo(Instant.parse("2026-02-10T11:30:00Z"))
        assertThat(revoked.revocationReason).isEqualTo("manual")
    }

    @Test
    fun `heartbeat updates only active session`() {
        val session = session()
        val heartbeat = session.heartbeat(Instant.parse("2026-02-10T10:05:00Z"))
        assertThat(heartbeat.lastSeenAt).isEqualTo(Instant.parse("2026-02-10T10:05:00Z"))

        val revoked = session.copy(status = SessionStatus.REVOKED)
        val heartbeatRevoked = revoked.heartbeat(Instant.parse("2026-02-10T10:10:00Z"))
        assertThat(heartbeatRevoked.lastSeenAt).isNull()
    }

    private fun session(
        issuedAt: Instant = Instant.parse("2026-02-10T10:00:00Z"),
        expiresAt: Instant = issuedAt.plus(Duration.ofHours(2)),
        status: SessionStatus = SessionStatus.ACTIVE,
    ): Session = Session(
        id = UUID.fromString("70000000-0000-0000-0000-000000000001"),
        userId = userId,
        tenantId = tenantId,
        issuedAt = issuedAt,
        expiresAt = expiresAt,
        ipAddress = "127.0.0.1",
        userAgent = "JUnit",
        mfaVerified = true,
        status = status,
    )
}
