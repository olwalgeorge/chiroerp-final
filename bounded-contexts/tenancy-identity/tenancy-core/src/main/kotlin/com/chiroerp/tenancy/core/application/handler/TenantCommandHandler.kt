package com.chiroerp.tenancy.core.application.handler

import com.chiroerp.shared.types.results.DomainError
import com.chiroerp.shared.types.results.Result
import com.chiroerp.tenancy.core.application.command.ActivateTenantCommand
import com.chiroerp.tenancy.core.application.command.CreateTenantCommand
import com.chiroerp.tenancy.core.application.command.SuspendTenantCommand
import com.chiroerp.tenancy.core.application.command.TerminateTenantCommand
import com.chiroerp.tenancy.core.application.command.UpdateTenantSettingsCommand
import com.chiroerp.tenancy.core.application.service.TenantIsolationService
import com.chiroerp.tenancy.core.application.service.TenantProvisioningService
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantQuota
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.core.domain.model.TenantSubscription
import com.chiroerp.tenancy.core.domain.port.TenantEventPublisher
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId

class TenantCommandHandler(
    private val tenantRepository: TenantRepository,
    private val tenantEventPublisher: TenantEventPublisher,
    private val provisioningService: TenantProvisioningService,
    private val isolationService: TenantIsolationService,
) {
    fun handle(command: CreateTenantCommand): Result<TenantId> {
        if (tenantRepository.findByDomain(command.domain) != null) {
            return Result.failure(SimpleDomainError("TENANT_DOMAIN_EXISTS", "Tenant domain already exists"))
        }

        val tenant = Tenant.create(
            id = TenantId.random(),
            name = command.name,
            domain = command.domain,
            tier = command.tier,
            isolationLevel = command.isolationLevel,
            settings = TenantSettings(),
            subscription = TenantSubscription(command.tier, maxUsers = 25, maxCompanyCodes = 1),
            quota = TenantQuota(maxStorageMb = 10240, maxApiRequestsPerMinute = 1200),
            dataResidency = DataResidency(region = "us-east-1", legalBoundary = "US"),
        )

        isolationService.initializeIsolation(tenant)
        provisioningService.provision(tenant)

        val saved = tenantRepository.save(tenant)
        tenantEventPublisher.publish(saved.pullDomainEvents())

        return Result.success(saved.id)
    }

    fun handle(command: ActivateTenantCommand): Result<Unit> {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: return notFound(command.tenantId)

        tenant.activate()
        tenantRepository.save(tenant)
        tenantEventPublisher.publish(tenant.pullDomainEvents())
        return Result.success(Unit)
    }

    fun handle(command: SuspendTenantCommand): Result<Unit> {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: return notFound(command.tenantId)

        tenant.suspend(command.reason)
        tenantRepository.save(tenant)
        tenantEventPublisher.publish(tenant.pullDomainEvents())
        return Result.success(Unit)
    }

    fun handle(command: TerminateTenantCommand): Result<Unit> {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: return notFound(command.tenantId)

        tenant.terminate(command.reason)
        tenantRepository.save(tenant)
        tenantEventPublisher.publish(tenant.pullDomainEvents())
        return Result.success(Unit)
    }

    fun handle(command: UpdateTenantSettingsCommand): Result<Unit> {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: return notFound(command.tenantId)

        tenant.updateSettings(command.settings)
        tenantRepository.save(tenant)
        tenantEventPublisher.publish(tenant.pullDomainEvents())
        return Result.success(Unit)
    }

    private fun notFound(tenantId: TenantId): Result<Unit> {
        return Result.failure(SimpleDomainError("TENANT_NOT_FOUND", "Tenant $tenantId was not found"))
    }

    private data class SimpleDomainError(
        override val code: String,
        override val message: String,
    ) : DomainError
}
