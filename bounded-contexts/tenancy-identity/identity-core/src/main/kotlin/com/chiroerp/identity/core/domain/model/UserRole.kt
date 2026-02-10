package com.chiroerp.identity.core.domain.model

import java.util.Locale

data class UserRole(
    val code: String,
    val description: String = "",
    val permissions: Set<Permission> = emptySet(),
    val sodGroup: String? = null,
) {
    init {
        require(code.isNotBlank()) { "Role code is required" }
    }

    val normalizedCode: String = code.trim().uppercase(Locale.ROOT)
}
