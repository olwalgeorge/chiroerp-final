package com.chiroerp.identity.core.infrastructure.outbox

import com.chiroerp.identity.core.domain.model.*
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.arc.properties.IfBuildProperty
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import java.time.Instant
import java.util.*

@ApplicationScoped
@IfBuildProperty(
    name = "chiroerp.identity.tests.in-memory-repositories.enabled",
    stringValue = "true",
)
class TestRepositoryConfiguration {

    @Produces
    @ApplicationScoped
    fun userRepository(): UserRepository = InMemoryUserRepository()

    @Produces
    @ApplicationScoped
    fun sessionRepository(): SessionRepository = InMemorySessionRepository()
}

class InMemoryUserRepository : UserRepository {
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

class InMemorySessionRepository : SessionRepository {
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
