package com.tecnosuper.neon531.domain

import com.tecnosuper.neon531.data.ExerciseTemplate
import com.tecnosuper.neon531.data.FiveThreeOneSource
import com.tecnosuper.neon531.data.MainLift
import com.tecnosuper.neon531.data.Manual531Set
import com.tecnosuper.neon531.data.Manual531Week
import com.tecnosuper.neon531.data.TrainingMaxes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineFactoryManual531Test {

    @Test
    fun manualWeekUsesExactUserWeightsAndReps() {
        val exercise = ExerciseTemplate(
            id = "bench",
            name = "Press plano",
            mainLift = MainLift.BENCH,
            fiveThreeOneSource = FiveThreeOneSource.MANUAL,
            manual531Weeks = listOf(
                Manual531Week(
                    week = 2,
                    sets = listOf(
                        Manual531Set(72.5, "3"),
                        Manual531Set(82.5, "3"),
                        Manual531Set(92.5, "3+")
                    )
                )
            )
        )

        val sets = RoutineFactory.strengthSetsForTemplate(
            template = exercise,
            week = 2,
            trainingMaxes = TrainingMaxes(999.0, 999.0, 999.0),
            rounding = 5.0
        )

        assertEquals(listOf(72.5, 82.5, 92.5), sets.map { it.weightKg })
        assertEquals(listOf("3", "3", "3+"), sets.map { it.targetReps })
        assertNull(sets.first().percentage)
        assertTrue(sets.last().isAmrap)
    }

    @Test
    fun incompleteManualWeekFallsBackToAuto() {
        val exercise = ExerciseTemplate(
            id = "squat",
            name = "Sentadilla",
            mainLift = MainLift.SQUAT,
            fiveThreeOneSource = FiveThreeOneSource.MANUAL,
            manual531Weeks = listOf(
                Manual531Week(
                    week = 1,
                    sets = listOf(
                        Manual531Set(88.0, "5"),
                        Manual531Set(null, "5"),
                        Manual531Set(115.0, "5+")
                    )
                )
            )
        )

        val sets = RoutineFactory.strengthSetsForTemplate(
            template = exercise,
            week = 1,
            trainingMaxes = TrainingMaxes(squat = 135.0),
            rounding = 1.0
        )

        assertEquals(listOf(88.0, 101.0, 115.0), sets.map { it.weightKg })
        assertEquals(listOf(65, 75, 85), sets.map { it.percentage })
    }
}
