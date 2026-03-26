/**
 * 部门管理相关 API
 */
import { get, post, put, del } from '@/utils/request';

/**
 * 获取部门树形结构
 * GET /v1/departments/tree
 */
export function getDepartmentTree() {
  return get('/departments/tree');
}

/**
 * 获取部门列表（平级）
 * GET /v1/departments
 */
export function getDepartments() {
  return get('/departments');
}

/**
 * 获取活跃部门（下拉选择用）
 * GET /v1/departments/active
 */
export function getActiveDepartments() {
  return get('/departments/active');
}

/**
 * 获取部门统计
 * GET /v1/departments/statistics
 */
export function getDepartmentStatistics() {
  return get('/departments/statistics');
}

/**
 * 获取部门详情
 * GET /v1/departments/{id}
 */
export function getDepartmentDetail(id) {
  return get(`/departments/${id}`);
}

/**
 * 创建部门
 * POST /v1/departments
 */
export function createDepartment(data) {
  return post('/departments', data);
}

/**
 * 更新部门
 * PUT /v1/departments/{id}
 */
export function updateDepartment(id, data) {
  return put(`/departments/${id}`, data);
}

/**
 * 删除部门
 * DELETE /v1/departments/{id}
 */
export function deleteDepartment(id) {
  return del(`/departments/${id}`);
}

/**
 * 更新部门状态
 * POST /v1/departments/{id}/status?status={status}
 */
export function updateDepartmentStatus(id, status) {
  return post(`/departments/${id}/status?status=${encodeURIComponent(status)}`);
}

/**
 * 设置部门负责人
 * POST /v1/departments/{id}/manager?userId={userId}
 */
export function setDepartmentManager(id, userId) {
  return post(`/departments/${id}/manager?userId=${encodeURIComponent(userId)}`);
}
