package dev.hyprdroid.launcher.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentColors = listOf(
    Color(0xFFA6F750),
    Color(0xFF7CE7FF),
    Color(0xFFFF7B9C),
    Color(0xFFB69CFF),
    Color(0xFFFFC46B),
    Color(0xFF72E2B8)
)

val Background = Color(0xFF080C16)
val Panel = Color(0xFF10192A)
val PanelRaised = Color(0xFF17243A)
val Muted = Color(0xFFB1BDCE)

@Composable
fun HyprDroidTheme(accentIndex: Int, content: @Composable () -> Unit) {
    val accent = AccentColors[accentIndex.coerceIn(0, AccentColors.lastIndex)]
    val colors = darkColorScheme(
        primary = accent,
        onPrimary = Color(0xFF10140B),
        secondary = Color(0xFFB3C6A0),
        background = Background,
        surface = Panel,
        surfaceVariant = PanelRaised,
        onBackground = Color(0xFFF1F3F6),
        onSurface = Color(0xFFF1F3F6),
        onSurfaceVariant = Muted,
        outline = Color(0xFF333B47)
    )
    MaterialTheme(colorScheme = colors, content = content)
}
