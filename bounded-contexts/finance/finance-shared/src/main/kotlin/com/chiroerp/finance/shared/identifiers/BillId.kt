package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class BillId(val value: UUID) {
    companion object {
        fun random(): BillId = BillId(UUID.randomUUID())
        fun from(value: String): BillId = BillId(UUID.fromString(value))
    }
}