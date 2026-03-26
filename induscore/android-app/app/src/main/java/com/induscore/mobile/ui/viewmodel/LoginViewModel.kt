package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = ServiceLocator.authRepository
    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state.asStateFlow()

    fun login(username: String, password: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                authRepository.login(username.trim(), password)
            }.onSuccess {
                _state.value = UiState.Success(Unit)
            }.onFailure {
                _state.value = UiState.Error(it.toUserMessage("登录失败"))
            }
        }
    }
}
