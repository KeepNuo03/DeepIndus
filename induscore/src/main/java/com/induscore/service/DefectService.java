package com.induscore.service;

import com.induscore.dto.DefectReviewRequest;

import java.util.Map;
import java.util.Optional;

/**
 * 缺陷详情业务接口。
 */
public interface DefectService {
    /**
     * 获取缺陷详情。
     */
    Map<String, Object> getDefectDetail(String id);

    /**
     * 按记录主键获取检测图像字节（来自 exports/defects/{id}.jpg），无文件时为空。
     */
    Optional<byte[]> getDefectImageBytes(String id);

    /**
     * 人工复核缺陷。
     */
    void reviewDefect(Long id, DefectReviewRequest request);
}
