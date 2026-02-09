package com.chiroerp.tenancy.core.application.handler

import com.chiroerp.tenancy.core.application.command.CreateTenantCommand
import com.chiroerp.tenancy.core.application.command.UpdateTenantSettingsCommand
import com.chiroerp.tenancy.core.application.exception.TenantAlreadyExistsException
import com.chiroerp.tenancy.core.application.exception.TenantNotFoundException
import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.core.domain.port.TenantEventPublisher
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class TenantCommandHandler(
    private val tenantRepository: TenantRepository,
    private val tenantEventPublisher: TenantEventPublisher,
) {
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

        val saved = tenantRepository.save(tenant)
        publish(saved.pullDomainEvents())
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
        publish(saved.pullDomainEvents())
        return saved
    }

    private fun publish(events: List<TenantDomainEvent>) {
        if (events.isNotEmpty()) {
            tenantEventPublisher.publish(events)
        }
    }
}
