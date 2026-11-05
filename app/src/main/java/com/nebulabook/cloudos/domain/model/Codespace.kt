package com.nebulabook.cloudos.domain.model

data class CodespaceInfo(
    val name: String,
    val state: String,
    val repository: String,
    val webUrl: String,
    val machineName: String,
    val cpus: Int,
    val memoryBytes: Long,
    val storageBytes: Long
)

data class CodespaceMachine(
    val name: String,
    val displayName: String,
    val cpus: Int,
    val memoryBytes: Long,
    val storageBytes: Long
) {
    fun memoryGiB(): String = "%.1f".format(memoryBytes / 1_073_741_824.0)
    fun storageGiB(): String = "%.1f".format(storageBytes / 1_073_741_824.0)
}
