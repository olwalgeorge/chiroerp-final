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
    name = "identity_user_roles",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_identity_user_roles_user_code",
            columnNames = ["user_id", "role_code"],
        ),
    ],
)
class UserRoleJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity? = null

    @Column(name = "role_code", nullable = false, length = 100)
    var roleCode: String? = null

    @Column(name = "description", nullable = false, length = 255)
    var description: String = ""

    @Column(name = "sod_group", length = 80)
    var sodGroup: String? = null

    @Column(name = "permissions_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var permissionsJson: String = "[]"

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}
