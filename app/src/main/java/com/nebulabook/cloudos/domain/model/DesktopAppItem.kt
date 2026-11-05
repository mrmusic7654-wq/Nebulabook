package com.nebulabook.cloudos.domain.model

data class DesktopAppItem(val name: String, val category: String, val icon: String, val ram: String, val command: String)
val desktopApps = listOf(
    DesktopAppItem("Blender 4.2", "3D & Creative", "◈", "4 GB", "apt-get update && apt-get install -y blender"),
    DesktopAppItem("VS Code", "AI & Dev", "〈〉", "2 GB", "apt-get install -y code"),
    DesktopAppItem("Codex + Claude CLI", "AI & Dev", "✳", "1 GB", "npm install -g @openai/codex @anthropic-ai/claude-code"),
    DesktopAppItem("Brave Browser", "Browsers", "◉", "2 GB", "curl -fsS https://dl.brave.com/install.sh | sh"),
    DesktopAppItem("Wine + Bottles", "Windows (.exe)", "▣", "4 GB", "apt-get install -y wine64 wine32 winetricks"),
    DesktopAppItem("GIMP Studio", "3D & Creative", "✿", "2 GB", "apt-get install -y gimp"),
    DesktopAppItem("LibreOffice", "AI & Dev", "▤", "2 GB", "apt-get install -y libreoffice")
)
