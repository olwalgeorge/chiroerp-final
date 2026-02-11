package com.chiroerp.identity.core.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt
import com.chiroerp.identity.core.application.service.PasswordPolicy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class PasswordEncoder @Inject constructor(
    private val policy: PasswordPolicy,
) {
    private val hasher = BCrypt.withDefaults()
    private val verifier = BCrypt.verifyer()

    fun hash(rawPassword: String): String {
        require(rawPassword.isNotBlank()) { "Password is required" }
        return hasher.hashToString(policy.bcryptCost, rawPassword.toCharArray())
    }

    fun matches(rawPassword: String, hashedPassword: String): Boolean {
        if (rawPassword.isBlank() || hashedPassword.isBlank()) {
            return false
        }
        return try {
            verifier.verify(rawPassword.toCharArray(), hashedPassword.toCharArray()).verified
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
