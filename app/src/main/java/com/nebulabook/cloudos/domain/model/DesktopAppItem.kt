package com.nebulabook.cloudos.domain.model

data class DesktopAppItem(val name: String, val category: String, val icon: String)
val desktopApps = listOf(
    DesktopAppItem("Blender", "3D & Creative", "◈"),
    DesktopAppItem("VS Code", "AI & Dev", "〈〉"),
    DesktopAppItem("Codex + Claude CLI", "AI & Dev", "✳"),
    DesktopAppItem("Brave Browser", "Browsers", "◉"),
    DesktopAppItem("Wine + Q4Wine", "Windows (.exe)", "▣"),
    DesktopAppItem("GIMP Studio", "3D & Creative", "✿"),
    DesktopAppItem("LibreOffice", "AI & Dev", "▤")
)
