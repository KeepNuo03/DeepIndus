package com.induscore.mobile.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.AppPrimaryButton
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.components.MetricTile
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.WorkbenchViewModel

@Composable
fun WorkbenchScreen(
    onOpenTasks: () -> Unit,
    onBackHome: () -> Unit,
    viewModel: WorkbenchViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    AppScreenContainer(
        title = "工作台",
        subtitle = "现场任务概览与复核入口"
    ) {
        when (state) {
            UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Text(
                    text = (state as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Success -> {
                val data = (state as UiState.Success).data
                MetricTile(
                    title = "待复核",
                    value = data.pendingReviewCount.toString(),
                    hint = "复核入口",
                    icon = Icons.Outlined.TaskAlt,
                    tint = Color(0xFF3B82F6)
                )
                AppGap(10.dp)
                MetricTile(
                    title = "处理中",
                    value = data.processingCount.toString(),
                    hint = "实时进度",
                    icon = Icons.Outlined.HourglassTop,
                    tint = Color(0xFFF59E0B)
                )
                AppGap(10.dp)
                MetricTile(
                    title = "今日抽检",
                    value = data.todaySamplingCount.toString(),
                    hint = "当天任务",
                    icon = Icons.Outlined.CloudQueue,
                    tint = Color(0xFF0EA5E9)
                )
                AppGap(10.dp)
                MetricTile(
                    title = "严重告警",
                    value = data.severeAlertCount.toString(),
                    hint = "需优先处理",
                    icon = Icons.Outlined.ErrorOutline,
                    tint = Color(0xFFEF4444)
                )
                AppGap(10.dp)
                Text("上传队列: ${data.uploadQueueStatus}", color = MaterialTheme.colorScheme.secondary)
            }
            UiState.Idle -> Unit
        }
        AppGap(16.dp)
        AppPrimaryButton(text = "进入复核任务", onClick = onOpenTasks)
        AppGap()
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
