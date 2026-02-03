package com.chiroerp.shared.types.cqrs

/**
 * Command Interface
 * 
 * Marker interface for all commands in the system.
 * Commands represent write operations (state changes) following CQRS pattern.
 * 
 * Architecture: ADR-001 (Modular CQRS)
 * Pattern: Command pattern
 * 
 * Examples:
 * - CreateCustomerCommand
 * - PostJournalEntryCommand
 * - ApproveInvoiceCommand
 */
interface Command {
    /**
     * Unique identifier for command tracing and idempotency.
     * Should be generated once per command instance.
     */
    val commandId: String
    
    /**
     * Timestamp when command was created (ISO-8601).
     */
    val timestamp: String
    
    /**
     * Tenant identifier for multi-tenancy (ADR-005).
     */
    val tenantId: String
    
    /**
     * User who initiated the command.
     */
    val userId: String
}

/**
 * Query Interface
 * 
 * Marker interface for all queries in the system.
 * Queries represent read operations (no state changes) following CQRS pattern.
 * 
 * Architecture: ADR-001 (Modular CQRS)
 * 
 * Examples:
 * - GetCustomerByIdQuery
 * - GetTrialBalanceQuery
 * - SearchInvoicesQuery
 */
interface Query<out R> {
    /**
     * Tenant identifier for multi-tenancy (ADR-005).
     */
    val tenantId: String
    
    /**
     * Optional user context for authorization.
     */
    val userId: String?
}

/**
 * CommandHandler Interface
 * 
 * Processes commands and returns results.
 * Each command should have exactly one handler.
 * 
 * @param C The command type
 * @param R The result type
 */
interface CommandHandler<in C : Command, out R> {
    /**
     * Handles the command and returns a result.
     * 
     * @param command The command to handle
     * @return Result of command execution
     * @throws BusinessException if business rule violated
     */
    suspend fun handle(command: C): R
}

/**
 * QueryHandler Interface
 * 
 * Processes queries and returns results.
 * Queries should not modify state.
 * 
 * @param Q The query type
 * @param R The result type
 */
interface QueryHandler<in Q : Query<R>, out R> {
    /**
     * Handles the query and returns a result.
     * 
     * @param query The query to handle
     * @return Query result
     */
    suspend fun handle(query: Q): R
}
