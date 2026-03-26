package com.induscore.model;

import jakarta.persistence.*;

/**
 * 摄像头实体（cameras 表）。
 */
@Entity
@Table(name = "cameras")
public class Camera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 摄像头主键
     */
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    /**
     * 所属生产线
     */
    private ProductionLine productionLine;

    @Column(nullable = false, length = 64)
    /**
     * 摄像头名称
     */
    private String name;

    @Column(length = 64)
    /**
     * 位置描述
     */
    private String position;

    @Column(nullable = false)
    /**
     * 是否在线
     */
    private Boolean online;

    @Column(length = 64)
    /**
     * IP 地址
     */
    private String ip;

    public Camera() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductionLine getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
