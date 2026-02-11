package com.chiroerp.identity.core.application.query

import com.chiroerp.identity.core.domain.model.UserStatus
import java.util.UUID

data class ListUsersQuery(
    val tenantId: UUID,
    val limit: Int = 50,
    val offset: Int = 0,
    val status: UserStatus? = null,
)
