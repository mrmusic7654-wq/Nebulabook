package com.nebulabook.cloudos.data.ssh

import android.webkit.WebView

object KasmInputBridge {
    fun sendKeyEvent(webView: WebView, key: String, code: String, keyCode: Int, ctrl: Boolean=false, shift: Boolean=false, alt: Boolean=false) {
        val k=key.replace("'","\\'"); val c=code.replace("'","\\'")
        webView.evaluateJavascript("(()=>{const t=document.querySelector('canvas')||document.body;const o={key:'$k',code:'$c',keyCode:$keyCode,which:$keyCode,ctrlKey:$ctrl,shiftKey:$shift,altKey:$alt,bubbles:true};t.dispatchEvent(new KeyboardEvent('keydown',o));t.dispatchEvent(new KeyboardEvent('keyup',o));})()", null)
    }
    fun sendMouseEvent(webView: WebView, type: String, x: Float, y: Float, button: Int) {
        val safeType=type.filter { it.isLetter() }
        webView.evaluateJavascript("(()=>{const t=document.elementFromPoint($x,$y)||document.body;t.dispatchEvent(new MouseEvent('$safeType',{clientX:$x,clientY:$y,button:$button,bubbles:true}));})()", null)
    }
}
