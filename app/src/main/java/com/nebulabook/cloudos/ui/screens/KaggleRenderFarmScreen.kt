package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.ui.components.GlassCard
import com.nebulabook.cloudos.ui.theme.*

@Composable fun KaggleRenderFarmScreen(remoteStatus:String, onCheckStatus:(String)->Unit) {
    var slug by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text("KAGGLE JOB STATUS",style=MaterialTheme.typography.headlineMedium,color=KaggleOrange)
        GlassCard(glow=KaggleOrange.copy(alpha=.1f)) {
            Text("LIVE PROVIDER DATA ONLY",color=KaggleOrange,style=MaterialTheme.typography.titleMedium)
            Text("No GPU hours, accelerator type, render progress, or completion state is estimated. Kaggle account quotas and available accelerators are controlled by Kaggle.",color=TextSecondary)
        }
        GlassCard {
            Text("CHECK AN EXISTING KERNEL",color=TextPrimary,style=MaterialTheme.typography.titleMedium)
            OutlinedTextField(slug,{slug=it},label={Text("Kernel slug")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Button(onClick={onCheckStatus(slug)},enabled=slug.isNotBlank(),modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=KaggleOrange,contentColor=ObsidianBg)) { Text("FETCH STATUS FROM KAGGLE API") }
            if(remoteStatus.isNotBlank()) Text(remoteStatus,color=TextPrimary,style=MaterialTheme.typography.labelSmall,fontFamily=androidx.compose.ui.text.font.FontFamily.Monospace)
        }
        GlassCard {
            Text("RENDER SUBMISSION",color=TextSecondary,style=MaterialTheme.typography.titleMedium)
            Text("New render submission is disabled until the app has a verified project-upload/kernel-push workflow. This screen will not show a fake queue or claim a GPU job was started.",color=TextSecondary)
        }
    }
}
