package com.induscore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 权限实体（permissions 表）。
 */
@Entity
@Table(name = "permissions")
public class Permission {
    public Permission() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 权限主键
     */
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    /**
     * 权限编码（唯一）
     */
    private String code;

    @Column(nullable = false, length = 128)
    /**
     * 权限名称
     */
    private String name;

    @Column(length = 64)
    /**
     * 所属模块
     */
    private String module;

    @Column(length = 255)
    /**
     * 权限描述
     */
    private String description;

    @Column(name = "created_at", nullable = false)
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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
}
