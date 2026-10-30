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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecnosuper.neon531.R
import com.tecnosuper.neon531.data.WorkoutSessionState
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberProgress
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.SystemBadge
import com.tecnosuper.neon531.ui.components.modeLabel
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberLine
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberText

@Composable
fun HomeScreen(
    active: WorkoutSessionState?,
    onStrength: () -> Unit,
    onHypertrophy: () -> Unit,
    onTools: () -> Unit,
    onHistory: () -> Unit,
    onRoutines: () -> Unit,
    onCycleUpdate: () -> Unit,
    onRestSettings: () -> Unit,
    onContinue: () -> Unit
) = CyberBackground {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            HomeHero()
        }

        if (active != null && !active.finished) {
            item {
                val exercise = active.currentExercise
                val set = active.currentSet
                val progress = if (active.totalSets == 0) {
                    0f
                } else {
                    active.completedSets.toFloat() / active.totalSets.toFloat()
                }

                CyberCard(
                    accent = CyberMagenta,
                    modifier = Modifier.clickable(onClick = onContinue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SystemBadge("SESSION // ACTIVE", CyberMagenta)
                            Spacer(Modifier.height(7.dp))
                            Text(
                                exercise?.name ?: active.dayName,
                                color = CyberText,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                buildString {
                                    append(modeLabel(active.mode))
                                    append(" · ${active.dayName}")
                                    active.week?.let { append(" · W$it") }
                                },
                                color = CyberMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (set != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    set.weightLabel,
                                    color = CyberCyan,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "× ${set.targetReps}",
                                    color = CyberText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    CyberProgress(progress, "${active.completedSets}/${active.totalSets} SERIES")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "CONTINUAR ENTRENAMIENTO  →",
                        color = CyberCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item { SectionLabel("iniciar sesión") }

        item {
            TrainingModeCard(
                index = "01",
                title = "FUERZA",
                protocol = "5 / 3 / 1",
                description = "La carga cambia serie a serie y semana a semana. La app calcula y guía cada set.",
                accent = CyberCyan,
                onClick = onStrength
            )
        }

        item {
            TrainingModeCard(
                index = "02",
                title = "HIPERTROFIA",
                protocol = "VOLUMEN",
                description = "Rutina completa editable con peso, series, reps, descanso y orden personalizado.",
                accent = CyberMagenta,
                onClick = onHypertrophy
            )
        }

        item { SectionLabel("personalizar") }

        item {
            RoutineVisualCard(onClick = onRoutines)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCycleUpdate,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null)
                    Text("  ACTUALIZAR 5·3·1")
                }
                OutlinedButton(
                    onClick = onRestSettings,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("  AVISOS")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTools,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Rounded.ShowChart, contentDescription = null)
                    Text("  RM / TOOLS")
                }

                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null)
                    Text("  HISTORIAL")
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}

@Composable
private fun HomeHero() {
    val shape = CutCornerShape(topEnd = 26.dp, bottomStart = 26.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(shape)
            .border(1.dp, CyberLine, shape)
    ) {
        Image(
            painter = painterResource(R.drawable.cyber_city),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.08f),
                        0.48f to Color.Black.copy(alpha = 0.18f),
                        1f to Color.Black.copy(alpha = 0.90f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
        ) {
            SystemBadge("TRAINING OS // ONLINE")
            Spacer(Modifier.height(8.dp))
            Text(
                "CYBERTRAIN",
                color = CyberText,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                "BUILD THE NEXT SET",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
        }
    }
}

@Composable
private fun RoutineVisualCard(onClick: () -> Unit) {
    val shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(shape)
            .border(1.dp, CyberCyan.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick)
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
                        0f to Color.Black.copy(alpha = 0.92f),
                        0.65f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black.copy(alpha = 0.18f)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = CyberCyan)
            Spacer(Modifier.padding(5.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "ROUTINE CONFIG",
                    color = CyberText,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
                Text(
                    "Agregar · renombrar · ordenar · duplicar · borrar · configurar 5/3/1",
                    color = CyberMuted,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "ABRIR EDITOR  →",
                    color = CyberCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TrainingModeCard(
    index: String,
    title: String,
    protocol: String,
    description: String,
    accent: Color,
    onClick: () -> Unit
) {
    CyberCard(accent = accent, modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                index,
                color = accent.copy(alpha = 0.50f),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.padding(7.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (title == "FUERZA") Icons.Rounded.Bolt else Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = accent
                    )
                    Spacer(Modifier.padding(3.dp))
                    Text(title, color = CyberText, style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    protocol,
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(7.dp))
                Text(description, color = CyberMuted, fontSize = 12.sp)
            }
            Text("›", color = accent, fontSize = 30.sp)
        }
    }
}
