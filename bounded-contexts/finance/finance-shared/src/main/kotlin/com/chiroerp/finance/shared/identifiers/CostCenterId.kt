package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class CostCenterId(val value: UUID) {
    companion object {
        fun random(): CostCenterId = CostCenterId(UUID.randomUUID())
        fun from(value: String): CostCenterId = CostCenterId(UUID.fromString(value))
    }
}
