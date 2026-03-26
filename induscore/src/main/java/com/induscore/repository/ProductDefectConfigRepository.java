package com.induscore.repository;

import com.induscore.model.ProductDefectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 产品缺陷类型配置数据访问层。
 */
@Repository
public interface ProductDefectConfigRepository extends JpaRepository<ProductDefectConfig, Long> {

    /**
     * 根据产品ID查询所有缺陷配置
     */
    List<ProductDefectConfig> findByProductId(Long productId);

    /**
     * 根据产品ID和启用的状态查询
     */
    List<ProductDefectConfig> findByProductIdAndEnabled(Long productId, Boolean enabled);

    /**
     * 删除产品的所有缺陷配置
     */
    @Modifying
    @Query("DELETE FROM ProductDefectConfig pdc WHERE pdc.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);

    /**
     * 统计产品的缺陷配置数量
     */
    long countByProductId(Long productId);
}
