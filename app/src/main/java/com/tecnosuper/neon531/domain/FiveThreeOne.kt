package com.tecnosuper.neon531.domain

import com.tecnosuper.neon531.data.WorkoutSetState
import java.util.Locale

/**
 * 5/3/1 prescription used by this app.
 *
 * Week 1: 65% x 5, 75% x 5, 85% x 5+
 * Week 2: 70% x 3, 80% x 3, 90% x 3+
 * Week 3: 75% x 5, 85% x 3, 95% x 1+
 *
 * The user reference has the last deadlift set in week 3 as an exact 1,
 * so the factory can request that special case while squat/bench remain 1+.
 */
object FiveThreeOne {

    data class Prescription(
        val percentage: Int,
        val reps: String,
        val amrap: Boolean
    )

    private val weeks = mapOf(
        1 to listOf(
            Prescription(65, "5", false),
            Prescription(75, "5", false),
            Prescription(85, "5+", true)
        ),
        2 to listOf(
            Prescription(70, "3", false),
            Prescription(80, "3", false),
            Prescription(90, "3+", true)
        ),
        3 to listOf(
            Prescription(75, "5", false),
            Prescription(85, "3", false),
            Prescription(95, "1+", true)
        ),
        4 to listOf(
            Prescription(40, "5", false),
            Prescription(50, "5", false),
            Prescription(60, "5", false)
        )
    )

    fun prescription(week: Int): List<Prescription> =
        weeks[week] ?: weeks.getValue(1)

    fun schemeLabel(week: Int): String =
        prescription(week).joinToString(" / ") { it.reps }

    fun sets(
        trainingMax: Double,
        week: Int,
        rounding: Double = 1.0,
        deadliftWeek3ExactPhoto: Boolean = false
    ): List<WorkoutSetState> {
        val template = prescription(week)
        return template.mapIndexed { index, prescription ->
            val weight = OneRepMax.roundTo(
                trainingMax * (prescription.percentage / 100.0),
                rounding
            )
            val exactDeadliftLast = deadliftWeek3ExactPhoto && week == 3 && index == 2
            WorkoutSetState(
                weightKg = weight,
                weightLabel = formatKg(weight),
                targetReps = if (exactDeadliftLast) "1" else prescription.reps,
                isAmrap = if (exactDeadliftLast) false else prescription.amrap,
                percentage = prescription.percentage
            )
        }
    }

    private fun formatKg(value: Double): String {
        val shown = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            "%.1f".format(Locale.US, value)
        }
        return "$shown kg"
    }
}
