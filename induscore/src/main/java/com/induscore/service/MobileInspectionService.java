package com.induscore.service;

import com.induscore.dto.DefectReviewRequest;
import com.induscore.dto.MobileUploadStatusResponse;
import com.induscore.dto.MobileWorkbenchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 安卓端巡检/抽检/复核业务接口。
 */
public interface MobileInspectionService {
    MobileWorkbenchResponse getWorkbench();

    Map<String, Object> getProfile();

    Map<String, Object> listReviewTasks(int page, int pageSize, String status);

    Map<String, Object> getReviewTaskDetail(Long id);

    Map<String, Object> submitSamplingUpload(
            MultipartFile file,
            String serialNo,
            Long productId,
            Long productionLineId,
            String idempotencyKey,
            String networkState
    );

    MobileUploadStatusResponse getUploadStatus(String idempotencyKey);

    Map<String, Object> reviewTask(Long id, DefectReviewRequest request);

    List<Map<String, Object>> listNotifications(int limit);

    Map<String, Object> getPilotMetrics();
}
