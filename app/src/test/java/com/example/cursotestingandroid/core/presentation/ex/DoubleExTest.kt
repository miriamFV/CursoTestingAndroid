package com.example.cursotestingandroid.core.presentation.ex

import org.junit.Assert.assertEquals
import org.junit.Test

class DoubleExTest {
    @Test
    fun roundTo2Decimals_roundsCorrectly() {
        assertEquals(13.05, 13.0491.roundTo2Decimals(), 0.0)
    }
}
