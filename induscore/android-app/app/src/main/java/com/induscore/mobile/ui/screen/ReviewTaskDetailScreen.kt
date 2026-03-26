package com.induscore.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AppCardBlock
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.AppPrimaryButton
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.ReviewTaskDetailViewModel

@Composable
fun ReviewTaskDetailScreen(
    taskId: Long,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onAskAgent: (taskId: Long, prefillQuestion: String) -> Unit,
    viewModel: ReviewTaskDetailViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    var selectedDecision by remember { mutableStateOf("approve") }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        viewModel.load(taskId)
    }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            viewModel.load(taskId)
        }
    }

    AppScreenContainer(
        title = "复核详情",
        subtitle = "任务ID: $taskId"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            when (detailState) {
                UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is UiState.Error -> {
                    Text(
                        text = (detailState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is UiState.Success -> {
                    val detail = (detailState as UiState.Success).data
                    AppCardBlock {
                        Text("检测编号: ${detail.detectionNo ?: "-"}")
                        Text("序列号: ${detail.serialNo ?: "-"}")
                        Text("缺陷类型: ${detail.defect ?: "-"}")
                        Text("严重程度: ${detail.severity ?: "-"}")
                        Text("流程状态: ${detail.processStatus ?: "-"}")
                        Text("时间: ${detail.timestamp ?: "-"}")
                        Text("模型建议: ${detail.modelSuggestion ?: "-"}")
                        Text("历史复核结果: ${detail.reviewResult ?: "-"}")
                        Text("历史复核意见: ${detail.reviewComment ?: "-"}")
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("复核操作", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { selectedDecision = "approve" },
                            modifier = Modifier.weight(1f),
                            enabled = selectedDecision != "approve"
                        ) {
                            Text("通过")
                        }
                        Button(
                            onClick = { selectedDecision = "reject" },
                            modifier = Modifier.weight(1f),
                            enabled = selectedDecision != "reject"
                        ) {
                            Text("驳回")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("复核备注（可选）") },
                        placeholder = { Text("建议填写现场复核说明，便于追踪") },
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    AppSecondaryButton(
                        text = "一键提问 Agent（分析当前任务）",
                        onClick = {
                            onAskAgent(
                                taskId,
                                "请分析复核任务#$taskId 的风险点，并给出优先处理建议。"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppPrimaryButton(
                        text = if (submitState is UiState.Loading) "提交中..." else "提交复核",
                        enabled = submitState !is UiState.Loading
                    ) {
                        viewModel.submit(
                            taskId = taskId,
                            decision = selectedDecision,
                            comment = comment
                        )
                    }

                    if (submitState is UiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (submitState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (submitState is UiState.Success) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "提交成功",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                UiState.Idle -> Unit
            }
        }

        AppGap(16.dp)
        AppSecondaryButton(text = "返回任务列表", onClick = onBack)
        AppGap(8.dp)
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
