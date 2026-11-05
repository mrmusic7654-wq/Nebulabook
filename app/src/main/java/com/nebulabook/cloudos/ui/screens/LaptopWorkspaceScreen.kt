package com.nebulabook.cloudos.ui.screens

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nebulabook.cloudos.data.ssh.KasmInputBridge
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.ui.components.BlenderMacroRibbon
import com.nebulabook.cloudos.ui.components.FloatingWorkspaceHud
import com.nebulabook.cloudos.ui.components.VirtualTrackpadOverlay
import com.nebulabook.cloudos.ui.theme.*

@Composable fun LaptopWorkspaceScreen(
    telemetry: TelemetryState,
    host: String,
    sessionStartedAt: Long,
    trackpad: Boolean,
    ribbon: Boolean,
    onTrackpad: () -> Unit,
    onRibbon: () -> Unit,
    onClose: () -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var cursorX by remember { mutableFloatStateOf(160f) }
    var cursorY by remember { mutableFloatStateOf(160f) }
    Box(Modifier.fillMaxSize().background(ObsidianBg)) {
        AndroidView(factory={ context -> WebView(context).apply {
            webView=this
            settings.javaScriptEnabled=true
            settings.domStorageEnabled=true
            settings.userAgentString="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120 Safari/537.36"
            webViewClient=WebViewClient()
            webChromeClient=WebChromeClient()
            loadUrl("http://127.0.0.1:3000")
        } },modifier=Modifier.fillMaxSize())
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            FloatingWorkspaceHud(telemetry,host,sessionStartedAt,onTrackpad,onRibbon,onClose)
            if(ribbon) BlenderMacroRibbon { shortcut ->
                val (key,code,keyCode)=when(shortcut) {
                    "CAM 0" -> Triple("0","Numpad0",96)
                    "FRONT 1" -> Triple("1","Numpad1",97)
                    "RIGHT 3" -> Triple("3","Numpad3",99)
                    "TOP 7" -> Triple("7","Numpad7",103)
                    "WIRE Z" -> Triple("z","KeyZ",90)
                    "F12" -> Triple("F12","F12",123)
                    else -> Triple(shortcut, "Key$shortcut", shortcut.firstOrNull()?.code ?: 0)
                }
                webView?.let { KasmInputBridge.sendKeyEvent(it,key,code,keyCode) }
            }
        }
        if(trackpad) VirtualTrackpadOverlay(
            onMove={dx,dy->cursorX+=dx;cursorY+=dy;webView?.let { KasmInputBridge.sendMouseEvent(it,"mousemove",cursorX,cursorY,0) }},
            onClick={button->webView?.let { view ->
                KasmInputBridge.sendMouseEvent(view,"mousedown",cursorX,cursorY,button)
                KasmInputBridge.sendMouseEvent(view,"mouseup",cursorX,cursorY,button)
            }},
            modifier=Modifier.align(Alignment.BottomEnd)
        )
    }
}
