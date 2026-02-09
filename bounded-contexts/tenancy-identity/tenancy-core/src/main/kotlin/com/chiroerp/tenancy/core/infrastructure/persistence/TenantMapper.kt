package com.chiroerp.tenancy.core.infrastructure.persistence

import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TenantMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toDomain(entity: TenantJpaEntity): Tenant {
        val settingsEntity = entity.settings

        return Tenant.rehydrate(
            id = TenantId(requireNotNull(entity.id)),
            name = requireNotNull(entity.name),
            domain = requireNotNull(entity.domain),
            tier = enumValueOfOrDefault(requireNotNull(entity.tier), TenantTier.STANDARD),
            status = enumValueOfOrDefault(requireNotNull(entity.status), TenantStatus.ACTIVE),
            dataResidency = DataResidency(requireNotNull(entity.dataResidencyCountry)),
            settings = TenantSettings(
                locale = settingsEntity?.locale ?: "en_US",
                timezone = settingsEntity?.timezone ?: "UTC",
                currency = settingsEntity?.currency ?: "USD",
                featureFlags = parseJson(settingsEntity?.featureFlagsJson ?: "{}", BOOLEAN_MAP_TYPE),
                customConfiguration = parseJson(settingsEntity?.customConfigJson ?: "{}", STRING_MAP_TYPE),
            ),
            createdAt = requireNotNull(entity.createdAt),
            updatedAt = requireNotNull(entity.updatedAt),
        )
    }

    fun toEntity(tenant: Tenant, existing: TenantJpaEntity? = null): TenantJpaEntity {
        val entity = existing ?: TenantJpaEntity()
        entity.id = tenant.id.value
        entity.name = tenant.name
        entity.domain = tenant.domain
        entity.tier = tenant.tier.name
        entity.status = tenant.status.name
        entity.dataResidencyCountry = tenant.dataResidency.countryCode
        entity.createdAt = tenant.createdAt
        entity.updatedAt = tenant.updatedAt

        val settingsEntity = entity.settings ?: TenantSettingsJpaEntity()
        settingsEntity.tenant = entity
        settingsEntity.tenantId = tenant.id.value
        settingsEntity.locale = tenant.settings.locale
        settingsEntity.timezone = tenant.settings.timezone
        settingsEntity.currency = tenant.settings.currency
        settingsEntity.featureFlagsJson = objectMapper.writeValueAsString(tenant.settings.featureFlags)
        settingsEntity.customConfigJson = objectMapper.writeValueAsString(tenant.settings.customConfiguration)
        settingsEntity.updatedAt = tenant.updatedAt

        entity.settings = settingsEntity
        return entity
    }

    private fun <T> parseJson(raw: String, typeReference: TypeReference<T>): T {
        return runCatching { objectMapper.readValue(raw, typeReference) }
            .getOrElse { objectMapper.readValue("{}", typeReference) }
    }

    private fun <T : Enum<T>> enumValueOfOrDefault(raw: String, fallback: T): T {
        return runCatching { java.lang.Enum.valueOf(fallback.declaringJavaClass, raw) }
            .getOrDefault(fallback)
    }

    companion object {
        private val BOOLEAN_MAP_TYPE = object : TypeReference<Map<String, Boolean>>() {}
        private val STRING_MAP_TYPE = object : TypeReference<Map<String, String>>() {}
    }
}
