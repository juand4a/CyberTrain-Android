package com.tecnosuper.neon531.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OneRepMaxTest {
    @Test
    fun oneRepReturnsSameWeight() {
        assertEquals(100.0, OneRepMax.estimated(100.0, 1), 0.001)
    }

    @Test
    fun estimatedRmIncreasesWithMultipleReps() {
        assertTrue(OneRepMax.estimated(100.0, 5) > 100.0)
    }

    @Test
    fun roundToUsesGymHalfUpInsteadOfBankersRounding() {
        assertEquals(95.0, OneRepMax.roundTo(94.5, 1.0), 0.001)
        assertEquals(123.0, OneRepMax.roundTo(122.5, 1.0), 0.001)
    }
}
