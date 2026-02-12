package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.infrastructure.security.JwtTokenProvider
import com.chiroerp.identity.core.infrastructure.security.JwtTokenRequest
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration
import java.time.Instant
import java.util.UUID

enum class IdentityTokenType {
    ACCESS,
    REFRESH,
}

data class TokenSubject(
    val userId: UUID,
    val tenantId: UUID,
    val sessionId: UUID? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
)

data class IssuedToken(
    val token: String,
    val expiresAt: Instant,
)

data class TokenPair(
    val access: IssuedToken,
    val refresh: IssuedToken,
)

data class TokenPrincipal(
    val userId: UUID,
    val tenantId: UUID,
    val sessionId: UUID?,
    val tokenType: IdentityTokenType,
    val roles: Set<String>,
    val permissions: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant,
)

@ApplicationScoped
class TokenService(
    private val jwtTokenProvider: JwtTokenProvider,
    @param:ConfigProperty(name = "chiroerp.identity.jwt.access-ttl-seconds", defaultValue = "900")
    private val accessTtlSeconds: Long,
    @param:ConfigProperty(name = "chiroerp.identity.jwt.refresh-ttl-seconds", defaultValue = "2592000")
    private val refreshTtlSeconds: Long,
) {
    fun issueTokenPair(subject: TokenSubject, now: Instant = Instant.now()): TokenPair = TokenPair(
        access = issueToken(subject, IdentityTokenType.ACCESS, Duration.ofSeconds(accessTtlSeconds), now),
        refresh = issueToken(subject, IdentityTokenType.REFRESH, Duration.ofSeconds(refreshTtlSeconds), now),
    )

    fun issueAccessToken(subject: TokenSubject, now: Instant = Instant.now()): IssuedToken =
        issueToken(subject, IdentityTokenType.ACCESS, Duration.ofSeconds(accessTtlSeconds), now)

    fun issueRefreshToken(subject: TokenSubject, now: Instant = Instant.now()): IssuedToken =
        issueToken(subject, IdentityTokenType.REFRESH, Duration.ofSeconds(refreshTtlSeconds), now)

    fun parse(token: String, expectedType: IdentityTokenType? = null, now: Instant = Instant.now()): TokenPrincipal? {
        val claims = jwtTokenProvider.parseAndValidate(token, now) ?: return null

        val tokenType = runCatching { IdentityTokenType.valueOf(claims.tokenType.uppercase()) }.getOrNull() ?: return null
        if (expectedType != null && tokenType != expectedType) {
            return null
        }

        return TokenPrincipal(
            userId = claims.subject,
            tenantId = claims.tenantId,
            sessionId = claims.sessionId,
            tokenType = tokenType,
            roles = claims.roles,
            permissions = claims.permissions,
            issuedAt = claims.issuedAt,
            expiresAt = claims.expiresAt,
        )
    }

    private fun issueToken(
        subject: TokenSubject,
        tokenType: IdentityTokenType,
        ttl: Duration,
        now: Instant,
    ): IssuedToken {
        val issued = jwtTokenProvider.issue(
            JwtTokenRequest(
                subject = subject.userId,
                tenantId = subject.tenantId,
                tokenType = tokenType.name,
                sessionId = subject.sessionId,
                roles = subject.roles,
                permissions = subject.permissions,
                ttl = ttl,
            ),
            now = now,
        )

        return IssuedToken(
            token = issued.token,
            expiresAt = issued.claims.expiresAt,
        )
    }
}
