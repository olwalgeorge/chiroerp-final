package com.chiroerp.identity.core.infrastructure.outbox

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@ApplicationScoped
class UserOutboxRelayWorker(
    private val userOutboxRelayService: UserOutboxRelayService,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.enabled", defaultValue = "true")
    private val enabled: Boolean,
) {
    private val logger = Logger.getLogger(UserOutboxRelayWorker::class.java)

    @Scheduled(
        every = "{chiroerp.messaging.identity-events.outbox.poll-interval:5s}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    fun relay() {
        if (!enabled) {
            return
        }

        val dispatched = userOutboxRelayService.relayBatch()
        if (dispatched > 0) {
            logger.debugf("Identity outbox relay processed %d event(s)", dispatched)
        }
    }
}
