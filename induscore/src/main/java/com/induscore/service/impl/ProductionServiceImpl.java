package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.model.Camera;
import com.induscore.model.Product;
import com.induscore.model.ProductLineRelation;
import com.induscore.model.ProductionLine;
import com.induscore.model.Station;
import com.induscore.repository.CameraRepository;
import com.induscore.repository.ProductLineRelationRepository;
import com.induscore.repository.ProductRepository;
import com.induscore.repository.ProductionLineRepository;
import com.induscore.repository.StationRepository;
import com.induscore.service.ProductionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生产线业务实现。
 */
@Service
public class ProductionServiceImpl implements ProductionService {

    private static final Logger log = LoggerFactory.getLogger(ProductionServiceImpl.class);

    private final ProductionLineRepository productionLineRepository;
    private final CameraRepository cameraRepository;
    private final StationRepository stationRepository;
    private final ProductRepository productRepository;
    private final ProductLineRelationRepository productLineRelationRepository;

    public ProductionServiceImpl(
            ProductionLineRepository productionLineRepository,
            CameraRepository cameraRepository,
            StationRepository stationRepository,
            ProductRepository productRepository,
            ProductLineRelationRepository productLineRelationRepository
    ) {
        this.productionLineRepository = productionLineRepository;
        this.cameraRepository = cameraRepository;
        this.stationRepository = stationRepository;
        this.productRepository = productRepository;
        this.productLineRelationRepository = productLineRelationRepository;
    }

    @Override
    public Map<String, Object> getProductionLines() {
        // 读取全部生产线并统计
        List<ProductionLine> lines = productionLineRepository.findAll();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", lines.size());
        statistics.put("running", lines.stream().filter(l -> "running".equalsIgnoreCase(l.getStatus())).count());
        statistics.put("maintenance", lines.stream().filter(l -> "maintenance".equalsIgnoreCase(l.getStatus())).count());
        statistics.put("todayProduction", lines.stream().mapToInt(l -> safeInt(l.getTodayOutput())).sum());
        statistics.put("avgYield", calculateAvgYield(lines));

        List<Map<String, Object>> lineItems = new ArrayList<>();
        // 组装生产线详情（包含摄像头/工位）
        for (ProductionLine line : lines) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", line.getId());
            item.put("name", line.getName());
            item.put("status", line.getStatus());
            item.put("location", line.getLocation());
            item.put("shift", line.getShift());
            item.put("runningTime", line.getRunningTime());
            item.put("todayOutput", line.getTodayOutput());
            item.put("targetOutput", line.getTargetOutput());
            item.put("qualifiedCount", line.getQualifiedCount());
            item.put("defectCount", line.getDefectCount());
            item.put("yieldRate", line.getYieldRate());
            item.put("utilizationRate", line.getUtilizationRate());
            item.put("cycleTime", line.getCycleTime());

            item.put("cameras", mapCameras(cameraRepository.findByProductionLineId(line.getId())));
            item.put("stations", mapStations(stationRepository.findByProductionLineId(line.getId())));

            lineItems.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("statistics", statistics);
        data.put("lines", lineItems);
        return data;
    }

    private List<Map<String, Object>> mapCameras(List<Camera> cameras) {
        // 摄像头字段映射为前端结构
        List<Map<String, Object>> list = new ArrayList<>();
        for (Camera camera : cameras) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", camera.getId());
            map.put("name", camera.getName());
            map.put("position", camera.getPosition());
            map.put("online", camera.getOnline());
            map.put("ip", camera.getIp());
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> mapStations(List<Station> stations) {
        // 工位字段映射为前端结构
        List<Map<String, Object>> list = new ArrayList<>();
        for (Station station : stations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", station.getId());
            map.put("name", station.getName());
            map.put("status", station.getStatus());
            list.add(map);
        }
        return list;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double calculateAvgYield(List<ProductionLine> lines) {
        // 平均良率（保留两位）
        if (lines.isEmpty()) {
            return 0;
        }
        double sum = lines.stream().mapToDouble(l -> l.getYieldRate() == null ? 0 : l.getYieldRate()).sum();
        return Math.round(sum / lines.size() * 100) / 100d;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByProductionLine(Long productionLineId) {
        // 先检查生产线是否存在
        ProductionLine productionLine = productionLineRepository.findById(productionLineId)
                .orElseThrow(() -> new ApiException(404, "生产线不存在: " + productionLineId));

        // 使用 ProductRepository 的 JOIN 查询，避免懒加载问题
        return productRepository.findByProductionLineId(productionLineId);
    }

    @Override
    @Transactional
    public void addProductToProductionLine(Long productionLineId, Long productId) {
        ProductionLine productionLine = productionLineRepository.findById(productionLineId)
                .orElseThrow(() -> new ApiException(404, "生产线不存在: " + productionLineId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + productId));

        // 检查是否已关联
        if (productLineRelationRepository.existsByProductIdAndProductionLineId(productId, productionLineId)) {
            throw new ApiException(400, "该产品已与该生产线关联");
        }

        ProductLineRelation relation = new ProductLineRelation();
        relation.setProduct(product);
        relation.setProductionLine(productionLine);
        relation.setPriority(0);
        relation.setIsPrimary(false);
        relation.setCreatedAt(LocalDateTime.now());
        relation.setUpdatedAt(LocalDateTime.now());

        productLineRelationRepository.save(relation);
        log.info("产品 {} 已添加到生产线 {}", product.getName(), productionLine.getName());
    }

    @Override
    @Transactional
    public void removeProductFromProductionLine(Long productionLineId, Long productId) {
        ProductionLine productionLine = productionLineRepository.findById(productionLineId)
                .orElseThrow(() -> new ApiException(404, "生产线不存在: " + productionLineId));

        ProductLineRelation relation = productLineRelationRepository
                .findByProductIdAndProductionLineId(productId, productionLineId)
                .orElseThrow(() -> new ApiException(404, "该产品与生产线无关联"));

        productionLine.removeProductRelation(relation);
        productLineRelationRepository.delete(relation);
        log.info("产品已从生产线 {} 移除", productionLine.getName());
    }
}
