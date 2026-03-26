package com.induscore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体（users 表）。
 */
@Entity
@Table(name = "users")
public class User {
    public User() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 用户主键
     */
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    /**
     * 用户名（唯一）
     */
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    /**
     * 邮箱（唯一）
     */
    private String email;

    @Column(nullable = false, length = 128)
    /**
     * 显示姓名
     */
    private String name;

    @Column(length = 255)
    /**
     * 头像 URL
     */
    private String avatar;

    @Column(name = "password_hash", nullable = false, length = 255)
    /**
     * 密码哈希（BCrypt）
     */
    private String passwordHash;

    @Column(nullable = false, length = 16)
    /**
     * 用户状态（active/disabled）
     */
    private String status;

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
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    /**
     * 角色集合
     */
    private Set<Role> roles = new HashSet<>();

    @Column(name = "department_id")
    /**
     * 所属部门ID
     */
    private Long departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    /**
     * 所属部门
     */
    private Department department;

    @Column(length = 20)
    /**
     * 手机号
     */
    private String phone;

    @Column(name = "last_login_at")
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    @Column(name = "login_count")
    /**
     * 登录次数
     */
    private Integer loginCount = 0;

    @Column(name = "online_status")
    /**
     * 在线状态：0离线 1在线
     */
    private Byte onlineStatus = 0;

    @Column(name = "employee_no", length = 64)
    /**
     * 员工编号
     */
    private String employeeNo;

    @Column(length = 128)
    /**
     * 职位
     */
    private String position;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Byte getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(Byte onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * 是否在线
     */
    public boolean isOnline() {
        return onlineStatus != null && onlineStatus == 1;
    }

    /**
     * 是否启用
     */
    public boolean isActive() {
        return "active".equals(status);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", departmentId=" + departmentId +
                '}';
    }
}
