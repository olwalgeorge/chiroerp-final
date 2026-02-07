package com.chiroerp.finance.ap.application.service

import com.chiroerp.shared.types.events.DomainEvent

interface APEventPublisher {
    fun publish(events: List<DomainEvent>)
}