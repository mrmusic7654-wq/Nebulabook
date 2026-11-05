package com.nebulabook.cloudos.domain.model

data class TelemetryState(val cpuPercent: Int = 0, val ramUsedGb: Double = 0.0, val ramTotalGb: Double = 16.0, val pingMs: Int = 0, val isConnected: Boolean = false)
