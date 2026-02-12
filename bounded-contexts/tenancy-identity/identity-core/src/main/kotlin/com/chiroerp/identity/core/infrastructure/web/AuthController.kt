package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.service.AuthenticationService
import com.chiroerp.identity.core.application.service.LoginAttempt
import com.chiroerp.identity.core.application.service.LoginResult
import com.chiroerp.identity.core.application.service.LogoutRequest
import com.chiroerp.identity.core.application.service.TokenService
import com.chiroerp.identity.core.application.service.TokenSubject
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/identity/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AuthController(
    private val authenticationService: AuthenticationService,
    private val tokenService: TokenService,
) {
    @POST
    @Path("/login")
    fun login(
        @Valid request: LoginRequest,
        @HeaderParam("X-Forwarded-For") forwardedFor: String?,
        @HeaderParam("User-Agent") userAgentHeader: String?,
    ): Response {
        val loginResult = authenticationService.login(
            LoginAttempt(
                tenantId = request.tenantId,
                email = request.email,
                password = request.password,
                ipAddress = parseClientIp(forwardedFor),
                userAgent = userAgentHeader,
                mfaVerified = !request.mfaCode.isNullOrBlank(),
            ),
        )

        return when (loginResult) {
            is LoginResult.Success -> {
                val tokenPair = tokenService.issueTokenPair(
                    TokenSubject(
                        userId = loginResult.userId,
                        tenantId = loginResult.tenantId,
                        sessionId = loginResult.sessionId,
                    ),
                )

                Response.ok(
                    LoginResponse(
                        accessToken = tokenPair.access.token,
                        refreshToken = tokenPair.refresh.token,
                        accessTokenExpiresAt = tokenPair.access.expiresAt,
                        refreshTokenExpiresAt = tokenPair.refresh.expiresAt,
                        sessionId = loginResult.sessionId,
                        userId = loginResult.userId,
                        tenantId = loginResult.tenantId,
                        mustChangePassword = loginResult.mustChangePassword,
                    ),
                ).build()
            }

            is LoginResult.Failure -> Response.status(Response.Status.UNAUTHORIZED)
                .entity(AuthErrorResponse(code = loginResult.code, message = "Invalid credentials"))
                .build()
        }
    }

    @POST
    @Path("/logout")
    fun logout(@Valid request: LogoutRequestBody): Response {
        val success = authenticationService.logout(
            LogoutRequest(
                tenantId = request.tenantId,
                userId = request.userId,
                sessionId = request.sessionId,
                reason = request.reason,
            ),
        )

        return if (success) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(AuthErrorResponse(code = "SESSION_NOT_FOUND", message = "Session not found or already revoked"))
                .build()
        }
    }

    @POST
    @Path("/token/verify")
    fun verify(@Valid request: VerifyTokenRequest): Response {
        val principal = tokenService.parse(
            token = request.token,
            expectedType = request.expectedType,
        ) ?: return Response.status(Response.Status.UNAUTHORIZED)
            .entity(AuthErrorResponse(code = "INVALID_TOKEN", message = "Token is invalid or expired"))
            .build()

        return Response.ok(TokenVerificationResponse.from(principal)).build()
    }

    private fun parseClientIp(forwardedFor: String?): String? {
        val headerValue = forwardedFor?.trim().orEmpty()
        if (headerValue.isEmpty()) {
            return null
        }

        return headerValue.split(',')
            .firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
