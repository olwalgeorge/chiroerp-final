package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.exception.InvalidPasswordException
import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserAlreadyExistsException
import com.chiroerp.identity.core.application.exception.UserLifecycleException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

@ApplicationScoped
class ErrorSanitizer {
    fun httpStatusFor(exception: Throwable): Int = when (exception) {
        is WebApplicationException -> exception.response?.status ?: Response.Status.INTERNAL_SERVER_ERROR.statusCode
        is ConstraintViolationException -> Response.Status.BAD_REQUEST.statusCode
        is IllegalArgumentException -> Response.Status.BAD_REQUEST.statusCode
        is InvalidPasswordException -> Response.Status.BAD_REQUEST.statusCode
        is TenantScopeViolationException -> Response.Status.FORBIDDEN.statusCode
        is UserNotFoundException -> Response.Status.NOT_FOUND.statusCode
        is UserAlreadyExistsException -> Response.Status.CONFLICT.statusCode
        is UserLifecycleException -> Response.Status.CONFLICT.statusCode
        else -> Response.Status.INTERNAL_SERVER_ERROR.statusCode
    }

    fun sanitize(status: Int): SanitizedError = when (status) {
        Response.Status.BAD_REQUEST.statusCode -> SanitizedError(
            code = "BAD_REQUEST",
            message = "Request could not be processed",
        )

        Response.Status.UNAUTHORIZED.statusCode -> SanitizedError(
            code = "UNAUTHORIZED",
            message = "Authentication failed",
        )

        Response.Status.FORBIDDEN.statusCode -> SanitizedError(
            code = "FORBIDDEN",
            message = "Access denied",
        )

        Response.Status.NOT_FOUND.statusCode -> SanitizedError(
            code = "NOT_FOUND",
            message = "Resource not found",
        )

        Response.Status.CONFLICT.statusCode -> SanitizedError(
            code = "CONFLICT",
            message = "Request conflicts with current state",
        )

        in 500..599 -> SanitizedError(
            code = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
        )

        else -> SanitizedError(
            code = "REQUEST_FAILED",
            message = "Request failed",
        )
    }
}

data class SanitizedError(
    val code: String,
    val message: String,
)
