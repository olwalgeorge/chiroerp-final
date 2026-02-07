package com.chiroerp.tenancy.core.application.command

import com.chiroerp.shared.types.cqrs.Command
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantTier

data class CreateTenantCommand(
    val name: String,
    val domain: String,
    val tier: TenantTier,
    val isolationLevel: IsolationLevel,
) : Command
