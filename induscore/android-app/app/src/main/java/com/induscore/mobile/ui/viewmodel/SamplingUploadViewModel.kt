package com.induscore.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.induscore.mobile.config.AppConfig
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ml.LocalInferenceResult
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.state.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SamplingUploadViewModel : ViewModel() {
    private val mobileRepository = ServiceLocator.mobileRepository
    private val localYoloEngine = ServiceLocator.localYoloEngine

    private val _submitState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val submitState: StateFlow<UiState<String>> = _submitState.asStateFlow()

    private val _localInferenceState = MutableStateFlow<UiState<LocalInferenceResult>>(UiState.Idle)
    val localInferenceState: StateFlow<UiState<LocalInferenceResult>> = _localInferenceState.asStateFlow()

    fun enqueueUpload(
        filePath: String,
        serialNo: String,
        productIdText: String,
        productionLineIdText: String
    ) {
        val normalizedPath = filePath.trim()
        if (normalizedPath.isBlank()) {
            _submitState.value = UiState.Error("请先拍照或从相册选择图片")
            return
        }
        if (!File(normalizedPath).exists()) {
            _submitState.value = UiState.Error("本地文件不存在，请检查路径")
            return
        }

        _submitState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                mobileRepository.enqueueUpload(
                    localFilePath = normalizedPath,
                    serialNo = serialNo.trim().ifBlank { null },
                    productId = productIdText.trim().toLongOrNull(),
                    productionLineId = productionLineIdText.trim().toLongOrNull(),
                    networkState = AppConfig.DEFAULT_UPLOAD_NETWORK_POLICY
                )
            }.onSuccess { key ->
                _submitState.value = UiState.Success(key)
            }.onFailure {
                _submitState.value = UiState.Error(it.toUserMessage("入队失败"))
            }
        }
    }

    fun runLocalInference(filePath: String) {
        val normalizedPath = filePath.trim()
        if (normalizedPath.isBlank()) {
            _localInferenceState.value = UiState.Error("请先选择图片后再进行本地预判")
            return
        }
        if (!File(normalizedPath).exists()) {
            _localInferenceState.value = UiState.Error("图片文件不存在，无法进行本地预判")
            return
        }
        _localInferenceState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                localYoloEngine.warmup()
                localYoloEngine.infer(normalizedPath)
            }.onSuccess { result ->
                _localInferenceState.value = UiState.Success(result)
            }.onFailure { t ->
                _localInferenceState.value = UiState.Error(t.toUserMessage("本地预判失败"))
            }
        }
    }
}
