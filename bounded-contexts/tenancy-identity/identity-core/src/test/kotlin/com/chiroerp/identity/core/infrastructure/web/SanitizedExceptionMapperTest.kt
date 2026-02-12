package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.exception.UserAlreadyExistsException
import com.chiroerp.tenancy.shared.TenantId
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import java.util.UUID

class SanitizedExceptionMapperTest {
    private val traceContext = RequestTraceContext()
    private val mapper = SanitizedExceptionMapper(traceContext, ErrorSanitizer())

    @Test
    fun `sanitizes web application exceptions and returns trace headers`() {
        traceContext.correlationId = "corr-locked-id"

        val response = mapper.toResponse(
            WebApplicationException("tenant 42 details should never leak", Response.Status.FORBIDDEN),
        )

        val entity = response.entity as SanitizedErrorResponse

        assertThat(response.status).isEqualTo(Response.Status.FORBIDDEN.statusCode)
        assertThat(entity.code).isEqualTo("FORBIDDEN")
        assertThat(entity.message).isEqualTo("Access denied")
        assertThat(entity.correlationId).isEqualTo("corr-locked-id")
        assertThat(response.getHeaderString(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-locked-id")
        assertThat(response.getHeaderString(CorrelationIdFilter.ERROR_ID_HEADER)).isEqualTo(entity.errorId)
    }

    @Test
    fun `maps domain conflicts without leaking sensitive details`() {
        traceContext.correlationId = "corr-domain-conflict"
        val sensitiveEmail = "ceo@tenant.example"

        val response = mapper.toResponse(
            UserAlreadyExistsException(
                tenantId = TenantId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                email = sensitiveEmail,
            ),
        )

        val entity = response.entity as SanitizedErrorResponse

        assertThat(response.status).isEqualTo(Response.Status.CONFLICT.statusCode)
        assertThat(entity.code).isEqualTo("CONFLICT")
        assertThat(entity.message).isEqualTo("Request conflicts with current state")
        assertThat(entity.message).doesNotContain(sensitiveEmail)
    }

    @Test
    fun `falls back to internal error for unknown exceptions`() {
        val response = mapper.toResponse(IllegalStateException("db secret: super-sensitive"))
        val entity = response.entity as SanitizedErrorResponse

        assertThat(response.status).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
        assertThat(entity.code).isEqualTo("INTERNAL_ERROR")
        assertThat(entity.message).isEqualTo("An unexpected error occurred")
        assertThatCode { UUID.fromString(entity.errorId) }.doesNotThrowAnyException()
    }
}
