package com.chiroerp.identity.core.domain.model

enum class UserStatus {
    PENDING,
    ACTIVE,
    LOCKED,
    DISABLED;

    fun canLogin(): Boolean = this == ACTIVE

    fun isTerminal(): Boolean = this == DISABLED
}
