package com.induscore.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.DeepIndusBrandHeader
import com.induscore.mobile.ui.components.BrandPrimaryButtonColor
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("123456") }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(260.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0x222563EB))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(260.dp)
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0x224F46E5))
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x801E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                        .border(1.dp, Color(0x55334155), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(20.dp)
                ) {
                    DeepIndusBrandHeader(titleSizeSp = 22)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("现场巡检系统", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text("移动客户端", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "用于现场抽检、复核任务处理与上传状态追踪，支持弱网重试。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginFeatureItem(
                        icon = Icons.Outlined.Security,
                        text = "金融级数据加密协议"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LoginFeatureItem(
                        icon = Icons.Outlined.Public,
                        text = "2026年全球工业互联标准"
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("欢迎登录", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("请输入账号与密码继续巡检任务", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    singleLine = true,
                    label = { Text("用户名或邮箱") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0x661E293B),
                        focusedContainerColor = Color(0x661E293B),
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedTextColor = Color(0xFFE2E8F0),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("安全密码") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0x661E293B),
                        focusedContainerColor = Color(0x661E293B),
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedTextColor = Color(0xFFE2E8F0),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("记住我的账户", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("忘记密码？", style = MaterialTheme.typography.bodySmall, color = Color(0xFF60A5FA))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(48.dp),
                    enabled = state !is UiState.Loading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryButtonColor)
                ) {
                    Text("立即登录", color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                when (state) {
                    UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is UiState.Error -> Text(
                        text = (state as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    else -> Unit
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "账号由管理员统一分配",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun LoginFeatureItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0x1A3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )
    }
}
