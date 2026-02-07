package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class BudgetId(val value: UUID) {
    companion object {
        fun random(): BudgetId = BudgetId(UUID.randomUUID())
        fun from(value: String): BudgetId = BudgetId(UUID.fromString(value))
    }
}
