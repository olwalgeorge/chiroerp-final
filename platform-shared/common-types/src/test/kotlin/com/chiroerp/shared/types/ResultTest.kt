package com.chiroerp.shared.types

import com.chiroerp.shared.types.results.Result
import com.chiroerp.shared.types.results.ValidationError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResultTest {
    @Test
    fun `should map success value`() {
        val result = Result.success(10).map { it * 2 }

        assertEquals(20, result.getOrNull())
    }

    @Test
    fun `should keep failure when mapping`() {
        val result = Result.failure(ValidationError("VALIDATION", "invalid", "amount")).map { 123 }

        assertTrue(result is Result.Failure)
    }
}
