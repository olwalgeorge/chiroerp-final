package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class TaxRuleId(val value: UUID) {
    companion object {
        fun random(): TaxRuleId = TaxRuleId(UUID.randomUUID())
        fun from(value: String): TaxRuleId = TaxRuleId(UUID.fromString(value))
    }
}
