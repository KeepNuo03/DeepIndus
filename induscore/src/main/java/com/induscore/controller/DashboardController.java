package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计接口。
 */
@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取 KPI 指标。
     */
    @GetMapping("/kpi")
    public ApiResponse<Map<String, Object>> getKpi() {
        return ApiResponse.success(dashboardService.getKpi());
    }

    /**
     * 获取缺陷趋势数据（可传起止时间）。
     */
    @GetMapping("/defect-trend")
    public ApiResponse<Map<String, Object>> getDefectTrend(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        return ApiResponse.success(dashboardService.getDefectTrend(startTime, endTime));
    }

    /**
     * 获取缺陷类型分布。
     */
    @GetMapping("/defect-distribution")
    public ApiResponse<List<Map<String, Object>>> getDefectDistribution() {
        return ApiResponse.success(dashboardService.getDefectDistribution());
    }

    /**
     * 获取实时报警列表。
     */
    @GetMapping("/alerts")
    public ApiResponse<List<Map<String, Object>>> getAlerts() {
        return ApiResponse.success(dashboardService.getAlerts());
    }
}
