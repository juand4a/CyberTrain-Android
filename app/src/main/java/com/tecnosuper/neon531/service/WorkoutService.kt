package com.tecnosuper.neon531.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.ServiceCompat
import com.tecnosuper.neon531.MainActivity
import com.tecnosuper.neon531.R
import com.tecnosuper.neon531.data.WorkoutMode
import com.tecnosuper.neon531.data.WorkoutSessionState
import com.tecnosuper.neon531.data.WorkoutStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Persistent workout controller.
 *
 * Reliability model:
 * - DataStore is the source of truth.
 * - A foreground service keeps the workout alive while the screen is off.
 * - restEndsAt is an absolute timestamp, never a decrementing counter.
 * - Handler is the in-process timer.
 * - AlarmManager is the system-level wake-up fallback.
 * - Android's chronometer remains available where the OS renders it, while
 *   a lightweight 1-second notification ticker keeps the title current too.
 * - No big bitmap is sent to the notification manager on every tick.
 * - No AudioFocus is requested for the rest alert, so music is not paused.
 */
class WorkoutService : Service() {

    // Timer/notification lifecycle state is serialized on the main thread.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var currentState: WorkoutSessionState? = null
    private var stateJob: Job? = null

    private var restWakeLock: PowerManager.WakeLock? = null
    private var restCompletionRunnable: Runnable? = null
    private var notificationTick: Runnable? = null
    private var scheduledRestEnd: Long? = null

    private var alarmManager: AlarmManager? = null
    private var alarmPendingIntent: PendingIntent? = null

    private var alertPlayer: MediaPlayer? = null
    private var lastRestAlertAt = 0L

    private val timerHandler by lazy { Handler(mainLooper) }

    override fun onCreate() {
        super.onCreate()

        prepareNotificationChannel(this)
        createWakeLock()

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmPendingIntent = PendingIntent.getBroadcast(
            this,
            REST_ALARM_REQUEST_CODE,
            Intent(this, RestTimerReceiver::class.java).apply {
                action = ACTION_REST_ALARM
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // A foreground notification must exist immediately. The collector may
        // not have loaded DataStore yet, so first publish a stable base state.
        startInForeground(currentState)

        ensureCollectors()
        handleAction(intent)

        // If Android recreates the service, DataStore is read again by the
        // collector and the rest alarm is re-scheduled from the absolute time.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Do not stop the foreground service when the user swipes the app away.
        // Re-publish the notification and re-arm the system alarm as a safety net.
        currentState?.let { state ->
            if (!state.finished) {
                runCatching {
                    startInForeground(state)
                    notifyState(state)
                    scheduleRestCompletion(state)
                }
            }
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stateJob?.cancel()
        stopNotificationTicker()
        // IMPORTANT: do not cancel the AlarmManager PendingIntent here.
        // If Android destroys the service, the broadcast alarm must survive.
        restCompletionRunnable?.let { timerHandler.removeCallbacks(it) }
        restCompletionRunnable = null
        releaseRestWakeLock()
        releaseAlertPlayer()
        scope.cancel()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Wake lock
    // -------------------------------------------------------------------------

    private fun createWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        restWakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "cybertrain:rest_timer"
        ).apply {
            setReferenceCounted(false)
        }
    }

    private fun syncRestWakeLock(state: WorkoutSessionState?) {
        val restEnd = state?.takeUnless { it.finished || it.paused }?.restEndsAt

        if (restEnd != null) {
            val lock = restWakeLock ?: return
            if (!lock.isHeld) {
                // Bounded lock: enough for the configured rest, but never leaked
                // indefinitely if the service stops behaving normally.
                val remaining = (restEnd - System.currentTimeMillis())
                    .coerceAtLeast(0L)
                runCatching { lock.acquire((remaining + 15_000L).coerceIn(15_000L, 3_600_000L)) }
            }
        } else {
            releaseRestWakeLock()
        }
    }

    private fun releaseRestWakeLock() {
        restWakeLock?.let { lock ->
            if (lock.isHeld) {
                runCatching { lock.release() }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Notification actions
    // -------------------------------------------------------------------------

    private fun handleAction(intent: Intent?) {
        when (intent?.action) {
            ACTION_COMPLETE_SET -> {
                val result = RemoteInput.getResultsFromIntent(intent)
                val reps = result
                    ?.getCharSequence(KEY_ACTUAL_REPS)
                    ?.toString()
                    ?.toIntOrNull()

                scope.launch {
                    WorkoutStore.completeCurrentSet(this@WorkoutService, reps)
                }
            }

            ACTION_POSTPONE_ONE -> scope.launch {
                WorkoutStore.postponeCurrentOne(this@WorkoutService)
            }

            ACTION_POSTPONE_TWO -> scope.launch {
                WorkoutStore.postponeCurrentTwo(this@WorkoutService)
            }

            ACTION_POSTPONE_END -> scope.launch {
                WorkoutStore.postponeCurrentToEnd(this@WorkoutService)
            }

            ACTION_ADD_30 -> scope.launch {
                WorkoutStore.addRestSeconds(this@WorkoutService, 30)
            }

            ACTION_SKIP_REST -> scope.launch {
                WorkoutStore.skipRest(this@WorkoutService)
            }

            ACTION_TOGGLE_PAUSE -> scope.launch {
                WorkoutStore.togglePause(this@WorkoutService)
            }

            ACTION_REST_ALARM -> scope.launch {
                handleRestAlarm()
            }

            ACTION_STOP -> scope.launch {
                WorkoutStore.endWorkout(this@WorkoutService)
                // Explicitly ended sessions must cancel the backup alarm.
                cancelRestCompletionSchedule()
                stopSelf()
            }
        }
    }

    // -------------------------------------------------------------------------
    // Persistent state
    // -------------------------------------------------------------------------

    private fun ensureCollectors() {
        if (stateJob != null) return

        stateJob = scope.launch {
            WorkoutStore.activeWorkout(this@WorkoutService)
                .flowOn(Dispatchers.Default)
                .collectLatest { state ->
                currentState = state
                syncRestWakeLock(state)
                scheduleRestCompletion(state)

                when {
                    state == null -> {
                        cancelRestCompletionSchedule()
                        stopSelf()
                    }

                    state.finished -> {
                        cancelRestCompletionSchedule()
                        releaseRestWakeLock()

                        // Keep the completion card visible in the shade for the
                        // user, but the service itself no longer needs to run.
                        notifyState(state)
                        stopForeground(STOP_FOREGROUND_DETACH)
                        stopSelf()
                    }

                    else -> {
                        // State changes publish immediately; while resting, a
                        // lightweight ticker also refreshes the visible title.
                        notifyState(state)
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Absolute rest timer
    // -------------------------------------------------------------------------

    private fun scheduleRestCompletion(state: WorkoutSessionState?) {
        val end = state?.restEndsAt
        val shouldSchedule = state != null &&
            !state.finished &&
            !state.paused &&
            end != null

        if (!shouldSchedule) {
            cancelRestCompletionSchedule()
            return
        }

        val target = end!!

        if (scheduledRestEnd == target) return

        cancelRestCompletionSchedule()
        scheduledRestEnd = target

        val now = System.currentTimeMillis()

        if (target <= now) {
            // This is especially important after a reboot: the rest may have
            // expired while the phone was powered off. Android cannot play audio
            // while physically powered off, but we reconcile immediately after boot.
            restCompletionRunnable = Runnable {
                scope.launch { handleRestAlarm() }
            }
            timerHandler.post(restCompletionRunnable!!)
        } else {
            val delayMs = target - now
            restCompletionRunnable = Runnable {
                scope.launch { handleRestAlarm() }
            }
            timerHandler.postDelayed(restCompletionRunnable!!, delayMs)
        }

        scheduleSystemRestAlarm(target)
        startNotificationTicker()
    }

    /**
     * The OS may hide the built-in chronometer in a collapsed or lock-screen
     * notification. Refresh a small, TEXT-ONLY notification every second so
     * the countdown is visible even in that layout (subject to user settings).
     * No persistence writes are made by this ticker.
     */
    private fun startNotificationTicker() {
        stopNotificationTicker()
        val runnable = object : Runnable {
            override fun run() {
                val state = currentState ?: return
                val end = state.restEndsAt ?: return
                if (state.finished || state.paused) return
                if (System.currentTimeMillis() >= end) {
                    // The finish action is idempotent in WorkoutStore.
                    scope.launch { handleRestAlarm() }
                    return
                }
                notifyState(state)
                val untilNextSecond = 1_000L - (System.currentTimeMillis() % 1_000L)
                timerHandler.postDelayed(this, untilNextSecond.coerceAtLeast(100L))
            }
        }
        notificationTick = runnable
        val delay = 1_000L - (System.currentTimeMillis() % 1_000L)
        timerHandler.postDelayed(runnable, delay.coerceAtLeast(100L))
    }

    private fun stopNotificationTicker() {
        notificationTick?.let { timerHandler.removeCallbacks(it) }
        notificationTick = null
    }

    private fun cancelRestCompletionSchedule() {
        stopNotificationTicker()
        restCompletionRunnable?.let { timerHandler.removeCallbacks(it) }
        restCompletionRunnable = null
        scheduledRestEnd = null

        alarmPendingIntent?.let { pending ->
            runCatching { alarmManager?.cancel(pending) }
        }
    }

    private fun scheduleSystemRestAlarm(targetAt: Long) {
        val manager = alarmManager ?: return
        val pending = alarmPendingIntent ?: return
        val triggerAt = targetAt.coerceAtLeast(System.currentTimeMillis() + 250L)

        runCatching {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    manager.canScheduleExactAlarms() -> {
                    manager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pending
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    // Fallback when exact-alarm access is not granted. The
                    // foreground service + PARTIAL_WAKE_LOCK still keeps the
                    // in-process timer running, while this alarm is a system backup.
                    manager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pending
                    )
                }

                else -> {
                    @Suppress("DEPRECATION")
                    manager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pending
                    )
                }
            }
        }
    }

    private suspend fun handleRestAlarm() {
        val finished = WorkoutStore.finishRestIfElapsed(this@WorkoutService)
        val state = WorkoutStore.activeWorkout(this@WorkoutService).first()

        currentState = state

        if (finished) {
            cancelRestCompletionSchedule()
            releaseRestWakeLock()
            restFinishedAlert()
            state?.let { notifyState(it) }
        } else {
            // A duplicate alarm can arrive after the Handler already handled it.
            // In that case the stored timestamp is authoritative and we simply
            // reconcile the next state.
            scheduleRestCompletion(state)
            state?.let { notifyState(it) }
        }
    }

    // -------------------------------------------------------------------------
    // Rest alert: 7 seconds, soft volume, NO AudioFocus
    // -------------------------------------------------------------------------

    private fun restFinishedAlert() {
        val now = System.currentTimeMillis()
        if (now - lastRestAlertAt < 1500L) return
        lastRestAlertAt = now

        scope.launch {
            val soundEnabled = WorkoutStore.loadRestSoundEnabled(this@WorkoutService)
            val vibrationEnabled = WorkoutStore.loadRestVibrationEnabled(this@WorkoutService)
            val volume = WorkoutStore.loadRestSoundVolume(this@WorkoutService)

            if (soundEnabled) playRestFinishedSound(volume)
            if (vibrationEnabled) vibrateRestFinished()
        }
    }

    private fun playRestFinishedSound(volume: Int) {
        timerHandler.post {
            releaseAlertPlayer()

            runCatching {
                val player = MediaPlayer()
                val level = volume.coerceIn(0, 100) / 100f

                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                // IMPORTANT: no AudioManager.requestAudioFocus().
                // The user's music player is therefore not intentionally paused.
                player.setVolume(level, level)
                player.setDataSource(
                    this@WorkoutService,
                    Uri.parse("android.resource://$packageName/${R.raw.alarma_gym_imnesthor}")
                )

                player.setOnPreparedListener { prepared ->
                    alertPlayer = prepared
                    prepared.start()

                    timerHandler.postDelayed({
                        releaseAlertPlayer()
                    }, ALERT_SOUND_DURATION_MS)
                }

                player.setOnCompletionListener {
                    releaseAlertPlayer()
                }

                player.setOnErrorListener { _, _, _ ->
                    releaseAlertPlayer()
                    true
                }

                alertPlayer = player
                player.prepareAsync()
            }.onFailure {
                releaseAlertPlayer()
            }
        }
    }

    private fun releaseAlertPlayer() {
        val player = alertPlayer ?: return
        alertPlayer = null

        runCatching {
            if (player.isPlaying) player.stop()
        }
        runCatching { player.reset() }
        runCatching { player.release() }
    }

    private fun vibrateRestFinished() {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(70L, 40)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(70L)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Foreground service + notification
    // -------------------------------------------------------------------------

    private fun startInForeground(state: WorkoutSessionState?) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(state),
            type
        )
    }

    private fun notifyState(state: WorkoutSessionState) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // The service can still run, but Android will not display a denied
            // notification permission. SetupScreen prevents this in normal use.
            return
        }

        runCatching {
            NotificationManagerCompat.from(this).notify(
                NOTIFICATION_ID,
                buildNotification(state)
            )
        }
    }

    private fun buildNotification(state: WorkoutSessionState?): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ACTIVE, true)
        }

        val contentPending = PendingIntent.getActivity(
            this,
            900,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_neon_bolt)
            .setContentIntent(contentPending)
            .setOngoing(state?.finished != true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(0xFF00E7F0.toInt())
            .setColorized(false)
            .setShowWhen(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            )
        }

        // VISIBILITY_PUBLIC requests full content on the lock screen.
        // Do not replace it with a static generic PublicVersion.

        if (state == null) {
            return builder
                .setContentTitle("CYBERTRAIN")
                .setContentText("Preparando entrenamiento…")
                .build()
        }

        if (state.finished) {
            return builder
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle("✓ SESIÓN COMPLETA")
                .setContentText("${state.dayName} · ${state.completedSets} series registradas")
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        buildString {
                            append(modeLabel(state.mode))
                            state.week?.let { append(" · WEEK $it") }
                            append("\n${state.completedSets}/${state.totalSets} series completadas")
                            append("\nProgreso guardado en el historial.")
                        }
                    )
                )
                .build()
        }

        val exercise = state.currentExercise
        val set = state.currentSet
        val total = state.totalSets.coerceAtLeast(1)
        val completed = state.completedSets.coerceIn(0, total)
        val nextExercise = state.exercises
            .drop(state.currentExerciseIndex + 1)
            .firstOrNull { !it.isCompleted }
            ?: state.exercises.firstOrNull { !it.isCompleted && it.id != exercise?.id }

        builder.setProgress(total, completed, false)

        if (state.paused) {
            return builder
                .setContentTitle("⏸ SESIÓN PAUSADA")
                .setContentText("${exercise?.name.orEmpty()} · $completed/$total series")
                .setSubText(modeLabel(state.mode))
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        buildString {
                            append("ACTUAL // ${exercise?.name.orEmpty()}")
                            append("\n${set?.weightLabel.orEmpty()} × ${set?.targetReps.orEmpty()}")
                            append("\n\nEl progreso permanece guardado.")
                        }
                    )
                )
                .addAction(action(ACTION_TOGGLE_PAUSE, "▶ REANUDAR", 11))
                .build()
        }

        val restEnd = state.restEndsAt
        if (restEnd != null) {
            val remainingMs = (restEnd - System.currentTimeMillis()).coerceAtLeast(0L)

            builder
                .setContentTitle("DESCANSO · ${formatTime(remainingMs)}")
                .setContentText(
                    "SIGUIENTE · ${set?.weightLabel.orEmpty()} × ${set?.targetReps.orEmpty()} · ${exercise?.name.orEmpty()}"
                )
                .setSubText(
                    buildString {
                        append(modeLabel(state.mode))
                        state.week?.let { append(" · W$it") }
                        append(" · $completed/$total")
                    }
                )
                .setWhen(restEnd)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)

            // Text-only notification. Updating a full-screen bitmap every
            // second can overload SystemUI / Binder on some devices.
            builder.setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "DESCANSO ${formatTime(remainingMs)} · ${exercise?.name.orEmpty()}\n" +
                        "SIGUIENTE // ${set?.weightLabel.orEmpty()} × ${set?.targetReps.orEmpty()}\n" +
                        "${modeLabel(state.mode)} · $completed/$total series"
                )
            )

            builder
                .addAction(action(ACTION_ADD_30, "+30 s", 21))
                .addAction(action(ACTION_SKIP_REST, "SALTAR", 22))
                .addAction(action(ACTION_TOGGLE_PAUSE, "PAUSA", 23))

            return builder.build()
        }

        val setNumber = state.currentSetIndex + 1
        val setCount = exercise?.sets?.size ?: 0
        val percentage = set?.percentage?.let { "$it% TM" }
        val protocol = if (exercise?.isMain531 == true) {
            "5/3/1${state.week?.let { " · W$it" } ?: ""}"
        } else {
            "NORMAL"
        }

        builder
            .setContentTitle("${exercise?.name ?: "Entrenamiento"} · SET $setNumber/$setCount")
            .setContentText(
                buildString {
                    append("${set?.weightLabel.orEmpty()} × ${set?.targetReps.orEmpty()}")
                    percentage?.let { append(" · $it") }
                }
            )
            .setSubText("$protocol · $completed/$total series")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    buildString {
                        append("NOW // ${exercise?.name.orEmpty()}")
                        append("\n\n${set?.weightLabel.orEmpty()} × ${set?.targetReps.orEmpty()}")
                        percentage?.let { append(" · $it") }
                        if (exercise?.isMain531 == true) {
                            append("\n\n${compact531Plan(state)}")
                        }
                        nextExercise?.let {
                            append("\n\nNEXT EXERCISE // ${it.name}")
                        }
                    }
                )
            )

        if (set?.isAmrap == true) {
            val input = RemoteInput.Builder(KEY_ACTUAL_REPS)
                .setLabel("Reps realizadas")
                .build()

            val inputAction = NotificationCompat.Action.Builder(
                R.drawable.ic_neon_bolt,
                "✓ REPS",
                servicePendingIntent(ACTION_COMPLETE_SET, 31, mutable = true)
            )
                .addRemoteInput(input)
                .build()

            builder.addAction(inputAction)
        } else {
            builder.addAction(action(ACTION_COMPLETE_SET, "✓ SERIE", 30))
        }

        builder
            .addAction(action(ACTION_POSTPONE_ONE, "↷ +1", 32))
            .addAction(action(ACTION_POSTPONE_TWO, "↷ +2", 33))
            .addAction(action(ACTION_POSTPONE_END, "AL FINAL", 34))

        return builder.build()
    }

    private fun compact531Plan(state: WorkoutSessionState): String {
        val exercise = state.currentExercise ?: return ""
        if (!exercise.isMain531) return ""

        return exercise.sets.mapIndexed { index, workoutSet ->
            val marker = when {
                workoutSet.completed -> "✓"
                index == state.currentSetIndex -> "→"
                else -> "·"
            }
            "$marker S${index + 1} ${workoutSet.weightLabel}×${workoutSet.targetReps}"
        }.joinToString("   ")
    }

    private fun action(
        action: String,
        title: String,
        requestCode: Int
    ): NotificationCompat.Action = NotificationCompat.Action.Builder(
        R.drawable.ic_neon_bolt,
        title,
        servicePendingIntent(action, requestCode, mutable = false)
    ).build()

    private fun servicePendingIntent(
        action: String,
        requestCode: Int,
        mutable: Boolean
    ): PendingIntent {
        val intent = Intent(this, WorkoutService::class.java).apply {
            this.action = action
        }

        val mutabilityFlag = if (mutable) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or mutabilityFlag
        )
    }


    private fun formatTime(ms: Long): String {
        val seconds = ((ms.coerceAtLeast(0L) + 999L) / 1000L)
        return "%02d:%02d".format(seconds / 60L, seconds % 60L)
    }

    companion object {
        // New ID: channel importance cannot be changed after creation.
        const val CHANNEL_ID = "active_workout_v11"
        const val NOTIFICATION_ID = 531
        const val KEY_ACTUAL_REPS = "actual_reps"

        const val ACTION_COMPLETE_SET = "neon531.COMPLETE_SET"
        const val ACTION_POSTPONE_ONE = "neon531.POSTPONE_ONE"
        const val ACTION_POSTPONE_TWO = "neon531.POSTPONE_TWO"
        const val ACTION_POSTPONE_END = "neon531.POSTPONE_END"
        const val ACTION_ADD_30 = "neon531.ADD_30"
        const val ACTION_SKIP_REST = "neon531.SKIP_REST"
        const val ACTION_TOGGLE_PAUSE = "neon531.TOGGLE_PAUSE"
        const val ACTION_STOP = "neon531.STOP"
        const val ACTION_REST_ALARM = "neon531.REST_ALARM"

        const val REST_ALARM_REQUEST_CODE = 1531
        const val ALERT_SOUND_DURATION_MS = 7_000L

        fun testRestAlert(
            context: Context,
            soundEnabled: Boolean,
            vibrationEnabled: Boolean,
            volume: Int
        ) {
            if (soundEnabled) {
                Handler(context.mainLooper).post {
                    runCatching {
                        val player = MediaPlayer()
                        val level = volume.coerceIn(0, 100) / 100f

                        player.setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        player.setVolume(level, level)
                        player.setDataSource(
                            context,
                            Uri.parse("android.resource://${context.packageName}/${R.raw.alarma_gym_imnesthor}")
                        )

                        val release = {
                            runCatching { if (player.isPlaying) player.stop() }
                            runCatching { player.reset() }
                            runCatching { player.release() }
                        }

                        player.setOnPreparedListener { prepared ->
                            prepared.start()
                            Handler(context.mainLooper).postDelayed(
                                { release() },
                                ALERT_SOUND_DURATION_MS
                            )
                        }
                        player.setOnCompletionListener { release() }
                        player.setOnErrorListener { _, _, _ ->
                            release()
                            true
                        }
                        player.prepareAsync()
                    }
                }
            }

            if (vibrationEnabled) {
                runCatching {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(VibratorManager::class.java).defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(70L, 40)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(70L)
                    }
                }
            }
        }

        fun notificationsReady(context: Context): Boolean {
            val manager = NotificationManagerCompat.from(context)
            if (!manager.areNotificationsEnabled()) return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = context
                    .getSystemService(NotificationManager::class.java)
                    .getNotificationChannel(CHANNEL_ID)

                if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                    return false
                }
            }

            return true
        }

        fun prepareNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Entrenamiento activo",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Entrenamiento activo y cuenta regresiva en pantalla bloqueada"
                        setSound(null, null)
                        enableVibration(false)
                        setShowBadge(false)
                        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }

        fun start(context: Context, action: String? = null) {
            val intent = Intent(context, WorkoutService::class.java).apply {
                if (action != null) this.action = action
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}

private fun modeLabel(mode: WorkoutMode): String =
    if (mode == WorkoutMode.STRENGTH) "FUERZA 5/3/1" else "HIPERTROFIA"
