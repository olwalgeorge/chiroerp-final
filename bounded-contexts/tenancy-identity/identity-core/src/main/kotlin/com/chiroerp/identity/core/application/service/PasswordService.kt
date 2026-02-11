package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.application.exception.InvalidPasswordException
import com.chiroerp.identity.core.infrastructure.security.PasswordEncoder
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.Optional

@ApplicationScoped
class PasswordService @Inject constructor(
    private val passwordEncoder: PasswordEncoder,
    private val policy: PasswordPolicy,
) {
    private val blockedPasswords: Set<String> = policy.denylist

    fun hash(rawPassword: String): String {
        require(rawPassword.isNotBlank()) { "Password is required" }
        requireValid(rawPassword)
        return passwordEncoder.hash(rawPassword)
    }

    fun verify(rawPassword: String, hashedPassword: String): Boolean {
        if (rawPassword.isBlank() || hashedPassword.isBlank()) {
            return false
        }
        return passwordEncoder.matches(rawPassword, hashedPassword)
    }

    fun validate(rawPassword: String): PasswordValidationResult {
        val violations = mutableListOf<String>()
        val length = rawPassword.length

        if (length < policy.minLength) {
            violations += "Password must be at least ${policy.minLength} characters."
        }
        policy.maxLength?.let { max ->
            if (length > max) {
                violations += "Password must be at most $max characters."
            }
        }
        if (policy.requireUppercase && rawPassword.none(Char::isUpperCase)) {
            violations += "Password must contain an uppercase character."
        }
        if (policy.requireLowercase && rawPassword.none(Char::isLowerCase)) {
            violations += "Password must contain a lowercase character."
        }
        if (policy.requireDigit && rawPassword.none(Char::isDigit)) {
            violations += "Password must contain a digit."
        }
        if (policy.requireSymbol && rawPassword.none { !it.isLetterOrDigit() }) {
            violations += "Password must contain a symbol."
        }
        if (policy.forbidWhitespace && rawPassword.any(Char::isWhitespace)) {
            violations += "Password cannot contain whitespace."
        }
        val normalized = rawPassword.trim().lowercase()
        if (blockedPasswords.contains(normalized)) {
            violations += "Password is too common."
        }

        return PasswordValidationResult(violations.isEmpty(), violations)
    }

    fun requireValid(rawPassword: String) {
        val result = validate(rawPassword)
        if (!result.valid) {
            throw InvalidPasswordException(result.violations)
        }
    }
}

data class PasswordValidationResult(
    val valid: Boolean,
    val violations: List<String>,
)

data class PasswordPolicy(
    val minLength: Int,
    val maxLength: Int?,
    val requireUppercase: Boolean,
    val requireLowercase: Boolean,
    val requireDigit: Boolean,
    val requireSymbol: Boolean,
    val forbidWhitespace: Boolean,
    val denylist: Set<String>,
    val bcryptCost: Int,
)

@Singleton
class PasswordPolicyProducer {
    @Produces
    fun passwordPolicy(
        @ConfigProperty(name = "chiroerp.identity.password.min-length", defaultValue = "12") minLength: Int,
        @ConfigProperty(name = "chiroerp.identity.password.max-length") maxLength: Optional<Int>,
        @ConfigProperty(name = "chiroerp.identity.password.require-uppercase", defaultValue = "true") requireUppercase: Boolean,
        @ConfigProperty(name = "chiroerp.identity.password.require-lowercase", defaultValue = "true") requireLowercase: Boolean,
        @ConfigProperty(name = "chiroerp.identity.password.require-digit", defaultValue = "true") requireDigit: Boolean,
        @ConfigProperty(name = "chiroerp.identity.password.require-symbol", defaultValue = "true") requireSymbol: Boolean,
        @ConfigProperty(name = "chiroerp.identity.password.forbid-whitespace", defaultValue = "true") forbidWhitespace: Boolean,
        @ConfigProperty(name = "chiroerp.identity.password.denylist") denylist: Optional<List<String>>,
        @ConfigProperty(name = "chiroerp.identity.password.bcrypt-cost", defaultValue = "12") bcryptCost: Int,
    ): PasswordPolicy = PasswordPolicy(
        minLength = minLength,
        maxLength = maxLength.orElse(null),
        requireUppercase = requireUppercase,
        requireLowercase = requireLowercase,
        requireDigit = requireDigit,
        requireSymbol = requireSymbol,
        forbidWhitespace = forbidWhitespace,
        denylist = denylist.orElse(emptyList()).map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet(),
        bcryptCost = bcryptCost,
    )
}
