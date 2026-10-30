package com.tecnosuper.neon531.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecnosuper.neon531.data.MainLift
import com.tecnosuper.neon531.data.WorkoutMode
import com.tecnosuper.neon531.data.WorkoutSetState
import com.tecnosuper.neon531.ui.theme.CyberBackground as CyberBackgroundColor
import com.tecnosuper.neon531.ui.theme.CyberBackground2
import com.tecnosuper.neon531.ui.theme.CyberCyan
import com.tecnosuper.neon531.ui.theme.CyberCyanDark
import com.tecnosuper.neon531.ui.theme.CyberLine
import com.tecnosuper.neon531.ui.theme.CyberMagenta
import com.tecnosuper.neon531.ui.theme.CyberMuted
import com.tecnosuper.neon531.ui.theme.CyberSuccess
import com.tecnosuper.neon531.ui.theme.CyberSurface
import com.tecnosuper.neon531.ui.theme.CyberSurface2
import com.tecnosuper.neon531.ui.theme.CyberText
import java.util.Locale
import kotlin.math.abs

@Composable
fun CyberBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CyberBackgroundColor, CyberBackground2, CyberBackgroundColor)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        CyberAtmosphere()
        content()
    }
}

@Composable
private fun CyberAtmosphere() {
    Canvas(Modifier.fillMaxSize()) {
        val step = 64.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = CyberCyan.copy(alpha = 0.018f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }

        drawLine(
            color = CyberCyan.copy(alpha = 0.11f),
            start = Offset(size.width * 0.68f, 0f),
            end = Offset(size.width, size.width * 0.32f),
            strokeWidth = 2f
        )
        drawLine(
            color = CyberMagenta.copy(alpha = 0.045f),
            start = Offset(0f, size.height * 0.84f),
            end = Offset(size.width * 0.30f, size.height * 0.73f),
            strokeWidth = 2f
        )
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    accent: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp)
    val borderColor = if (accent == Color.Transparent) CyberLine else accent
    Box(modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = if (accent == Color.Transparent) 0.62f else 0.92f),
                    shape = shape
                ),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.98f))
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), content = content)
        }
        Box(
            Modifier
                .padding(start = 16.dp)
                .width(if (accent == Color.Transparent) 42.dp else 72.dp)
                .height(2.dp)
                .background(if (accent == Color.Transparent) CyberCyanDark else accent)
        )
    }
}

@Composable
fun CyberTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver", tint = CyberCyan)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = CyberText,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = CyberMuted,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun SystemBadge(text: String, color: Color = CyberCyan) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(7.dp)
                .height(7.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.9.sp
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = CyberCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.9.sp,
        modifier = modifier
    )
}

@Composable
fun CyberChoice(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color.Black
            ),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(text, fontWeight = FontWeight.Black, maxLines = 1, fontSize = 12.sp)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(text, maxLines = 1, fontSize = 12.sp)
        }
    }
}

@Composable
fun CyberStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier
            .background(CyberSurface2, CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp))
            .border(
                1.dp,
                if (highlight) CyberCyan else CyberLine,
                CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
            )
            .padding(horizontal = 7.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label.uppercase(), color = CyberMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color = if (highlight) CyberCyan else CyberText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FiveThreeOneSetStrip(
    sets: List<WorkoutSetState>,
    currentIndex: Int? = null,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        sets.forEachIndexed { index, set ->
            val active = currentIndex == index && !set.completed
            val done = set.completed
            val accent = when {
                done -> CyberSuccess
                active -> CyberCyan
                set.isAmrap -> CyberMagenta
                else -> CyberLine
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(CyberSurface2, CutCornerShape(topEnd = 10.dp, bottomStart = 10.dp))
                    .border(1.dp, accent, CutCornerShape(topEnd = 10.dp, bottomStart = 10.dp))
                    .padding(horizontal = 6.dp, vertical = if (compact) 8.dp else 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (done) "✓ SET ${index + 1}" else "SET ${index + 1}",
                    color = if (done) CyberSuccess else CyberMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
                set.percentage?.let {
                    Text("$it% TM", color = CyberMuted, fontSize = 8.sp)
                }
                Text(
                    set.weightLabel,
                    color = if (active) CyberCyan else if (done) CyberSuccess else CyberText,
                    fontSize = if (compact) 12.sp else 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    "× ${set.targetReps}",
                    color = if (set.isAmrap && !done) CyberMagenta else CyberText,
                    fontSize = if (compact) 11.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CyberProgress(progress: Float, label: String) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = CyberMuted, fontSize = 10.sp)
            Text(
                "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = CyberCyan,
            trackColor = CyberLine
        )
    }
}

@Composable
fun CyberNumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    integer: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = if (integer) {
                input.filter(Char::isDigit)
            } else {
                input.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
            }
            onValueChange(filtered)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal
        ),
        modifier = modifier
    )
}

fun modeLabel(mode: WorkoutMode): String =
    if (mode == WorkoutMode.STRENGTH) "FUERZA 5/3/1" else "HIPERTROFIA"

fun mainLiftLabel(mainLift: MainLift): String = when (mainLift) {
    MainLift.SQUAT -> "SQUAT"
    MainLift.BENCH -> "BENCH"
    MainLift.DEADLIFT -> "DEADLIFT"
}

fun cleanNumber(value: Double): String {
    if (!value.isFinite()) return "0"
    return if (abs(value - value.toInt()) < 0.0001) {
        value.toInt().toString()
    } else {
        "%.1f".format(Locale.US, value)
    }
}

fun formatSeconds(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

fun formatMillis(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    return "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}
