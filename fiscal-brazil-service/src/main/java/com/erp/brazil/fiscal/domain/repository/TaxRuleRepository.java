package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TaxRule entities with caching support.
 */
@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, String> {

    @Query("""
            SELECT r FROM TaxRule r
            WHERE r.taxType = :taxType
            AND r.active = true
            AND r.effectiveFrom <= :date
            AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date)
            AND (:ncm IS NULL OR r.ncmCode IS NULL OR r.ncmCode = :ncm)
            AND (:cest IS NULL OR r.cestCode IS NULL OR r.cestCode = :cest)
            AND (:cfop IS NULL OR r.cfopCode IS NULL OR r.cfopCode = :cfop)
            AND (:ufOrigin IS NULL OR r.ufOrigin IS NULL OR r.ufOrigin = :ufOrigin)
            AND (:ufDestination IS NULL OR r.ufDestination IS NULL OR r.ufDestination = :ufDestination)
            AND (:taxRegime IS NULL OR r.taxRegime IS NULL OR r.taxRegime = :taxRegime)
            ORDER BY r.priority DESC, r.effectiveFrom DESC
            """)
    List<TaxRule> findApplicableRules(
            @Param("taxType") String taxType,
            @Param("date") LocalDate date,
            @Param("ncm") String ncm,
            @Param("cest") String cest,
            @Param("cfop") String cfop,
            @Param("ufOrigin") String ufOrigin,
            @Param("ufDestination") String ufDestination,
            @Param("taxRegime") String taxRegime
    );

    @Query("SELECT r FROM TaxRule r WHERE r.taxType = :taxType AND r.active = true ORDER BY r.priority DESC")
    List<TaxRule> findActiveByTaxType(@Param("taxType") String taxType);

    Optional<TaxRule> findByIdAndActiveTrue(String id);

    List<TaxRule> findByNcmCodeAndActiveTrue(String ncmCode);

    List<TaxRule> findByCestCodeAndActiveTrue(String cestCode);

    List<TaxRule> findByCfopCodeAndActiveTrue(String cfopCode);

    @Query("SELECT r FROM TaxRule r WHERE r.version = :version AND r.active = true")
    List<TaxRule> findByVersion(@Param("version") String version);

    long countByTaxTypeAndActiveTrue(String taxType);
}