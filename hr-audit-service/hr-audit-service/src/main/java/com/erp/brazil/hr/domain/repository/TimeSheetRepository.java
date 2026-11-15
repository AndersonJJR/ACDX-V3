package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.TimeSheet;
import com.erp.brazil.hr.domain.model.TimeSheetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {

    Optional<TimeSheet> findByEmployeeIdAndReferenceMonthAndReferenceYear(Long employeeId, Integer month, Integer year);

    Optional<TimeSheet> findByUuid(String uuid);

    List<TimeSheet> findByEmployeeIdAndReferenceYear(Long employeeId, Integer year);

    List<TimeSheet> findByEmployeeIdAndReferencePeriodBetween(Long employeeId, YearMonth start, YearMonth end);

    Page<TimeSheet> findByEmployeeIdAndReferencePeriodBetween(Long employeeId, YearMonth start, YearMonth end, Pageable pageable);

    List<TimeSheet> findByReferenceMonthAndReferenceYear(Integer month, Integer year);

    Page<TimeSheet> findByReferenceMonthAndReferenceYear(Integer month, Integer year, Pageable pageable);

    List<TimeSheet> findByReferenceMonthAndReferenceYearAndStatus(Integer month, Integer year, TimeSheetStatus status);

    @Query("SELECT ts FROM TimeSheet ts WHERE ts.employee.companyId = :companyId AND ts.referenceYear = :year AND ts.referenceMonth = :month")
    List<TimeSheet> findByCompanyIdAndPeriod(@Param("companyId") Long companyId,
                                              @Param("year") Integer year,
                                              @Param("month") Integer month);

    @Query("SELECT ts FROM TimeSheet ts WHERE ts.employee.companyId = :companyId AND ts.referenceYear = :year AND ts.referenceMonth = :month AND ts.status = :status")
    List<TimeSheet> findByCompanyIdAndPeriodAndStatus(@Param("companyId") Long companyId,
                                                       @Param("year") Integer year,
                                                       @Param("month") Integer month,
                                                       @Param("status") TimeSheetStatus status);

    @Query("SELECT ts FROM TimeSheet ts WHERE ts.status = :status")
    List<TimeSheet> findByStatus(TimeSheetStatus status);

    long countByEmployeeIdAndReferenceYearAndStatus(Long employeeId, Integer year, TimeSheetStatus status);

    boolean existsByEmployeeIdAndReferenceMonthAndReferenceYear(Long employeeId, Integer month, Integer year);
}