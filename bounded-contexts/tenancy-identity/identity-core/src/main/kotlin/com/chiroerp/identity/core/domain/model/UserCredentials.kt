package com.chiroerp.identity.core.domain.model

import java.time.Duration
import java.time.Instant

data class UserCredentials(
    val hashedPassword: String,
    val passwordVersion: Int,
    val passwordChangedAt: Instant,
    val passwordHistory: List<String> = emptyList(),
    val mustChangePassword: Boolean = false,
    val expiresAt: Instant? = null,
) {
    init {
        require(hashedPassword.isNotBlank()) { "Password hash cannot be blank" }
        require(passwordVersion >= 1) { "Password version must be positive" }
    }

    fun rotate(
        newHash: String,
        changedAt: Instant = Instant.now(),
        enforceHistory: Int = DEFAULT_HISTORY_SIZE,
        forceChange: Boolean = false,
        ttl: Duration? = null,
    ): UserCredentials {
        require(newHash.isNotBlank()) { "New password hash is required" }
        require(newHash != hashedPassword) { "Password hash must change" }
        if (passwordHistory.take(enforceHistory).any { it == newHash }) {
            throw IllegalArgumentException("Password was recently used")
        }
        val newHistory = (listOf(hashedPassword) + passwordHistory).take(enforceHistory)
        val newExpiry = ttl?.let { changedAt.plus(it) }
        return copy(
            hashedPassword = newHash,
            passwordVersion = passwordVersion + 1,
            passwordChangedAt = changedAt,
            passwordHistory = newHistory,
            mustChangePassword = forceChange,
            expiresAt = newExpiry,
        )
    }

    fun requiresRotation(now: Instant = Instant.now()): Boolean {
        if (mustChangePassword) return true
        return expiresAt?.isBefore(now) ?: false
    }

    fun withForceChange(): UserCredentials = copy(mustChangePassword = true)

    companion object {
        private const val DEFAULT_HISTORY_SIZE = 5
    }
}
