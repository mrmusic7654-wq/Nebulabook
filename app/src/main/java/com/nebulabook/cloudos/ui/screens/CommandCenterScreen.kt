package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.CloudEngine
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.ui.components.*
import com.nebulabook.cloudos.ui.theme.*

@Composable fun CommandCenterScreen(simulation:Boolean, engine:CloudEngine, telemetry:TelemetryState, onToggleMode:()->Unit, onEngine:(CloudEngine)->Unit, onLaunch:()->Unit, onVault:()->Unit) {
    var githubTurbo by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Column { Text("NEBULABOOK OS", style=MaterialTheme.typography.titleLarge, color=TextPrimary); Text("CLOUD LAPTOP CONTROL PLANE", style=MaterialTheme.typography.labelSmall, color=TextSecondary) }
            TextButton(onClick=onToggleMode) { Text(if(simulation) "● SIMULATION" else "● LIVE MODE", color=if(simulation) NeonCyan else GoogleEmerald) }
        }
        GlassCard(glow=NeonCyan.copy(alpha=.08f)) { Text("UNIFIED QUOTA OVERVIEW", color=TextSecondary, style=MaterialTheme.typography.labelLarge); QuotaArcGauge(); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("Google · 50 hrs/week", color=GoogleEmerald); Text("Codespaces · account dependent", color=GitHubIndigo) } }
        Text("SELECT COMPUTE ENGINE", color=TextSecondary, style=MaterialTheme.typography.labelLarge)
        listOf(CloudEngine.GOOGLE,CloudEngine.GITHUB).forEach { item ->
            GlassCard(glow=if(engine==item) (if(item==CloudEngine.GOOGLE) GoogleEmerald else GitHubIndigo).copy(alpha=.25f) else androidx.compose.ui.graphics.Color.Transparent) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text("${if(item==CloudEngine.GOOGLE) "NODE 01" else "NODE 02"}  ·  ${item.title}", color=if(item==CloudEngine.GOOGLE) GoogleEmerald else GitHubIndigo, style=MaterialTheme.typography.titleMedium); Text(if(item==CloudEngine.GOOGLE) "Linux shell · persistent home · quota and access depend on your Google account" else "2–4 cores · machine size and included hours depend on your GitHub plan", color=TextSecondary, style=MaterialTheme.typography.bodySmall) }
                    RadioButton(selected=engine==item,onClick={onEngine(item)})
                }
                if(item==CloudEngine.GITHUB) {
                    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected=!githubTurbo,onClick={githubTurbo=false},label={Text("2 CORE · 8 GB")})
                        FilterChip(selected=githubTurbo,onClick={githubTurbo=true},label={Text("4 CORE · 16 GB")})
                    }
                    Text(if(githubTurbo) "Illustrative estimate: a larger machine may consume included core-hours faster." else "Illustrative estimate: smaller machine may run longer per included core-hour." , color=TextSecondary, style=MaterialTheme.typography.labelSmall)
                }
            }
        }
        GlassCard { Text("HARDWARE TELEMETRY", color=TextSecondary, style=MaterialTheme.typography.labelLarge); Spacer(Modifier.height(8.dp)); Text(if(telemetry.isConnected) "RUNNING · DEMO DESKTOP" else "STANDBY", color=if(telemetry.isConnected) GoogleEmerald else TextSecondary); Text("Ping ${telemetry.pingMs} ms     CPU ${telemetry.cpuPercent}%     RAM ${telemetry.ramUsedGb}/${telemetry.ramTotalGb} GB", color=NeonCyan, style=MaterialTheme.typography.labelSmall) }
        Button(onClick=onLaunch, modifier=Modifier.fillMaxWidth().height(58.dp), colors=ButtonDefaults.buttonColors(containerColor=NeonCyan,contentColor=ObsidianBg)) { Text("OPEN LAPTOP LID  ·  LAUNCH OS", style=MaterialTheme.typography.titleMedium) }
        TextButton(onClick=onVault, modifier=Modifier.fillMaxWidth()) { Text("Configure credentials and keys →",color=TextSecondary) }
        Text("Demo quotas and telemetry are illustrative. Live provider limits and billing are controlled by the provider.", color=TextSecondary, style=MaterialTheme.typography.labelSmall)
    }
}
