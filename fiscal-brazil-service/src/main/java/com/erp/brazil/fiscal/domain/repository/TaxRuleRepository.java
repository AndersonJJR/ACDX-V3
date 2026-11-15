package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.ICMSRule;
import com.erp.brazil.fiscal.domain.model.IPIRule;
import com.erp.brazil.fiscal.domain.model.PISRule;
import com.erp.brazil.fiscal.domain.model.COFINSRule;
import com.erp.brazil.fiscal.domain.model.ISSRule;
import com.erp.brazil.fiscal.domain.model.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, String> {
    @Query("SELECT r FROM TaxRule r WHERE r.taxType = :taxType AND r.active = true AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) ORDER BY r.priority DESC")
    List<TaxRule> findActiveRulesByTypeAndDate(@Param("taxType") String taxType, @Param("date") LocalDate date);

    @Query("SELECT r FROM ICMSRule r WHERE r.active = true AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) AND (r.ufOrigin = :ufOrigin OR r.ufOrigin IS NULL) AND (r.ufDestination = :ufDestination OR r.ufDestination IS NULL) AND (r.ncmCode = :ncmCode OR r.ncmCode IS NULL) AND (r.cestCode = :cestCode OR r.cestCode IS NULL) AND (r.cfopCode = :cfopCode OR r.cfopCode IS NULL) AND (r.taxRegime = :taxRegime OR r.taxRegime IS NULL) ORDER BY r.priority DESC")
    List<ICMSRule> findMatchingICMSRules(
            @Param("date") LocalDate date,
            @Param("ufOrigin") String ufOrigin,
            @Param("ufDestination") String ufDestination,
            @Param("ncmCode") String ncmCode,
            @Param("cestCode") String cestCode,
            @Param("cfopCode") String cfopCode,
            @Param("taxRegime") String taxRegime);

    @Query("SELECT r FROM IPIRule r WHERE r.active = true AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) AND (r.ncmCode = :ncmCode OR r.ncmCode IS NULL) ORDER BY r.priority DESC")
    List<IPIRule> findMatchingIPIRules(@Param("date") LocalDate date, @Param("ncmCode") String ncmCode);

    @Query("SELECT r FROM PISRule r WHERE r.active = true AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) AND (r.ncmCode = :ncmCode OR r.ncmCode IS NULL) AND (r.cfopCode = :cfopCode OR r.cfopCode IS NULL) AND (r.taxRegime = :taxRegime OR r.taxRegime IS NULL) ORDER BY r.priority DESC")
    List<PISRule> findMatchingPISRules(
            @Param("date") LocalDate date,
            @Param("ncmCode") String ncmCode,
            @Param("cfopCode") String cfopCode,
            @Param("taxRegime") String taxRegime);

    @Query("SELECT r FROM COFINSRule r WHERE r.active = true AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) AND (r.ncmCode = :ncmCode OR r.ncmCode IS NULL) AND (r.cfopCode = :cfopCode OR r.cfopCode IS NULL) AND (r.taxRegime = :taxRegime OR r.taxRegime IS NULL) ORDER BY r.priority DESC")
    List<COFINSRule> findMatchingCOFINSRules(
            @Param("date") LocalDate date,
            @Param("ncmCode") String ncmCode,
            @Param("cfopCode") String cfopCode,
            @Param("taxRegime") String taxRegime);

    @Query("SELECT r FROM ISSRule r WHERE r.active = true AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) AND (r.municipalityCode = :municipalityCode OR r.municipalityCode IS NULL) AND (r.serviceCode = :serviceCode OR r.serviceCode IS NULL) AND (r.cnaeCode = :cnaeCode OR r.cnaeCode IS NULL) ORDER BY r.priority DESC")
    List<ISSRule> findMatchingISSRules(
            @Param("date") LocalDate date,
            @Param("municipalityCode") String municipalityCode,
            @Param("serviceCode") String serviceCode,
            @Param("cnaeCode") String cnaeCode);

    Optional<ICMSRule> findICMSRuleById(String id);
    Optional<IPIRule> findIPIRuleById(String id);
    Optional<PISRule> findPISRuleById(String id);
    Optional<COFINSRule> findCOFINSRuleById(String id);
    Optional<ISSRule> findISSRuleById(String id);
}