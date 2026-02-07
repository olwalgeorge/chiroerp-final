package com.chiroerp.tenancy.core.application.command

import com.chiroerp.shared.types.cqrs.Command
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.shared.TenantId

data class UpdateTenantSettingsCommand(
    val tenantId: TenantId,
    val settings: TenantSettings,
) : Command
