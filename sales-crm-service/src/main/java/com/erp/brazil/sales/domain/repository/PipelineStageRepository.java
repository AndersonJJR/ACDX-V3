package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.PipelineStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {

    Optional<PipelineStage> findByPipelineIdAndOrderIndex(UUID pipelineId, Integer orderIndex);

    List<PipelineStage> findByPipelineIdOrderByOrderIndexAsc(UUID pipelineId);

    List<PipelineStage> findByPipelineIdAndType(UUID pipelineId, com.erp.brazil.sales.domain.model.PipelineStageType type);

    @Query("SELECT ps FROM PipelineStage ps WHERE ps.pipeline.id = :pipelineId AND ps.isActive = true ORDER BY ps.orderIndex")
    List<PipelineStage> findActiveByPipelineIdOrderByOrderIndex(UUID pipelineId);

    Page<PipelineStage> findByPipelineId(UUID pipelineId, Pageable pageable);

    boolean existsByPipelineIdAndOrderIndexAndIdNot(UUID pipelineId, Integer orderIndex, UUID id);

    long countByPipelineId(UUID pipelineId);
}