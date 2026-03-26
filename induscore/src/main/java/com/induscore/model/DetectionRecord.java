package com.induscore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检测记录实体（detection_records 表）。
 */
@Entity
@Table(name = "detection_records")
public class DetectionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 记录主键
     */
    private Long id;

    @Column(name = "detection_no", nullable = false, unique = true, length = 64)
    /**
     * 检测编号（唯一）
     */
    private String detectionNo;

    @Column(name = "serial_no", nullable = false, length = 64)
    /**
     * 产品序列号
     */
    private String serialNo;

    @Column(name = "defect", length = 255)
    /**
     * 缺陷描述
     */
    private String defect;

    @Column(name = "defect_type", length = 64)
    /**
     * 缺陷类型（scratch/oversize/crack 等）
     */
    private String defectType;

    @Column(name = "severity", length = 32)
    /**
     * 严重程度（critical/warning/minor）
     */
    private String severity;

    @Column(name = "confidence")
    /**
     * 置信度（0-100）
     */
    private Double confidence;

    @Column(name = "position_x")
    /**
     * 缺陷位置 X
     */
    private String positionX;

    @Column(name = "position_y")
    /**
     * 缺陷位置 Y
     */
    private String positionY;

    @Column(name = "area")
    /**
     * 缺陷面积
     */
    private String area;

    @Column(name = "impact_level", length = 16)
    /**
     * 影响等级（A/B/C）
     */
    private String impactLevel;

    @Column(name = "production_line", length = 64)
    /**
     * 生产线名称（冗余字段，便于查询显示）
     */
    private String productionLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id")
    /**
     * 关联的生产线实体
     */
    private ProductionLine productionLineEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    /**
     * 关联的产品实体
     */
    private Product product;

    @Column(name = "shift", length = 64)
    /**
     * 班次
     */
    private String shift;

    @Column(name = "model_name", length = 64)
    /**
     * 模型名称
     */
    private String modelName;

    @Column(name = "status", length = 16)
    /**
     * 检测结果（pass/fail）
     */
    private String status;

    @Column(name = "process_status", length = 16)
    /**
     * 处理状态（待处理/处理中/已完成）
     */
    private String processStatus;

    @Column(name = "status_note", length = 255)
    /**
     * 处理说明
     */
    private String statusNote;

    @Column(name = "image_url", length = 255)
    /**
     * 缺陷图片 URL
     */
    private String imageUrl;

    @Column(name = "timestamp", nullable = false)
    /**
     * 检测时间
     */
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "detectionRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    /**
     * 处理记录流水
     */
    private List<ProcessRecord> processRecords = new ArrayList<>();

    public DetectionRecord() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetectionNo() {
        return detectionNo;
    }

    public void setDetectionNo(String detectionNo) {
        this.detectionNo = detectionNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getDefect() {
        return defect;
    }

    public void setDefect(String defect) {
        this.defect = defect;
    }

    public String getDefectType() {
        return defectType;
    }

    public void setDefectType(String defectType) {
        this.defectType = defectType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getPositionX() {
        return positionX;
    }

    public void setPositionX(String positionX) {
        this.positionX = positionX;
    }

    public String getPositionY() {
        return positionY;
    }

    public void setPositionY(String positionY) {
        this.positionY = positionY;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getImpactLevel() {
        return impactLevel;
    }

    public void setImpactLevel(String impactLevel) {
        this.impactLevel = impactLevel;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public ProductionLine getProductionLineEntity() {
        return productionLineEntity;
    }

    public void setProductionLineEntity(ProductionLine productionLineEntity) {
        this.productionLineEntity = productionLineEntity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getStatusNote() {
        return statusNote;
    }

    public void setStatusNote(String statusNote) {
        this.statusNote = statusNote;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<ProcessRecord> getProcessRecords() {
        return processRecords;
    }

    public void setProcessRecords(List<ProcessRecord> processRecords) {
        this.processRecords = processRecords;
    }
}
