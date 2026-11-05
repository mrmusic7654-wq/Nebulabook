package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.CodespaceInfo
import com.nebulabook.cloudos.domain.model.CodespaceMachine
import com.nebulabook.cloudos.ui.components.GlassCard
import com.nebulabook.cloudos.ui.theme.*

@Composable fun CodespacesScreen(
    account:String,
    codespaces:List<CodespaceInfo>,
    selectedName:String,
    machines:List<CodespaceMachine>,
    busy:Boolean,
    status:String,
    onRefresh:()->Unit,
    onSelect:(String)->Unit,
    onStart:(String)->Unit,
    onStop:(String)->Unit,
    onOpen:(String)->Unit,
    onSettings:()->Unit
) {
    val selected=codespaces.firstOrNull { it.name==selectedName }
    val twoCore=machines.filter { it.cpus==2 && it.memoryBytes>0 && it.storageBytes>0 }.minByOrNull { it.memoryBytes }
    Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Column { Text("CODESPACES",style=MaterialTheme.typography.headlineMedium,color=TextPrimary); Text("LIVE GITHUB API",style=MaterialTheme.typography.labelSmall,color=TextSecondary) }
            TextButton(onClick=onRefresh,enabled=!busy) { Text(if(busy) "WAIT…" else "REFRESH",color=NeonCyan) }
        }
        GlassCard(glow=if(account.isNotBlank()) GitHubIndigo.copy(alpha=.16f) else androidx.compose.ui.graphics.Color.Transparent) {
            Text(if(account.isBlank()) "GitHub not connected" else "Connected as @$account",color=if(account.isBlank()) TextSecondary else GitHubIndigo,style=MaterialTheme.typography.titleMedium)
            Text("GitHub’s API reports Codespace state and available machine sizes. RAM and machine storage below come from that response; check GitHub Billing for your actual allowance and charges. Codespaces are not guaranteed free.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            if(account.isBlank()) TextButton(onClick=onSettings) { Text("Add a token in Settings →",color=NeonCyan) }
        }
        if(status.isNotBlank()) Text(status,color=TextSecondary,style=MaterialTheme.typography.labelSmall)
        if(selected!=null) {
            GlassCard(glow=NeonCyan.copy(alpha=.12f)) {
                Text("SELECTED WORKSPACE",color=NeonCyan,style=MaterialTheme.typography.labelLarge)
                Text(selected.name,color=TextPrimary,style=MaterialTheme.typography.titleMedium)
                Text("State: ${selected.state}",color=if(selected.state.equals("Available",true)) GoogleEmerald else TextSecondary)
                if(selected.repository.isNotBlank()) Text(selected.repository,color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                if(selected.cpus>0) Text("Current machine: ${selected.cpus} vCPU${if(selected.cpus==1) "" else "s"}${if(selected.memoryBytes>0) " · ${"%.1f".format(selected.memoryBytes/1_073_741_824.0)} GiB RAM" else ""}${if(selected.storageBytes>0) " · ${"%.1f".format(selected.storageBytes/1_073_741_824.0)} GiB machine storage" else ""}",color=TextPrimary)
                if(twoCore!=null) Text("Available 2-core machine: ${twoCore.displayName} · ${twoCore.memoryGiB()} GiB RAM · ${twoCore.storageGiB()} GiB machine storage",color=NeonCyan,style=MaterialTheme.typography.bodySmall)
                else if(machines.isNotEmpty()) Text("GitHub reports no available 2-core machine for this Codespace.",color=KaggleOrange,style=MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Button(onClick={onStart(selected.name)},enabled=!busy && twoCore!=null && (selected.state.contains("shutdown",true) || selected.state.equals("stopped",true) || (selected.state.equals("Available",true) && selected.machineName==twoCore.name)),colors=ButtonDefaults.buttonColors(containerColor=NeonCyan,contentColor=ObsidianBg)) { Text("START · 2 CORE") }
                    OutlinedButton(onClick={onStop(selected.name)},enabled=!busy && selected.state.equals("Available",true)) { Text("STOP",color=DangerCrimson) }
                }
                if(selected.webUrl.startsWith("https://")) OutlinedButton(onClick={onOpen(selected.webUrl)},modifier=Modifier.fillMaxWidth()) { Text("OPEN CODESPACE IN BROWSER",color=TextPrimary) }
                Text("Opening the Codespace editor is separate from connecting a desktop stream. Desktop access requires a configured forwarded desktop port and GitHub’s supported port authentication.",color=TextSecondary,style=MaterialTheme.typography.labelSmall)
            }
        }
        Text("YOUR CODESPACES · API RESULTS",color=TextSecondary,style=MaterialTheme.typography.labelLarge)
        if(codespaces.isEmpty()) {
            GlassCard(Modifier.fillMaxWidth()) { Text("No Codespaces returned. Verify a token with Codespaces read/write access, then refresh.",color=TextSecondary) }
        } else {
            LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=18.dp)) {
                items(codespaces,key={it.name}) { code ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(code.name,color=TextPrimary,style=MaterialTheme.typography.titleMedium)
                        Text("${code.state}${if(code.repository.isNotBlank()) " · ${code.repository}" else ""}",color=if(code.state.equals("Available",true)) GoogleEmerald else TextSecondary,style=MaterialTheme.typography.bodySmall)
                        TextButton(onClick={onSelect(code.name)}) { Text(if(code.name==selectedName) "SELECTED · LOAD MACHINE OPTIONS" else "SELECT WORKSPACE",color=NeonCyan) }
                    }
                }
            }
        }
    }
}
