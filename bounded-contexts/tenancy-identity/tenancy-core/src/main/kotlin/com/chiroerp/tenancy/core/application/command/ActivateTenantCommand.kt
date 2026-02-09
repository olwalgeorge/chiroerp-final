package com.chiroerp.tenancy.core.application.command

import com.chiroerp.tenancy.shared.TenantId

data class ActivateTenantCommand(
    val tenantId: TenantId,
)
