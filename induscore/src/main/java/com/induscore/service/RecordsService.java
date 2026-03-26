package com.induscore.service;

import com.induscore.dto.RecordsBatchDeleteRequest;
import com.induscore.dto.RecordsExportRequest;

import java.util.Map;

/**
 * 检测记录业务接口。
 */
public interface RecordsService {
    /**
     * 分页查询检测记录。
     */
    Map<String, Object> queryRecords(
            int page,
            int pageSize,
            String dateStart,
            String dateEnd,
            String defectType,
            String status,
            String search
    );

    /**
     * 批量导出（返回下载地址）。
     */
    Map<String, Object> exportRecords(RecordsExportRequest request);

    /**
     * 批量删除。
     */
    void deleteRecords(RecordsBatchDeleteRequest request);
}
