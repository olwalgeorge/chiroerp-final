package com.chiroerp.identity.core.domain.model

enum class IdentityProvider {
    LOCAL,
    SAML,
    OIDC,
    LDAP;

    companion object {
        fun from(raw: String): IdentityProvider = values().firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
            ?: throw IllegalArgumentException("Unsupported identity provider: $raw")
    }
}
