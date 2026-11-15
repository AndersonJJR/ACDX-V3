package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
public class RecordTimeEntryUseCase {

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditEventUseCase auditEventUseCase;

    public RecordTimeEntryUseCase(TimeRecordRepository timeRecordRepository,
                                   EmployeeRepository employeeRepository,
                                   AuditEventUseCase auditEventUseCase) {
        this.timeRecordRepository = timeRecordRepository;
        this.employeeRepository = employeeRepository;
        this.auditEventUseCase = auditEventUseCase;
    }

    @Transactional
    public TimeRecord record(Long employeeId, LocalDate date, TimeRecord.RecordType type,
                              LocalTime time, TimeRecord.RecordSource source,
                              Double latitude, Double longitude, String deviceId, String ipAddress) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        if (!employee.isActive()) {
            throw new IllegalStateException("Employee is not active: " + employeeId);
        }

        // Validate sequence
        List<TimeRecord> existingRecords = timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordTimeAsc(employeeId, date);
        validateSequence(existingRecords, type, time);

        // Check for duplicate
        if (timeRecordRepository.existsByEmployeeIdAndRecordDateAndRecordType(employeeId, date, type)) {
            throw new IllegalStateException("Record already exists for this type on this date");
        }

        TimeRecord record = new TimeRecord(
            null, UUID.randomUUID(), employeeId, date, type, time, source,
            latitude, longitude, deviceId, ipAddress, false, null, null, null,
            TimeRecord.RecordStatus.PENDING, null, null
        );

        TimeRecord saved = timeRecordRepository.save(record);
        auditEventUseCase.logEvent("TIME_RECORD_CREATED", "TimeRecord", saved.uuid().toString(),
            employee.companyId(), employeeId, saved);

        return saved;
    }

    private void validateSequence(List<TimeRecord> existing, TimeRecord.RecordType newType, LocalTime newTime) {
        if (existing.isEmpty()) {
            if (newType != TimeRecord.RecordType.ENTRY && newType != TimeRecord.RecordType.OVERTIME_ENTRY) {
                throw new IllegalStateException("First record of the day must be ENTRY or OVERTIME_ENTRY");
            }
            return;
        }

        TimeRecord lastRecord = existing.get(existing.size() - 1);
        TimeRecord.RecordType lastType = lastRecord.recordType();

        // Validate sequence: ENTRY -> EXIT_LUNCH -> RETURN_LUNCH -> EXIT
        // OVERTIME_ENTRY -> OVERTIME_EXIT
        if (lastType == TimeRecord.RecordType.ENTRY) {
            if (newType != TimeRecord.RecordType.EXIT_LUNCH && newType != TimeRecord.RecordType.EXIT) {
                throw new IllegalStateException("After ENTRY, must be EXIT_LUNCH or EXIT");
            }
        } else if (lastType == TimeRecord.RecordType.EXIT_LUNCH) {
            if (newType != TimeRecord.RecordType.RETURN_LUNCH) {
                throw new IllegalStateException("After EXIT_LUNCH, must be RETURN_LUNCH");
            }
        } else if (lastType == TimeRecord.RecordType.RETURN_LUNCH) {
            if (newType != TimeRecord.RecordType.EXIT) {
                throw new IllegalStateException("After RETURN_LUNCH, must be EXIT");
            }
        } else if (lastType == TimeRecord.RecordType.OVERTIME_ENTRY) {
            if (newType != TimeRecord.RecordType.OVERTIME_EXIT) {
                throw new IllegalStateException("After OVERTIME_ENTRY, must be OVERTIME_EXIT");
            }
        } else if (lastType == TimeRecord.RecordType.EXIT || lastType == TimeRecord.RecordType.OVERTIME_EXIT) {
            throw new IllegalStateException("Day already closed, cannot add more records");
        }

        // Validate time is after last record
        if (!newTime.isAfter(lastRecord.recordTime())) {
            throw new IllegalStateException("Record time must be after previous record");
        }
    }

    @Transactional
    public TimeRecord adjust(Long recordId, Long adjustedBy, String reason, LocalTime newTime) {
        TimeRecord record = timeRecordRepository.findById(recordId)
            .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        TimeRecord adjusted = new TimeRecord(
            record.id(), record.uuid(), record.employeeId(), record.recordDate(), record.recordType(),
            newTime, record.recordSource(), record.latitude(), record.longitude(),
            record.deviceId(), record.ipAddress(), true, reason, adjustedBy, java.time.Instant.now(),
            TimeRecord.RecordStatus.PENDING, null, null
        );

        TimeRecord saved = timeRecordRepository.save(adjusted);
        auditEventUseCase.logEvent("TIME_RECORD_ADJUSTED", "TimeRecord", saved.uuid().toString(),
            null, adjustedBy, saved);

        return saved;
    }

    @Transactional
    public TimeRecord approve(Long recordId, Long approvedBy) {
        TimeRecord record = timeRecordRepository.findById(recordId)
            .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        TimeRecord approved = new TimeRecord(
            record.id(), record.uuid(), record.employeeId(), record.recordDate(), record.recordType(),
            record.recordTime(), record.recordSource(), record.latitude(), record.longitude(),
            record.deviceId(), record.ipAddress(), record.isManualAdjustment(), record.adjustmentReason(),
            record.adjustedBy(), record.adjustedAt(), TimeRecord.RecordStatus.APPROVED, approvedBy, java.time.Instant.now()
        );

        return timeRecordRepository.save(approved);
    }
}