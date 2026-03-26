package com.induscore.service;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏业务接口。
 */
public interface DashboardService {
    /**
     * 获取 KPI 指标。
     */
    Map<String, Object> getKpi();

    /**
     * 获取缺陷趋势（起止时间可选）。
     */
    Map<String, Object> getDefectTrend(String startTime, String endTime);

    /**
     * 获取缺陷类型分布。
     */
    List<Map<String, Object>> getDefectDistribution();

    /**
     * 获取报警列表。
     */
    List<Map<String, Object>> getAlerts();
}
