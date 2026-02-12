package com.chiroerp.identity.core.infrastructure.web

import io.mockk.every
import io.mockk.mockk
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.core.MultivaluedHashMap
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import java.util.UUID

class CorrelationIdFilterTest {
    private val traceContext = RequestTraceContext()
    private val filter = CorrelationIdFilter(traceContext)

    @Test
    fun `propagates inbound correlation id to response header`() {
        val requestContext = mockk<ContainerRequestContext>()
        val responseContext = mockk<ContainerResponseContext>()
        val headers = MultivaluedHashMap<String, Any>()

        every { requestContext.getHeaderString(CorrelationIdFilter.CORRELATION_ID_HEADER) } returns "corr-123"
        every { responseContext.headers } returns headers
        every { responseContext.status } returns 200

        filter.filter(requestContext)
        filter.filter(requestContext, responseContext)

        assertThat(traceContext.correlationId).isEqualTo("corr-123")
        assertThat(headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-123")
        assertThat(headers.containsKey(CorrelationIdFilter.ERROR_ID_HEADER)).isFalse()
    }

    @Test
    fun `generates correlation and error ids when request fails`() {
        val requestContext = mockk<ContainerRequestContext>()
        val responseContext = mockk<ContainerResponseContext>()
        val headers = MultivaluedHashMap<String, Any>()

        every { requestContext.getHeaderString(CorrelationIdFilter.CORRELATION_ID_HEADER) } returns "invalid\nheader"
        every { responseContext.headers } returns headers
        every { responseContext.status } returns 401

        filter.filter(requestContext)
        filter.filter(requestContext, responseContext)

        val correlationId = headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER).toString()
        val errorId = headers.getFirst(CorrelationIdFilter.ERROR_ID_HEADER).toString()

        assertThatCode { UUID.fromString(correlationId) }.doesNotThrowAnyException()
        assertThatCode { UUID.fromString(errorId) }.doesNotThrowAnyException()
        assertThat(traceContext.errorId).isEqualTo(errorId)
    }
}
