package com.induscore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 产品创建/更新请求 DTO。
 */
public record ProductRequest(
        Long id,
        @NotBlank(message = "产品名称不能为空") String name,
        @NotBlank(message = "产品型号不能为空") String model,
        String category, // steel/aluminum/plastic
        String dimensions,
        String material,
        Double weight,
        String surfaceTreatment,
        String standard,
        Double threshold,
        Double targetYield,
        String status, // active/inactive
        String imageUrl,
        String description,
        @Valid List<DefectTypeConfigRequest> defectTypes,
        @Valid List<ProductionLineRelationRequest> productionLines
) {}
