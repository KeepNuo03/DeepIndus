package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.*;
import com.induscore.model.*;
import com.induscore.repository.*;
import com.induscore.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品管理业务实现。
 */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductDefectConfigRepository defectConfigRepository;
    private final ProductLineRelationRepository lineRelationRepository;
    private final ProductionLineRepository productionLineRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductDefectConfigRepository defectConfigRepository,
            ProductLineRelationRepository lineRelationRepository,
            ProductionLineRepository productionLineRepository
    ) {
        this.productRepository = productRepository;
        this.defectConfigRepository = defectConfigRepository;
        this.lineRelationRepository = lineRelationRepository;
        this.productionLineRepository = productionLineRepository;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 检查型号是否重复
        if (productRepository.existsByModel(request.model())) {
            throw new ApiException(400, "产品型号已存在: " + request.model());
        }

        // 创建产品实体
        Product product = new Product();
        product.setName(request.name());
        product.setModel(request.model());
        product.setCategory(request.category());
        product.setDimensions(request.dimensions());
        product.setMaterial(request.material());
        product.setWeight(request.weight());
        product.setSurfaceTreatment(request.surfaceTreatment());
        product.setStandard(request.standard());
        product.setThreshold(request.threshold());
        product.setTargetYield(request.targetYield());
        product.setStatus(request.status() != null ? request.status() : "active");
        product.setImageUrl(request.imageUrl());
        product.setDescription(request.description());

        productRepository.save(product);

        // 保存缺陷类型配置
        if (request.defectTypes() != null && !request.defectTypes().isEmpty()) {
            for (DefectTypeConfigRequest defectReq : request.defectTypes()) {
                ProductDefectConfig config = new ProductDefectConfig();
                config.setName(defectReq.name());
                config.setThreshold(defectReq.threshold());
                config.setEnabled(defectReq.enabled() != null ? defectReq.enabled() : true);
                product.addDefectConfig(config);
            }
        }

        // 保存生产线关联
        if (request.productionLines() != null && !request.productionLines().isEmpty()) {
            for (ProductionLineRelationRequest lineReq : request.productionLines()) {
                ProductionLine line = productionLineRepository.findById(lineReq.productionLineId())
                        .orElseThrow(() -> new ApiException(404, "生产线不存在: " + lineReq.productionLineId()));

                ProductLineRelation relation = new ProductLineRelation();
                relation.setProductionLine(line);
                relation.setPriority(lineReq.priority() != null ? lineReq.priority() : 0);
                relation.setIsPrimary(lineReq.isPrimary() != null ? lineReq.isPrimary() : false);
                product.addLineRelation(relation);
            }
        }

        productRepository.save(product);
        log.info("产品创建成功: id={}, model={}", product.getId(), product.getModel());

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + id));

        // 检查型号是否与其他产品重复
        if (request.model() != null && !request.model().equals(product.getModel())) {
            if (productRepository.existsByModelAndIdNot(request.model(), id)) {
                throw new ApiException(400, "产品型号已存在: " + request.model());
            }
            product.setModel(request.model());
        }

        // 更新基本字段
        if (request.name() != null) product.setName(request.name());
        if (request.category() != null) product.setCategory(request.category());
        if (request.dimensions() != null) product.setDimensions(request.dimensions());
        if (request.material() != null) product.setMaterial(request.material());
        if (request.weight() != null) product.setWeight(request.weight());
        if (request.surfaceTreatment() != null) product.setSurfaceTreatment(request.surfaceTreatment());
        if (request.standard() != null) product.setStandard(request.standard());
        if (request.threshold() != null) product.setThreshold(request.threshold());
        if (request.targetYield() != null) product.setTargetYield(request.targetYield());
        if (request.status() != null) product.setStatus(request.status());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.description() != null) product.setDescription(request.description());

        // 更新缺陷类型配置（全量替换）
        if (request.defectTypes() != null) {
            // 清除原有配置
            product.getDefectConfigs().clear();

            for (DefectTypeConfigRequest defectReq : request.defectTypes()) {
                ProductDefectConfig config = new ProductDefectConfig();
                config.setName(defectReq.name());
                config.setThreshold(defectReq.threshold());
                config.setEnabled(defectReq.enabled() != null ? defectReq.enabled() : true);
                product.addDefectConfig(config);
            }
        }

        // 更新生产线关联（全量替换）
        if (request.productionLines() != null) {
            // 清除原有关联
            product.getLineRelations().clear();

            for (ProductionLineRelationRequest lineReq : request.productionLines()) {
                ProductionLine line = productionLineRepository.findById(lineReq.productionLineId())
                        .orElseThrow(() -> new ApiException(404, "生产线不存在: " + lineReq.productionLineId()));

                ProductLineRelation relation = new ProductLineRelation();
                relation.setProductionLine(line);
                relation.setPriority(lineReq.priority() != null ? lineReq.priority() : 0);
                relation.setIsPrimary(lineReq.isPrimary() != null ? lineReq.isPrimary() : false);
                product.addLineRelation(relation);
            }
        }

        productRepository.save(product);
        log.info("产品更新成功: id={}", id);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + id));

        // 由于配置了级联删除，直接删除产品即可
        productRepository.delete(product);
        log.info("产品删除成功: id={}", id);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + id));
        return mapToResponse(product);
    }

    @Override
    public Map<String, Object> getProducts(ProductQueryRequest query) {
        Pageable pageable = PageRequest.of(query.page() - 1, query.pageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> page = productRepository.findByFilters(
                query.category(),
                query.search(),
                query.status(),
                pageable
        );

        List<ProductResponse> products = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("products", products);
        data.put("total", page.getTotalElements());
        data.put("page", query.page());
        data.put("pageSize", query.pageSize());

        return data;
    }

    @Override
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByStatusOrderByCreatedAtDesc("active")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long id, String status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + id));

        if (!"active".equals(status) && !"inactive".equals(status)) {
            throw new ApiException(400, "无效的状态值: " + status);
        }

        product.setStatus(status);
        productRepository.save(product);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse addProductionLine(Long productId, Long lineId, Integer priority, Boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + productId));

        ProductionLine line = productionLineRepository.findById(lineId)
                .orElseThrow(() -> new ApiException(404, "生产线不存在: " + lineId));

        // 检查是否已关联
        if (lineRelationRepository.existsByProductIdAndProductionLineId(productId, lineId)) {
            throw new ApiException(400, "该产品已与该生产线关联");
        }

        // 如果设置为新的主生产线，先清除其他主生产线
        if (Boolean.TRUE.equals(isPrimary)) {
            lineRelationRepository.findPrimaryRelationByProductId(productId)
                    .ifPresent(primaryRel -> {
                        primaryRel.setIsPrimary(false);
                        lineRelationRepository.save(primaryRel);
                    });
        }

        ProductLineRelation relation = new ProductLineRelation();
        relation.setProduct(product);
        relation.setProductionLine(line);
        relation.setPriority(priority != null ? priority : 0);
        relation.setIsPrimary(isPrimary != null ? isPrimary : false);

        lineRelationRepository.save(relation);

        return getProductById(productId);
    }

    @Override
    @Transactional
    public ProductResponse removeProductionLine(Long productId, Long lineId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + productId));

        ProductLineRelation relation = lineRelationRepository
                .findByProductIdAndProductionLineId(productId, lineId)
                .orElseThrow(() -> new ApiException(404, "关联不存在"));

        product.removeLineRelation(relation);
        lineRelationRepository.delete(relation);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse setPrimaryProductionLine(Long productId, Long lineId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(404, "产品不存在: " + productId));

        // 验证关联存在
        ProductLineRelation relation = lineRelationRepository
                .findByProductIdAndProductionLineId(productId, lineId)
                .orElseThrow(() -> new ApiException(404, "该产品未与指定生产线关联"));

        // 清除其他主生产线
        lineRelationRepository.findPrimaryRelationByProductId(productId)
                .ifPresent(primaryRel -> {
                    if (!primaryRel.getId().equals(relation.getId())) {
                        primaryRel.setIsPrimary(false);
                        lineRelationRepository.save(primaryRel);
                    }
                });

        // 设置新的主生产线
        relation.setIsPrimary(true);
        lineRelationRepository.save(relation);

        return getProductById(productId);
    }

    @Override
    public List<ProductResponse> getProductsByProductionLine(Long lineId) {
        return productRepository.findByProductionLineId(lineId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isModelAvailable(String model, Long excludeId) {
        if (excludeId == null) {
            return !productRepository.existsByModel(model);
        }
        return !productRepository.existsByModelAndIdNot(model, excludeId);
    }

    /**
     * 将Product实体转换为响应DTO
     */
    private ProductResponse mapToResponse(Product product) {
        // 转换缺陷配置
        List<DefectTypeConfigResponse> defectTypes = product.getDefectConfigs().stream()
                .map(config -> new DefectTypeConfigResponse(
                        config.getId(),
                        config.getName(),
                        config.getThreshold(),
                        config.getEnabled()
                ))
                .collect(Collectors.toList());

        // 转换生产线关联
        List<ProductionLineRelationResponse> productionLines = product.getLineRelations().stream()
                .map(relation -> new ProductionLineRelationResponse(
                        relation.getId(),
                        relation.getProductionLine().getId(),
                        relation.getProductionLine().getName(),
                        relation.getProductionLine().getStatus(),
                        relation.getProductionLine().getLocation(),
                        relation.getPriority(),
                        relation.getIsPrimary()
                ))
                .collect(Collectors.toList());

        // 统计字段（后续可根据实际业务数据计算）
        Integer todayOutput = 0;
        Double yieldRate = product.getTargetYield();
        Integer batchCount = 0;
        Long totalOutput = 0L;
        Double avgYieldRate = product.getTargetYield();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getModel(),
                product.getCategory(),
                product.getDimensions(),
                product.getMaterial(),
                product.getWeight(),
                product.getSurfaceTreatment(),
                product.getStandard(),
                product.getThreshold(),
                product.getTargetYield(),
                product.getStatus(),
                product.getImageUrl(),
                product.getDescription(),
                defectTypes,
                productionLines,
                todayOutput,
                yieldRate,
                batchCount,
                totalOutput,
                avgYieldRate,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
