package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.service.IdentityTokenType
import com.chiroerp.identity.core.application.service.TokenPrincipal
import java.time.Instant
import java.util.UUID

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val accessTokenExpiresAt: Instant,
    val refreshTokenExpiresAt: Instant,
    val sessionId: UUID,
    val userId: UUID,
    val tenantId: UUID,
    val mustChangePassword: Boolean,
)

data class AuthErrorResponse(
    val code: String,
    val message: String,
)

data class TokenVerificationResponse(
    val valid: Boolean,
    val userId: UUID,
    val tenantId: UUID,
    val sessionId: UUID?,
    val tokenType: IdentityTokenType,
    val roles: Set<String>,
    val permissions: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant,
) {
    companion object {
        fun from(principal: TokenPrincipal): TokenVerificationResponse = TokenVerificationResponse(
            valid = true,
            userId = principal.userId,
            tenantId = principal.tenantId,
            sessionId = principal.sessionId,
            tokenType = principal.tokenType,
            roles = principal.roles,
            permissions = principal.permissions,
            issuedAt = principal.issuedAt,
            expiresAt = principal.expiresAt,
        )
    }
}
