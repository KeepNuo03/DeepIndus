package com.induscore.repository;

import com.induscore.model.ProcessRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 处理记录数据访问层。
 */
public interface ProcessRecordRepository extends JpaRepository<ProcessRecord, Long> {
    /**
     * 查询某个检测记录的处理流水（按时间升序）。
     */
    List<ProcessRecord> findByDetectionRecordIdOrderByCreatedAtAsc(Long detectionId);
}
