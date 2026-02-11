package com.chiroerp.identity.core.application.query

import java.util.UUID

data class GetUserByEmailQuery(
    val tenantId: UUID,
    val email: String,
)
