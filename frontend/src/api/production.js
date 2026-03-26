/**
 * 生产线管理相关 API
 */
import { get, post, put } from '@/utils/request';

/**
 * 获取生产线列表
 */
export function getProductionLines() {
  return get('/production/lines');
}

/**
 * 控制生产线（启动/停止）
 */
export function controlLine(id, action) {
  return post(`/production/lines/${id}/control`, { action });
}

/**
 * 编辑生产线
 */
export function updateLine(id, data) {
  return put(`/production/lines/${id}`, data);
}

/**
 * 配置摄像头
 */
export function updateCamera(lineId, cameraId, data) {
  return put(`/production/lines/${lineId}/cameras/${cameraId}`, data);
}

/**
 * 获取生产线可生产的产品列表
 */
export function getLineProducts(lineId) {
  return get(`/production/lines/${lineId}/products`);
}
