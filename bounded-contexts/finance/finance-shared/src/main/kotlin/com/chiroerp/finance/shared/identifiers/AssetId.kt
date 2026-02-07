package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class AssetId(val value: UUID) {
    companion object {
        fun random(): AssetId = AssetId(UUID.randomUUID())
        fun from(value: String): AssetId = AssetId(UUID.fromString(value))
    }
}
