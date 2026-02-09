package com.chiroerp.tenancy.core.application.handler

import com.chiroerp.tenancy.core.application.command.ActivateTenantCommand
import com.chiroerp.tenancy.core.application.command.CreateTenantCommand
import com.chiroerp.tenancy.core.application.command.SuspendTenantCommand
import com.chiroerp.tenancy.core.application.command.TerminateTenantCommand
import com.chiroerp.tenancy.core.application.command.UpdateTenantSettingsCommand
import com.chiroerp.tenancy.core.application.exception.TenantAlreadyExistsException
import com.chiroerp.tenancy.core.application.exception.TenantLifecycleTransitionException
import com.chiroerp.tenancy.core.application.exception.TenantNotFoundException
import com.chiroerp.tenancy.core.application.exception.TenantProvisioningException
import com.chiroerp.tenancy.core.application.service.TenantProvisioningService
import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.core.domain.port.TenantEventPublisher
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class TenantCommandHandler(
    private val tenantRepository: TenantRepository,
    private val tenantEventPublisher: TenantEventPublisher,
    private val tenantProvisioningService: TenantProvisioningService,
) {
    /**
     * Creates a new tenant in PENDING status, provisions infrastructure, and activates if successful.
     * 
     * Lifecycle: PENDING -> provision -> ACTIVE (or stays PENDING if provisioning fails)
     */
    @Transactional
    fun handle(command: CreateTenantCommand): Tenant {
        val normalizedDomain = command.domain.trim().lowercase()
        tenantRepository.findByDomain(normalizedDomain)?.let {
            throw TenantAlreadyExistsException(normalizedDomain)
        }

        val tenant = Tenant.create(
            name = command.name,
            domain = normalizedDomain,
            tier = command.tier,
            dataResidency = DataResidency(command.dataResidencyCountry.uppercase()),
            settings = TenantSettings(
                locale = command.locale,
                timezone = command.timezone,
                currency = command.currency,
                featureFlags = command.featureFlags,
                customConfiguration = command.customConfiguration,
            ),
        )

        // Provision infrastructure - tenant stays PENDING if this fails
        val provisioningResult = try {
            tenantProvisioningService.provision(tenant)
        } catch (ex: Exception) {
            Log.errorf(ex, "Provisioning failed for tenant %s", tenant.id.value)
            throw TenantProvisioningException(
                tenantId = tenant.id,
                reason = "Provisioning failed: ${ex.message}",
            )
        }
        
        // Save tenant first (in PENDING status)
        val saved = tenantRepository.save(tenant)
        publish(tenant.pullDomainEvents())
        
        // Activate if provisioning was successful
        if (provisioningResult.readyForActivation) {
            saved.activate()
            tenantRepository.save(saved)
            publish(saved.pullDomainEvents())
            Log.infof("Tenant %s created and activated successfully", saved.id.value)
        } else {
            Log.warnf(
                "Tenant %s created in PENDING status - provisioning incomplete",
                saved.id.value,
            )
        }
        
        return saved
    }

    @Transactional
    fun handle(command: UpdateTenantSettingsCommand): Tenant {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: throw TenantNotFoundException(command.tenantId)

        val mergedSettings = TenantSettings(
            locale = command.locale ?: tenant.settings.locale,
            timezone = command.timezone ?: tenant.settings.timezone,
            currency = command.currency ?: tenant.settings.currency,
            featureFlags = command.featureFlags ?: tenant.settings.featureFlags,
            customConfiguration = command.customConfiguration ?: tenant.settings.customConfiguration,
        )

        tenant.updateSettings(mergedSettings)

        val saved = tenantRepository.save(tenant)
        publish(tenant.pullDomainEvents())
        return saved
    }

    @Transactional
    fun handle(command: ActivateTenantCommand): Tenant = mutateTenant(command.tenantId) {
        it.activate()
    }

    @Transactional
    fun handle(command: SuspendTenantCommand): Tenant = mutateTenant(command.tenantId) {
        it.suspend(command.reason)
    }

    @Transactional
    fun handle(command: TerminateTenantCommand): Tenant = mutateTenant(command.tenantId) {
        it.terminate(command.reason)
    }

    private fun mutateTenant(tenantId: TenantId, operation: (Tenant) -> Unit): Tenant {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw TenantNotFoundException(tenantId)

        try {
            operation(tenant)
        } catch (ex: IllegalStateException) {
            throw TenantLifecycleTransitionException(
                tenantId = tenantId,
                reason = ex.message ?: "Invalid lifecycle transition",
            )
        }

        val saved = tenantRepository.save(tenant)
        publish(tenant.pullDomainEvents())
        return saved
    }

    private fun publish(events: List<TenantDomainEvent>) {
        if (events.isNotEmpty()) {
            tenantEventPublisher.publish(events)
        }
    }
}
