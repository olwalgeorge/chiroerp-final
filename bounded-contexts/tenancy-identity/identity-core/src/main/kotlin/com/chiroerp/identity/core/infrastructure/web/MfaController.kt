package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.command.EnableMfaCommand
import com.chiroerp.identity.core.application.handler.UserCommandHandler
import com.chiroerp.identity.core.application.service.BackupCodeValidationResult
import com.chiroerp.identity.core.application.service.MfaEnrollmentRequest
import com.chiroerp.identity.core.application.service.MfaService
import com.chiroerp.identity.core.domain.model.MfaMethod
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.time.Instant
import java.util.UUID

@Path("/api/identity/mfa")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "jwt")
@Tag(name = "MFA", description = "Multi-factor authentication enrollment and verification")
class MfaController(
    private val mfaService: MfaService,
    private val userCommandHandler: UserCommandHandler,
) {
    @POST
    @Path("/enroll")
    @Operation(operationId = "enrollMfa", summary = "Enroll a new MFA method")
    @APIResponse(responseCode = "200", description = "MFA enrollment initiated")
    fun enroll(@Valid request: MfaEnrollRequest): MfaEnrollResponse {
        val enrollment = mfaService.createEnrollment(
            MfaEnrollmentRequest(
                accountName = request.accountName,
                issuer = request.issuer,
            ),
        )

        return MfaEnrollResponse(
            sharedSecret = enrollment.sharedSecret,
            otpAuthUri = enrollment.otpAuthUri,
            backupCodes = enrollment.backupCodes,
        )
    }

    @POST
    @Path("/verify")
    @Operation(operationId = "verifyMfa", summary = "Verify a TOTP code")
    @APIResponse(responseCode = "200", description = "Verification result")
    fun verify(@Valid request: MfaVerifyRequest): MfaVerifyResponse = MfaVerifyResponse(
        valid = mfaService.verifyTotp(
            sharedSecret = request.sharedSecret,
            code = request.code,
        ),
    )

    @POST
    @Path("/backup-codes/consume")
    @Operation(operationId = "consumeBackupCode", summary = "Consume a backup code")
    @APIResponse(responseCode = "200", description = "Backup code validation result")
    fun consumeBackupCode(@Valid request: ConsumeBackupCodeRequest): ConsumeBackupCodeResponse {
        val result: BackupCodeValidationResult = mfaService.consumeBackupCode(
            backupCodes = request.backupCodes,
            providedCode = request.code,
        )

        return ConsumeBackupCodeResponse(
            valid = result.valid,
            remainingCodes = result.remainingCodes,
        )
    }

    @POST
    @Path("/enable")
    @Operation(operationId = "enableMfa", summary = "Enable MFA for a user")
    @APIResponse(responseCode = "200", description = "MFA enabled")
    fun enable(@Valid request: EnableMfaRequest): Response {
        val user = userCommandHandler.handle(
            EnableMfaCommand(
                tenantId = request.tenantId,
                userId = request.userId,
                methods = request.methods,
                sharedSecret = request.sharedSecret,
                backupCodes = request.backupCodes,
                verifiedAt = request.verifiedAt,
            ),
        )

        return Response.ok(
            EnableMfaResponse(
                userId = user.id.value,
                tenantId = user.tenantId.value,
                mfaEnabled = user.isMfaEnabled,
            ),
        ).build()
    }
}

data class MfaEnrollRequest(
    @field:NotBlank
    val accountName: String,
    val issuer: String? = null,
)

data class MfaEnrollResponse(
    val sharedSecret: String,
    val otpAuthUri: String,
    val backupCodes: Set<String>,
)

data class MfaVerifyRequest(
    @field:NotBlank
    val sharedSecret: String,
    @field:NotBlank
    val code: String,
)

data class MfaVerifyResponse(
    val valid: Boolean,
)

data class ConsumeBackupCodeRequest(
    val backupCodes: Set<String>,
    @field:NotBlank
    val code: String,
)

data class ConsumeBackupCodeResponse(
    val valid: Boolean,
    val remainingCodes: Set<String>,
)

data class EnableMfaRequest(
    @field:NotNull
    val tenantId: UUID,
    @field:NotNull
    val userId: UUID,
    val methods: Set<MfaMethod> = setOf(MfaMethod.TOTP),
    @field:NotBlank
    val sharedSecret: String,
    val backupCodes: Set<String> = emptySet(),
    val verifiedAt: Instant? = null,
)

data class EnableMfaResponse(
    val userId: UUID,
    val tenantId: UUID,
    val mfaEnabled: Boolean,
)
