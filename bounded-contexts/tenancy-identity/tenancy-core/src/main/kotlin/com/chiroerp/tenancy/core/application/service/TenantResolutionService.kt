package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.application.query.GetTenantByDomainQuery
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TenantResolutionService(
    private val tenantRepository: TenantRepository,
) {
    fun resolveByDomain(query: GetTenantByDomainQuery): Tenant? =
        tenantRepository.findByDomain(query.normalizedDomain)

    fun resolveByDomain(domain: String): Tenant? =
        tenantRepository.findByDomain(GetTenantByDomainQuery.normalize(domain))

    fun resolveTenant(
        queryDomain: String?,
        headerTenantId: String?,
        headerTenantDomain: String?,
        hostHeader: String?,
    ): TenantResolutionResult? {
        if (!headerTenantId.isNullOrBlank()) {
            val tenantId = parseTenantId(headerTenantId)
            val tenant = tenantRepository.findById(tenantId) ?: return null
            return TenantResolutionResult(
                tenantId = tenant.id,
                domain = tenant.domain,
                source = TenantResolutionSource.HEADER_TENANT_ID,
            )
        }

        val candidates = listOfNotNull(
            headerTenantDomain?.takeIf { it.isNotBlank() }?.let { it to TenantResolutionSource.HEADER_TENANT_DOMAIN },
            queryDomain?.takeIf { it.isNotBlank() }?.let { it to TenantResolutionSource.QUERY_DOMAIN },
            extractHostDomain(hostHeader)?.let { it to TenantResolutionSource.HOST_HEADER },
        )

        for ((rawDomain, source) in candidates) {
            val normalizedDomain = try {
                GetTenantByDomainQuery.normalize(rawDomain)
            } catch (ex: IllegalArgumentException) {
                if (source == TenantResolutionSource.HOST_HEADER) {
                    continue
                }
                throw ex
            }

            val tenant = tenantRepository.findByDomain(normalizedDomain) ?: continue
            return TenantResolutionResult(
                tenantId = tenant.id,
                domain = tenant.domain,
                source = source,
            )
        }

        return null
    }

    private fun parseTenantId(rawTenantId: String): TenantId {
        val value = rawTenantId.trim()
        return runCatching { TenantId.from(value) }
            .getOrElse { throw IllegalArgumentException("Invalid X-Tenant-ID header: $rawTenantId") }
    }

    private fun extractHostDomain(hostHeader: String?): String? {
        val host = hostHeader?.trim()?.substringBefore(":") ?: return null
        return host.takeIf { it.isNotBlank() }
    }
}

data class TenantResolutionResult(
    val tenantId: TenantId,
    val domain: String,
    val source: TenantResolutionSource,
)

enum class TenantResolutionSource {
    HEADER_TENANT_ID,
    HEADER_TENANT_DOMAIN,
    QUERY_DOMAIN,
    HOST_HEADER,
}
