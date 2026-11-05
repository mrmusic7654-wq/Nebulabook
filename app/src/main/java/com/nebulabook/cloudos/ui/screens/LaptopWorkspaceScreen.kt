package com.nebulabook.cloudos.ui.screens

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nebulabook.cloudos.data.ssh.KasmInputBridge
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.domain.model.CloudEngine
import com.nebulabook.cloudos.ui.components.*
import com.nebulabook.cloudos.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable fun LaptopWorkspaceScreen(simulation:Boolean, telemetry:TelemetryState, engine:CloudEngine, trackpad:Boolean, ribbon:Boolean, window:String, onTrackpad:()->Unit, onRibbon:()->Unit, onClose:()->Unit, onWindow:(String)->Unit, onMessage:(String)->Unit) {
    var rx by remember { mutableFloatStateOf(.45f) }; var ry by remember { mutableFloatStateOf(.55f) }; var scale by remember { mutableFloatStateOf(1f) }
    var liveWebView by remember { mutableStateOf<WebView?>(null) }
    var cursorX by remember { mutableFloatStateOf(160f) }; var cursorY by remember { mutableFloatStateOf(160f) }
    Box(Modifier.fillMaxSize().background(Color(0xFF151923))) {
        if (!simulation) {
            AndroidView(factory={ context -> WebView(context).apply { liveWebView=this; settings.javaScriptEnabled=true; settings.domStorageEnabled=true; settings.userAgentString="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120 Safari/537.36"; webViewClient=WebViewClient(); webChromeClient=WebChromeClient(); loadUrl("http://127.0.0.1:3000") } }, modifier=Modifier.fillMaxSize())
        } else {
            Column(Modifier.fillMaxSize().background(Color(0xFF1B202A))) {
                Row(Modifier.fillMaxWidth().background(Color(0xFF252B38)).padding(horizontal=10.dp,vertical=6.dp), horizontalArrangement=Arrangement.spacedBy(10.dp), verticalAlignment=Alignment.CenterVertically) {
                    Text("◉ Nebula XFCE",color=NeonCyan); listOf("Blender 4.2","VS Code","Brave").forEach { label -> TextButton(onClick={onWindow(label)}) { Text(label,color=if(window==label) TextPrimary else TextSecondary) } }
                }
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Column(Modifier.width(46.dp).fillMaxHeight().background(Color(0xFF11151D)),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) { listOf("▦","⌂","◉","⚙").forEach { TextButton(onClick={onMessage("Desktop launcher: $it")}) { Text(it,color=NeonCyan) } } }
                    Column(Modifier.weight(1f).fillMaxHeight().padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth().background(Color(0xFF242A35),RoundedCornerShape(topStart=8.dp,topEnd=8.dp)).padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween) { Text("${window.uppercase()}  ·  ${if(window.startsWith("Blender")) "LAYOUT" else "WORKSPACE"}",color=TextPrimary); Text("−   □   ×",color=TextSecondary) }
                        if(window.startsWith("Blender")) {
                            Canvas(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF181D25)).pointerInput(Unit) { detectDragGestures { change,delta -> change.consume(); ry+=delta.x*.01f; rx+=delta.y*.01f } }) {
                                val cx=size.width*.52f; val cy=size.height*.48f; val unit=minOf(size.width,size.height)*.19f*scale
                                for(i in -10..10) { val x=cx+i*unit*.28f; val y=cy+i*unit*.28f; drawLine(Color(0xFF35414D),Offset(x,0f),Offset(x,size.height),1f); drawLine(Color(0xFF35414D),Offset(0f,y),Offset(size.width,y),1f) }
                                val verts=listOf(-1f to -1f to -1f,-1f to -1f to 1f,-1f to 1f to -1f,-1f to 1f to 1f,1f to -1f to -1f,1f to -1f to 1f,1f to 1f to -1f,1f to 1f to 1f)
                                val pts=verts.map { v -> val x=v.first.first; val y=v.first.second; val z=v.second; val x1=x*cos(ry)-z*sin(ry); val z1=x*sin(ry)+z*cos(ry); val y1=y*cos(rx)-z1*sin(rx); Offset(cx+x1*unit,cy+y1*unit) }
                                val edges=listOf(0 to 1,0 to 2,0 to 4,1 to 3,1 to 5,2 to 3,2 to 6,3 to 7,4 to 5,4 to 6,5 to 7,6 to 7)
                                edges.forEach { drawLine(NeonCyan,pts[it.first],pts[it.second],2.5f) }
                                drawCircle(HyperViolet,5f,Offset(cx,cy))
                            }
                            Text("Drag to orbit  ·  G move  ·  R rotate  ·  S scale",color=TextSecondary,style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(8.dp))
                        } else {
                            Column(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111720)).padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
                                Text(if(window=="VS Code") "EXPLORER     SEARCH     SOURCE CONTROL" else "NebulaBook demo browser",color=NeonCyan)
                                Spacer(Modifier.height(16.dp)); Text(if(window=="VS Code") "Welcome to your simulated editor" else "A simulated Linux desktop window",color=TextPrimary)
                                Text("Interactive preview · no remote desktop connection",color=TextSecondary)
                            }
                        }
                    }
                    Column(Modifier.width(48.dp).fillMaxHeight().background(Color(0xFF11151D)),horizontalAlignment=Alignment.CenterHorizontally) { Text("N\nT\nO\nR\n",color=TextSecondary) }
                }
                Row(Modifier.fillMaxWidth().background(Color(0xFF222833)).padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween) { Text("Applications",color=TextPrimary); Text("NebulaBook · Demo desktop",color=TextSecondary); Text("▴  Wi-Fi  🔋",color=NeonCyan) }
            }
        }
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            FloatingWorkspaceHud(telemetry,engine,onTrackpad,onRibbon,onClose)
            if(ribbon) BlenderMacroRibbon { key ->
                if (!simulation) liveWebView?.let { web -> KasmInputBridge.sendKeyEvent(web,key,key,key.firstOrNull()?.code ?: 0) }
                when(key) { "G"->onMessage("Grab / move tool"); "R"->onMessage("Rotate tool"); "S"->{scale=if(scale>1.2f).8f else 1.3f;onMessage("Scale")}; "FRONT 1"->{rx=0f;ry=0f}; "RIGHT 3"->{rx=0f;ry=1.57f}; "TOP 7"->{rx=1.57f;ry=0f}; "F12"->onMessage("Render shortcut simulated"); else->onMessage("$key shortcut") }
            }
        }
        if(trackpad) VirtualTrackpadOverlay(onMove={dx,dy->
            if(simulation) { ry+=dx*.008f; rx+=dy*.008f } else { cursorX+=dx; cursorY+=dy; liveWebView?.let { KasmInputBridge.sendMouseEvent(it,"mousemove",cursorX,cursorY,0) } }
        },onClick={button->
            if(!simulation) liveWebView?.let { KasmInputBridge.sendMouseEvent(it,"mousedown",cursorX,cursorY,button); KasmInputBridge.sendMouseEvent(it,"mouseup",cursorX,cursorY,button) }
            onMessage(listOf("Left click","Middle orbit","Right click").getOrElse(button){"Click"})
        }, modifier=Modifier.align(Alignment.BottomEnd))
    }
}
