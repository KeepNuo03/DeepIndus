package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.remote.dto.MobileReviewTaskDto
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewTaskListViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository
    private val _state = MutableStateFlow<UiState<List<MobileReviewTaskDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<MobileReviewTaskDto>>> = _state.asStateFlow()

    fun load(status: String = "all") {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.getReviewTasks(page = 1, pageSize = 20, status = status)
            }.onSuccess {
                _state.value = UiState.Success(it)
            }.onFailure {
                _state.value = UiState.Error(it.toUserMessage("任务加载失败"))
            }
        }
    }
}
