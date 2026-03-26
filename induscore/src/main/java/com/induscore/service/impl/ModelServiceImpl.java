package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.*;
import com.induscore.model.AiModel;
import com.induscore.model.ModelVersion;
import com.induscore.repository.AiModelRepository;
import com.induscore.repository.ModelVersionRepository;
import com.induscore.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 模型管理服务实现。
 */
@Service
@Transactional(readOnly = true)
public class ModelServiceImpl implements ModelService {

    private static final Logger log = LoggerFactory.getLogger(ModelServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AiModelRepository modelRepository;
    private final ModelVersionRepository versionRepository;

    public ModelServiceImpl(AiModelRepository modelRepository, ModelVersionRepository versionRepository) {
        this.modelRepository = modelRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    @Transactional
    public ModelResponse createModel(ModelRequest request) {
        // 检查编码是否重复
        if (modelRepository.existsByCode(request.code())) {
            throw new ApiException(400, "模型编码已存在: " + request.code());
        }

        AiModel model = new AiModel();
        model.setName(request.name());
        model.setCode(request.code());
        model.setDescription(request.description());
        model.setArchitecture(request.architecture());
        model.setDataset(request.dataset());
        model.setTaskType(request.taskType());
        model.setStatus("inactive");
        model.setIsMain(false);
        model.setModelFilePath(request.modelFilePath());
        model.setConfigFilePath(request.configFilePath());

        modelRepository.save(model);
        log.info("模型创建成功: id={}, name={}, code={}", model.getId(), model.getName(), model.getCode());

        return mapToModelResponse(model, false);
    }

    @Override
    @Transactional
    public ModelResponse updateModel(Long id, ModelRequest request) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + id));

        // 检查编码是否与其他模型重复
        if (request.code() != null && !request.code().equals(model.getCode())) {
            if (modelRepository.existsByCodeAndIdNot(request.code(), id)) {
                throw new ApiException(400, "模型编码已存在: " + request.code());
            }
            model.setCode(request.code());
        }

        if (request.name() != null) model.setName(request.name());
        if (request.description() != null) model.setDescription(request.description());
        if (request.architecture() != null) model.setArchitecture(request.architecture());
        if (request.dataset() != null) model.setDataset(request.dataset());
        if (request.taskType() != null) model.setTaskType(request.taskType());
        if (request.modelFilePath() != null) model.setModelFilePath(request.modelFilePath());
        if (request.configFilePath() != null) model.setConfigFilePath(request.configFilePath());

        modelRepository.save(model);
        log.info("模型更新成功: id={}", id);

        return mapToModelResponse(model, true);
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + id));

        // 如果模型已部署，不允许删除
        if ("deployed".equals(model.getStatus())) {
            throw new ApiException(400, "已部署的模型不能删除，请先下线模型");
        }

        // 如果模型是主模型，清除主模型标记
        if (Boolean.TRUE.equals(model.getIsMain())) {
            model.setIsMain(false);
        }

        modelRepository.delete(model);
        log.info("模型删除成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ModelResponse getModelById(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + id));

        // 强制初始化版本列表，避免懒加载问题
        model.getVersions().size();

        return mapToModelResponse(model, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ModelResponse getModelByCode(String code) {
        AiModel model = modelRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + code));

        // 强制初始化版本列表，避免懒加载问题
        model.getVersions().size();

        return mapToModelResponse(model, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getModels(ModelQueryRequest query) {
        List<AiModel> models;

        // 根据查询条件筛选
        if (query.status() != null && !"all".equals(query.status())) {
            models = modelRepository.findByStatusOrderByCreatedAtDesc(query.status());
        } else if (query.architecture() != null && !"all".equals(query.architecture())) {
            models = modelRepository.findByArchitectureOrderByCreatedAtDesc(query.architecture());
        } else if (query.keyword() != null && !query.keyword().isBlank()) {
            models = modelRepository.searchByKeyword(query.keyword());
        } else {
            models = modelRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // 强制初始化所有模型的版本列表，避免懒加载问题
        for (AiModel model : models) {
            model.getVersions().size();
        }

        // 手动分页
        int total = models.size();
        int start = (query.page() - 1) * query.pageSize();
        int end = Math.min(start + query.pageSize(), total);

        // 列表接口也需要包含当前版本详情（用于显示性能指标）
        List<ModelResponse> list = models.subList(start, end).stream()
                .map(m -> mapToModelResponse(m, true))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("models", list);
        data.put("total", total);
        data.put("page", query.page());
        data.put("pageSize", query.pageSize());

        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelResponse> getDeployedModels() {
        List<AiModel> models = modelRepository.findByStatusInOrderByCreatedAtDesc(List.of("active", "deployed"));

        // 强制初始化所有模型的版本列表，避免懒加载问题
        for (AiModel model : models) {
            model.getVersions().size();
        }

        return models.stream()
                .map(m -> mapToModelResponse(m, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ModelResponse getMainModel() {
        AiModel model = modelRepository.findByIsMainTrue()
                .orElseThrow(() -> new ApiException(404, "未设置主模型"));

        // 强制初始化版本列表，避免懒加载问题
        model.getVersions().size();

        return mapToModelResponse(model, true);
    }

    @Override
    @Transactional
    public ModelResponse deployModel(Long modelId, DeployModelRequest request) {
        AiModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        ModelVersion version = versionRepository.findById(request.versionId())
                .orElseThrow(() -> new ApiException(404, "版本不存在: " + request.versionId()));

        // 验证版本属于该模型
        if (!version.getModelId().equals(modelId)) {
            throw new ApiException(400, "版本不属于该模型");
        }

        // 取消其他已部署模型的部署状态（只能有一个部署模型）
        List<AiModel> deployedModels = modelRepository.findDeployedModels();
        for (AiModel deployedModel : deployedModels) {
            if (!deployedModel.getId().equals(modelId)) {
                deployedModel.setStatus("active");
                modelRepository.save(deployedModel);
                log.info("其他模型已下线: id={}", deployedModel.getId());
            }
        }

        // 设置当前版本
        versionRepository.clearCurrentVersion(modelId);
        version.setIsCurrent(true);
        version.setStatus("deployed");
        version.setDeployedAt(LocalDateTime.now());
        version.setDeployedBy(request.deployedBy() != null ? request.deployedBy() : "system");
        versionRepository.save(version);

        // 更新模型状态
        model.setStatus("deployed");
        model.setCurrentVersionId(version.getId());
        model.setCurrentVersionName(version.getVersion());
        model.setIsMain(true); // 部署的模型自动设为主模型
        modelRepository.save(model);

        log.info("模型部署成功: modelId={}, version={}", modelId, version.getVersion());

        // 重新加载模型以获取最新状态（包括版本关联）
        AiModel updatedModel = modelRepository.findById(modelId).orElse(model);
        updatedModel.getVersions().size(); // 强制初始化

        return mapToModelResponse(updatedModel, true);
    }

    @Override
    @Transactional
    public ModelResponse undeployModel(Long modelId) {
        AiModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        if (!"deployed".equals(model.getStatus())) {
            throw new ApiException(400, "模型未部署");
        }

        // 清除当前版本的部署状态
        if (model.getCurrentVersionId() != null) {
            versionRepository.findById(model.getCurrentVersionId()).ifPresent(v -> {
                v.setStatus("ready");
                v.setIsCurrent(false);
                v.setDeployedAt(null);
                v.setDeployedBy(null);
                versionRepository.save(v);
            });
        }

        model.setStatus("inactive");
        model.setIsMain(false);
        modelRepository.save(model);

        log.info("模型已下线: modelId={}", modelId);

        // 重新加载模型以获取最新状态
        AiModel updatedModel = modelRepository.findById(modelId).orElse(model);
        updatedModel.getVersions().size(); // 强制初始化

        return mapToModelResponse(updatedModel, true);
    }

    @Override
    @Transactional
    public ModelResponse setMainModel(Long modelId, boolean isMain) {
        AiModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        // 如果要设为主模型，先清除其他主模型
        if (isMain) {
            modelRepository.findByIsMainTrue().ifPresent(mainModel -> {
                if (!mainModel.getId().equals(modelId)) {
                    mainModel.setIsMain(false);
                    modelRepository.save(mainModel);
                }
            });
        }

        model.setIsMain(isMain);
        modelRepository.save(model);

        // 强制初始化版本列表
        model.getVersions().size();

        return mapToModelResponse(model, true);
    }

    @Override
    @Transactional
    public ModelResponse updateModelStatus(Long modelId, String status) {
        AiModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        // 验证状态值
        List<String> validStatuses = List.of("inactive", "active", "deployed");
        if (!validStatuses.contains(status)) {
            throw new ApiException(400, "无效的状态值: " + status);
        }

        model.setStatus(status);
        modelRepository.save(model);

        // 强制初始化版本列表
        model.getVersions().size();

        return mapToModelResponse(model, true);
    }

    // ==================== 版本管理 ====================

    @Override
    @Transactional
    public ModelVersionResponse createVersion(Long modelId, ModelVersionRequest request) {
        AiModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        // 检查版本号是否重复
        if (versionRepository.existsByModelIdAndVersion(modelId, request.version())) {
            throw new ApiException(400, "版本号已存在: " + request.version());
        }

        ModelVersion version = new ModelVersion();
        version.setModelId(modelId);
        version.setVersion(request.version());
        version.setDescription(request.description());
        version.setStatus("ready");
        version.setIsCurrent(false);

        // 性能指标
        version.setAccuracy(request.accuracy());
        version.setRecall(request.recall());
        version.setF1Score(request.f1Score());
        version.setPrecisionVal(request.precision());
        version.setMapVal(request.map());
        version.setMap50_95(request.map50_95());
        version.setInferenceSpeed(request.inferenceSpeed());
        version.setModelSizeMb(request.modelSizeMb());

        // 训练信息
        version.setTrainingDataSize(request.trainingDataSize());
        if (request.trainedAt() != null && !request.trainedAt().isBlank()) {
            version.setTrainedAt(LocalDate.parse(request.trainedAt(), DATE_FORMATTER));
        }
        version.setTrainer(request.trainer());
        version.setTrainingEpochs(request.trainingEpochs());
        version.setBatchSize(request.batchSize());
        version.setLearningRate(request.learningRate());

        // 文件信息
        version.setModelFile(request.modelFile());
        version.setConfigFile(request.configFile());
        version.setLabelFile(request.labelFile());

        versionRepository.save(version);
        log.info("模型版本创建成功: modelId={}, version={}", modelId, request.version());

        return mapToVersionResponse(version);
    }

    @Override
    @Transactional
    public ModelVersionResponse updateVersion(Long modelId, Long versionId, ModelVersionRequest request) {
        ModelVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ApiException(404, "版本不存在: " + versionId));

        if (!version.getModelId().equals(modelId)) {
            throw new ApiException(400, "版本不属于该模型");
        }

        // 已部署的版本不允许修改
        if ("deployed".equals(version.getStatus())) {
            throw new ApiException(400, "已部署的版本不能修改");
        }

        // 更新字段
        if (request.description() != null) version.setDescription(request.description());
        if (request.accuracy() != null) version.setAccuracy(request.accuracy());
        if (request.recall() != null) version.setRecall(request.recall());
        if (request.f1Score() != null) version.setF1Score(request.f1Score());
        if (request.precision() != null) version.setPrecisionVal(request.precision());
        if (request.map() != null) version.setMapVal(request.map());
        if (request.map50_95() != null) version.setMap50_95(request.map50_95());
        if (request.inferenceSpeed() != null) version.setInferenceSpeed(request.inferenceSpeed());
        if (request.modelSizeMb() != null) version.setModelSizeMb(request.modelSizeMb());
        if (request.trainingDataSize() != null) version.setTrainingDataSize(request.trainingDataSize());
        if (request.trainer() != null) version.setTrainer(request.trainer());
        if (request.trainingEpochs() != null) version.setTrainingEpochs(request.trainingEpochs());
        if (request.batchSize() != null) version.setBatchSize(request.batchSize());
        if (request.learningRate() != null) version.setLearningRate(request.learningRate());
        if (request.modelFile() != null) version.setModelFile(request.modelFile());
        if (request.configFile() != null) version.setConfigFile(request.configFile());
        if (request.labelFile() != null) version.setLabelFile(request.labelFile());

        versionRepository.save(version);
        log.info("模型版本更新成功: versionId={}", versionId);

        return mapToVersionResponse(version);
    }

    @Override
    @Transactional
    public void deleteVersion(Long modelId, Long versionId) {
        ModelVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ApiException(404, "版本不存在: " + versionId));

        if (!version.getModelId().equals(modelId)) {
            throw new ApiException(400, "版本不属于该模型");
        }

        // 已部署或当前激活的版本不能删除
        if ("deployed".equals(version.getStatus()) || Boolean.TRUE.equals(version.getIsCurrent())) {
            throw new ApiException(400, "已部署或当前激活的版本不能删除");
        }

        versionRepository.delete(version);
        log.info("模型版本删除成功: versionId={}", versionId);
    }

    @Override
    public ModelVersionResponse getVersionById(Long versionId) {
        ModelVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ApiException(404, "版本不存在: " + versionId));
        return mapToVersionResponse(version);
    }

    @Override
    public List<ModelVersionResponse> getVersionsByModelId(Long modelId) {
        // 验证模型存在
        modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        return versionRepository.findByModelIdOrderByCreatedAtDesc(modelId)
                .stream()
                .map(this::mapToVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ModelResponse switchVersion(Long modelId, Long versionId) {
        AiModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ApiException(404, "模型不存在: " + modelId));

        ModelVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ApiException(404, "版本不存在: " + versionId));

        if (!version.getModelId().equals(modelId)) {
            throw new ApiException(400, "版本不属于该模型");
        }

        // 清除当前版本标记
        versionRepository.clearCurrentVersion(modelId);

        // 设置新版本为当前版本
        version.setIsCurrent(true);
        versionRepository.save(version);

        // 更新模型的当前版本
        model.setCurrentVersionId(versionId);
        model.setCurrentVersionName(version.getVersion());
        modelRepository.save(model);

        log.info("模型版本切换成功: modelId={}, version={}", modelId, version.getVersion());

        return mapToModelResponse(model, true);
    }

    @Override
    public ModelStatisticsResponse getStatistics() {
        List<AiModel> allModels = modelRepository.findAll();

        int total = allModels.size();
        int deployed = (int) allModels.stream().filter(m -> "deployed".equals(m.getStatus())).count();
        int active = (int) allModels.stream().filter(m -> "active".equals(m.getStatus())).count();
        int draft = (int) allModels.stream().filter(m -> "inactive".equals(m.getStatus())).count();

        // 计算平均准确率（从已部署版本的性能指标）
        Double avgAccuracy = allModels.stream()
                .filter(m -> m.getCurrentVersionId() != null)
                .map(m -> versionRepository.findById(m.getCurrentVersionId()).orElse(null))
                .filter(Objects::nonNull)
                .mapToDouble(v -> v.getAccuracy() != null ? v.getAccuracy() : 0)
                .average()
                .orElse(0.0);

        long totalVersions = versionRepository.count();

        // AB测试数量（有多个已部署版本的模型数）
        int abTests = (int) allModels.stream()
                .filter(m -> versionRepository.countByModelId(m.getId()) > 1)
                .count();

        return new ModelStatisticsResponse(
                total, deployed, active, draft,
                Math.round(avgAccuracy * 100) / 100.0,
                (int) totalVersions, abTests
        );
    }

    @Override
    public String getDeployedModelName() {
        return modelRepository.findByIsMainTrue()
                .map(AiModel::getName)
                .orElse("YOLOv11-Industrial"); // 默认回退
    }

    @Override
    public Map<String, Object> getDeployedModelInfo() {
        Optional<AiModel> mainModelOpt = modelRepository.findByIsMainTrue();

        if (mainModelOpt.isEmpty()) {
            Map<String, Object> defaultInfo = new HashMap<>();
            defaultInfo.put("id", 1);
            defaultInfo.put("name", "YOLOv11-Industrial");
            defaultInfo.put("version", "v1.0.0");
            defaultInfo.put("architecture", "YOLOv11");
            defaultInfo.put("status", "deployed");
            return defaultInfo;
        }

        AiModel model = mainModelOpt.get();
        Map<String, Object> info = new HashMap<>();
        info.put("id", model.getId());
        info.put("name", model.getName());
        info.put("code", model.getCode());
        info.put("version", model.getCurrentVersionName());
        info.put("architecture", model.getArchitecture());
        info.put("dataset", model.getDataset());
        info.put("status", model.getStatus());

        if (model.getCurrentVersionId() != null) {
            versionRepository.findById(model.getCurrentVersionId()).ifPresent(v -> {
                info.put("accuracy", v.getAccuracy());
                info.put("recall", v.getRecall());
                info.put("f1Score", v.getF1Score());
                info.put("inferenceSpeed", v.getInferenceSpeed());
                info.put("modelSize", v.getModelSizeMb());
            });
        }

        return info;
    }

    // ==================== 映射方法 ====================

    private ModelResponse mapToModelResponse(AiModel model, boolean includeVersions) {
        List<ModelVersionResponse> versions = null;
        ModelVersionResponse currentVersionDetail = null;

        if (includeVersions) {
            versions = model.getVersions().stream()
                    .map(this::mapToVersionResponse)
                    .collect(Collectors.toList());

            if (model.getCurrentVersionId() != null) {
                currentVersionDetail = versionRepository.findById(model.getCurrentVersionId())
                        .map(this::mapToVersionResponse)
                        .orElse(null);
            }
        }

        return new ModelResponse(
                model.getId(),
                model.getName(),
                model.getCode(),
                model.getDescription(),
                model.getArchitecture(),
                model.getDataset(),
                model.getTaskType(),
                model.getStatus(),
                model.getIsMain(),
                model.getCurrentVersionName(),
                model.getCurrentVersionId(),
                model.getModelFilePath(),
                model.getConfigFilePath(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                versions,
                currentVersionDetail
        );
    }

    private ModelVersionResponse mapToVersionResponse(ModelVersion version) {
        return new ModelVersionResponse(
                version.getId(),
                version.getModelId(),
                version.getVersion(),
                version.getDescription(),
                version.getStatus(),
                version.getIsCurrent(),
                version.getAccuracy(),
                version.getRecall(),
                version.getF1Score(),
                version.getPrecisionVal(),
                version.getMapVal(),
                version.getMap50_95(),
                version.getInferenceSpeed(),
                version.getModelSizeMb(),
                version.getTrainingDataSize(),
                version.getTrainedAt(),
                version.getTrainer(),
                version.getTrainingEpochs(),
                version.getBatchSize(),
                version.getLearningRate(),
                version.getModelFile(),
                version.getConfigFile(),
                version.getLabelFile(),
                version.getDeployedAt(),
                version.getDeployedBy(),
                version.getCreatedAt(),
                version.getUpdatedAt()
        );
    }
}
