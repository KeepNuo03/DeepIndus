package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.DefectReviewRequest;
import com.induscore.service.DefectService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 缺陷详情与复核接口。
 */
@RestController
@RequestMapping("/v1/defect")
public class DefectController {
    private final DefectService defectService;

    public DefectController(DefectService defectService) {
        this.defectService = defectService;
    }

    /**
     * 获取缺陷详情。{id} 支持数字主键（与列表 GET /v1/records/query 返回的 record.id 一致）或检测编号 detectionNo。
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getDefectDetail(@PathVariable String id) {
        String decodedId = UriUtils.decode(id, StandardCharsets.UTF_8);
        return ApiResponse.success(defectService.getDefectDetail(decodedId));
    }

    /**
     * 获取缺陷检测图像。用于详情页「检测图像」展示，无图时 404。
     */
    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getDefectImage(@PathVariable String id) {
        String decodedId = UriUtils.decode(id, StandardCharsets.UTF_8);
        return defectService.getDefectImageBytes(decodedId)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                        .body(bytes))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 提交人工复核结果。
     */
    @PostMapping("/{id}/review")
    public ApiResponse<Map<String, Object>> reviewDefect(
            @PathVariable Long id,
            @RequestBody DefectReviewRequest request
    ) {
        defectService.reviewDefect(id, request);
        return ApiResponse.success("提交成功", Map.of());
    }
}
