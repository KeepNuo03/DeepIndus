package com.induscore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体（roles 表）。
 */
@Entity
@Table(name = "roles")
public class Role {
    public Role() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 角色主键
     */
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    /**
     * 角色名称
     */
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    /**
     * 角色编码（唯一）
     */
    private String code;

    @Column(length = 255)
    /**
     * 角色描述
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    /**
     * 权限集合
     */
    private Set<Permission> permissions = new HashSet<>();

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

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
