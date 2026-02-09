package com.chiroerp.tenancy.shared

data class TenantContext(
    val tenantId: TenantId,
    val isolationLevel: IsolationLevel = IsolationLevel.SCHEMA,
)
