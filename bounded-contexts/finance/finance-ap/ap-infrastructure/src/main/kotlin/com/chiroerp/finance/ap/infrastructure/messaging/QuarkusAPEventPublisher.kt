package com.chiroerp.finance.ap.infrastructure.messaging

import com.chiroerp.finance.ap.application.service.APEventPublisher
import com.chiroerp.shared.types.events.DomainEvent
import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.inject.Inject

@ApplicationScoped
@Unremovable
class QuarkusAPEventPublisher @Inject constructor(
    private val eventBus: Event<DomainEvent>
) : APEventPublisher {

    override fun publish(events: List<DomainEvent>) {
        events.forEach { eventBus.fire(it) }
    }
}