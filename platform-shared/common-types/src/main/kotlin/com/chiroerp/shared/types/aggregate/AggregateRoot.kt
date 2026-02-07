package com.chiroerp.shared.types.aggregate

import com.chiroerp.shared.types.events.DomainEvent

abstract class AggregateRoot<ID : Any>(id: ID) : Entity<ID>(id) {
    private val pendingEvents: MutableList<DomainEvent> = mutableListOf()

    protected fun registerEvent(event: DomainEvent) {
        pendingEvents += event
    }

    fun pullDomainEvents(): List<DomainEvent> = pendingEvents.toList().also {
        pendingEvents.clear()
    }
}
