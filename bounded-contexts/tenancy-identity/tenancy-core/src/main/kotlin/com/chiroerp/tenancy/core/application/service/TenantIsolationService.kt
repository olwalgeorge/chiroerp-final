package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.domain.model.Tenant

interface TenantIsolationService {
    fun initializeIsolation(tenant: Tenant)
}
