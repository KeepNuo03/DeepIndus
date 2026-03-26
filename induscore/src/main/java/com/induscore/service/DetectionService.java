package com.induscore.service;

import com.induscore.dto.DetectionControlRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 实时检测业务接口。
 */
public interface DetectionService {
    /**
     * 获取实时检测统计。
     */
    Map<String, Object> getRealtimeStatistics();

    /**
     * 获取实时检测记录列表。
     */
    Map<String, Object> getRealtimeRecords(int limit, int offset);

    /**
     * 获取检测趋势数据。
     */
    Map<String, Object> getRealtimeTrend();

    /**
     * 启动/停止检测（占位逻辑）。
     */
    Map<String, Object> controlDetection(DetectionControlRequest request);

    /**
     * 上传图片并调用 YOLO 检测。
     */
    Map<String, Object> uploadAndDetect(MultipartFile file, String serialNo);

    /**
     * 上传图片并调用 YOLO 检测（指定产品和生产线）。
     */
    Map<String, Object> uploadAndDetect(MultipartFile file, String serialNo, Long productId, Long productionLineId);

    /**
     * 处理实时检测的一帧（本机摄像头抓帧上传）：YOLO 推理、落库、WebSocket 推送，并返回检测结果与标注图供前端绘制。
     */
    Map<String, Object> processRealtimeFrame(MultipartFile file, String cameraId);

    /**
     * 处理实时检测的一帧（指定产品和生产线）。
     */
    Map<String, Object> processRealtimeFrame(MultipartFile file, String cameraId, Long productId, Long productionLineId);
}
