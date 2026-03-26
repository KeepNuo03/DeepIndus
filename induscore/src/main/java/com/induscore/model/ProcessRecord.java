package com.induscore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 缺陷处理记录实体（process_records 表）。
 */
@Entity
@Table(name = "process_records")
public class ProcessRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 处理记录主键
     */
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_id", nullable = false)
    /**
     * 所属检测记录
     */
    private DetectionRecord detectionRecord;

    @Column(name = "type", length = 32)
    /**
     * 记录类型（create/review/confirm/resolve）
     */
    private String type;

    @Column(name = "action", length = 255)
    /**
     * 操作描述
     */
    private String action;

    @Column(name = "operator", length = 64)
    /**
     * 操作人
     */
    private String operator;

    @Column(name = "note", length = 255)
    /**
     * 备注
     */
    private String note;

    @Column(name = "created_at", nullable = false)
    /**
     * 记录时间
     */
    private LocalDateTime createdAt;

    public ProcessRecord() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DetectionRecord getDetectionRecord() {
        return detectionRecord;
    }

    public void setDetectionRecord(DetectionRecord detectionRecord) {
        this.detectionRecord = detectionRecord;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
