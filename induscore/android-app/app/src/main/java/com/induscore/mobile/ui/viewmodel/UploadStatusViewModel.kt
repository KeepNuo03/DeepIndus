package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.local.entity.UploadOperationLogEntity
import com.induscore.mobile.data.local.entity.UploadQueueEntity
import com.induscore.mobile.data.remote.dto.MobileUploadStatusDto
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import com.induscore.mobile.worker.CellularSyncPolicy
import com.induscore.mobile.worker.CellularSyncRisk
import com.induscore.mobile.worker.UploadQueueWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UploadStatusViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository

    data class QueueStats(
        val pending: Int = 0,
        val uploading: Int = 0,
        val failed: Int = 0,
        val dead: Int = 0,
        val completed: Int = 0
    )

    data class CellularSyncPrompt(
        val message: String,
        val taskCount: Int,
        val totalBytesText: String
    )

    val queueState: StateFlow<List<UploadQueueEntity>> = mobileRepository.observeUploadQueue()
        .map { it.sortedByDescending { item -> item.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val operationLogsState: StateFlow<List<UploadOperationLogEntity>> = mobileRepository.observeRecentOperationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val queueStatsState: StateFlow<QueueStats> = queueState
        .map { queue ->
            QueueStats(
                pending = queue.count { it.status == "pending" },
                uploading = queue.count { it.status == "uploading" },
                failed = queue.count { it.status == "failed" },
                dead = queue.count { it.status == "dead" },
                completed = queue.count { it.status == "completed" }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QueueStats())

    private val _remoteStatusState = MutableStateFlow<UiState<MobileUploadStatusDto>>(UiState.Idle)
    val remoteStatusState: StateFlow<UiState<MobileUploadStatusDto>> = _remoteStatusState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    private val _cellularSyncPromptState = MutableStateFlow<CellularSyncPrompt?>(null)
    val cellularSyncPromptState: StateFlow<CellularSyncPrompt?> = _cellularSyncPromptState.asStateFlow()

    fun queryRemoteStatus(idempotencyKey: String) {
        val normalized = idempotencyKey.trim()
        if (normalized.isBlank()) {
            _remoteStatusState.value = UiState.Error("请输入幂等键")
            return
        }
        _remoteStatusState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.getUploadStatus(normalized)
            }.onSuccess {
                _remoteStatusState.value = UiState.Success(it)
            }.onFailure {
                _remoteStatusState.value = UiState.Error(it.toUserMessage("查询上传状态失败"))
            }
        }
    }

    fun retrySingle(id: Long) {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val changed = mobileRepository.markUploadForManualRetry(id = id, resetRetryCount = true)
                if (!changed) error("任务不存在")
                UploadQueueWorker.enqueueNow(ServiceLocator.getAppContext(), targetId = id)
                val message = "已触发手动重试"
                mobileRepository.appendOperationLog("manual_retry_single", "single", 1, true, message)
                _actionState.value = UiState.Success(message)
            } catch (t: Throwable) {
                val err = t.toUserMessage("手动重试失败")
                mobileRepository.appendOperationLog("manual_retry_single", "single", 1, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun retryAllFailed() {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val count = mobileRepository.markAllFailedForManualRetry(resetRetryCount = true)
                UploadQueueWorker.enqueueNow(ServiceLocator.getAppContext())
                val message = "已触发 $count 条失败任务重试"
                mobileRepository.appendOperationLog("manual_retry_failed_all", "failed_and_dead", count, true, message)
                _actionState.value = UiState.Success(message)
            } catch (t: Throwable) {
                val err = t.toUserMessage("批量重试失败")
                mobileRepository.appendOperationLog("manual_retry_failed_all", "failed_and_dead", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun retryAllDeadOnly() {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val count = mobileRepository.markDeadForManualRetry(resetRetryCount = true)
                if (count > 0) {
                    UploadQueueWorker.enqueueNow(ServiceLocator.getAppContext())
                }
                val message = "已触发 $count 条超重试任务重试"
                mobileRepository.appendOperationLog("manual_retry_dead_only", "dead", count, true, message)
                _actionState.value = UiState.Success(message)
            } catch (t: Throwable) {
                val err = t.toUserMessage("超重试任务重试失败")
                mobileRepository.appendOperationLog("manual_retry_dead_only", "dead", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun triggerImmediateSync() {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                UploadQueueWorker.enqueueNow(ServiceLocator.getAppContext(), allowCellular = false)
                val message = "已触发立即同步"
                mobileRepository.appendOperationLog("manual_sync_now", "queue", 0, true, message)
                _actionState.value = UiState.Success(message)
            } catch (t: Throwable) {
                val err = t.toUserMessage("立即同步失败")
                mobileRepository.appendOperationLog("manual_sync_now", "queue", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun triggerImmediateSyncAllowCellular() {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val risk = CellularSyncPolicy.evaluate(queueState.value)
                if (risk.shouldWarn) {
                    _cellularSyncPromptState.value = CellularSyncPrompt(
                        message = CellularSyncPolicy.buildWarningMessage(risk),
                        taskCount = risk.taskCount,
                        totalBytesText = CellularSyncPolicy.formatBytes(risk.totalBytes)
                    )
                    _actionState.value = UiState.Error("超过蜂窝阈值，请确认后继续")
                    return@launch
                }
                triggerAllowCellularSync(forceOverride = false, risk = risk)
            } catch (t: Throwable) {
                val err = t.toUserMessage("蜂窝同步触发失败")
                mobileRepository.appendOperationLog("manual_sync_allow_cellular", "queue", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun confirmCellularSyncAnyway() {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val risk = CellularSyncPolicy.evaluate(queueState.value)
                triggerAllowCellularSync(forceOverride = true, risk = risk)
                _cellularSyncPromptState.value = null
            } catch (t: Throwable) {
                val err = t.toUserMessage("蜂窝同步触发失败")
                mobileRepository.appendOperationLog("manual_sync_allow_cellular_force", "queue", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun dismissCellularSyncPrompt() {
        _cellularSyncPromptState.value = null
        if (actionState.value is UiState.Loading) return
        _actionState.value = UiState.Idle
    }

    fun cleanupCompleted(keepHours: Long = 24) {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val removed = mobileRepository.cleanupCompletedHistory(keepRecentMs = keepHours * 60 * 60 * 1000)
                val message = "已清理 $removed 条历史完成记录"
                mobileRepository.appendOperationLog("cleanup_completed", "completed", removed, true, message)
                _actionState.value = UiState.Success(message)
            } catch (t: Throwable) {
                val err = t.toUserMessage("清理记录失败")
                mobileRepository.appendOperationLog("cleanup_completed", "completed", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    fun cleanupTerminal(keepDeadHours: Long = 72, keepCompletedHours: Long = 24) {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val removedDead = mobileRepository.cleanupDeadHistory(keepRecentMs = keepDeadHours * 60 * 60 * 1000)
                val removedCompleted = mobileRepository.cleanupCompletedHistory(
                    keepRecentMs = keepCompletedHours * 60 * 60 * 1000
                )
                val affected = removedDead + removedCompleted
                val message = "已清理 dead=$removedDead, completed=$removedCompleted"
                mobileRepository.appendOperationLog("cleanup_terminal", "dead_and_completed", affected, true, message)
                _actionState.value = UiState.Success(message)
            } catch (t: Throwable) {
                val err = t.toUserMessage("终态清理失败")
                mobileRepository.appendOperationLog("cleanup_terminal", "dead_and_completed", 0, false, err)
                _actionState.value = UiState.Error(err)
            }
        }
    }

    private suspend fun triggerAllowCellularSync(forceOverride: Boolean, risk: CellularSyncRisk) {
        UploadQueueWorker.enqueueNow(
            context = ServiceLocator.getAppContext(),
            allowCellular = true,
            forceCellularOverride = forceOverride
        )
        val riskText = "任务=${risk.taskCount}, 大小=${CellularSyncPolicy.formatBytes(risk.totalBytes)}"
        val action = if (forceOverride) "manual_sync_allow_cellular_force" else "manual_sync_allow_cellular"
        val message = if (forceOverride) {
            "已触发立即同步（允许蜂窝网络，强制确认）"
        } else {
            "已触发立即同步（允许蜂窝网络）"
        }
        mobileRepository.appendOperationLog(action, "queue", risk.taskCount, true, "$message；$riskText")
        _actionState.value = UiState.Success("$message；$riskText")
    }
}
