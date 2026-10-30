package com.tecnosuper.neon531.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecnosuper.neon531.R
import com.tecnosuper.neon531.data.ExerciseTemplate
import com.tecnosuper.neon531.data.FiveThreeOneSource
import com.tecnosuper.neon531.data.MainLift
import com.tecnosuper.neon531.data.Manual531Set
import com.tecnosuper.neon531.data.Manual531Week
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.domain.RoutineFactory
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberChoice
import com.tecnosuper.neon531.ui.components.CyberNumberField
import com.tecnosuper.neon531.ui.components.CyberStat
import com.tecnosuper.neon531.ui.components.CyberTopBar
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.SystemBadge
import com.tecnosuper.neon531.ui.components.cleanNumber
import com.tecnosuper.neon531.ui.components.formatSeconds
import com.tecnosuper.neon531.ui.components.mainLiftLabel
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberLine
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberSurface2
import com.tecnosuper.neon531.ui.theme.CyberText
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(onBack: () -> Unit) = CyberBackground {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val routines by WorkoutStore.routines(context)
        .collectAsStateWithLifecycle(initialValue = RoutineFactory.defaultRoutineDays())

    var selectedDay by remember { mutableStateOf(0) }
    var editingExercise by remember { mutableStateOf<ExerciseTemplate?>(null) }
    var creatingExercise by remember { mutableStateOf(false) }
    var reorderMode by remember { mutableStateOf(false) }
    var renameDay by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<ExerciseTemplate?>(null) }
    var resetRequested by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val currentDay = routines.firstOrNull { it.dayIndex == selectedDay }
        ?: RoutineFactory.defaultRoutineDays().first { it.dayIndex == selectedDay }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(1800)
            statusMessage = null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CyberTopBar(
                title = "EDITAR RUTINA",
                subtitle = "Todo lo que cambies queda guardado",
                onBack = onBack
            )
            statusMessage?.let {
                CyberCard(accent = CyberCyan) {
                    SystemBadge("GUARDADO // $it")
                }
            }
        }

        item {
            RoutineEditorHero()
        }

        item {
            SectionLabel("elige día")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                routines.sortedBy { it.dayIndex }.take(5).forEach { day ->
                    CyberChoice(
                        text = "D${day.dayIndex + 1}",
                        selected = selectedDay == day.dayIndex,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedDay = day.dayIndex
                        editingExercise = null
                        creatingExercise = false
                        reorderMode = false
                    }
                }
            }
        }

        item {
            CyberCard(accent = CyberCyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("DÍA ${selectedDay + 1}", color = CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text(currentDay.dayName, color = CyberText, fontSize = 21.sp, fontWeight = FontWeight.Black)
                        Text(
                            "${currentDay.exercises.size} ejercicios · toca uno para editarlo",
                            color = CyberMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = { renameDay = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Renombrar día", tint = CyberCyan)
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        creatingExercise = true
                        editingExercise = null
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("  AÑADIR", fontWeight = FontWeight.Black)
                }
                OutlinedButton(
                    onClick = { reorderMode = !reorderMode },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Rounded.DragHandle, contentDescription = null)
                    Text(if (reorderMode) "  LISTO" else "  ORDENAR")
                }
            }
        }

        item {
            SectionLabel(if (reorderMode) "modo ordenar" else "ejercicios")
            Text(
                if (reorderMode) {
                    "Usa SUBIR / BAJAR. El nuevo orden se guarda al instante."
                } else {
                    "Toca una tarjeta para cambiar nombre, peso, series, reps, descanso, posición o 5/3/1."
                },
                color = CyberMuted,
                fontSize = 11.sp
            )
        }

        if (currentDay.exercises.isEmpty()) {
            item {
                CyberCard(accent = CyberMagenta) {
                    Text("DÍA VACÍO", color = CyberMagenta, fontWeight = FontWeight.Black)
                    Text("Pulsa AÑADIR para crear el primer ejercicio.", color = CyberMuted, fontSize = 12.sp)
                }
            }
        }

        itemsIndexed(currentDay.exercises, key = { _, item -> item.id }) { index, item ->
            CyberCard(
                accent = if (item.mainLift != null) CyberMagenta else Color.Transparent,
                modifier = if (!reorderMode) Modifier.clickable { editingExercise = item } else Modifier
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${index + 1}".padStart(2, '0'),
                        color = CyberCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.padding(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, color = CyberText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text(
                            if (item.mainLift != null) {
                                "5/3/1 · ${mainLiftLabel(item.mainLift)} · ${if (item.fiveThreeOneSource == FiveThreeOneSource.MANUAL) "MANUAL" else "AUTO"}"
                            } else {
                                "${item.weightLabel} · ${item.series} × ${item.reps} · ${formatSeconds(item.restSeconds)} descanso"
                            },
                            color = if (item.mainLift != null) CyberMagenta else CyberMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!reorderMode) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = CyberCyan)
                    }
                }

                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CyberStat("PESO", item.weightLabel, Modifier.weight(1f))
                    CyberStat("SERIES", item.series.toString(), Modifier.weight(1f))
                    CyberStat("REPS", item.reps, Modifier.weight(1f))
                    CyberStat("REST", formatSeconds(item.restSeconds), Modifier.weight(1f))
                }

                if (reorderMode) {
                    Spacer(Modifier.height(9.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    WorkoutStore.moveRoutineExercise(context, selectedDay, item.id, -1)
                                    statusMessage = "Orden actualizado"
                                }
                            },
                            enabled = index > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null)
                            Text("  SUBIR")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    WorkoutStore.moveRoutineExercise(context, selectedDay, item.id, 1)
                                    statusMessage = "Orden actualizado"
                                }
                            },
                            enabled = index < currentDay.exercises.lastIndex,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
                            Text("  BAJAR")
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(5.dp))
            TextButton(
                onClick = { resetRequested = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = CyberMagenta)
            ) {
                Icon(Icons.Rounded.Restore, contentDescription = null)
                Text("  RESTAURAR ESTE DÍA")
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (creatingExercise || editingExercise != null) {
        val existing = editingExercise
        val initialPosition = existing?.let { currentDay.exercises.indexOfFirst { row -> row.id == it.id } }
            ?.takeIf { it >= 0 }
            ?: currentDay.exercises.size

        ModalBottomSheet(
            onDismissRequest = {
                creatingExercise = false
                editingExercise = null
            },
            containerColor = CyberSurface2,
            contentColor = CyberText
        ) {
            ExerciseEditorSheet(
                initial = existing,
                initialPosition = initialPosition,
                maxPosition = currentDay.exercises.size,
                onCancel = {
                    creatingExercise = false
                    editingExercise = null
                },
                onDuplicate = existing?.let { source ->
                    {
                        scope.launch {
                            WorkoutStore.duplicateRoutineExercise(context, selectedDay, source.id)
                            statusMessage = "Ejercicio duplicado"
                            editingExercise = null
                        }
                    }
                },
                onDelete = existing?.let { source ->
                    {
                        deleteCandidate = source
                        editingExercise = null
                    }
                },
                onSave = { draft, targetPosition ->
                    scope.launch {
                        WorkoutStore.upsertRoutineExercise(context, selectedDay, draft)
                        WorkoutStore.moveRoutineExerciseToPosition(
                            context = context,
                            dayIndex = selectedDay,
                            exerciseId = draft.id,
                            targetPosition = targetPosition
                        )
                        statusMessage = if (existing == null) "Ejercicio agregado" else "Cambios guardados"
                        creatingExercise = false
                        editingExercise = null
                    }
                }
            )
        }
    }

    if (renameDay) {
        var newName by remember(currentDay.dayName) { mutableStateOf(currentDay.dayName) }
        AlertDialog(
            onDismissRequest = { renameDay = false },
            title = { Text("Renombrar día") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        WorkoutStore.renameRoutineDay(context, selectedDay, newName)
                        statusMessage = "Día renombrado"
                        renameDay = false
                    }
                }) { Text("GUARDAR") }
            },
            dismissButton = { TextButton(onClick = { renameDay = false }) { Text("CANCELAR") } }
        )
    }

    deleteCandidate?.let { candidate ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Eliminar ejercicio") },
            text = { Text("¿Eliminar ${candidate.name} de este día?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            WorkoutStore.deleteRoutineExercise(context, selectedDay, candidate.id)
                            statusMessage = "Ejercicio eliminado"
                            deleteCandidate = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CyberMagenta)
                ) { Text("ELIMINAR") }
            },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("CANCELAR") } }
        )
    }

    if (resetRequested) {
        AlertDialog(
            onDismissRequest = { resetRequested = false },
            title = { Text("Restaurar día") },
            text = { Text("Se perderán los cambios personalizados de ${currentDay.dayName}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            WorkoutStore.resetRoutineDay(context, selectedDay)
                            statusMessage = "Rutina restaurada"
                            resetRequested = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CyberMagenta)
                ) { Text("RESTAURAR") }
            },
            dismissButton = { TextButton(onClick = { resetRequested = false }) { Text("CANCELAR") } }
        )
    }
}

@Composable
private fun ExerciseEditorSheet(
    initial: ExerciseTemplate?,
    initialPosition: Int,
    maxPosition: Int,
    onCancel: () -> Unit,
    onDuplicate: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onSave: (ExerciseTemplate, Int) -> Unit
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var weightLabel by remember(initial?.id) { mutableStateOf(initial?.weightLabel ?: "") }
    var weightKg by remember(initial?.id) { mutableStateOf(initial?.weightKg?.let(::cleanNumber) ?: "") }
    var series by remember(initial?.id) { mutableStateOf(initial?.series?.toString() ?: "3") }
    var reps by remember(initial?.id) { mutableStateOf(initial?.reps ?: "8") }
    var restSeconds by remember(initial?.id) { mutableStateOf(initial?.restSeconds?.toString() ?: "180") }
    var position by remember(initial?.id) { mutableStateOf((initialPosition + 1).toString()) }
    var mainLift by remember(initial?.id) { mutableStateOf(initial?.mainLift) }
    var source by remember(initial?.id) {
        mutableStateOf(initial?.fiveThreeOneSource ?: FiveThreeOneSource.AUTO)
    }
    var manualWeeks by remember(initial?.id) {
        mutableStateOf(normalizeManualWeeks(initial?.manual531Weeks.orEmpty()))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            SystemBadge(if (initial == null) "CREATE // EXERCISE" else "EDIT // EXERCISE")
            Spacer(Modifier.height(4.dp))
            Text(
                if (initial == null) "Nuevo ejercicio" else initial.name,
                color = CyberText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Edita lo que necesites. Los cambios quedan guardados al cerrar la app.",
                color = CyberMuted,
                fontSize = 11.sp
            )
        }

        item {
            SectionLabel("información")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del ejercicio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionLabel("datos normales / hipertrofia")
            Text(
                "Estos datos se usan siempre en HIPERTROFIA y también en FUERZA cuando el ejercicio está como NORMAL.",
                color = CyberMuted,
                fontSize = 10.sp
            )
            OutlinedTextField(
                value = weightLabel,
                onValueChange = { weightLabel = it },
                label = { Text("Texto del peso") },
                supportingText = { Text("Ej: 95 kg · Max Power · -2 placas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            CyberNumberField("Peso numérico en kg (opcional)", weightKg) { weightKg = it }

            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CyberNumberField("Series", series, Modifier.weight(1f), integer = true) { series = it }
                OutlinedTextField(
                    value = reps,
                    onValueChange = { value -> reps = value.filter { it.isDigit() || it == '+' || it == '-' } },
                    label = { Text("Reps") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CyberNumberField("Descanso (s)", restSeconds, Modifier.weight(1f), integer = true) { restSeconds = it }
                CyberNumberField("Posición", position, Modifier.weight(1f), integer = true) { position = it }
            }
        }

        item {
            SectionLabel("modo fuerza")
            Text(
                "NORMAL conserva las series anteriores. SQUAT, BENCH o DEADLIFT convierten este ejercicio en principal 5/3/1.",
                color = CyberMuted,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CyberChoice("NORMAL", mainLift == null, Modifier.weight(1f)) { mainLift = null }
                CyberChoice("SQUAT", mainLift == MainLift.SQUAT, Modifier.weight(1f)) { mainLift = MainLift.SQUAT }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CyberChoice("BENCH", mainLift == MainLift.BENCH, Modifier.weight(1f)) { mainLift = MainLift.BENCH }
                CyberChoice("DEADLIFT", mainLift == MainLift.DEADLIFT, Modifier.weight(1f)) { mainLift = MainLift.DEADLIFT }
            }
        }

        if (mainLift != null) {
            item {
                SectionLabel("cómo obtener los pesos 5/3/1")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    CyberChoice(
                        text = "AUTO · TM",
                        selected = source == FiveThreeOneSource.AUTO,
                        modifier = Modifier.weight(1f)
                    ) { source = FiveThreeOneSource.AUTO }
                    CyberChoice(
                        text = "MANUAL",
                        selected = source == FiveThreeOneSource.MANUAL,
                        modifier = Modifier.weight(1f)
                    ) { source = FiveThreeOneSource.MANUAL }
                }

                Spacer(Modifier.height(7.dp))
                CyberCard(accent = if (source == FiveThreeOneSource.MANUAL) CyberMagenta else CyberCyan) {
                    Text(
                        if (source == FiveThreeOneSource.AUTO) "CÁLCULO AUTOMÁTICO" else "CARGAS MANUALES",
                        color = if (source == FiveThreeOneSource.AUTO) CyberCyan else CyberMagenta,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        if (source == FiveThreeOneSource.AUTO) {
                            "CyberTrain calculará los 3 pesos de cada semana desde tu Training Max y el redondeo configurado."
                        } else {
                            "Escribe tus pesos y reps exactamente como ya los tienes. CyberTrain usará esos valores sin recalcularlos."
                        },
                        color = CyberMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (mainLift != null && source == FiveThreeOneSource.MANUAL) {
            item {
                SectionLabel("5/3/1 manual · semanas 1 a 3")
                Text(
                    "Cada semana tiene 3 series principales. Puedes escribir 5+, 3+ o 1+ para marcar una serie AMRAP. Si dejas una semana incompleta, la app usa AUTO solo para esa semana como respaldo.",
                    color = CyberMuted,
                    fontSize = 10.sp
                )
            }

            itemsIndexed(manualWeeks, key = { _, item -> item.week }) { weekIndex, weekData ->
                Manual531WeekCard(
                    week = weekData,
                    onChange = { updated ->
                        manualWeeks = manualWeeks.toMutableList().also { it[weekIndex] = updated }
                    }
                )
            }
        }

        item {
            Button(
                onClick = {
                    val finalName = name.trim()
                    if (finalName.isBlank()) return@Button

                    val numericWeight = weightKg.toDoubleOrNull()
                    val finalLabel = weightLabel.trim().ifBlank {
                        numericWeight?.let { "${cleanNumber(it)} kg" } ?: "—"
                    }
                    val largestPosition = if (initial == null) maxPosition + 1 else maxPosition.coerceAtLeast(1)
                    val finalPosition = (position.toIntOrNull() ?: initialPosition + 1)
                        .coerceIn(1, largestPosition.coerceAtLeast(1))

                    onSave(
                        ExerciseTemplate(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            name = finalName,
                            weightLabel = finalLabel,
                            weightKg = numericWeight,
                            series = series.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            reps = reps.trim().ifBlank { "1" },
                            restSeconds = restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                            mainLift = mainLift,
                            fiveThreeOneSource = if (mainLift == null) FiveThreeOneSource.AUTO else source,
                            // Keep manual values even while AUTO is selected so the user can
                            // switch back to MANUAL later without retyping all three weeks.
                            manual531Weeks = if (mainLift != null) manualWeeks else emptyList()
                        ),
                        finalPosition - 1
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Text("  GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
            }
        }

        if (initial != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onDuplicate?.let { duplicate ->
                        OutlinedButton(onClick = duplicate, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                            Text("  DUPLICAR")
                        }
                    }
                    onDelete?.let { delete ->
                        OutlinedButton(
                            onClick = delete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberMagenta)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                            Text("  ELIMINAR")
                        }
                    }
                }
            }
        }

        item {
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("CANCELAR") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Manual531WeekCard(
    week: Manual531Week,
    onChange: (Manual531Week) -> Unit
) {
    var weightInputs by remember(week.week) {
        mutableStateOf(week.sets.take(3).map { it.weightKg?.let(::cleanNumber) ?: "" })
    }

    CyberCard(accent = CyberMagenta) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "WEEK ${week.week}",
                    color = CyberMagenta,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    when (week.week) {
                        1 -> "Referencia: 5 / 5 / 5+"
                        2 -> "Referencia: 3 / 3 / 3+"
                        else -> "Referencia: 5 / 3 / 1+"
                    },
                    color = CyberMuted,
                    fontSize = 10.sp
                )
            }
            SystemBadge("3 SETS")
        }

        Spacer(Modifier.height(8.dp))
        week.sets.take(3).forEachIndexed { setIndex, set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "S${setIndex + 1}",
                    color = CyberCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                CyberNumberField(
                    label = "kg",
                    value = weightInputs.getOrElse(setIndex) { "" },
                    modifier = Modifier.weight(1f)
                ) { value ->
                    weightInputs = weightInputs.toMutableList().also { rows ->
                        while (rows.size < 3) rows.add("")
                        rows[setIndex] = value
                    }
                    onChange(
                        week.copy(
                            sets = week.sets.toMutableList().also { rows ->
                                rows[setIndex] = set.copy(weightKg = value.toDoubleOrNull())
                            }
                        )
                    )
                }
                OutlinedTextField(
                    value = set.reps,
                    onValueChange = { value ->
                        val filtered = value.filter { it.isDigit() || it == '+' || it == '-' }.take(4)
                        onChange(
                            week.copy(
                                sets = week.sets.toMutableList().also { rows ->
                                    rows[setIndex] = set.copy(reps = filtered)
                                }
                            )
                        )
                    },
                    label = { Text("reps") },
                    singleLine = true,
                    modifier = Modifier.weight(0.85f)
                )
            }
            if (setIndex < 2) Spacer(Modifier.height(7.dp))
        }
    }
}

private fun normalizeManualWeeks(input: List<Manual531Week>): List<Manual531Week> {
    val defaults = listOf(
        Manual531Week(
            week = 1,
            sets = listOf(
                Manual531Set(null, "5"),
                Manual531Set(null, "5"),
                Manual531Set(null, "5+")
            )
        ),
        Manual531Week(
            week = 2,
            sets = listOf(
                Manual531Set(null, "3"),
                Manual531Set(null, "3"),
                Manual531Set(null, "3+")
            )
        ),
        Manual531Week(
            week = 3,
            sets = listOf(
                Manual531Set(null, "5"),
                Manual531Set(null, "3"),
                Manual531Set(null, "1+")
            )
        )
    )

    return defaults.map { defaultWeek ->
        val stored = input.firstOrNull { it.week == defaultWeek.week } ?: return@map defaultWeek
        defaultWeek.copy(
            sets = defaultWeek.sets.mapIndexed { index, defaultSet ->
                stored.sets.getOrNull(index)?.let { saved ->
                    Manual531Set(
                        weightKg = saved.weightKg,
                        reps = saved.reps.ifBlank { defaultSet.reps }
                    )
                } ?: defaultSet
            }
        )
    }
}


@Composable
private fun RoutineEditorHero() {
    val shape = CutCornerShape(topEnd = 22.dp, bottomStart = 22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(shape)
            .border(1.dp, CyberLine, shape)
    ) {
        Image(
            painter = painterResource(R.drawable.cyber_moon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.90f),
                        0.58f to Color.Black.copy(alpha = 0.52f),
                        1f to Color.Black.copy(alpha = 0.22f)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(15.dp)
        ) {
            SystemBadge("ROUTINE // EDIT MODE")
            Spacer(Modifier.height(6.dp))
            Text(
                "TOCA · EDITA · REORDENA",
                color = CyberText,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
            Text(
                "Los cambios quedan guardados automáticamente.",
                color = CyberMuted,
                fontSize = 10.sp
            )
        }
    }
}
