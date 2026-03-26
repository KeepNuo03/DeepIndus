package com.induscore.config;

import com.induscore.model.*;
import com.induscore.repository.*;
import com.induscore.util.DefectTypeMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 启动时初始化基础数据（权限/角色/管理员/业务示例数据）。
 *
 * 仅用于开发和联调环境，生产环境应由运维脚本或管理后台维护。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ProductionLineRepository productionLineRepository;
    private final CameraRepository cameraRepository;
    private final StationRepository stationRepository;
    private final DetectionRecordRepository detectionRecordRepository;
    private final ProcessRecordRepository processRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            ProductionLineRepository productionLineRepository,
            CameraRepository cameraRepository,
            StationRepository stationRepository,
            DetectionRecordRepository detectionRecordRepository,
            ProcessRecordRepository processRecordRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.productionLineRepository = productionLineRepository;
        this.cameraRepository = cameraRepository;
        this.stationRepository = stationRepository;
        this.detectionRecordRepository = detectionRecordRepository;
        this.processRecordRepository = processRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1) 初始化权限与角色
        Permission userManage = ensurePermission("user_manage", "用户管理", "system", "用户与权限管理");
        Permission systemConfig = ensurePermission("system_config", "系统配置", "system", "系统配置");
        Permission recordsRead = ensurePermission("records_read", "记录查看", "records", "检测记录查看");

        Role admin = ensureRole(
                "超级管理员",
                "admin",
                "拥有系统所有权限",
                List.of(userManage, systemConfig, recordsRead)
        );
        Role production = ensureRole(
                "生产管理员",
                "production",
                "生产相关权限",
                List.of(recordsRead)
        );

        // 2) 初始化管理员账号
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            User user = new User();
            user.setUsername("admin");
            user.setEmail("admin@industry.com");
            user.setName("管理员");
            user.setAvatar("https://cdn.induscore.com/avatars/admin.png");
            user.setPasswordHash(passwordEncoder.encode("Admin@123456"));
            user.setStatus("active");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setRoles(new HashSet<>(List.of(admin, production)));
            userRepository.save(user);
        }

        // 3) 初始化生产线与检测记录示例数据
        seedProductionLines();
        seedDetectionRecords();
    }

    private Permission ensurePermission(String code, String name, String module, String description) {
        // 若权限已存在则直接返回，否则创建
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            permission.setModule(module);
            permission.setDescription(description);
            permission.setCreatedAt(LocalDateTime.now());
            permission.setUpdatedAt(LocalDateTime.now());
            return permissionRepository.save(permission);
        });
    }

    private Role ensureRole(String name, String code, String description, List<Permission> permissions) {
        // 若角色已存在则直接返回，否则创建并绑定权限
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setCode(code);
            role.setDescription(description);
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            role.setPermissions(new HashSet<>(permissions));
            return roleRepository.save(role);
        });
    }

    private void seedProductionLines() {
        // 已有数据时不重复初始化
        if (productionLineRepository.count() > 0) {
            return;
        }
        ProductionLine line = new ProductionLine();
        line.setName("生产线 A-01");
        line.setStatus("running");
        line.setLocation("车间一 A区");
        line.setShift("早班 08:00-16:00");
        line.setRunningTime("6h 42m");
        line.setTodayOutput(2840);
        line.setTargetOutput(3200);
        line.setQualifiedCount(2828);
        line.setDefectCount(12);
        line.setYieldRate(99.58);
        line.setUtilizationRate(88.75);
        line.setCycleTime(12.5);
        productionLineRepository.save(line);

        Camera camera = new Camera();
        camera.setProductionLine(line);
        camera.setName("CAM-A01-01");
        camera.setPosition("入料口");
        camera.setOnline(true);
        camera.setIp("192.168.1.101");
        cameraRepository.save(camera);

        Station station = new Station();
        station.setProductionLine(line);
        station.setName("工位1");
        station.setStatus("working");
        stationRepository.save(station);
    }

    private void seedDetectionRecords() {
        // 已有数据时不重复初始化
        if (detectionRecordRepository.count() > 0) {
            return;
        }
        DetectionRecord record = buildRecord(
                "#DET-2026A01",
                "SN-6729-BM-01",
                "scratches",
                "critical",
                98.4,
                LocalDateTime.now().minusHours(2)
        );
        DetectionRecord record2 = buildRecord(
                "#DET-2026A02",
                "SN-6729-BM-02",
                "crazing",
                "warning",
                92.1,
                LocalDateTime.now().minusHours(5)
        );
        DetectionRecord record3 = buildRecord(
                "#DET-2026A03",
                "SN-6729-BM-03",
                "inclusion",
                "minor",
                88.6,
                LocalDateTime.now().minusHours(8)
        );
        DetectionRecord record4 = buildRecord(
                "#DET-2026A04",
                "SN-6729-BM-04",
                null,
                null,
                99.2,
                LocalDateTime.now().minusHours(1)
        );
        record4.setStatus("pass");
        record4.setProcessStatus("已完成");
        record4.setStatusNote("检测合格");
        detectionRecordRepository.saveAll(List.of(record, record2, record3, record4));

        List<ProcessRecord> processRecords = new ArrayList<>();
        for (DetectionRecord item : List.of(record, record2, record3, record4)) {
            ProcessRecord process = new ProcessRecord();
            process.setDetectionRecord(item);
            process.setType("create");
            process.setAction("AI 系统自动检测到缺陷");
            process.setOperator("System_Auto");
            process.setCreatedAt(item.getTimestamp());
            processRecords.add(process);
        }
        processRecordRepository.saveAll(processRecords);
    }

    private DetectionRecord buildRecord(
            String detectionNo,
            String serialNo,
            String defectType,
            String severity,
            double confidence,
            LocalDateTime timestamp
    ) {
        // 构造基础检测记录
        DetectionRecord record = new DetectionRecord();
        record.setDetectionNo(detectionNo);
        record.setSerialNo(serialNo);
        record.setDefectType(defectType);
        record.setDefect(DefectTypeMapper.displayName(defectType));
        record.setSeverity(severity);
        record.setConfidence(confidence);
        record.setPositionX("342px");
        record.setPositionY("156px");
        record.setArea("2.4cm²");
        record.setImpactLevel("A级");
        record.setProductionLine("流水线 A-01");
        record.setShift("早班 08:00-16:00");
        record.setModelName("YOLOv8-Industrial");
        record.setStatus("fail");
        record.setProcessStatus("待处理");
        record.setStatusNote("等待质检员人工复核确认");
        record.setImageUrl("https://cdn.induscore.com/defects/20260201/image001.jpg");
        record.setTimestamp(timestamp);
        return record;
    }
}
