package com.chiroerp.identity.core.infrastructure

import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.infrastructure.persistence.UserJpaEntity
import com.chiroerp.identity.core.infrastructure.persistence.UserJpaRepository
import com.chiroerp.identity.core.infrastructure.persistence.UserMapper
import com.chiroerp.tenancy.shared.TenantId
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

class UserJpaRepositoryTest {
    private val entityManager = mockk<EntityManager>()
    private val userMapper = mockk<UserMapper>()
    private val repository = UserJpaRepository(entityManager, userMapper)

    @Test
    fun `save persists new user when no existing row is found`() {
        val user = sampleUser()
        val entity = UserJpaEntity().apply { id = user.id.value }
        mockTenantContextQueries()

        every { entityManager.find(UserJpaEntity::class.java, user.id.value) } returns null
        every { userMapper.toEntity(user, null) } returns entity
        every { entityManager.persist(entity) } just runs
        every { entityManager.flush() } just runs
        every { userMapper.toDomain(entity) } returns user

        val saved = repository.save(user)

        assertThat(saved).isEqualTo(user)
        verify(exactly = 1) { entityManager.persist(entity) }
        verify(exactly = 0) { entityManager.merge(any<UserJpaEntity>()) }
        verify(exactly = 1) { entityManager.flush() }
    }

    @Test
    fun `save merges existing user when row already exists`() {
        val user = sampleUser()
        val existing = UserJpaEntity().apply { id = user.id.value }
        val merged = UserJpaEntity().apply { id = user.id.value }
        mockTenantContextQueries()

        every { entityManager.find(UserJpaEntity::class.java, user.id.value) } returns existing
        every { userMapper.toEntity(user, existing) } returns existing
        every { entityManager.merge(existing) } returns merged
        every { entityManager.flush() } just runs
        every { userMapper.toDomain(merged) } returns user

        val saved = repository.save(user)

        assertThat(saved).isEqualTo(user)
        verify(exactly = 0) { entityManager.persist(any<UserJpaEntity>()) }
        verify(exactly = 1) { entityManager.merge(existing) }
        verify(exactly = 1) { entityManager.flush() }
    }

    private fun mockTenantContextQueries() {
        val query = mockk<Query>()
        every { entityManager.createNativeQuery(any()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns "ok"
    }

    private fun sampleUser(): User = User.register(
        tenantId = TenantId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        profile = UserProfile(
            firstName = "Jane",
            lastName = "Doe",
            email = "jane.doe@example.com",
            phoneNumber = "+1-555-0101",
            locale = Locale.US,
            timeZone = ZoneId.of("UTC"),
        ),
        passwordHash = "hash-1",
        now = Instant.parse("2026-02-12T10:00:00Z"),
    )
}
