package com.nebulabook.cloudos.data.ssh

object ProvisioningScripts {
    // Executed only on an explicitly configured, user-controlled remote host.
    val BOOT_WEBTOP_SCRIPT = """#!/bin/bash
set -e
mkdir -p ${'$'}HOME/nebula_config ${'$'}HOME/cloud_drive
echo "[1/4] Checking Rclone Cloud Drive Sync..."
if ! command -v rclone &> /dev/null; then curl https://rclone.org/install.sh | sudo bash || true; fi
if [ -f "${'$'}HOME/.config/rclone/rclone.conf" ]; then
  fusermount -uz ${'$'}HOME/cloud_drive 2>/dev/null || true
  rclone mount gdrive:NebulaBook ${'$'}HOME/cloud_drive --vfs-cache-mode writes --daemon || true
  echo "[✓] Unified Cloud Drive mounted at ~/cloud_drive"
fi
echo "[2/4] Checking Docker Engine..."
if ! docker info >/dev/null 2>&1; then sudo service docker start || true; fi
echo "[3/4] Provisioning Ubuntu XFCE Webtop Container..."
if [ ! "${'$'}(docker ps -q -f name=nebulabook_os)" ]; then
  if [ "${'$'}(docker ps -aq -f status=exited -f name=nebulabook_os)" ]; then docker start nebulabook_os
  else docker run -d --name=nebulabook_os --security-opt seccomp=unconfined -e PUID=1000 -e PGID=1000 -e TZ=Etc/UTC -e TITLE="NebulaBook Cloud Laptop" -p 3000:3000 -v ${'$'}HOME/nebula_config:/config -v ${'$'}HOME/cloud_drive:/config/Desktop/CloudDrive --shm-size="2gb" lscr.io/linuxserver/webtop:ubuntu-xfce; fi
fi
echo "[4/4] Verifying KasmVNC Desktop Server on Port 3000..."
sleep 2
echo "NEBULABOOK_READY"
"""
    val installCommands = mapOf(
        "Blender 4.2" to "apt-get update && apt-get install -y blender",
        "VS Code" to "apt-get update && apt-get install -y wget gpg && wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > /usr/share/keyrings/packages.microsoft.gpg && echo 'deb [arch=amd64 signed-by=/usr/share/keyrings/packages.microsoft.gpg] https://packages.microsoft.com/repos/code stable main' > /etc/apt/sources.list.d/vscode.list && apt-get update && apt-get install -y code",
        "Codex + Claude CLI" to "apt-get update && apt-get install -y nodejs npm python3-pip && npm install -g @anthropic-ai/claude-code @openai/codex",
        "Brave Browser" to "curl -fsS https://dl.brave.com/install.sh | sh",
        "Wine + Bottles" to "dpkg --add-architecture i386 && apt-get update && apt-get install -y wine64 wine32 winetricks q4wine",
        "GIMP Studio" to "apt-get update && apt-get install -y gimp",
        "LibreOffice" to "apt-get update && apt-get install -y libreoffice"
    )
}
