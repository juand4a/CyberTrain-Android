package com.tecnosuper.neon531.data

import kotlinx.serialization.Serializable

@Serializable
enum class WorkoutMode { STRENGTH, HYPERTROPHY }

@Serializable
enum class MainLift { SQUAT, BENCH, DEADLIFT }

@Serializable
enum class FiveThreeOneSource { AUTO, MANUAL }

@Serializable
data class Manual531Set(
    val weightKg: Double? = null,
    val reps: String = "5"
)

@Serializable
data class Manual531Week(
    val week: Int,
    val sets: List<Manual531Set> = listOf(
        Manual531Set(),
        Manual531Set(),
        Manual531Set()
    )
)

@Serializable
data class ExerciseTemplate(
    val id: String,
    val name: String,
    val weightLabel: String = "—",
    val weightKg: Double? = null,
    val series: Int = 3,
    val reps: String = "8",
    val restSeconds: Int = 180,
    val mainLift: MainLift? = null,
    val fiveThreeOneSource: FiveThreeOneSource = FiveThreeOneSource.AUTO,
    val manual531Weeks: List<Manual531Week> = emptyList()
)

@Serializable
data class RoutineDayTemplate(
    val dayIndex: Int,
    val dayName: String,
    val exercises: List<ExerciseTemplate> = emptyList()
)

@Serializable
data class WorkoutSetState(
    val weightKg: Double? = null,
    val weightLabel: String = "—",
    val targetReps: String = "1",
    val completed: Boolean = false,
    val actualReps: Int? = null,
    val isAmrap: Boolean = false,
    /** Percentage of the Training Max when this is a generated 5/3/1 set. */
    val percentage: Int? = null
)

@Serializable
data class WorkoutExerciseState(
    val id: String,
    val name: String,
    val restSeconds: Int = 180,
    val sets: List<WorkoutSetState> = emptyList(),
    val isMain531: Boolean = false
) {
    val isCompleted: Boolean
        get() = sets.isNotEmpty() && sets.all { it.completed }

    val completedSets: Int
        get() = sets.count { it.completed }
}

@Serializable
data class WorkoutSessionState(
    val mode: WorkoutMode,
    val dayIndex: Int,
    val dayName: String,
    val week: Int? = null,
    val exercises: List<WorkoutExerciseState>,
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val restEndsAt: Long? = null,
    val paused: Boolean = false,
    val pausedRestRemainingMs: Long? = null,
    val finished: Boolean = false
) {
    val currentExercise: WorkoutExerciseState?
        get() = exercises.getOrNull(currentExerciseIndex)

    val currentSet: WorkoutSetState?
        get() = currentExercise?.sets?.getOrNull(currentSetIndex)

    val totalSets: Int
        get() = exercises.sumOf { it.sets.size }

    val completedSets: Int
        get() = exercises.sumOf { it.completedSets }
}

@Serializable
data class WorkoutHistoryEntry(
    val completedAt: Long,
    val mode: WorkoutMode,
    val dayName: String,
    val week: Int? = null,
    val totalSets: Int,
    val totalVolumeKg: Double
)

data class TrainingMaxes(
    val squat: Double = 135.0,
    val bench: Double = 97.0,
    val deadlift: Double = 175.0
)
