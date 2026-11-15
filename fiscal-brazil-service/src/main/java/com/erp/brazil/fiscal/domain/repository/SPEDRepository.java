package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SPED blocks.
 */
@Repository
public interface SPEDRepository extends JpaRepository<SPEDBlock, String> {

    List<SPEDBlock> findByCompanyId(String companyId);

    List<SPEDBlock> findByCompanyIdAndPeriod(String companyId, String period);

    Optional<SPEDBlock> findByCompanyIdAndPeriodAndType(
            String companyId,
            String period,
            String type
    );

    @Query("SELECT s FROM SPEDBlock s WHERE s.status = :status")
    List<SPEDBlock> findByStatus(@Param("status") SPEDBlock.SPEDStatus status);

    @Query("SELECT s FROM SPEDBlock s WHERE s.companyId = :companyId AND s.startDate >= :start AND s.endDate <= :end")
    List<SPEDBlock> findByCompanyIdAndDateRange(
            @Param("companyId") String companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT s FROM SPEDBlock s WHERE s.generatedAt BETWEEN :start AND :end")
    List<SPEDBlock> findGeneratedBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(s) FROM SPEDBlock s WHERE s.companyId = :companyId AND s.status = 'SUBMITTED'")
    long countSubmittedByCompanyId(@Param("companyId") String companyId);

    boolean existsByCompanyIdAndPeriodAndType(String companyId, String period, String type);
}