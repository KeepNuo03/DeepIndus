package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.*;
import com.induscore.service.ModelService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 模型管理接口控制器。
 */
@RestController
@RequestMapping("/v1/models")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    private final ModelService modelService;

    @Autowired
    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    // ==================== 模型管理 ====================

    /**
     * 获取模型列表（支持筛选和分页）
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getModels(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String architecture,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        ModelQueryRequest query = new ModelQueryRequest(status, architecture, keyword, page, pageSize);
        Map<String, Object> data = modelService.getModels(query);
        return ApiResponse.success(data);
    }

    /**
     * 获取模型统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<ModelStatisticsResponse> getStatistics() {
        ModelStatisticsResponse statistics = modelService.getStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取已部署的模型列表
     */
    @GetMapping("/deployed")
    public ApiResponse<List<ModelResponse>> getDeployedModels() {
        List<ModelResponse> models = modelService.getDeployedModels();
        return ApiResponse.success(models);
    }

    /**
     * 获取主模型信息
     */
    @GetMapping("/main")
    public ApiResponse<ModelResponse> getMainModel() {
        ModelResponse model = modelService.getMainModel();
        return ApiResponse.success(model);
    }

    /**
     * 获取当前部署模型的简要信息（供检测服务使用）
     */
    @GetMapping("/deployed/info")
    public ApiResponse<Map<String, Object>> getDeployedModelInfo() {
        Map<String, Object> info = modelService.getDeployedModelInfo();
        return ApiResponse.success(info);
    }

    /**
     * 根据ID获取模型详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ModelResponse> getModelById(@PathVariable Long id) {
        ModelResponse model = modelService.getModelById(id);
        return ApiResponse.success(model);
    }

    /**
     * 根据编码获取模型
     */
    @GetMapping("/code/{code}")
    public ApiResponse<ModelResponse> getModelByCode(@PathVariable String code) {
        ModelResponse model = modelService.getModelByCode(code);
        return ApiResponse.success(model);
    }

    /**
     * 创建模型
     */
    @PostMapping
    public ApiResponse<ModelResponse> createModel(@Valid @RequestBody ModelRequest request) {
        log.info("创建模型请求: name={}, code={}", request.name(), request.code());
        ModelResponse model = modelService.createModel(request);
        return ApiResponse.success(model);
    }

    /**
     * 更新模型
     */
    @PutMapping("/{id}")
    public ApiResponse<ModelResponse> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelRequest request
    ) {
        log.info("更新模型请求: id={}", id);
        ModelResponse model = modelService.updateModel(id, request);
        return ApiResponse.success(model);
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteModel(@PathVariable Long id) {
        log.info("删除模型请求: id={}", id);
        modelService.deleteModel(id);
        return ApiResponse.success(null);
    }

    /**
     * 部署模型（激活指定版本）
     */
    @PostMapping("/{id}/deploy")
    public ApiResponse<ModelResponse> deployModel(
            @PathVariable Long id,
            @Valid @RequestBody DeployModelRequest request
    ) {
        log.info("部署模型请求: modelId={}, versionId={}", id, request.versionId());
        ModelResponse model = modelService.deployModel(id, request);
        return ApiResponse.success(model);
    }

    /**
     * 下线模型
     */
    @PostMapping("/{id}/undeploy")
    public ApiResponse<ModelResponse> undeployModel(@PathVariable Long id) {
        log.info("下线模型请求: modelId={}", id);
        ModelResponse model = modelService.undeployModel(id);
        return ApiResponse.success(model);
    }

    /**
     * 设置/取消主模型
     */
    @PostMapping("/{id}/main")
    public ApiResponse<ModelResponse> setMainModel(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean isMain
    ) {
        log.info("设置主模型请求: modelId={}, isMain={}", id, isMain);
        ModelResponse model = modelService.setMainModel(id, isMain);
        return ApiResponse.success(model);
    }

    /**
     * 更新模型状态
     */
    @PostMapping("/{id}/status")
    public ApiResponse<ModelResponse> updateModelStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.info("更新模型状态请求: modelId={}, status={}", id, status);
        ModelResponse model = modelService.updateModelStatus(id, status);
        return ApiResponse.success(model);
    }

    // ==================== 版本管理 ====================

    /**
     * 获取模型的所有版本
     */
    @GetMapping("/{modelId}/versions")
    public ApiResponse<List<ModelVersionResponse>> getVersions(@PathVariable Long modelId) {
        List<ModelVersionResponse> versions = modelService.getVersionsByModelId(modelId);
        return ApiResponse.success(versions);
    }

    /**
     * 获取版本详情
     */
    @GetMapping("/{modelId}/versions/{versionId}")
    public ApiResponse<ModelVersionResponse> getVersionById(
            @PathVariable Long modelId,
            @PathVariable Long versionId
    ) {
        ModelVersionResponse version = modelService.getVersionById(versionId);
        return ApiResponse.success(version);
    }

    /**
     * 创建模型版本
     */
    @PostMapping("/{modelId}/versions")
    public ApiResponse<ModelVersionResponse> createVersion(
            @PathVariable Long modelId,
            @Valid @RequestBody ModelVersionRequest request
    ) {
        log.info("创建模型版本请求: modelId={}, version={}", modelId, request.version());
        ModelVersionResponse version = modelService.createVersion(modelId, request);
        return ApiResponse.success(version);
    }

    /**
     * 更新模型版本
     */
    @PutMapping("/{modelId}/versions/{versionId}")
    public ApiResponse<ModelVersionResponse> updateVersion(
            @PathVariable Long modelId,
            @PathVariable Long versionId,
            @Valid @RequestBody ModelVersionRequest request
    ) {
        log.info("更新模型版本请求: modelId={}, versionId={}", modelId, versionId);
        ModelVersionResponse version = modelService.updateVersion(modelId, versionId, request);
        return ApiResponse.success(version);
    }

    /**
     * 删除模型版本
     */
    @DeleteMapping("/{modelId}/versions/{versionId}")
    public ApiResponse<Void> deleteVersion(
            @PathVariable Long modelId,
            @PathVariable Long versionId
    ) {
        log.info("删除模型版本请求: modelId={}, versionId={}", modelId, versionId);
        modelService.deleteVersion(modelId, versionId);
        return ApiResponse.success(null);
    }

    /**
     * 切换当前版本
     */
    @PostMapping("/{modelId}/versions/{versionId}/switch")
    public ApiResponse<ModelResponse> switchVersion(
            @PathVariable Long modelId,
            @PathVariable Long versionId
    ) {
        log.info("切换模型版本请求: modelId={}, versionId={}", modelId, versionId);
        ModelResponse model = modelService.switchVersion(modelId, versionId);
        return ApiResponse.success(model);
    }
}
