package com.tecnosuper.neon531.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tecnosuper.neon531.domain.RoutineFactory
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "neon531_store")

object WorkoutStore {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mutex = Mutex()

    private val ACTIVE = stringPreferencesKey("active_workout")
    private val HISTORY = stringPreferencesKey("workout_history")
    private val TM_SQUAT = doublePreferencesKey("tm_squat")
    private val TM_BENCH = doublePreferencesKey("tm_bench")
    private val TM_DEADLIFT = doublePreferencesKey("tm_deadlift")
    private val ROUNDING = doublePreferencesKey("rounding")
    private val ROUTINES = stringPreferencesKey("routine_templates")
    private val SOUND_ENABLED = booleanPreferencesKey("rest_sound_enabled")
    private val VIBRATION_ENABLED = booleanPreferencesKey("rest_vibration_enabled")
    private val SOUND_VOLUME = intPreferencesKey("rest_sound_volume")

    fun routines(context: Context): Flow<List<RoutineDayTemplate>> =
        context.dataStore.data.map { prefs -> decodeRoutines(prefs[ROUTINES]) }

    fun activeWorkout(context: Context): Flow<WorkoutSessionState?> =
        context.dataStore.data.map { prefs -> decodeSession(prefs[ACTIVE]) }

    fun history(context: Context): Flow<List<WorkoutHistoryEntry>> =
        context.dataStore.data.map { prefs -> decodeHistory(prefs[HISTORY]) }

    suspend fun loadRoutineDays(context: Context): List<RoutineDayTemplate> =
        decodeRoutines(context.dataStore.data.first()[ROUTINES])

    suspend fun loadTrainingMaxes(context: Context): TrainingMaxes {
        val prefs = context.dataStore.data.first()
        return TrainingMaxes(
            squat = prefs[TM_SQUAT] ?: 135.0,
            bench = prefs[TM_BENCH] ?: 97.0,
            deadlift = prefs[TM_DEADLIFT] ?: 175.0
        )
    }

    suspend fun loadRounding(context: Context): Double =
        context.dataStore.data.first()[ROUNDING] ?: 1.0

    suspend fun loadRestSoundEnabled(context: Context): Boolean =
        context.dataStore.data.first()[SOUND_ENABLED] ?: true

    suspend fun loadRestVibrationEnabled(context: Context): Boolean =
        context.dataStore.data.first()[VIBRATION_ENABLED] ?: true

    suspend fun loadRestSoundVolume(context: Context): Int =
        context.dataStore.data.first()[SOUND_VOLUME]?.coerceIn(0, 10) ?: 10

    suspend fun saveRestAlertSettings(
        context: Context,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        volume: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[SOUND_ENABLED] = soundEnabled
            prefs[VIBRATION_ENABLED] = vibrationEnabled
            prefs[SOUND_VOLUME] = volume.coerceIn(0, 10)
        }
    }

    suspend fun saveTrainingMaxes(
        context: Context,
        squat: Double,
        bench: Double,
        deadlift: Double,
        rounding: Double
    ) {
        context.dataStore.edit { prefs ->
            prefs[TM_SQUAT] = squat.coerceAtLeast(0.0)
            prefs[TM_BENCH] = bench.coerceAtLeast(0.0)
            prefs[TM_DEADLIFT] = deadlift.coerceAtLeast(0.0)
            prefs[ROUNDING] = rounding.takeIf { it > 0.0 } ?: 1.0
        }
    }

    suspend fun updateTrainingMax(
        context: Context,
        lift: MainLift,
        incrementKg: Double
    ) = mutex.withLock {
        val current = loadTrainingMaxes(context)
        val next = when (lift) {
            MainLift.SQUAT -> current.squat + incrementKg
            MainLift.BENCH -> current.bench + incrementKg
            MainLift.DEADLIFT -> current.deadlift + incrementKg
        }
        saveTrainingMaxes(
            context,
            squat = if (lift == MainLift.SQUAT) next else current.squat,
            bench = if (lift == MainLift.BENCH) next else current.bench,
            deadlift = if (lift == MainLift.DEADLIFT) next else current.deadlift,
            rounding = loadRounding(context)
        )
    }

    suspend fun applyCycleTrainingMaxes(
        context: Context,
        squat: Double,
        bench: Double,
        deadlift: Double,
        rounding: Double
    ) = saveTrainingMaxes(context, squat, bench, deadlift, rounding)

    suspend fun renameRoutineDay(context: Context, dayIndex: Int, newName: String) = mutex.withLock {
        val name = newName.trim()
        if (name.isBlank()) return@withLock
        val routines = loadRoutineDays(context).toMutableList()
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition < 0) return@withLock
        routines[dayPosition] = routines[dayPosition].copy(dayName = name)
        saveRoutines(context, routines)
    }

    suspend fun upsertRoutineExercise(context: Context, dayIndex: Int, exercise: ExerciseTemplate) = mutex.withLock {
        val routines = loadRoutineDays(context).toMutableList()
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition < 0) return@withLock
        val day = routines[dayPosition]
        val exercises = day.exercises.toMutableList()
        val position = exercises.indexOfFirst { it.id == exercise.id }
        val normalized = exercise.copy(
            id = exercise.id.ifBlank { UUID.randomUUID().toString() },
            name = exercise.name.trim().ifBlank { "Ejercicio" },
            weightLabel = exercise.weightLabel.trim().ifBlank {
                exercise.weightKg?.let { "${cleanNumber(it)} kg" } ?: "—"
            },
            series = exercise.series.coerceAtLeast(1),
            reps = exercise.reps.trim().ifBlank { "1" },
            restSeconds = exercise.restSeconds.coerceAtLeast(0),
            mainLift = exercise.mainLift
        )
        if (position >= 0) exercises[position] = normalized else exercises.add(normalized)
        routines[dayPosition] = day.copy(exercises = exercises)
        saveRoutines(context, routines)
    }

    suspend fun deleteRoutineExercise(context: Context, dayIndex: Int, exerciseId: String) = mutex.withLock {
        val routines = loadRoutineDays(context).toMutableList()
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition < 0) return@withLock
        val day = routines[dayPosition]
        routines[dayPosition] = day.copy(exercises = day.exercises.filterNot { it.id == exerciseId })
        saveRoutines(context, routines)
    }

    suspend fun duplicateRoutineExercise(context: Context, dayIndex: Int, exerciseId: String) = mutex.withLock {
        val routines = loadRoutineDays(context).toMutableList()
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition < 0) return@withLock
        val day = routines[dayPosition]
        val exercises = day.exercises.toMutableList()
        val sourcePosition = exercises.indexOfFirst { it.id == exerciseId }
        if (sourcePosition < 0) return@withLock
        val source = exercises[sourcePosition]
        exercises.add(sourcePosition + 1, source.copy(id = UUID.randomUUID().toString(), name = "${source.name} copia"))
        routines[dayPosition] = day.copy(exercises = exercises)
        saveRoutines(context, routines)
    }

    suspend fun moveRoutineExercise(context: Context, dayIndex: Int, exerciseId: String, delta: Int) = mutex.withLock {
        val routines = loadRoutineDays(context).toMutableList()
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition < 0) return@withLock
        val day = routines[dayPosition]
        val exercises = day.exercises.toMutableList()
        if (exercises.isEmpty()) return@withLock
        val from = exercises.indexOfFirst { it.id == exerciseId }
        if (from < 0) return@withLock
        val to = (from + delta).coerceIn(0, exercises.lastIndex)
        if (from == to) return@withLock
        val item = exercises.removeAt(from)
        exercises.add(to, item)
        routines[dayPosition] = day.copy(exercises = exercises)
        saveRoutines(context, routines)
    }

    suspend fun moveRoutineExerciseToPosition(context: Context, dayIndex: Int, exerciseId: String, targetPosition: Int) = mutex.withLock {
        val routines = loadRoutineDays(context).toMutableList()
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition < 0) return@withLock
        val day = routines[dayPosition]
        val exercises = day.exercises.toMutableList()
        val from = exercises.indexOfFirst { it.id == exerciseId }
        if (from < 0) return@withLock
        val item = exercises.removeAt(from)
        exercises.add(targetPosition.coerceIn(0, exercises.size), item)
        routines[dayPosition] = day.copy(exercises = exercises)
        saveRoutines(context, routines)
    }

    suspend fun resetRoutineDay(context: Context, dayIndex: Int) = mutex.withLock {
        val routines = loadRoutineDays(context).toMutableList()
        val defaultDay = RoutineFactory.defaultRoutineDays().firstOrNull { it.dayIndex == dayIndex } ?: return@withLock
        val dayPosition = routines.indexOfFirst { it.dayIndex == dayIndex }
        if (dayPosition >= 0) routines[dayPosition] = defaultDay else routines.add(defaultDay)
        saveRoutines(context, routines.sortedBy { it.dayIndex })
    }

    suspend fun resetAllRoutines(context: Context) = mutex.withLock { saveRoutines(context, RoutineFactory.defaultRoutineDays()) }

    suspend fun startWorkout(context: Context, mode: WorkoutMode, dayIndex: Int, week: Int) = mutex.withLock {
        val tms = loadTrainingMaxes(context)
        val rounding = loadRounding(context)
        val day = loadRoutineDays(context).firstOrNull { it.dayIndex == dayIndex } ?: return@withLock
        if (day.exercises.isEmpty()) return@withLock
        saveSession(context, RoutineFactory.createSession(mode, dayIndex, day.dayName, week, tms, rounding, day.exercises))
    }

    suspend fun completeCurrentSet(context: Context, actualReps: Int? = null) = update(context) { state ->
        if (state.finished || state.paused) return@update state
        val exercise = state.currentExercise ?: return@update state
        val set = state.currentSet ?: return@update state
        if (set.isAmrap && actualReps == null) return@update state
        val exercises = state.exercises.toMutableList()
        val sets = exercise.sets.toMutableList()
        sets[state.currentSetIndex] = set.copy(completed = true, actualReps = actualReps ?: set.targetReps.filter(Char::isDigit).toIntOrNull())
        exercises[state.currentExerciseIndex] = exercise.copy(sets = sets)
        val nextSetIndex = sets.indexOfFirst { !it.completed }
        if (nextSetIndex >= 0) return@update state.copy(exercises = exercises, currentSetIndex = nextSetIndex, restEndsAt = System.currentTimeMillis() + exercise.restSeconds * 1000L, paused = false, pausedRestRemainingMs = null)
        val nextExerciseIndex = findNextIncompleteExercise(exercises, state.currentExerciseIndex)
        if (nextExerciseIndex == null) {
            val completed = state.copy(exercises = exercises, finished = true, restEndsAt = null, paused = false, pausedRestRemainingMs = null)
            archive(context, completed)
            completed
        } else state.copy(exercises = exercises, currentExerciseIndex = nextExerciseIndex, currentSetIndex = firstIncompleteSet(exercises[nextExerciseIndex]), restEndsAt = System.currentTimeMillis() + exercise.restSeconds * 1000L, paused = false, pausedRestRemainingMs = null)
    }

    suspend fun postponeCurrentOne(context: Context) = postponeCurrentBy(context, 1)
    suspend fun postponeCurrentTwo(context: Context) = postponeCurrentBy(context, 2)

    private suspend fun postponeCurrentBy(context: Context, positions: Int) = update(context) { state ->
        if (state.finished || state.exercises.size < 2 || positions <= 0) return@update state
        val currentIndex = state.currentExerciseIndex
        val exercises = state.exercises.toMutableList()
        val current = exercises.removeAt(currentIndex)
        exercises.add((currentIndex + positions).coerceAtMost(exercises.size), current)
        val candidate = currentIndex.coerceIn(0, exercises.lastIndex)
        val newCurrent = pendingIndexFrom(exercises, candidate, excludedId = current.id) ?: exercises.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
        state.copy(exercises = exercises, currentExerciseIndex = newCurrent, currentSetIndex = firstIncompleteSet(exercises[newCurrent]), restEndsAt = null, paused = false, pausedRestRemainingMs = null)
    }

    suspend fun postponeCurrentToEnd(context: Context) = update(context) { state ->
        if (state.finished || state.exercises.size < 2) return@update state
        val current = state.currentExercise ?: return@update state
        val exercises = state.exercises.toMutableList()
        exercises.removeAt(state.currentExerciseIndex)
        exercises.add(current)
        val candidate = state.currentExerciseIndex.coerceIn(0, exercises.lastIndex)
        val newCurrent = pendingIndexFrom(exercises, candidate, excludedId = current.id) ?: exercises.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
        state.copy(exercises = exercises, currentExerciseIndex = newCurrent, currentSetIndex = firstIncompleteSet(exercises[newCurrent]), restEndsAt = null, paused = false, pausedRestRemainingMs = null)
    }

    suspend fun moveExercise(context: Context, exerciseId: String, delta: Int) = update(context) { state ->
        val exercises = state.exercises.toMutableList()
        if (exercises.isEmpty()) return@update state
        val from = exercises.indexOfFirst { it.id == exerciseId }
        if (from < 0) return@update state
        val to = (from + delta).coerceIn(0, exercises.lastIndex)
        if (from == to) return@update state
        val activeId = state.currentExercise?.id
        val item = exercises.removeAt(from)
        exercises.add(to, item)
        val activeIndex = activeId?.let { id -> exercises.indexOfFirst { it.id == id } }?.takeIf { it >= 0 } ?: state.currentExerciseIndex.coerceIn(0, exercises.lastIndex)
        state.copy(exercises = exercises, currentExerciseIndex = activeIndex, currentSetIndex = firstIncompleteSet(exercises[activeIndex]))
    }

    suspend fun selectExercise(context: Context, exerciseId: String) = update(context) { state ->
        val index = state.exercises.indexOfFirst { it.id == exerciseId && !it.isCompleted }
        if (index < 0) state else state.copy(currentExerciseIndex = index, currentSetIndex = firstIncompleteSet(state.exercises[index]), restEndsAt = null, paused = false, pausedRestRemainingMs = null)
    }

    suspend fun addRestSeconds(context: Context, seconds: Int) = update(context) { state ->
        val now = System.currentTimeMillis()
        val base = state.restEndsAt?.coerceAtLeast(now) ?: (now + (state.pausedRestRemainingMs ?: 0L))
        state.copy(restEndsAt = base + seconds.coerceAtLeast(0) * 1000L, paused = false, pausedRestRemainingMs = null)
    }

    suspend fun skipRest(context: Context) = update(context) { state -> state.copy(restEndsAt = null, paused = false, pausedRestRemainingMs = null) }

    suspend fun finishRestIfElapsed(context: Context): Boolean {
        var finished = false
        update(context) { state ->
            val end = state.restEndsAt
            if (end != null && System.currentTimeMillis() >= end) {
                finished = true
                state.copy(restEndsAt = null, paused = false, pausedRestRemainingMs = null)
            } else state
        }
        return finished
    }

    suspend fun togglePause(context: Context) = update(context) { state ->
        val now = System.currentTimeMillis()
        if (!state.paused) {
            val remaining = state.restEndsAt?.let { (it - now).coerceAtLeast(0L) }
            state.copy(paused = true, pausedRestRemainingMs = remaining, restEndsAt = null)
        } else {
            val remaining = state.pausedRestRemainingMs
            state.copy(paused = false, restEndsAt = remaining?.let { now + it }, pausedRestRemainingMs = null)
        }
    }

    suspend fun endWorkout(context: Context) = update(context) { state ->
        if (!state.finished) archive(context, state.copy(finished = true, restEndsAt = null, paused = false, pausedRestRemainingMs = null))
        state.copy(finished = true, restEndsAt = null, paused = false, pausedRestRemainingMs = null)
    }

    suspend fun clearWorkout(context: Context) = mutex.withLock { context.dataStore.edit { prefs -> prefs.remove(ACTIVE) } }
    suspend fun clearHistory(context: Context) = mutex.withLock { context.dataStore.edit { prefs -> prefs.remove(HISTORY) } }

    private suspend fun update(context: Context, transform: suspend (WorkoutSessionState) -> WorkoutSessionState) = mutex.withLock {
        val prefs = context.dataStore.data.first()
        val old = decodeSession(prefs[ACTIVE]) ?: return@withLock
        saveSession(context, transform(old))
    }

    private suspend fun saveSession(context: Context, state: WorkoutSessionState) {
        context.dataStore.edit { prefs -> prefs[ACTIVE] = json.encodeToString(WorkoutSessionState.serializer(), state) }
    }

    private suspend fun saveRoutines(context: Context, routines: List<RoutineDayTemplate>) {
        context.dataStore.edit { prefs -> prefs[ROUTINES] = json.encodeToString(ListSerializer(RoutineDayTemplate.serializer()), routines) }
    }

    private fun decodeSession(raw: String?): WorkoutSessionState? = runCatching { raw?.let { json.decodeFromString(WorkoutSessionState.serializer(), it) } }.getOrNull()
    private fun decodeHistory(raw: String?): List<WorkoutHistoryEntry> = runCatching { raw?.let { json.decodeFromString(ListSerializer(WorkoutHistoryEntry.serializer()), it) } ?: emptyList() }.getOrDefault(emptyList())
    private fun decodeRoutines(raw: String?): List<RoutineDayTemplate> = runCatching { raw?.let { json.decodeFromString(ListSerializer(RoutineDayTemplate.serializer()), it) } ?: RoutineFactory.defaultRoutineDays() }.getOrElse { RoutineFactory.defaultRoutineDays() }

    private suspend fun archive(context: Context, state: WorkoutSessionState) {
        val existing = decodeHistory(context.dataStore.data.first()[HISTORY]).toMutableList()
        val allSets = state.exercises.flatMap { it.sets }
        val volume = allSets.filter { it.completed && it.weightKg != null }.sumOf { set ->
            val reps = set.actualReps ?: set.targetReps.filter(Char::isDigit).toIntOrNull() ?: 0
            (set.weightKg ?: 0.0) * reps
        }
        existing.add(0, WorkoutHistoryEntry(System.currentTimeMillis(), state.mode, state.dayName, state.week, allSets.count { it.completed }, volume))
        context.dataStore.edit { prefs -> prefs[HISTORY] = json.encodeToString(ListSerializer(WorkoutHistoryEntry.serializer()), existing.take(50)) }
    }

    private fun firstIncompleteSet(exercise: WorkoutExerciseState): Int = exercise.sets.indexOfFirst { !it.completed }.takeIf { it >= 0 } ?: exercise.sets.lastIndex.coerceAtLeast(0)

    private fun findNextIncompleteExercise(exercises: List<WorkoutExerciseState>, current: Int): Int? {
        for (index in current + 1 until exercises.size) if (!exercises[index].isCompleted) return index
        for (index in 0 until current) if (!exercises[index].isCompleted) return index
        return null
    }

    private fun pendingIndexFrom(exercises: List<WorkoutExerciseState>, start: Int, excludedId: String): Int? {
        for (index in start until exercises.size) if (exercises[index].id != excludedId && !exercises[index].isCompleted) return index
        for (index in 0 until start) if (exercises[index].id != excludedId && !exercises[index].isCompleted) return index
        return null
    }

    private fun cleanNumber(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(java.util.Locale.US, value)
}
