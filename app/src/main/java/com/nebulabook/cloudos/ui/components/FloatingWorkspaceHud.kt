package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.ui.theme.*
import kotlinx.coroutines.delay

@Composable fun FloatingWorkspaceHud(telemetry: TelemetryState, host: String, sessionStartedAt: Long, onTrackpad: () -> Unit, onRibbon: () -> Unit, onClose: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(sessionStartedAt) { while (sessionStartedAt > 0L) { now=System.currentTimeMillis(); delay(1_000) } }
    val seconds=((now-sessionStartedAt).coerceAtLeast(0L)/1_000L).toInt()
    val elapsed="%02d:%02d:%02d".format(seconds/3600,(seconds%3600)/60,seconds%60)
    GlassCard(Modifier.fillMaxWidth(), glow=NeonCyan.copy(alpha=.14f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Text("SSH · $host", color=NeonCyan, style=MaterialTheme.typography.labelLarge)
            Text(if(telemetry.isConnected) "${telemetry.pingMs} ms · CPU ${telemetry.cpuPercent}% · RAM ${"%.1f".format(telemetry.ramUsedGb)}/${"%.1f".format(telemetry.ramTotalGb)} GB · $elapsed" else "SSH telemetry pending · $elapsed", color=TextPrimary, style=MaterialTheme.typography.labelSmall)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            TextButton(onClick=onTrackpad) { Text("TRACKPAD", color=NeonCyan) }
            TextButton(onClick=onRibbon) { Text("SHORTCUTS", color=NeonCyan) }
            TextButton(onClick=onClose) { Text("CLOSE LID", color=DangerCrimson) }
        }
    }
}
