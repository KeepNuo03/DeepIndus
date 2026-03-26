package com.induscore.repository;

import com.induscore.model.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 生产线数据访问层。
 */
public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {
}
