package com.chiroerp.shared.messaging

/**
 * Interface for publishing domain events.
 * 
 * Phase 0: In-memory event bus (for testing)
 * Phase 1: Kafka-based event streaming
 * Phase 2: Event sourcing with replay capabilities
 * 
 * Related ADRs: ADR-003, ADR-006, ADR-020
 */
interface DomainEventPublisher {
    /**
     * Publish a domain event to the event bus.
     * 
     * @param event Domain event to publish
     * @return Result indicating success or failure
     */
    suspend fun publish(event: DomainEvent): Result<Unit>
    
    /**
     * Publish multiple domain events atomically.
     * 
     * @param events List of domain events
     * @return Result indicating success or failure
     */
    suspend fun publishBatch(events: List<DomainEvent>): Result<Unit>
}
