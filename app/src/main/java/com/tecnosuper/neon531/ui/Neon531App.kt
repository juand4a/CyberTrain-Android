package com.tecnosuper.neon531.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecnosuper.neon531.data.WorkoutMode
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.ui.screens.ActiveWorkoutScreen
import com.tecnosuper.neon531.ui.screens.HistoryScreen
import com.tecnosuper.neon531.ui.screens.CycleUpdateScreen
import com.tecnosuper.neon531.ui.screens.RestAlertSettingsScreen
import com.tecnosuper.neon531.ui.screens.HomeScreen
import com.tecnosuper.neon531.ui.screens.RoutineEditorScreen
import com.tecnosuper.neon531.ui.screens.SetupScreen
import com.tecnosuper.neon531.ui.screens.SplashScreen
import com.tecnosuper.neon531.ui.screens.ToolsScreen

@Composable
fun Neon531App(
    openActiveOnLaunch: Boolean = false,
    onActiveLaunchConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activeWorkout by WorkoutStore.activeWorkout(context)
        .collectAsStateWithLifecycle(initialValue = null)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // If the user taps the persistent notification while the app is already open,
    // jump directly to the active workout instead of forcing another splash/home step.
    LaunchedEffect(openActiveOnLaunch, activeWorkout, currentRoute) {
        if (
            openActiveOnLaunch &&
            currentRoute != null &&
            currentRoute != "splash" &&
            activeWorkout != null &&
            activeWorkout?.finished != true
        ) {
            navController.navigate("active") {
                launchSingleTop = true
            }
            onActiveLaunchConsumed()
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen {
                val destination = if (
                    openActiveOnLaunch &&
                    activeWorkout != null &&
                    activeWorkout?.finished != true
                ) {
                    "active"
                } else {
                    "home"
                }

                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                    launchSingleTop = true
                }

                if (openActiveOnLaunch) {
                    onActiveLaunchConsumed()
                }
            }
        }

        composable("home") {
            HomeScreen(
                active = activeWorkout,
                onStrength = { navController.navigate("setup/STRENGTH") },
                onHypertrophy = { navController.navigate("setup/HYPERTROPHY") },
                onTools = { navController.navigate("tools") },
                onHistory = { navController.navigate("history") },
                onRoutines = { navController.navigate("routines") },
                onCycleUpdate = { navController.navigate("cycle-update") },
                onRestSettings = { navController.navigate("rest-settings") },
                onContinue = { navController.navigate("active") }
            )
        }

        composable(
            route = "setup/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { entry ->
            val mode = runCatching {
                WorkoutMode.valueOf(entry.arguments?.getString("mode") ?: "HYPERTROPHY")
            }.getOrDefault(WorkoutMode.HYPERTROPHY)

            SetupScreen(
                mode = mode,
                onBack = { navController.popBackStack() },
                onEditRoutine = { navController.navigate("routines") },
                onCycleUpdate = { navController.navigate("cycle-update") },
                onRestSettings = { navController.navigate("rest-settings") },
                onStarted = {
                    navController.navigate("active") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("active") {
            ActiveWorkoutScreen(
                state = activeWorkout,
                onBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("routines") {
            RoutineEditorScreen(onBack = { navController.popBackStack() })
        }

        composable("cycle-update") {
            CycleUpdateScreen(onBack = { navController.popBackStack() })
        }

        composable("rest-settings") {
            RestAlertSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable("tools") {
            ToolsScreen(onBack = { navController.popBackStack() })
        }

        composable("history") {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
