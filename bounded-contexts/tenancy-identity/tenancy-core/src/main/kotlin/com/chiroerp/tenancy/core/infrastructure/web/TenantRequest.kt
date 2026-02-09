package com.chiroerp.tenancy.core.infrastructure.api

import com.chiroerp.tenancy.core.application.command.CreateTenantCommand
import com.chiroerp.tenancy.core.application.command.SuspendTenantCommand
import com.chiroerp.tenancy.core.application.command.TerminateTenantCommand
import com.chiroerp.tenancy.core.application.command.UpdateTenantSettingsCommand
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantTier
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateTenantRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    @field:NotBlank
    @field:Size(max = 150)
    @field:Pattern(regexp = "^[a-z0-9.-]+$")
    val domain: String,
    val tier: TenantTier = TenantTier.STANDARD,
    @field:Pattern(regexp = "^[A-Z]{2}$")
    val dataResidencyCountry: String = "US",
    @field:NotBlank
    val locale: String = "en_US",
    @field:NotBlank
    val timezone: String = "UTC",
    @field:NotBlank
    @field:Size(max = 10)
    val currency: String = "USD",
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val customConfiguration: Map<String, String> = emptyMap(),
) {
    fun toCommand(): CreateTenantCommand = CreateTenantCommand(
        name = name,
        domain = domain,
        tier = tier,
        dataResidencyCountry = dataResidencyCountry,
        locale = locale,
        timezone = timezone,
        currency = currency,
        featureFlags = featureFlags,
        customConfiguration = customConfiguration,
    )
}

data class UpdateTenantSettingsRequest(
    val locale: String? = null,
    val timezone: String? = null,
    val currency: String? = null,
    val featureFlags: Map<String, Boolean>? = null,
    val customConfiguration: Map<String, String>? = null,
) {
    fun toCommand(tenantId: TenantId): UpdateTenantSettingsCommand = UpdateTenantSettingsCommand(
        tenantId = tenantId,
        locale = locale,
        timezone = timezone,
        currency = currency,
        featureFlags = featureFlags,
        customConfiguration = customConfiguration,
    )
}

data class SuspendTenantRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val reason: String,
) {
    fun toCommand(tenantId: TenantId): SuspendTenantCommand = SuspendTenantCommand(
        tenantId = tenantId,
        reason = reason,
    )
}

data class TerminateTenantRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val reason: String,
) {
    fun toCommand(tenantId: TenantId): TerminateTenantCommand = TerminateTenantCommand(
        tenantId = tenantId,
        reason = reason,
    )
}
