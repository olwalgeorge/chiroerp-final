package com.chiroerp.identity.core.infrastructure.persistence

import com.chiroerp.identity.core.domain.model.ExternalIdentity
import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.MfaConfiguration
import com.chiroerp.identity.core.domain.model.MfaMethod
import com.chiroerp.identity.core.domain.model.Permission
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserCredentials
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.domain.model.UserRole
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.tenancy.shared.TenantId
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import java.time.ZoneId
import java.util.Locale

@ApplicationScoped
class UserMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toDomain(entity: UserJpaEntity): User {
        val profile = UserProfile(
            firstName = requireNotNull(entity.firstName),
            lastName = requireNotNull(entity.lastName),
            email = requireNotNull(entity.email),
            phoneNumber = entity.phoneNumber,
            locale = parseLocale(entity.locale),
            timeZone = parseZoneId(entity.timeZoneId),
        )

        val credentials = UserCredentials(
            hashedPassword = requireNotNull(entity.passwordHash),
            passwordVersion = entity.passwordVersion,
            passwordChangedAt = requireNotNull(entity.passwordChangedAt),
            passwordHistory = parseStringList(entity.passwordHistoryJson),
            mustChangePassword = entity.mustChangePassword,
            expiresAt = entity.passwordExpiresAt,
        )

        val roles = entity.roles.mapNotNull { roleEntity ->
            val roleCode = roleEntity.roleCode?.trim().orEmpty()
            if (roleCode.isBlank()) {
                null
            } else {
                UserRole(
                    code = roleCode,
                    description = roleEntity.description,
                    permissions = parseRolePermissions(roleEntity.permissionsJson).toSet(),
                    sodGroup = roleEntity.sodGroup,
                )
            }
        }.toSet()

        val directPermissions = entity.permissions.mapNotNull { permissionEntity ->
            val objectId = permissionEntity.objectId?.trim().orEmpty()
            if (objectId.isBlank()) {
                null
            } else {
                val actions = parseStringSet(permissionEntity.actionsJson)
                if (actions.isEmpty()) {
                    null
                } else {
                    Permission(
                        objectId = objectId,
                        actions = actions,
                        constraints = parseStringMap(permissionEntity.constraintsJson),
                    )
                }
            }
        }.toSet()

        val mfaMethods = parseStringSet(entity.mfaMethodsJson ?: "[]")
            .mapNotNull { raw -> runCatching { MfaMethod.valueOf(raw) }.getOrNull() }
            .toSet()

        val mfaSharedSecret = entity.mfaSharedSecret?.trim().orEmpty()
        val mfaConfiguration = if (mfaSharedSecret.isNotBlank() && mfaMethods.isNotEmpty()) {
            MfaConfiguration(
                methods = mfaMethods,
                sharedSecret = mfaSharedSecret,
                backupCodes = parseStringSet(entity.mfaBackupCodesJson ?: "[]"),
                enrolledAt = entity.mfaEnrolledAt ?: requireNotNull(entity.updatedAt),
                verifiedAt = entity.mfaVerifiedAt,
            )
        } else {
            null
        }

        val externalIdentities = entity.externalIdentities.mapNotNull { externalIdentityEntity ->
            val provider = externalIdentityEntity.provider?.trim().orEmpty()
            val subject = externalIdentityEntity.subject?.trim().orEmpty()
            if (provider.isBlank() || subject.isBlank()) {
                null
            } else {
                ExternalIdentity(
                    provider = runCatching { IdentityProvider.valueOf(provider) }.getOrElse { IdentityProvider.LOCAL },
                    subject = subject,
                    linkedAt = externalIdentityEntity.linkedAt,
                    claims = parseStringMap(externalIdentityEntity.claimsJson),
                )
            }
        }.toSet()

        return User.rehydrate(
            id = UserId(requireNotNull(entity.id)),
            tenantId = TenantId(requireNotNull(entity.tenantId)),
            profile = profile,
            credentials = credentials,
            roles = roles,
            permissions = directPermissions,
            mfaConfiguration = mfaConfiguration,
            externalIdentities = externalIdentities,
            status = runCatching { UserStatus.valueOf(entity.status) }.getOrElse { UserStatus.PENDING },
            createdAt = requireNotNull(entity.createdAt),
            updatedAt = requireNotNull(entity.updatedAt),
            lastLoginAt = entity.lastLoginAt,
        )
    }

    fun toEntity(user: User, existing: UserJpaEntity? = null): UserJpaEntity {
        val entity = existing ?: UserJpaEntity()

        entity.id = user.id.value
        entity.tenantId = user.tenantId.value
        entity.firstName = user.profile.firstName.trim()
        entity.lastName = user.profile.lastName.trim()
        entity.email = user.profile.normalizedEmail
        entity.phoneNumber = user.profile.phoneNumber
        entity.locale = user.profile.locale.toLanguageTag()
        entity.timeZoneId = user.profile.timeZone.id

        entity.passwordHash = user.credentialsSnapshot.hashedPassword
        entity.passwordVersion = user.credentialsSnapshot.passwordVersion
        entity.passwordChangedAt = user.credentialsSnapshot.passwordChangedAt
        entity.passwordHistoryJson = objectMapper.writeValueAsString(user.credentialsSnapshot.passwordHistory)
        entity.mustChangePassword = user.credentialsSnapshot.mustChangePassword
        entity.passwordExpiresAt = user.credentialsSnapshot.expiresAt

        entity.status = user.status.name
        entity.createdAt = user.createdAt
        entity.updatedAt = user.updatedAt
        entity.lastLoginAt = user.lastLoginAt

        val mfaConfiguration = user.mfaConfigurationSnapshot
        if (mfaConfiguration == null) {
            entity.mfaMethodsJson = null
            entity.mfaSharedSecret = null
            entity.mfaBackupCodesJson = null
            entity.mfaEnrolledAt = null
            entity.mfaVerifiedAt = null
        } else {
            entity.mfaMethodsJson = objectMapper.writeValueAsString(mfaConfiguration.methods.map { it.name }.toSet())
            entity.mfaSharedSecret = mfaConfiguration.sharedSecret
            entity.mfaBackupCodesJson = objectMapper.writeValueAsString(mfaConfiguration.backupCodes)
            entity.mfaEnrolledAt = mfaConfiguration.enrolledAt
            entity.mfaVerifiedAt = mfaConfiguration.verifiedAt
        }

        entity.roles.clear()
        user.assignedRoles
            .sortedBy { it.normalizedCode }
            .forEach { role ->
                val roleEntity = UserRoleJpaEntity()
                roleEntity.user = entity
                roleEntity.roleCode = role.normalizedCode
                roleEntity.description = role.description
                roleEntity.sodGroup = role.sodGroup
                roleEntity.permissionsJson = objectMapper.writeValueAsString(
                    role.permissions.map {
                        PermissionPayload(
                            objectId = it.objectId,
                            actions = it.actions,
                            constraints = it.constraints,
                        )
                    },
                )
                roleEntity.createdAt = user.updatedAt
                entity.roles += roleEntity
            }

        entity.permissions.clear()
        user.directPermissions
            .sortedBy { it.objectId }
            .forEach { permission ->
                val permissionEntity = UserPermissionJpaEntity()
                permissionEntity.user = entity
                permissionEntity.objectId = permission.objectId
                permissionEntity.actionsJson = objectMapper.writeValueAsString(permission.actions)
                permissionEntity.constraintsJson = objectMapper.writeValueAsString(permission.constraints)
                permissionEntity.createdAt = user.updatedAt
                entity.permissions += permissionEntity
            }

        entity.externalIdentities.clear()
        user.linkedIdentities
            .sortedBy { "${it.provider.name}:${it.subject}" }
            .forEach { externalIdentity ->
                val externalIdentityEntity = UserExternalIdentityJpaEntity()
                externalIdentityEntity.user = entity
                externalIdentityEntity.provider = externalIdentity.provider.name
                externalIdentityEntity.subject = externalIdentity.subject
                externalIdentityEntity.claimsJson = objectMapper.writeValueAsString(externalIdentity.claims)
                externalIdentityEntity.linkedAt = externalIdentity.linkedAt
                entity.externalIdentities += externalIdentityEntity
            }

        return entity
    }

    private fun parseLocale(raw: String): Locale {
        val normalized = raw.trim().ifEmpty { "en-US" }.replace('_', '-')
        val parsed = Locale.forLanguageTag(normalized)
        return if (parsed.language.isNullOrBlank()) Locale.US else parsed
    }

    private fun parseZoneId(raw: String): ZoneId = runCatching { ZoneId.of(raw.trim().ifEmpty { "UTC" }) }
        .getOrDefault(ZoneId.of("UTC"))

    private fun parseRolePermissions(raw: String): List<Permission> = runCatching {
        objectMapper.readValue(raw, ROLE_PERMISSION_TYPE)
    }
        .getOrDefault(emptyList())
        .mapNotNull { permissionPayload ->
            runCatching {
                Permission(
                    objectId = permissionPayload.objectId,
                    actions = permissionPayload.actions,
                    constraints = permissionPayload.constraints,
                )
            }.getOrNull()
        }

    private fun parseStringList(raw: String): List<String> = runCatching {
        objectMapper.readValue(raw, STRING_LIST_TYPE)
    }.getOrDefault(emptyList())

    private fun parseStringSet(raw: String): Set<String> = parseStringList(raw)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    private fun parseStringMap(raw: String): Map<String, String> = runCatching {
        objectMapper.readValue(raw, STRING_MAP_TYPE)
    }.getOrDefault(emptyMap())

    private data class PermissionPayload(
        val objectId: String,
        val actions: Set<String>,
        val constraints: Map<String, String> = emptyMap(),
    )

    companion object {
        private val STRING_LIST_TYPE = object : TypeReference<List<String>>() {}
        private val STRING_MAP_TYPE = object : TypeReference<Map<String, String>>() {}
        private val ROLE_PERMISSION_TYPE = object : TypeReference<List<PermissionPayload>>() {}
    }
}
