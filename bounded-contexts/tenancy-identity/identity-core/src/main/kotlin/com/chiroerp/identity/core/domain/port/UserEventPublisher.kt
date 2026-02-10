package com.chiroerp.identity.core.domain.port

import com.chiroerp.identity.core.domain.event.UserDomainEvent

interface UserEventPublisher {
    fun publish(events: Collection<UserDomainEvent>)
}
