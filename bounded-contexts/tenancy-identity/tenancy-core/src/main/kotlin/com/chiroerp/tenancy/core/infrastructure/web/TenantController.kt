package com.chiroerp.tenancy.core.infrastructure.api

import com.chiroerp.tenancy.core.application.command.ActivateTenantCommand
import com.chiroerp.tenancy.core.application.exception.TenantAlreadyExistsException
import com.chiroerp.tenancy.core.application.exception.TenantLifecycleTransitionException
import com.chiroerp.tenancy.core.application.exception.TenantNotFoundException
import com.chiroerp.tenancy.core.application.handler.TenantCommandHandler
import com.chiroerp.tenancy.core.application.handler.TenantQueryHandler
import com.chiroerp.tenancy.core.application.query.GetTenantByDomainQuery
import com.chiroerp.tenancy.core.application.query.GetTenantQuery
import com.chiroerp.tenancy.core.application.query.ListTenantsQuery
import com.chiroerp.tenancy.core.application.service.TenantResolutionService
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.infrastructure.observability.TenantAuditActor
import com.chiroerp.tenancy.core.infrastructure.observability.TenantAuditLogger
import com.chiroerp.tenancy.core.infrastructure.observability.TenantLifecycleAction
import com.chiroerp.tenancy.shared.TenantId
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
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
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/api/tenants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("tenant-admin", "platform-admin")
@SecurityRequirement(name = "jwt")
@Tag(name = "Tenants", description = "Multi-tenant provisioning and lifecycle management")
class TenantController(
    private val tenantCommandHandler: TenantCommandHandler,
    private val tenantQueryHandler: TenantQueryHandler,
    private val tenantResolutionService: TenantResolutionService,
    private val tenantAuditLogger: TenantAuditLogger,
    private val meterRegistry: MeterRegistry,
    private val securityIdentity: SecurityIdentity,
) {
    @POST
    @Operation(operationId = "createTenant", summary = "Create a new tenant")
    @APIResponses(
        APIResponse(responseCode = "201", description = "Tenant created"),
        APIResponse(responseCode = "409", description = "Tenant already exists"),
    )
    fun createTenant(@Valid request: CreateTenantRequest): Response {
        return try {
            val tenant = tenantCommandHandler.handle(request.toCommand())
            meterRegistry.counter("chiroerp.tenancy.api.create.requests").increment()
            auditLifecycle(
                action = TenantLifecycleAction.CREATE,
                tenant = tenant,
                callerTenantIdHeader = null,
                metadata = mapOf(
                    "domain" to tenant.domain,
                    "tier" to tenant.tier.name,
                ),
            )
            Response.status(Response.Status.CREATED)
                .entity(TenantResponse.from(tenant))
                .build()
        } catch (ex: TenantAlreadyExistsException) {
            throw WebApplicationException(ex.message, Response.Status.CONFLICT)
        }
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getTenant", summary = "Get a tenant by ID")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Tenant found"),
        APIResponse(responseCode = "404", description = "Tenant not found"),
    )
    fun getTenant(
        @PathParam("id") rawTenantId: String,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantResponse {
        val tenantId = parseTenantId(rawTenantId)
        requireTenantScope(tenantId, callerTenantIdHeader)
        val tenant = tenantQueryHandler.handle(GetTenantQuery(tenantId))
            ?: throw NotFoundException("Tenant $rawTenantId not found")

        meterRegistry.counter("chiroerp.tenancy.api.get.requests").increment()
        return TenantResponse.from(tenant)
    }

    @GET
    @Path("/by-domain/{domain}")
    @RolesAllowed("tenant-admin", "platform-admin")
    @Operation(operationId = "getTenantByDomain", summary = "Look up a tenant by domain")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Tenant found"),
        APIResponse(responseCode = "404", description = "Tenant not found"),
    )
    fun getTenantByDomain(
        @PathParam("domain") rawDomain: String,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantResponse {
        val tenant = try {
            tenantQueryHandler.handle(GetTenantByDomainQuery(rawDomain))
        } catch (ex: IllegalArgumentException) {
            throw BadRequestException(ex.message)
        } ?: throw NotFoundException("Tenant with domain '$rawDomain' not found")
        requireTenantScope(tenant.id, callerTenantIdHeader)

        meterRegistry.counter("chiroerp.tenancy.api.get-by-domain.requests").increment()
        return TenantResponse.from(tenant)
    }

    @GET
    @Operation(operationId = "listTenants", summary = "List tenants with pagination")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Paginated tenant list"),
        APIResponse(responseCode = "403", description = "Cross-tenant access denied"),
    )
    fun listTenants(
        @DefaultValue("0") @QueryParam("offset") offset: Int,
        @DefaultValue("50") @QueryParam("limit") limit: Int,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantListResponse {
        if (securityIdentity.hasRole("tenant-admin") && !securityIdentity.hasRole("platform-admin")) {
            val scopedTenantId = requireCallerTenantId(callerTenantIdHeader)
            val tenant = tenantQueryHandler.handle(GetTenantQuery(scopedTenantId))
                ?: throw NotFoundException("Tenant '${scopedTenantId.value}' not found")

            meterRegistry.counter("chiroerp.tenancy.api.list.requests").increment()
            return TenantListResponse(
                items = listOf(TenantResponse.from(tenant)),
                offset = 0,
                limit = 1,
                count = 1,
            )
        }

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
    @Operation(operationId = "updateTenantSettings", summary = "Update tenant settings")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Settings updated"),
        APIResponse(responseCode = "404", description = "Tenant not found"),
    )
    fun updateTenantSettings(
        @PathParam("id") rawTenantId: String,
        @Valid request: UpdateTenantSettingsRequest,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantResponse {
        val tenantId = parseTenantId(rawTenantId)
        requireTenantScope(tenantId, callerTenantIdHeader)

        return try {
            val tenant = tenantCommandHandler.handle(request.toCommand(tenantId))
            meterRegistry.counter("chiroerp.tenancy.api.patch.settings.requests").increment()
            TenantResponse.from(tenant)
        } catch (ex: TenantNotFoundException) {
            throw NotFoundException(ex.message)
        }
    }

    @POST
    @Path("/{id}/activate")
    @Consumes(MediaType.WILDCARD)
    @Operation(operationId = "activateTenant", summary = "Activate a tenant")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Tenant activated"),
        APIResponse(responseCode = "404", description = "Tenant not found"),
        APIResponse(responseCode = "409", description = "Invalid lifecycle transition"),
    )
    fun activateTenant(
        @PathParam("id") rawTenantId: String,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantResponse = executeLifecycleOperation(
        rawTenantId = rawTenantId,
        callerTenantIdHeader = callerTenantIdHeader,
        metric = "chiroerp.tenancy.api.activate.requests",
        action = TenantLifecycleAction.ACTIVATE,
    ) { tenantId ->
        tenantCommandHandler.handle(ActivateTenantCommand(tenantId))
    }

    @POST
    @Path("/{id}/suspend")
    @Operation(operationId = "suspendTenant", summary = "Suspend a tenant")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Tenant suspended"),
        APIResponse(responseCode = "404", description = "Tenant not found"),
        APIResponse(responseCode = "409", description = "Invalid lifecycle transition"),
    )
    fun suspendTenant(
        @PathParam("id") rawTenantId: String,
        @Valid request: SuspendTenantRequest,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantResponse = executeLifecycleOperation(
        rawTenantId = rawTenantId,
        callerTenantIdHeader = callerTenantIdHeader,
        metric = "chiroerp.tenancy.api.suspend.requests",
        action = TenantLifecycleAction.SUSPEND,
        metadata = mapOf("reason" to request.reason),
    ) { tenantId ->
        tenantCommandHandler.handle(request.toCommand(tenantId))
    }

    @POST
    @Path("/{id}/terminate")
    @Operation(operationId = "terminateTenant", summary = "Terminate a tenant")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Tenant terminated"),
        APIResponse(responseCode = "404", description = "Tenant not found"),
        APIResponse(responseCode = "409", description = "Invalid lifecycle transition"),
    )
    fun terminateTenant(
        @PathParam("id") rawTenantId: String,
        @Valid request: TerminateTenantRequest,
        @HeaderParam("X-Tenant-ID") callerTenantIdHeader: String?,
    ): TenantResponse = executeLifecycleOperation(
        rawTenantId = rawTenantId,
        callerTenantIdHeader = callerTenantIdHeader,
        metric = "chiroerp.tenancy.api.terminate.requests",
        action = TenantLifecycleAction.TERMINATE,
        metadata = mapOf("reason" to request.reason),
    ) { tenantId ->
        tenantCommandHandler.handle(request.toCommand(tenantId))
    }

    @GET
    @Path("/resolve")
    @RolesAllowed("gateway-service", "platform-admin")
    @Operation(operationId = "resolveTenant", summary = "Resolve tenant from domain or headers")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Tenant resolved"),
        APIResponse(responseCode = "404", description = "Unable to resolve tenant"),
    )
    fun resolveTenant(
        @QueryParam("domain") queryDomain: String?,
        @HeaderParam("X-Tenant-ID") headerTenantId: String?,
        @HeaderParam("X-Tenant-Domain") headerTenantDomain: String?,
        @HeaderParam("Host") hostHeader: String?,
    ): TenantResolutionResponse {
        val resolution = try {
            tenantResolutionService.resolveTenant(
                queryDomain = queryDomain,
                headerTenantId = headerTenantId,
                headerTenantDomain = headerTenantDomain,
                hostHeader = hostHeader,
            )
        } catch (ex: IllegalArgumentException) {
            throw BadRequestException(ex.message)
        } ?: throw NotFoundException("Unable to resolve tenant from supplied domain/headers")

        meterRegistry.counter("chiroerp.tenancy.api.resolve.requests").increment()
        return TenantResolutionResponse.from(resolution)
    }

    private fun executeLifecycleOperation(
        rawTenantId: String,
        callerTenantIdHeader: String?,
        metric: String,
        action: TenantLifecycleAction,
        metadata: Map<String, String?> = emptyMap(),
        operation: (TenantId) -> com.chiroerp.tenancy.core.domain.model.Tenant,
    ): TenantResponse {
        val tenantId = parseTenantId(rawTenantId)
        requireTenantScope(tenantId, callerTenantIdHeader)
        return try {
            val tenant = operation(tenantId)
            meterRegistry.counter(metric).increment()
            auditLifecycle(
                action = action,
                tenant = tenant,
                callerTenantIdHeader = callerTenantIdHeader,
                metadata = metadata,
            )
            TenantResponse.from(tenant)
        } catch (ex: TenantNotFoundException) {
            throw NotFoundException(ex.message)
        } catch (ex: TenantLifecycleTransitionException) {
            throw WebApplicationException(ex.message, Response.Status.CONFLICT)
        }
    }

    private fun parseTenantId(rawTenantId: String): TenantId {
        return runCatching { TenantId.from(rawTenantId) }
            .getOrElse { throw BadRequestException("Invalid tenant id: $rawTenantId") }
    }

    private fun requireTenantScope(targetTenantId: TenantId, callerTenantIdHeader: String?) {
        if (securityIdentity.hasRole("platform-admin")) {
            return
        }
        if (!securityIdentity.hasRole("tenant-admin")) {
            return
        }

        val callerTenantId = requireCallerTenantId(callerTenantIdHeader)
        if (callerTenantId != targetTenantId) {
            throw ForbiddenException("Cross-tenant access denied")
        }
    }

    private fun requireCallerTenantId(callerTenantIdHeader: String?): TenantId {
        val raw = callerTenantIdHeader?.trim()
        if (raw.isNullOrBlank()) {
            throw ForbiddenException("Missing X-Tenant-ID header for tenant-scoped access")
        }

        return runCatching { TenantId.from(raw) }
            .getOrElse { throw BadRequestException("Invalid X-Tenant-ID header: $callerTenantIdHeader") }
    }

    private fun auditLifecycle(
        action: TenantLifecycleAction,
        tenant: Tenant,
        callerTenantIdHeader: String?,
        metadata: Map<String, String?> = emptyMap(),
    ) {
        val actorTenantUuid = callerTenantIdHeader?.let { parseTenantIdOrNull(it)?.value }
        val actor = TenantAuditActor(
            principalId = securityIdentity.principal?.name ?: "unknown",
            tenantId = actorTenantUuid,
            roles = securityIdentity.roles.toSet(),
        )

        tenantAuditLogger.logLifecycle(
            action = action,
            tenant = tenant,
            actor = actor,
            metadata = metadata,
        )
    }

    private fun parseTenantIdOrNull(raw: String?): TenantId? {
        val trimmed = raw?.trim()
        if (trimmed.isNullOrBlank()) {
            return null
        }
        return runCatching { TenantId.from(trimmed) }.getOrNull()
    }
}
