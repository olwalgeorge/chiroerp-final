package com.chiroerp.tenancy.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TenantIdTest {
    @Test
    fun `should parse tenant id from string`() {
        val id = TenantId.from("11111111-1111-1111-1111-111111111111")

        assertEquals("11111111-1111-1111-1111-111111111111", id.toString())
    }
}
