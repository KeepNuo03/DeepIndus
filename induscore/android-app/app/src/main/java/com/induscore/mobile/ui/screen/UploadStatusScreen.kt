package com.induscore.mobile.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AppDangerButton
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.AppPrimaryButton
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.UploadStatusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UploadStatusScreen(
    onBackHome: () -> Unit,
    onAskAgent: (idempotencyKey: String, prefillQuestion: String) -> Unit,
    viewModel: UploadStatusViewModel = viewModel()
) {
    val queue by viewModel.queueState.collectAsStateWithLifecycle()
    val stats by viewModel.queueStatsState.collectAsStateWithLifecycle()
    val opLogs by viewModel.operationLogsState.collectAsStateWithLifecycle()
    val cellularPrompt by viewModel.cellularSyncPromptState.collectAsStateWithLifecycle()
    val remoteState by viewModel.remoteStatusState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    var keyInput by remember { mutableStateOf("") }
    var showAdvancedActions by remember { mutableStateOf(false) }

    AppScreenContainer(
        title = "上传状态",
        subtitle = "本地重试队列与服务端幂等状态"
    ) {
        cellularPrompt?.let { prompt ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissCellularSyncPrompt() },
                title = { Text("蜂窝同步风险提示") },
                text = {
                    Text(
                        "${prompt.message}\n\n当前待同步任务：${prompt.taskCount} 条\n估算流量：${prompt.totalBytesText}"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmCellularSyncAnyway() }) {
                        Text("仍然继续")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissCellularSyncPrompt() }) {
                        Text("取消")
                    }
                }
            )
        }

        OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("幂等键") },
            placeholder = { Text("例如: MOB-xxxxxxxx-xxxx") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        AppGap(8.dp)
        AppPrimaryButton(
            text = if (remoteState is UiState.Loading) "查询中..." else "查询服务端状态",
            enabled = remoteState !is UiState.Loading
        ) { viewModel.queryRemoteStatus(keyInput) }
        AppGap(8.dp)
        val remoteKey = (remoteState as? UiState.Success)?.data?.idempotencyKey
        val targetKey = keyInput.trim().ifBlank { remoteKey.orEmpty() }
        AppSecondaryButton(
            text = "一键提问 Agent（分析上传状态）",
            onClick = {
                if (targetKey.isNotBlank()) {
                    onAskAgent(
                        targetKey,
                        "请基于当前上传任务状态（幂等键: $targetKey）分析风险，并给出下一步建议。"
                    )
                }
            }
        )
        AppGap(8.dp)
        AppPrimaryButton(text = "立即同步队列", onClick = { viewModel.triggerImmediateSync() })
        AppGap(8.dp)
        AppSecondaryButton(
            text = if (showAdvancedActions) "收起高级操作" else "展开高级操作",
            onClick = { showAdvancedActions = !showAdvancedActions }
        )
        if (showAdvancedActions) {
            AppGap(8.dp)
            AppSecondaryButton(
                text = "立即同步（允许蜂窝）",
                onClick = { viewModel.triggerImmediateSyncAllowCellular() }
            )
            AppGap(8.dp)
            AppDangerButton(text = "重试全部失败任务", onClick = { viewModel.retryAllFailed() })
            AppGap(8.dp)
            AppDangerButton(text = "仅重试超重试任务（dead）", onClick = { viewModel.retryAllDeadOnly() })
            AppGap(8.dp)
            AppSecondaryButton(text = "清理历史完成记录", onClick = { viewModel.cleanupCompleted() })
            AppGap(8.dp)
            AppSecondaryButton(text = "清理终态记录（dead+completed）", onClick = { viewModel.cleanupTerminal() })
        }

        when (actionState) {
            UiState.Loading -> {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text((actionState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text((actionState as UiState.Success).data, color = MaterialTheme.colorScheme.primary)
            }
            UiState.Idle -> Unit
        }

        when (remoteState) {
            UiState.Loading -> {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text((remoteState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                val status = (remoteState as UiState.Success).data
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("查询结果", style = MaterialTheme.typography.titleMedium)
                        Text("幂等键: ${status.idempotencyKey}")
                        Text("状态: ${status.status}")
                        Text("记录ID: ${status.serverRecordId ?: "-"}")
                        Text("重试次数: ${status.retryCount ?: "-"}")
                        Text("更新时间: ${status.updatedAt ?: "-"}")
                        Text("说明: ${status.message ?: "-"}")
                    }
                }
            }
            UiState.Idle -> Unit
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("本地上传队列", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("终态治理面板", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("pending: ${stats.pending}", modifier = Modifier.weight(1f))
                    Text("uploading: ${stats.uploading}", modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("failed: ${stats.failed}", modifier = Modifier.weight(1f))
                    Text("dead: ${stats.dead}", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                }
                Text("completed: ${stats.completed}")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("最近操作记录", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                if (opLogs.isEmpty()) {
                    Text("暂无操作日志", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val formatter = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()) }
                    opLogs.take(5).forEach { log ->
                        val time = formatter.format(Date(log.createdAt))
                        val statusText = if (log.success) "成功" else "失败"
                        val statusColor = if (log.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        Text(
                            text = "[$time] ${log.actionType}(${log.targetScope}) x${log.affectedCount} - $statusText",
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor
                        )
                        if (!log.message.isNullOrBlank()) {
                            Text(
                                text = log.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (queue.isEmpty()) {
            Text(
                text = "当前无上传任务",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true)
            ) {
                items(queue, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Key: ${item.idempotencyKey}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "状态: ${item.status}",
                                color = when (item.status) {
                                    "completed" -> MaterialTheme.colorScheme.primary
                                    "failed", "dead" -> MaterialTheme.colorScheme.error
                                    "uploading" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text("重试: ${item.retryCount}")
                            Text("记录ID: ${item.serverRecordId ?: "-"}")
                            Text("错误: ${item.lastError ?: "-"}")
                            if (item.status == "failed" || item.status == "dead") {
                                Spacer(modifier = Modifier.height(8.dp))
                                AppDangerButton(text = "手动重试该任务", onClick = { viewModel.retrySingle(item.id) })
                            }
                        }
                    }
                }
            }
        }

        AppGap()
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
