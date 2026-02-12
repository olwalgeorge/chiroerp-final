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
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/api/identity/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Login, logout, and token verification")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val tokenService: TokenService,
) {
    @POST
    @Path("/login")
    @SecurityRequirementsSet
    @Operation(operationId = "login", summary = "Authenticate a user and issue tokens")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Login successful"),
        APIResponse(responseCode = "401", description = "Invalid credentials"),
    )
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
    @SecurityRequirement(name = "jwt")
    @Operation(operationId = "logout", summary = "End a user session")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Logout successful"),
        APIResponse(responseCode = "404", description = "Session not found"),
    )
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
    @SecurityRequirementsSet
    @Operation(operationId = "verifyToken", summary = "Verify and introspect a token")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Token is valid"),
        APIResponse(responseCode = "401", description = "Token is invalid or expired"),
    )
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
