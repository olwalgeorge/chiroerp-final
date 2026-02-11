package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import com.chiroerp.identity.core.domain.event.UserDomainEvent
import com.chiroerp.identity.core.domain.model.Session
import com.chiroerp.identity.core.domain.model.SessionStatus
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.SessionRepository
import com.chiroerp.identity.core.domain.port.UserEventPublisher
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.UUID

private const val DEFAULT_AUTH_FAILURE_CODE = "AUTHENTICATION_FAILED"

@ApplicationScoped
class AuthenticationService(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val passwordService: PasswordService,
    private val userEventPublisher: UserEventPublisher,
    @param:ConfigProperty(name = "chiroerp.identity.auth.session-ttl-seconds", defaultValue = "28800")
    private val sessionTtlSeconds: Long,
    @param:ConfigProperty(name = "chiroerp.identity.auth.minimum-response-ms", defaultValue = "100")
    private val minimumResponseMs: Long,
) {
    /**
     * ADR-007 anti-enumeration guard: every login response takes at least
     * the configured minimum duration regardless of success/failure.
     */
    fun login(attempt: LoginAttempt): LoginResult {
        val startedAtNanos = System.nanoTime()

        return try {
            val tenantId = TenantId(attempt.tenantId)
            val normalizedEmail = attempt.email.trim().lowercase(Locale.ROOT)
            val user = userRepository.findByEmail(tenantId, normalizedEmail)

            val hashToVerify = user?.credentialsSnapshot?.hashedPassword ?: DUMMY_BCRYPT_HASH
            val passwordMatches = passwordService.verify(attempt.password, hashToVerify)

            if (user == null || !user.status.canLogin() || !passwordMatches) {
                LoginResult.Failure()
            } else {
                val now = Instant.now()
                val expiresAt = now.plusSeconds(sessionTtlSeconds)
                val session = Session(
                    id = UUID.randomUUID(),
                    userId = user.id,
                    tenantId = user.tenantId,
                    issuedAt = now,
                    expiresAt = expiresAt,
                    ipAddress = attempt.ipAddress,
                    userAgent = attempt.userAgent,
                    mfaVerified = attempt.mfaVerified,
                    status = SessionStatus.ACTIVE,
                    lastSeenAt = now,
                )

                sessionRepository.save(session)
                user.recordSuccessfulLogin(
                    sessionId = session.id,
                    ipAddress = attempt.ipAddress,
                    userAgent = attempt.userAgent,
                    mfaVerified = attempt.mfaVerified,
                    occurredAt = now,
                )
                userRepository.save(user)
                publish(user.pullDomainEvents())

                LoginResult.Success(
                    userId = user.id.value,
                    tenantId = user.tenantId.value,
                    sessionId = session.id,
                    expiresAt = expiresAt,
                    mustChangePassword = user.credentialsSnapshot.requiresRotation(now),
                )
            }
        } finally {
            enforceMinimumDuration(startedAtNanos)
        }
    }

    fun logout(request: LogoutRequest): Boolean {
        val tenantId = TenantId(request.tenantId)
        val userId = UserId(request.userId)

        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)

        if (user.tenantId != tenantId) {
            throw TenantScopeViolationException(tenantId)
        }

        val session = sessionRepository.findById(request.sessionId) ?: return false
        if (session.tenantId != tenantId || session.userId != user.id) {
            throw TenantScopeViolationException(tenantId)
        }
        if (session.status != SessionStatus.ACTIVE) {
            return false
        }

        val revokedAt = Instant.now()
        sessionRepository.save(session.markLogout(request.reason, revokedAt))

        user.recordLogout(
            sessionId = request.sessionId,
            reason = request.reason,
            occurredAt = revokedAt,
        )
        userRepository.save(user)
        publish(user.pullDomainEvents())

        return true
    }

    private fun enforceMinimumDuration(startedAtNanos: Long) {
        if (minimumResponseMs <= 0) {
            return
        }

        val elapsed = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis()
        val remaining = minimumResponseMs - elapsed
        if (remaining <= 0) {
            return
        }

        try {
            Thread.sleep(remaining)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun publish(events: List<UserDomainEvent>) {
        if (events.isNotEmpty()) {
            userEventPublisher.publish(events)
        }
    }

    companion object {
        // Constant fake hash used when user does not exist to equalize password verification path.
        private const val DUMMY_BCRYPT_HASH = "\$2a\$10\$7EqJtq98hPqEX7fNZaFWoOeRj6A9lNf4ch5G0YJYcqRR3YKHyCuXG"
    }
}

data class LoginAttempt(
    val tenantId: UUID,
    val email: String,
    val password: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val mfaVerified: Boolean = false,
)

sealed interface LoginResult {
    data class Success(
        val userId: UUID,
        val tenantId: UUID,
        val sessionId: UUID,
        val expiresAt: Instant,
        val mustChangePassword: Boolean,
    ) : LoginResult

    data class Failure(
        val code: String = DEFAULT_AUTH_FAILURE_CODE,
    ) : LoginResult
}

data class LogoutRequest(
    val tenantId: UUID,
    val userId: UUID,
    val sessionId: UUID,
    val reason: String? = null,
)
