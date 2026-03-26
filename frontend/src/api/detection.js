/**
 * 检测记录相关 API
 */
import { get, post, del, upload } from '@/utils/request';

/**
 * 查询检测记录（记录管理列表专用，与实时检测、数据大屏接口不同）
 * 实际请求：GET {baseURL}/records/query 即 GET /v1/records/query
 * 支持参数：page, pageSize, dateStart, dateEnd, defectType, status, search
 * 不传 dateStart/dateEnd 时后端返回全部日期；defectType/status 传 all 或不传表示不过滤
 */
export function getRecords(params) {
  return get('/records/query', params);
}

/**
 * 批量导出记录
 */
export function exportRecords(data) {
  return post('/records/export', data);
}

/**
 * 批量删除记录
 */
export function deleteRecords(data) {
  return del('/records/batch', data);
}

/**
 * 获取缺陷详情
 * 请求：GET /v1/defect/:id（id 会 encodeURIComponent，避免 detectionNo 含 # 时被浏览器截断）
 */
export function getDefectDetail(id) {
  const encodedId = encodeURIComponent(String(id));
  return get(`/defect/${encodedId}`);
}

/**
 * 开始人工复核
 */
export function reviewDefect(id, data) {
  const encodedId = encodeURIComponent(String(id));
  return post(`/defect/${encodedId}/review`, data);
}

// ==================== 实时检测相关 ====================

/**
 * 获取实时检测统计
 */
export function getRealtimeStatistics() {
  return get('/detection/realtime/statistics');
}

/**
 * 获取实时检测记录
 */
export function getRealtimeRecords(params) {
  return get('/detection/realtime/records', params);
}

/**
 * 获取实时趋势数据
 */
export function getRealtimeTrend() {
  return get('/detection/realtime/trend');
}

/**
 * 控制检测状态（启动/停止）
 */
export function controlDetection(data) {
  return post('/detection/realtime/control', data);
}

/**
 * 上传图片检测
 */
export function uploadDetection(formData) {
  return upload('/detection/upload', formData);
}

/**
 * 实时检测：上传一帧画面（本机摄像头定时采集）
 * POST /v1/detection/realtime/frame，FormData: file（必填）, cameraId（可选 "browser"）
 */
export function uploadRealtimeFrame(formData) {
  return upload('/detection/realtime/frame', formData);
}
