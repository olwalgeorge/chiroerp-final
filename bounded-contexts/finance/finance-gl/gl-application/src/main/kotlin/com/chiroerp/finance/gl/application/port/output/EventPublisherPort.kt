package com.chiroerp.finance.gl.application.port.output

import com.chiroerp.shared.types.events.DomainEvent

interface EventPublisherPort {
    fun publish(events: List<DomainEvent>)
}
