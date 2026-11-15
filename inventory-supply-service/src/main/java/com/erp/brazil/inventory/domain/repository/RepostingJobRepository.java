package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.RepostingJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepostingJobRepository extends JpaRepository<RepostingJob, UUID> {

    List<RepostingJob> findByCompanyIdAndStatus(UUID companyId, RepostingJob.RepostingStatus status);

    Page<RepostingJob> findByCompanyIdAndStatus(UUID companyId, RepostingJob.RepostingStatus status, Pageable pageable);

    List<RepostingJob> findByCompanyIdAndTriggeredBy(UUID companyId, UUID triggeredBy);

    List<RepostingJob> findByCompanyIdAndCreatedAtBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT rj FROM RepostingJob rj WHERE rj.companyId = :companyId AND rj.status IN ('PENDING', 'QUEUED', 'RUNNING') ORDER BY rj.priority ASC, rj.createdAt ASC")
    List<RepostingJob> findPendingJobs(@Param("companyId") UUID companyId);

    @Query("SELECT rj FROM RepostingJob rj WHERE rj.companyId = :companyId AND rj.itemId = :itemId AND rj.warehouseId = :warehouseId AND rj.status IN ('COMPLETED', 'PARTIALLY_COMPLETED') ORDER BY rj.completedAt DESC")
    List<RepostingJob> findCompletedJobsForItemWarehouse(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT COUNT(rj) FROM RepostingJob rj WHERE rj.companyId = :companyId AND rj.status IN ('PENDING', 'QUEUED', 'RUNNING')")
    long countPendingJobs(@Param("companyId") UUID companyId);
}