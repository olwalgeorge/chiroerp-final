package com.chiroerp.tenancy.core.infrastructure.persistence

import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional

@ApplicationScoped
class TenantJpaRepository(
    private val entityManager: EntityManager,
    private val tenantMapper: TenantMapper,
) : TenantRepository {
    @Transactional
    override fun save(tenant: Tenant): Tenant {
        val existing = entityManager.find(TenantJpaEntity::class.java, tenant.id.value)
        val entity = tenantMapper.toEntity(tenant, existing)

        val managed = if (existing == null) {
            entityManager.persist(entity)
            entity
        } else {
            entityManager.merge(entity)
        }

        entityManager.flush()
        return tenantMapper.toDomain(managed)
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun findById(id: TenantId): Tenant? {
        val entity = entityManager.createQuery(
            "select t from TenantJpaEntity t left join fetch t.settings where t.id = :id",
            TenantJpaEntity::class.java,
        )
            .setParameter("id", id.value)
            .resultList
            .firstOrNull()

        return entity?.let(tenantMapper::toDomain)
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun findByDomain(domain: String): Tenant? {
        val entity = entityManager.createQuery(
            "select t from TenantJpaEntity t left join fetch t.settings where t.domain = :domain",
            TenantJpaEntity::class.java,
        )
            .setParameter("domain", domain.trim().lowercase())
            .resultList
            .firstOrNull()

        return entity?.let(tenantMapper::toDomain)
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun findAll(limit: Int, offset: Int, status: TenantStatus?): List<Tenant> {
        val query = if (status == null) {
            entityManager.createQuery(
                "select t from TenantJpaEntity t left join fetch t.settings order by t.createdAt desc",
                TenantJpaEntity::class.java,
            )
        } else {
            entityManager.createQuery(
                "select t from TenantJpaEntity t left join fetch t.settings where t.status = :status order by t.createdAt desc",
                TenantJpaEntity::class.java,
            ).setParameter("status", status.name)
        }

        return query
            .setFirstResult(offset)
            .setMaxResults(limit)
            .resultList
            .map(tenantMapper::toDomain)
    }
}
