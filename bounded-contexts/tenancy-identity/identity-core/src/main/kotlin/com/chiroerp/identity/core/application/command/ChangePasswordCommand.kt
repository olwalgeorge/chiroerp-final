package com.chiroerp.identity.core.application.command

import java.time.Duration
import java.util.UUID

/**
 * Command issued when a user password rotation is requested.
 * The new password must already be hashed.
 */
data class ChangePasswordCommand(
    val userId: UUID,
    val tenantId: UUID,
    val newPasswordHash: String,
    val historySize: Int? = null,
    val newTtl: Duration? = null,
    val forceChangeOnNextLogin: Boolean = false,
)
