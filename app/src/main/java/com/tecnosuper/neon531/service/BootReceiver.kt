package com.tecnosuper.neon531.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Restores an active workout after a device reboot or app replacement.
 *
 * Android cancels AlarmManager alarms when the device shuts down, so the
 * foreground service is restarted after boot and reads the persisted
 * WorkoutSessionState from DataStore. If a rest end time is already in the
 * past, WorkoutService reconciles it immediately and plays the 7-second alert.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val serviceIntent = Intent(
                    context,
                    WorkoutService::class.java
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (_: Exception) {
                    // The system can reject a background start on some OEMs.
                    // The next foreground app launch will restart the service.
                }
            }
        }
    }
}
