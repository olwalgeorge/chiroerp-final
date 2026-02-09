package com.chiroerp.tenancy.core.application.command

import com.chiroerp.tenancy.shared.TenantId

data class SuspendTenantCommand(
    val tenantId: TenantId,
    val reason: String,
) {
    init {
        require(reason.isNotBlank()) { "Suspension reason is required" }
    }
}
