package com.chiroerp.finance.api

import com.chiroerp.finance.domain.GLAccount
import com.chiroerp.finance.domain.AccountType
import com.chiroerp.finance.domain.BalanceType
import com.chiroerp.finance.infrastructure.persistence.GLAccountEntity
import com.chiroerp.finance.infrastructure.persistence.GLAccountRepository
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.UUID

/**
 * REST API for GL Account management.
 *
 * Provides endpoints for creating, reading, and managing general ledger accounts.
 * All operations are tenant-aware and enforce isolation.
 */
@Path("/api/gl-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "GL Accounts", description = "General Ledger Account Management")
class GLAccountResource {

    @Inject
    lateinit var repository: GLAccountRepository

    /**
     * Get all active GL accounts for the current tenant.
     *
     * TODO: Extract tenant ID from security context
     */
    @GET
    @Operation(summary = "List all active GL accounts")
    fun listAccounts(
        @QueryParam("tenantId") tenantId: UUID? = null
    ): List<GLAccountDTO> {
        val tid = tenantId ?: UUID.fromString("00000000-0000-0000-0000-000000000001") // SINGLE_TENANT_DEV
        return repository.findActiveByTenant(tid)
            .map { it.toDomain().toDTO() }
    }

    /**
     * Get a specific GL account by account number.
     */
    @GET
    @Path("/{accountNumber}")
    @Operation(summary = "Get GL account by account number")
    fun getAccount(
        @PathParam("accountNumber") accountNumber: String,
        @QueryParam("tenantId") tenantId: UUID? = null
    ): Response {
        val tid = tenantId ?: UUID.fromString("00000000-0000-0000-0000-000000000001")
        val entity = repository.findByTenantAndAccountNumber(tid, accountNumber)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(entity.toDomain().toDTO()).build()
    }

    /**
     * Create a new GL account.
     */
    @POST
    @Transactional
    @Operation(summary = "Create a new GL account")
    fun createAccount(
        request: CreateGLAccountRequest,
        @QueryParam("tenantId") tenantId: UUID? = null
    ): Response {
        val tid = tenantId ?: UUID.fromString("00000000-0000-0000-0000-000000000001")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000002") // TODO: From security context

        // Check if account already exists
        if (repository.existsByTenantAndAccountNumber(tid, request.accountNumber)) {
            return Response.status(Response.Status.CONFLICT)
                .entity(mapOf("error" to "Account number already exists"))
                .build()
        }

        // Create domain model
        val account = request.toDomain()

        // Convert to entity and persist
        val entity = GLAccountEntity.fromDomain(account, tid, userId)
        repository.persist(entity)

        return Response.status(Response.Status.CREATED)
            .entity(entity.toDomain().toDTO())
            .build()
    }

    /**
     * Update an existing GL account.
     */
    @PUT
    @Path("/{accountNumber}")
    @Transactional
    @Operation(summary = "Update an existing GL account")
    fun updateAccount(
        @PathParam("accountNumber") accountNumber: String,
        request: UpdateGLAccountRequest,
        @QueryParam("tenantId") tenantId: UUID? = null
    ): Response {
        val tid = tenantId ?: UUID.fromString("00000000-0000-0000-0000-000000000001")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000002")

        val entity = repository.findByTenantAndAccountNumber(tid, accountNumber)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        // Update from request
        val updatedAccount = request.applyTo(entity.toDomain())
        entity.updateFromDomain(updatedAccount, userId)

        return Response.ok(entity.toDomain().toDTO()).build()
    }

    /**
     * Deactivate a GL account (soft delete).
     */
    @DELETE
    @Path("/{accountNumber}")
    @Transactional
    @Operation(summary = "Deactivate a GL account")
    fun deactivateAccount(
        @PathParam("accountNumber") accountNumber: String,
        @QueryParam("tenantId") tenantId: UUID? = null
    ): Response {
        val tid = tenantId ?: UUID.fromString("00000000-0000-0000-0000-000000000001")

        val entity = repository.findByTenantAndAccountNumber(tid, accountNumber)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        entity.isActive = false

        return Response.noContent().build()
    }
}

/**
 * DTO for GL Account responses.
 */
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

/**
 * Request for creating a GL account.
 */
data class CreateGLAccountRequest(
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val balanceType: String,
    val currency: String,
    val companyCode: String,
    val isPostingAllowed: Boolean = true,
    val parentAccount: String? = null,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null
) {
    fun toDomain() = GLAccount(
        accountNumber = accountNumber,
        accountName = accountName,
        accountType = AccountType.valueOf(accountType),
        balanceType = BalanceType.valueOf(balanceType),
        currency = currency,
        companyCode = companyCode,
        isActive = true,
        isPostingAllowed = isPostingAllowed,
        parentAccount = parentAccount,
        costCenter = costCenter,
        profitCenter = profitCenter,
        description = description
    )
}

/**
 * Request for updating a GL account.
 */
data class UpdateGLAccountRequest(
    val accountName: String?,
    val isPostingAllowed: Boolean?,
    val costCenter: String?,
    val profitCenter: String?,
    val description: String?
) {
    fun applyTo(account: GLAccount): GLAccount {
        return account.copy(
            accountName = accountName ?: account.accountName,
            isPostingAllowed = isPostingAllowed ?: account.isPostingAllowed,
            costCenter = costCenter ?: account.costCenter,
            profitCenter = profitCenter ?: account.profitCenter,
            description = description ?: account.description
        )
    }
}

/**
 * Extension to convert domain model to DTO.
 */
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
