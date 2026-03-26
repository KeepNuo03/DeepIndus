package com.induscore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 产品实体（products 表）。
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 64, unique = true)
    private String model;

    @Column(length = 32)
    private String category; // steel/aluminum/plastic

    @Column(length = 128)
    private String dimensions;

    @Column(length = 64)
    private String material;

    private Double weight;

    @Column(name = "surface_treatment", length = 64)
    private String surfaceTreatment;

    @Column(length = 128)
    private String standard;

    private Double threshold;

    @Column(name = "target_yield")
    private Double targetYield;

    @Column(nullable = false, length = 16)
    private String status = "active"; // active/inactive

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(length = 512)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDefectConfig> defectConfigs = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductLineRelation> lineRelations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Product() {}

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getSurfaceTreatment() {
        return surfaceTreatment;
    }

    public void setSurfaceTreatment(String surfaceTreatment) {
        this.surfaceTreatment = surfaceTreatment;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getTargetYield() {
        return targetYield;
    }

    public void setTargetYield(Double targetYield) {
        this.targetYield = targetYield;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<ProductDefectConfig> getDefectConfigs() {
        return defectConfigs;
    }

    public void setDefectConfigs(List<ProductDefectConfig> defectConfigs) {
        this.defectConfigs = defectConfigs;
    }

    public List<ProductLineRelation> getLineRelations() {
        return lineRelations;
    }

    public void setLineRelations(List<ProductLineRelation> lineRelations) {
        this.lineRelations = lineRelations;
    }

    /**
     * 添加缺陷配置
     */
    public void addDefectConfig(ProductDefectConfig config) {
        defectConfigs.add(config);
        config.setProduct(this);
    }

    /**
     * 移除缺陷配置
     */
    public void removeDefectConfig(ProductDefectConfig config) {
        defectConfigs.remove(config);
        config.setProduct(null);
    }

    /**
     * 添加生产线关联
     */
    public void addLineRelation(ProductLineRelation relation) {
        lineRelations.add(relation);
        relation.setProduct(this);
    }

    /**
     * 移除生产线关联
     */
    public void removeLineRelation(ProductLineRelation relation) {
        lineRelations.remove(relation);
        relation.setProduct(null);
    }
}
