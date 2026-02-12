package com.chiroerp.identity.core.infrastructure.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.hibernate.annotations.ColumnTransformer
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "identity_users",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_identity_users_tenant_email",
            columnNames = ["tenant_id", "email"],
        ),
    ],
)
class UserJpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID? = null

    @Column(name = "first_name", nullable = false, length = 120)
    var firstName: String? = null

    @Column(name = "last_name", nullable = false, length = 120)
    var lastName: String? = null

    @Column(name = "email", nullable = false, length = 320)
    var email: String? = null

    @Column(name = "phone_number", length = 60)
    var phoneNumber: String? = null

    @Column(name = "locale", nullable = false, length = 20)
    var locale: String = "en-US"

    @Column(name = "time_zone_id", nullable = false, length = 80)
    var timeZoneId: String = "UTC"

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String? = null

    @Column(name = "password_version", nullable = false)
    var passwordVersion: Int = 1

    @Column(name = "password_changed_at", nullable = false)
    var passwordChangedAt: Instant? = null

    @Column(name = "password_history_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var passwordHistoryJson: String = "[]"

    @Column(name = "must_change_password", nullable = false)
    var mustChangePassword: Boolean = false

    @Column(name = "password_expires_at")
    var passwordExpiresAt: Instant? = null

    @Column(name = "status", nullable = false, length = 20)
    var status: String = "PENDING"

    @Column(name = "mfa_methods_json", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var mfaMethodsJson: String? = null

    @Column(name = "mfa_shared_secret", length = 255)
    var mfaSharedSecret: String? = null

    @Column(name = "mfa_backup_codes_json", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var mfaBackupCodesJson: String? = null

    @Column(name = "mfa_enrolled_at")
    var mfaEnrolledAt: Instant? = null

    @Column(name = "mfa_verified_at")
    var mfaVerifiedAt: Instant? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0

    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.EAGER,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var roles: MutableSet<UserRoleJpaEntity> = linkedSetOf()

    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.EAGER,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var permissions: MutableSet<UserPermissionJpaEntity> = linkedSetOf()

    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.EAGER,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var externalIdentities: MutableSet<UserExternalIdentityJpaEntity> = linkedSetOf()
}
