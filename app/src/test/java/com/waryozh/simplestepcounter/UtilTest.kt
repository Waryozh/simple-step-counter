package com.waryozh.simplestepcounter

import com.waryozh.simplestepcounter.util.calculateDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilTest {
    @Test
    fun calculateDistance_zeroSteps() {
        assertEquals(0, calculateDistance(0, 1))
    }

    @Test
    fun calculateDistance_zeroLength() {
        assertEquals(0, calculateDistance(1, 0))
    }

    @Test
    fun calculateDistance_normalParameters() {
        assertEquals(1400, calculateDistance(2000, 70))
    }

    @Test
    fun calculateDistance_minimalPositiveStepsAndLength() {
        assertEquals(0, calculateDistance(1, 1))
    }

    @Test
    fun calculateDistance_stepsOverflow() {
        assertEquals(Int.MAX_VALUE, calculateDistance(Int.MAX_VALUE, 150))
    }
}
