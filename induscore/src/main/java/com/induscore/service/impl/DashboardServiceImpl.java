package com.induscore.service.impl;

import com.induscore.model.DetectionRecord;
import com.induscore.repository.DetectionRecordRepository;
import com.induscore.repository.ProductionLineRepository;
import com.induscore.service.DashboardService;
import com.induscore.util.DefectTypeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏业务实现。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("HH:mm");

    private final DetectionRecordRepository recordRepository;
    private final ProductionLineRepository productionLineRepository;

    public DashboardServiceImpl(
            DetectionRecordRepository recordRepository,
            ProductionLineRepository productionLineRepository
    ) {
        this.recordRepository = recordRepository;
        this.productionLineRepository = productionLineRepository;
    }

    @Override
    public Map<String, Object> getKpi() {
        // 取当天的数据范围
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        // 汇总统计
        long total = recordRepository.countBetween(start, end);
        long pass = recordRepository.countByStatusBetween("pass", start, end);
        long fail = recordRepository.countByStatusBetween("fail", start, end);

        Map<String, Object> data = new HashMap<>();
        data.put("todayDetectionCount", total);
        data.put("avgDefectRate", total == 0 ? 0 : roundPercent((double) fail / total));
        data.put("yieldRate", total == 0 ? 0 : roundPercent((double) pass / total));
        data.put("utilizationRate", calculateUtilizationRate());
        return data;
    }

    @Override
    public Map<String, Object> getDefectTrend(String startTime, String endTime) {
        // 缺省取近 12 小时
        LocalDateTime start = parseDateTimeOrDefault(startTime, LocalDateTime.now().minusHours(12));
        LocalDateTime end = parseDateTimeOrDefault(endTime, LocalDateTime.now());

        List<String> labels = new ArrayList<>();
        List<Long> defectCounts = new ArrayList<>();
        List<Double> yieldRates = new ArrayList<>();

        LocalDateTime cursor = start;
        // 按 2 小时一个桶聚合
        while (cursor.isBefore(end)) {
            LocalDateTime next = cursor.plusHours(2);
            if (next.isAfter(end)) {
                next = end;
            }
            long total = recordRepository.countBetween(cursor, next);
            long fail = recordRepository.countByStatusBetween("fail", cursor, next);
            long pass = recordRepository.countByStatusBetween("pass", cursor, next);

            labels.add(cursor.format(TIME_LABEL));
            defectCounts.add(fail);
            yieldRates.add(total == 0 ? 0 : roundPercent((double) pass / total));

            cursor = next;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("timeLabels", labels);
        data.put("defectCounts", defectCounts);
        data.put("yieldRates", yieldRates);
        return data;
    }

    @Override
    public List<Map<String, Object>> getDefectDistribution() {
        // 统计近 24 小时的缺陷类型分布，返回标准英文 type（前端负责转中文展示）
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(1);
        List<Object[]> rows = recordRepository.countByDefectTypeBetween(start, end);

        // 按规范化后的标准类型聚合（scratch/划伤 等合并为 scratches）
        Map<String, Long> byStandardType = new HashMap<>();
        for (Object[] row : rows) {
            String rawType = (String) row[0];
            long count = (Long) row[1];
            // 规范化缺陷类型：将 scratch/oversize/crack 等映射到标准类型
            String standardType = DefectTypeMapper.normalize(rawType);
            if (standardType == null || standardType.isBlank()) {
                standardType = rawType != null ? rawType.trim().toLowerCase() : "unknown";
            }
            byStandardType.merge(standardType, count, Long::sum);
        }

        long total = byStandardType.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : byStandardType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", e.getKey());
            item.put("count", e.getValue());
            item.put("percentage", total == 0 ? 0 : Math.round((double) e.getValue() * 100 / total));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAlerts() {
        // 取最近的严重报警记录
        List<DetectionRecord> records = recordRepository.findTop10BySeverityOrderByTimestampDesc("critical");
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (DetectionRecord record : records) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", record.getId());
            alert.put("severity", "critical");
            alert.put("title", record.getDefect() == null ? "异常报警" : record.getDefect());
            String line = record.getProductionLine() == null ? "生产线" : record.getProductionLine();
            alert.put("description", line + " | 组件 ID: " + record.getSerialNo());
            alert.put("time", record.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            alert.put("icon", "material-symbols:warning-rounded");
            alerts.add(alert);
        }
        return alerts;
    }

    private LocalDateTime parseDateTimeOrDefault(String value, LocalDateTime fallback) {
        // 支持为空时使用默认时间
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return LocalDateTime.parse(value.trim(), DATE_TIME);
    }

    /**
     * 百分比四舍五入保留 1 位小数。
     */
    private double roundPercent(double value) {
        return Math.round(value * 1000d) / 10d;
    }

    /**
     * 简单根据运行中产线占比计算稼动率。
     */
    private double calculateUtilizationRate() {
        long total = productionLineRepository.count();
        if (total == 0) {
            return 0;
        }
        long running = productionLineRepository.findAll().stream()
                .filter(line -> "running".equalsIgnoreCase(line.getStatus()))
                .count();
        return roundPercent((double) running / total);
    }

    private String mapDefectType(String defectType) {
        return DefectTypeMapper.displayName(defectType);
    }
}
