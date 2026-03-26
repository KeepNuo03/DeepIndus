package com.induscore.model;

import jakarta.persistence.*;

/**
 * 工位实体（stations 表）。
 */
@Entity
@Table(name = "stations")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 工位主键
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
     * 工位名称
     */
    private String name;

    @Column(nullable = false, length = 32)
    /**
     * 工位状态（working/idle/maintenance）
     */
    private String status;

    public Station() {}

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
