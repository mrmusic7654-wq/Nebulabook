package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nebulabook.cloudos.ui.theme.*

@Composable fun QuotaArcGauge(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(14.dp)) {
            val sw = 13.dp.toPx(); val top = 180f
            drawArc(GoogleEmerald.copy(alpha=.18f), 180f, top, false, style=Stroke(sw, cap=StrokeCap.Round))
            drawArc(GoogleEmerald, 180f, 114f, false, style=Stroke(sw, cap=StrokeCap.Round))
            drawArc(GitHubIndigo.copy(alpha=.18f), 180f, top, false, style=Stroke(sw, cap=StrokeCap.Round))
            drawArc(GitHubIndigo, 300f, 57f, false, style=Stroke(sw, cap=StrokeCap.Round))
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text("275 HRS", color=TextPrimary, fontSize=32.sp)
            Text("AVAILABLE · SIMULATED QUOTA", color=NeonCyan, fontSize=10.sp, letterSpacing=1.5.sp)
            Spacer(Modifier.height(8.dp)); Text("⚡ Safe daily budget 9.1 hrs", color=TextSecondary, fontSize=12.sp)
        }
    }
}
