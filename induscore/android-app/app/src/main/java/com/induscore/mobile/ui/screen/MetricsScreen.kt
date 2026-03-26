package com.induscore.mobile.ui.screen

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AppCardBlock
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.MetricTile
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.MetricsViewModel

@Composable
fun MetricsScreen(
    onBackHome: () -> Unit,
    viewModel: MetricsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    AppScreenContainer(
        title = "试点指标",
        subtitle = "闭环率、恢复率与时延表现"
    ) {
        when (state) {
            UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Text((state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                val metrics = (state as UiState.Success).data
                AppCardBlock { Text("试点线体: ${metrics.pilotLine ?: "-"}") }
                AppGap(10.dp)
                MetricTile(
                    title = "今日闭环率",
                    value = metrics.todayReviewCloseLoopRate ?: "-",
                    hint = "目标 ${metrics.targetCloseLoopRate ?: "-"}",
                    icon = Icons.Outlined.CheckCircleOutline,
                    tint = Color(0xFF10B981)
                )
                AppGap(10.dp)
                MetricTile(
                    title = "上传恢复率",
                    value = metrics.uploadRecoveryRate ?: "-",
                    hint = "目标 ${metrics.targetUploadRecoveryRate ?: "-"}",
                    icon = Icons.Outlined.AutoGraph,
                    tint = Color(0xFF3B82F6)
                )
                AppGap(10.dp)
                MetricTile(
                    title = "接口时延 P95",
                    value = "${metrics.targetApiP95LatencyMs ?: "-"} ms",
                    hint = "生成于 ${metrics.generatedAt ?: "-"}",
                    icon = Icons.Outlined.Speed,
                    tint = Color(0xFFF59E0B)
                )
                AppGap(10.dp)
                MetricTile(
                    title = "上传完成 / 失败",
                    value = "${metrics.uploadCompleted ?: 0} / ${metrics.uploadFailed ?: 0}",
                    hint = "今日复核 ${metrics.todayReviewClosed ?: 0}/${metrics.todayReviewTotal ?: 0}",
                    icon = Icons.Outlined.Bolt,
                    tint = Color(0xFF8B5CF6)
                )
            }
            UiState.Idle -> Unit
        }

        AppGap(16.dp)
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
