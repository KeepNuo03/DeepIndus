package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.remote.dto.MobileNotificationDto
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository
    private val _state = MutableStateFlow<UiState<List<MobileNotificationDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<MobileNotificationDto>>> = _state.asStateFlow()

    fun load(limit: Int = 10) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.getNotifications(limit)
            }.onSuccess {
                _state.value = UiState.Success(it)
            }.onFailure {
                _state.value = UiState.Error(it.toUserMessage("通知加载失败"))
            }
        }
    }
}
