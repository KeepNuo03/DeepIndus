package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.DefectReviewRequest;
import com.induscore.dto.MobileReviewTaskResponse;
import com.induscore.dto.MobileUploadStatusResponse;
import com.induscore.dto.MobileWorkbenchResponse;
import com.induscore.model.DetectionRecord;
import com.induscore.model.User;
import com.induscore.repository.DetectionRecordRepository;
import com.induscore.repository.UserRepository;
import com.induscore.security.RequestAuthContext;
import com.induscore.security.RequestAuthContextHolder;
import com.induscore.service.DefectService;
import com.induscore.service.DetectionService;
import com.induscore.service.MobileInspectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安卓端巡检/抽检/复核业务实现。
 */
@Service
public class MobileInspectionServiceImpl implements MobileInspectionService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DetectionRecordRepository detectionRecordRepository;
    private final DetectionService detectionService;
    private final DefectService defectService;
    private final UserRepository userRepository;
    private final long uploadStateTtlMillis;
    private final String pilotLineName;
    private final double targetCloseLoopRate;
    private final double targetUploadRecoveryRate;
    private final int targetApiP95LatencyMs;

    private final Map<String, UploadSession> uploadSessions = new ConcurrentHashMap<>();

    public MobileInspectionServiceImpl(
            DetectionRecordRepository detectionRecordRepository,
            DetectionService detectionService,
            DefectService defectService,
            UserRepository userRepository,
            @Value("${mobile.upload.state-ttl-minutes:1440}") long uploadStateTtlMinutes,
            @Value("${mobile.pilot.line-name:A-01}") String pilotLineName,
            @Value("${mobile.pilot.target.close-loop-rate:95}") double targetCloseLoopRate,
            @Value("${mobile.pilot.target.upload-recovery-rate:99}") double targetUploadRecoveryRate,
            @Value("${mobile.pilot.target.api-p95-latency-ms:500}") int targetApiP95LatencyMs
    ) {
        this.detectionRecordRepository = detectionRecordRepository;
        this.detectionService = detectionService;
        this.defectService = defectService;
        this.userRepository = userRepository;
        this.uploadStateTtlMillis = Math.max(uploadStateTtlMinutes, 10) * 60_000L;
        this.pilotLineName = pilotLineName;
        this.targetCloseLoopRate = targetCloseLoopRate;
        this.targetUploadRecoveryRate = targetUploadRecoveryRate;
        this.targetApiP95LatencyMs = targetApiP95LatencyMs;
    }

    @Override
    @Transactional(readOnly = true)
    public MobileWorkbenchResponse getWorkbench() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        long todaySamplingCount = detectionRecordRepository.countBetween(start, end);
        long pendingReviewCount = detectionRecordRepository.count(buildReviewStatusSpec("todo"));
        long processingCount = detectionRecordRepository.count(buildReviewStatusSpec("processing"));
        long severeAlertCount = detectionRecordRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("severity"), "critical"),
                cb.greaterThanOrEqualTo(root.get("timestamp"), start),
                cb.lessThanOrEqualTo(root.get("timestamp"), end)
        ));
        long processingUploads = uploadSessions.values().stream()
                .filter(session -> "processing".equals(session.status))
                .count();
        String queueStatus = processingUploads > 0 ? ("processing:" + processingUploads) : "idle";
        return new MobileWorkbenchResponse(
                pendingReviewCount,
                processingCount,
                todaySamplingCount,
                severeAlertCount,
                queueStatus
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProfile() {
        RequestAuthContext context = currentContext();
        User user = userRepository.findById(context.userId())
                .orElseThrow(() -> new ApiException(401, "用户不存在或已失效"));
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("username", user.getUsername());
        profile.put("name", user.getName());
        profile.put("departmentId", user.getDepartmentId());
        profile.put("position", user.getPosition());
        profile.put("roles", user.getRoles().stream().map(r -> r.getCode()).toList());
        profile.put("clientType", context.clientType());
        return profile;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listReviewTasks(int page, int pageSize, String status) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);
        PageRequest pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<DetectionRecord> result = detectionRecordRepository.findAll(buildReviewStatusSpec(status), pageable);
        List<MobileReviewTaskResponse> tasks = result.getContent().stream()
                .map(this::toReviewTask)
                .toList();
        Map<String, Object> data = new HashMap<>();
        data.put("items", tasks);
        data.put("total", result.getTotalElements());
        data.put("page", safePage);
        data.put("pageSize", safePageSize);
        data.put("status", status == null ? "all" : status);
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewTaskDetail(Long id) {
        if (id == null) {
            throw new ApiException(400, "任务ID不能为空");
        }
        return defectService.getDefectDetail(String.valueOf(id));
    }

    @Override
    public Map<String, Object> submitSamplingUpload(
            MultipartFile file,
            String serialNo,
            Long productId,
            Long productionLineId,
            String idempotencyKey,
            String networkState
    ) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "抽检图片不能为空");
        }
        String key = normalizeIdempotencyKey(idempotencyKey);
        UploadSession existing = uploadSessions.get(key);
        if (existing != null) {
            if ("completed".equals(existing.status)) {
                Map<String, Object> deduplicated = new HashMap<>();
                deduplicated.put("idempotencyKey", key);
                deduplicated.put("deduplicated", true);
                deduplicated.put("uploadStatus", toUploadStatus(existing, true));
                deduplicated.put("result", existing.cachedResult);
                return deduplicated;
            }
            if ("processing".equals(existing.status)) {
                Map<String, Object> processing = new HashMap<>();
                processing.put("idempotencyKey", key);
                processing.put("deduplicated", true);
                processing.put("uploadStatus", toUploadStatus(existing, true));
                return processing;
            }
        }

        UploadSession session = new UploadSession();
        session.idempotencyKey = key;
        session.status = "processing";
        session.networkState = normalizeNetworkState(networkState);
        if (existing != null) {
            session.retryCount = existing.retryCount + 1;
        }
        session.updatedAt = System.currentTimeMillis();
        uploadSessions.put(key, session);

        try {
            Map<String, Object> result = detectionService.uploadAndDetect(file, serialNo, productId, productionLineId);
            session.status = "completed";
            session.cachedResult = result;
            session.recordId = extractRecordId(result);
            session.message = "上传并检测成功";
            session.updatedAt = System.currentTimeMillis();

            Map<String, Object> payload = new HashMap<>();
            payload.put("idempotencyKey", key);
            payload.put("deduplicated", false);
            payload.put("uploadStatus", toUploadStatus(session, false));
            payload.put("result", result);
            return payload;
        } catch (RuntimeException ex) {
            session.status = "failed";
            session.message = ex.getMessage();
            session.updatedAt = System.currentTimeMillis();
            throw ex;
        }
    }

    @Override
    public MobileUploadStatusResponse getUploadStatus(String idempotencyKey) {
        String key = normalizeIdempotencyKey(idempotencyKey);
        UploadSession session = uploadSessions.get(key);
        if (session == null) {
            throw new ApiException(404, "上传任务不存在或已过期");
        }
        return toUploadStatus(session, false);
    }

    @Override
    public Map<String, Object> reviewTask(Long id, DefectReviewRequest request) {
        if (id == null) {
            throw new ApiException(400, "任务ID不能为空");
        }
        defectService.reviewDefect(id, request);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", id);
        data.put("status", "done");
        data.put("action", request == null ? null : request.action());
        data.put("operator", currentContext().username());
        data.put("updatedAt", LocalDateTime.now().format(DATE_TIME));
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listNotifications(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<DetectionRecord> records = detectionRecordRepository.findTop10BySeverityOrderByTimestampDesc("critical");
        return records.stream()
                .limit(safeLimit)
                .map(record -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", record.getId());
                    item.put("title", record.getDefect() == null ? "异常告警" : record.getDefect());
                    item.put("severity", record.getSeverity());
                    item.put("timestamp", record.getTimestamp() == null ? null : record.getTimestamp().format(DATE_TIME));
                    item.put("taskStatus", record.getProcessStatus());
                    item.put("detectionNo", record.getDetectionNo());
                    return item;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPilotMetrics() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        long completedReview = detectionRecordRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), "fail"),
                cb.equal(root.get("processStatus"), "已完成"),
                cb.greaterThanOrEqualTo(root.get("timestamp"), start),
                cb.lessThanOrEqualTo(root.get("timestamp"), end)
        ));
        long totalReview = detectionRecordRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), "fail"),
                cb.greaterThanOrEqualTo(root.get("timestamp"), start),
                cb.lessThanOrEqualTo(root.get("timestamp"), end)
        ));
        long completedUploads = uploadSessions.values().stream()
                .filter(session -> "completed".equals(session.status))
                .count();
        long failedUploads = uploadSessions.values().stream()
                .filter(session -> "failed".equals(session.status))
                .count();
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("pilotLine", pilotLineName);
        metrics.put("targetCloseLoopRate", String.format(Locale.ROOT, "%.2f%%", targetCloseLoopRate));
        metrics.put("targetUploadRecoveryRate", String.format(Locale.ROOT, "%.2f%%", targetUploadRecoveryRate));
        metrics.put("targetApiP95LatencyMs", targetApiP95LatencyMs);
        metrics.put("todayReviewTotal", totalReview);
        metrics.put("todayReviewClosed", completedReview);
        metrics.put("todayReviewCloseLoopRate", percentage(completedReview, totalReview));
        metrics.put("uploadCompleted", completedUploads);
        metrics.put("uploadFailed", failedUploads);
        metrics.put("uploadRecoveryRate", percentage(completedUploads, completedUploads + failedUploads));
        metrics.put("generatedAt", LocalDateTime.now().format(DATE_TIME));
        return metrics;
    }

    @Scheduled(fixedDelayString = "${mobile.upload.cleanup-interval-ms:300000}")
    public void cleanupExpiredUploadSessions() {
        long now = System.currentTimeMillis();
        uploadSessions.entrySet().removeIf(entry -> now - entry.getValue().updatedAt > uploadStateTtlMillis);
    }

    private MobileReviewTaskResponse toReviewTask(DetectionRecord record) {
        return new MobileReviewTaskResponse(
                record.getId(),
                record.getDetectionNo(),
                record.getSerialNo(),
                record.getDefect(),
                record.getSeverity(),
                record.getProcessStatus(),
                record.getTimestamp() == null ? null : record.getTimestamp().format(DATE_TIME),
                "/v1/defect/" + record.getId() + "/image"
        );
    }

    private Specification<DetectionRecord> buildReviewStatusSpec(String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "fail"));
            String normalized = status == null ? "all" : status.trim().toLowerCase(Locale.ROOT);
            switch (normalized) {
                case "todo" -> predicates.add(cb.equal(root.get("processStatus"), "待处理"));
                case "processing" -> predicates.add(cb.equal(root.get("processStatus"), "处理中"));
                case "done" -> predicates.add(cb.equal(root.get("processStatus"), "已完成"));
                default -> {
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private MobileUploadStatusResponse toUploadStatus(UploadSession session, boolean deduplicated) {
        return new MobileUploadStatusResponse(
                session.idempotencyKey,
                session.status,
                deduplicated,
                session.message,
                session.recordId,
                session.retryCount,
                session.updatedAt
        );
    }

    @SuppressWarnings("unchecked")
    private Long extractRecordId(Map<String, Object> result) {
        if (result == null) {
            return null;
        }
        Object dataObj = result.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            return null;
        }
        Object recordObj = data.get("record");
        if (!(recordObj instanceof Map<?, ?> record)) {
            return null;
        }
        Object id = record.get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        if (id == null) {
            return null;
        }
        try {
            return Long.parseLong(id.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return "MOB-" + UUID.randomUUID();
        }
        return idempotencyKey.trim();
    }

    private String normalizeNetworkState(String networkState) {
        if (networkState == null || networkState.isBlank()) {
            return "unknown";
        }
        return networkState.trim().toLowerCase(Locale.ROOT);
    }

    private String percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return "0.00%";
        }
        double value = (double) numerator * 100d / denominator;
        return String.format(Locale.ROOT, "%.2f%%", value);
    }

    private RequestAuthContext currentContext() {
        return RequestAuthContextHolder.get()
                .filter(context -> context.userId() != null)
                .orElseThrow(() -> new ApiException(401, "未认证"));
    }

    private static final class UploadSession {
        private String idempotencyKey;
        private String status;
        private String message;
        private String networkState;
        private Long recordId;
        private int retryCount;
        private long updatedAt = Instant.now().toEpochMilli();
        private Map<String, Object> cachedResult;

        @Override
        public int hashCode() {
            return Objects.hash(idempotencyKey);
        }
    }
}
