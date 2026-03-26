package com.induscore.repository;

import com.induscore.model.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI 模型版本数据访问接口。
 */
@Repository
public interface ModelVersionRepository extends JpaRepository<ModelVersion, Long> {

    /**
     * 根据模型ID查询所有版本
     */
    List<ModelVersion> findByModelIdOrderByCreatedAtDesc(Long modelId);

    /**
     * 根据模型ID和版本号查找
     */
    Optional<ModelVersion> findByModelIdAndVersion(Long modelId, String version);

    /**
     * 检查版本是否已存在
     */
    boolean existsByModelIdAndVersion(Long modelId, String version);

    /**
     * 获取模型的当前激活版本
     */
    Optional<ModelVersion> findByModelIdAndIsCurrentTrue(Long modelId);

    /**
     * 获取指定模型的所有已部署版本
     */
    List<ModelVersion> findByModelIdAndStatusOrderByCreatedAtDesc(Long modelId, String status);

    /**
     * 获取指定状态的所有版本
     */
    List<ModelVersion> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 清除模型的当前版本标记（用于切换版本时）
     */
    @Modifying
    @Query("UPDATE ModelVersion v SET v.isCurrent = false WHERE v.modelId = :modelId")
    void clearCurrentVersion(@Param("modelId") Long modelId);

    /**
     * 统计模型的版本数量
     */
    long countByModelId(Long modelId);

    /**
     * 删除模型的所有版本
     */
    @Modifying
    @Query("DELETE FROM ModelVersion v WHERE v.modelId = :modelId")
    void deleteByModelId(@Param("modelId") Long modelId);
}
