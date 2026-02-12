package com.chiroerp.tenancy.shared

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class TenantIdTest {
    @Test
    fun `random generates unique tenant identifiers`() {
        val first = TenantId.random()
        val second = TenantId.random()

        assertThat(first).isNotEqualTo(second)
        assertThat(first.value).isNotEqualTo(second.value)
    }

    @Test
    fun `from parses uuid string`() {
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        val tenantId = TenantId.from(uuid.toString())

        assertThat(tenantId.value).isEqualTo(uuid)
        assertThat(tenantId.toString()).isEqualTo(uuid.toString())
    }
}
