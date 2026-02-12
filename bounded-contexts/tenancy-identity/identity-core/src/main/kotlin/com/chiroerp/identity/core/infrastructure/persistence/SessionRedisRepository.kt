package com.chiroerp.identity.core.infrastructure.persistence

import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Default session repository implementation.
 *
 * The bounded context targets Redis for session storage, but this implementation
 * keeps a durable contract with in-memory fallback semantics so local/dev/test
 * environments can run without external Redis wiring.
 */
@DefaultBean
@ApplicationScoped
class SessionRedisRepository : SessionRepository {
    private val sessions = ConcurrentHashMap<UUID, Session>()

    override fun save(session: Session) {
        sessions[session.id] = session
    }

    override fun findById(sessionId: UUID): Session? {
        val session = sessions[sessionId] ?: return null
        val normalized = normalizeSession(session)
        if (normalized != session) {
            sessions[sessionId] = normalized
        }
        return normalized
    }

    override fun findActiveSessions(userId: UserId): List<Session> = sessions.values
        .asSequence()
        .filter { it.userId == userId }
        .map { session ->
            val normalized = normalizeSession(session)
            if (normalized != session) {
                sessions[session.id] = normalized
            }
            normalized
        }
        .filter { it.status == SessionStatus.ACTIVE }
        .sortedByDescending { it.lastSeenAt ?: it.issuedAt }
        .toList()

    override fun revokeAll(userId: UserId, reason: String, revokedAt: Instant): Int {
        var revoked = 0

        sessions.replaceAll { _, current ->
            val normalized = normalizeSession(current, revokedAt)
            if (normalized.userId == userId && normalized.status == SessionStatus.ACTIVE) {
                revoked += 1
                normalized.markLogout(reason, revokedAt)
            } else {
                normalized
            }
        }

        return revoked
    }

    override fun findTenantSessions(tenantId: TenantId, limit: Int): List<Session> = sessions.values
        .asSequence()
        .filter { it.tenantId == tenantId }
        .map { session ->
            val normalized = normalizeSession(session)
            if (normalized != session) {
                sessions[session.id] = normalized
            }
            normalized
        }
        .sortedByDescending { it.lastSeenAt ?: it.issuedAt }
        .take(limit.coerceIn(1, 500))
        .toList()

    private fun normalizeSession(session: Session, now: Instant = Instant.now()): Session {
        if (session.status != SessionStatus.ACTIVE) {
            return session
        }
        return if (session.expiresAt.isBefore(now)) {
            session.expire(now)
        } else {
            session
        }
    }
}
