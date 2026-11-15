package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {
    Optional<TimeSheet> findByUuid(UUID uuid);
    Optional<TimeSheet> findByEmployeeIdAndReferenceMonthAndReferenceYear(Long employeeId, int month, int year);
    List<TimeSheet> findByEmployeeId(Long employeeId);
    List<TimeSheet> findByReferenceMonthAndReferenceYear(int month, int year);
    List<TimeSheet> findByReferenceYearAndStatus(int year, TimeSheet.TimeSheetStatus status);
    List<TimeSheet> findByEmployeeIdAndReferenceYear(Long employeeId, int year);
    List<TimeSheet> findByStatus(TimeSheet.TimeSheetStatus status);
    List<TimeSheet> findByEmployeeIdAndStatusIn(Long employeeId, List<TimeSheet.TimeSheetStatus> statuses);

    @Query("SELECT t FROM TimeSheet t WHERE t.employeeId = :employeeId AND t.referenceYear = :year AND t.referenceMonth <= :month ORDER BY t.referenceMonth DESC")
    List<TimeSheet> findByEmployeeIdAndYearUpToMonth(@Param("employeeId") Long employeeId, @Param("year") int year, @Param("month") int month);

    boolean existsByEmployeeIdAndReferenceMonthAndReferenceYear(Long employeeId, int month, int year);
}