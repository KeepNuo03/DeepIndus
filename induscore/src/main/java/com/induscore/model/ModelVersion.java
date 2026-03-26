package com.induscore.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 模型版本实体（model_versions 表）。
 * 管理模型的各个版本及其性能指标、训练信息和部署状态。
 */
@Entity
@Table(name = "model_versions")
public class ModelVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", insertable = false, updatable = false)
    private AiModel model;

    @Column(nullable = false, length = 64)
    private String version;

    @Column(length = 512)
    private String description;

    @Column(nullable = false, length = 32)
    private String status = "draft";

    @Column(name = "is_current")
    private Boolean isCurrent = false;

    // 性能指标
    private Double accuracy;

    private Double recall;

    @Column(name = "f1_score")
    private Double f1Score;

    @Column(name = "precision_val")
    private Double precisionVal;

    @Column(name = "map_val")
    private Double mapVal;

    @Column(name = "map50_95")
    private Double map50_95;

    @Column(name = "inference_speed")
    private Double inferenceSpeed;

    @Column(name = "model_size_mb")
    private Double modelSizeMb;

    // 训练信息
    @Column(name = "training_data_size")
    private Integer trainingDataSize;

    @Column(name = "trained_at")
    private LocalDate trainedAt;

    @Column(length = 64)
    private String trainer;

    @Column(name = "training_epochs")
    private Integer trainingEpochs;

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "learning_rate", length = 32)
    private String learningRate;

    // 文件信息
    @Column(name = "model_file", length = 255)
    private String modelFile;

    @Column(name = "config_file", length = 255)
    private String configFile;

    @Column(name = "label_file", length = 255)
    private String labelFile;

    // 部署信息
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @Column(name = "deployed_by", length = 64)
    private String deployedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ModelVersion() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public AiModel getModel() {
        return model;
    }

    public void setModel(AiModel model) {
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }

    public Double getPrecisionVal() {
        return precisionVal;
    }

    public void setPrecisionVal(Double precisionVal) {
        this.precisionVal = precisionVal;
    }

    public Double getMapVal() {
        return mapVal;
    }

    public void setMapVal(Double mapVal) {
        this.mapVal = mapVal;
    }

    public Double getMap50_95() {
        return map50_95;
    }

    public void setMap50_95(Double map50_95) {
        this.map50_95 = map50_95;
    }

    public Double getInferenceSpeed() {
        return inferenceSpeed;
    }

    public void setInferenceSpeed(Double inferenceSpeed) {
        this.inferenceSpeed = inferenceSpeed;
    }

    public Double getModelSizeMb() {
        return modelSizeMb;
    }

    public void setModelSizeMb(Double modelSizeMb) {
        this.modelSizeMb = modelSizeMb;
    }

    public Integer getTrainingDataSize() {
        return trainingDataSize;
    }

    public void setTrainingDataSize(Integer trainingDataSize) {
        this.trainingDataSize = trainingDataSize;
    }

    public LocalDate getTrainedAt() {
        return trainedAt;
    }

    public void setTrainedAt(LocalDate trainedAt) {
        this.trainedAt = trainedAt;
    }

    public String getTrainer() {
        return trainer;
    }

    public void setTrainer(String trainer) {
        this.trainer = trainer;
    }

    public Integer getTrainingEpochs() {
        return trainingEpochs;
    }

    public void setTrainingEpochs(Integer trainingEpochs) {
        this.trainingEpochs = trainingEpochs;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(String learningRate) {
        this.learningRate = learningRate;
    }

    public String getModelFile() {
        return modelFile;
    }

    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getLabelFile() {
        return labelFile;
    }

    public void setLabelFile(String labelFile) {
        this.labelFile = labelFile;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 是否已部署
     */
    public boolean isDeployed() {
        return "deployed".equals(status);
    }

    /**
     * 是否就绪
     */
    public boolean isReady() {
        return "ready".equals(status) || "deployed".equals(status);
    }

    @Override
    public String toString() {
        return "ModelVersion{" +
                "id=" + id +
                ", modelId=" + modelId +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
