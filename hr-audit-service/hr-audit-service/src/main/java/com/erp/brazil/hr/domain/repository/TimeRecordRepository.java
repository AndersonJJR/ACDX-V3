package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.TimeRecord;
import com.erp.brazil.hr.domain.model.TimeRecordStatus;
import com.erp.brazil.hr.domain.model.TimeRecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    Optional<TimeRecord> findByEmployeeIdAndRecordDateAndRecordType(Long employeeId, LocalDate recordDate, TimeRecordType recordType);

    List<TimeRecord> findByEmployeeIdAndRecordDate(Long employeeId, LocalDate recordDate);

    List<TimeRecord> findByEmployeeIdAndRecordDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    Page<TimeRecord> findByEmployeeIdAndRecordDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<TimeRecord> findByEmployeeIdAndRecordDateBetweenAndStatus(Long employeeId, LocalDate startDate, LocalDate endDate, TimeRecordStatus status);

    @Query("SELECT r FROM TimeRecord r WHERE r.employee.companyId = :companyId AND r.recordDate BETWEEN :startDate AND :endDate")
    List<TimeRecord> findByCompanyIdAndRecordDateBetween(@Param("companyId") Long companyId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM TimeRecord r WHERE r.employee.companyId = :companyId AND r.recordDate BETWEEN :startDate AND :endDate AND r.status = :status")
    List<TimeRecord> findByCompanyIdAndRecordDateBetweenAndStatus(@Param("companyId") Long companyId,
                                                                   @Param("startDate") LocalDate startDate,
                                                                   @Param("endDate") LocalDate endDate,
                                                                   @Param("status") TimeRecordStatus status);

    @Query("SELECT r FROM TimeRecord r WHERE r.employeeId = :employeeId AND r.recordDate = :date AND r.recordType IN :types")
    List<TimeRecord> findByEmployeeIdAndDateAndTypes(@Param("employeeId") Long employeeId,
                                                      @Param("date") LocalDate date,
                                                      @Param("types") List<TimeRecordType> types);

    @Query("SELECT r FROM TimeRecord r WHERE r.status = :status AND r.recordDate < :date")
    List<TimeRecord> findPendingRecordsOlderThan(@Param("status") TimeRecordStatus status,
                                                  @Param("date") LocalDate date);

    long countByEmployeeIdAndRecordDateAndRecordType(Long employeeId, LocalDate recordDate, TimeRecordType recordType);

    boolean existsByEmployeeIdAndRecordDateAndRecordType(Long employeeId, LocalDate recordDate, TimeRecordType recordType);

    @Query("SELECT COUNT(r) FROM TimeRecord r WHERE r.employeeId = :employeeId AND r.recordDate BETWEEN :startDate AND :endDate AND r.recordType IN :workTypes")
    long countWorkRecordsByEmployeeAndPeriod(@Param("employeeId") Long employeeId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("workTypes") List<TimeRecordType> workTypes);
}