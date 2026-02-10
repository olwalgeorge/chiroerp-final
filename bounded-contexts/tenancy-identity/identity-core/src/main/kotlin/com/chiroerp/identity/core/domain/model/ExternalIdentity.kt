package com.chiroerp.identity.core.domain.model

import java.time.Instant

data class ExternalIdentity(
    val provider: IdentityProvider,
    val subject: String,
    val linkedAt: Instant = Instant.now(),
    val claims: Map<String, String> = emptyMap(),
) {
    init {
        require(subject.isNotBlank()) { "External subject is required" }
    }
}
