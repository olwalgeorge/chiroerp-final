package com.chiroerp.identity.core.infrastructure.sso

import com.chiroerp.identity.core.domain.model.ExternalIdentity
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.IdentityProviderGateway
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class LdapIdentityProvider : IdentityProviderGateway {
    private val identities = ConcurrentHashMap<String, ExternalIdentity>()

    override fun resolve(
        tenantId: TenantId,
        provider: IdentityProvider,
        subject: String,
    ): ExternalIdentity? {
        if (provider != IdentityProvider.LDAP) {
            return null
        }
        return identities[keyFor(tenantId, subject)]
    }

    override fun link(
        tenantId: TenantId,
        userId: UserId,
        externalIdentity: ExternalIdentity,
    ) {
        if (externalIdentity.provider != IdentityProvider.LDAP) {
            return
        }

        identities[keyFor(tenantId, externalIdentity.subject)] = externalIdentity.copy(
            claims = externalIdentity.claims + ("linkedUserId" to userId.value.toString()),
        )
    }

    private fun keyFor(tenantId: TenantId, subject: String): String =
        "${tenantId.value}:${subject.trim()}"
}
