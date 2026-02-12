package com.chiroerp.tenancy.shared

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.UUID

class TenantContextTest {
    @AfterEach
    fun teardown() {
        TenantContextHolder.clear()
    }

    @Test
    fun `require throws when tenant context is missing`() {
        assertThat(TenantContextHolder.get()).isNull()

        assertThatThrownBy { TenantContextHolder.require() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Tenant context is not set")
    }

    @Test
    fun `set get and clear manage thread local tenant context`() {
        val tenantContext = TenantContext(
            tenantId = TenantId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
            isolationLevel = IsolationLevel.SCHEMA,
        )

        TenantContextHolder.set(tenantContext)

        assertThat(TenantContextHolder.get()).isEqualTo(tenantContext)
        assertThat(TenantContextHolder.require()).isEqualTo(tenantContext)

        TenantContextHolder.clear()

        assertThat(TenantContextHolder.get()).isNull()
    }
}
