package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.nebulabook.cloudos.domain.model.CloudEngine
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.ui.theme.*

@Composable fun FloatingWorkspaceHud(telemetry: TelemetryState, engine: CloudEngine, onTrackpad: () -> Unit, onRibbon: () -> Unit, onClose: () -> Unit) {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); seconds++ } }
    val elapsed="%02d:%02d:%02d".format(seconds/3600,(seconds%3600)/60,seconds%60)
    GlassCard(Modifier.fillMaxWidth(), glow=NeonCyan.copy(alpha=.14f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Text("◉  ${engine.title.uppercase()} · ${engine.ram}", color=NeonCyan, style=MaterialTheme.typography.labelLarge)
            Text("${telemetry.pingMs} ms  ·  ${telemetry.cpuPercent}% CPU  ·  ${"%.1f".format(telemetry.ramUsedGb)} GB RAM  ·  $elapsed", color=TextPrimary, style=MaterialTheme.typography.labelSmall)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            TextButton(onClick=onTrackpad) { Text("TRACKPAD", color=NeonCyan) }
            TextButton(onClick=onRibbon) { Text("SHORTCUTS", color=NeonCyan) }
            TextButton(onClick=onClose) { Text("CLOSE LID", color=DangerCrimson) }
        }
    }
}
