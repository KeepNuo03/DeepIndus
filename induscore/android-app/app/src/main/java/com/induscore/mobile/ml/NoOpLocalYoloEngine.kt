package com.induscore.mobile.ml

class NoOpLocalYoloEngine(
    private val reason: String
) : LocalYoloEngine {
    override suspend fun warmup() = Unit

    override suspend fun infer(imagePath: String): LocalInferenceResult {
        return LocalInferenceResult(
            modelSource = "disabled",
            latencyMs = 0L,
            detections = emptyList(),
            warning = reason
        )
    }

    override fun isReady(): Boolean = false
}
