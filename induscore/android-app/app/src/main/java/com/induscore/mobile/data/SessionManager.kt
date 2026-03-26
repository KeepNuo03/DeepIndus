package com.induscore.mobile.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.induscore.mobile.data.local.dao.AuthSessionDao
import com.induscore.mobile.data.local.entity.AuthSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(
    private val authSessionDao: AuthSessionDao
) {
    val tokenFlow: Flow<String?> = authSessionDao.observeSession().map { it?.accessToken }
    val roleCodesFlow: Flow<Set<String>> = authSessionDao.observeSession().map { session ->
        runCatching {
            parseRoleCodes(session?.rolesJson ?: "[]")
        }.getOrElse { emptySet() }
    }

    suspend fun getToken(): String? = authSessionDao.getSession()?.accessToken

    suspend fun getRoleCodes(): Set<String> {
        val rolesJson = authSessionDao.getSession()?.rolesJson ?: return emptySet()
        return runCatching {
            parseRoleCodes(rolesJson)
        }.getOrElse { emptySet() }
    }

    suspend fun saveSession(
        token: String,
        username: String,
        userId: Long,
        rolesJson: String
    ) {
        authSessionDao.upsert(
            AuthSessionEntity(
                id = 1,
                accessToken = token,
                username = username,
                userId = userId,
                rolesJson = rolesJson
            )
        )
    }

    suspend fun clearSession() {
        authSessionDao.clear()
    }

    private fun parseRoleCodes(rolesJson: String): Set<String> {
        val element = JsonParser.parseString(rolesJson)
        if (!element.isJsonArray) return emptySet()
        val array = element.asJsonArray
        return array.asRoleCodes()
    }

    private fun JsonArray.asRoleCodes(): Set<String> {
        return mapNotNull { item ->
            when {
                item.isJsonPrimitive && item.asJsonPrimitive.isString -> item.asString
                item.isJsonObject -> item.asJsonObject.roleCodeOrName()
                else -> null
            }
        }.map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun JsonObject.roleCodeOrName(): String? {
        return when {
            has("code") && get("code").isJsonPrimitive -> get("code").asString
            has("name") && get("name").isJsonPrimitive -> get("name").asString
            else -> null
        }
    }
}
