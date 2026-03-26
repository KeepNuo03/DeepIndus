package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.remote.dto.MobilePilotMetricsDto
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetricsViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository
    private val _state = MutableStateFlow<UiState<MobilePilotMetricsDto>>(UiState.Idle)
    val state: StateFlow<UiState<MobilePilotMetricsDto>> = _state.asStateFlow()

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.getPilotMetrics()
            }.onSuccess {
                _state.value = UiState.Success(it)
            }.onFailure {
                _state.value = UiState.Error(it.toUserMessage("试点指标加载失败"))
            }
        }
    }
}
