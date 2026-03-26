package com.induscore.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.induscore.mobile.data.local.dao.AuthSessionDao
import com.induscore.mobile.data.local.dao.ReviewTaskCacheDao
import com.induscore.mobile.data.local.dao.UploadOperationLogDao
import com.induscore.mobile.data.local.dao.UploadQueueDao
import com.induscore.mobile.data.local.entity.AuthSessionEntity
import com.induscore.mobile.data.local.entity.ReviewTaskCacheEntity
import com.induscore.mobile.data.local.entity.UploadOperationLogEntity
import com.induscore.mobile.data.local.entity.UploadQueueEntity

@Database(
    entities = [
        AuthSessionEntity::class,
        ReviewTaskCacheEntity::class,
        UploadQueueEntity::class,
        UploadOperationLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authSessionDao(): AuthSessionDao
    abstract fun reviewTaskCacheDao(): ReviewTaskCacheDao
    abstract fun uploadQueueDao(): UploadQueueDao
    abstract fun uploadOperationLogDao(): UploadOperationLogDao
}
