package com.chiroerp.identity.core.infrastructure.web

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger
import java.util.UUID

@Provider
@Priority(Priorities.USER)
class SanitizedExceptionMapper(
    private val traceContext: RequestTraceContext,
    private val errorSanitizer: ErrorSanitizer,
) : ExceptionMapper<Throwable> {
    private val logger: Logger = Logger.getLogger(SanitizedExceptionMapper::class.java)

    override fun toResponse(exception: Throwable): Response {
        val correlationId = traceContext.correlationId ?: UUID.randomUUID().toString().also {
            traceContext.correlationId = it
        }
        val errorId = UUID.randomUUID().toString()
        traceContext.errorId = errorId

        val status = errorSanitizer.httpStatusFor(exception)
        val sanitizedError = errorSanitizer.sanitize(status)

        if (status >= 500) {
            logger.errorf(
                "Identity request failed with server error (status=%d, errorId=%s, correlationId=%s, exception=%s)",
                status,
                errorId,
                correlationId,
                exception::class.java.simpleName,
            )
            logger.debug("Identity request failure details", exception)
        } else {
            logger.warnf(
                "Identity request failed (status=%d, errorId=%s, correlationId=%s, exception=%s)",
                status,
                errorId,
                correlationId,
                exception::class.java.simpleName,
            )
        }

        return Response.status(status)
            .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
            .header(CorrelationIdFilter.ERROR_ID_HEADER, errorId)
            .entity(
                SanitizedErrorResponse(
                    code = sanitizedError.code,
                    message = sanitizedError.message,
                    errorId = errorId,
                    correlationId = correlationId,
                ),
            )
            .build()
    }
}
