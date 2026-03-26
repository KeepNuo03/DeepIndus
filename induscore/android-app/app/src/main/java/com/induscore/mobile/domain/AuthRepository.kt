package com.induscore.mobile.domain

import com.google.gson.Gson
import com.induscore.mobile.data.SessionManager
import com.induscore.mobile.data.remote.ApiBusinessException
import com.induscore.mobile.data.remote.MobileApiService
import com.induscore.mobile.data.remote.dto.LoginRequest
import com.induscore.mobile.di.ServiceLocator

class AuthRepository(
    private val api: MobileApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(username: String, password: String) {
        val response = api.login(LoginRequest(username = username, password = password))
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        val payload = response.data
        val user = payload.user
        val userId = user?.get("id")?.asLong ?: 0L
        val rolesJson = if (user == null) "[]" else Gson().toJson(user.get("roles"))
        sessionManager.saveSession(
            token = payload.token,
            username = username,
            userId = userId,
            rolesJson = rolesJson
        )
        ServiceLocator.updateAuthToken(payload.token)
    }
}
