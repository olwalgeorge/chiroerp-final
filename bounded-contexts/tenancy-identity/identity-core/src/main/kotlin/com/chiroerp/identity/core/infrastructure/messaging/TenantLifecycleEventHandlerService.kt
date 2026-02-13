package com.chiroerp.identity.core.infrastructure.messaging

import com.chiroerp.identity.core.application.command.ActivateUserCommand
import com.chiroerp.identity.core.application.command.CreateUserCommand
import com.chiroerp.identity.core.application.command.LockUserCommand
import com.chiroerp.identity.core.application.command.RoleAssignment
import com.chiroerp.identity.core.application.exception.UserAlreadyExistsException
import com.chiroerp.identity.core.application.exception.UserLifecycleException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import com.chiroerp.identity.core.application.handler.UserCommandHandler
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.identity.core.infrastructure.security.PasswordEncoder
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

@ApplicationScoped
class TenantLifecycleEventHandlerService(
    private val userCommandHandler: UserCommandHandler,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.enabled",
        defaultValue = "true",
    )
    private val bootstrapAdminEnabled: Boolean,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.email-prefix",
        defaultValue = "admin",
    )
    private val bootstrapAdminEmailPrefix: String,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.first-name",
        defaultValue = "Tenant",
    )
    private val bootstrapAdminFirstName: String,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.last-name",
        defaultValue = "Admin",
    )
    private val bootstrapAdminLastName: String,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.locale",
        defaultValue = "en-US",
    )
    private val bootstrapAdminLocale: String,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.time-zone-id",
        defaultValue = "UTC",
    )
    private val bootstrapAdminTimeZoneId: String,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.bootstrap-admin.role-code",
        defaultValue = "TENANT_ADMIN",
    )
    private val bootstrapAdminRoleCode: String,
    @param:ConfigProperty(
        name = "chiroerp.messaging.tenancy-events.user-sync.batch-size",
        defaultValue = "200",
    )
    private val userSyncBatchSize: Int,
) : TenantLifecycleEventHandler {
    private val logger = Logger.getLogger(TenantLifecycleEventHandlerService::class.java)

    override fun handle(event: TenantLifecycleEvent) {
        when (event.eventType.uppercase(Locale.ROOT)) {
            TENANT_CREATED -> handleTenantCreated(event)
            TENANT_ACTIVATED -> activatePendingUsers(TenantId(event.tenantId))
            TENANT_SUSPENDED -> lockTenantUsers(
                tenantId = TenantId(event.tenantId),
                reason = extractReason(event, defaultReason = "Tenant suspended"),
            )
            TENANT_TERMINATED -> lockTenantUsers(
                tenantId = TenantId(event.tenantId),
                reason = extractReason(event, defaultReason = "Tenant terminated"),
            )
            else -> logger.debugf("No-op tenancy lifecycle event type=%s", event.eventType)
        }
    }

    private fun handleTenantCreated(event: TenantLifecycleEvent) {
        if (!bootstrapAdminEnabled) {
            logger.debugf("Bootstrap admin creation disabled; skipping tenant=%s", event.tenantId)
            return
        }

        val tenantId = event.tenantId
        val email = resolveBootstrapAdminEmail(event)
        val command = CreateUserCommand(
            tenantId = tenantId,
            firstName = bootstrapAdminFirstName.trim().ifBlank { "Tenant" },
            lastName = bootstrapAdminLastName.trim().ifBlank { "Admin" },
            email = email,
            passwordHash = passwordEncoder.hash(generateBootstrapPassword(event)),
            locale = parseLocale(bootstrapAdminLocale),
            timeZoneId = parseZoneId(bootstrapAdminTimeZoneId),
            roles = setOf(
                RoleAssignment(
                    code = bootstrapAdminRoleCode.trim().ifBlank { "TENANT_ADMIN" },
                    description = "Bootstrap tenant administrator role",
                ),
            ),
        )

        runCatching { userCommandHandler.handle(command) }
            .onSuccess {
                logger.infof(
                    "Bootstrap tenant admin created for tenant=%s email=%s",
                    tenantId,
                    email,
                )
            }
            .onFailure { ex ->
                when (ex) {
                    is UserAlreadyExistsException -> logger.debugf(
                        "Bootstrap tenant admin already exists for tenant=%s email=%s",
                        tenantId,
                        email,
                    )
                    else -> throw ex
                }
            }
    }

    private fun activatePendingUsers(tenantId: TenantId) {
        val batchSize = normalizedBatchSize()
        var activated = 0

        while (true) {
            val pendingUsers = userRepository.listByTenant(
                tenantId = tenantId,
                limit = batchSize,
                offset = 0,
                status = UserStatus.PENDING,
            )
            if (pendingUsers.isEmpty()) {
                break
            }

            var batchActivated = 0
            pendingUsers.forEach { user ->
                runCatching {
                    userCommandHandler.handle(
                        ActivateUserCommand(
                            userId = user.id.value,
                            tenantId = tenantId.value,
                        ),
                    )
                }.onSuccess {
                    activated += 1
                    batchActivated += 1
                }.onFailure { ex ->
                    when (ex) {
                        is UserNotFoundException,
                        is UserLifecycleException,
                        -> logger.debugf(
                            "Skipping user activation for tenant=%s user=%s (%s)",
                            tenantId.value,
                            user.id.value,
                            ex.message ?: "n/a",
                        )
                        else -> throw ex
                    }
                }
            }

            if (batchActivated == 0) {
                logger.warnf(
                    "Tenant activation sync made no progress for tenant=%s; aborting current event handling pass",
                    tenantId.value,
                )
                break
            }
        }

        logger.infof("Tenant activation sync completed tenant=%s activatedUsers=%d", tenantId.value, activated)
    }

    private fun lockTenantUsers(tenantId: TenantId, reason: String) {
        val statuses = listOf(UserStatus.ACTIVE, UserStatus.PENDING)
        val batchSize = normalizedBatchSize()
        var locked = 0

        statuses.forEach { status ->
            while (true) {
                val users = userRepository.listByTenant(
                    tenantId = tenantId,
                    limit = batchSize,
                    offset = 0,
                    status = status,
                )
                if (users.isEmpty()) {
                    break
                }

                var batchLocked = 0
                users.forEach { user ->
                    runCatching {
                        userCommandHandler.handle(
                            LockUserCommand(
                                tenantId = tenantId.value,
                                userId = user.id.value,
                                reason = reason,
                            ),
                        )
                    }.onSuccess {
                        locked += 1
                        batchLocked += 1
                    }.onFailure { ex ->
                        when (ex) {
                            is UserNotFoundException,
                            is UserLifecycleException,
                            -> logger.debugf(
                                "Skipping user lock for tenant=%s user=%s (%s)",
                                tenantId.value,
                                user.id.value,
                                ex.message ?: "n/a",
                            )
                            else -> throw ex
                        }
                    }
                }

                if (batchLocked == 0) {
                    logger.warnf(
                        "Tenant lock sync made no progress for tenant=%s status=%s; aborting current event handling pass",
                        tenantId.value,
                        status.name,
                    )
                    break
                }
            }
        }

        logger.infof("Tenant lock sync completed tenant=%s lockedUsers=%d", tenantId.value, locked)
    }

    private fun extractReason(event: TenantLifecycleEvent, defaultReason: String): String {
        val fromPayload = event.payload.path("reason").asText("").trim()
        return if (fromPayload.isNotEmpty()) {
            fromPayload
        } else {
            "$defaultReason (eventId=${event.eventId})"
        }
    }

    private fun resolveBootstrapAdminEmail(event: TenantLifecycleEvent): String {
        val prefix = bootstrapAdminEmailPrefix.trim().ifBlank { "admin" }
        val domain = event.payload.path("domain").asText("").trim().lowercase(Locale.ROOT)
        if (EMAIL_DOMAIN_REGEX.matches(domain)) {
            return "$prefix@$domain"
        }
        return "$prefix+${event.tenantId}@tenant.local"
    }

    private fun generateBootstrapPassword(event: TenantLifecycleEvent): String {
        val suffix = event.eventId.toString().replace("-", "").take(10)
        return "Tmp!${suffix}aA9"
    }

    private fun parseLocale(raw: String): Locale {
        val normalized = raw.trim().replace('_', '-')
        val parsed = Locale.forLanguageTag(normalized)
        return if (parsed.language.isNullOrBlank()) Locale.US else parsed
    }

    private fun parseZoneId(raw: String): String {
        val normalized = raw.trim()
        if (normalized.isBlank()) {
            return "UTC"
        }
        return runCatching { ZoneId.of(normalized).id }.getOrElse { "UTC" }
    }

    private fun normalizedBatchSize(): Int = userSyncBatchSize.coerceIn(1, 1000)

    companion object {
        private const val TENANT_CREATED = "TENANTCREATED"
        private const val TENANT_ACTIVATED = "TENANTACTIVATED"
        private const val TENANT_SUSPENDED = "TENANTSUSPENDED"
        private const val TENANT_TERMINATED = "TENANTTERMINATED"
        private val EMAIL_DOMAIN_REGEX = Regex("^[a-z0-9.-]+\\.[a-z]{2,}$")
    }
}
