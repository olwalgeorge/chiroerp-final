package com.chiroerp.shared.types.primitives

import java.time.Instant

/**
 * Technical audit metadata for created/updated tracking.
 */
data class AuditInfo(
    val createdAt: Instant,
    val createdBy: String,
    val updatedAt: Instant = createdAt,
    val updatedBy: String = createdBy,
) {
    fun updated(by: String, at: Instant = Instant.now()): AuditInfo = copy(
        updatedAt = at,
        updatedBy = by,
    )
}
