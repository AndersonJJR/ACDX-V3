package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.NFE;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for NF-e entities.
 */
@Repository
public interface NFERepository extends JpaRepository<NFE, String> {

    Optional<NFE> findByAccessKey(String accessKey);

    Optional<NFE> findByCompanyIdAndSeriesAndNumber(String companyId, String series, String number);

    @Query("SELECT n FROM NFE n WHERE n.companyId = :companyId AND n.status = :status")
    List<NFE> findByCompanyIdAndStatus(@Param("companyId") String companyId, @Param("status") String status);

    @Query("SELECT n FROM NFE n WHERE n.companyId = :companyId AND n.emissionDate BETWEEN :start AND :end")
    List<NFE> findByCompanyIdAndEmissionDateBetween(
            @Param("companyId") String companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT n FROM NFE n WHERE n.companyId = :companyId AND n.emissionDate BETWEEN :start AND :end")
    Page<NFE> findByCompanyIdAndEmissionDateBetween(
            @Param("companyId") String companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("SELECT n FROM NFE n WHERE n.protocol IS NOT NULL AND n.protocol = :protocol")
    Optional<NFE> findByProtocol(@Param("protocol") String protocol);

    List<NFE> findByCompanyIdOrderByEmissionDateDesc(String companyId);

    @Query("SELECT COUNT(n) FROM NFE n WHERE n.companyId = :companyId AND n.status = 'EMITTED'")
    long countEmittedByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT n FROM NFE n WHERE n.cancelledAt IS NOT NULL AND n.cancelledAt BETWEEN :start AND :end")
    List<NFE> findCancelledBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    boolean existsByAccessKey(String accessKey);
}