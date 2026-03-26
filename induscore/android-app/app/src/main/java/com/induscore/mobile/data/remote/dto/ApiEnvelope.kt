package com.induscore.mobile.data.remote.dto

data class ApiEnvelope<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: Long
)
