package com.chiroerp.identity.core.application.exception

import com.chiroerp.identity.core.domain.model.UserId

class UserLifecycleException(
    val userId: UserId,
    reason: String,
) : RuntimeException("Invalid lifecycle transition for user ${userId.value}: $reason")
