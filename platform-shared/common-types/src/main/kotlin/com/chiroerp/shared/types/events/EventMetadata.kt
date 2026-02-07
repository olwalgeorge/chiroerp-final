package com.chiroerp.shared.types.events

import java.time.Instant

data class EventMetadata(
    val occurredAt: Instant = Instant.now(),
    val correlationId: String? = null,
    val causationId: String? = null,
    val tenantId: String? = null,
    val triggeredBy: String? = null,
)
