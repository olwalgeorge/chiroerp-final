package com.chiroerp.identity.core.domain.model

import com.chiroerp.tenancy.shared.TenantId
import java.time.Duration
import java.time.Instant
import java.util.UUID

enum class SessionStatus {
    ACTIVE,
    EXPIRED,
    REVOKED,
}

data class Session(
    val id: UUID,
    val userId: UserId,
    val tenantId: TenantId,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val ipAddress: String?,
    val userAgent: String?,
    val mfaVerified: Boolean,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val lastSeenAt: Instant? = null,
    val revokedAt: Instant? = null,
    val revocationReason: String? = null,
) {
    init {
        require(expiresAt.isAfter(issuedAt)) { "Session expiration must be after issuance" }
    }

    fun refresh(ttl: Duration, now: Instant = Instant.now()): Session {
        check(status == SessionStatus.ACTIVE) { "Only active sessions can be refreshed" }
        return copy(expiresAt = now.plus(ttl), lastSeenAt = now)
    }

    fun heartbeat(now: Instant = Instant.now()): Session =
        if (status == SessionStatus.ACTIVE) copy(lastSeenAt = now) else this

    fun markLogout(reason: String?, at: Instant = Instant.now()): Session =
        copy(status = SessionStatus.REVOKED, revokedAt = at, revocationReason = reason)

    fun expire(at: Instant = Instant.now()): Session = copy(status = SessionStatus.EXPIRED, revokedAt = at)
}
