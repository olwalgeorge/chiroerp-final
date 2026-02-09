package com.chiroerp.tenancy.core.domain.port

import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent

interface TenantEventPublisher {
    fun publish(events: List<TenantDomainEvent>)
}
