package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.ui.theme.*

@Composable fun BlenderMacroRibbon(onKey: (String) -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), glow=HyperViolet.copy(alpha=.15f)) {
        Text("BLENDER QUICK KEYS", color=HyperViolet, style=MaterialTheme.typography.labelLarge)
        listOf(listOf("CTRL", "SHIFT", "ALT", "TAB", "ESC"), listOf("G", "R", "S", "X", "Y", "Z"), listOf("CAM 0", "FRONT 1", "RIGHT 3", "TOP 7", "WIRE Z", "F12")).forEach { row ->
            Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) { row.forEach { key -> TextButton(onClick={onKey(key)}) { Text(key, color=TextPrimary) } } }
        }
    }
}
