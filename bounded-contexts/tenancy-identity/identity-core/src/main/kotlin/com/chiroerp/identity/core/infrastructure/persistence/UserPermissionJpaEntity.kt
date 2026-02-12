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
    name = "identity_permissions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_identity_permissions_user_object",
            columnNames = ["user_id", "object_id"],
        ),
    ],
)
class UserPermissionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity? = null

    @Column(name = "object_id", nullable = false, length = 120)
    var objectId: String? = null

    @Column(name = "actions_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var actionsJson: String = "[]"

    @Column(name = "constraints_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var constraintsJson: String = "{}"

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}
