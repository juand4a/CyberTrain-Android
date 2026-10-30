package com.tecnosuper.neon531.domain

import com.tecnosuper.neon531.data.ExerciseTemplate
import com.tecnosuper.neon531.data.FiveThreeOneSource
import com.tecnosuper.neon531.data.MainLift
import com.tecnosuper.neon531.data.Manual531Week
import com.tecnosuper.neon531.data.RoutineDayTemplate
import com.tecnosuper.neon531.data.TrainingMaxes
import com.tecnosuper.neon531.data.WorkoutExerciseState
import com.tecnosuper.neon531.data.WorkoutMode
import com.tecnosuper.neon531.data.WorkoutSessionState
import com.tecnosuper.neon531.data.WorkoutSetState
import java.util.Locale

object RoutineFactory {

    val defaultDayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")

    fun createSession(
        mode: WorkoutMode,
        dayIndex: Int,
        dayName: String,
        week: Int = 1,
        trainingMaxes: TrainingMaxes = TrainingMaxes(),
        rounding: Double = 1.0,
        templates: List<ExerciseTemplate>
    ): WorkoutSessionState {
        val exercises = templates.map { template ->
            val normal = template.toWorkoutExercise()

            if (mode != WorkoutMode.STRENGTH || template.mainLift == null) {
                normal
            } else {
                normal.copy(
                    sets = strengthSetsForTemplate(
                        template = template,
                        week = week,
                        trainingMaxes = trainingMaxes,
                        rounding = rounding
                    ),
                    restSeconds = 300,
                    isMain531 = true
                )
            }
        }

        return WorkoutSessionState(
            mode = mode,
            dayIndex = dayIndex,
            dayName = dayName,
            week = if (mode == WorkoutMode.STRENGTH) week else null,
            exercises = exercises,
            currentExerciseIndex = exercises.indexOfFirst { !it.isCompleted }.coerceAtLeast(0)
        )
    }

    /**
     * Returns the exact three work sets that will be used for a 5/3/1 main lift.
     * AUTO calculates from the Training Max.
     * MANUAL uses the values stored inside the routine editor for the selected week.
     * If a manual week is incomplete, AUTO is used as a safe fallback so a workout
     * can never start with blank/zero loads by accident.
     */
    fun strengthSetsForTemplate(
        template: ExerciseTemplate,
        week: Int,
        trainingMaxes: TrainingMaxes,
        rounding: Double
    ): List<WorkoutSetState> {
        val lift = template.mainLift ?: return template.toWorkoutExercise().sets

        if (template.fiveThreeOneSource == FiveThreeOneSource.MANUAL) {
            val manual = manualWeekSets(template.manual531Weeks, week)
            if (manual != null) return manual
        }

        val trainingMax = when (lift) {
            MainLift.SQUAT -> trainingMaxes.squat
            MainLift.BENCH -> trainingMaxes.bench
            MainLift.DEADLIFT -> trainingMaxes.deadlift
        }

        return FiveThreeOne.sets(
            trainingMax = trainingMax,
            week = week,
            rounding = rounding,
            deadliftWeek3ExactPhoto = lift == MainLift.DEADLIFT
        )
    }

    private fun manualWeekSets(
        weeks: List<Manual531Week>,
        week: Int
    ): List<WorkoutSetState>? {
        val manualWeek = weeks.firstOrNull { it.week == week } ?: return null
        if (manualWeek.sets.size < 3) return null

        val firstThree = manualWeek.sets.take(3)
        if (firstThree.any { it.weightKg == null || it.weightKg <= 0.0 || it.reps.isBlank() }) {
            return null
        }

        return firstThree.map { set ->
            val kg = set.weightKg ?: 0.0
            WorkoutSetState(
                weightKg = kg,
                weightLabel = formatKg(kg),
                targetReps = set.reps.trim(),
                isAmrap = set.reps.trim().endsWith("+"),
                percentage = null
            )
        }
    }

    fun defaultRoutineDays(): List<RoutineDayTemplate> = listOf(
        RoutineDayTemplate(
            dayIndex = 0,
            dayName = "Lunes · Pierna",
            exercises = listOf(
                template("d1-squat", "Sentadilla", "95 kg", 95.0, 3, "5", 300, MainLift.SQUAT),
                template("d1-rdl", "Peso muerto rumano", "120 kg", 120.0, 3, "7", 180),
                template("d1-legpress", "Prensa unilateral", "90 kg", 90.0, 3, "8", 180),
                template("d1-extension", "Extensión de cuádriceps", "55 kg", 55.0, 4, "8", 180),
                template("d1-adductor", "Adductor", "Max Power", null, 4, "12", 120),
                template("d1-calf", "Pantorrilla sentado", "50 kg", 50.0, 4, "10", 120)
            )
        ),
        RoutineDayTemplate(
            dayIndex = 1,
            dayName = "Martes · Pecho",
            exercises = listOf(
                template("d2-bench", "Press plano", "70 kg", 70.0, 4, "8", 300, MainLift.BENCH),
                template("d2-incline", "Press inclinado", "33 kg", 33.0, 3, "7", 300),
                template("d2-cross", "Aperturas crossover", "15 kg", 15.0, 3, "12", 180),
                template("d2-highcross", "Crossover alto", "20 kg", 20.0, 3, "12", 180),
                template("d2-lateral", "Elevaciones laterales", "10 kg", 10.0, 4, "12", 180),
                template("d2-extension", "Extensión", "60 kg", 60.0, 3, "8", 180)
            )
        ),
        RoutineDayTemplate(
            dayIndex = 2,
            dayName = "Miércoles · Espalda",
            exercises = listOf(
                template("d3-pulldown", "Jalón al pecho", "-2 plaquitas", null, 4, "10", 360),
                template("d3-sealrow", "Seal row", "35 kg", 35.0, 4, "7", 300),
                template("d3-row", "Remo uni. polea", "55 kg", 55.0, 3, "8", 180),
                template("d3-pullover", "Pull over", "50 kg", 50.0, 4, "8", 180),
                template("d3-hammer", "Martillo", "12.5 kg", 12.5, 3, "15", 180),
                template("d3-reversecurl", "Curl invertido", "25 kg", 25.0, 4, "8", 180)
            )
        ),
        RoutineDayTemplate(
            dayIndex = 3,
            dayName = "Jueves · Pierna",
            exercises = listOf(
                template("d4-deadlift", "Peso muerto", "135 kg", 135.0, 3, "5", 300, MainLift.DEADLIFT),
                template("d4-goodmorning", "Buenos días", "40 kg", 40.0, 3, "8", 180),
                template("d4-legcurl", "Curl femoral uni", "90 kg", 90.0, 4, "8", 180),
                template("d4-extension", "Extensión de cuádriceps", "55 kg", 55.0, 4, "8", 180),
                template("d4-abductor", "Abductor", "Max Power", null, 3, "15", 120),
                template("d4-calfhack", "Pantorrilla en Hack", "70 kg", 70.0, 4, "8", 120)
            )
        ),
        RoutineDayTemplate(
            dayIndex = 4,
            dayName = "Viernes · Hombro / Brazos",
            exercises = listOf(
                template("d5-ohp", "Press militar", "50 kg", 50.0, 3, "8", 300),
                template("d5-reardelt", "Hombro posterior", "12 kg", 12.0, 4, "12", 180),
                template("d5-french", "Press francés", "17.5 kg", 17.5, 4, "8", 180),
                template("d5-extension", "Extensión uni", "20 kg", 20.0, 3, "10", 180),
                template("d5-preacher", "Predicador unilateral", "15 kg", 15.0, 4, "10", 180),
                template("d5-barcurl", "Curl con barra", "30 kg", 30.0, 3, "10", 180)
            )
        )
    )

    private fun ExerciseTemplate.toWorkoutExercise(): WorkoutExerciseState = WorkoutExerciseState(
        id = id,
        name = name,
        restSeconds = restSeconds,
        sets = List(series.coerceAtLeast(1)) {
            WorkoutSetState(
                weightKg = weightKg,
                weightLabel = weightLabel,
                targetReps = reps
            )
        }
    )

    private fun template(
        id: String,
        name: String,
        weightLabel: String,
        weightKg: Double?,
        series: Int,
        reps: String,
        restSeconds: Int,
        mainLift: MainLift? = null
    ) = ExerciseTemplate(
        id = id,
        name = name,
        weightLabel = weightLabel,
        weightKg = weightKg,
        series = series,
        reps = reps,
        restSeconds = restSeconds,
        mainLift = mainLift
    )

    private fun formatKg(value: Double): String {
        val shown = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            "%.1f".format(Locale.US, value)
        }
        return "$shown kg"
    }
}
