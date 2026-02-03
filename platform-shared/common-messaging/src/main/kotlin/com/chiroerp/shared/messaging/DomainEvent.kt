package com.chiroerp.shared.messaging

import java.time.Instant
import java.util.UUID

/**
 * Base interface for all domain events.
 * 
 * Domain events represent facts that have occurred in the system.
 * They are immutable and should be named in past tense (e.g., InvoiceCreated).
 * 
 * Related ADRs: ADR-003, ADR-006, ADR-020
 */
interface DomainEvent {
    /**
     * Unique identifier for this event instance.
     */
    val eventId: String
    
    /**
     * Type of the event (e.g., "InvoiceCreated", "PaymentProcessed").
     */
    val eventType: String
    
    /**
     * Timestamp when the event occurred.
     */
    val occurredAt: Instant
    
    /**
     * ID of the aggregate that produced this event.
     */
    val aggregateId: String
    
    /**
     * Type of the aggregate (e.g., "Invoice", "Payment").
     */
    val aggregateType: String
    
    /**
     * Tenant ID for multi-tenancy.
     */
    val tenantId: String
    
    /**
     * User who triggered the event (if applicable).
     */
    val userId: String?
    
    /**
     * Correlation ID for tracing across services.
     */
    val correlationId: String
    
    /**
     * Version of the event schema (for evolution).
     */
    val version: Int
}

/**
 * Abstract base class for domain events with common fields.
 */
abstract class BaseDomainEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Instant = Instant.now(),
    override val correlationId: String = UUID.randomUUID().toString(),
    override val version: Int = 1
) : DomainEvent
