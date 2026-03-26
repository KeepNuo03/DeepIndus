package com.induscore.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.induscore.mobile.data.local.entity.ReviewTaskCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewTaskCacheDao {
    @Query("SELECT * FROM review_task_cache ORDER BY syncedAt DESC")
    fun observeAll(): Flow<List<ReviewTaskCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ReviewTaskCacheEntity>)

    @Query("DELETE FROM review_task_cache")
    suspend fun clear()
}
