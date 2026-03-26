package com.induscore.repository;

import com.induscore.model.ProductLineRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产品与生产线关联数据访问层。
 */
@Repository
public interface ProductLineRelationRepository extends JpaRepository<ProductLineRelation, Long> {

    /**
     * 根据产品ID查询所有关联
     */
    List<ProductLineRelation> findByProductId(Long productId);

    /**
     * 根据生产线ID查询所有关联
     */
    List<ProductLineRelation> findByProductionLineId(Long productionLineId);

    /**
     * 根据产品ID和生产线ID查询关联
     */
    Optional<ProductLineRelation> findByProductIdAndProductionLineId(Long productId, Long productionLineId);

    /**
     * 删除产品与生产线的关联
     */
    @Modifying
    @Query("DELETE FROM ProductLineRelation plr WHERE plr.product.id = :productId AND plr.productionLine.id = :lineId")
    void deleteByProductIdAndProductionLineId(@Param("productId") Long productId, @Param("lineId") Long lineId);

    /**
     * 删除产品的所有生产线关联
     */
    @Modifying
    @Query("DELETE FROM ProductLineRelation plr WHERE plr.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);

    /**
     * 检查关联是否存在
     */
    boolean existsByProductIdAndProductionLineId(Long productId, Long productionLineId);

    /**
     * 查询产品的主生产线关联
     */
    @Query("SELECT plr FROM ProductLineRelation plr WHERE plr.product.id = :productId AND plr.isPrimary = true")
    Optional<ProductLineRelation> findPrimaryRelationByProductId(@Param("productId") Long productId);
}
