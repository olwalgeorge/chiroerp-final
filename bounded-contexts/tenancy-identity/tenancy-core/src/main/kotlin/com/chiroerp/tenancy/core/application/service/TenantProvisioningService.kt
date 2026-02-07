package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.domain.model.Tenant

interface TenantProvisioningService {
    fun provision(tenant: Tenant)
}
