package com.induscore.repository;

import com.induscore.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 工位数据访问层。
 */
public interface StationRepository extends JpaRepository<Station, Long> {
    /**
     * 查询某生产线的工位列表。
     */
    List<Station> findByProductionLineId(Long productionLineId);
}
