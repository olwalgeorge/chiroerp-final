package com.chiroerp.identity.core.application.service

import com.chiroerp.identity.core.domain.model.ExternalIdentity
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.port.IdentityProviderGateway
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import java.time.Instant
import java.util.UUID

data class SsoAuthenticationRequest(
    val tenantId: UUID,
    val provider: IdentityProvider,
    val subject: String,
)

sealed interface SsoAuthenticationResult {
    data class Success(
        val userId: UUID,
        val tenantId: UUID,
        val provider: IdentityProvider,
        val subject: String,
    ) : SsoAuthenticationResult

    data class Failure(
        val code: String = "SSO_AUTHENTICATION_FAILED",
    ) : SsoAuthenticationResult
}

data class SsoLinkRequest(
    val tenantId: UUID,
    val userId: UUID,
    val provider: IdentityProvider,
    val subject: String,
    val claims: Map<String, String> = emptyMap(),
    val linkedAt: Instant = Instant.now(),
)

sealed interface SsoLinkResult {
    data class Success(
        val userId: UUID,
        val tenantId: UUID,
        val provider: IdentityProvider,
        val subject: String,
    ) : SsoLinkResult

    data class Failure(
        val code: String,
    ) : SsoLinkResult
}

@ApplicationScoped
class SsoIntegrationService(
    private val userRepository: UserRepository,
    private val identityProviderGateways: Instance<IdentityProviderGateway>,
) {
    fun authenticate(request: SsoAuthenticationRequest): SsoAuthenticationResult {
        val normalizedSubject = request.subject.trim()
        if (normalizedSubject.isEmpty()) {
            return SsoAuthenticationResult.Failure(code = "INVALID_SUBJECT")
        }

        val tenantId = TenantId(request.tenantId)

        val externalIdentity = resolveExternalIdentity(tenantId, request.provider, normalizedSubject)
            ?: return SsoAuthenticationResult.Failure()

        val user = userRepository.findByExternalIdentity(
            tenantId = tenantId,
            provider = request.provider,
            subject = externalIdentity.subject,
        ) ?: return SsoAuthenticationResult.Failure()

        if (!user.status.canLogin()) {
            return SsoAuthenticationResult.Failure(code = "USER_NOT_ACTIVE")
        }

        return SsoAuthenticationResult.Success(
            userId = user.id.value,
            tenantId = user.tenantId.value,
            provider = request.provider,
            subject = externalIdentity.subject,
        )
    }

    fun link(request: SsoLinkRequest): SsoLinkResult {
        val normalizedSubject = request.subject.trim()
        if (normalizedSubject.isEmpty()) {
            return SsoLinkResult.Failure(code = "INVALID_SUBJECT")
        }

        val tenantId = TenantId(request.tenantId)
        val user = userRepository.findById(UserId(request.userId))
            ?: return SsoLinkResult.Failure(code = "USER_NOT_FOUND")

        if (user.tenantId != tenantId) {
            return SsoLinkResult.Failure(code = "TENANT_SCOPE_VIOLATION")
        }

        val identity = ExternalIdentity(
            provider = request.provider,
            subject = normalizedSubject,
            linkedAt = request.linkedAt,
            claims = request.claims,
        )

        user.linkExternalIdentity(identity)
        userRepository.save(user)

        identityProviderGateways.forEach { gateway ->
            gateway.link(tenantId = tenantId, userId = user.id, externalIdentity = identity)
        }

        return SsoLinkResult.Success(
            userId = user.id.value,
            tenantId = tenantId.value,
            provider = request.provider,
            subject = normalizedSubject,
        )
    }

    fun resolveExternalIdentity(
        tenantId: TenantId,
        provider: IdentityProvider,
        subject: String,
    ): ExternalIdentity? {
        identityProviderGateways.forEach { gateway ->
            val resolved = gateway.resolve(tenantId = tenantId, provider = provider, subject = subject)
            if (resolved != null) {
                return resolved
            }
        }
        return null
    }
}
