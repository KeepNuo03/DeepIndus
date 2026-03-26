package com.induscore.mobile.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AlertNoticeCard
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    onBackHome: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load(limit = 20)
    }

    AppScreenContainer(
        title = "通知中心",
        subtitle = "现场告警与任务提醒"
    ) {
        when (state) {
            UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Text((state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                val notifications = (state as UiState.Success).data
                if (notifications.isEmpty()) {
                    Text("暂无通知", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
                        items(notifications, key = { it.id ?: it.hashCode().toLong() }) { notice ->
                            AlertNoticeCard(
                                title = notice.title ?: "未命名通知",
                                subtitle = "流水线 ${notice.taskStatus ?: "-"} | 组ID: ${notice.detectionNo ?: "-"}",
                                timestamp = notice.timestamp ?: "-",
                                severity = notice.severity,
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
            UiState.Idle -> Unit
        }

        AppGap()
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
