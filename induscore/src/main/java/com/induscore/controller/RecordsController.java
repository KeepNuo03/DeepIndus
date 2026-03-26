package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.RecordsBatchDeleteRequest;
import com.induscore.dto.RecordsExportRequest;
import com.induscore.service.RecordsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 检测记录查询与批量操作接口。
 */
@RestController
@RequestMapping("/v1/records")
public class RecordsController {
    private final RecordsService recordsService;

    public RecordsController(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    /**
     * 查询检测记录（支持分页与筛选）。
     */
    @GetMapping("/query")
    public ApiResponse<Map<String, Object>> queryRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String dateStart,
            @RequestParam(required = false) String dateEnd,
            @RequestParam(defaultValue = "all") String defectType,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(recordsService.queryRecords(
                page, pageSize, dateStart, dateEnd, defectType, status, search
        ));
    }

    /**
     * 批量导出（目前返回下载地址占位）。
     */
    @PostMapping("/export")
    public ApiResponse<Map<String, Object>> exportRecords(@RequestBody RecordsExportRequest request) {
        return ApiResponse.success(recordsService.exportRecords(request));
    }

    /**
     * 批量删除检测记录。
     */
    @DeleteMapping("/batch")
    public ApiResponse<Map<String, Object>> deleteRecords(@RequestBody RecordsBatchDeleteRequest request) {
        recordsService.deleteRecords(request);
        return ApiResponse.success("删除成功", Map.of());
    }
}
