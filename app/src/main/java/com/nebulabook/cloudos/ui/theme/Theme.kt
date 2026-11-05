package com.nebulabook.cloudos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NebulaColors = darkColorScheme(
    primary = NeonCyan, secondary = HyperViolet, tertiary = GoogleEmerald,
    background = ObsidianBg, surface = MidnightSurface,
    onPrimary = ObsidianBg, onBackground = TextPrimary, onSurface = TextPrimary
)

@Composable fun NebulaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NebulaColors, typography = NebulaTypography, content = content)
}
