package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.LeaveBalanceDTO;
import com.erp.brazil.hr.application.dto.LeaveRequestDTO;
import com.erp.brazil.hr.application.usecase.ApproveLeaveRequestUseCase;
import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.domain.model.LeaveRequest;
import com.erp.brazil.hr.domain.model.LeaveBalance;
import com.erp.brazil.hr.domain.repository.LeaveRequestRepository;
import com.erp.brazil.hr.domain.repository.LeaveBalanceRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave")
@Tag(name = "Leave Management", description = "Leave requests and balances API")
public class LeaveController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ApproveLeaveRequestUseCase approveLeaveRequestUseCase;
    private final ProcessWebhookUseCase webhookUseCase;

    public LeaveController(LeaveRequestRepository leaveRequestRepository,
                            LeaveBalanceRepository leaveBalanceRepository,
                            ApproveLeaveRequestUseCase approveLeaveRequestUseCase,
                            ProcessWebhookUseCase webhookUseCase) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.approveLeaveRequestUseCase = approveLeaveRequestUseCase;
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping("/requests")
    @Operation(summary = "Create a new leave request")
    public ResponseEntity<LeaveRequestDTO> createRequest(@Valid @RequestBody LeaveRequestDTO dto) {
        LeaveRequest request = new LeaveRequest(
            null, UUID.randomUUID(), dto.employeeId(), dto.leaveTypeId(),
            dto.startDate(), dto.endDate(), dto.startTime(), dto.endTime(),
            dto.totalDays(), dto.totalHours(), dto.reason(), dto.attachmentUrl(),
            LeaveRequest.LeaveStatus.PENDING, java.time.LocalDateTime.now(), null, null, null,
            null, null, null, null, null, null
        );

        LeaveRequest saved = leaveRequestRepository.save(request);
        webhookUseCase.triggerEvent("LEAVE_REQUESTED", Map.of("request", saved), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(LeaveRequestDTO.from(saved));
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "Get leave request by ID")
    public ResponseEntity<LeaveRequestDTO> getRequest(@PathVariable UUID id) {
        return leaveRequestRepository.findByUuid(id)
            .map(LeaveRequestDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/requests")
    @Operation(summary = "List leave requests with filters")
    public ResponseEntity<Page<LeaveRequestDTO>> listRequests(
            @Parameter(description = "Employee ID") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Status") @RequestParam(required = false) LeaveRequest.LeaveStatus status,
            @Parameter(description = "Start date") @RequestParam(required = false) LocalDate start,
            @Parameter(description = "End date") @RequestParam(required = false) LocalDate end,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LeaveRequestDTO> page;
        if (employeeId != null && status != null) {
            page = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status, pageable)
                .map(LeaveRequestDTO::from);
        } else if (employeeId != null && start != null && end != null) {
            page = leaveRequestRepository.findByEmployeeIdAndStartDateBetween(employeeId, start, end, pageable)
                .map(LeaveRequestDTO::from);
        } else if (employeeId != null) {
            page = leaveRequestRepository.findByEmployeeId(employeeId, pageable)
                .map(LeaveRequestDTO::from);
        } else if (status != null) {
            page = leaveRequestRepository.findByStatus(status, pageable)
                .map(LeaveRequestDTO::from);
        } else {
            page = leaveRequestRepository.findAll(pageable).map(LeaveRequestDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @PutMapping("/requests/{id}/approve")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<LeaveRequestDTO> approveRequest(
            @PathVariable UUID id,
            @RequestParam Long approvedBy,
            @RequestParam(required = false) String notes) {
        LeaveRequest approved = approveLeaveRequestUseCase.approve(id, approvedBy, notes);
        webhookUseCase.triggerEvent("LEAVE_APPROVED", Map.of("request", approved), null);
        return ResponseEntity.ok(LeaveRequestDTO.from(approved));
    }

    @PutMapping("/requests/{id}/reject")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<LeaveRequestDTO> rejectRequest(
            @PathVariable UUID id,
            @RequestParam Long rejectedBy,
            @RequestParam String reason) {
        LeaveRequest rejected = approveLeaveRequestUseCase.reject(id, rejectedBy, reason);
        webhookUseCase.triggerEvent("LEAVE_REJECTED", Map.of("request", rejected), null);
        return ResponseEntity.ok(LeaveRequestDTO.from(rejected));
    }

    @PutMapping("/requests/{id}/cancel")
    @Operation(summary = "Cancel a leave request")
    public ResponseEntity<LeaveRequestDTO> cancelRequest(
            @PathVariable UUID id,
            @RequestParam Long cancelledBy,
            @RequestParam String reason) {
        LeaveRequest cancelled = approveLeaveRequestUseCase.cancel(id, cancelledBy, reason);
        webhookUseCase.triggerEvent("LEAVE_CANCELLED", Map.of("request", cancelled), null);
        return ResponseEntity.ok(LeaveRequestDTO.from(cancelled));
    }

    @GetMapping("/balances/{employeeId}")
    @Operation(summary = "Get leave balances for employee")
    public ResponseEntity<List<LeaveBalanceDTO>> getBalances(
            @PathVariable Long employeeId,
            @Parameter(description = "Reference year") @RequestParam(required = false) Integer year) {
        int refYear = year != null ? year : LocalDate.now().getYear();
        List<LeaveBalanceDTO> balances = leaveBalanceRepository.findByEmployeeIdAndReferenceYear(employeeId, refYear)
            .stream().map(LeaveBalanceDTO::from).toList();
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/balances/accrue")
    @Operation(summary = "Accrue leave days (typically run by batch job)")
    public ResponseEntity<Void> accrueLeave(
            @RequestParam Long employeeId,
            @RequestParam Long leaveTypeId,
            @RequestParam BigDecimal days,
            @RequestParam int year) {
        // Implementation would update leave balance
        return ResponseEntity.ok().build();
    }
}