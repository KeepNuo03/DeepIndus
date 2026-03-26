package com.induscore.service.impl;

import com.induscore.dto.RecordsBatchDeleteRequest;
import com.induscore.dto.RecordsExportRequest;
import com.induscore.model.DetectionRecord;
import com.induscore.repository.DetectionRecordRepository;
import com.induscore.service.RecordsService;
import com.induscore.util.DefectTypeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 检测记录业务实现。
 */
@Service
public class RecordsServiceImpl implements RecordsService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PersistenceContext
    private EntityManager entityManager;

    private final DetectionRecordRepository recordRepository;
    private final String exportPath;
    private final String exportBaseUrl;

    public RecordsServiceImpl(
            DetectionRecordRepository recordRepository,
            @Value("${file.export.path}") String exportPath,
            @Value("${file.export.base-url}") String exportBaseUrl
    ) {
        this.recordRepository = recordRepository;
        this.exportPath = exportPath;
        this.exportBaseUrl = exportBaseUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> queryRecords(
            int page,
            int pageSize,
            String dateStart,
            String dateEnd,
            String defectType,
            String status,
            String search
    ) {
        // 动态拼装查询条件
        Specification<DetectionRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dateStart != null && !dateStart.isBlank()) {
                LocalDate start = LocalDate.parse(dateStart.trim());
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), start.atStartOfDay()));
            }
            if (dateEnd != null && !dateEnd.isBlank()) {
                LocalDate end = LocalDate.parse(dateEnd.trim());
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), end.atTime(LocalTime.MAX)));
            }
            if (defectType != null && !"all".equalsIgnoreCase(defectType)) {
                String normalized = DefectTypeMapper.normalize(defectType);
                if (normalized != null && !normalized.isBlank()) {
                    predicates.add(cb.equal(root.get("defectType"), normalized));
                }
            }
            if (status != null && !"all".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("serialNo"), like),
                        cb.like(root.get("detectionNo"), like)
                ));
            }
            // 无任何筛选条件时返回 null，表示不添加 WHERE，否则 cb.and(空数组) 可能导致查不到数据
            if (predicates.isEmpty()) {
                return null;
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 分页 + 时间倒序，使用 JOIN FETCH 预加载产品和生产线
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<DetectionRecord> result;
        if (spec == null) {
            // 无条件时使用 JOIN FETCH 查询
            result = recordRepository.findAllWithProductAndLine(pageable);
        } else {
            // 有条件时使用普通查询（懒加载由 @Transactional 处理）
            result = recordRepository.findAll(spec, pageable);
        }

        // 预加载所有关联的产品和生产线信息（避免懒加载问题）
        List<DetectionRecord> recordList = result.getContent();

        // 提取所有 productId 和 lineId
        List<Long> productIds = recordList.stream()
                .map(DetectionRecord::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        // 直接查询数据库获取关联数据（通过原生SQL或JPQL）
        List<Object[]> productData = entityManager.createQuery(
                        "SELECT dr.id, p.id, p.name, p.model FROM DetectionRecord dr LEFT JOIN dr.product p WHERE dr.id IN :ids",
                        Object[].class)
                .setParameter("ids", productIds)
                .getResultList();

        List<Object[]> lineData = entityManager.createQuery(
                        "SELECT dr.id, pl.id, pl.name FROM DetectionRecord dr LEFT JOIN dr.productionLineEntity pl WHERE dr.id IN :ids",
                        Object[].class)
                .setParameter("ids", productIds)
                .getResultList();

        // 转换为 Map 便于查找
        Map<Long, ProductInfo> productMap = new HashMap<>();
        for (Object[] row : productData) {
            Long recordId = (Long) row[0];
            Long pId = (Long) row[1];
            String pName = (String) row[2];
            String pModel = (String) row[3];
            productMap.put(recordId, new ProductInfo(pId, pName, pModel));
        }

        Map<Long, LineInfo> lineMap = new HashMap<>();
        for (Object[] row : lineData) {
            Long recordId = (Long) row[0];
            Long lId = (Long) row[1];
            String lName = (String) row[2];
            lineMap.put(recordId, new LineInfo(lId, lName));
        }

        // 转换为前端需要的字段结构
        List<Map<String, Object>> records = new ArrayList<>();
        for (DetectionRecord record : recordList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("detectionNo", record.getDetectionNo());
            item.put("serialNo", record.getSerialNo());
            item.put("defect", record.getDefect());
            item.put("severity", record.getSeverity());
            item.put("confidence", formatPercent(record.getConfidence()));
            item.put("timestamp", record.getTimestamp().format(DATE_TIME));
            item.put("status", record.getStatus());
            item.put("selected", false);

            // 从预加载的 Map 中获取产品信息
            ProductInfo pInfo = productMap.get(record.getId());
            if (pInfo != null && pInfo.id != null) {
                item.put("productId", pInfo.id);
                item.put("productName", pInfo.name);
                item.put("productModel", pInfo.model);
            } else {
                item.put("productId", null);
                item.put("productName", "未知产品");
                item.put("productModel", "");
            }

            // 从预加载的 Map 中获取生产线信息
            LineInfo lInfo = lineMap.get(record.getId());
            if (lInfo != null && lInfo.id != null) {
                item.put("productionLineId", lInfo.id);
                item.put("productionLineName", lInfo.name);
            } else {
                item.put("productionLineId", null);
                item.put("productionLineName", record.getProductionLine() != null ? record.getProductionLine() : "未知生产线");
            }
            records.add(item);
        }

        // 清理 EntityManager 缓存
        entityManager.clear();

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotalElements());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    @Override
    public Map<String, Object> exportRecords(RecordsExportRequest request) {
        // 生成 CSV 导出文件并返回下载地址
        List<DetectionRecord> records = fetchRecordsForExport(request);
        String filename = "records_" + System.currentTimeMillis() + ".csv";
        Path filePath = buildExportPath(filename);
        writeCsv(filePath, records);

        Map<String, Object> data = new HashMap<>();
        data.put("downloadUrl", exportBaseUrl + filename);
        return data;
    }

    @Override
    public void deleteRecords(RecordsBatchDeleteRequest request) {
        // 空集合直接忽略
        if (request == null || request.recordIds() == null || request.recordIds().isEmpty()) {
            return;
        }
        recordRepository.deleteAllById(request.recordIds());
    }

    /**
     * 构建导出文件路径并确保目录存在。
     */
    private Path buildExportPath(String filename) {
        try {
            Path dir = Paths.get(exportPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            return dir.resolve(filename);
        } catch (IOException ex) {
            throw new IllegalStateException("导出目录创建失败");
        }
    }

    /**
     * 拉取导出记录集合。
     */
    private List<DetectionRecord> fetchRecordsForExport(RecordsExportRequest request) {
        if (request == null || request.recordIds() == null || request.recordIds().isEmpty()) {
            return recordRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        }
        return recordRepository.findAllById(request.recordIds());
    }

    /**
     * 写出 CSV 文件。
     */
    private void writeCsv(Path filePath, List<DetectionRecord> records) {
        StringBuilder builder = new StringBuilder();
        builder.append("id,detectionNo,serialNo,defect,severity,confidence,timestamp,status\n");
        for (DetectionRecord record : records) {
            builder.append(record.getId()).append(',')
                    .append(escape(record.getDetectionNo())).append(',')
                    .append(escape(record.getSerialNo())).append(',')
                    .append(escape(record.getDefect())).append(',')
                    .append(escape(record.getSeverity())).append(',')
                    .append(formatPercent(record.getConfidence())).append(',')
                    .append(record.getTimestamp() == null ? "" : record.getTimestamp().format(DATE_TIME)).append(',')
                    .append(escape(record.getStatus()))
                    .append('\n');
        }
        try {
            Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("导出文件写入失败");
        }
    }

    /**
     * CSV 字段转义。
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String safe = value.replace("\"", "\"\"");
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return "\"" + safe + "\"";
        }
        return safe;
    }

    /**
     * 将数值转为百分比字符串，保留一位小数。
     */
    private String formatPercent(Double value) {
        if (value == null) {
            return "0%";
        }
        return String.format("%.1f%%", value);
    }

    /**
     * 临时存储产品信息的内部类
     */
    private static class ProductInfo {
        Long id;
        String name;
        String model;
        ProductInfo(Long id, String name, String model) {
            this.id = id;
            this.name = name;
            this.model = model;
        }
    }

    /**
     * 临时存储生产线信息的内部类
     */
    private static class LineInfo {
        Long id;
        String name;
        LineInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
