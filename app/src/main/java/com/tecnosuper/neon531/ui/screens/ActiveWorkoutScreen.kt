package com.tecnosuper.neon531.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecnosuper.neon531.data.WorkoutSessionState
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberProgress
import com.tecnosuper.neon531.ui.components.CyberStat
import com.tecnosuper.neon531.ui.components.CyberTopBar
import com.tecnosuper.neon531.ui.components.FiveThreeOneSetStrip
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.SystemBadge
import com.tecnosuper.neon531.ui.components.formatMillis
import com.tecnosuper.neon531.ui.components.formatSeconds
import com.tecnosuper.neon531.ui.components.modeLabel
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberSuccess
import com.tecnosuper.neon531.ui.theme.CyberText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActiveWorkoutScreen(
    state: WorkoutSessionState?,
    onBack: () -> Unit,
    onFinished: () -> Unit
) = CyberBackground {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var amrapReps by remember { mutableStateOf("") }
    var showQueue by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            // One update per second avoids needlessly recomposing the whole
            // training screen four times a second.
            delay(1_000L - (System.currentTimeMillis() % 1_000L))
        }
    }

    if (state == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("NO ACTIVE SESSION", color = CyberMuted, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("VOLVER") }
        }
        return@CyberBackground
    }

    if (state.finished) {
        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SystemBadge("SESSION // COMPLETE", CyberSuccess)
            Spacer(Modifier.height(10.dp))
            Text(
                "ENTRENAMIENTO\nFINALIZADO",
                color = CyberText,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${state.completedSets} series registradas",
                color = CyberMuted
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    scope.launch {
                        WorkoutStore.clearWorkout(context)
                        onFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Text("GUARDAR Y SALIR", fontWeight = FontWeight.Black)
            }
        }
        return@CyberBackground
    }

    val exercise = state.currentExercise
    val set = state.currentSet
    val restRemaining = state.restEndsAt?.let { (it - now).coerceAtLeast(0L) }
    val sessionProgress = if (state.totalSets == 0) 0f else state.completedSets.toFloat() / state.totalSets
    val nextPending = state.exercises
        .drop(state.currentExerciseIndex + 1)
        .firstOrNull { !it.isCompleted }
        ?: state.exercises.firstOrNull { !it.isCompleted && it.id != exercise?.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            CyberTopBar(
                title = state.dayName,
                subtitle = "${modeLabel(state.mode)}${state.week?.let { " · WEEK $it" } ?: ""}",
                onBack = onBack
            )
            CyberProgress(sessionProgress, "PROGRESO ${state.completedSets}/${state.totalSets} SERIES")
        }

        if (exercise?.isMain531 == true && exercise.sets.isNotEmpty()) {
            item {
                SectionLabel("5/3/1 · cargas de este ejercicio")
                FiveThreeOneSetStrip(
                    sets = exercise.sets,
                    currentIndex = state.currentSetIndex
                )
            }
        }

        item {
            CyberCard(
                accent = when {
                    state.paused -> CyberMagenta
                    set?.isAmrap == true -> CyberMagenta
                    else -> CyberCyan
                }
            ) {
                SystemBadge(
                    text = when {
                        state.paused -> "SYSTEM // PAUSED"
                        restRemaining != null -> "REST // ACTIVE"
                        set?.isAmrap == true -> "TOP SET // AMRAP"
                        else -> "SET // READY"
                    },
                    color = when {
                        state.paused -> CyberMagenta
                        set?.isAmrap == true -> CyberMagenta
                        else -> CyberCyan
                    }
                )
                Spacer(Modifier.height(9.dp))

                Text(
                    exercise?.name ?: "—",
                    color = CyberText,
                    style = MaterialTheme.typography.headlineLarge
                )

                when {
                    state.paused -> {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Tu progreso y el tiempo restante están guardados.",
                            color = CyberMuted,
                            fontSize = 12.sp
                        )
                    }

                    restRemaining != null -> {
                        Spacer(Modifier.height(12.dp))
                        val totalRestMs = ((exercise?.restSeconds ?: 1).coerceAtLeast(1) * 1000L).toFloat()
                        val restProgress = (restRemaining.toFloat() / totalRestMs).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { restProgress },
                                modifier = Modifier.size(150.dp),
                                color = CyberCyan,
                                trackColor = CyberCyan.copy(alpha = 0.10f),
                                strokeWidth = 7.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "REST",
                                    color = CyberMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    formatMillis(restRemaining),
                                    color = CyberCyan,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "pantalla apagada: activo",
                                    color = CyberMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Text(
                            "SIGUIENTE // ${set?.weightLabel ?: "—"} × ${set?.targetReps ?: "—"}",
                            color = CyberText,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        if (exercise?.isMain531 == true) {
                            Text(
                                "La carga cambia automáticamente al terminar el descanso.",
                                color = CyberMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    else -> {
                        Spacer(Modifier.height(7.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                set?.weightLabel ?: "—",
                                color = CyberCyan,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black
                            )
                            set?.percentage?.let {
                                Text(
                                    "  $it% TM",
                                    color = CyberMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 9.dp)
                                )
                            }
                        }
                        Text(
                            "${set?.targetReps ?: "—"} REPS",
                            color = if (set?.isAmrap == true) CyberMagenta else CyberText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(11.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            CyberStat(
                                "SERIE",
                                "${state.currentSetIndex + 1}/${exercise?.sets?.size ?: 0}",
                                Modifier.weight(1f),
                                true
                            )
                            CyberStat("DESCANSO", formatSeconds(exercise?.restSeconds ?: 0), Modifier.weight(1f))
                            CyberStat(
                                "PROTOCOLO",
                                if (exercise?.isMain531 == true) "5/3/1" else "NORMAL",
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (!state.paused && restRemaining == null && exercise != null && set != null) {
            item {
                val nextSet = exercise.sets.getOrNull(state.currentSetIndex + 1)
                if (nextSet != null) {
                    CyberCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "NEXT SET",
                                    color = CyberMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "${nextSet.weightLabel} × ${nextSet.targetReps}",
                                    color = CyberText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            nextSet.percentage?.let {
                                Text(
                                    "$it% TM",
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.paused) {
            item {
                Button(
                    onClick = { scope.launch { WorkoutStore.togglePause(context) } },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Text("  REANUDAR SESIÓN", fontWeight = FontWeight.Black)
                }
            }
        } else if (restRemaining != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedButton(
                        onClick = { scope.launch { WorkoutStore.addRestSeconds(context, 30) } },
                        modifier = Modifier.weight(1f)
                    ) { Text("+30 s") }
                    OutlinedButton(
                        onClick = { scope.launch { WorkoutStore.skipRest(context) } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = null)
                        Text("  SALTAR")
                    }
                }
                TextButton(
                    onClick = { scope.launch { WorkoutStore.togglePause(context) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Pause, contentDescription = null)
                    Text("  PAUSAR ENTRENAMIENTO")
                }
            }
        } else {
            if (set?.isAmrap == true) {
                item {
                    CyberCard(accent = CyberMagenta) {
                        Text("¿CUÁNTAS REPS HICISTE?", color = CyberMagenta, fontWeight = FontWeight.Black)
                        Text(
                            "El objetivo es ${set.targetReps}; registra el resultado real del top set.",
                            color = CyberMuted,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amrapReps,
                            onValueChange = { amrapReps = it.filter(Char::isDigit) },
                            label = { Text("Reps realizadas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            WorkoutStore.completeCurrentSet(context, amrapReps.toIntOrNull())
                            amrapReps = ""
                        }
                    },
                    enabled = set?.isAmrap != true || amrapReps.toIntOrNull() != null,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text("  SERIE COMPLETADA", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedButton(
                        onClick = { scope.launch { WorkoutStore.postponeCurrentOne(context) } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = null)
                        Text("  APLAZAR")
                    }
                    OutlinedButton(
                        onClick = { showQueue = !showQueue },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.SwapVert, contentDescription = null)
                        Text("  CAMBIAR")
                    }
                }
                nextPending?.let {
                    Text(
                        "Si aplazas, la app continúa con: ${it.name}",
                        color = CyberMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showQueue = !showQueue },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (showQueue) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
                Text(if (showQueue) "  OCULTAR ORDEN" else "  VER / CAMBIAR ORDEN DE EJERCICIOS")
            }
        }

        if (showQueue) {
            item {
                SectionLabel("cola de ejercicios")
                Text(
                    "Gimnasio lleno: toca HACER AHORA para cambiar sin perder ninguna serie. ↑ y ↓ cambian el orden de la cola.",
                    color = CyberMuted,
                    fontSize = 11.sp
                )
            }

            itemsIndexed(state.exercises, key = { _, item -> item.id }) { index, item ->
                val isCurrent = index == state.currentExerciseIndex
                CyberCard(
                    accent = when {
                        item.isCompleted -> CyberSuccess
                        isCurrent -> CyberCyan
                        else -> Color.Transparent
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${index + 1}".padStart(2, '0'),
                            color = if (item.isCompleted) CyberSuccess else CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.padding(5.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.name,
                                color = if (item.isCompleted) CyberSuccess else CyberText,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                "${item.completedSets}/${item.sets.size} series${if (item.isMain531) " · 5/3/1" else ""}",
                                color = CyberMuted,
                                fontSize = 10.sp
                            )
                        }

                        if (!item.isCompleted) {
                            if (!isCurrent) {
                                TextButton(
                                    onClick = { scope.launch { WorkoutStore.selectExercise(context, item.id) } }
                                ) { Text("HACER AHORA") }
                            } else {
                                Text("ACTUAL", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    if (!item.isCompleted) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { scope.launch { WorkoutStore.moveExercise(context, item.id, -1) } },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Subir")
                                Text("SUBIR")
                            }
                            TextButton(
                                onClick = { scope.launch { WorkoutStore.moveExercise(context, item.id, 1) } },
                                enabled = index < state.exercises.lastIndex
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Bajar")
                                Text("BAJAR")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { scope.launch { WorkoutStore.togglePause(context) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Pause, contentDescription = null)
                Text(if (state.paused) "  REANUDAR SESIÓN" else "  PAUSAR SESIÓN")
            }
            TextButton(
                onClick = { scope.launch { WorkoutStore.endWorkout(context) } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = CyberMagenta)
            ) {
                Text("FINALIZAR ENTRENAMIENTO")
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
