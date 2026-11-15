package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {
    Optional<TimeRecord> findByUuid(UUID uuid);
    List<TimeRecord> findByEmployeeIdAndRecordDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<TimeRecord> findByEmployeeIdAndRecordDateOrderByRecordTimeAsc(Long employeeId, LocalDate date);
    Optional<TimeRecord> findByEmployeeIdAndRecordDateAndRecordType(Long employeeId, LocalDate date, TimeRecord.RecordType type);
    List<TimeRecord> findByEmployeeIdAndRecordDateAndStatus(Long employeeId, LocalDate date, TimeRecord.RecordStatus status);
    List<TimeRecord> findByStatus(TimeRecord.RecordStatus status);
    List<TimeRecord> findByEmployeeIdAndRecordDateBetweenAndStatusIn(Long employeeId, LocalDate start, LocalDate end, List<TimeRecord.RecordStatus> statuses);
    long countByEmployeeIdAndRecordDateAndRecordType(Long employeeId, LocalDate date, TimeRecord.RecordType type);

    @Query("SELECT r FROM TimeRecord r WHERE r.employeeId = :employeeId AND r.recordDate BETWEEN :start AND :end ORDER BY r.recordDate, r.recordTime")
    List<TimeRecord> findByEmployeeIdAndDateRangeOrderByDateTime(@Param("employeeId") Long employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    boolean existsByEmployeeIdAndRecordDateAndRecordType(Long employeeId, LocalDate date, TimeRecord.RecordType type);
    List<TimeRecord> findByAdjustedBy(Long adjustedBy);
    List<TimeRecord> findByApprovedBy(Long approvedBy);
}