package com.chiroerp.tenancy.core.infrastructure.api

import com.chiroerp.tenancy.core.application.exception.TenantAlreadyExistsException
import com.chiroerp.tenancy.core.application.exception.TenantNotFoundException
import com.chiroerp.tenancy.core.application.handler.TenantCommandHandler
import com.chiroerp.tenancy.core.application.handler.TenantQueryHandler
import com.chiroerp.tenancy.core.application.query.GetTenantQuery
import com.chiroerp.tenancy.core.application.query.ListTenantsQuery
import com.chiroerp.tenancy.shared.TenantId
import io.micrometer.core.instrument.MeterRegistry
import jakarta.validation.Valid
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/tenants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TenantController(
    private val tenantCommandHandler: TenantCommandHandler,
    private val tenantQueryHandler: TenantQueryHandler,
    private val meterRegistry: MeterRegistry,
) {
    @POST
    fun createTenant(@Valid request: CreateTenantRequest): Response {
        return try {
            val tenant = tenantCommandHandler.handle(request.toCommand())
            meterRegistry.counter("chiroerp.tenancy.api.create.requests").increment()
            Response.status(Response.Status.CREATED)
                .entity(TenantResponse.from(tenant))
                .build()
        } catch (ex: TenantAlreadyExistsException) {
            throw WebApplicationException(ex.message, Response.Status.CONFLICT)
        }
    }

    @GET
    @Path("/{id}")
    fun getTenant(@PathParam("id") rawTenantId: String): TenantResponse {
        val tenantId = parseTenantId(rawTenantId)
        val tenant = tenantQueryHandler.handle(GetTenantQuery(tenantId))
            ?: throw NotFoundException("Tenant $rawTenantId not found")

        meterRegistry.counter("chiroerp.tenancy.api.get.requests").increment()
        return TenantResponse.from(tenant)
    }

    @GET
    fun listTenants(
        @DefaultValue("0") @QueryParam("offset") offset: Int,
        @DefaultValue("50") @QueryParam("limit") limit: Int,
    ): TenantListResponse {
        val normalizedOffset = offset.coerceAtLeast(0)
        val normalizedLimit = limit.coerceIn(1, 200)

        val tenants = tenantQueryHandler.handle(
            ListTenantsQuery(offset = normalizedOffset, limit = normalizedLimit),
        )

        meterRegistry.counter("chiroerp.tenancy.api.list.requests").increment()

        return TenantListResponse(
            items = tenants.map(TenantResponse::from),
            offset = normalizedOffset,
            limit = normalizedLimit,
            count = tenants.size,
        )
    }

    @PATCH
    @Path("/{id}/settings")
    fun updateTenantSettings(
        @PathParam("id") rawTenantId: String,
        @Valid request: UpdateTenantSettingsRequest,
    ): TenantResponse {
        val tenantId = parseTenantId(rawTenantId)

        return try {
            val tenant = tenantCommandHandler.handle(request.toCommand(tenantId))
            meterRegistry.counter("chiroerp.tenancy.api.patch.settings.requests").increment()
            TenantResponse.from(tenant)
        } catch (ex: TenantNotFoundException) {
            throw NotFoundException(ex.message)
        }
    }

    private fun parseTenantId(rawTenantId: String): TenantId {
        return runCatching { TenantId.from(rawTenantId) }
            .getOrElse { throw BadRequestException("Invalid tenant id: $rawTenantId") }
    }
}
