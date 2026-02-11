package com.chiroerp.identity.core.application.exception

import com.chiroerp.identity.core.domain.model.UserId

class UserNotFoundException(
    val userId: UserId,
) : RuntimeException("User ${userId.value} not found")
