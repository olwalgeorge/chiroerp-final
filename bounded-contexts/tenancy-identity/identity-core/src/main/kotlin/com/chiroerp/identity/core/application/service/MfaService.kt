package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.infrastructure.security.TotpGenerator
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import java.time.Instant

private const val BACKUP_CODE_LENGTH = 8
private const val BACKUP_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

data class MfaEnrollmentRequest(
    val accountName: String,
    val issuer: String? = null,
)

data class MfaEnrollment(
    val sharedSecret: String,
    val otpAuthUri: String,
    val backupCodes: Set<String>,
)

data class BackupCodeValidationResult(
    val valid: Boolean,
    val remainingCodes: Set<String>,
)

@ApplicationScoped
class MfaService(
    private val totpGenerator: TotpGenerator,
    @param:ConfigProperty(name = "chiroerp.identity.mfa.issuer", defaultValue = "ChiroERP")
    private val defaultIssuer: String,
    @param:ConfigProperty(name = "chiroerp.identity.mfa.backup-code-count", defaultValue = "8")
    private val backupCodeCount: Int,
) {
    private val secureRandom = SecureRandom()

    fun createEnrollment(request: MfaEnrollmentRequest): MfaEnrollment {
        val issuer = request.issuer?.trim().takeUnless { it.isNullOrEmpty() } ?: defaultIssuer
        val secret = totpGenerator.generateSecret()
        val backupCodes = generateBackupCodes()

        return MfaEnrollment(
            sharedSecret = secret,
            otpAuthUri = totpGenerator.buildOtpAuthUri(
                secret = secret,
                accountName = request.accountName,
                issuer = issuer,
            ),
            backupCodes = backupCodes,
        )
    }

    fun verifyTotp(sharedSecret: String, code: String, now: Instant = Instant.now()): Boolean =
        runCatching { totpGenerator.verifyCode(sharedSecret, code, now) }.getOrDefault(false)

    fun consumeBackupCode(backupCodes: Set<String>, providedCode: String): BackupCodeValidationResult {
        val normalizedCode = providedCode.trim().uppercase()
        if (normalizedCode.isEmpty()) {
            return BackupCodeValidationResult(valid = false, remainingCodes = backupCodes)
        }

        val normalizedExisting = backupCodes
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (normalizedCode !in normalizedExisting) {
            return BackupCodeValidationResult(valid = false, remainingCodes = backupCodes)
        }

        return BackupCodeValidationResult(
            valid = true,
            remainingCodes = normalizedExisting - normalizedCode,
        )
    }

    private fun generateBackupCodes(): Set<String> {
        val count = backupCodeCount.coerceIn(4, 20)
        return buildSet(count) {
            while (size < count) {
                add(generateBackupCode())
            }
        }
    }

    private fun generateBackupCode(): String {
        val chars = CharArray(BACKUP_CODE_LENGTH)
        for (i in chars.indices) {
            chars[i] = BACKUP_CODE_ALPHABET[secureRandom.nextInt(BACKUP_CODE_ALPHABET.length)]
        }
        return chars.concatToString()
    }
}
