package com.chiroerp.tenancy.core.application.command

import com.chiroerp.tenancy.shared.TenantId

data class TerminateTenantCommand(
    val tenantId: TenantId,
    val reason: String,
) {
    init {
        require(reason.isNotBlank()) { "Termination reason is required" }
    }
}
