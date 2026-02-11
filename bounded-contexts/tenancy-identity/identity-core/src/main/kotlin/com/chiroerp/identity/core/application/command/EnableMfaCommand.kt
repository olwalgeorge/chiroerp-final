package com.chiroerp.identity.core.application.command

import com.chiroerp.identity.core.domain.model.MfaMethod
import java.time.Instant
import java.util.UUID

data class EnableMfaCommand(
    val tenantId: UUID,
    val userId: UUID,
    val methods: Set<MfaMethod>,
    val sharedSecret: String,
    val backupCodes: Set<String> = emptySet(),
    val verifiedAt: Instant? = null,
)
