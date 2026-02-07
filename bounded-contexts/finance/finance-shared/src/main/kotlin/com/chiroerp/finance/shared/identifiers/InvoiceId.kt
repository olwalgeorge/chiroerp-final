package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class InvoiceId(val value: UUID) {
    companion object {
        fun random(): InvoiceId = InvoiceId(UUID.randomUUID())
        fun from(value: String): InvoiceId = InvoiceId(UUID.fromString(value))
    }
}
