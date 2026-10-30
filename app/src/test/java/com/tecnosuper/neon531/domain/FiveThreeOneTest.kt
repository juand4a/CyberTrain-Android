package com.tecnosuper.neon531.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FiveThreeOneTest {

    @Test
    fun squatMatchesReferenceTableAcrossThreeWeeks() {
        val week1 = FiveThreeOne.sets(135.0, 1, 1.0)
        assertEquals(listOf(88.0, 101.0, 115.0), week1.map { it.weightKg })
        assertEquals(listOf("5", "5", "5+"), week1.map { it.targetReps })

        val week2 = FiveThreeOne.sets(135.0, 2, 1.0)
        assertEquals(listOf(95.0, 108.0, 122.0), week2.map { it.weightKg })
        assertEquals(listOf("3", "3", "3+"), week2.map { it.targetReps })

        val week3 = FiveThreeOne.sets(135.0, 3, 1.0)
        assertEquals(listOf(101.0, 115.0, 128.0), week3.map { it.weightKg })
        assertEquals(listOf("5", "3", "1+"), week3.map { it.targetReps })
    }

    @Test
    fun benchMatchesReferenceTable() {
        val week1 = FiveThreeOne.sets(97.0, 1, 1.0)
        assertEquals(listOf(63.0, 73.0, 82.0), week1.map { it.weightKg })

        val week2 = FiveThreeOne.sets(97.0, 2, 1.0)
        assertEquals(listOf(68.0, 78.0, 87.0), week2.map { it.weightKg })

        val week3 = FiveThreeOne.sets(97.0, 3, 1.0)
        assertEquals(listOf(73.0, 82.0, 92.0), week3.map { it.weightKg })
    }

    @Test
    fun deadliftMatchesReferenceTableIncludingExactWeekThreeLastRep() {
        val week1 = FiveThreeOne.sets(175.0, 1, 1.0)
        assertEquals(listOf(114.0, 131.0, 149.0), week1.map { it.weightKg })

        val week2 = FiveThreeOne.sets(175.0, 2, 1.0)
        assertEquals(listOf(123.0, 140.0, 158.0), week2.map { it.weightKg })

        val week3 = FiveThreeOne.sets(
            trainingMax = 175.0,
            week = 3,
            rounding = 1.0,
            deadliftWeek3ExactPhoto = true
        )
        assertEquals(listOf(131.0, 149.0, 166.0), week3.map { it.weightKg })
        assertEquals("1", week3.last().targetReps)
        assertFalse(week3.last().isAmrap)
    }

    @Test
    fun mainSetsCarryTrainingMaxPercentageForUiAndNotifications() {
        val sets = FiveThreeOne.sets(100.0, 1, 1.0)
        assertEquals(listOf(65, 75, 85), sets.map { it.percentage })
        assertTrue(sets.last().isAmrap)
    }
}
