package com.nebulabook.cloudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.nebulabook.cloudos.ui.theme.*

@Composable fun GlassCard(modifier: Modifier = Modifier, glow: Color = Color.Transparent, shape: Shape = RoundedCornerShape(18.dp), content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.shadow(if (glow == Color.Transparent) 0.dp else 16.dp, shape, ambientColor = glow, spotColor = glow)
        .background(MidnightSurface, shape).border(1.dp, SurfaceBorderGradient, shape).padding(16.dp), content = content)
}
