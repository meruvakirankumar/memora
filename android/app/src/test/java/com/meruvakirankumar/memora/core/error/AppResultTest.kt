package com.meruvakirankumar.memora.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun `map transforms success value`() {
        val result: AppResult<Int> = 2.asSuccess()

        val mapped = result.map { it * 3 }

        assertEquals(AppResult.Success(6), mapped)
    }

    @Test
    fun `map preserves failure`() {
        val result: AppResult<Int> = AppError.NotUnderstood.asFailure()

        val mapped = result.map { it * 3 }

        assertTrue(mapped is AppResult.Failure)
        assertEquals(AppError.NotUnderstood, (mapped as AppResult.Failure).error)
    }
}
