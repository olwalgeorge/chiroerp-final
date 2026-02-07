package com.chiroerp.finance.ar.application.service

import com.chiroerp.shared.types.events.DomainEvent

interface AREventPublisher {
    fun publish(events: List<DomainEvent>)
}
