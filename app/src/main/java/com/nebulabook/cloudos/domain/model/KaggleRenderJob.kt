package com.nebulabook.cloudos.domain.model

data class KaggleRenderJob(val file: String, val startFrame: Int, val endFrame: Int, val status: String, val progress: Int)
