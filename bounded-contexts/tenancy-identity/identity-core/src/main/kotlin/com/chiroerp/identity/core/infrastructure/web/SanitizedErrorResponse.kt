package com.chiroerp.identity.core.infrastructure.web

import java.time.Instant

data class SanitizedErrorResponse(
    val code: String,
    val message: String,
    val errorId: String,
    val correlationId: String,
    val timestamp: Instant = Instant.now(),
)
