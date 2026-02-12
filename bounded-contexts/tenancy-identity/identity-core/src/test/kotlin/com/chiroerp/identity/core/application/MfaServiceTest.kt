package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.service.MfaEnrollmentRequest
import com.chiroerp.identity.core.application.service.MfaService
import com.chiroerp.identity.core.infrastructure.security.TotpGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class MfaServiceTest {
    private val totpGenerator = TotpGenerator(
        digits = 6,
        timeStepSeconds = 30,
        secretBytes = 20,
    )

    private val service = MfaService(
        totpGenerator = totpGenerator,
        defaultIssuer = "ChiroERP",
        backupCodeCount = 8,
    )

    @Test
    fun `create enrollment returns secret backup codes and otp auth uri`() {
        val enrollment = service.createEnrollment(
            MfaEnrollmentRequest(
                accountName = "jane.doe@example.com",
                issuer = "ChiroERP",
            ),
        )

        assertThat(enrollment.sharedSecret).isNotBlank()
        assertThat(enrollment.otpAuthUri).contains("otpauth://totp")
        assertThat(enrollment.otpAuthUri).contains("jane.doe%40example.com")
        assertThat(enrollment.backupCodes).hasSize(8)
    }

    @Test
    fun `verify totp validates generated code`() {
        val secret = totpGenerator.generateSecret()
        val now = Instant.parse("2026-02-12T10:00:00Z")
        val code = totpGenerator.generateCode(secret, now)

        val valid = service.verifyTotp(secret, code, now)
        val invalid = service.verifyTotp(secret, "000000", now)

        assertThat(valid).isTrue()
        assertThat(invalid).isFalse()
    }

    @Test
    fun `consume backup code returns remaining unconsumed codes`() {
        val backupCodes = setOf("ABC12345", "XYZ67890")

        val result = service.consumeBackupCode(backupCodes, "abc12345")

        assertThat(result.valid).isTrue()
        assertThat(result.remainingCodes).containsExactlyInAnyOrder("XYZ67890")
    }
}
