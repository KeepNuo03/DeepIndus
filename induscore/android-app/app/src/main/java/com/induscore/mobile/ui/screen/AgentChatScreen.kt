package com.induscore.mobile.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ui.components.AgentBrandLogoBadge
import com.induscore.mobile.ui.viewmodel.AgentChatViewModel
import com.induscore.mobile.ui.viewmodel.AgentUiMessage
import kotlinx.coroutines.launch

@Composable
fun AgentChatScreen(
    onBackHome: () -> Unit,
    initialScene: String = "general",
    initialTaskId: Long? = null,
    initialIdempotencyKey: String? = null,
    initialPrefill: String? = null,
    viewModel: AgentChatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    var hasAutoAsked by remember { mutableStateOf(false) }
    var inputFocused by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val inputMinHeight by animateDpAsState(targetValue = if (inputFocused) 56.dp else 48.dp, label = "inputHeight")
    val bottomGap by animateDpAsState(targetValue = if (inputFocused) 4.dp else 14.dp, label = "bottomGap")

    LaunchedEffect(initialScene, initialTaskId, initialIdempotencyKey) {
        viewModel.setContext(
            scene = initialScene,
            taskId = initialTaskId,
            idempotencyKey = initialIdempotencyKey
        )
    }
    LaunchedEffect(initialPrefill) {
        if (!initialPrefill.isNullOrBlank()) {
            input = initialPrefill
        }
    }
    LaunchedEffect(initialPrefill, state.isSending) {
        if (!hasAutoAsked && !initialPrefill.isNullOrBlank() && !state.isSending && state.messages.isEmpty()) {
            viewModel.sendQuestion(initialPrefill)
            hasAutoAsked = true
            input = ""
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadSessionList(reset = true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text("历史会话", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "已加载 ${state.sessionItems.size}/${state.sessionTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        DrawerActionChip(
                            text = "刷新",
                            icon = Icons.Rounded.Refresh,
                            onClick = { viewModel.loadSessionList(reset = true) },
                            modifier = Modifier.weight(1f)
                        )
                        DrawerActionChip(
                            text = "更多",
                            icon = Icons.Rounded.Menu,
                            onClick = { viewModel.loadSessionList(reset = false) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        DrawerActionChip(
                            text = "新会话",
                            icon = Icons.Rounded.Add,
                            onClick = { viewModel.resetCurrentSession() },
                            modifier = Modifier.weight(1f)
                        )
                        if (state.sessionId != null) {
                            DrawerActionChip(
                                text = "归档",
                                icon = Icons.Rounded.Archive,
                                onClick = { viewModel.archiveCurrentSession() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.sessionItems, key = { it.sessionId }) { session ->
                            val selected = session.sessionId == state.sessionId
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .clickable {
                                        viewModel.switchSession(session.sessionId)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = session.title?.ifBlank { "新会话" } ?: "新会话",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = session.lastMessageAt ?: "-",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "菜单",
                            tint = Color.White
                        )
                    }
                    AgentBrandLogoBadge(size = 28.dp, iconSize = 14.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "质检AI助理",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = bottomGap)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextField(
                            value = input,
                            onValueChange = {
                                input = it
                                if (state.errorMessage != null) {
                                    viewModel.clearError()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { inputFocused = it.isFocused },
                            minLines = if (inputFocused) 2 else 1,
                            maxLines = if (inputFocused) 6 else 4,
                            placeholder = { Text("尽管问，带图也行") },
                            enabled = !state.isSending && !state.isSessionLoading,
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.sendQuestion(input)
                                input = ""
                            },
                            enabled = !state.isSending && !state.isSessionLoading && input.isNotBlank(),
                            modifier = Modifier.padding(bottom = ((inputMinHeight - 40.dp) / 2).coerceAtLeast(0.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "发送",
                                tint = if (!state.isSending && input.isNotBlank()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    if (!state.errorMessage.isNullOrBlank()) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    FilledTonalButton(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 6.dp),
                        onClick = onBackHome
                    ) {
                        Text("返回系统主页")
                    }
                }
            }
        ) { innerPadding ->
            if (state.messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "你好，我可以帮你分析工作台、检测记录、大屏、生产线和模型管理数据。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.messages) { index, message ->
                        AgentMessageBubble(
                            message = message,
                            modifier = Modifier.fillMaxWidth(),
                            isLast = index == state.messages.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerActionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AgentMessageBubble(
    message: AgentUiMessage,
    modifier: Modifier = Modifier,
    isLast: Boolean = false
) {
    val isUser = message.role.equals("user", ignoreCase = true)
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val title = if (isUser) "我" else "质检AI助理"
    val accentColor = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFF8B5CF6)

    Column(
        modifier = modifier,
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        if (isLast && !isUser) {
            Text(
                text = "回答遵循：结论 - 依据 - 下一步建议",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
