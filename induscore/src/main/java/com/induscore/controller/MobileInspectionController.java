package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.DefectReviewRequest;
import com.induscore.dto.MobileUploadStatusResponse;
import com.induscore.dto.MobileWorkbenchResponse;
import com.induscore.service.MobileInspectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 安卓端巡检/抽检/复核接口。
 */
@RestController
@RequestMapping("/v1/mobile")
public class MobileInspectionController {
    private final MobileInspectionService mobileInspectionService;

    public MobileInspectionController(MobileInspectionService mobileInspectionService) {
        this.mobileInspectionService = mobileInspectionService;
    }

    @GetMapping("/workbench")
    public ApiResponse<MobileWorkbenchResponse> getWorkbench() {
        return ApiResponse.success(mobileInspectionService.getWorkbench());
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile() {
        return ApiResponse.success(mobileInspectionService.getProfile());
    }

    @GetMapping("/tasks/review")
    public ApiResponse<Map<String, Object>> listReviewTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "all") String status
    ) {
        return ApiResponse.success(mobileInspectionService.listReviewTasks(page, pageSize, status));
    }

    @GetMapping("/tasks/review/{id}")
    public ApiResponse<Map<String, Object>> getReviewTaskDetail(@PathVariable Long id) {
        return ApiResponse.success(mobileInspectionService.getReviewTaskDetail(id));
    }

    @PostMapping("/tasks/{id}/review")
    public ApiResponse<Map<String, Object>> reviewTask(
            @PathVariable Long id,
            @RequestBody DefectReviewRequest request
    ) {
        return ApiResponse.success("复核提交成功", mobileInspectionService.reviewTask(id, request));
    }

    @PostMapping("/uploads/sampling")
    public ApiResponse<Map<String, Object>> uploadSampling(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String serialNo,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long productionLineId,
            @RequestParam(required = false) String networkState,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ApiResponse.success("上传成功", mobileInspectionService.submitSamplingUpload(
                file, serialNo, productId, productionLineId, idempotencyKey, networkState
        ));
    }

    @GetMapping("/uploads/{idempotencyKey}")
    public ApiResponse<MobileUploadStatusResponse> getUploadStatus(@PathVariable String idempotencyKey) {
        return ApiResponse.success(mobileInspectionService.getUploadStatus(idempotencyKey));
    }

    @GetMapping("/notifications")
    public ApiResponse<List<Map<String, Object>>> listNotifications(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.success(mobileInspectionService.listNotifications(limit));
    }

    @GetMapping("/pilot/metrics")
    public ApiResponse<Map<String, Object>> getPilotMetrics() {
        return ApiResponse.success(mobileInspectionService.getPilotMetrics());
    }
}
