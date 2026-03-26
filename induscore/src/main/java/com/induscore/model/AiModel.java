package com.induscore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 模型实体（ai_models 表）。
 * 管理 YOLO 等检测模型的元数据、版本和部署状态。
 */
@Entity
@Table(name = "ai_models")
public class AiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(length = 512)
    private String description;

    @Column(length = 64)
    private String architecture;

    @Column(length = 128)
    private String dataset;

    @Column(name = "task_type", length = 64)
    private String taskType;

    @Column(nullable = false, length = 32)
    private String status = "inactive";

    @Column(name = "is_main")
    private Boolean isMain = false;

    @Column(name = "current_version_id")
    private Long currentVersionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id", insertable = false, updatable = false)
    private ModelVersion currentVersion;

    @Column(name = "current_version", length = 64)
    private String currentVersionName;

    @Column(name = "model_file_path", length = 255)
    private String modelFilePath;

    @Column(name = "config_file_path", length = 255)
    private String configFilePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ModelVersion> versions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AiModel() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public Long getCurrentVersionId() {
        return currentVersionId;
    }

    public void setCurrentVersionId(Long currentVersionId) {
        this.currentVersionId = currentVersionId;
    }

    public ModelVersion getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(ModelVersion currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getCurrentVersionName() {
        return currentVersionName;
    }

    public void setCurrentVersionName(String currentVersionName) {
        this.currentVersionName = currentVersionName;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public void setModelFilePath(String modelFilePath) {
        this.modelFilePath = modelFilePath;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
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

    public List<ModelVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ModelVersion> versions) {
        this.versions = versions;
    }

    /**
     * 添加版本
     */
    public void addVersion(ModelVersion version) {
        versions.add(version);
        version.setModel(this);
    }

    /**
     * 移除版本
     */
    public void removeVersion(ModelVersion version) {
        versions.remove(version);
        version.setModel(null);
    }

    /**
     * 是否已部署
     */
    public boolean isDeployed() {
        return "deployed".equals(status);
    }

    /**
     * 是否激活中
     */
    public boolean isActive() {
        return "active".equals(status) || "deployed".equals(status);
    }

    @Override
    public String toString() {
        return "AiModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", version='" + currentVersionName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
