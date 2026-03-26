package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.data.remote.dto.MobileReviewTaskDetailDto
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewTaskDetailViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository

    private val _detailState = MutableStateFlow<UiState<MobileReviewTaskDetailDto>>(UiState.Idle)
    val detailState: StateFlow<UiState<MobileReviewTaskDetailDto>> = _detailState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun load(taskId: Long) {
        _detailState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.getReviewTaskDetail(taskId)
            }.onSuccess {
                _detailState.value = UiState.Success(it)
            }.onFailure {
                _detailState.value = UiState.Error(it.toUserMessage("复核详情加载失败"))
            }
        }
    }

    fun submit(taskId: Long, decision: String, comment: String) {
        _submitState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.submitTaskReview(taskId, decision, comment)
            }.onSuccess {
                _submitState.value = UiState.Success(Unit)
            }.onFailure {
                _submitState.value = UiState.Error(it.toUserMessage("复核提交失败"))
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = UiState.Idle
    }
}
