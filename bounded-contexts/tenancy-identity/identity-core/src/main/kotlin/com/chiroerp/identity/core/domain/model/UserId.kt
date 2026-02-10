package com.chiroerp.identity.core.domain.model

import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun random(): UserId = UserId(UUID.randomUUID())

        fun from(raw: String): UserId = UserId(UUID.fromString(raw))
    }
}
