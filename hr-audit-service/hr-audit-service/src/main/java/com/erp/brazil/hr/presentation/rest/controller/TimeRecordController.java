package com.erp.brazil.hr.presentation.rest.controller;

import com.erp.brazil.hr.application.usecase.RecordTimeEntryUseCase;
import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.TimeRecordRepository;
import com.erp.brazil.hr.domain.repository.TimeSheetRepository;
import com.erp.brazil.hr.presentation.rest.dto.TimeRecordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/time-records")
@RequiredArgsConstructor
@Tag(name = "Time Records", description = "Ponto eletrônico APIs")
public class TimeRecordController {

    private final RecordTimeEntryUseCase recordTimeEntryUseCase;
    private final TimeRecordRepository timeRecordRepository;
    private final TimeSheetRepository timeSheetRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Record time entry")
    public ResponseEntity<TimeRecordDto.Response> record(@Valid @RequestBody TimeRecordDto.Request request) {
        TimeRecord record = recordTimeEntryUseCase.recordEntry(
            request.getEmployeeId(),
            request.getRecordDate(),
            request.getRecordTime(),
            request.getRecordType(),
            request.getRecordSource()
        );
        return ResponseEntity.ok(toResponse(record));
    }

    @PostMapping("/biometric")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Record biometric entry")
    public ResponseEntity<TimeRecordDto.Response> recordBiometric(
            @RequestParam Long employeeId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime time,
            @RequestParam String deviceId) {
        TimeRecord record = recordTimeEntryUseCase.recordBiometricEntry(employeeId, date, time, deviceId);
        return ResponseEntity.ok(toResponse(record));
    }

    @PostMapping("/mobile")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Record mobile entry with GPS")
    public ResponseEntity<TimeRecordDto.Response> recordMobile(
            @RequestParam Long employeeId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime time,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        TimeRecord record = recordTimeEntryUseCase.recordMobileEntry(employeeId, date, time, latitude, longitude);
        return ResponseEntity.ok(toResponse(record));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Manual time record adjustment")
    public ResponseEntity<TimeRecordDto.Response> manualAdjustment(@Valid @RequestBody TimeRecordDto.ManualAdjustmentRequest request) {
        TimeRecord record = recordTimeEntryUseCase.recordManualAdjustment(
            request.getEmployeeId(),
            request.getRecordDate(),
            request.getRecordType(),
            request.getRecordTime(),
            request.getReason(),
            getCurrentUserId()
        );
        return ResponseEntity.ok(toResponse(record));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Approve time record")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        recordTimeEntryUseCase.approveRecord(id, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Reject time record")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        recordTimeEntryUseCase.rejectRecord(id, getCurrentUserId(), reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee time records")
    public ResponseEntity<TimeRecordDto.PageResponse> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("recordDate").descending().and(Sort.by("recordTime").ascending()));
        List<TimeRecord> records = timeRecordRepository.findByEmployeeIdAndRecordDateBetween(employeeId, startDate, endDate);

        // Simple pagination in memory (would be better with custom query)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), records.size());
        List<TimeRecord> pageContent = records.subList(start, end);

        return ResponseEntity.ok(TimeRecordDto.PageResponse.builder()
            .content(pageContent.stream().map(this::toResponse).collect(Collectors.toList()))
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(records.size())
            .totalPages((int) Math.ceil((double) records.size() / pageable.getPageSize()))
            .first(pageable.getPageNumber() == 0)
            .last(end >= records.size())
            .build());
    }

    @GetMapping("/daily-summary/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get daily time summary for employee")
    public ResponseEntity<List<TimeRecordDto.DailySummary>> getDailySummary(
            @PathVariable Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<TimeRecord> records = timeRecordRepository.findByEmployeeIdAndRecordDateBetween(employeeId, startDate, endDate);

        // Group by date and build summary
        return ResponseEntity.ok(records.stream()
            .collect(Collectors.groupingBy(TimeRecord::getRecordDate))
            .entrySet().stream()
            .map(entry -> buildDailySummary(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get pending time records for approval")
    public ResponseEntity<TimeRecordDto.PageResponse> getPending(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        // Would need custom query
        return ResponseEntity.ok(TimeRecordDto.PageResponse.builder()
            .content(List.of())
            .page(page)
            .size(size)
            .totalElements(0)
            .totalPages(0)
            .first(true)
            .last(true)
            .build());
    }

    private TimeRecordDto.Response toResponse(TimeRecord record) {
        return TimeRecordDto.Response.builder()
            .id(record.getId())
            .uuid(record.getUuid())
            .employeeId(record.getEmployee().getId())
            .employeeName(record.getEmployee().getFullName())
            .recordDate(record.getRecordDate())
            .recordType(record.getRecordType())
            .recordTypeDescription(record.getRecordType().getDescription())
            .recordTime(record.getRecordTime())
            .recordSource(record.getRecordSource())
            .recordSourceDescription(record.getRecordSource().getDescription())
            .latitude(record.getLatitude())
            .longitude(record.getLongitude())
            .deviceId(record.getDeviceId())
            .ipAddress(record.getIpAddress())
            .isManualAdjustment(record.getIsManualAdjustment())
            .adjustmentReason(record.getAdjustmentReason())
            .status(record.getStatus())
            .statusDescription(record.getStatus().getDescription())
            .approvedAt(record.getApprovedAt())
            .adjustedAt(record.getAdjustedAt())
            .build();
    }

    private TimeRecordDto.DailySummary buildDailySummary(LocalDate date, List<TimeRecord> records) {
        TimeRecordDto.DailySummary summary = new TimeRecordDto.DailySummary();
        summary.setDate(date);
        summary.setComplete(false);
        summary.setMissingRecords(new java.util.ArrayList<>());

        for (TimeRecord r : records) {
            switch (r.getRecordType()) {
                case ENTRY -> summary.setEntry(r.getRecordTime());
                case EXIT_LUNCH -> summary.setExitLunch(r.getRecordTime());
                case RETURN_LUNCH -> summary.setReturnLunch(r.getRecordTime());
                case EXIT -> summary.setExit(r.getRecordTime());
            }
        }

        // Check completeness
        if (summary.getEntry() == null) summary.getMissingRecords().add("Entrada");
        if (summary.getExitLunch() == null) summary.getMissingRecords().add("Saída Almoço");
        if (summary.getReturnLunch() == null) summary.getMissingRecords().add("Retorno Almoço");
        if (summary.getExit() == null) summary.getMissingRecords().add("Saída");

        summary.setComplete(summary.getMissingRecords().isEmpty());
        return summary;
    }

    private Long getCurrentUserId() {
        // Extract from security context
        return 1L;
    }
}