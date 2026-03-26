/**
 * 权限管理相关 API
 */
import { get } from '@/utils/request';

/**
 * 获取所有权限
 * GET /v1/permissions
 */
export function getPermissions() {
  return get('/permissions');
}

/**
 * 按模块获取权限
 * GET /v1/permissions/by-module
 */
export function getPermissionsByModule() {
  return get('/permissions/by-module');
}
