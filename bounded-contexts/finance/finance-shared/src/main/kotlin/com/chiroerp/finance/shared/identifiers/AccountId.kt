package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class AccountId(val value: UUID) {
    companion object {
        fun random(): AccountId = AccountId(UUID.randomUUID())
        fun from(value: String): AccountId = AccountId(UUID.fromString(value))
    }
}
