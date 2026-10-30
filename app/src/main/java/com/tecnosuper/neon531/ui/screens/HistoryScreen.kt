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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecnosuper.neon531.data.WorkoutStore
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberStat
import com.tecnosuper.neon531.ui.components.CyberTopBar
import com.tecnosuper.neon531.ui.components.cleanNumber
import com.tecnosuper.neon531.ui.components.modeLabel
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(onBack: () -> Unit) = CyberBackground {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val history by WorkoutStore.history(context).collectAsStateWithLifecycle(initialValue = emptyList())
    var clearRequested by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CyberTopBar(
                title = "HISTORY // LOG",
                subtitle = "ÚLTIMAS ${history.size} SESIONES",
                onBack = onBack,
                trailing = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { clearRequested = true }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Limpiar", tint = CyberMagenta)
                        }
                    }
                }
            )
        }

        if (history.isEmpty()) {
            item {
                CyberCard {
                    Text("SIN DATOS", color = CyberCyan, fontWeight = FontWeight.Black)
                    Text("Las sesiones finalizadas aparecerán aquí.", color = CyberMuted, fontSize = 12.sp)
                }
            }
        } else {
            itemsIndexed(history) { index, entry ->
                CyberCard(accent = if (index == 0) CyberCyan else androidx.compose.ui.graphics.Color.Transparent) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.dayName, color = CyberText, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text(
                                "${modeLabel(entry.mode)}${entry.week?.let { " · W$it" } ?: ""}",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(formatter.format(Date(entry.completedAt)), color = CyberMuted, fontSize = 11.sp)
                        }
                        Text("#${(index + 1).toString().padStart(2, '0')}", color = CyberMuted, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CyberStat("SETS", entry.totalSets.toString(), Modifier.weight(1f))
                        CyberStat("VOLUME", "${cleanNumber(entry.totalVolumeKg)} kg", Modifier.weight(2f), index == 0)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(26.dp)) }
    }

    if (clearRequested) {
        AlertDialog(
            onDismissRequest = { clearRequested = false },
            title = { Text("Limpiar historial") },
            text = { Text("Se eliminarán todas las sesiones guardadas del historial.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { WorkoutStore.clearHistory(context) }
                    clearRequested = false
                }) { Text("ELIMINAR", color = CyberMagenta) }
            },
            dismissButton = { TextButton(onClick = { clearRequested = false }) { Text("CANCELAR") } }
        )
    }
}
