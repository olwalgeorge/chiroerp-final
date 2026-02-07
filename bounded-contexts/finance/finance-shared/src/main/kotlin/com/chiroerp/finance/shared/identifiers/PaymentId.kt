package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class PaymentId(val value: UUID) {
    companion object {
        fun random(): PaymentId = PaymentId(UUID.randomUUID())
        fun from(value: String): PaymentId = PaymentId(UUID.fromString(value))
    }
}
