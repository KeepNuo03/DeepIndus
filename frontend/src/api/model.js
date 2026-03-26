/**
 * AI 模型管理相关 API
 */
import { get, post } from '@/utils/request';

/**
 * 获取模型列表
 * GET /v1/models
 */
export function getModels() {
  return get('/models');
}

/**
 * 获取模型统计数据
 * GET /v1/models/statistics
 */
export function getModelStatistics() {
  return get('/models/statistics');
}

/**
 * 部署模型
 * POST /v1/models/{modelId}/deploy
 * 后端要求：
 * - 路径参数 {id} = 模型ID
 * - 请求体 body = { versionId: xxx, deployedBy: 'xxx' }
 */
export function deployModel(modelId, versionId, deployedBy = '') {
  return post(`/models/${modelId}/deploy`, {
    versionId: versionId,
    deployedBy: deployedBy,
  });
}

/**
 * 下线模型
 * POST /v1/models/{id}/undeploy
 */
export function undeployModel(id) {
  return post(`/models/${id}/undeploy`);
}

/**
 * 获取当前部署的模型信息
 * GET /v1/models/deployed/info
 */
export function getDeployedModelInfo() {
  return get('/models/deployed/info');
}
