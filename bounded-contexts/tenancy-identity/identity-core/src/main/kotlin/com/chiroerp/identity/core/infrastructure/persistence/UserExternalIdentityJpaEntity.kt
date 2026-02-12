package com.chiroerp.identity.core.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.ColumnTransformer
import java.time.Instant

@Entity
@Table(
    name = "identity_external_identities",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_identity_external_identity_user_provider_subject",
            columnNames = ["user_id", "provider", "subject"],
        ),
    ],
)
class UserExternalIdentityJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity? = null

    @Column(name = "provider", nullable = false, length = 20)
    var provider: String? = null

    @Column(name = "subject", nullable = false, length = 255)
    var subject: String? = null

    @Column(name = "claims_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var claimsJson: String = "{}"

    @Column(name = "linked_at", nullable = false)
    var linkedAt: Instant = Instant.now()
}
