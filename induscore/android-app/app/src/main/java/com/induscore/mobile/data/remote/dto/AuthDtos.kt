package com.induscore.mobile.data.remote.dto

import com.google.gson.JsonObject

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginPayload(
    val token: String,
    val user: JsonObject? = null
)
