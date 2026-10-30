package com.tecnosuper.neon531.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.tecnosuper.neon531.data.MainLift
import com.tecnosuper.neon531.data.TrainingMaxes
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.domain.FiveThreeOne
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberNumberField
import com.tecnosuper.neon531.ui.components.CyberTopBar
import com.tecnosuper.neon531.ui.components.FiveThreeOneSetStrip
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.SystemBadge
import com.tecnosuper.neon531.ui.components.cleanNumber
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberText
import kotlinx.coroutines.launch

@Composable
fun CycleUpdateScreen(onBack: () -> Unit) = CyberBackground {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var squat by remember { mutableStateOf("135") }
    var bench by remember { mutableStateOf("97") }
    var deadlift by remember { mutableStateOf("175") }
    var squatAdd by remember { mutableStateOf("5") }
    var benchAdd by remember { mutableStateOf("2.5") }
    var deadliftAdd by remember { mutableStateOf("5") }
    var rounding by remember { mutableStateOf("1") }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val tm = WorkoutStore.loadTrainingMaxes(context)
        squat = cleanNumber(tm.squat)
        bench = cleanNumber(tm.bench)
        deadlift = cleanNumber(tm.deadlift)
        rounding = cleanNumber(WorkoutStore.loadRounding(context))
    }

    val s = squat.toDoubleOrNull() ?: 0.0
    val b = bench.toDoubleOrNull() ?: 0.0
    val d = deadlift.toDoubleOrNull() ?: 0.0
    val sAdd = squatAdd.toDoubleOrNull() ?: 0.0
    val bAdd = benchAdd.toDoubleOrNull() ?: 0.0
    val dAdd = deadliftAdd.toDoubleOrNull() ?: 0.0
    val r = rounding.toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0
    val next = TrainingMaxes(s + sAdd, b + bAdd, d + dAdd)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CyberTopBar(
                title = "ACTUALIZAR 5·3·1",
                subtitle = "Decide cuánto sube cada levantamiento",
                onBack = onBack
            )
        }

        item {
            CyberCard(accent = CyberMagenta) {
                SystemBadge("NUEVO CICLO // INCREMENTO INDIVIDUAL", CyberMagenta)
                Spacer(Modifier.height(7.dp))
                Text(
                    "No existe una subida global. Puedes subir 5 kg en peso muerto, 2.5 kg en press y cualquier otro valor en sentadilla.",
                    color = CyberMuted,
                    fontSize = 11.sp
                )
            }
        }

        item {
            SectionLabel("sentadilla")
            CycleLiftCard(
                name = "SENTADILLA",
                current = s,
                increment = squatAdd,
                next = next.squat,
                onIncrement = { squatAdd = it },
                onMinus = { squatAdd = ((sAdd - 0.5).coerceAtLeast(0.0)).let(::cleanNumber) },
                onPlus = { squatAdd = cleanNumber(sAdd + 0.5) }
            )
        }

        item {
            SectionLabel("press banca")
            CycleLiftCard(
                name = "PRESS",
                current = b,
                increment = benchAdd,
                next = next.bench,
                onIncrement = { benchAdd = it },
                onMinus = { benchAdd = ((bAdd - 0.5).coerceAtLeast(0.0)).let(::cleanNumber) },
                onPlus = { benchAdd = cleanNumber(bAdd + 0.5) }
            )
        }

        item {
            SectionLabel("peso muerto")
            CycleLiftCard(
                name = "PESO MUERTO",
                current = d,
                increment = deadliftAdd,
                next = next.deadlift,
                onIncrement = { deadliftAdd = it },
                onMinus = { deadliftAdd = ((dAdd - 0.5).coerceAtLeast(0.0)).let(::cleanNumber) },
                onPlus = { deadliftAdd = cleanNumber(dAdd + 0.5) }
            )
        }

        item {
            SectionLabel("vista previa del nuevo ciclo")
            CyclePreview("SENTADILLA · TM ${cleanNumber(next.squat)}", next.squat, r)
            Spacer(Modifier.height(8.dp))
            CyclePreview("PRESS · TM ${cleanNumber(next.bench)}", next.bench, r)
            Spacer(Modifier.height(8.dp))
            CyclePreview("PESO MUERTO · TM ${cleanNumber(next.deadlift)}", next.deadlift, r)
        }

        item {
            CyberCard {
                Text("REDONDEO", color = CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
                CyberNumberField("kg", rounding, Modifier.fillMaxWidth()) { rounding = it }
            }
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        WorkoutStore.applyCycleTrainingMaxes(context, next.squat, next.bench, next.deadlift, r)
                        saved = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Text("  GUARDAR NUEVO CICLO", fontWeight = FontWeight.Black)
            }
            if (saved) {
                Text("✓ Training Max actualizado. Las próximas rutinas AUTO usarán las nuevas cargas.", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun CycleLiftCard(
    name: String,
    current: Double,
    increment: String,
    next: Double,
    onIncrement: (String) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    CyberCard(accent = CyberCyan) {
        Text(name, color = CyberText, fontWeight = FontWeight.Black, fontSize = 17.sp)
        Text("TM ACTUAL · ${cleanNumber(current)} kg", color = CyberMuted, fontSize = 10.sp)
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(onClick = onMinus, modifier = Modifier.weight(0.8f)) {
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
            }
            CyberNumberField("SUBIR (kg)", increment, Modifier.weight(1.4f)) { onIncrement(it) }
            OutlinedButton(onClick = onPlus, modifier = Modifier.weight(0.8f)) {
                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("NUEVO TM", color = CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Text("${cleanNumber(next)} kg", color = CyberCyan, fontSize = 27.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CyclePreview(title: String, tm: Double, rounding: Double) {
    CyberCard {
        Text(title, color = CyberText, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        (1..3).forEach { week ->
            val sets = FiveThreeOne.sets(tm, week, rounding, deadliftWeek3ExactPhoto = false)
            Text("W$week", color = CyberMagenta, fontWeight = FontWeight.Black)
            FiveThreeOneSetStrip(sets)
            Spacer(Modifier.height(5.dp))
        }
    }
}
