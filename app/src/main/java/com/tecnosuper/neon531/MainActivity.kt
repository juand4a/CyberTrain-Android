package com.tecnosuper.neon531

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.service.WorkoutService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.tecnosuper.neon531.ui.Neon531App
import com.tecnosuper.neon531.ui.theme.Neon531Theme

class MainActivity : ComponentActivity() {

    private val openActiveWorkout = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        openActiveWorkout.value = intent?.getBooleanExtra(EXTRA_OPEN_ACTIVE, false) == true

        setContent {
            Neon531Theme {
                Neon531App(
                    openActiveOnLaunch = openActiveWorkout.value,
                    onActiveLaunchConsumed = { openActiveWorkout.value = false }
                )
            }
        }
    }

    // Repair service loss on returning to the app after an OEM task killer,
    // permission change, or process restart. DataStore remains authoritative.
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val stored = runCatching { WorkoutStore.activeWorkout(applicationContext).first() }
                .getOrNull()
            if (stored != null && !stored.finished) {
                runCatching { WorkoutService.start(applicationContext) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_ACTIVE, false)) {
            openActiveWorkout.value = true
        }
    }

    companion object {
        const val EXTRA_OPEN_ACTIVE = "neon531.OPEN_ACTIVE"
    }
}
