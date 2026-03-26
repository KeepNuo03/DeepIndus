package com.induscore.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_task_cache")
data class ReviewTaskCacheEntity(
    @PrimaryKey val taskId: Long,
    val detectionNo: String? = null,
    val serialNo: String? = null,
    val defect: String? = null,
    val severity: String? = null,
    val processStatus: String? = null,
    val timestamp: String? = null,
    val imageUrl: String? = null,
    val syncedAt: Long = System.currentTimeMillis()
)
