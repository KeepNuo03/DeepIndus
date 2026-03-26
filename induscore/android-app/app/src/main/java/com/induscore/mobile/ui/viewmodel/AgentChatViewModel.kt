package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.remote.dto.AgentSessionSummaryDto
import com.induscore.mobile.data.remote.dto.AgentToolCall
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.domain.MobileRepository
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AgentUiMessage(
    val role: String,
    val content: String
)

data class AgentChatUiState(
    val sessionId: String? = null,
    val scene: String = "general",
    val page: String = "android_agent",
    val taskId: Long? = null,
    val idempotencyKey: String? = null,
    val messages: List<AgentUiMessage> = emptyList(),
    val isSending: Boolean = false,
    val isSessionLoading: Boolean = false,
    val isSessionListLoading: Boolean = false,
    val traceId: String? = null,
    val lastToolCalls: List<AgentToolCall> = emptyList(),
    val errorMessage: String? = null,
    val sessionItems: List<AgentSessionSummaryDto> = emptyList(),
    val sessionPage: Int = 1,
    val sessionTotal: Long = 0
)

class AgentChatViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository
    private val _state = MutableStateFlow(AgentChatUiState())
    val state: StateFlow<AgentChatUiState> = _state.asStateFlow()

    fun setScene(scene: String) {
        if (scene.isBlank()) return
        _state.value = _state.value.copy(scene = scene)
    }

    fun setContext(
        scene: String,
        taskId: Long? = null,
        idempotencyKey: String? = null,
        page: String = "android_agent"
    ) {
        _state.value = _state.value.copy(
            scene = if (scene.isBlank()) "general" else scene,
            taskId = taskId,
            idempotencyKey = idempotencyKey,
            page = page
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun resetCurrentSession() {
        _state.value = _state.value.copy(
            sessionId = null,
            traceId = null,
            messages = emptyList(),
            lastToolCalls = emptyList(),
            errorMessage = null
        )
    }

    fun loadSessionList(reset: Boolean = false) {
        if (_state.value.isSessionListLoading) return
        val targetPage = if (reset) 1 else _state.value.sessionPage
        _state.value = _state.value.copy(
            isSessionListLoading = true,
            errorMessage = null
        )
        viewModelScope.launch {
            runCatching {
                mobileRepository.listAgentSessions(page = targetPage, pageSize = 10)
            }.onSuccess { page ->
                val merged = if (reset || targetPage == 1) {
                    page.items
                } else {
                    (_state.value.sessionItems + page.items).distinctBy { it.sessionId }
                }
                _state.value = _state.value.copy(
                    isSessionListLoading = false,
                    sessionItems = merged,
                    sessionPage = page.page + 1,
                    sessionTotal = page.total
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isSessionListLoading = false,
                    errorMessage = error.toUserMessage("会话列表加载失败")
                )
            }
        }
    }

    fun switchSession(sessionId: String) {
        if (sessionId.isBlank() || _state.value.isSessionLoading) return
        _state.value = _state.value.copy(
            isSessionLoading = true,
            errorMessage = null
        )
        viewModelScope.launch {
            runCatching {
                mobileRepository.getAgentSessionDetail(sessionId = sessionId, page = 1, pageSize = 80)
            }.onSuccess { detail ->
                _state.value = _state.value.copy(
                    sessionId = detail.sessionId,
                    traceId = null,
                    isSessionLoading = false,
                    lastToolCalls = emptyList(),
                    messages = detail.messages.map {
                        AgentUiMessage(role = it.role, content = it.content)
                    }
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isSessionLoading = false,
                    errorMessage = error.toUserMessage("会话详情加载失败")
                )
            }
        }
    }

    fun archiveCurrentSession() {
        val currentSessionId = _state.value.sessionId ?: return
        if (_state.value.isSessionLoading) return
        _state.value = _state.value.copy(isSessionLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                mobileRepository.archiveAgentSession(currentSessionId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isSessionLoading = false,
                    sessionId = null,
                    traceId = null,
                    messages = emptyList(),
                    lastToolCalls = emptyList(),
                    sessionItems = _state.value.sessionItems.filterNot { it.sessionId == currentSessionId }
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isSessionLoading = false,
                    errorMessage = error.toUserMessage("会话归档失败")
                )
            }
        }
    }

    fun sendQuestion(input: String) {
        val question = input.trim()
        if (question.isBlank() || _state.value.isSending || _state.value.isSessionLoading) {
            return
        }
        val snapshot = _state.value
        _state.value = snapshot.copy(
            isSending = true,
            errorMessage = null,
            lastToolCalls = emptyList(),
            messages = snapshot.messages +
                    AgentUiMessage(role = "user", content = question) +
                    AgentUiMessage(role = "assistant", content = "")
        )
        viewModelScope.launch {
            runCatching {
                mobileRepository.streamChatWithAgent(
                    question = question,
                    sessionId = _state.value.sessionId,
                    scene = _state.value.scene,
                    page = _state.value.page,
                    pageParams = buildPageParams(_state.value.taskId, _state.value.idempotencyKey),
                    taskId = _state.value.taskId,
                    idempotencyKey = _state.value.idempotencyKey,
                    onEvent = { event -> consumeStreamEvent(event) }
                )
            }.onSuccess {
                if (_state.value.isSending) {
                    _state.value = _state.value.copy(isSending = false)
                }
            }.onFailure { error ->
                // 流式失败时自动降级为同步请求，提升弱网/代理环境可用性。
                runCatching {
                    mobileRepository.chatWithAgent(
                        question = question,
                        sessionId = _state.value.sessionId,
                        scene = _state.value.scene,
                        page = _state.value.page,
                        pageParams = buildPageParams(_state.value.taskId, _state.value.idempotencyKey),
                        taskId = _state.value.taskId,
                        idempotencyKey = _state.value.idempotencyKey
                    )
                }.onSuccess { response ->
                    _state.value = _state.value.copy(
                        sessionId = response.sessionId,
                        isSending = false,
                        traceId = response.traceId,
                        lastToolCalls = response.toolCalls
                    )
                    replaceAssistantContent(response.answer)
                }.onFailure { fallbackError ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        errorMessage = fallbackError.toUserMessage(
                            error.toUserMessage("Agent 回复失败，请稍后重试")
                        )
                    )
                    cleanupEmptyAssistantTail()
                }
            }
        }
    }

    private fun consumeStreamEvent(event: MobileRepository.AgentStreamEvent) {
        when (event) {
            is MobileRepository.AgentStreamEvent.Meta -> {
                _state.value = _state.value.copy(
                    sessionId = event.sessionId ?: _state.value.sessionId,
                    traceId = event.traceId ?: _state.value.traceId
                )
            }
            is MobileRepository.AgentStreamEvent.Tool -> {
                val nextTools = _state.value.lastToolCalls + AgentToolCall(
                    tool = event.tool,
                    success = event.success,
                    latencyMs = event.latencyMs
                )
                _state.value = _state.value.copy(lastToolCalls = nextTools)
            }
            is MobileRepository.AgentStreamEvent.Chunk -> appendAssistantChunk(event.text)
            is MobileRepository.AgentStreamEvent.Done -> {
                _state.value = _state.value.copy(isSending = false)
                cleanupEmptyAssistantTail()
            }
            is MobileRepository.AgentStreamEvent.Error -> {
                _state.value = _state.value.copy(
                    errorMessage = event.message ?: "Agent 服务异常，请稍后重试"
                )
            }
        }
    }

    private fun appendAssistantChunk(chunk: String) {
        if (chunk.isBlank()) return
        val current = _state.value
        val list = current.messages.toMutableList()
        if (list.isEmpty() || !list.last().role.equals("assistant", ignoreCase = true)) {
            list.add(AgentUiMessage(role = "assistant", content = chunk))
        } else {
            val last = list.last()
            list[list.lastIndex] = last.copy(content = last.content + chunk)
        }
        _state.value = current.copy(messages = list)
    }

    private fun replaceAssistantContent(content: String) {
        val current = _state.value
        val list = current.messages.toMutableList()
        if (list.isEmpty() || !list.last().role.equals("assistant", ignoreCase = true)) {
            list.add(AgentUiMessage(role = "assistant", content = content))
        } else {
            list[list.lastIndex] = list.last().copy(content = content)
        }
        _state.value = current.copy(messages = list)
    }

    private fun cleanupEmptyAssistantTail() {
        val current = _state.value
        val list = current.messages.toMutableList()
        if (list.isNotEmpty()) {
            val last = list.last()
            if (last.role.equals("assistant", ignoreCase = true) && last.content.isBlank()) {
                list.removeAt(list.lastIndex)
            }
        }
        _state.value = current.copy(messages = list)
    }

    private fun buildPageParams(taskId: Long?, idempotencyKey: String?): Map<String, Any?>? {
        val params = linkedMapOf<String, Any?>()
        if (taskId != null) {
            params["taskId"] = taskId
        }
        if (!idempotencyKey.isNullOrBlank()) {
            params["idempotencyKey"] = idempotencyKey
        }
        return params.takeIf { it.isNotEmpty() }
    }
}
