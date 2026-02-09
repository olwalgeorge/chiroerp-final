package com.chiroerp.tenancy.core.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnTransformer
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenant_settings")
class TenantSettingsJpaEntity {
    @Id
    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID? = null

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    lateinit var tenant: TenantJpaEntity

    @Column(name = "locale", nullable = false, length = 20)
    var locale: String = "en_US"

    @Column(name = "timezone", nullable = false, length = 50)
    var timezone: String = "UTC"

    @Column(name = "currency", nullable = false, length = 10)
    var currency: String = "USD"

    @Column(name = "feature_flags", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var featureFlagsJson: String = "{}"

    @Column(name = "custom_config", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var customConfigJson: String = "{}"

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
}
