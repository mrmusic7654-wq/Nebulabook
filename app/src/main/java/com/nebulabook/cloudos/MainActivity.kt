package com.nebulabook.cloudos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nebulabook.cloudos.ui.navigation.NebulaNavGraph
import com.nebulabook.cloudos.ui.theme.NebulaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NebulaTheme { NebulaNavGraph() } } }
}
