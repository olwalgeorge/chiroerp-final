package com.chiroerp.tenancy.core.infrastructure.outbox

import io.quarkus.scheduler.Scheduled
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TenantOutboxRelayWorker(
    private val tenantOutboxRelayService: TenantOutboxRelayService,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.enabled", defaultValue = "true")
    private val enabled: Boolean,
) {
    private val logger = Logger.getLogger(TenantOutboxRelayWorker::class.java)

    @Scheduled(
        every = "{chiroerp.messaging.tenant-events.outbox.poll-interval:5s}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    fun relay() {
        if (!enabled) {
            return
        }

        val dispatched = tenantOutboxRelayService.relayBatch()
        if (dispatched > 0) {
            logger.debugf("Outbox relay processed %d event(s)", dispatched)
        }
    }
}
