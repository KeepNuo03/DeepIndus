/**
 * 用户管理相关 API
 */
import { get } from '@/utils/request';

/**
 * 获取用户列表
 * GET /v1/users?page=1&pageSize=20&department=all&status=active&search=
 */
export function getUsers(params = {}) {
  const { page = 1, pageSize = 20, department = 'all', status = 'active', search = '' } = params;
  const queryParams = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
    department,
    status,
    search,
  }).toString();
  return get(`/users?${queryParams}`);
}

/**
 * 获取用户角色列表（Mock）
 * GET /v1/users/roles
 */
export function getUserRoles() {
  return get('/users/roles');
}
