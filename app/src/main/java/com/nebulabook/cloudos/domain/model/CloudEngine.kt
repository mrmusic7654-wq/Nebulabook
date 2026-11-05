package com.nebulabook.cloudos.domain.model

enum class CloudEngine(val title: String, val label: String, val ram: String) {
    GOOGLE("Google Cloud Shell", "PRIMARY", "up to 16 GB"),
    GITHUB("GitHub Codespaces", "BACKUP", "8–16 GB")
}
