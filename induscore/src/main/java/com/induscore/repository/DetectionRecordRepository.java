package com.induscore.repository;

import com.induscore.model.DetectionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 检测记录数据访问层。
 */
public interface DetectionRecordRepository extends JpaRepository<DetectionRecord, Long>, JpaSpecificationExecutor<DetectionRecord> {

    /**
     * 按检测编号查询记录。
     */
    Optional<DetectionRecord> findByDetectionNo(String detectionNo);

    /**
     * 统计时间范围内的记录数量。
     */
    @Query("select count(r) from DetectionRecord r where r.timestamp between :start and :end")
    long countBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 统计指定状态在时间范围内的数量。
     */
    @Query("select count(r) from DetectionRecord r where r.status = :status and r.timestamp between :start and :end")
    long countByStatusBetween(
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 统计缺陷类型分布。
     */
    @Query("select r.defectType, count(r) from DetectionRecord r " +
            "where r.defectType is not null and r.timestamp between :start and :end " +
            "group by r.defectType")
    List<Object[]> countByDefectTypeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 统计置信度平均值。
     */
    @Query("select avg(r.confidence) from DetectionRecord r where r.timestamp between :start and :end")
    Double avgConfidenceBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 查询最近严重报警记录。
     */
    List<DetectionRecord> findTop10BySeverityOrderByTimestampDesc(String severity);

    /**
     * 查询相似缺陷记录（同类型，排除自身）。
     */
    List<DetectionRecord> findTop5ByDefectTypeAndIdNotOrderByTimestampDesc(String defectType, Long id);

    /**
     * 查询所有记录并预加载产品和生产线信息（解决懒加载问题）。
     */
    @Query("SELECT r FROM DetectionRecord r LEFT JOIN FETCH r.product LEFT JOIN FETCH r.productionLineEntity ORDER BY r.timestamp DESC")
    List<DetectionRecord> findAllWithProductAndLine();

    /**
     * 分页查询并预加载产品和生产线信息。
     */
    @Query(value = "SELECT r FROM DetectionRecord r LEFT JOIN FETCH r.product LEFT JOIN FETCH r.productionLineEntity",
           countQuery = "SELECT COUNT(r) FROM DetectionRecord r")
    Page<DetectionRecord> findAllWithProductAndLine(Pageable pageable);
}
