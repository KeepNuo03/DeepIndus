package com.induscore.service;

import com.induscore.dto.*;

import java.util.List;
import java.util.Map;

/**
 * AI 模型管理服务接口。
 */
public interface ModelService {

    /**
     * 创建模型
     */
    ModelResponse createModel(ModelRequest request);

    /**
     * 更新模型
     */
    ModelResponse updateModel(Long id, ModelRequest request);

    /**
     * 删除模型
     */
    void deleteModel(Long id);

    /**
     * 获取模型详情
     */
    ModelResponse getModelById(Long id);

    /**
     * 根据编码获取模型
     */
    ModelResponse getModelByCode(String code);

    /**
     * 获取模型列表（支持筛选）
     */
    Map<String, Object> getModels(ModelQueryRequest query);

    /**
     * 获取所有已部署模型
     */
    List<ModelResponse> getDeployedModels();

    /**
     * 获取当前激活的主模型
     */
    ModelResponse getMainModel();

    /**
     * 部署模型（将指定版本设为当前版本并激活模型）
     */
    ModelResponse deployModel(Long modelId, DeployModelRequest request);

    /**
     * 下线模型
     */
    ModelResponse undeployModel(Long modelId);

    /**
     * 设置主模型
     */
    ModelResponse setMainModel(Long modelId, boolean isMain);

    /**
     * 更新模型状态
     */
    ModelResponse updateModelStatus(Long modelId, String status);

    // ==================== 版本管理 ====================

    /**
     * 创建模型版本
     */
    ModelVersionResponse createVersion(Long modelId, ModelVersionRequest request);

    /**
     * 更新模型版本
     */
    ModelVersionResponse updateVersion(Long modelId, Long versionId, ModelVersionRequest request);

    /**
     * 删除模型版本
     */
    void deleteVersion(Long modelId, Long versionId);

    /**
     * 获取版本详情
     */
    ModelVersionResponse getVersionById(Long versionId);

    /**
     * 获取模型的所有版本
     */
    List<ModelVersionResponse> getVersionsByModelId(Long modelId);

    /**
     * 切换当前版本
     */
    ModelResponse switchVersion(Long modelId, Long versionId);

    /**
     * 获取模型统计信息
     */
    ModelStatisticsResponse getStatistics();

    /**
     * 获取当前部署的模型名称（用于检测服务）
     */
    String getDeployedModelName();

    /**
     * 获取当前部署的模型版本信息
     */
    Map<String, Object> getDeployedModelInfo();
}
