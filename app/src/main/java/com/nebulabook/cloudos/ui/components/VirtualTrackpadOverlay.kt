package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.ui.theme.*

@Composable fun VirtualTrackpadOverlay(onMove: (Float,Float)->Unit, onClick: (Int)->Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(12.dp), horizontalAlignment=Alignment.End) {
        Box(Modifier.size(190.dp,100.dp).background(MidnightSurface.copy(alpha=.9f), RoundedCornerShape(16.dp))
            .pointerInput(Unit) { detectDragGestures { change, delta -> change.consume(); onMove(delta.x,delta.y) } }, contentAlignment=Alignment.Center) {
            Text("TRACKPAD · DRAG", color=NeonCyan)
        }
        Row { listOf("LEFT" to 0,"ORBIT" to 1,"RIGHT" to 2).forEach { (label,button) -> androidx.compose.material3.TextButton(onClick={onClick(button)}) { Text(label,color=TextPrimary) } } }
    }
}
