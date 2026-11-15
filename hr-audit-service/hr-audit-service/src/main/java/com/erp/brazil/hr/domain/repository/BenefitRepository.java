package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {
    Optional<Benefit> findByUuid(UUID uuid);
    Optional<Benefit> findByCompanyIdAndCode(Long companyId, String code);
    List<Benefit> findByCompanyId(Long companyId);
    List<Benefit> findByCompanyIdAndIsActiveTrue(Long companyId);
    List<Benefit> findByCompanyIdAndBenefitType(Long companyId, Benefit.BenefitType type);
    List<Benefit> findByProviderCnpj(String cnpj);
    List<Benefit> findActiveByCompanyIdAndDate(Long companyId, LocalDate date);

    @Query("SELECT b FROM Benefit b WHERE b.companyId = :companyId AND b.isActive = true AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate >= :date)")
    List<Benefit> findActiveBenefits(@Param("companyId") Long companyId, @Param("date") LocalDate date);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}