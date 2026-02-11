package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class SessionService(
    private val sessionRepository: SessionRepository,
) {
    fun refresh(request: RefreshSessionRequest, now: Instant = Instant.now()): Session? {
        val session = resolveSession(
            sessionId = request.sessionId,
            tenantId = TenantId(request.tenantId),
            userId = UserId(request.userId),
        )

        if (session.status != SessionStatus.ACTIVE) {
            return null
        }
        if (session.expiresAt.isBefore(now)) {
            val expired = session.expire(now)
            sessionRepository.save(expired)
            return null
        }

        val refreshed = session.refresh(request.ttl, now)
        sessionRepository.save(refreshed)
        return refreshed
    }

    fun heartbeat(request: SessionHeartbeatRequest, now: Instant = Instant.now()): Session? {
        val session = resolveSession(
            sessionId = request.sessionId,
            tenantId = TenantId(request.tenantId),
            userId = UserId(request.userId),
        )

        if (session.status != SessionStatus.ACTIVE) {
            return null
        }
        if (session.expiresAt.isBefore(now)) {
            val expired = session.expire(now)
            sessionRepository.save(expired)
            return null
        }

        val touched = session.heartbeat(now)
        sessionRepository.save(touched)
        return touched
    }

    fun revoke(request: RevokeSessionRequest, now: Instant = Instant.now()): Boolean {
        val session = resolveSession(
            sessionId = request.sessionId,
            tenantId = TenantId(request.tenantId),
            userId = UserId(request.userId),
        )

        if (session.status != SessionStatus.ACTIVE) {
            return false
        }

        sessionRepository.save(session.markLogout(request.reason, now))
        return true
    }

    fun revokeAll(request: RevokeAllSessionsRequest, now: Instant = Instant.now()): Int {
        val tenantId = TenantId(request.tenantId)
        val userId = UserId(request.userId)

        // Scope check before mass revoke to satisfy ADR-005 tenant isolation.
        val sessions = sessionRepository.findActiveSessions(userId)
        if (sessions.any { it.tenantId != tenantId }) {
            throw TenantScopeViolationException(tenantId)
        }

        return sessionRepository.revokeAll(
            userId = userId,
            reason = request.reason,
            revokedAt = now,
        )
    }

    fun listTenantSessions(tenantId: UUID, limit: Int = 100): List<Session> {
        require(limit in 1..500) { "Limit must be between 1 and 500" }
        return sessionRepository.findTenantSessions(TenantId(tenantId), limit)
    }

    private fun resolveSession(
        sessionId: UUID,
        tenantId: TenantId,
        userId: UserId,
    ): Session {
        val session = sessionRepository.findById(sessionId)
            ?: throw IllegalArgumentException("Session $sessionId not found")

        if (session.tenantId != tenantId || session.userId != userId) {
            throw TenantScopeViolationException(tenantId)
        }

        return session
    }
}

data class RefreshSessionRequest(
    val tenantId: UUID,
    val userId: UUID,
    val sessionId: UUID,
    val ttl: Duration,
)

data class SessionHeartbeatRequest(
    val tenantId: UUID,
    val userId: UUID,
    val sessionId: UUID,
)

data class RevokeSessionRequest(
    val tenantId: UUID,
    val userId: UUID,
    val sessionId: UUID,
    val reason: String? = null,
)

data class RevokeAllSessionsRequest(
    val tenantId: UUID,
    val userId: UUID,
    val reason: String,
)
