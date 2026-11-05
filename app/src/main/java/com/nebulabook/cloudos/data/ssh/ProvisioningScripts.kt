package com.nebulabook.cloudos.data.ssh

object ProvisioningScripts {
    /** Run only on the Linux host the user configured and authorized over verified SSH. */
    val BOOT_WEBTOP_SCRIPT = """#!/bin/bash
set -e
mkdir -p ${'$'}HOME/nebula_config ${'$'}HOME/cloud_drive
echo "[1/4] Checking the host's existing Rclone configuration..."
if command -v rclone >/dev/null 2>&1 && [ -f "${'$'}HOME/.config/rclone/rclone.conf" ]; then
  fusermount -uz ${'$'}HOME/cloud_drive 2>/dev/null || true
  if rclone mount gdrive:NebulaBook ${'$'}HOME/cloud_drive --vfs-cache-mode writes --daemon; then
    echo "[✓] Rclone mount command succeeded"
  else
    echo "[!] Rclone mount failed; continuing without a synced drive"
  fi
else
  echo "[i] Rclone remote is not configured on this host; drive sync is not active"
fi
echo "[2/4] Checking Docker Engine..."
if ! docker info >/dev/null 2>&1; then sudo service docker start || true; fi
docker info >/dev/null
echo "[3/4] Provisioning Ubuntu XFCE Webtop container..."
if [ ! "${'$'}(docker ps -q -f name=nebulabook_os)" ]; then
  if [ "${'$'}(docker ps -aq -f status=exited -f name=nebulabook_os)" ]; then docker start nebulabook_os
  else docker run -d --name=nebulabook_os --security-opt seccomp=unconfined -e PUID=1000 -e PGID=1000 -e TZ=Etc/UTC -e TITLE="NebulaBook Cloud Laptop" -p 3000:3000 -v ${'$'}HOME/nebula_config:/config -v ${'$'}HOME/cloud_drive:/config/Desktop/CloudDrive --shm-size="2gb" lscr.io/linuxserver/webtop:ubuntu-xfce; fi
fi
echo "[4/4] Waiting for the Webtop HTTP service on port 3000..."
for ${'$'}attempt in {1..45}; do
  if curl --fail --silent --show-error http://127.0.0.1:3000/ >/dev/null 2>&1; then
    echo "[✓] Webtop HTTP endpoint responded"
    echo "NEBULABOOK_READY"
    exit 0
  fi
  sleep 1
done
echo "[!] Webtop did not become ready before timeout" >&2
exit 1
"""
    val installCommands = mapOf(
        "Blender" to "apt-get update && apt-get install -y blender",
        "VS Code" to "apt-get update && apt-get install -y wget gpg && wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > /usr/share/keyrings/packages.microsoft.gpg && echo 'deb [arch=amd64 signed-by=/usr/share/keyrings/packages.microsoft.gpg] https://packages.microsoft.com/repos/code stable main' > /etc/apt/sources.list.d/vscode.list && apt-get update && apt-get install -y code",
        "Codex + Claude CLI" to "apt-get update && apt-get install -y nodejs npm python3-pip && npm install -g @anthropic-ai/claude-code @openai/codex",
        "Brave Browser" to "curl -fsS https://dl.brave.com/install.sh | sh",
        "Wine + Q4Wine" to "dpkg --add-architecture i386 && apt-get update && apt-get install -y wine64 wine32 winetricks q4wine",
        "GIMP Studio" to "apt-get update && apt-get install -y gimp",
        "LibreOffice" to "apt-get update && apt-get install -y libreoffice"
    )
}
