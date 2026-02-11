package com.chiroerp.identity.core.application.command

import java.util.UUID

data class LockUserCommand(
    val tenantId: UUID,
    val userId: UUID,
    val reason: String,
)
