package com.tecnosuper.neon531.domain

import kotlin.math.round

object PlateCalculator {
    private val plates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25, 1.0, 0.5)

    data class Result(
        val target: Double,
        val bar: Double,
        val perSide: List<Double>,
        val achieved: Double,
        val difference: Double
    )

    fun calculate(target: Double, bar: Double = 20.0): Result {
        var side = ((target - bar).coerceAtLeast(0.0)) / 2.0
        val selected = mutableListOf<Double>()
        for (plate in plates) {
            while (side + 0.0001 >= plate) {
                selected += plate
                side -= plate
            }
        }
        val achieved = bar + selected.sum() * 2.0
        return Result(
            target = target,
            bar = bar,
            perSide = selected,
            achieved = round(achieved * 100) / 100,
            difference = round((target - achieved) * 100) / 100
        )
    }
}
