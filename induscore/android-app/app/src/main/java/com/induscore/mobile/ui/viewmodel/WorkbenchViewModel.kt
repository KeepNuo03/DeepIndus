package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.remote.dto.MobileWorkbenchDto
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkbenchViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository
    private val _state = MutableStateFlow<UiState<MobileWorkbenchDto>>(UiState.Idle)
    val state: StateFlow<UiState<MobileWorkbenchDto>> = _state.asStateFlow()

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.getWorkbench()
            }.onSuccess {
                _state.value = UiState.Success(it)
            }.onFailure {
                _state.value = UiState.Error(it.toUserMessage("工作台加载失败"))
            }
        }
    }
}
