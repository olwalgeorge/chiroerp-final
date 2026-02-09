package com.chiroerp.tenancy.core.application.query

data class GetTenantByDomainQuery(
    val domain: String,
) {
    init {
        require(domain.isNotBlank()) { "Tenant domain is required" }
    }

    val normalizedDomain: String = normalize(domain)

    companion object {
        private val DOMAIN_REGEX = Regex("^[a-z0-9.-]+$")

        fun normalize(rawDomain: String): String {
            val normalized = rawDomain.trim().lowercase()
            require(normalized.isNotBlank()) { "Tenant domain is required" }
            require(DOMAIN_REGEX.matches(normalized)) {
                "Tenant domain must contain only lowercase letters, digits, dots, and hyphens"
            }
            return normalized
        }
    }
}
