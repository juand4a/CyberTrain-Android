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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecnosuper.neon531.domain.FiveThreeOne
import com.tecnosuper.neon531.domain.OneRepMax
import com.tecnosuper.neon531.domain.PlateCalculator
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberNumberField
import com.tecnosuper.neon531.ui.components.CyberStat
import com.tecnosuper.neon531.ui.components.CyberTopBar
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.cleanNumber
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberText

@Composable
fun ToolsScreen(onBack: () -> Unit) = CyberBackground {
    var weight by remember { mutableStateOf("100") }
    var reps by remember { mutableStateOf("5") }
    var targetBar by remember { mutableStateOf("140") }
    var barWeight by remember { mutableStateOf("20") }

    val lifted = weight.toDoubleOrNull() ?: 0.0
    val repetitions = reps.toIntOrNull() ?: 0
    val epley = OneRepMax.epley(lifted, repetitions)
    val brzycki = OneRepMax.brzycki(lifted, repetitions)
    val estimated = OneRepMax.estimated(lifted, repetitions)
    val trainingMax = estimated * 0.90
    val plates = PlateCalculator.calculate(
        target = targetBar.toDoubleOrNull() ?: 0.0,
        bar = barWeight.toDoubleOrNull() ?: 20.0
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            CyberTopBar("RM // LAB", "ESTIMACIONES Y CÁLCULOS DE ENTRENAMIENTO", onBack)
        }

        item {
            CyberCard(accent = CyberCyan) {
                SectionLabel("one rep max")
                Text("ESTIMADOR 1RM", color = CyberText, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("Promedio entre Epley y Brzycki.", color = CyberMuted, fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
                CyberNumberField("Peso levantado (kg)", weight) { weight = it }
                Spacer(Modifier.height(8.dp))
                CyberNumberField("Repeticiones", reps, integer = true) { reps = it }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    CyberStat("1RM", "${cleanNumber(estimated)} kg", Modifier.weight(1f), true)
                    CyberStat("TM 90%", "${cleanNumber(trainingMax)} kg", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Epley ${cleanNumber(epley)} kg  //  Brzycki ${cleanNumber(brzycki)} kg",
                    color = CyberMuted,
                    fontSize = 11.sp
                )
            }
        }

        item {
            CyberCard {
                SectionLabel("percentages")
                OneRepMax.percentTable(estimated).forEach { (percentage, kg) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$percentage%", color = if (percentage >= 90) CyberMagenta else CyberMuted)
                        Text("${cleanNumber(kg)} kg", color = CyberText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            CyberCard {
                SectionLabel("rep equivalents")
                listOf(1, 2, 3, 5, 6, 8, 10, 12).forEach { targetReps ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${targetReps}RM", color = CyberMuted)
                        Text(
                            "${cleanNumber(OneRepMax.estimatedWeightForReps(estimated, targetReps))} kg",
                            color = CyberText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            CyberCard(accent = CyberMagenta) {
                SectionLabel("5/3/1 cycle preview")
                Text("TM ${cleanNumber(trainingMax)} KG", color = CyberMagenta, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(7.dp))
                (1..3).forEach { week ->
                    Text("WEEK $week", color = CyberText, fontWeight = FontWeight.Black)
                    val sets = FiveThreeOne.sets(trainingMax = trainingMax, week = week, rounding = 1.0)
                    Text(
                        sets.joinToString("  ·  ") { "${it.weightLabel}×${it.targetReps}" },
                        color = CyberMuted,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        item {
            CyberCard {
                SectionLabel("plate calculator")
                CyberNumberField("Peso objetivo (kg)", targetBar) { targetBar = it }
                Spacer(Modifier.height(8.dp))
                CyberNumberField("Peso de la barra (kg)", barWeight) { barWeight = it }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (plates.perSide.isEmpty()) "SIN DISCOS" else "POR LADO // ${plates.perSide.joinToString(" + ") { cleanNumber(it) }} kg",
                    color = CyberCyan,
                    fontWeight = FontWeight.Black
                )
                Text("Peso armado ${cleanNumber(plates.achieved)} kg", color = CyberText)
                if (plates.difference != 0.0) {
                    Text("Diferencia ${cleanNumber(plates.difference)} kg", color = CyberMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
