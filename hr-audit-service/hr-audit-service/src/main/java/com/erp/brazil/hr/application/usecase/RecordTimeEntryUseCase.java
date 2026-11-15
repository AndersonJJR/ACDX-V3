package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordTimeEntryUseCase {

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;
    private final EmploymentContractRepository contractRepository;
    private final TimeSheetRepository timeSheetRepository;

    private static final int TOLERANCE_MINUTES = 10;
    private static final int MAX_OVERTIME_HOURS_DAY = 2;
    private static final int MAX_OVERTIME_HOURS_MONTH = 40;

    @Transactional
    public TimeRecord recordEntry(Long employeeId, LocalDate date, LocalTime time, TimeRecordType type, TimeRecordSource source) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (!employee.isActive()) {
            throw new IllegalStateException("Cannot record time for inactive employee");
        }

        // Check for duplicate
        Optional<TimeRecord> existing = timeRecordRepository.findByEmployeeIdAndRecordDateAndRecordType(employeeId, date, type);
        if (existing.isPresent()) {
            throw new IllegalStateException("Time record already exists for this employee, date and type");
        }

        TimeRecord record = new TimeRecord();
        record.setEmployee(employee);
        record.setRecordDate(date);
        record.setRecordType(type);
        record.setRecordTime(time);
        record.setRecordSource(source);
        record.setStatus(TimeRecordStatus.PENDING);

        // Auto-approve if from automatic source
        if (source.isAutomatic()) {
            record.setStatus(TimeRecordStatus.APPROVED);
            record.setApprovedAt(java.time.Instant.now());
        }

        TimeRecord saved = timeRecordRepository.save(record);

        // Update or create time sheet
        updateTimeSheet(employee, date.getYear(), date.getMonthValue());

        log.info("Time record created for employee {}: {} at {}", employeeId, type, time);
        return saved;
    }

    @Transactional
    public TimeRecord recordBiometricEntry(Long employeeId, LocalDate date, LocalTime time, String deviceId) {
        TimeRecord record = new TimeRecord();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        record.setEmployee(employee);
        record.setRecordDate(date);
        record.setRecordType(TimeRecordType.ENTRY);
        record.setRecordTime(time);
        record.setRecordSource(TimeRecordSource.BIOMETRIC);
        record.setDeviceId(deviceId);
        record.setStatus(TimeRecordStatus.APPROVED);
        record.setApprovedAt(java.time.Instant.now());

        TimeRecord saved = timeRecordRepository.save(record);
        updateTimeSheet(employee, date.getYear(), date.getMonthValue());
        return saved;
    }

    @Transactional
    public TimeRecord recordMobileEntry(Long employeeId, LocalDate date, LocalTime time, Double latitude, Double longitude) {
        TimeRecord record = new TimeRecord();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        record.setEmployee(employee);
        record.setRecordDate(date);
        record.setRecordType(TimeRecordType.ENTRY);
        record.setRecordTime(time);
        record.setRecordSource(TimeRecordSource.MOBILE_APP);
        record.setLatitude(latitude);
        record.setLongitude(longitude);
        record.setStatus(TimeRecordStatus.APPROVED);
        record.setApprovedAt(java.time.Instant.now());

        TimeRecord saved = timeRecordRepository.save(record);
        updateTimeSheet(employee, date.getYear(), date.getMonthValue());
        return saved;
    }

    @Transactional
    public TimeRecord recordManualAdjustment(Long employeeId, LocalDate date, TimeRecordType type,
                                             LocalTime time, String reason, Long adjustedBy) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Employee adjustedByEmployee = employeeRepository.findById(adjustedBy)
                .orElseThrow(() -> new IllegalArgumentException("Adjusted by employee not found"));

        Optional<TimeRecord> existing = timeRecordRepository.findByEmployeeIdAndRecordDateAndRecordType(employeeId, date, type);
        TimeRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setAdjustedBy(adjustedByEmployee);
            record.setAdjustedAt(java.time.Instant.now());
            record.setAdjustmentReason(reason);
            record.setIsManualAdjustment(true);
            record.setRecordTime(time);
            record.setStatus(TimeRecordStatus.PENDING);
        } else {
            record = new TimeRecord();
            record.setEmployee(employee);
            record.setRecordDate(date);
            record.setRecordType(type);
            record.setRecordTime(time);
            record.setRecordSource(TimeRecordSource.MANUAL);
            record.setIsManualAdjustment(true);
            record.setAdjustmentReason(reason);
            record.setAdjustedBy(adjustedByEmployee);
            record.setAdjustedAt(java.time.Instant.now());
            record.setStatus(TimeRecordStatus.PENDING);
        }

        TimeRecord saved = timeRecordRepository.save(record);
        updateTimeSheet(employee, date.getYear(), date.getMonthValue());
        return saved;
    }

    @Transactional
    public void approveRecord(Long recordId, Long approvedBy) {
        TimeRecord record = timeRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Time record not found"));

        if (record.getStatus() != TimeRecordStatus.PENDING) {
            throw new IllegalStateException("Only pending records can be approved");
        }

        Employee approvedByEmployee = employeeRepository.findById(approvedBy)
                .orElseThrow(() -> new IllegalArgumentException("Approved by employee not found"));

        record.setStatus(TimeRecordStatus.APPROVED);
        record.setApprovedBy(approvedByEmployee);
        record.setApprovedAt(java.time.Instant.now());
        timeRecordRepository.save(record);

        updateTimeSheet(record.getEmployee(), record.getRecordDate().getYear(), record.getRecordDate().getMonthValue());
    }

    @Transactional
    public void rejectRecord(Long recordId, Long rejectedBy, String reason) {
        TimeRecord record = timeRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Time record not found"));

        if (record.getStatus() != TimeRecordStatus.PENDING) {
            throw new IllegalStateException("Only pending records can be rejected");
        }

        Employee rejectedByEmployee = employeeRepository.findById(rejectedBy)
                .orElseThrow(() -> new IllegalArgumentException("Rejected by employee not found"));

        record.setStatus(TimeRecordStatus.REJECTED);
        record.setApprovedBy(rejectedByEmployee);
        record.setApprovedAt(java.time.Instant.now());
        record.setAdjustmentReason(reason);
        timeRecordRepository.save(record);
    }

    public List<TimeRecord> getEmployeeRecords(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return timeRecordRepository.findByEmployeeIdAndRecordDateBetween(employeeId, startDate, endDate);
    }

    public List<TimeRecord> getPendingRecords(Long companyId) {
        // Would need a custom query
        return List.of();
    }

    private void updateTimeSheet(Employee employee, int year, int month) {
        List<TimeRecord> records = timeRecordRepository.findByEmployeeIdAndRecordDateBetween(
                employee.getId(),
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth())
        );

        TimeSheet timeSheet = timeSheetRepository.findByEmployeeIdAndReferenceMonthAndReferenceYear(employee.getId(), month, year)
                .orElseGet(() -> {
                    TimeSheet ts = new TimeSheet();
                    ts.setEmployee(employee);
                    ts.setReferenceYear(year);
                    ts.setReferenceMonth(month);
                    ts.setStatus(TimeSheet.TimeSheetStatus.OPEN);
                    return timeSheetRepository.save(ts);
                });

        // Calculate totals
        BigDecimal workedHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal nightHours = BigDecimal.ZERO;
        BigDecimal absenceHours = BigDecimal.ZERO;
        int lateMinutes = 0;
        int earlyMinutes = 0;
        int daysWorked = 0;
        int daysAbsent = 0;

        // Group by date and calculate daily totals
        Map<LocalDate, List<TimeRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(TimeRecord::getRecordDate));

        for (Map.Entry<LocalDate, List<TimeRecord>> entry : recordsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<TimeRecord> dayRecords = entry.getValue();

            if (isHoliday(date, employee)) continue;

            TimeRecord entryRecord = dayRecords.stream()
                    .filter(r -> r.getRecordType() == TimeRecordType.ENTRY)
                    .findFirst().orElse(null);
            TimeRecord exitRecord = dayRecords.stream()
                    .filter(r -> r.getRecordType() == TimeRecordType.EXIT)
                    .findFirst().orElse(null);

            if (entryRecord != null && exitRecord != null) {
                daysWorked++;
                long minutes = java.time.Duration.between(entryRecord.getRecordTime(), exitRecord.getRecordTime()).toMinutes();
                workedHours = workedHours.add(BigDecimal.valueOf(minutes / 60.0));

                // Check late arrival
                EmploymentContract contract = contractRepository.findByEmployeeId(employee.getId()).orElse(null);
                if (contract != null) {
                    LocalTime expectedEntry = LocalTime.of(8, 0); // Would come from contract/work schedule
                    if (entryRecord.getRecordTime().isAfter(expectedEntry.plusMinutes(TOLERANCE_MINUTES))) {
                        lateMinutes += (int) java.time.Duration.between(expectedEntry.plusMinutes(TOLERANCE_MINUTES), entryRecord.getRecordTime()).toMinutes();
                    }
                }
            } else if (dayRecords.stream().anyMatch(r -> r.getRecordType().isAbsenceRecord())) {
                daysAbsent++;
                absenceHours = absenceHours.add(BigDecimal.valueOf(8)); // Standard 8 hours
            }
        }

        timeSheet.setTotalWorkedHours(workedHours.setScale(2, BigDecimal.ROUND_HALF_UP));
        timeSheet.setTotalOvertimeHours(overtimeHours.setScale(2, BigDecimal.ROUND_HALF_UP));
        timeSheet.setTotalNightHours(nightHours.setScale(2, BigDecimal.ROUND_HALF_UP));
        timeSheet.setTotalAbsenceHours(absenceHours.setScale(2, BigDecimal.ROUND_HALF_UP));
        timeSheet.setTotalLateMinutes(lateMinutes);
        timeSheet.setTotalEarlyDepartureMinutes(earlyMinutes);
        timeSheet.setDaysWorked(daysWorked);
        timeSheet.setDaysAbsent(daysAbsent);
        timeSheetRepository.save(timeSheet);
    }

    private boolean isHoliday(LocalDate date, Employee employee) {
        // Check national holidays
        List<Holiday> holidays = holidayRepository.findByDate(date);
        for (Holiday holiday : holidays) {
            if (holiday.appliesToCompany(employee.getCompanyId())) {
                return true;
            }
        }
        return false;
    }
}