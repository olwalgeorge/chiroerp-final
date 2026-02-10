package com.chiroerp.identity.core.domain.port

import com.chiroerp.identity.core.domain.model.ExternalIdentity
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId

interface IdentityProviderGateway {
    fun resolve(
        tenantId: TenantId,
        provider: IdentityProvider,
        subject: String,
    ): ExternalIdentity?

    fun link(
        tenantId: TenantId,
        userId: UserId,
        externalIdentity: ExternalIdentity,
    )
}
