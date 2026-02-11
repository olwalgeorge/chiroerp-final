package com.chiroerp.identity.core.application.query

import java.util.UUID

data class GetUserQuery(
    val tenantId: UUID,
    val userId: UUID,
)
