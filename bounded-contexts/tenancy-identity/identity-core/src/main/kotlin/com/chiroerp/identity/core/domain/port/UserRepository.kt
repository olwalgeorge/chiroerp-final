package com.chiroerp.identity.core.domain.port

import com.chiroerp.identity.core.domain.model.IdentityProvider
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.tenancy.shared.TenantId

interface UserRepository {
    fun save(user: User): User

    fun findById(id: UserId): User?

    fun findByEmail(tenantId: TenantId, email: String): User?

    fun findByExternalIdentity(
        tenantId: TenantId,
        provider: IdentityProvider,
        subject: String,
    ): User?

    fun listByTenant(
        tenantId: TenantId,
        limit: Int = 100,
        offset: Int = 0,
        status: UserStatus? = null,
    ): List<User> = emptyList()
}
