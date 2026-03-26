package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.DetectionControlRequest;
import com.induscore.service.DetectionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 实时检测相关接口（REST）。
 */
@RestController
@RequestMapping("/v1/detection")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    /**
     * 获取实时检测统计。
     */
    @GetMapping("/realtime/statistics")
    public ApiResponse<Map<String, Object>> getRealtimeStatistics() {
        return ApiResponse.success(detectionService.getRealtimeStatistics());
    }

    /**
     * 获取实时检测记录列表。
     */
    @GetMapping("/realtime/records")
    public ApiResponse<Map<String, Object>> getRealtimeRecords(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.success(detectionService.getRealtimeRecords(limit, offset));
    }

    /**
     * 获取实时趋势数据。
     */
    @GetMapping("/realtime/trend")
    public ApiResponse<Map<String, Object>> getRealtimeTrend() {
        return ApiResponse.success(detectionService.getRealtimeTrend());
    }

    /**
     * 控制检测状态（启动/停止）。本机摄像头模式时可用 cameraId="browser" 仅做状态同步，不触发服务端占位推流。
     */
    @PostMapping("/realtime/control")
    public ApiResponse<Map<String, Object>> controlDetection(@RequestBody DetectionControlRequest request) {
        return ApiResponse.success(detectionService.controlDetection(request));
    }

    /**
     * 实时检测单帧：前端从本机摄像头抓取一帧上传，后端 YOLO 推理后返回检测结果与标注图，并落库、WebSocket 推送以更新面板统计。
     */
    @PostMapping("/realtime/frame")
    public ApiResponse<Map<String, Object>> submitRealtimeFrame(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cameraId", required = false) String cameraId
    ) {
        return ApiResponse.success(detectionService.processRealtimeFrame(file, cameraId));
    }

    /**
     * 上传图片并调用 YOLO 检测（便于联调测试）。
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> uploadAndDetect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "serialNo", required = false) String serialNo
    ) {
        return ApiResponse.success("检测完成", detectionService.uploadAndDetect(file, serialNo));
    }

    /**
     * 上传图片并调用 YOLO 检测（指定产品和生产线）。
     */
    @PostMapping("/upload-with-product")
    public ApiResponse<Map<String, Object>> uploadAndDetectWithProduct(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "serialNo", required = false) String serialNo,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productionLineId", required = false) Long productionLineId
    ) {
        return ApiResponse.success("检测完成",
                detectionService.uploadAndDetect(file, serialNo, productId, productionLineId));
    }
}
