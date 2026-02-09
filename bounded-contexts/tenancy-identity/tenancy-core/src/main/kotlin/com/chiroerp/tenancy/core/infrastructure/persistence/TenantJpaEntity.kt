package com.chiroerp.tenancy.core.infrastructure.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenants")
class TenantJpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "name", nullable = false, length = 200)
    var name: String? = null

    @Column(name = "domain", nullable = false, length = 150, unique = true)
    var domain: String? = null

    @Column(name = "tier", nullable = false, length = 50)
    var tier: String? = null

    @Column(name = "status", nullable = false, length = 50)
    var status: String? = null

    @Column(name = "data_residency_country", nullable = false, length = 2)
    var dataResidencyCountry: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0

    @OneToOne(mappedBy = "tenant", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var settings: TenantSettingsJpaEntity? = null
}
