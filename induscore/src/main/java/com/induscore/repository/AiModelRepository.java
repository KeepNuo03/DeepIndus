package com.induscore.repository;

import com.induscore.model.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI 模型数据访问接口。
 */
@Repository
public interface AiModelRepository extends JpaRepository<AiModel, Long> {

    /**
     * 根据编码查找模型
     */
    Optional<AiModel> findByCode(String code);

    /**
     * 检查编码是否已存在
     */
    boolean existsByCode(String code);

    /**
     * 检查编码是否与其他模型重复
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 根据状态查询模型列表
     */
    List<AiModel> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 查询所有已部署的模型
     */
    List<AiModel> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    /**
     * 获取当前主模型
     */
    Optional<AiModel> findByIsMainTrue();

    /**
     * 获取当前部署的模型
     */
    @Query("SELECT m FROM AiModel m WHERE m.status = 'deployed'")
    List<AiModel> findDeployedModels();

    /**
     * 获取当前激活的模型（状态为 active 或 deployed）
     */
    @Query("SELECT m FROM AiModel m WHERE m.status IN ('active', 'deployed') ORDER BY m.isMain DESC, m.updatedAt DESC")
    List<AiModel> findActiveModels();

    /**
     * 根据架构类型查询
     */
    List<AiModel> findByArchitectureOrderByCreatedAtDesc(String architecture);

    /**
     * 搜索模型（名称或描述包含关键字）
     */
    @Query("SELECT m FROM AiModel m WHERE m.name LIKE %:keyword% OR m.description LIKE %:keyword% ORDER BY m.createdAt DESC")
    List<AiModel> searchByKeyword(String keyword);
}
