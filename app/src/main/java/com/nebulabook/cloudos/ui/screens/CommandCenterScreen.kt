package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.ui.components.GlassCard
import com.nebulabook.cloudos.ui.theme.*

@Composable fun CommandCenterScreen(connected:Boolean, booting:Boolean, host:String, cloudShellBusy:Boolean, cloudShellStatus:String, telemetry:TelemetryState, githubAccount:String, logs:List<String>, onLaunch:()->Unit, onCloudShell:()->Unit, onClose:()->Unit, onVault:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Column {
            Text("NEBULABOOK", style=MaterialTheme.typography.titleLarge, color=TextPrimary)
            Text("REMOTE DESKTOP · LIVE STATUS ONLY", style=MaterialTheme.typography.labelSmall, color=TextSecondary)
        }
        GlassCard(glow=if(connected) GoogleEmerald.copy(alpha=.18f) else NeonCyan.copy(alpha=.06f)) {
            Text("SSH SESSION",color=TextSecondary,style=MaterialTheme.typography.labelLarge)
            Text(if(connected) "CONNECTED · $host" else if(booting) "CONNECTING · $host" else "NOT CONNECTED",color=if(connected) GoogleEmerald else if(booting) NeonCyan else TextSecondary,style=MaterialTheme.typography.titleLarge)
            Text(if(githubAccount.isBlank()) "GitHub API: not verified" else "GitHub API: @$githubAccount",color=if(githubAccount.isBlank()) TextSecondary else GoogleEmerald)
            if(cloudShellStatus.isNotBlank()) Text("Cloud Shell · $cloudShellStatus",color=TextSecondary,style=MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            if(connected && telemetry.isConnected) {
                Text("Live SSH telemetry",color=NeonCyan,style=MaterialTheme.typography.labelLarge)
                Text("Ping ${telemetry.pingMs} ms   ·   CPU ${telemetry.cpuPercent}%   ·   RAM ${"%.1f".format(telemetry.ramUsedGb)} / ${"%.1f".format(telemetry.ramTotalGb)} GB",color=TextPrimary,style=MaterialTheme.typography.labelSmall)
            } else if(connected) {
                Text("SSH connected · waiting for the first telemetry sample",color=TextSecondary)
            } else {
                Text("No quota, CPU, RAM, ping, or session figures are estimated or simulated. Connect to a configured SSH host to display live measurements.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            }
        }
        if(logs.isNotEmpty()) GlassCard {
            Text("LATEST SSH OUTPUT",color=NeonCyan,style=MaterialTheme.typography.labelLarge)
            logs.takeLast(8).forEach { Text(it,color=TextSecondary,style=MaterialTheme.typography.labelSmall,fontFamily=androidx.compose.ui.text.font.FontFamily.Monospace) }
        }
        if(connected) {
            Button(onClick=onClose,modifier=Modifier.fillMaxWidth().height(56.dp),colors=ButtonDefaults.buttonColors(containerColor=DangerCrimson,contentColor=TextPrimary)) { Text("CLOSE LID · STOP REMOTE DESKTOP") }
        } else {
            Button(onClick=onLaunch,enabled=!booting,modifier=Modifier.fillMaxWidth().height(56.dp),colors=ButtonDefaults.buttonColors(containerColor=NeonCyan,contentColor=ObsidianBg)) { Text(if(booting) "CONNECTING TO SSH…" else "CONNECT TO SSH DESKTOP") }
        }
        if(!connected) OutlinedButton(onClick=onCloudShell,enabled=!cloudShellBusy,modifier=Modifier.fillMaxWidth()) { Text(if(cloudShellBusy) "STARTING GOOGLE CLOUD SHELL…" else "START / CONNECT GOOGLE CLOUD SHELL",color=GoogleEmerald) }
        OutlinedButton(onClick=onVault,modifier=Modifier.fillMaxWidth()) { Text("Configure SSH and provider credentials",color=TextSecondary) }
        Text("Provider quotas and billing are not inferred. Use your provider account for current limits and charges.",color=TextSecondary,style=MaterialTheme.typography.labelSmall)
    }
}
