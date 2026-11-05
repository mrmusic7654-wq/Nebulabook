package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.desktopApps
import com.nebulabook.cloudos.ui.components.GlassCard
import com.nebulabook.cloudos.ui.theme.*

@Composable fun AppDeckScreen(simulation:Boolean, installed:Set<String>, filter:String, onFilter:(String)->Unit, onInstall:(String)->Unit) {
    val filters=listOf("All","3D & Creative","AI & Dev","Browsers","Windows (.exe)")
    Column(Modifier.fillMaxSize().padding(horizontal=14.dp)) {
        Text("APP DECK", style=MaterialTheme.typography.headlineMedium, color=TextPrimary, modifier=Modifier.padding(vertical=14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(5.dp)) { filters.forEach { f -> FilterChip(selected=filter==f,onClick={onFilter(f)},label={Text(f)}, colors=FilterChipDefaults.filterChipColors(selectedContainerColor=NeonCyan.copy(alpha=.2f),selectedLabelColor=NeonCyan)) } }
        LazyVerticalGrid(columns=GridCells.Adaptive(160.dp),contentPadding=PaddingValues(bottom=20.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            items(desktopApps.filter { filter=="All" || it.category==filter }, key={it.name}) { app ->
                var expanded by remember { mutableStateOf(false) }
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(app.icon, color=NeonCyan, style=MaterialTheme.typography.headlineMedium)
                    Text(app.name,color=TextPrimary,style=MaterialTheme.typography.titleMedium)
                    Text("${app.category} · RAM ${app.ram}",color=TextSecondary,style=MaterialTheme.typography.labelSmall)
                    TextButton(onClick={expanded=!expanded},contentPadding=PaddingValues(0.dp)) { Text(if(expanded) "Hide command" else "Preview command",color=NeonCyan) }
                    if(expanded) Text(app.command,color=TextSecondary,style=MaterialTheme.typography.labelSmall)
                    Button(onClick={onInstall(app.name)},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=if(app.name in installed) GoogleEmerald.copy(alpha=.18f) else NeonCyan.copy(alpha=.16f),contentColor=if(app.name in installed) GoogleEmerald else NeonCyan)) { Text(if(app.name in installed) "INSTALLED ✓" else "INSTALL") }
                }
            }
        }
        if(!simulation) Text("Live installs require an explicitly configured SSH destination. Nothing is sent from this screen.",color=TextSecondary,style=MaterialTheme.typography.labelSmall)
    }
}
