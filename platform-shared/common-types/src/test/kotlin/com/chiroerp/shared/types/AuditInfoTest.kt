package com.chiroerp.shared.types

import com.chiroerp.shared.types.primitives.AuditInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AuditInfoTest {
    @Test
    fun `should update updater metadata`() {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val initial = AuditInfo(createdAt = createdAt, createdBy = "system")

        val updated = initial.updated(by = "admin", at = updatedAt)

        assertEquals("admin", updated.updatedBy)
        assertEquals(updatedAt, updated.updatedAt)
        assertEquals("system", updated.createdBy)
        assertEquals(createdAt, updated.createdAt)
    }
}
