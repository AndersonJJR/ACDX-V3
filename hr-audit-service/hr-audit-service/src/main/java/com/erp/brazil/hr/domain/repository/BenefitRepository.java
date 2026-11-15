package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Benefit;
import com.erp.brazil.hr.domain.model.BenefitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    Optional<Benefit> findByCompanyIdAndCode(Long companyId, String code);

    List<Benefit> findByCompanyId(Long companyId);

    Page<Benefit> findByCompanyId(Long companyId, Pageable pageable);

    List<Benefit> findByCompanyIdAndIsActiveTrue(Long companyId);

    List<Benefit> findByCompanyIdAndBenefitType(Long companyId, BenefitType benefitType);

    List<Benefit> findByCompanyIdAndBenefitTypeAndIsActiveTrue(Long companyId, BenefitType benefitType);

    @Query("SELECT b FROM Benefit b WHERE b.companyId = :companyId AND b.isActive = true AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate >= :date)")
    List<Benefit> findActiveByCompanyIdAndDate(@Param("companyId") Long companyId,
                                                @Param("date") LocalDate date);

    @Query("SELECT b FROM Benefit b WHERE b.companyId = :companyId AND b.isActive = true AND b.isMandatory = true AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate >= :date)")
    List<Benefit> findMandatoryActiveByCompanyIdAndDate(@Param("companyId") Long companyId,
                                                         @Param("date") LocalDate date);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}