package com.induscore.mobile.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.induscore.mobile.config.AppConfig
import com.induscore.mobile.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Offline upload retry worker.
 */
class UploadQueueWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private enum class ActiveNetworkType { WIFI, CELLULAR, OTHER, OFFLINE }

    companion object {
        private const val KEY_TARGET_ID = "target_upload_id"
        private const val KEY_ALLOW_CELLULAR = "allow_cellular"
        private const val KEY_FORCE_CELLULAR_OVERRIDE = "force_cellular_override"
        private const val UNIQUE_WORK_NAME = "upload_queue_worker_immediate"
        const val MAX_AUTO_RETRIES = 5

        fun enqueueNow(
            context: Context,
            targetId: Long? = null,
            allowCellular: Boolean = false,
            forceCellularOverride: Boolean = false
        ) {
            val builder = OneTimeWorkRequestBuilder<UploadQueueWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
            if (targetId != null) {
                builder.setInputData(
                    androidx.work.workDataOf(
                        KEY_TARGET_ID to targetId,
                        KEY_ALLOW_CELLULAR to allowCellular,
                        KEY_FORCE_CELLULAR_OVERRIDE to forceCellularOverride
                    )
                )
            } else {
                builder.setInputData(
                    androidx.work.workDataOf(
                        KEY_ALLOW_CELLULAR to allowCellular,
                        KEY_FORCE_CELLULAR_OVERRIDE to forceCellularOverride
                    )
                )
            }
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                builder.build()
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val queueDao = ServiceLocator.database.uploadQueueDao()
        val api = ServiceLocator.mobileApiService
        val targetId = inputData.getLong(KEY_TARGET_ID, -1L).takeIf { it >= 0 }
        val allowCellular = inputData.getBoolean(KEY_ALLOW_CELLULAR, false)
        val forceCellularOverride = inputData.getBoolean(KEY_FORCE_CELLULAR_OVERRIDE, false)
        val activeNetwork = detectActiveNetworkType(applicationContext)
        val pending = if (targetId != null) {
            queueDao.findById(targetId)
                ?.takeIf { it.status == "pending" || it.status == "failed" || it.status == "dead" }
                ?.let { listOf(it) }
                ?: emptyList()
        } else {
            queueDao.getRetryable(MAX_AUTO_RETRIES)
        }

        if (allowCellular && !forceCellularOverride) {
            val risk = CellularSyncPolicy.evaluate(pending)
            if (risk.shouldWarn) {
                val warning = CellularSyncPolicy.buildWarningMessage(risk)
                pending.forEach { item ->
                    queueDao.update(
                        item.copy(
                            status = "pending",
                            lastError = warning,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                return@withContext Result.success()
            }
        }

        pending.forEach { item ->
            if (!canUploadOnCurrentNetwork(item.networkState, activeNetwork, allowCellular)) {
                queueDao.update(
                    item.copy(
                        status = "pending",
                        lastError = "当前网络不满足策略，等待 Wi-Fi 或手动允许蜂窝同步",
                        updatedAt = System.currentTimeMillis()
                    )
                )
                return@forEach
            }

            val file = File(item.localFilePath)
            if (!file.exists()) {
                queueDao.update(
                    item.copy(
                        status = "dead",
                        retryCount = item.retryCount + 1,
                        lastError = "Local file missing",
                        updatedAt = System.currentTimeMillis()
                    )
                )
                return@forEach
            }

            queueDao.update(item.copy(status = "uploading", updatedAt = System.currentTimeMillis()))
            runCatching {
                val fileBody = file.asRequestBody("image/*".toMediaType())
                val multipart = MultipartBody.Part.createFormData("file", file.name, fileBody)
                val serialNo = item.serialNo?.toRequestBody("text/plain".toMediaType())
                val productId = item.productId?.toString()?.toRequestBody("text/plain".toMediaType())
                val lineId = item.productionLineId?.toString()?.toRequestBody("text/plain".toMediaType())
                val networkState = item.networkState?.toRequestBody("text/plain".toMediaType())
                api.uploadSampling(
                    idempotencyKey = item.idempotencyKey,
                    file = multipart,
                    serialNo = serialNo,
                    productId = productId,
                    productionLineId = lineId,
                    networkState = networkState
                )
            }.onSuccess { response ->
                val status = response.data?.getAsJsonObject("uploadStatus")
                val recordId = status?.get("recordId")?.asLong
                queueDao.update(
                    item.copy(
                        status = "completed",
                        serverRecordId = recordId,
                        updatedAt = System.currentTimeMillis(),
                        lastError = null
                    )
                )
            }.onFailure { throwable ->
                val nextRetry = item.retryCount + 1
                val nextStatus = if (nextRetry >= MAX_AUTO_RETRIES) "dead" else "failed"
                val reason = if (nextStatus == "dead") {
                    "超过自动重试上限($MAX_AUTO_RETRIES)，请手动重试"
                } else {
                    throwable.message
                }
                queueDao.update(
                    item.copy(
                        status = nextStatus,
                        retryCount = nextRetry,
                        lastError = reason,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        Result.success()
    }

    private fun canUploadOnCurrentNetwork(
        taskPolicyRaw: String?,
        activeNetwork: ActiveNetworkType,
        allowCellular: Boolean
    ): Boolean {
        if (activeNetwork == ActiveNetworkType.OFFLINE) return false
        if (allowCellular) return true
        val policy = normalizeTaskPolicy(taskPolicyRaw)
        return when (policy) {
            AppConfig.NETWORK_POLICY_WIFI_ONLY -> activeNetwork == ActiveNetworkType.WIFI
            AppConfig.NETWORK_POLICY_ANY -> activeNetwork != ActiveNetworkType.OFFLINE
            else -> activeNetwork == ActiveNetworkType.WIFI
        }
    }

    private fun normalizeTaskPolicy(raw: String?): String {
        val normalized = raw?.trim()?.lowercase().orEmpty()
        return when (normalized) {
            "wifi", "wifi_only", "wifi-only" -> AppConfig.NETWORK_POLICY_WIFI_ONLY
            "cellular", "any", "all", "mobile" -> AppConfig.NETWORK_POLICY_ANY
            else -> AppConfig.DEFAULT_UPLOAD_NETWORK_POLICY
        }
    }

    private fun detectActiveNetworkType(context: Context): ActiveNetworkType {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ActiveNetworkType.OFFLINE
        val network = cm.activeNetwork ?: return ActiveNetworkType.OFFLINE
        val caps = cm.getNetworkCapabilities(network) ?: return ActiveNetworkType.OFFLINE
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ActiveNetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ActiveNetworkType.CELLULAR
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ActiveNetworkType.OTHER
            else -> ActiveNetworkType.OFFLINE
        }
    }
}
