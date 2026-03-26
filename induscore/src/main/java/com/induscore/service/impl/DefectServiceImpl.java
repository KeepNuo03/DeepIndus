package com.induscore.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.common.ApiException;
import com.induscore.dto.DefectReviewRequest;
import com.induscore.model.DetectionRecord;
import com.induscore.model.ProcessRecord;
import com.induscore.repository.DetectionRecordRepository;
import com.induscore.repository.ProcessRecordRepository;
import com.induscore.service.DefectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 缺陷详情业务实现。
 */
@Service
public class DefectServiceImpl implements DefectService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DetectionRecordRepository recordRepository;
    private final ProcessRecordRepository processRecordRepository;
    private final ObjectMapper objectMapper;
    private final String exportPath;

    public DefectServiceImpl(
            DetectionRecordRepository recordRepository,
            ProcessRecordRepository processRecordRepository,
            ObjectMapper objectMapper,
            @Value("${file.export.path}") String exportPath
    ) {
        this.recordRepository = recordRepository;
        this.processRecordRepository = processRecordRepository;
        this.objectMapper = objectMapper;
        this.exportPath = exportPath;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDefectDetail(String id) {
        if (id == null || id.isBlank()) {
            throw new ApiException(400, "缺陷记录ID不能为空");
        }

        String normalizedId = id.trim();
        DetectionRecord record = resolveRecordByMixedId(normalizedId)
                .orElseThrow(() -> new ApiException(404, "缺陷记录不存在"));

        // 强制初始化懒加载对象
        if (record.getProduct() != null) {
            record.getProduct().getName(); // 触发加载
        }
        if (record.getProductionLineEntity() != null) {
            record.getProductionLineEntity().getName(); // 触发加载
        }

        // 组装详情数据
        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        data.put("detectionNo", record.getDetectionNo());
        data.put("serialNo", record.getSerialNo());
        data.put("type", record.getDefect());
        data.put("confidence", formatPercent(record.getConfidence()));
        data.put("severity", mapSeverity(record.getSeverity()));
        data.put("positionX", record.getPositionX());
        data.put("positionY", record.getPositionY());
        data.put("area", record.getArea());
        data.put("impactLevel", record.getImpactLevel());
        data.put("shift", record.getShift());
        data.put("timestamp", record.getTimestamp() == null ? null : record.getTimestamp().format(DATE_TIME));
        data.put("model", record.getModelName());
        data.put("status", record.getProcessStatus());
        data.put("statusNote", record.getStatusNote());

        // 产品信息（新增）
        if (record.getProduct() != null) {
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("id", record.getProduct().getId());
            productInfo.put("name", record.getProduct().getName());
            productInfo.put("model", record.getProduct().getModel());
            productInfo.put("category", record.getProduct().getCategory());
            data.put("product", productInfo);
        } else {
            data.put("product", null);
        }

        // 生产线信息（新增）
        if (record.getProductionLineEntity() != null) {
            Map<String, Object> lineInfo = new HashMap<>();
            lineInfo.put("id", record.getProductionLineEntity().getId());
            lineInfo.put("name", record.getProductionLineEntity().getName());
            lineInfo.put("location", record.getProductionLineEntity().getLocation());
            data.put("productionLine", lineInfo);
        } else {
            // 兼容旧数据，只用字符串
            Map<String, Object> lineInfo = new HashMap<>();
            lineInfo.put("name", record.getProductionLine() != null ? record.getProductionLine() : "未知生产线");
            data.put("productionLine", lineInfo);
        }

        // 详情页统一用「按 id 取图」接口，前端用 apiBase + imageUrl 即可加载（无图时接口 404，前端可占位）
        data.put("imageUrl", "/v1/defect/" + record.getId() + "/image");
        // detections 数据用于在图片上绘制红框，已移除该功能
        data.put("detections", new ArrayList<>());
        data.put("detectionCount", 0);

        // 查询处理记录
        List<Map<String, Object>> processRecords = new ArrayList<>();
        for (ProcessRecord processRecord : processRecordRepository.findByDetectionRecordIdOrderByCreatedAtAsc(record.getId())) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", processRecord.getId());
            item.put("type", processRecord.getType());
            item.put("time", processRecord.getCreatedAt() == null ? null : processRecord.getCreatedAt().format(DATE_TIME));
            item.put("action", processRecord.getAction());
            item.put("operator", processRecord.getOperator());
            if (processRecord.getNote() != null) {
                item.put("note", processRecord.getNote());
            }
            processRecords.add(item);
        }
        data.put("processRecords", processRecords);

        // 相似缺陷
        data.put("similarDefects", buildSimilarDefects(record));
        return data;
    }

    @Override
    public Optional<byte[]> getDefectImageBytes(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        DetectionRecord record = resolveRecordByMixedId(id.trim()).orElse(null);
        if (record == null) {
            return Optional.empty();
        }
        Path file = Paths.get(exportPath).resolve("defects").resolve(record.getId() + ".jpg").normalize();
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(file));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void reviewDefect(Long id, DefectReviewRequest request) {
        // 校验记录是否存在
        DetectionRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "缺陷记录不存在"));

        // 更新主记录的处理状态
        String action = request == null ? null : request.action();
        String note = request == null ? null : request.note();
        record.setProcessStatus("confirm".equalsIgnoreCase(action) ? "已完成" : "处理中");
        record.setStatusNote(note == null ? "人工复核" : note);
        recordRepository.save(record);

        // 写入处理记录流水
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setDetectionRecord(record);
        processRecord.setType("review");
        processRecord.setAction("confirm".equalsIgnoreCase(action) ? "人工确认缺陷" : "人工驳回复核");
        processRecord.setOperator("质检员");
        processRecord.setNote(note);
        processRecord.setCreatedAt(LocalDateTime.now());
        processRecordRepository.save(processRecord);
    }

    /**
     * 相似缺陷推荐（按缺陷类型取最近记录）。缺陷类型为空时不查相似记录，避免 NPE 与无效查询。
     */
    private List<Map<String, Object>> buildSimilarDefects(DetectionRecord record) {
        if (record.getDefectType() == null || record.getDefectType().isBlank()) {
            return new ArrayList<>();
        }
        List<DetectionRecord> similar = recordRepository.findTop5ByDefectTypeAndIdNotOrderByTimestampDesc(
                record.getDefectType(), record.getId()
        );
        List<Map<String, Object>> result = new ArrayList<>();
        int index = 0;
        for (DetectionRecord item : similar) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("type", item.getDefect());
            map.put("similarity", String.format("%d.%d%%", 90 + index, 2));
            map.put("date", item.getTimestamp() == null ? "" : item.getTimestamp().toLocalDate().toString());
            map.put("thumbnailUrl", item.getImageUrl());
            result.add(map);
            index++;
        }
        return result;
    }

    /**
     * 百分比格式化。
     */
    private String formatPercent(Double value) {
        if (value == null) {
            return "0%";
        }
        return String.format("%.1f%%", value);
    }

    /**
     * 缺陷严重程度映射为中文文案。
     */
    private String mapSeverity(String severity) {
        if (severity == null) {
            return "";
        }
        return switch (severity) {
            case "critical" -> "严重";
            case "warning" -> "中等";
            case "minor" -> "轻微";
            default -> severity;
        };
    }

    /**
     * 兼容 detectionNo 和数字主键 id 两种入参。
     */
    private Optional<DetectionRecord> resolveRecordByMixedId(String id) {
        Optional<DetectionRecord> byDetectionNo = recordRepository.findByDetectionNo(id);
        if (byDetectionNo.isPresent()) {
            return byDetectionNo;
        }
        if (!id.chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }
        return recordRepository.findById(Long.parseLong(id));
    }

    private List<Map<String, Object>> readDetectionsFile(Long recordId) {
        if (recordId == null) {
            return new ArrayList<>();
        }
        Path file = Paths.get(exportPath).resolve("defects").resolve(recordId + ".json").normalize();
        if (!Files.isRegularFile(file)) {
            return new ArrayList<>();
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(
                    file.toFile(),
                    new TypeReference<Map<String, Object>>() {}
            );
            Object value = payload.get("detections");
            if (value instanceof List<?> list) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        Map<String, Object> normalized = new HashMap<>();
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            if (entry.getKey() != null) {
                                normalized.put(String.valueOf(entry.getKey()), entry.getValue());
                            }
                        }
                        result.add(normalized);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
