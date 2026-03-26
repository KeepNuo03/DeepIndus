package com.induscore.mobile.config

object AppConfig {
    // For emulator, use 10.0.2.2 to access host machine localhost.
    const val BASE_URL = "http://10.0.2.2:8000/"
    const val CLIENT_TYPE = "android"

    // Upload queue network policy.
    const val NETWORK_POLICY_WIFI_ONLY = "wifi_only"
    const val NETWORK_POLICY_ANY = "any"
    const val DEFAULT_UPLOAD_NETWORK_POLICY = NETWORK_POLICY_WIFI_ONLY

    // Cellular sync safety thresholds.
    const val CELLULAR_SYNC_TASK_THRESHOLD = 3
    const val CELLULAR_SYNC_TOTAL_BYTES_THRESHOLD = 20L * 1024L * 1024L // 20MB

    // Local YOLO inference (Phase-1 scaffold).
    const val LOCAL_YOLO_ENABLED = true
    const val LOCAL_YOLO_MODEL_ASSET = "models/yolo11n_induscore.tflite"
    const val LOCAL_YOLO_LABELS_ASSET = "models/labels.txt"
    const val LOCAL_YOLO_INPUT_SIZE = 640
    const val LOCAL_YOLO_CONF_THRESHOLD = 0.35f
    const val LOCAL_YOLO_IOU_THRESHOLD = 0.45f
    const val LOCAL_YOLO_MAX_RESULTS = 20
}
