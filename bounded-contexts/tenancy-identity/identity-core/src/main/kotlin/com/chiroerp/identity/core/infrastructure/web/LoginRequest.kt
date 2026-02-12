package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.service.IdentityTokenType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class LoginRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
    val mfaCode: String? = null,
)

data class LogoutRequestBody(
    @field:NotNull
    val tenantId: UUID,
    @field:NotNull
    val userId: UUID,
    @field:NotNull
    val sessionId: UUID,
    val reason: String? = null,
)

data class VerifyTokenRequest(
    @field:NotBlank
    val token: String,
    val expectedType: IdentityTokenType? = null,
)
