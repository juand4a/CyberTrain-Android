package com.tecnosuper.neon531.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CutCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecnosuper.neon531.R
import com.tecnosuper.neon531.data.FiveThreeOneSource
import com.tecnosuper.neon531.data.MainLift
import com.tecnosuper.neon531.data.TrainingMaxes
import com.tecnosuper.neon531.data.WorkoutMode
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.domain.FiveThreeOne
import com.tecnosuper.neon531.domain.RoutineFactory
import com.tecnosuper.neon531.service.WorkoutService
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberChoice
import com.tecnosuper.neon531.ui.components.CyberNumberField
import com.tecnosuper.neon531.ui.components.FiveThreeOneSetStrip
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.SystemBadge
import com.tecnosuper.neon531.ui.components.cleanNumber
import com.tecnosuper.neon531.ui.components.mainLiftLabel
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberLine
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberText
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    mode: WorkoutMode,
    onBack: () -> Unit,
    onEditRoutine: () -> Unit,
    onCycleUpdate: () -> Unit,
    onRestSettings: () -> Unit,
    onStarted: () -> Unit
) = CyberBackground {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val routines by WorkoutStore.routines(context)
        .collectAsStateWithLifecycle(initialValue = RoutineFactory.defaultRoutineDays())

    var week by remember { mutableStateOf(1) }
    var selectedDay by remember { mutableStateOf(0) }
    var squat by remember { mutableStateOf("135") }
    var bench by remember { mutableStateOf("97") }
    var deadlift by remember { mutableStateOf("175") }
    var rounding by remember { mutableStateOf("1") }

    var notificationStartPending by remember { mutableStateOf(false) }

    fun startWorkoutNow() {
        notificationStartPending = false
        scope.launch {
            val s = squat.toDoubleOrNull() ?: 135.0
            val b = bench.toDoubleOrNull() ?: 97.0
            val d = deadlift.toDoubleOrNull() ?: 175.0
            val r = rounding.toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0
            WorkoutStore.saveTrainingMaxes(context, s, b, d, r)
            WorkoutStore.startWorkout(context, mode, selectedDay, week)
            WorkoutService.start(context)
            onStarted()
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startWorkoutNow()
        } else {
            notificationStartPending = false
            val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(settingsIntent)
        }
    }

    LaunchedEffect(Unit) {
        val maxes = WorkoutStore.loadTrainingMaxes(context)
        squat = cleanNumber(maxes.squat)
        bench = cleanNumber(maxes.bench)
        deadlift = cleanNumber(maxes.deadlift)
        rounding = cleanNumber(WorkoutStore.loadRounding(context))
    }

    val selectedRoutine = routines.firstOrNull { it.dayIndex == selectedDay }
        ?: RoutineFactory.defaultRoutineDays().first { it.dayIndex == selectedDay }
    val mainExercise = selectedRoutine.exercises.firstOrNull { it.mainLift != null }
    val mainLift = mainExercise?.mainLift
    val roundingValue = rounding.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val previewMaxes = TrainingMaxes(
        squat = squat.toDoubleOrNull() ?: 135.0,
        bench = bench.toDoubleOrNull() ?: 97.0,
        deadlift = deadlift.toDoubleOrNull() ?: 175.0
    )
    val mainSets = if (mode == WorkoutMode.STRENGTH && mainExercise != null) {
        RoutineFactory.strengthSetsForTemplate(
            template = mainExercise,
            week = week,
            trainingMaxes = previewMaxes,
            rounding = roundingValue
        )
    } else {
        emptyList()
    }
    val mainUsesManual531 = mainExercise?.fiveThreeOneSource == FiveThreeOneSource.MANUAL

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            com.tecnosuper.neon531.ui.components.CyberTopBar(
                title = if (mode == WorkoutMode.STRENGTH) "FUERZA // 5·3·1" else "HIPERTROFIA",
                subtitle = if (mode == WorkoutMode.STRENGTH) {
                    "Cada serie principal usa un peso diferente según la semana"
                } else {
                    "Selecciona el día y comienza"
                },
                onBack = onBack
            )
        }

        item {
            SetupVisualHeader(mode = mode)
        }

        if (mode == WorkoutMode.STRENGTH) {
            item {
                SectionLabel("1 · semana del ciclo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    (1..3).forEach { candidate ->
                        CyberChoice(
                            text = "W$candidate",
                            selected = week == candidate,
                            modifier = Modifier.weight(1f)
                        ) { week = candidate }
                    }
                }
                Spacer(Modifier.height(7.dp))
                CyberCard(accent = CyberCyan) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = CyberCyan)
                        Spacer(Modifier.padding(4.dp))
                        Column(Modifier.weight(1f)) {
                            SystemBadge("WEEK $week // ${FiveThreeOne.schemeLabel(week)}")
                            Text(
                                when (week) {
                                    1 -> "65% × 5  ·  75% × 5  ·  85% × 5+"
                                    2 -> "70% × 3  ·  80% × 3  ·  90% × 3+"
                                    else -> "75% × 5  ·  85% × 3  ·  95% × 1+"
                                },
                                color = CyberText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                "Puedes usar cálculo automático por Training Max o escribir manualmente los 3 pesos de cada semana desde EDITAR RUTINA.",
                                color = CyberMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCycleUpdate,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("ACTUALIZAR 5·3·1", fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = onRestSettings,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("AVISOS", fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                }
            }

            item {
                SectionLabel("2 · training max")
                CyberCard {
                    Text(
                        "BASE DE CÁLCULO AUTOMÁTICO",
                        color = CyberText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Estos valores se usan en ejercicios 5/3/1 configurados como AUTO. Si un ejercicio está en MANUAL, se usan exactamente los pesos guardados en su editor.",
                        color = CyberMuted,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CyberNumberField("Sentadilla", squat, Modifier.weight(1f)) { squat = it }
                        CyberNumberField("Press", bench, Modifier.weight(1f)) { bench = it }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CyberNumberField("Peso muerto", deadlift, Modifier.weight(1f)) { deadlift = it }
                        CyberNumberField("Redondeo", rounding, Modifier.weight(1f)) { rounding = it }
                    }
                }
            }
        }

        item {
            SectionLabel(if (mode == WorkoutMode.STRENGTH) "3 · elige entrenamiento" else "elige entrenamiento")
        }

        routines.sortedBy { it.dayIndex }.take(5).forEach { day ->
            item(key = "day-${day.dayIndex}") {
                val selected = selectedDay == day.dayIndex
                val lift = day.exercises.firstOrNull { it.mainLift != null }?.mainLift
                CyberCard(
                    accent = if (selected) CyberCyan else Color.Transparent,
                    modifier = Modifier.clickable { selectedDay = day.dayIndex }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "DÍA ${day.dayIndex + 1}",
                                color = if (selected) CyberCyan else CyberMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                day.dayName,
                                color = CyberText,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "${day.exercises.size} ejercicios${if (mode == WorkoutMode.STRENGTH && lift != null) " · 5/3/1 ${mainLiftLabel(lift)}" else ""}",
                                color = if (lift != null && mode == WorkoutMode.STRENGTH) CyberMagenta else CyberMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (selected) {
                            Text("✓", color = CyberCyan, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        if (mode == WorkoutMode.STRENGTH && mainLift != null && mainSets.isNotEmpty()) {
            item {
                SectionLabel("pesos de hoy · ${mainExercise?.name ?: mainLiftLabel(mainLift)}")
                CyberCard(accent = CyberMagenta) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = CyberMagenta)
                        Spacer(Modifier.padding(4.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${mainExercise?.name?.uppercase()} // WEEK $week",
                                color = CyberText,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                if (mainUsesManual531) "MANUAL · 3 series · cargas guardadas" else "AUTO · 3 series · calculadas por TM",
                                color = CyberMagenta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    FiveThreeOneSetStrip(mainSets)
                    Spacer(Modifier.height(9.dp))
                    Text(
                        if (mainUsesManual531) {
                            "Estos son exactamente los valores que guardaste en la rutina. La app avanzará de una carga a la siguiente."
                        } else {
                            "Los pesos se calcularon desde tu Training Max. La app avanzará automáticamente al peso de la siguiente serie."
                        },
                        color = CyberMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            SectionLabel("resumen del día")
            CyberCard {
                selectedRoutine.exercises.forEachIndexed { index, exercise ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}".padStart(2, '0'),
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.padding(5.dp))
                        Column(Modifier.weight(1f)) {
                            Text(exercise.name, color = CyberText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (mode == WorkoutMode.STRENGTH && exercise.mainLift != null) {
                                    "5/3/1 · ${mainLiftLabel(exercise.mainLift)} · ${if (exercise.fiveThreeOneSource == FiveThreeOneSource.MANUAL) "MANUAL" else "AUTO"}"
                                } else {
                                    "${exercise.weightLabel} · ${exercise.series} × ${exercise.reps}"
                                },
                                color = if (exercise.mainLift != null && mode == WorkoutMode.STRENGTH) CyberMagenta else CyberMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onEditRoutine,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("EDITAR ESTA RUTINA")
            }
        }

        item {
            Button(
                onClick = {
                    WorkoutService.prepareNotificationChannel(context)
                    val notificationsEnabled =
                        WorkoutService.notificationsReady(context)

                    if (!notificationsEnabled) {
                        if (Build.VERSION.SDK_INT >= 33 && !notificationStartPending) {
                            notificationStartPending = true
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(settingsIntent)
                        }
                    } else {
                        startWorkoutNow()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Text("  INICIAR ${if (mode == WorkoutMode.STRENGTH) "FUERZA" else "RUTINA"}", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}


@Composable
private fun SetupVisualHeader(mode: WorkoutMode) {
    val shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp)
            .clip(shape)
            .border(1.dp, CyberLine, shape)
    ) {
        Image(
            painter = painterResource(R.drawable.cyber_moon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.88f),
                        0.58f to Color.Black.copy(alpha = 0.48f),
                        1f to Color.Black.copy(alpha = 0.20f)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp)
        ) {
            SystemBadge(if (mode == WorkoutMode.STRENGTH) "STRENGTH PROTOCOL" else "HYPERTROPHY PROTOCOL")
            Spacer(Modifier.height(6.dp))
            Text(
                if (mode == WorkoutMode.STRENGTH) "3 SETS · 3 CARGAS" else "TU RUTINA · TU ORDEN",
                color = CyberText,
                fontWeight = FontWeight.Black,
                fontSize = 19.sp
            )
            Text(
                if (mode == WorkoutMode.STRENGTH) {
                    "Selecciona semana, día y revisa los pesos antes de empezar."
                } else {
                    "Selecciona el día y entra directo a entrenar."
                },
                color = CyberMuted,
                fontSize = 10.sp
            )
        }
    }
}
