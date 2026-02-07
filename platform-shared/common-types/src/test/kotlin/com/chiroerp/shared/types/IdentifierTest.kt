package com.chiroerp.shared.types

import com.chiroerp.shared.types.primitives.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdentifierTest {
    @Test
    fun `should wrap raw identifier value`() {
        val identifier = Identifier("tenant-001")

        assertEquals("tenant-001", identifier.value)
    }
}
