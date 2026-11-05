package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.domain.model.KaggleRenderJob
import com.nebulabook.cloudos.ui.components.GlassCard
import com.nebulabook.cloudos.ui.theme.*

@Composable fun KaggleRenderFarmScreen(simulation:Boolean, job:KaggleRenderJob?, onDispatch:(String,Int,Int)->Unit) {
    var path by remember { mutableStateOf("/CloudDrive/Projects/cyberpunk_scene.blend") }
    var start by remember { mutableStateOf("1") }; var end by remember { mutableStateOf("250") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text("GPU RENDER FARM",style=MaterialTheme.typography.headlineMedium,color=KaggleOrange)
        GlassCard(glow=KaggleOrange.copy(alpha=.12f)) { Text("Batch rendering workflow",color=KaggleOrange,style=MaterialTheme.typography.titleMedium); Text("Prepare a Blender project, upload it to a Kaggle kernel or dataset, then run a batch job if GPU access is available to your account. This app does not guarantee free GPU time or dual-T4 access.",color=TextSecondary) }
        GlassCard { Text("GPU QUOTA · PROVIDER ACCOUNT",color=TextSecondary,style=MaterialTheme.typography.labelLarge); Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress={1f},modifier=Modifier.fillMaxWidth(),color=KaggleOrange); Text("Account limits vary · verify current Kaggle quota",color=KaggleOrange,style=MaterialTheme.typography.labelSmall) }
        GlassCard {
            Text("RENDER CONFIGURATION",color=TextPrimary,style=MaterialTheme.typography.titleMedium)
            OutlinedTextField(path,{path=it},label={Text(".blend project path")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Demo scene","Product render").forEach { AssistChip(onClick={path="/CloudDrive/Projects/${if(it.startsWith("Demo")) "demo_scene" else "product"}.blend"},label={Text(it)}) } }
            Text("Accelerator: account-dependent GPU (not guaranteed)",color=KaggleOrange,style=MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { OutlinedTextField(start,{start=it.filter(Char::isDigit)},label={Text("Start frame")},modifier=Modifier.weight(1f)); OutlinedTextField(end,{end=it.filter(Char::isDigit)},label={Text("End frame")},modifier=Modifier.weight(1f)) }
            Text("Output format: MP4 H.264 · Configure Blender kernel and inputs in Kaggle",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            Button(onClick={ onDispatch(path,start.toIntOrNull()?:1,end.toIntOrNull()?:250) },modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=KaggleOrange,contentColor=ObsidianBg)) { Text(if(simulation) "SIMULATE RENDER" else "CHECK LIVE CONFIGURATION") }
        }
        job?.let { GlassCard { Text("${it.status.uppercase()} · ${it.progress}%",color=KaggleOrange); LinearProgressIndicator(progress={it.progress/100f},modifier=Modifier.fillMaxWidth(),color=KaggleOrange); Text("${it.file} · frames ${it.startFrame}–${it.endFrame}",color=TextSecondary); if(it.progress==100) Text("Download is a demo state; no output file was produced.",color=TextSecondary,style=MaterialTheme.typography.labelSmall) } }
    }
}
