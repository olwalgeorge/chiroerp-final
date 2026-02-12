package com.chiroerp.identity.core.infrastructure.persistence

import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantContextHolder
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.util.Locale

@DefaultBean
@ApplicationScoped
class UserJpaRepository(
    private val entityManager: EntityManager,
    private val userMapper: UserMapper,
) : UserRepository {
    @Transactional
    override fun save(user: User): User {
        return withTenantScope(user.tenantId) {
            val existing = entityManager.find(UserJpaEntity::class.java, user.id.value)
            val entity = userMapper.toEntity(user, existing)

            val managed = if (existing == null) {
                entityManager.persist(entity)
                entity
            } else {
                entityManager.merge(entity)
            }

            entityManager.flush()
            userMapper.toDomain(managed)
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun findById(id: UserId): User? {
        return withTenantScope(TenantContextHolder.get()?.tenantId) {
            val entity = entityManager.createQuery(
                """
                select distinct u
                from UserJpaEntity u
                left join fetch u.roles
                left join fetch u.permissions
                left join fetch u.externalIdentities
                where u.id = :id
                """.trimIndent(),
                UserJpaEntity::class.java,
            )
                .setParameter("id", id.value)
                .resultList
                .firstOrNull()

            entity?.let(userMapper::toDomain)
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun findByEmail(tenantId: TenantId, email: String): User? {
        return withTenantScope(tenantId) {
            val normalizedEmail = email.trim().lowercase(Locale.ROOT)
            val entity = entityManager.createQuery(
                """
                select distinct u
                from UserJpaEntity u
                left join fetch u.roles
                left join fetch u.permissions
                left join fetch u.externalIdentities
                where u.tenantId = :tenantId
                  and u.email = :email
                """.trimIndent(),
                UserJpaEntity::class.java,
            )
                .setParameter("tenantId", tenantId.value)
                .setParameter("email", normalizedEmail)
                .resultList
                .firstOrNull()

            entity?.let(userMapper::toDomain)
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun findByExternalIdentity(
        tenantId: TenantId,
        provider: IdentityProvider,
        subject: String,
    ): User? {
        return withTenantScope(tenantId) {
            val normalizedSubject = subject.trim()
            val entity = entityManager.createQuery(
                """
                select distinct u
                from UserJpaEntity u
                left join fetch u.roles
                left join fetch u.permissions
                left join fetch u.externalIdentities ext
                where u.tenantId = :tenantId
                  and ext.provider = :provider
                  and ext.subject = :subject
                """.trimIndent(),
                UserJpaEntity::class.java,
            )
                .setParameter("tenantId", tenantId.value)
                .setParameter("provider", provider.name)
                .setParameter("subject", normalizedSubject)
                .resultList
                .firstOrNull()

            entity?.let(userMapper::toDomain)
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun listByTenant(
        tenantId: TenantId,
        limit: Int,
        offset: Int,
        status: UserStatus?,
    ): List<User> {
        return withTenantScope(tenantId) {
            val clampedLimit = limit.coerceIn(1, 500)
            val normalizedOffset = offset.coerceAtLeast(0)

            val query = if (status == null) {
                entityManager.createQuery(
                    """
                    select u
                    from UserJpaEntity u
                    where u.tenantId = :tenantId
                    order by u.createdAt desc
                    """.trimIndent(),
                    UserJpaEntity::class.java,
                ).setParameter("tenantId", tenantId.value)
            } else {
                entityManager.createQuery(
                    """
                    select u
                    from UserJpaEntity u
                    where u.tenantId = :tenantId
                      and u.status = :status
                    order by u.createdAt desc
                    """.trimIndent(),
                    UserJpaEntity::class.java,
                )
                    .setParameter("tenantId", tenantId.value)
                    .setParameter("status", status.name)
            }

            query
                .setFirstResult(normalizedOffset)
                .setMaxResults(clampedLimit)
                .resultList
                .map(userMapper::toDomain)
        }
    }

    private fun <T> withTenantScope(tenantId: TenantId?, operation: () -> T): T {
        if (tenantId == null) {
            return operation()
        }

        setTenantContext(tenantId)
        return try {
            operation()
        } finally {
            clearTenantContext()
        }
    }

    private fun setTenantContext(tenantId: TenantId) {
        entityManager.createNativeQuery("SELECT set_config('app.current_tenant_id', :tenantId, true)")
            .setParameter("tenantId", tenantId.value.toString())
            .singleResult
    }

    private fun clearTenantContext() {
        entityManager.createNativeQuery("SELECT set_config('app.current_tenant_id', '', true)")
            .singleResult
    }
}
