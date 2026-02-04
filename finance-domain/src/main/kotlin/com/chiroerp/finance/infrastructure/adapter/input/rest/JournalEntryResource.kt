package com.chiroerp.finance.infrastructure.adapter.input.rest

import com.chiroerp.finance.application.port.input.command.CreateJournalEntryCommand
import com.chiroerp.finance.application.port.input.command.PostJournalEntryCommand
import com.chiroerp.finance.application.port.input.command.JournalEntryLineCommand
import com.chiroerp.finance.application.port.input.query.GetJournalEntryQuery
import com.chiroerp.finance.application.port.input.query.ListJournalEntriesQuery
import com.chiroerp.finance.application.service.CreateJournalEntryUseCase
import com.chiroerp.finance.application.service.GetJournalEntryUseCase
import com.chiroerp.finance.application.service.ListJournalEntriesUseCase
import com.chiroerp.finance.application.service.PostJournalEntryUseCase
import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.infrastructure.security.TenantContext
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.time.LocalDate
import java.util.UUID

@Path("/api/v1/finance/journal-entries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Journal Entries", description = "Journal Entry Management")
class JournalEntryResource {

    @Inject
    lateinit var createJournalEntryUseCase: CreateJournalEntryUseCase

    @Inject
    lateinit var postJournalEntryUseCase: PostJournalEntryUseCase

    @Inject
    lateinit var listJournalEntriesUseCase: ListJournalEntriesUseCase

    @Inject
    lateinit var getJournalEntryUseCase: GetJournalEntryUseCase

    @Inject
    lateinit var tenantContext: TenantContext

    @GET
    @Operation(summary = "List journal entries")
    fun list(): List<JournalEntryDto> {
        val query = ListJournalEntriesQuery(tenantId = tenantContext.getTenantId())
        return listJournalEntriesUseCase.execute(query).map { it.toDto() }
    }

    @GET
    @Path("/{entryNumber}")
    @Operation(summary = "Get journal entry by entry number")
    fun get(
        @PathParam("entryNumber") entryNumber: String
    ): Response {
        val query = GetJournalEntryQuery(tenantId = tenantContext.getTenantId(), entryNumber = entryNumber)
        val entry = getJournalEntryUseCase.execute(query) ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(entry.toDto()).build()
    }

    @POST
    @Operation(summary = "Create a new journal entry")
    fun create(@Valid request: CreateJournalEntryRequest): Response {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val command = request.toCommand(tenantContext.getTenantId(), userId)
        val entry = createJournalEntryUseCase.execute(command)
        return Response.status(Response.Status.CREATED).entity(entry.toDto()).build()
    }

    @POST
    @Path("/{entryNumber}/post")
    @Operation(summary = "Post a journal entry")
    fun post(
        @PathParam("entryNumber") entryNumber: String
    ): Response {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val command = PostJournalEntryCommand(
            tenantId = tenantContext.getTenantId(),
            entryNumber = entryNumber,
            userId = userId
        )
        val entry = postJournalEntryUseCase.execute(command)
        return Response.ok(entry.toDto()).build()
    }
}

// DTOs and mappers

data class CreateJournalEntryRequest(
    @field:NotBlank val entryNumber: String,
    @field:NotBlank val companyCode: String,
    @field:NotBlank val entryType: String,
    @field:NotNull val documentDate: LocalDate,
    @field:NotNull val postingDate: LocalDate,
    @field:NotNull val fiscalYear: Int,
    @field:NotNull val fiscalPeriod: Int,
    @field:NotBlank val currency: String,
    @field:NotNull val exchangeRate: java.math.BigDecimal = java.math.BigDecimal.ONE,
    @field:NotBlank val description: String,
    val reference: String? = null,
    @field:Valid val lines: List<CreateJournalEntryLineRequest>
)

data class CreateJournalEntryLineRequest(
    @field:NotNull val lineNumber: Int,
    @field:NotBlank val accountNumber: String,
    @field:NotNull val debitAmount: java.math.BigDecimal,
    @field:NotNull val creditAmount: java.math.BigDecimal,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null
)

fun CreateJournalEntryRequest.toCommand(tenantId: UUID, userId: UUID) = CreateJournalEntryCommand(
    tenantId = tenantId,
    entryNumber = entryNumber,
    companyCode = companyCode,
    entryType = entryType,
    documentDate = documentDate,
    postingDate = postingDate,
    fiscalYear = fiscalYear,
    fiscalPeriod = fiscalPeriod,
    currency = currency,
    exchangeRate = exchangeRate,
    description = description,
    reference = reference,
    userId = userId,
    lines = lines.map {
        JournalEntryLineCommand(
            lineNumber = it.lineNumber,
            accountNumber = it.accountNumber,
            debitAmount = it.debitAmount,
            creditAmount = it.creditAmount,
            costCenter = it.costCenter,
            profitCenter = it.profitCenter,
            description = it.description
        )
    }
)

data class JournalEntryDto(
    val entryNumber: String,
    val companyCode: String,
    val entryType: String,
    val postingDate: LocalDate,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val currency: String,
    val description: String?,
    val status: String,
    val lines: List<JournalEntryLineDto>
)

data class JournalEntryLineDto(
    val lineNumber: Int,
    val accountNumber: String,
    val debitAmount: Double?,
    val creditAmount: Double?,
    val costCenter: String?,
    val profitCenter: String?,
    val description: String?
)

fun JournalEntry.toDto() = JournalEntryDto(
    entryNumber = entryNumber,
    companyCode = companyCode,
    entryType = entryType.name,
    postingDate = postingDate,
    fiscalYear = fiscalYear,
    fiscalPeriod = fiscalPeriod,
    currency = currency,
    description = description,
    status = status.name,
    lines = lines.map {
        JournalEntryLineDto(
            lineNumber = it.lineNumber,
            accountNumber = it.accountNumber,
            debitAmount = it.debitAmount?.toDouble(),
            creditAmount = it.creditAmount?.toDouble(),
            costCenter = it.costCenter,
            profitCenter = it.profitCenter,
            description = it.text
        )
    }
)
