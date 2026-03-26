package com.induscore.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 生产线实体（production_lines 表）。
 */
@Entity
@Table(name = "production_lines")
public class ProductionLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 生产线主键
     */
    private Long id;

    @Column(nullable = false, length = 64)
    /**
     * 生产线名称
     */
    private String name;

    @Column(nullable = false, length = 32)
    /**
     * 状态（running/maintenance/stopped）
     */
    private String status;

    @Column(length = 128)
    /**
     * 位置描述
     */
    private String location;

    @Column(length = 64)
    /**
     * 班次
     */
    private String shift;

    @Column(name = "running_time", length = 32)
    /**
     * 运行时长显示文本
     */
    private String runningTime;

    @Column(name = "today_output")
    /**
     * 今日产量
     */
    private Integer todayOutput;

    @Column(name = "target_output")
    /**
     * 目标产量
     */
    private Integer targetOutput;

    @Column(name = "qualified_count")
    /**
     * 良品数
     */
    private Integer qualifiedCount;

    @Column(name = "defect_count")
    /**
     * 次品数
     */
    private Integer defectCount;

    @Column(name = "yield_rate")
    /**
     * 良率（%）
     */
    private Double yieldRate;

    @Column(name = "utilization_rate")
    /**
     * 稼动率（%）
     */
    private Double utilizationRate;

    @Column(name = "cycle_time")
    /**
     * 平均节拍（秒）
     */
    private Double cycleTime;

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    /**
     * 摄像头列表
     */
    private List<Camera> cameras = new ArrayList<>();

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    /**
     * 工位列表
     */
    private List<Station> stations = new ArrayList<>();

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    /**
     * 产品关联列表（多对多关系的反向映射）
     */
    private List<ProductLineRelation> productRelations = new ArrayList<>();

    public ProductionLine() {}

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    public Integer getTodayOutput() {
        return todayOutput;
    }

    public void setTodayOutput(Integer todayOutput) {
        this.todayOutput = todayOutput;
    }

    public Integer getTargetOutput() {
        return targetOutput;
    }

    public void setTargetOutput(Integer targetOutput) {
        this.targetOutput = targetOutput;
    }

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public Integer getDefectCount() {
        return defectCount;
    }

    public void setDefectCount(Integer defectCount) {
        this.defectCount = defectCount;
    }

    public Double getYieldRate() {
        return yieldRate;
    }

    public void setYieldRate(Double yieldRate) {
        this.yieldRate = yieldRate;
    }

    public Double getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(Double utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public Double getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(Double cycleTime) {
        this.cycleTime = cycleTime;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<ProductLineRelation> getProductRelations() {
        return productRelations;
    }

    public void setProductRelations(List<ProductLineRelation> productRelations) {
        this.productRelations = productRelations;
    }

    /**
     * 添加产品关联
     */
    public void addProductRelation(ProductLineRelation relation) {
        productRelations.add(relation);
        relation.setProductionLine(this);
    }

    /**
     * 移除产品关联
     */
    public void removeProductRelation(ProductLineRelation relation) {
        productRelations.remove(relation);
        relation.setProductionLine(null);
    }
}
