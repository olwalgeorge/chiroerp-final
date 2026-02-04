package com.chiroerp.finance.infrastructure.adapter.input.rest

import com.chiroerp.finance.application.port.input.command.CreateGLAccountCommand
import com.chiroerp.finance.application.port.input.command.UpdateGLAccountCommand
import com.chiroerp.finance.application.port.input.query.GetGLAccountQuery
import com.chiroerp.finance.application.port.input.query.ListGLAccountsQuery
import com.chiroerp.finance.application.service.CreateGLAccountUseCase
import com.chiroerp.finance.application.service.GetGLAccountUseCase
import com.chiroerp.finance.application.service.ListGLAccountsUseCase
import com.chiroerp.finance.application.service.UpdateGLAccountUseCase
import com.chiroerp.finance.domain.GLAccount
import com.chiroerp.finance.infrastructure.security.TenantContext
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.UUID

@Path("/api/v1/finance/gl-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "GL Accounts", description = "General Ledger Account Management")
class GLAccountResource {

    @Inject
    lateinit var createGLAccountUseCase: CreateGLAccountUseCase

    @Inject
    lateinit var updateGLAccountUseCase: UpdateGLAccountUseCase

    @Inject
    lateinit var listGLAccountsUseCase: ListGLAccountsUseCase

    @Inject
    lateinit var getGLAccountUseCase: GetGLAccountUseCase

    @Inject
    lateinit var tenantContext: TenantContext

    @GET
    @Operation(summary = "List all active GL accounts")
    fun list(): List<GLAccountDTO> {
        val query = ListGLAccountsQuery(tenantId = tenantContext.getTenantId())
        val accounts = listGLAccountsUseCase.execute(query)
        return accounts.map { it.toDTO() }
    }

    @GET
    @Path("/{accountNumber}")
    @Operation(summary = "Get GL account by account number")
    fun get(
        @PathParam("accountNumber") accountNumber: String
    ): Response {
        val query = GetGLAccountQuery(tenantId = tenantContext.getTenantId(), accountNumber = accountNumber)
        val account = getGLAccountUseCase.execute(query)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(account.toDTO()).build()
    }

    @POST
    @Operation(summary = "Create a new GL account")
    fun create(
        @Valid request: CreateGLAccountRequest
    ): Response {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val command = CreateGLAccountCommand(
            tenantId = tenantContext.getTenantId(),
            accountNumber = request.accountNumber,
            accountName = request.accountName,
            accountType = request.accountType,
            balanceType = request.balanceType,
            currency = request.currency,
            companyCode = request.companyCode,
            isActive = request.isActive ?: true,
            isPostingAllowed = request.isPostingAllowed ?: true,
            parentAccount = request.parentAccount,
            costCenter = request.costCenter,
            profitCenter = request.profitCenter,
            description = request.description,
            userId = userId
        )
        val account = createGLAccountUseCase.execute(command)
        return Response.status(Response.Status.CREATED).entity(account.toDTO()).build()
    }

    @PUT
    @Path("/{accountNumber}")
    @Operation(summary = "Update an existing GL account")
    fun update(
        @PathParam("accountNumber") accountNumber: String,
        @Valid request: UpdateGLAccountRequest
    ): Response {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val command = UpdateGLAccountCommand(
            tenantId = tenantContext.getTenantId(),
            accountNumber = accountNumber,
            accountName = request.accountName,
            isActive = request.isActive,
            isPostingAllowed = request.isPostingAllowed,
            costCenter = request.costCenter,
            profitCenter = request.profitCenter,
            description = request.description,
            userId = userId
        )
        val account = updateGLAccountUseCase.execute(command)
        return Response.ok(account.toDTO()).build()
    }
}

data class CreateGLAccountRequest(
    @field:NotBlank val accountNumber: String,
    @field:NotBlank val accountName: String,
    @field:NotBlank val accountType: String,
    @field:NotBlank val balanceType: String,
    @field:NotBlank @field:Size(min = 3, max = 3) val currency: String,
    @field:NotBlank val companyCode: String,
    val isActive: Boolean? = true,
    val isPostingAllowed: Boolean? = true,
    val parentAccount: String? = null,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null
)

data class UpdateGLAccountRequest(
    @field:NotBlank val accountName: String,
    @field:NotNull val isActive: Boolean,
    @field:NotNull val isPostingAllowed: Boolean,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null
)

data class GLAccountDTO(
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val balanceType: String,
    val currency: String,
    val companyCode: String,
    val isActive: Boolean,
    val isPostingAllowed: Boolean,
    val parentAccount: String?,
    val costCenter: String?,
    val profitCenter: String?,
    val description: String?
)

fun GLAccount.toDTO() = GLAccountDTO(
    accountNumber = accountNumber,
    accountName = accountName,
    accountType = accountType.name,
    balanceType = balanceType.name,
    currency = currency,
    companyCode = companyCode,
    isActive = isActive,
    isPostingAllowed = isPostingAllowed,
    parentAccount = parentAccount,
    costCenter = costCenter,
    profitCenter = profitCenter,
    description = description
)
