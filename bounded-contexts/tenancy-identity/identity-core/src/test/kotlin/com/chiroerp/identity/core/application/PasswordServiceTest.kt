package com.chiroerp.identity.core.application

import com.chiroerp.identity.core.application.exception.InvalidPasswordException
import com.chiroerp.identity.core.application.service.PasswordPolicy
import com.chiroerp.identity.core.application.service.PasswordService
import com.chiroerp.identity.core.infrastructure.security.PasswordEncoder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PasswordServiceTest {
    private fun policy(): PasswordPolicy = PasswordPolicy(
        minLength = 12,
        maxLength = 64,
        requireUppercase = true,
        requireLowercase = true,
        requireDigit = true,
        requireSymbol = true,
        forbidWhitespace = true,
        denylist = setOf("password123!", "welcome123!"),
        bcryptCost = 6,
    )

    private fun service(): PasswordService {
        val policy = policy()
        val encoder = PasswordEncoder(policy)
        return PasswordService(encoder, policy)
    }

    @Test
    fun `hash enforces policy and produces verifiable hash`() {
        val service = service()
        val password = "Sup3rSecure!@#"

        val hashed = service.hash(password)

        assertThat(hashed).isNotBlank()
        assertThat(service.verify(password, hashed)).isTrue()
    }

    @Test
    fun `validate returns violations for weak passwords`() {
        val service = service()

        val result = service.validate("short")

        assertThat(result.valid).isFalse()
        assertThat(result.violations).isNotEmpty
    }

    @Test
    fun `denylist triggers exception`() {
        val service = service()

        assertThatThrownBy { service.hash("Password123!") }
            .isInstanceOf(InvalidPasswordException::class.java)
    }
}
