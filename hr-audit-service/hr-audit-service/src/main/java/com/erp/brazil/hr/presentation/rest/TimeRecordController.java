package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.TimeRecordDTO;
import com.erp.brazil.hr.application.dto.TimeSheetDTO;
import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.application.usecase.RecordTimeEntryUseCase;
import com.erp.brazil.hr.domain.model.TimeRecord;
import com.erp.brazil.hr.domain.model.TimeSheet;
import com.erp.brazil.hr.domain.repository.TimeRecordRepository;
import com.erp.brazil.hr.domain.repository.TimeSheetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/time-records")
@Tag(name = "Time Records", description = "Electronic time clock (Ponto Eletrônico) API")
public class TimeRecordController {

    private final TimeRecordRepository timeRecordRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final RecordTimeEntryUseCase recordTimeEntryUseCase;
    private final ProcessWebhookUseCase webhookUseCase;

    public TimeRecordController(TimeRecordRepository timeRecordRepository,
                                 TimeSheetRepository timeSheetRepository,
                                 RecordTimeEntryUseCase recordTimeEntryUseCase,
                                 ProcessWebhookUseCase webhookUseCase) {
        this.timeRecordRepository = timeRecordRepository;
        this.timeSheetRepository = timeSheetRepository;
        this.recordTimeEntryUseCase = recordTimeEntryUseCase;
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping("/record")
    @Operation(summary = "Record a time entry (ponto eletrônico)")
    public ResponseEntity<TimeRecordDTO> record(
            @Valid @RequestBody TimeRecordDTO dto) {
        TimeRecord record = recordTimeEntryUseCase.record(
            dto.employeeId(), dto.recordDate(), dto.recordType(), dto.recordTime(),
            dto.recordSource(), dto.latitude(), dto.longitude(), dto.deviceId(), dto.ipAddress()
        );

        webhookUseCase.triggerEvent("TIME_RECORD_CREATED", Map.of("record", record), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeRecordDTO.from(record));
    }

    @GetMapping("/{employeeId}/timesheet")
    @Operation(summary = "Get timesheet for employee and period")
    public ResponseEntity<List<TimeRecordDTO>> getTimesheet(
            @PathVariable Long employeeId,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam LocalDate end) {
        List<TimeRecordDTO> records = timeRecordRepository.findByEmployeeIdAndRecordDateBetweenOrderByDateTime(employeeId, start, end)
            .stream().map(TimeRecordDTO::from).toList();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{employeeId}/timesheet/{year}/{month}")
    @Operation(summary = "Get calculated timesheet (fechamento de ponto)")
    public ResponseEntity<TimeSheetDTO> getCalculatedTimesheet(
            @PathVariable Long employeeId,
            @PathVariable int year,
            @PathVariable int month) {
        return timeSheetRepository.findByEmployeeIdAndReferenceMonthAndReferenceYear(employeeId, month, year)
            .map(TimeSheetDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/adjust")
    @Operation(summary = "Adjust a time record (manual correction)")
    public ResponseEntity<TimeRecordDTO> adjust(
            @PathVariable UUID id,
            @RequestParam Long adjustedBy,
            @RequestParam String reason,
            @RequestParam LocalTime newTime) {
        TimeRecord adjusted = recordTimeEntryUseCase.adjust(id, adjustedBy, reason, newTime);
        webhookUseCase.triggerEvent("TIME_RECORD_ADJUSTED", Map.of("record", adjusted), null);
        return ResponseEntity.ok(TimeRecordDTO.from(adjusted));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a time record")
    public ResponseEntity<TimeRecordDTO> approve(
            @PathVariable UUID id,
            @RequestParam Long approvedBy) {
        TimeRecord approved = recordTimeEntryUseCase.approve(id, approvedBy);
        return ResponseEntity.ok(TimeRecordDTO.from(approved));
    }

    @GetMapping
    @Operation(summary = "List time records with filters")
    public ResponseEntity<Page<TimeRecordDTO>> list(
            @Parameter(description = "Employee ID") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Start date") @RequestParam(required = false) LocalDate start,
            @Parameter(description = "End date") @RequestParam(required = false) LocalDate end,
            @Parameter(description = "Status") @RequestParam(required = false) TimeRecord.RecordStatus status,
            @PageableDefault(size = 50) Pageable pageable) {
        // Simplified - would need custom query for combined filters
        Page<TimeRecordDTO> page;
        if (employeeId != null && start != null && end != null) {
            page = timeRecordRepository.findByEmployeeIdAndRecordDateBetween(employeeId, start, end, pageable)
                .map(TimeRecordDTO::from);
        } else if (status != null) {
            page = timeRecordRepository.findByStatus(status, pageable)
                .map(TimeRecordDTO::from);
        } else {
            page = timeRecordRepository.findAll(pageable).map(TimeRecordDTO::from);
        }
        return ResponseEntity.ok(page);
    }
}