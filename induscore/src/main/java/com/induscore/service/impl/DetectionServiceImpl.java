package com.induscore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.common.ApiException;
import com.induscore.dto.DetectionControlRequest;
import com.induscore.model.DetectionRecord;
import com.induscore.model.ProcessRecord;
import com.induscore.model.Product;
import com.induscore.model.ProductionLine;
import com.induscore.repository.DetectionRecordRepository;
import com.induscore.repository.ProcessRecordRepository;
import com.induscore.repository.ProductRepository;
import com.induscore.repository.ProductionLineRepository;
import com.induscore.service.DetectionService;
import com.induscore.service.ModelService;
import com.induscore.service.YoloService;
import com.induscore.websocket.DetectionWebSocketHub;
import com.induscore.util.DefectTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * 实时检测业务实现。
 */
@Service
public class DetectionServiceImpl implements DetectionService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(DetectionServiceImpl.class);

    /** 当前正在运行视频流检测的摄像头 ID 集合（start/stop 控制）。 */
    private final Set<String> runningCameras = ConcurrentHashMap.newKeySet();

    private final DetectionRecordRepository recordRepository;
    private final ProcessRecordRepository processRecordRepository;
    private final ProductRepository productRepository;
    private final ProductionLineRepository productionLineRepository;
    private final YoloService yoloService;
    private final DetectionWebSocketHub webSocketHub;
    private final ObjectMapper objectMapper;
    private final ModelService modelService;
    private final String exportPath;
    private final String exportBaseUrl;

    public DetectionServiceImpl(
            DetectionRecordRepository recordRepository,
            ProcessRecordRepository processRecordRepository,
            ProductRepository productRepository,
            ProductionLineRepository productionLineRepository,
            YoloService yoloService,
            DetectionWebSocketHub webSocketHub,
            ObjectMapper objectMapper,
            ModelService modelService,
            @Value("${file.export.path}") String exportPath,
            @Value("${file.export.base-url}") String exportBaseUrl
    ) {
        this.recordRepository = recordRepository;
        this.processRecordRepository = processRecordRepository;
        this.productRepository = productRepository;
        this.productionLineRepository = productionLineRepository;
        this.yoloService = yoloService;
        this.webSocketHub = webSocketHub;
        this.objectMapper = objectMapper;
        this.modelService = modelService;
        this.exportPath = exportPath;
        this.exportBaseUrl = exportBaseUrl.endsWith("/") ? exportBaseUrl : exportBaseUrl + "/";
    }

    @Override
    public Map<String, Object> getRealtimeStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        long total = recordRepository.countBetween(start, end);
        long warning = recordRepository.countByStatusBetween("fail", start, end);
        double avgConfidence = averageConfidence(start, end);

        // 从模型服务获取当前部署的模型版本
        String deployedModelVersion = modelService.getDeployedModelName();

        Map<String, Object> data = new HashMap<>();
        data.put("todayTargets", total);
        data.put("warningCount", warning);
        data.put("avgConfidence", avgConfidence);
        data.put("modelVersion", deployedModelVersion);
        return data;
    }

    @Override
    public Map<String, Object> getRealtimeRecords(int limit, int offset) {
        int safeLimit = limit <= 0 ? 10 : limit;
        int page = offset <= 0 ? 0 : offset / safeLimit;
        PageRequest pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "timestamp"));

        List<DetectionRecord> records = recordRepository.findAll(pageable).getContent();
        List<Map<String, Object>> items = new ArrayList<>();
        for (DetectionRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("type", mapRealtimeType(record));
            item.put("title", record.getDefect() == null ? "正常" : record.getDefect());
            item.put("description", formatRealtimeDescription(record));
            item.put("confidence", formatPercent(record.getConfidence()));
            item.put("timestamp", record.getTimestamp().format(DATE_TIME));
            items.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", items);
        data.put("total", recordRepository.count());
        return data;
    }

    @Override
    public Map<String, Object> getRealtimeTrend() {
        // 取最近 60 分钟，按 10 分钟划分
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(60);
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {
            LocalDateTime next = cursor.plusMinutes(10);
            if (next.isAfter(end)) {
                next = end;
            }
            labels.add(cursor.format(DateTimeFormatter.ofPattern("HH:mm")));
            counts.add(recordRepository.countBetween(cursor, next));
            cursor = next;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("timeLabels", labels);
        data.put("detectionCounts", counts);
        return data;
    }

    @Override
    public Map<String, Object> controlDetection(DetectionControlRequest request) {
        if (request == null || request.action() == null) {
            throw new ApiException(400, "action 不能为空");
        }
        String action = request.action().trim().toLowerCase();
        String cameraId = request.cameraId() != null ? request.cameraId().trim() : "";
        // 本机摄像头（browser）仅做状态同步，不加入 runningCameras，避免定时任务用占位图重复推流
        boolean isBrowserCamera = cameraId != null && cameraId.toLowerCase().startsWith("browser");
        if ("start".equals(action)) {
            if (!cameraId.isEmpty() && !isBrowserCamera) {
                runningCameras.add(cameraId);
                log.info("视频流检测已启动: cameraId={}", cameraId);
            } else if (isBrowserCamera) {
                log.info("本机摄像头检测已启动: cameraId={}", cameraId);
            }
        } else if ("stop".equals(action)) {
            if (!cameraId.isEmpty()) {
                runningCameras.remove(cameraId);
                log.info("视频流检测已停止: cameraId={}", cameraId);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("action", request.action());
        data.put("cameraId", cameraId.isEmpty() ? request.cameraId() : cameraId);
        data.put("status", "ok");
        data.put("running", runningCameras.contains(cameraId));
        data.put("runningCameras", new ArrayList<>(runningCameras));
        return data;
    }

    @Override
    public Map<String, Object> uploadAndDetect(MultipartFile file, String serialNo) {
        // 兼容旧版本，默认使用热轧带钢(假设ID=1)和生产线A-01(假设ID=1)
        // 如果数据库中热轧带钢的ID不是1，请修改这里
        Long defaultProductId = 1L;
        Long defaultLineId = 1L;
        return uploadAndDetect(file, serialNo, defaultProductId, defaultLineId);
    }

    @Override
    public Map<String, Object> uploadAndDetect(MultipartFile file, String serialNo, Long productId, Long productionLineId) {
        log.info("离线检测上传: fileSize={}, serialNo={}, productId={}, lineId={}",
                file != null ? file.getSize() : 0, serialNo, productId, productionLineId);
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "文件不能为空");
        }
        // 获取当前部署的模型信息
        Map<String, Object> modelInfo = modelService.getDeployedModelInfo();
        String modelName = (String) modelInfo.get("name");
        String modelVersion = (String) modelInfo.get("version");
        String modelFile = (String) modelInfo.get("modelFile");

        Map<String, Object> yoloResult;
        try {
            yoloResult = yoloService.detectImage(file, modelName, modelVersion, modelFile);
        } catch (Exception e) {
            log.warn("YOLO 检测失败，仍写入一条记录便于在记录管理中查看: {}", e.getMessage());
            yoloResult = null;
        }
        byte[] imageBytes = extractImageBytes(file, yoloResult);
        Map<String, Object> recordData = saveDetectionResult(yoloResult, serialNo, imageBytes, productId, productionLineId);
        log.info("离线检测记录已写入: recordId={}", recordData.get("id"));

        try {
            webSocketHub.broadcast("detection", recordData);
            if ("danger".equals(recordData.get("type"))) {
                webSocketHub.broadcast("alert", buildAlert(recordData));
            }
        } catch (Exception e) {
            log.warn("WebSocket 推送失败，不影响记录保存: {}", e.getMessage());
        }

        Map<String, Object> data = yoloResult != null ? getMap(yoloResult, "data") : new HashMap<>();
        List<Map<String, Object>> detections = getList(data, "detections");
        boolean hasDefect = detections != null && !detections.isEmpty();

        // 返回结构与实时帧一致，便于前端在「视频流检测区域」统一展示：标注图 + 缺陷列表 + 记录
        Map<String, Object> payload = new HashMap<>();
        payload.put("yolo", yoloResult);
        payload.put("record", recordData);
        payload.put("detections", detections != null ? detections : List.of());
        payload.put("detection_count", detections != null ? detections.size() : 0);
        payload.put("has_defect", hasDefect);
        Object annotatedImage = data.get("annotated_image");
        if (annotatedImage != null && annotatedImage.toString().length() > 0) {
            payload.put("annotated_image", annotatedImage);
        }
        Object inferenceTimeMs = data.get("inference_time_ms");
        if (inferenceTimeMs != null) {
            payload.put("inference_time_ms", inferenceTimeMs);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", payload);
        return response;
    }

    @Override
    public Map<String, Object> processRealtimeFrame(MultipartFile file, String cameraId) {
        // 兼容旧版本，默认使用热轧带钢(假设ID=1)和生产线A-01(假设ID=1)
        Long defaultProductId = 1L;
        Long defaultLineId = 1L;
        return processRealtimeFrame(file, cameraId, defaultProductId, defaultLineId);
    }

    @Override
    public Map<String, Object> processRealtimeFrame(MultipartFile file, String cameraId, Long productId, Long productionLineId) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "请上传一帧图像");
        }

        // 获取当前部署的模型信息
        Map<String, Object> modelInfo = modelService.getDeployedModelInfo();
        String modelName = (String) modelInfo.get("name");
        String modelVersion = (String) modelInfo.get("version");
        String modelFile = (String) modelInfo.get("modelFile");

        Map<String, Object> yoloResult = yoloService.detectImage(file, modelName, modelVersion, modelFile);
        Map<String, Object> data = getMap(yoloResult, "data");
        List<Map<String, Object>> detections = getList(data, "detections");
        boolean hasDefect = detections != null && !detections.isEmpty();
        String serialNo = (cameraId != null && !cameraId.isBlank())
                ? "SN-" + cameraId + "-" + System.currentTimeMillis()
                : "SN-LIVE-" + System.currentTimeMillis();
        byte[] imageBytes = extractImageBytes(file, yoloResult);
        Map<String, Object> recordData = saveDetectionResult(yoloResult, serialNo, imageBytes, productId, productionLineId);

        webSocketHub.broadcast("detection", recordData);
        if ("danger".equals(recordData.get("type"))) {
            webSocketHub.broadcast("alert", buildAlert(recordData));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("detections", detections != null ? detections : List.of());
        response.put("detection_count", detections != null ? detections.size() : 0);
        response.put("has_defect", hasDefect);
        response.put("record", recordData);
        Object annotatedImage = data.get("annotated_image");
        if (annotatedImage != null && annotatedImage.toString().length() > 0) {
            response.put("annotated_image", annotatedImage);
        }
        Object inferenceTimeMs = data.get("inference_time_ms");
        if (inferenceTimeMs != null) {
            response.put("inference_time_ms", inferenceTimeMs);
        }
        return response;
    }

    /**
     * 定时任务：对已启动的摄像头进行视频流检测（拉取占位帧 → YOLO 推理 → 落库并 WebSocket 推送）。
     * 当前使用占位图模拟帧；后续可接入真实 RTSP/拉流。
     */
    @Scheduled(fixedDelayString = "${detection.realtime.intervalMs:5000}")
    public void runRealtimeDetection() {
        if (runningCameras.isEmpty()) {
            return;
        }
        byte[] frame = getPlaceholderFrame();
        if (frame == null || frame.length == 0) {
            return;
        }
        // 默认使用热轧带钢(ID=1)和生产线A-01(ID=1)
        Long defaultProductId = 1L;
        Long defaultLineId = 1L;

        // 获取当前部署的模型信息
        Map<String, Object> modelInfo = modelService.getDeployedModelInfo();
        String modelName = (String) modelInfo.get("name");
        String modelVersion = (String) modelInfo.get("version");
        String modelFile = (String) modelInfo.get("modelFile");

        for (String cameraId : List.copyOf(runningCameras)) {
            try {
                Map<String, Object> yoloResult = yoloService.detectVideoFrame(frame, modelName, modelVersion, modelFile);
                String serialNo = "SN-" + cameraId + "-" + System.currentTimeMillis();
                Map<String, Object> recordData = saveDetectionResult(yoloResult, serialNo, null, defaultProductId, defaultLineId);
                webSocketHub.broadcast("detection", recordData);
                if ("danger".equals(recordData.get("type"))) {
                    webSocketHub.broadcast("alert", buildAlert(recordData));
                }
            } catch (Exception e) {
                log.warn("视频流检测失败 cameraId={}: {}", cameraId, e.getMessage());
            }
        }
    }

    /**
     * 获取一帧图像用于视频流检测。优先使用 classpath:realtime-placeholder.jpg，否则生成最小占位图。
     */
    private byte[] getPlaceholderFrame() {
        try {
            ClassPathResource resource = new ClassPathResource("realtime-placeholder.jpg");
            if (resource.exists()) {
                try (InputStream in = resource.getInputStream()) {
                    return in.readAllBytes();
                }
            }
        } catch (Exception e) {
            log.trace("未找到占位图，使用内存占位: {}", e.getMessage());
        }
        return createMinimalJpeg();
    }

    private static byte[] createMinimalJpeg() {
        try {
            BufferedImage img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    img.setRGB(x, y, 0x808080);
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (ImageIO.write(img, "jpeg", baos)) {
                return baos.toByteArray();
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(DetectionServiceImpl.class).warn("生成占位图失败: {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * 保存检测结果并可选保存检测图像（供缺陷详情页展示）。无图像时使用占位 URL。
     * 支持指定产品和生产线，并自动更新生产线的良品/次品统计。
     */
    private Map<String, Object> saveDetectionResult(Map<String, Object> yoloResult, String serialNo, byte[] imageBytes, Long productId, Long productionLineId) {
        // 使用唯一检测编号，避免并发上传导致唯一键冲突
        String detectionNo = "#DET-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String sn = serialNo == null || serialNo.isBlank() ? "SN-AUTO" : serialNo;
        LocalDateTime now = LocalDateTime.now();

        // 从模型服务获取当前部署的模型名称
        String deployedModelName = modelService.getDeployedModelName();

        DetectionRecord record = new DetectionRecord();
        record.setDetectionNo(detectionNo);
        record.setSerialNo(sn);
        record.setTimestamp(now);
        record.setModelName(deployedModelName);
        record.setShift("早班 08:00-16:00");
        record.setImageUrl(null);

        // 获取产品和生产线信息
        Product product = null;
        ProductionLine productionLine = null;
        boolean hasDefect = false;

        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                record.setProduct(product);
                log.info("检测关联产品: id={}, name={}", product.getId(), product.getName());
            }
        }

        if (productionLineId != null) {
            productionLine = productionLineRepository.findById(productionLineId).orElse(null);
            if (productionLine != null) {
                record.setProductionLineEntity(productionLine);
                record.setProductionLine(productionLine.getName());
                log.info("检测关联生产线: id={}, name={}", productionLine.getId(), productionLine.getName());
            }
        }

        // 如果没有指定生产线，默认使用硬编码的"流水线 A-01"
        if (productionLine == null) {
            record.setProductionLine("流水线 A-01");
        }

        // 解析YOLO检测结果
        if (yoloResult == null) {
            record.setDefect("检测服务异常");
            record.setDefectType(null);
            record.setStatus("fail");
            record.setProcessStatus("待处理");
            record.setStatusNote("YOLO 服务调用失败，未完成检测");
            record.setConfidence(0d);
            record.setSeverity("minor");
        } else {
            Map<String, Object> data = getMap(yoloResult, "data");
            List<Map<String, Object>> detections = getList(data, "detections");
            hasDefect = detections != null && !detections.isEmpty();

            if (hasDefect) {
                Map<String, Object> first = detections.get(0);
                String defectName = String.valueOf(first.getOrDefault("class_name", "缺陷"));
                String defectType = DefectTypeMapper.normalize(defectName);
                if (defectType == null || defectType.isBlank()) {
                    defectType = defectName != null ? defectName.trim().toLowerCase() : "unknown";
                }
                double confidence = toDouble(first.get("confidence"));
                record.setDefect(DefectTypeMapper.displayName(defectType));
                record.setDefectType(defectType);
                record.setSeverity(mapSeverity(confidence));
                record.setConfidence(confidence * 100);
                applyFirstDetectionBox(record, first);
                record.setStatus("fail");
                record.setProcessStatus("待处理");
                record.setStatusNote("等待质检员人工复核确认");
            } else {
                record.setDefect("正常");
                record.setStatus("pass");
                record.setProcessStatus("已完成");
                record.setConfidence(100d);
                record.setStatusNote("检测合格");
            }
        }

        // 保存检测记录
        try {
            recordRepository.save(record);
            log.info("detection_records 已保存: id={}, detectionNo={}", record.getId(), record.getDetectionNo());
        } catch (Exception e) {
            log.error("保存 detection_records 失败: {}", e.getMessage(), e);
            throw new ApiException(500, "记录保存失败: " + e.getMessage());
        }

        // 更新生产线的良品/次品统计
        if (productionLine != null) {
            updateProductionLineStats(productionLine, hasDefect);
        }

        // 保存检测图像
        if (imageBytes != null && imageBytes.length > 0) {
            try {
                saveDefectImage(record.getId(), imageBytes);
                record.setImageUrl(exportBaseUrl + "defects/" + record.getId() + ".jpg");
                recordRepository.save(record);
                log.info("检测图像已落盘: id={}, size={} bytes", record.getId(), imageBytes.length);
            } catch (Exception e) {
                log.warn("保存检测图像失败，详情页将无图: {}", e.getMessage(), e);
                record.setImageUrl("https://cdn.induscore.com/defects/20260201/image001.jpg");
                recordRepository.save(record);
            }
        } else {
            log.info("无图像数据可保存，recordId={}（将使用占位或详情图接口 404）", record.getId());
            record.setImageUrl("https://cdn.induscore.com/defects/20260201/image001.jpg");
            recordRepository.save(record);
        }

        // 保存检测详情到文件
        if (yoloResult != null) {
            Map<String, Object> data = getMap(yoloResult, "data");
            List<Map<String, Object>> detections = getList(data, "detections");
            writeDetectionsFile(record.getId(), detections);
        }

        // 创建处理记录
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setDetectionRecord(record);
        processRecord.setType("create");
        processRecord.setAction("AI 系统自动检测到缺陷");
        processRecord.setOperator("System_Auto");
        processRecord.setCreatedAt(record.getTimestamp());
        processRecordRepository.save(processRecord);

        return mapRealtimeRecord(record);
    }

    /**
     * 更新生产线的良品/次品统计
     */
    private void updateProductionLineStats(ProductionLine productionLine, boolean hasDefect) {
        try {
            if (hasDefect) {
                // 次品数+1
                productionLine.setDefectCount((productionLine.getDefectCount() != null ? productionLine.getDefectCount() : 0) + 1);
                log.info("生产线 {} 次品数+1，当前次品数: {}", productionLine.getName(), productionLine.getDefectCount());
            } else {
                // 良品数+1
                productionLine.setQualifiedCount((productionLine.getQualifiedCount() != null ? productionLine.getQualifiedCount() : 0) + 1);
                log.info("生产线 {} 良品数+1，当前良品数: {}", productionLine.getName(), productionLine.getQualifiedCount());
            }

            // 更新今日产量
            productionLine.setTodayOutput((productionLine.getTodayOutput() != null ? productionLine.getTodayOutput() : 0) + 1);

            // 更新良率
            int total = (productionLine.getQualifiedCount() != null ? productionLine.getQualifiedCount() : 0)
                    + (productionLine.getDefectCount() != null ? productionLine.getDefectCount() : 0);
            if (total > 0) {
                double yieldRate = (double) productionLine.getQualifiedCount() / total * 100;
                productionLine.setYieldRate(Math.round(yieldRate * 100) / 100.0);
            }

            productionLineRepository.save(productionLine);
            log.info("生产线 {} 统计已更新: 今日产量={}, 良品数={}, 次品数={}, 良率={}%",
                    productionLine.getName(),
                    productionLine.getTodayOutput(),
                    productionLine.getQualifiedCount(),
                    productionLine.getDefectCount(),
                    productionLine.getYieldRate());
        } catch (Exception e) {
            log.error("更新生产线统计失败: {}", e.getMessage(), e);
            // 不影响检测记录的保存
        }
    }

    /**
     * 将检测图像写入 exports/defects/{recordId}.jpg，供缺陷详情页通过 /files/defects/{id}.jpg 访问。
     */
    private void saveDefectImage(Long recordId, byte[] imageBytes) throws IOException {
        Path dir = Paths.get(exportPath).resolve("defects").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path file = dir.resolve(recordId + ".jpg");
        BufferedImage image = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            image = ImageIO.read(inputStream);
        } catch (Exception e) {
            log.debug("检测图像解码失败，回退原始字节: {}", e.getMessage());
        }
        if (image == null) {
            Files.write(file, imageBytes);
            return;
        }
        if (image.getColorModel() != null && image.getColorModel().hasAlpha()) {
            BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgb.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            image = rgb;
        }
        try (OutputStream out = Files.newOutputStream(file)) {
            if (!ImageIO.write(image, "jpg", out)) {
                Files.write(file, imageBytes);
            }
        }
    }

    private void writeDetectionsFile(Long recordId, List<Map<String, Object>> detections) {
        if (recordId == null) {
            return;
        }
        if (detections == null) {
            detections = Collections.emptyList();
        }
        Path dir = Paths.get(exportPath).resolve("defects").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(recordId + ".json");
            Map<String, Object> payload = new HashMap<>();
            payload.put("detections", detections);
            objectMapper.writeValue(file.toFile(), payload);
        } catch (Exception e) {
            log.debug("写入 detections 元数据失败: {}", e.getMessage());
        }
    }

    private void applyFirstDetectionBox(DetectionRecord record, Map<String, Object> detection) {
        if (record == null || detection == null) {
            return;
        }
        Object bboxObj = detection.get("bbox");
        if (!(bboxObj instanceof Map<?, ?> raw)) {
            return;
        }
        Map<String, Object> bbox = new HashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                bbox.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        double x1 = toDouble(bbox.get("x1"));
        double y1 = toDouble(bbox.get("y1"));
        double x2 = toDouble(bbox.get("x2"));
        double y2 = toDouble(bbox.get("y2"));
        double width = Math.max(0, x2 - x1);
        double height = Math.max(0, y2 - y1);
        record.setPositionX(formatNumber(x1));
        record.setPositionY(formatNumber(y1));
        record.setArea(formatNumber(width * height));
    }

    private String formatNumber(double value) {
        return String.valueOf(Math.round(value));
    }

    /**
     * 优先使用上传原图（保证落盘），其次 YOLO 标注图 base64，供缺陷详情展示。
     */
    private byte[] extractImageBytes(MultipartFile file, Map<String, Object> yoloResult) {
        if (file != null && !file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                if (bytes != null && bytes.length > 0) {
                    return bytes;
                }
            } catch (IOException e) {
                log.warn("读取上传文件失败: {}", e.getMessage());
            }
        }
        if (yoloResult != null) {
            Map<String, Object> data = getMap(yoloResult, "data");
            Object annotated = data.get("annotated_image");
            if (annotated != null && annotated.toString().length() > 0) {
                String base64 = annotated.toString().trim();
                if (base64.contains(",")) {
                    base64 = base64.substring(base64.indexOf(',') + 1);
                }
                try {
                    return Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.debug("annotated_image base64 解码失败: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    private Map<String, Object> mapRealtimeRecord(DetectionRecord record) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", record.getId());
        item.put("type", mapRealtimeType(record));
        item.put("title", record.getDefect() == null ? "正常" : record.getDefect());
        item.put("description", formatRealtimeDescription(record));
        item.put("confidence", formatPercent(record.getConfidence()));
        item.put("timestamp", record.getTimestamp().format(DATE_TIME));
        return item;
    }

    private String formatRealtimeDescription(DetectionRecord record) {
        String line = record.getProductionLine() == null ? "生产线" : record.getProductionLine();
        return record.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                " " + line + " " + record.getSerialNo();
    }

    private String mapRealtimeType(DetectionRecord record) {
        if ("pass".equalsIgnoreCase(record.getStatus())) {
            return "normal";
        }
        String severity = record.getSeverity();
        if ("critical".equalsIgnoreCase(severity)) {
            return "danger";
        }
        return "warning";
    }

    private Map<String, Object> buildAlert(Map<String, Object> recordData) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", recordData.get("id"));
        alert.put("severity", "critical");
        alert.put("title", recordData.get("title"));
        alert.put("description", recordData.get("description"));
        alert.put("time", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        alert.put("icon", "material-symbols:warning-rounded");
        return alert;
    }

    private String formatPercent(Double value) {
        if (value == null) {
            return "0%";
        }
        return String.format("%.0f%%", value);
    }

    private String mapSeverity(double confidence) {
        if (confidence >= 0.9) {
            return "critical";
        }
        if (confidence >= 0.7) {
            return "warning";
        }
        return "minor";
    }

    private double averageConfidence(LocalDateTime start, LocalDateTime end) {
        Double avg = recordRepository.avgConfidenceBetween(start, end);
        if (avg == null) {
            return 0;
        }
        return Math.round(avg * 10d) / 10d;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return Collections.emptyList();
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
