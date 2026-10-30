package com.tecnosuper.neon531.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
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
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.tecnosuper.neon531.service.WorkoutService
import com.tecnosuper.neon531.ui.components.CyberBackground
import com.tecnosuper.neon531.ui.components.CyberCard
import com.tecnosuper.neon531.ui.components.CyberTopBar
import com.tecnosuper.neon531.ui.components.SectionLabel
import com.tecnosuper.neon531.ui.components.SystemBadge
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberText
import kotlinx.coroutines.launch

@Composable
fun RestAlertSettingsScreen(onBack: () -> Unit) = CyberBackground {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var volume by remember { mutableStateOf(10f) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sound = com.tecnosuper.neon531.data.WorkoutStore.loadRestSoundEnabled(context)
        vibration = com.tecnosuper.neon531.data.WorkoutStore.loadRestVibrationEnabled(context)
        volume = com.tecnosuper.neon531.data.WorkoutStore.loadRestSoundVolume(context).toFloat()
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        CyberTopBar("AVISOS DE DESCANSO", "Sonido suave sin detener la música", onBack)
        androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                CyberCard(accent = CyberCyan) {
                    SystemBadge("AUDIO // MEDIA MIX", CyberCyan)
                    Spacer(Modifier.height(8.dp))
                    Text("CyberTrain no solicita Audio Focus para el aviso. El beep se mezcla con la música y no manda a Spotify/YouTube Music a pausa.", color = CyberMuted, fontSize = 11.sp)
                }
            }
            item {
                SectionLabel("controles")
                CyberCard {
                    AlertToggle("SONIDO AL LLEGAR A 00:00", sound, "Alarma GYM durante máximo 7 segundos · 10%") { sound = it; saved = false }
                    AlertToggle("VIBRACIÓN", vibration, "Pulso de 70 ms") { vibration = it; saved = false }
                }
            }
            item {
                CyberCard {
                    SystemBadge("VOLUMEN FIJO // ${volume.toInt()}%", CyberMagenta)
                    Spacer(Modifier.height(8.dp))
                    Text("La alarma utiliza el MP3 GYM al ${volume.toInt()}% del volumen del reproductor.", color = CyberText, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Text("La música no recibe Audio Focus y continúa reproduciéndose.", color = CyberMuted, fontSize = 10.sp)
                }
            }
            item {
                val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                CyberCard(accent = if (notificationsEnabled) CyberCyan else CyberMagenta) {
                    SystemBadge(
                        if (notificationsEnabled) "NOTIFICACIONES // ACTIVAS" else "NOTIFICACIONES // BLOQUEADAS",
                        if (notificationsEnabled) CyberCyan else CyberMagenta
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (notificationsEnabled) {
                            "La sesión puede mantenerse visible en la pantalla bloqueada. En el sistema también debe estar permitido mostrar contenido en pantalla de bloqueo."
                        } else {
                            "Android está bloqueando las notificaciones de CyberTrain. Actívalas para que el control de la rutina aparezca cuando la pantalla esté apagada."
                        },
                        color = CyberMuted,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    if (!notificationsEnabled) {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta, contentColor = Color.White)
                        ) {
                            Text("ACTIVAR NOTIFICACIONES", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
            item {
                val exactAlarmsAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    alarmManager?.canScheduleExactAlarms() == true
                } else {
                    true
                }

                CyberCard(accent = if (exactAlarmsAllowed) CyberCyan else CyberMagenta) {
                    SystemBadge(
                        if (exactAlarmsAllowed) "ALARMA EXACTA // ACTIVA" else "ALARMA EXACTA // REQUIERE PERMISO",
                        if (exactAlarmsAllowed) CyberCyan else CyberMagenta
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (exactAlarmsAllowed) {
                            "El temporizador puede despertar el teléfono durante el modo ahorro/Doze."
                        } else {
                            "Activa Alarmas y recordatorios para que el descanso pueda despertar el teléfono con máxima precisión."
                        },
                        color = CyberMuted,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    if (!exactAlarmsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta, contentColor = Color.White)
                        ) {
                            Text("ACTIVAR ALARMAS Y RECORDATORIOS", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            com.tecnosuper.neon531.data.WorkoutStore.saveRestAlertSettings(context, sound, vibration, volume.toInt())
                            saved = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) { Text("GUARDAR CONFIGURACIÓN", fontWeight = FontWeight.Black) }
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            com.tecnosuper.neon531.data.WorkoutStore.saveRestAlertSettings(context, sound, vibration, volume.toInt())
                            WorkoutService.testRestAlert(context, sound, vibration, volume.toInt())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta, contentColor = Color.White)
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.NotificationsActive, contentDescription = null)
                    Text("  PROBAR AVISO", fontWeight = FontWeight.Black)
                }
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                ) {
                    Text("AJUSTES DE NOTIFICACIÓN / PANTALLA BLOQUEADA", fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
            item {
                if (saved) Text("✓ Configuración guardada.", color = CyberCyan, fontWeight = FontWeight.Black, fontSize = 11.sp)
                Spacer(Modifier.height(25.dp))
            }
        }
    }
}

@Composable
private fun AlertToggle(title: String, checked: Boolean, subtitle: String, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = CyberText, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Text(subtitle, color = CyberMuted, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
