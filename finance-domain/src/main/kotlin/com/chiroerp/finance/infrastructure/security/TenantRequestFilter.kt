package com.chiroerp.finance.infrastructure.security

import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.ext.Provider
import java.util.UUID

/**
 * Extracts tenant id from HTTP header and populates TenantContext.
 * Header: X-Tenant-Id (UUID)
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
class TenantRequestFilter : ContainerRequestFilter {

    @Inject
    lateinit var tenantContext: TenantContext

    override fun filter(requestContext: ContainerRequestContext) {
        val tenantHeader = requestContext.getHeaderString("X-Tenant-Id")
            ?: throw WebApplicationException(
                "X-Tenant-Id header is required",
                Response.Status.BAD_REQUEST
            )
        val tenantId = try {
            UUID.fromString(tenantHeader)
        } catch (ex: IllegalArgumentException) {
            throw WebApplicationException("Invalid tenant id format", Response.Status.BAD_REQUEST)
        }
        tenantContext.setTenantId(tenantId)
    }
}
