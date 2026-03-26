package com.induscore.mobile.worker

import com.induscore.mobile.config.AppConfig
import com.induscore.mobile.data.local.entity.UploadQueueEntity
import java.io.File
import kotlin.math.roundToLong

data class CellularSyncRisk(
    val taskCount: Int,
    val totalBytes: Long,
    val exceedTaskThreshold: Boolean,
    val exceedBytesThreshold: Boolean
) {
    val shouldWarn: Boolean
        get() = exceedTaskThreshold || exceedBytesThreshold
}

object CellularSyncPolicy {
    private val retryableStatuses = setOf("pending", "failed", "dead")

    fun evaluate(
        queueItems: List<UploadQueueEntity>,
        fileSizeResolver: (String) -> Long = ::resolveFileSize
    ): CellularSyncRisk {
        val candidates = queueItems.filter { retryableStatuses.contains(it.status) }
        val totalBytes = candidates.sumOf { item ->
            fileSizeResolver(item.localFilePath).coerceAtLeast(0L)
        }
        val taskCount = candidates.size
        return CellularSyncRisk(
            taskCount = taskCount,
            totalBytes = totalBytes,
            exceedTaskThreshold = taskCount > AppConfig.CELLULAR_SYNC_TASK_THRESHOLD,
            exceedBytesThreshold = totalBytes > AppConfig.CELLULAR_SYNC_TOTAL_BYTES_THRESHOLD
        )
    }

    fun buildWarningMessage(risk: CellularSyncRisk): String {
        val reasons = buildList {
            if (risk.exceedTaskThreshold) {
                add("任务数 ${risk.taskCount} > ${AppConfig.CELLULAR_SYNC_TASK_THRESHOLD}")
            }
            if (risk.exceedBytesThreshold) {
                add("总大小 ${formatBytes(risk.totalBytes)} > ${formatBytes(AppConfig.CELLULAR_SYNC_TOTAL_BYTES_THRESHOLD)}")
            }
        }
        val reasonText = if (reasons.isEmpty()) "未超过阈值" else reasons.joinToString("；")
        return "蜂窝同步风险提示：$reasonText。继续将消耗较多流量，建议优先连接 Wi-Fi。"
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "${(kb * 10).roundToLong() / 10.0} KB"
        val mb = kb / 1024.0
        if (mb < 1024) return "${(mb * 10).roundToLong() / 10.0} MB"
        val gb = mb / 1024.0
        return "${(gb * 10).roundToLong() / 10.0} GB"
    }

    private fun resolveFileSize(path: String): Long {
        return runCatching {
            val file = File(path)
            if (file.exists()) file.length() else 0L
        }.getOrDefault(0L)
    }
}
