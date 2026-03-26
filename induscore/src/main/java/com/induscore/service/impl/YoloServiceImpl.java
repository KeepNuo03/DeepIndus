package com.induscore.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.common.ApiException;
import com.induscore.service.YoloService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * YOLO 推理服务调用实现（HTTP）。
 */
@Service
public class YoloServiceImpl implements YoloService {

    private static final Logger log = LoggerFactory.getLogger(YoloServiceImpl.class);

    private final String yoloServiceUrl;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public YoloServiceImpl(
            @Value("${yolo.service.url}") String yoloServiceUrl,
            @Value("${yolo.service.timeoutMs:30000}") long timeoutMs,
            ObjectMapper objectMapper
    ) {
        this.yoloServiceUrl = yoloServiceUrl;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public Map<String, Object> detectImage(MultipartFile file) {
        return detectImage(file, null, null, null);
    }

    @Override
    public Map<String, Object> detectImage(MultipartFile file, String modelName, String modelVersion, String modelFile) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "file",
                            file.getOriginalFilename(),
                            RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType()))
                    );

            // 添加模型参数（如果提供）
            if (modelName != null && !modelName.isBlank()) {
                builder.addFormDataPart("model_name", modelName);
            }
            if (modelVersion != null && !modelVersion.isBlank()) {
                builder.addFormDataPart("model_version", modelVersion);
            }
            if (modelFile != null && !modelFile.isBlank()) {
                builder.addFormDataPart("model_file", modelFile);
            }

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(yoloServiceUrl + "/detect/image")
                    .post(requestBody)
                    .build();

            return execute(request);
        } catch (IOException ex) {
            log.error("YOLO 图片检测失败: {}", ex.getMessage(), ex);
            throw new ApiException(500, "YOLO 服务调用失败: " + ex.getMessage());
        }
    }

    @Override
    public Map<String, Object> detectVideoFrame(byte[] frameData) {
        return detectVideoFrame(frameData, null, null, null);
    }

    @Override
    public Map<String, Object> detectVideoFrame(byte[] frameData, String modelName, String modelVersion, String modelFile) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        "frame.jpg",
                        RequestBody.create(frameData, MediaType.parse("image/jpeg"))
                );

        // 添加模型参数（如果提供）
        if (modelName != null && !modelName.isBlank()) {
            builder.addFormDataPart("model_name", modelName);
        }
        if (modelVersion != null && !modelVersion.isBlank()) {
            builder.addFormDataPart("model_version", modelVersion);
        }
        if (modelFile != null && !modelFile.isBlank()) {
            builder.addFormDataPart("model_file", modelFile);
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(yoloServiceUrl + "/detect/video-frame")
                .post(requestBody)
                .build();

        return execute(request);
    }

    @Override
    public Map<String, Object> getServiceStatus() {
        try {
            Request request = new Request.Builder()
                    .url(yoloServiceUrl + "/health")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("获取YOLO服务状态失败: {}", e.getMessage());
        }

        // 返回默认状态
        Map<String, Object> status = new HashMap<>();
        status.put("status", "unknown");
        status.put("message", "无法连接到YOLO服务");
        return status;
    }

    /**
     * 执行请求并解析 JSON 响应。
     */
    private Map<String, Object> execute(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String errorMsg = response.body() != null ? response.body().string() : "Unknown error";
                log.error("YOLO 服务响应异常: status={}, body={}", response.code(), errorMsg);
                throw new ApiException(500, "YOLO 服务异常: " + response.code());
            }
            String body = response.body().string();
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (IOException ex) {
            log.error("YOLO 服务调用失败: {}", ex.getMessage(), ex);
            throw new ApiException(500, "YOLO 服务调用失败: " + ex.getMessage());
        }
    }
}
