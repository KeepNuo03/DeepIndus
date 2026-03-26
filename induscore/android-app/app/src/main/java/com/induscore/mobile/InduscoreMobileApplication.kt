package com.induscore.mobile

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.worker.UploadQueueWorker
import java.util.concurrent.TimeUnit

class InduscoreMobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        enqueueUploadWorker()
    }

    private fun enqueueUploadWorker() {
        val request = PeriodicWorkRequestBuilder<UploadQueueWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "upload_queue_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
