package com.induscore.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.induscore.mobile.data.local.entity.AuthSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthSessionDao {
    @Query("SELECT * FROM auth_session WHERE id = 1")
    fun observeSession(): Flow<AuthSessionEntity?>

    @Query("SELECT * FROM auth_session WHERE id = 1")
    suspend fun getSession(): AuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: AuthSessionEntity)

    @Query("DELETE FROM auth_session")
    suspend fun clear()
}
