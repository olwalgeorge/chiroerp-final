package com.chiroerp.tenancy.core.application.command

import com.chiroerp.shared.types.cqrs.Command
import com.chiroerp.tenancy.shared.TenantId

data class ActivateTenantCommand(val tenantId: TenantId) : Command
