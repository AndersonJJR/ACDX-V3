package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Payroll;
import com.erp.brazil.hr.domain.model.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByCompanyIdAndReferenceMonthAndReferenceYear(Long companyId, Integer month, Integer year);

    Optional<Payroll> findByUuid(String uuid);

    List<Payroll> findByCompanyId(Long companyId);

    Page<Payroll> findByCompanyId(Long companyId, Pageable pageable);

    List<Payroll> findByCompanyIdAndStatus(Long companyId, PayrollStatus status);

    List<Payroll> findByStatus(PayrollStatus status);

    List<Payroll> findByCompanyIdAndReferenceYear(Long companyId, Integer year);

    @Query("SELECT p FROM Payroll p WHERE p.companyId = :companyId AND p.referenceYear = :year AND p.referenceMonth = :month")
    Optional<Payroll> findByCompanyIdAndPeriod(@Param("companyId") Long companyId,
                                                @Param("year") Integer year,
                                                @Param("month") Integer month);

    @Query("SELECT p FROM Payroll p WHERE p.status IN :statuses")
    List<Payroll> findByStatusIn(@Param("statuses") List<PayrollStatus> statuses);

    @Query("SELECT p FROM Payroll p WHERE p.status = :status AND p.calculatedAt < :date")
    List<Payroll> findStaleCalculatingPayrolls(@Param("status") PayrollStatus status,
                                                @Param("date") java.time.Instant date);

    boolean existsByCompanyIdAndReferenceMonthAndReferenceYear(Long companyId, Integer month, Integer year);

    long countByCompanyIdAndStatus(Long companyId, PayrollStatus status);
}