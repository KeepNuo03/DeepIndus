package com.induscore.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.induscore.mobile.data.local.entity.UploadOperationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadOperationLogDao {
    @Query("SELECT * FROM upload_operation_log ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<UploadOperationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UploadOperationLogEntity): Long

    @Query("DELETE FROM upload_operation_log WHERE createdAt < :before")
    suspend fun cleanupBefore(before: Long): Int
}
