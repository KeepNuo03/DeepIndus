package com.induscore.mobile.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class TfliteLocalYoloEngine(
    context: Context,
    private val modelAssetPath: String,
    private val labelsAssetPath: String,
    private val inputSize: Int,
    private val confThreshold: Float,
    private val iouThreshold: Float,
    private val maxResults: Int
) : LocalYoloEngine {

    private val appContext = context.applicationContext
    @Volatile
    private var interpreter: Interpreter? = null
    @Volatile
    private var labels: List<String> = emptyList()

    override suspend fun warmup() {
        withContext(Dispatchers.Default) {
            ensureInterpreter()
        }
    }

    override suspend fun infer(imagePath: String): LocalInferenceResult = withContext(Dispatchers.Default) {
        val startedAt = System.currentTimeMillis()
        val tflite = ensureInterpreter()
        val outputTensor = tflite.getOutputTensor(0)
        val shape = outputTensor.shape()
        require(shape.size == 3 && shape[0] == 1) {
            "暂不支持的输出维度: ${shape.contentToString()}"
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)
            ?: throw IllegalArgumentException("图片读取失败: $imagePath")
        val input = bitmap.toModelInput(inputSize)
        val out = Array(1) { Array(shape[1]) { FloatArray(shape[2]) } }

        tflite.run(input, out)
        val detections = decodeDetections(out[0], shape[1], shape[2])
        LocalInferenceResult(
            modelSource = "tflite(local)",
            latencyMs = System.currentTimeMillis() - startedAt,
            detections = detections,
            warning = null
        )
    }

    override fun isReady(): Boolean = interpreter != null

    private fun ensureInterpreter(): Interpreter {
        interpreter?.let { return it }
        synchronized(this) {
            interpreter?.let { return it }
            val options = Interpreter.Options().apply {
                setNumThreads(max(1, Runtime.getRuntime().availableProcessors() / 2))
            }
            val modelBuffer = loadModelBuffer(modelAssetPath)
            labels = loadLabels(labelsAssetPath)
            val created = Interpreter(modelBuffer, options)
            interpreter = created
            return created
        }
    }

    private fun loadModelBuffer(assetPath: String): ByteBuffer {
        val descriptor = appContext.assets.openFd(assetPath)
        FileInputStream(descriptor.fileDescriptor).use { input ->
            val channel = input.channel
            return channel.map(
                FileChannel.MapMode.READ_ONLY,
                descriptor.startOffset,
                descriptor.declaredLength
            )
        }
    }

    private fun loadLabels(assetPath: String): List<String> {
        return runCatching {
            appContext.assets.open(assetPath).bufferedReader().use { reader ->
                reader.readLines().map { it.trim() }.filter { it.isNotBlank() }
            }
        }.getOrElse { emptyList() }
    }

    private fun Bitmap.toModelInput(size: Int): ByteBuffer {
        val scaled = if (width == size && height == size) this else Bitmap.createScaledBitmap(this, size, size, true)
        val input = ByteBuffer.allocateDirect(1 * size * size * 3 * 4).order(ByteOrder.nativeOrder())
        val pixels = IntArray(size * size)
        scaled.getPixels(pixels, 0, size, 0, 0, size, size)
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            input.putFloat(r)
            input.putFloat(g)
            input.putFloat(b)
        }
        input.rewind()
        if (scaled !== this) {
            scaled.recycle()
        }
        return input
    }

    private fun decodeDetections(
        output: Array<FloatArray>,
        dim1: Int,
        dim2: Int
    ): List<LocalDetection> {
        val featureMajor = dim1 <= dim2
        val featureSize = if (featureMajor) dim1 else dim2
        val boxCount = if (featureMajor) dim2 else dim1
        if (featureSize < 5) return emptyList()

        val hasObjectness = when {
            labels.isNotEmpty() && featureSize - 5 == labels.size -> true
            labels.isNotEmpty() && featureSize - 4 == labels.size -> false
            else -> false
        }
        val clsStart = if (hasObjectness) 5 else 4

        val raw = mutableListOf<LocalDetection>()
        val feature = FloatArray(featureSize)
        for (i in 0 until boxCount) {
            for (k in 0 until featureSize) {
                feature[k] = if (featureMajor) output[k][i] else output[i][k]
            }
            if (clsStart >= featureSize) continue

            var bestClass = -1
            var bestClassScore = 0f
            for (cls in clsStart until featureSize) {
                val score = feature[cls]
                if (score > bestClassScore) {
                    bestClassScore = score
                    bestClass = cls - clsStart
                }
            }
            if (bestClass < 0) continue

            val confidence = if (hasObjectness) {
                feature[4].coerceAtLeast(0f) * bestClassScore
            } else {
                bestClassScore
            }
            if (confidence < confThreshold) continue

            val cx = normalizeCoord(feature[0], inputSize)
            val cy = normalizeCoord(feature[1], inputSize)
            val w = normalizeCoord(feature[2], inputSize)
            val h = normalizeCoord(feature[3], inputSize)
            val left = (cx - w / 2f).coerceIn(0f, 1f)
            val top = (cy - h / 2f).coerceIn(0f, 1f)
            val right = (cx + w / 2f).coerceIn(0f, 1f)
            val bottom = (cy + h / 2f).coerceIn(0f, 1f)
            if (right <= left || bottom <= top) continue

            raw += LocalDetection(
                label = labels.getOrNull(bestClass) ?: "class_$bestClass",
                confidence = confidence,
                left = left,
                top = top,
                right = right,
                bottom = bottom
            )
        }
        return nms(raw, iouThreshold).take(maxResults)
    }

    private fun normalizeCoord(value: Float, size: Int): Float {
        return if (value > 1f) (value / size).coerceIn(0f, 1f) else value.coerceIn(0f, 1f)
    }

    private fun nms(detections: List<LocalDetection>, iouThreshold: Float): List<LocalDetection> {
        if (detections.isEmpty()) return detections
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val kept = mutableListOf<LocalDetection>()
        while (sorted.isNotEmpty()) {
            val current = sorted.removeAt(0)
            kept += current
            sorted.removeAll { other -> iou(current, other) > iouThreshold }
        }
        return kept
    }

    private fun iou(a: LocalDetection, b: LocalDetection): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)
        if (interRight <= interLeft || interBottom <= interTop) return 0f

        val interArea = (interRight - interLeft) * (interBottom - interTop)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - interArea
        if (union <= 0f) return 0f
        return interArea / union
    }
}
