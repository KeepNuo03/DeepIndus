package com.induscore.mobile.ml

interface LocalYoloEngine {
    suspend fun warmup()
    suspend fun infer(imagePath: String): LocalInferenceResult
    fun isReady(): Boolean
}
