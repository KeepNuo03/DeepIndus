package com.induscore.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_operation_log")
data class UploadOperationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,
    val targetScope: String,
    val affectedCount: Int = 0,
    val success: Boolean = true,
    val message: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
