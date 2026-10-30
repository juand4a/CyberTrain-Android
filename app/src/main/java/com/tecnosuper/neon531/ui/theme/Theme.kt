package com.tecnosuper.neon531.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CyberBackground = Color(0xFF04070A)
val CyberBackground2 = Color(0xFF071017)
val CyberSurface = Color(0xFF0A1117)
val CyberSurface2 = Color(0xFF0E1921)
val CyberSurface3 = Color(0xFF14232D)
val CyberCyan = Color(0xFF00E7F0)
val CyberCyanSoft = Color(0xFF8AF8FB)
val CyberCyanDark = Color(0xFF006A72)
val CyberMagenta = Color(0xFFFF3D86)
val CyberText = Color(0xFFF4F7F8)
val CyberMuted = Color(0xFF8797A0)
val CyberLine = Color(0xFF20343E)
val CyberSuccess = Color(0xFF73F3C0)

private val NeonColors = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001416),
    secondary = CyberMagenta,
    onSecondary = Color.Black,
    background = CyberBackground,
    onBackground = CyberText,
    surface = CyberSurface,
    onSurface = CyberText,
    surfaceVariant = CyberSurface2,
    onSurfaceVariant = CyberMuted,
    outline = CyberLine,
    error = CyberMagenta
)

private val NeonTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.6.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.3.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 19.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)

private val NeonShapes = Shapes(
    small = CutCornerShape(topEnd = 7.dp, bottomStart = 7.dp),
    medium = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp),
    large = CutCornerShape(topEnd = 22.dp, bottomStart = 22.dp)
)

@Composable
fun Neon531Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeonColors,
        typography = NeonTypography,
        shapes = NeonShapes,
        content = content
    )
}
