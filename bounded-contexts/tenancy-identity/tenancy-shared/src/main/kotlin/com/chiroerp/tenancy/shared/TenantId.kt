package com.chiroerp.tenancy.shared

import java.util.UUID

@JvmInline
value class TenantId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun random(): TenantId = TenantId(UUID.randomUUID())
        fun from(value: String): TenantId = TenantId(UUID.fromString(value))
    }
}
