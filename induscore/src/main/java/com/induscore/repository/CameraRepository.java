package com.induscore.repository;

import com.induscore.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 摄像头数据访问层。
 */
public interface CameraRepository extends JpaRepository<Camera, Long> {
    /**
     * 查询某生产线的摄像头列表。
     */
    List<Camera> findByProductionLineId(Long productionLineId);
}
