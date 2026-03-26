package com.induscore.mobile.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.induscore.mobile.ml.LocalDetection
import com.induscore.mobile.ui.components.BrandPrimaryButtonColor
import com.induscore.mobile.ui.state.UiState
import com.induscore.mobile.ui.viewmodel.SamplingUploadViewModel
import com.induscore.mobile.ui.components.AppPrimaryButton
import com.induscore.mobile.ui.components.AppSecondaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

@Composable
fun SamplingUploadScreen(
    onBackHome: () -> Unit,
    viewModel: SamplingUploadViewModel = viewModel()
) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val localInferenceState by viewModel.localInferenceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var filePath by remember { mutableStateOf("") }
    var serialNo by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }
    var productionLineId by remember { mutableStateOf("") }
    var pendingCapturePath by remember { mutableStateOf<String?>(null) }
    var localHint by remember { mutableStateOf<String?>(null) }
    var showMoreFields by remember { mutableStateOf(false) }
    var isPickingImage by remember { mutableStateOf(false) }
    val uiScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        isPickingImage = false
        uiScope.launch {
            val copiedPath = withContext(Dispatchers.IO) {
                copyUriToCache(context, uri)
            }
            if (!copiedPath.isNullOrBlank()) {
                filePath = copiedPath
                localHint = null
            } else {
                localHint = "相册图片读取失败，请重试"
            }
        }
    }
    val launchImagePicker = {
        if (!isPickingImage) {
            isPickingImage = true
            runCatching {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }.onFailure {
                isPickingImage = false
                localHint = "无法打开图片选择器，请重试"
            }
        }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            filePath = pendingCapturePath ?: filePath
            localHint = null
        } else {
            localHint = "拍照取消或失败"
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture(
                context = context,
                onUriReady = { uri, path ->
                    pendingCapturePath = path
                    takePictureLauncher.launch(uri)
                },
                onFailed = { localHint = it }
            )
        } else {
            localHint = "未授予相机权限，无法拍照"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "抽检上传", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "先拍照或从相册选图，系统会自动入队并按默认 Wi-Fi 策略上传。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = filePath,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("已选择图片") },
                    placeholder = { Text("请点击下方按钮选择图片") },
                    singleLine = true,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = launchImagePicker,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryButtonColor)
                    ) {
                        Text("从相册选择")
                    }
                    Button(
                        onClick = launchImagePicker,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("重新选择")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val permissionState = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionState == PackageManager.PERMISSION_GRANTED) {
                            launchCameraCapture(
                                context = context,
                                onUriReady = { uri, path ->
                                    pendingCapturePath = path
                                    takePictureLauncher.launch(uri)
                                },
                                onFailed = { localHint = it }
                            )
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("拍照采集")
                }
                if (!localHint.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = localHint ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        DetectionPreviewCard(
            filePath = filePath,
            detections = (localInferenceState as? UiState.Success)?.data?.detections.orEmpty()
        )
        Spacer(modifier = Modifier.height(10.dp))
        AppSecondaryButton(
            text = if (localInferenceState is UiState.Loading) "本地预判中..." else "本地AI快速检测",
            onClick = { viewModel.runLocalInference(filePath) }
        )
        when (localInferenceState) {
            UiState.Loading -> {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (localInferenceState as UiState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Success -> {
                val result = (localInferenceState as UiState.Success).data
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("本地预判结果", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "来源: ${result.modelSource} | 耗时: ${result.latencyMs}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!result.warning.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (result.detections.isEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "未检出目标（或当前为骨架模式）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            result.detections.take(3).forEach { det ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${det.label} ${(det.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            UiState.Idle -> Unit
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { showMoreFields = !showMoreFields },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(if (showMoreFields) "收起可选信息" else "填写更多信息（可选）")
        }
        if (showMoreFields) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = serialNo,
                onValueChange = { serialNo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("序列号（可选）") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = productId,
                onValueChange = { productId = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("产品ID（可选）") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = productionLineId,
                onValueChange = { productionLineId = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("产线ID（可选）") },
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        AppPrimaryButton(
            text = if (submitState is UiState.Loading) "入队中..." else "加入上传队列",
            enabled = submitState !is UiState.Loading && filePath.isNotBlank()
        ) {
            viewModel.enqueueUpload(
                filePath = filePath,
                serialNo = serialNo,
                productIdText = productId,
                productionLineIdText = productionLineId
            )
        }

        when (submitState) {
            UiState.Loading -> {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = (submitState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Success -> {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "入队成功，幂等键：${(submitState as UiState.Success).data}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            UiState.Idle -> Unit
        }

        Spacer(modifier = Modifier.height(16.dp))
        AppSecondaryButton(text = "返回系统主页", onClick = onBackHome)
    }
}

@Composable
private fun DetectionPreviewCard(
    filePath: String,
    detections: List<LocalDetection>
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = filePath) {
        value = if (filePath.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                decodePreviewBitmap(filePath, maxSidePx = 1280)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("检测图片预览", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            if (bitmap == null) {
                Text(
                    text = "请先拍照或从相册选择图片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val previewBitmap = bitmap ?: return@Column
                DetectionOverlayImage(
                    bitmap = previewBitmap,
                    detections = detections,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (detections.isEmpty()) "当前未显示检测框（请先执行“本地AI快速检测”）"
                    else "检测框数量：${detections.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetectionOverlayImage(
    bitmap: Bitmap,
    detections: List<LocalDetection>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val labelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 12f
            style = Paint.Style.FILL
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val labelBgPaint = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(180, 26, 115, 232)
            style = Paint.Style.FILL
        }
    }

    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "检测图片预览",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            labelPaint.textSize = with(density) { 12.sp.toPx() }

            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()
            val scale = min(size.width / imageWidth, size.height / imageHeight)
            val drawWidth = imageWidth * scale
            val drawHeight = imageHeight * scale
            val offsetX = (size.width - drawWidth) / 2f
            val offsetY = (size.height - drawHeight) / 2f

            detections.forEach { detection ->
                val left = offsetX + detection.left * drawWidth
                val top = offsetY + detection.top * drawHeight
                val right = offsetX + detection.right * drawWidth
                val bottom = offsetY + detection.bottom * drawHeight
                if (right <= left || bottom <= top) return@forEach

                drawRect(
                    color = Color(0xFF1A73E8),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 3f)
                )

                val label = "${detection.label} ${(detection.confidence * 100).toInt()}%"
                val textWidth = labelPaint.measureText(label)
                val textHeight = labelPaint.textSize
                val bgTop = (top - textHeight - 8f).coerceAtLeast(0f)
                drawContext.canvas.nativeCanvas.apply {
                    drawRect(left, bgTop, left + textWidth + 12f, bgTop + textHeight + 8f, labelBgPaint)
                    drawText(label, left + 6f, bgTop + textHeight + 2f, labelPaint)
                }
            }
        }
    }
}

private fun copyUriToCache(context: Context, uri: Uri?): String? {
    if (uri == null) return null
    return runCatching {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val target = File(context.cacheDir, "sampling_${System.currentTimeMillis()}.jpg")
        input.use { inputStream ->
            target.outputStream().use { output ->
                inputStream.copyTo(output)
            }
        }
        target.absolutePath
    }.getOrNull()
}

private fun decodePreviewBitmap(filePath: String, maxSidePx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(filePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    var width = bounds.outWidth
    var height = bounds.outHeight
    while (width > maxSidePx || height > maxSidePx) {
        sampleSize *= 2
        width /= 2
        height /= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return BitmapFactory.decodeFile(filePath, options)
}

private fun launchCameraCapture(
    context: Context,
    onUriReady: (Uri, String) -> Unit,
    onFailed: (String) -> Unit
) {
    runCatching {
        val target = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            target
        )
        onUriReady(uri, target.absolutePath)
    }.onFailure {
        onFailed("创建拍照文件失败，请重试")
    }
}
