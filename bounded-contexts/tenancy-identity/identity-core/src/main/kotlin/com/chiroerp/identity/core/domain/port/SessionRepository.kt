package com.chiroerp.identity.core.domain.port

import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

interface SessionRepository {
    fun save(session: Session)

    fun findById(sessionId: UUID): Session?

    fun findActiveSessions(userId: UserId): List<Session>

    fun revokeAll(userId: UserId, reason: String, revokedAt: Instant = Instant.now()): Int

    fun findTenantSessions(tenantId: TenantId, limit: Int = 100): List<Session>
}
