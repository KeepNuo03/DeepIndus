package com.induscore.repository;

import com.induscore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产品数据访问层。
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据型号查找产品
     */
    Optional<Product> findByModel(String model);

    /**
     * 根据型号查找产品（排除指定ID，用于更新时检查重复）
     */
    Optional<Product> findByModelAndIdNot(String model, Long id);

    /**
     * 根据分类查询产品列表（支持分页）
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * 根据状态查询产品列表
     */
    List<Product> findByStatus(String status);

    /**
     * 分页查询所有产品（支持搜索和分类筛选）
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR :category = 'all' OR p.category = :category) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.model) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = 'all' OR p.status = :status)")
    Page<Product> findByFilters(@Param("category") String category,
                                @Param("search") String search,
                                @Param("status") String status,
                                Pageable pageable);

    /**
     * 查询所有活跃的产品
     */
    List<Product> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 根据生产线ID查询关联的产品（通过关联表）
     */
    @Query("SELECT p FROM Product p JOIN p.lineRelations plr WHERE plr.productionLine.id = :lineId")
    List<Product> findByProductionLineId(@Param("lineId") Long lineId);

    /**
     * 检查型号是否已存在
     */
    boolean existsByModel(String model);

    /**
     * 检查型号是否已存在（排除指定ID）
     */
    boolean existsByModelAndIdNot(String model, Long id);
}
