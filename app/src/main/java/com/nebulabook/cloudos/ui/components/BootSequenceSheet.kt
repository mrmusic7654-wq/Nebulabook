package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun BootSequenceSheet(logs: List<String>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ObsidianBg) {
        Column(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(bottom=32.dp)) {
            Text("BOOT SEQUENCE", style=MaterialTheme.typography.titleLarge, color=NeonCyan)
            Spacer(Modifier.height(12.dp))
            logs.forEach { Text("› $it", color=TextPrimary, style=MaterialTheme.typography.bodyMedium, fontFamily=androidx.compose.ui.text.font.FontFamily.Monospace) }
        }
    }
}
