package com.induscore.mobile.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.AppPrimaryButton
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.components.SemanticIconBadge
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.ReviewTaskListViewModel

@Composable
fun ReviewTaskListScreen(
    onOpenTaskDetail: (Long) -> Unit,
    onBackHome: () -> Unit,
    viewModel: ReviewTaskListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    AppScreenContainer(
        title = "复核任务",
        subtitle = "查看待处理任务并进入详情"
    ) {
        when (state) {
            UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is UiState.Error -> Text(
                text = (state as UiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is UiState.Success -> {
                val itemsData = (state as UiState.Success).data
                LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
                    items(itemsData) { item ->
                        androidx.compose.material3.Card(
                            modifier = Modifier.padding(top = 12.dp).clickable { onOpenTaskDetail(item.id) },
                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val status = item.processStatus ?: "-"
                                val (statusIcon, statusColor) = when (status) {
                                    "已完成" -> Icons.Outlined.CheckCircleOutline to Color(0xFF10B981)
                                    "处理中" -> Icons.Outlined.PendingActions to Color(0xFFF59E0B)
                                    else -> Icons.Outlined.BugReport to Color(0xFF3B82F6)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("任务ID: ${item.id}")
                                    SemanticIconBadge(icon = statusIcon, tint = statusColor)
                                }
                                Text("检测编号: ${item.detectionNo ?: "-"}")
                                Text("缺陷: ${item.defect ?: "-"}")
                                Text("状态: $status", color = statusColor)
                                Text("时间: ${item.timestamp ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))
                                AppPrimaryButton(text = "查看详情", onClick = { onOpenTaskDetail(item.id) })
                            }
                        }
                    }
                }
            }
            UiState.Idle -> Unit
        }
        AppGap(12.dp)
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
