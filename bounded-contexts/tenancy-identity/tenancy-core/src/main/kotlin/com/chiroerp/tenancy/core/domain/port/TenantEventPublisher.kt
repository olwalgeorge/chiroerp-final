package com.chiroerp.tenancy.core.domain.port

import com.chiroerp.shared.types.events.DomainEvent

interface TenantEventPublisher {
    fun publish(events: List<DomainEvent>)
}
