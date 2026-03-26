package com.induscore.service.agent.tool;

import com.induscore.dto.MobileUploadStatusResponse;
import com.induscore.dto.MobileWorkbenchResponse;
import com.induscore.dto.ModelQueryRequest;
import com.induscore.dto.ModelStatisticsResponse;
import com.induscore.model.ProductionLine;
import com.induscore.repository.ProductionLineRepository;
import com.induscore.service.DashboardService;
import com.induscore.service.ModelService;
import com.induscore.service.MobileInspectionService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 可调用的只读业务工具集合。
 * 当前覆盖：
 * - 工作台与通知
 * - 上传状态
 * - 检测记录（复核任务）
 * - 数据大屏指标
 */
@Service
public class AgentToolService {
    private final MobileInspectionService mobileInspectionService;
    private final DashboardService dashboardService;
    private final ProductionLineRepository productionLineRepository;
    private final ModelService modelService;

    public AgentToolService(
            MobileInspectionService mobileInspectionService,
            DashboardService dashboardService,
            ProductionLineRepository productionLineRepository,
            ModelService modelService
    ) {
        this.mobileInspectionService = mobileInspectionService;
        this.dashboardService = dashboardService;
        this.productionLineRepository = productionLineRepository;
        this.modelService = modelService;
    }

    public Map<String, Object> getMobileWorkbench() {
        MobileWorkbenchResponse response = mobileInspectionService.getWorkbench();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("pendingReviewCount", response.pendingReviewCount());
        map.put("processingCount", response.processingCount());
        map.put("todaySamplingCount", response.todaySamplingCount());
        map.put("severeAlertCount", response.severeAlertCount());
        map.put("uploadQueueStatus", response.uploadQueueStatus());
        return map;
    }

    public List<Map<String, Object>> getMobileNotifications(int limit) {
        return mobileInspectionService.listNotifications(limit);
    }

    public Map<String, Object> getMobileUploadStatus(String idempotencyKey) {
        MobileUploadStatusResponse response = mobileInspectionService.getUploadStatus(idempotencyKey);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idempotencyKey", response.idempotencyKey());
        map.put("status", response.status());
        map.put("deduplicated", response.deduplicated());
        map.put("message", response.message());
        map.put("recordId", response.recordId());
        map.put("retryCount", response.retryCount());
        map.put("updatedAt", response.updatedAt());
        return map;
    }

    /**
     * 检测记录聚合：来自移动端复核任务列表能力。
     */
    public Map<String, Object> getDetectionRecordsOverview(int page, int pageSize, String status) {
        return mobileInspectionService.listReviewTasks(page, pageSize, status);
    }

    /**
     * 单条检测记录详情（复核任务详情）。
     */
    public Map<String, Object> getDetectionRecordDetail(Long id) {
        return mobileInspectionService.getReviewTaskDetail(id);
    }

    /**
     * 试点指标（close-loop、upload recovery 等）。
     */
    public Map<String, Object> getPilotMetrics() {
        return mobileInspectionService.getPilotMetrics();
    }

    /**
     * 数据大屏 KPI。
     */
    public Map<String, Object> getDashboardKpi() {
        return dashboardService.getKpi();
    }

    /**
     * 数据大屏趋势。
     */
    public Map<String, Object> getDashboardDefectTrend(String startTime, String endTime) {
        return dashboardService.getDefectTrend(startTime, endTime);
    }

    /**
     * 数据大屏缺陷分布。
     */
    public List<Map<String, Object>> getDashboardDefectDistribution() {
        return dashboardService.getDefectDistribution();
    }

    /**
     * 数据大屏报警列表。
     */
    public List<Map<String, Object>> getDashboardAlerts() {
        return dashboardService.getAlerts();
    }

    /**
     * 生产线全局摘要（数量、状态分布、重点线体）。
     */
    public Map<String, Object> getProductionLinesOverview() {
        List<ProductionLine> all = productionLineRepository.findAll();
        long total = all.size();
        long running = all.stream().filter(line -> "running".equalsIgnoreCase(line.getStatus())).count();
        long maintenance = all.stream().filter(line -> "maintenance".equalsIgnoreCase(line.getStatus())).count();
        long stopped = all.stream().filter(line -> "stopped".equalsIgnoreCase(line.getStatus())).count();

        List<Map<String, Object>> topLines = all.stream()
                .limit(10)
                .map(line -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", line.getId());
                    item.put("name", line.getName());
                    item.put("status", line.getStatus());
                    item.put("todayOutput", line.getTodayOutput());
                    item.put("targetOutput", line.getTargetOutput());
                    item.put("yieldRate", line.getYieldRate());
                    item.put("utilizationRate", line.getUtilizationRate());
                    return item;
                })
                .toList();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", total);
        map.put("running", running);
        map.put("maintenance", maintenance);
        map.put("stopped", stopped);
        map.put("lines", topLines);
        return map;
    }

    /**
     * 单条生产线详情，便于回答“某条产线当前状态”类问题。
     */
    public Map<String, Object> getProductionLineDetail(Long lineId) {
        ProductionLine line = productionLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("生产线不存在: " + lineId));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", line.getId());
        map.put("name", line.getName());
        map.put("status", line.getStatus());
        map.put("location", line.getLocation());
        map.put("shift", line.getShift());
        map.put("runningTime", line.getRunningTime());
        map.put("todayOutput", line.getTodayOutput());
        map.put("targetOutput", line.getTargetOutput());
        map.put("qualifiedCount", line.getQualifiedCount());
        map.put("defectCount", line.getDefectCount());
        map.put("yieldRate", line.getYieldRate());
        map.put("utilizationRate", line.getUtilizationRate());
        map.put("cycleTime", line.getCycleTime());
        return map;
    }

    /**
     * AI 模型管理摘要（统计 + 当前部署 + 列表快照）。
     */
    public Map<String, Object> getModelManagementOverview() {
        ModelStatisticsResponse statistics = modelService.getStatistics();
        Map<String, Object> deployedInfo = modelService.getDeployedModelInfo();
        Map<String, Object> modelList = modelService.getModels(new ModelQueryRequest("all", "all", null, 1, 10));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("statistics", statistics);
        map.put("deployedModel", deployedInfo);
        map.put("models", modelList);
        return map;
    }
}
