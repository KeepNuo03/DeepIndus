/**
 * 数据大屏相关 API
 */
import { get } from '@/utils/request';

/**
 * 获取 KPI 指标
 */
export function getKPI() {
  return get('/dashboard/kpi');
}

/**
 * 获取缺陷趋势数据
 */
export function getDefectTrend(params) {
  return get('/dashboard/defect-trend', params);
}

/**
 * 获取缺陷分布数据
 */
export function getDefectDistribution() {
  return get('/dashboard/defect-distribution');
}

/**
 * 获取报警列表
 */
export function getAlerts() {
  return get('/dashboard/alerts');
}
