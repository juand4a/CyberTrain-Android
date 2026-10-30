package com.tecnosuper.neon531.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Safety net for the rest timer.
 *
 * The workout keeps its absolute restEndsAt in DataStore. If the foreground
 * service is delayed or recreated while the phone is idle, AlarmManager can
 * wake this receiver and ask the service to reconcile the stored timestamp.
 */
class RestTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != WorkoutService.ACTION_REST_ALARM) return
        WorkoutService.start(context, WorkoutService.ACTION_REST_ALARM)
    }
}
