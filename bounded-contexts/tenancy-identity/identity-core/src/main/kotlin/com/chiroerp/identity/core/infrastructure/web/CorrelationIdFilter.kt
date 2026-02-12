package com.chiroerp.identity.core.infrastructure.web

import org.jboss.logging.MDC
import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import java.util.UUID

@Provider
@Priority(Priorities.AUTHENTICATION)
class CorrelationIdFilter(
    private val traceContext: RequestTraceContext,
) : ContainerRequestFilter, ContainerResponseFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val correlationId = normalizeCorrelationId(
            requestContext.getHeaderString(CORRELATION_ID_HEADER),
        ) ?: UUID.randomUUID().toString()

        traceContext.correlationId = correlationId
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
    }

    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext,
    ) {
        val correlationId = traceContext.correlationId ?: UUID.randomUUID().toString().also {
            traceContext.correlationId = it
        }
        responseContext.headers.putSingle(CORRELATION_ID_HEADER, correlationId)

        if (responseContext.status >= 400) {
            val errorId = traceContext.errorId ?: UUID.randomUUID().toString().also {
                traceContext.errorId = it
            }
            responseContext.headers.putSingle(ERROR_ID_HEADER, errorId)
            MDC.put(ERROR_ID_MDC_KEY, errorId)
        }

        MDC.remove(CORRELATION_ID_MDC_KEY)
        MDC.remove(ERROR_ID_MDC_KEY)
    }

    private fun normalizeCorrelationId(rawValue: String?): String? {
        val candidate = rawValue?.trim().orEmpty()
        if (candidate.isEmpty() || candidate.length > MAX_HEADER_LENGTH) {
            return null
        }

        val valid = candidate.all { char ->
            char.isLetterOrDigit() || char == '-' || char == '_' || char == '.' || char == ':'
        }

        return candidate.takeIf { valid }
    }

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
        const val ERROR_ID_HEADER = "X-Error-ID"

        private const val MAX_HEADER_LENGTH = 128
        private const val CORRELATION_ID_MDC_KEY = "correlationId"
        private const val ERROR_ID_MDC_KEY = "errorId"
    }
}
