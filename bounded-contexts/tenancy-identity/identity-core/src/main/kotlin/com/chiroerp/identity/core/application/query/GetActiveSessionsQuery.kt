package com.chiroerp.identity.core.application.query

import java.util.UUID

data class GetActiveSessionsQuery(
    val tenantId: UUID,
    val userId: UUID,
)
