package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.domain.model.Tenant
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger
import java.time.Clock
import java.util.UUID
import jakarta.json.Json

@ApplicationScoped
class TenantAuditLogger(
    private val clock: Clock = Clock.systemUTC(),
) {
    private val logger = Logger.getLogger(TenantAuditLogger::class.java)

    fun logLifecycle(
        action: TenantLifecycleAction,
        tenant: Tenant,
        actor: TenantAuditActor,
        metadata: Map<String, String?> = emptyMap(),
    ) {
        val payload = buildLifecyclePayload(action, tenant, actor, metadata)
        logger.infov("AUDIT {}", payload)
    }

    internal fun buildLifecyclePayload(
        action: TenantLifecycleAction,
        tenant: Tenant,
        actor: TenantAuditActor,
        metadata: Map<String, String?>,
    ): String {
        val rolesArray = Json.createArrayBuilder()
        actor.roles.sorted().forEach { rolesArray.add(it) }

        val builder = Json.createObjectBuilder()
            .add("eventType", "TENANT_LIFECYCLE")
            .add("action", action.name)
            .add("tenantId", tenant.id.value.toString())
            .add("tenantDomain", tenant.domain)
            .add("tenantStatus", tenant.status.name)
            .add("actorId", actor.principalId)
            .add("actorRoles", rolesArray)
            .add("timestamp", clock.instant().toString())

        actor.tenantId?.let { builder.add("actorTenantId", it.toString()) }

        metadata.forEach { (key, value) ->
            if (!value.isNullOrBlank()) {
                builder.add(key, value)
            }
        }

        return builder.build().toString()
    }
}

data class TenantAuditActor(
    val principalId: String,
    val tenantId: UUID?,
    val roles: Set<String>,
)

enum class TenantLifecycleAction {
    CREATE,
    ACTIVATE,
    SUSPEND,
    TERMINATE,
}
