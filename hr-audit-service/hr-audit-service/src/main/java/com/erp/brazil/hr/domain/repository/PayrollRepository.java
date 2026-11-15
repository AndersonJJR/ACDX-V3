package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByUuid(UUID uuid);
    Optional<Payroll> findByCompanyIdAndReferenceMonthAndReferenceYear(Long companyId, int month, int year);
    List<Payroll> findByCompanyId(Long companyId);
    List<Payroll> findByCompanyIdAndReferenceYear(Long companyId, int year);
    List<Payroll> findByStatus(Payroll.PayrollStatus status);
    List<Payroll> findByCompanyIdAndStatus(Long companyId, Payroll.PayrollStatus status);
    List<Payroll> findByReferenceYearAndReferenceMonth(int year, int month);

    @Query("SELECT p FROM Payroll p WHERE p.companyId = :companyId AND p.referenceYear = :year AND p.referenceMonth <= :month ORDER BY p.referenceMonth DESC")
    List<Payroll> findByCompanyIdAndYearUpToMonth(@Param("companyId") Long companyId, @Param("year") int year, @Param("month") int month);

    boolean existsByCompanyIdAndReferenceMonthAndReferenceYear(Long companyId, int month, int year);
    long countByCompanyIdAndStatus(Long companyId, Payroll.PayrollStatus status);
}