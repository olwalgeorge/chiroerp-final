package com.chiroerp.identity.core.domain.model

import java.time.Instant

enum class MfaMethod {
    TOTP,
    SMS,
    EMAIL,
}

data class MfaConfiguration(
    val methods: Set<MfaMethod>,
    val sharedSecret: String,
    val backupCodes: Set<String> = emptySet(),
    val enrolledAt: Instant = Instant.now(),
    val verifiedAt: Instant? = null,
) {
    init {
        require(methods.isNotEmpty()) { "At least one MFA method is required" }
        require(sharedSecret.isNotBlank()) { "Shared secret cannot be blank" }
    }

    fun isMethodEnabled(method: MfaMethod): Boolean = methods.contains(method)

    fun markVerified(at: Instant = Instant.now()): MfaConfiguration = copy(verifiedAt = at)
}
