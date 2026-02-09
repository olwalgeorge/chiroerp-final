package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantTier
import org.eclipse.microprofile.config.inject.ConfigProperty
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TenantIsolationService(
    @ConfigProperty(name = "chiroerp.tenancy.isolation-strategy", defaultValue = "AUTO")
    private val configuredIsolationStrategy: String,
) {
    companion object {
        /**
         * PostgreSQL identifier limit is 63 bytes. Reserve space for prefix and hash suffix.
         * Format: tenant_<slug>_<hash> where hash is 8 chars, prefix is 7 chars ("tenant_")
         * Max slug length = 63 - 7 (prefix) - 9 (underscore + 8-char hash) = 47
         */
        private const val MAX_SLUG_LENGTH = 47
        private const val IDENTIFIER_HASH_LENGTH = 8
    }

    fun buildProvisioningPlan(tenant: Tenant): TenantIsolationPlan {
        val isolationLevel = resolveIsolationLevel(tenant.tier)
        val resourceSlug = resourceSlug(tenant)

        return when (isolationLevel) {
            IsolationLevel.DISCRIMINATOR -> TenantIsolationPlan(
                isolationLevel = isolationLevel,
                schemaName = null,
                databaseName = null,
                requiresTenantDiscriminator = true,
                routingKey = "shared-row-level",
            )

            IsolationLevel.SCHEMA -> TenantIsolationPlan(
                isolationLevel = isolationLevel,
                schemaName = "tenant_$resourceSlug",
                databaseName = null,
                requiresTenantDiscriminator = false,
                routingKey = "schema:tenant_$resourceSlug",
            )

            IsolationLevel.DATABASE -> TenantIsolationPlan(
                isolationLevel = isolationLevel,
                schemaName = "public",
                databaseName = "tenant_$resourceSlug",
                requiresTenantDiscriminator = false,
                routingKey = "database:tenant_$resourceSlug",
            )
        }
    }

    fun resolveIsolationLevel(tier: TenantTier): IsolationLevel {
        val configured = parseConfiguredIsolationLevel()
        if (configured != null) {
            return configured
        }

        return when (tier) {
            TenantTier.STANDARD -> IsolationLevel.DISCRIMINATOR
            TenantTier.PREMIUM -> IsolationLevel.SCHEMA
            TenantTier.ENTERPRISE -> IsolationLevel.DATABASE
        }
    }

    private fun parseConfiguredIsolationLevel(): IsolationLevel? {
        val raw = configuredIsolationStrategy.trim().uppercase()
        if (raw.isBlank() || raw == "AUTO") {
            return null
        }

        return runCatching { IsolationLevel.valueOf(raw) }
            .getOrElse {
                throw IllegalArgumentException(
                    "Unsupported tenancy isolation strategy '$configuredIsolationStrategy'. " +
                        "Use AUTO, DISCRIMINATOR, SCHEMA, or DATABASE.",
                )
            }
    }

    /**
     * Generates a safe, unique resource slug for schema/database naming.
     * 
     * Constraints:
     * - PostgreSQL identifiers max 63 bytes
     * - Must be lowercase alphanumeric with underscores only
     * - Includes hash suffix for collision resistance when truncated
     * - Uses tenant ID as entropy source for hash
     */
    private fun resourceSlug(tenant: Tenant): String {
        val source = tenant.domain.ifBlank { tenant.id.value.toString() }
        val normalized = source.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        val baseSlug = normalized.ifBlank { tenant.id.value.toString().replace("-", "_") }
        
        // Always append a hash suffix for collision resistance
        val hashSuffix = generateHashSuffix(tenant.id.value.toString())
        
        return if (baseSlug.length > MAX_SLUG_LENGTH) {
            // Truncate and append hash for uniqueness
            "${baseSlug.take(MAX_SLUG_LENGTH)}_$hashSuffix"
        } else {
            "${baseSlug}_$hashSuffix"
        }
    }
    
    private fun generateHashSuffix(tenantIdString: String): String {
        // Use first 8 chars of tenant ID hash for collision resistance
        val hash = tenantIdString.hashCode().toUInt().toString(16).padStart(8, '0')
        return hash.take(IDENTIFIER_HASH_LENGTH)
    }
}

data class TenantIsolationPlan(
    val isolationLevel: IsolationLevel,
    val schemaName: String?,
    val databaseName: String?,
    val requiresTenantDiscriminator: Boolean,
    val routingKey: String,
)
