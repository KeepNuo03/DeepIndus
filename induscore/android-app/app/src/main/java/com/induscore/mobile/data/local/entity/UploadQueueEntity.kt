package com.induscore.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "upload_queue",
    indices = [Index(value = ["idempotencyKey"], unique = true)]
)
data class UploadQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idempotencyKey: String,
    val localFilePath: String,
    val serialNo: String? = null,
    val productId: Long? = null,
    val productionLineId: Long? = null,
    val networkState: String? = null,
    val status: String = "pending",
    val retryCount: Int = 0,
    val lastError: String? = null,
    val serverRecordId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
