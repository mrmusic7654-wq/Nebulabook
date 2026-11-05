package com.nebulabook.cloudos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NebulaTypography = Typography().copy(
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    bodyMedium = Typography().bodyMedium.copy(lineHeight = 21.sp),
    labelSmall = Typography().labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = .5.sp)
)
