package com.induscore.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.induscore.mobile.data.local.entity.UploadQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadQueueDao {
    @Query("SELECT * FROM upload_queue ORDER BY createdAt DESC")
    fun observeQueue(): Flow<List<UploadQueueEntity>>

    @Query("SELECT * FROM upload_queue WHERE status IN ('pending','failed') AND retryCount < :maxRetries ORDER BY createdAt ASC")
    suspend fun getRetryable(maxRetries: Int): List<UploadQueueEntity>

    @Query("SELECT * FROM upload_queue WHERE status IN ('failed','dead') ORDER BY updatedAt DESC")
    suspend fun getFailedOrDead(): List<UploadQueueEntity>

    @Query("SELECT * FROM upload_queue WHERE status = 'dead' ORDER BY updatedAt DESC")
    suspend fun getDeadOnly(): List<UploadQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: UploadQueueEntity): Long

    @Update
    suspend fun update(item: UploadQueueEntity)

    @Query("SELECT * FROM upload_queue WHERE idempotencyKey = :key LIMIT 1")
    suspend fun findByKey(key: String): UploadQueueEntity?

    @Query("SELECT * FROM upload_queue WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UploadQueueEntity?

    @Query("DELETE FROM upload_queue WHERE status = 'completed' AND updatedAt < :before")
    suspend fun cleanupCompleted(before: Long): Int

    @Query("DELETE FROM upload_queue WHERE status = 'dead' AND updatedAt < :before")
    suspend fun cleanupDead(before: Long): Int
}
