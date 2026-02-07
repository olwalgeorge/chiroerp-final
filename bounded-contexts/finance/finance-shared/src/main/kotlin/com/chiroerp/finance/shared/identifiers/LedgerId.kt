package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class LedgerId(val value: UUID) {
    companion object {
        fun random(): LedgerId = LedgerId(UUID.randomUUID())
        fun from(value: String): LedgerId = LedgerId(UUID.fromString(value))
    }
}
