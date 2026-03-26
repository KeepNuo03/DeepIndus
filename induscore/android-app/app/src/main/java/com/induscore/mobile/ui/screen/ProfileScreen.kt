package com.induscore.mobile.ui.screen

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
import com.induscore.mobile.ui.components.AppCardBlock
import com.induscore.mobile.ui.components.AppGap
import com.induscore.mobile.ui.components.AppScreenContainer
import com.induscore.mobile.ui.components.AppSecondaryButton
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onBackHome: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    AppScreenContainer(
        title = "个人信息",
        subtitle = "当前登录账号与岗位角色"
    ) {
        when (state) {
            UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Text((state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                val profile = (state as UiState.Success).data
                AppCardBlock {
                    Text("用户ID: ${profile.userId ?: "-"}")
                    Text("用户名: ${profile.username ?: "-"}")
                    Text("姓名: ${profile.name ?: "-"}")
                    Text("部门ID: ${profile.departmentId ?: "-"}")
                    Text("岗位: ${profile.position ?: "-"}")
                    Text("客户端类型: ${profile.clientType ?: "-"}")
                    Text("角色: ${profile.roles?.joinToString(", ") ?: "-"}")
                }
            }
            UiState.Idle -> Unit
        }

        AppGap(16.dp)
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}
