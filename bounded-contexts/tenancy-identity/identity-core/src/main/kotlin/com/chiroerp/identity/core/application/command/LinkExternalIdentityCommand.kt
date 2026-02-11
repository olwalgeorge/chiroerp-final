package com.chiroerp.identity.core.application.command

import com.chiroerp.identity.core.domain.model.IdentityProvider
import java.time.Instant
import java.util.UUID

data class LinkExternalIdentityCommand(
    val tenantId: UUID,
    val userId: UUID,
    val provider: IdentityProvider,
    val subject: String,
    val claims: Map<String, String> = emptyMap(),
    val linkedAt: Instant = Instant.now(),
)
