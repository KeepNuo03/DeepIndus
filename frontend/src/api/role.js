/**
 * 角色管理相关 API
 */
import { get, post, put, del } from '@/utils/request';

/**
 * 获取角色列表（分页）
 * GET /v1/roles?page=1&pageSize=20
 */
export function getRoles(params = {}) {
  const { page = 1, pageSize = 20 } = params;
  const queryParams = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  }).toString();
  return get(`/roles?${queryParams}`);
}

/**
 * 获取所有角色（下拉选择）
 * GET /v1/roles/all
 */
export function getAllRoles() {
  return get('/roles/all');
}

/**
 * 获取角色详情
 * GET /v1/roles/{id}
 */
export function getRoleDetail(id) {
  return get(`/roles/${id}`);
}

/**
 * 创建角色
 * POST /v1/roles
 */
export function createRole(data) {
  return post('/roles', data);
}

/**
 * 更新角色
 * PUT /v1/roles/{id}
 */
export function updateRole(id, data) {
  return put(`/roles/${id}`, data);
}

/**
 * 删除角色
 * DELETE /v1/roles/{id}
 */
export function deleteRole(id) {
  return del(`/roles/${id}`);
}

/**
 * 分配权限
 * POST /v1/roles/{id}/permissions
 * permissionIds: number[]
 */
export function assignRolePermissions(id, permissionIds) {
  return post(`/roles/${id}/permissions`, permissionIds);
}
