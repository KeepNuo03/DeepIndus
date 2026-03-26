package com.induscore.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * YOLO 推理服务接口。
 */
public interface YoloService {
    /**
     * 检测单张图片。
     */
    Map<String, Object> detectImage(MultipartFile file);

    /**
     * 检测单张图片（使用指定模型）。
     *
     * @param file 图片文件
     * @param modelName 模型名称（如 YOLOv11-Industrial）
     * @param modelVersion 模型版本（如 v1.0.0）
     * @param modelFile 模型文件路径
     * @return 检测结果
     */
    Map<String, Object> detectImage(MultipartFile file, String modelName, String modelVersion, String modelFile);

    /**
     * 检测视频帧（用于实时检测）。
     */
    Map<String, Object> detectVideoFrame(byte[] frameData);

    /**
     * 检测视频帧（用于实时检测，使用指定模型）。
     *
     * @param frameData 帧数据
     * @param modelName 模型名称
     * @param modelVersion 模型版本
     * @param modelFile 模型文件路径
     * @return 检测结果
     */
    Map<String, Object> detectVideoFrame(byte[] frameData, String modelName, String modelVersion, String modelFile);

    /**
     * 获取当前YOLO服务状态。
     */
    Map<String, Object> getServiceStatus();
}
