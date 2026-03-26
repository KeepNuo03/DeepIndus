package com.induscore.mobile.ml

data class LocalDetection(
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class LocalInferenceResult(
    val modelSource: String,
    val latencyMs: Long,
    val detections: List<LocalDetection>,
    val warning: String? = null
)
