package com.tecnosuper.neon531.domain

import kotlin.math.max
import kotlin.math.floor

object OneRepMax {
    fun epley(weight: Double, reps: Int): Double {
        if (weight <= 0 || reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1.0 + reps / 30.0)
    }

    fun brzycki(weight: Double, reps: Int): Double {
        if (weight <= 0 || reps <= 0) return 0.0
        if (reps == 1) return weight
        val safeReps = reps.coerceAtMost(36)
        return weight * 36.0 / (37.0 - safeReps)
    }

    fun estimated(weight: Double, reps: Int): Double {
        if (weight <= 0 || reps <= 0) return 0.0
        return (epley(weight, reps) + brzycki(weight, reps)) / 2.0
    }

    fun trainingMax(weight: Double, reps: Int, percentage: Double = 0.90): Double =
        estimated(weight, reps) * percentage

    fun percentTable(oneRm: Double): List<Pair<Int, Double>> =
        listOf(100, 95, 90, 85, 80, 75, 70, 65, 60, 55, 50).map { pct ->
            pct to roundTo(oneRm * pct / 100.0, 0.5)
        }

    fun estimatedWeightForReps(oneRm: Double, reps: Int): Double {
        if (oneRm <= 0 || reps <= 0) return 0.0
        return max(0.0, oneRm / (1.0 + reps / 30.0))
    }

    fun roundTo(value: Double, increment: Double): Double {
        if (increment <= 0) return value
        // Gym plates use conventional half-up rounding. kotlin.math.round uses
        // bankers rounding at .5, which turned 94.5 into 94 and 122.5 into 122.
        // The reference 5/3/1 table expects 95 and 123 respectively.
        val units = value / increment
        return floor(units + 0.500000001) * increment
    }
}
