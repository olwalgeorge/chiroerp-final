package com.chiroerp.tenancy.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TenantContextTest {
    @Test
    fun `should set and clear thread local context`() {
        val tenantContext = TenantContext(
            tenantId = TenantId.from("22222222-2222-2222-2222-222222222222"),
            tier = TenantTier.PREMIUM,
            isolationLevel = IsolationLevel.SCHEMA_LEVEL,
            domain = "acme.example",
        )

        TenantContextHolder.set(tenantContext)
        assertEquals(tenantContext, TenantContextHolder.get())

        TenantContextHolder.clear()
        assertNull(TenantContextHolder.get())
    }
}
