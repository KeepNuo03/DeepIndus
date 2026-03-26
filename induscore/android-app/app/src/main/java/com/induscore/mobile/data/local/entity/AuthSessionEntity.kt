package com.induscore.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_session")
data class AuthSessionEntity(
    @PrimaryKey val id: Int = 1,
    val accessToken: String,
    val username: String,
    val userId: Long,
    val rolesJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
