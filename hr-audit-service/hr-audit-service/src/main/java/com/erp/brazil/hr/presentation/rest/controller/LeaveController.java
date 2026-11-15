package com.erp.brazil.hr.presentation.rest.controller;

import com.erp.brazil.hr.application.usecase.ApproveLeaveRequestUseCase;
import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.presentation.rest.dto.LeaveDto;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Leave requests and balances APIs")
public class LeaveController {

    private final ApproveLeaveRequestUseCase leaveUseCase;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    // Leave Types
    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List leave types")
    public ResponseEntity<List<LeaveDto.LeaveTypeResponse>> listLeaveTypes(@RequestParam Long companyId) {
        List<LeaveType> types = leaveTypeRepository.findByCompanyIdAndIsActiveTrue(companyId);
        return ResponseEntity.ok(types.stream().map(this::toLeaveTypeResponse).collect(Collectors.toList()));
    }

    @PostMapping("/types")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Create leave type")
    public ResponseEntity<LeaveDto.LeaveTypeResponse> createLeaveType(@Valid @RequestBody LeaveDto.LeaveTypeRequest request) {
        LeaveType type = new LeaveType();
        type.setCompanyId(request.getCompanyId());
        type.setCode(request.getCode());
        type.setName(request.getName());
        type.setDescription(request.getDescription());
        type.setCategory(request.getCategory());
        type.setMaxDaysPerYear(request.getMaxDaysPerYear());
        type.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : true);
        type.setRequiresDocument(request.getRequiresDocument() != null ? request.getRequiresDocument() : false);
        type.setPaid(request.getPaid() != null ? request.getPaid() : true);
        type.setAffectsBenefits(request.getAffectsBenefits() != null ? request.getAffectsBenefits() : false);
        type.setColor(request.getColor());
        type.setIsActive(true);

        LeaveType saved = leaveTypeRepository.save(type);
        return ResponseEntity.ok(toLeaveTypeResponse(saved));
    }

    // Leave Requests
    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Request leave")
    public ResponseEntity<LeaveDto.LeaveRequestResponse> requestLeave(@Valid @RequestBody LeaveDto.LeaveRequestRequest request) {
        LeaveRequest leaveRequest = leaveUseCase.requestLeave(
            request.getEmployeeId(),
            request.getLeaveTypeId(),
            request.getStartDate(),
            request.getEndDate(),
            request.getReason(),
            request.getAttachmentUrl()
        );
        return ResponseEntity.ok(toLeaveRequestResponse(leaveRequest));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List leave requests")
    public ResponseEntity<LeaveDto.PageResponse> listRequests(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());

        // Simplified - would need custom queries
        List<LeaveRequest> requests = employeeId != null ?
            leaveRequestRepository.findByEmployeeId(employeeId) :
            leaveRequestRepository.findByCompanyIdAndStatus(companyId, status != null ? status : LeaveRequestStatus.PENDING);

        return ResponseEntity.ok(LeaveDto.PageResponse.builder()
            .content(requests.stream().map(this::toLeaveRequestResponse).collect(Collectors.toList()))
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(requests.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .build());
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get leave request")
    public ResponseEntity<LeaveDto.LeaveRequestResponse> getRequest(@PathVariable Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        return ResponseEntity.ok(toLeaveRequestResponse(request));
    }

    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Approve leave request")
    public ResponseEntity<LeaveDto.LeaveRequestResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDto.ApprovalRequest request) {
        LeaveRequest approved = leaveUseCase.approve(id, getCurrentUserId(), request.getNotes());
        return ResponseEntity.ok(toLeaveRequestResponse(approved));
    }

    @PutMapping("/requests/{id}/reject")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Reject leave request")
    public ResponseEntity<LeaveDto.LeaveRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDto.RejectionRequest request) {
        LeaveRequest rejected = leaveUseCase.reject(id, getCurrentUserId(), request.getReason());
        return ResponseEntity.ok(toLeaveRequestResponse(rejected));
    }

    @PutMapping("/requests/{id}/cancel")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Cancel leave request")
    public ResponseEntity<LeaveDto.LeaveRequestResponse> cancel(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDto.CancellationRequest request) {
        LeaveRequest cancelled = leaveUseCase.cancel(id, getCurrentUserId(), request.getReason());
        return ResponseEntity.ok(toLeaveRequestResponse(cancelled));
    }

    // Leave Balances
    @GetMapping("/balances/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee leave balances")
    public ResponseEntity<List<LeaveDto.LeaveBalanceResponse>> getBalances(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year) {

        int referenceYear = year != null ? year : LocalDate.now().getYear();
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndReferenceYear(employeeId, referenceYear);
        return ResponseEntity.ok(balances.stream().map(this::toBalanceResponse).collect(Collectors.toList()));
    }

    @PostMapping("/accrue")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Accrue leave balances for all employees")
    public ResponseEntity<Void> accrueBalances(@RequestParam Long companyId, @RequestParam Integer year) {
        leaveUseCase.accrueLeaveBalances(companyId, year);
        return ResponseEntity.ok().build();
    }

    private LeaveDto.LeaveTypeResponse toLeaveTypeResponse(LeaveType type) {
        return LeaveDto.LeaveTypeResponse.builder()
            .id(type.getId())
            .uuid(type.getUuid())
            .code(type.getCode())
            .name(type.getName())
            .description(type.getDescription())
            .category(type.getCategory())
            .categoryDescription(type.getCategory().getDescription())
            .maxDaysPerYear(type.getMaxDaysPerYear())
            .requiresApproval(type.getRequiresApproval())
            .requiresDocument(type.getRequiresDocument())
            .paid(type.getPaid())
            .affectsBenefits(type.getAffectsBenefits())
            .color(type.getColor())
            .isActive(type.getIsActive())
            .build();
    }

    private LeaveDto.LeaveRequestResponse toLeaveRequestResponse(LeaveRequest request) {
        return LeaveDto.LeaveRequestResponse.builder()
            .id(request.getId())
            .uuid(request.getUuid())
            .employeeId(request.getEmployee().getId())
            .employeeName(request.getEmployee().getFullName())
            .leaveTypeId(request.getLeaveType().getId())
            .leaveTypeCode(request.getLeaveType().getCode())
            .leaveTypeName(request.getLeaveType().getName())
            .leaveCategory(request.getLeaveType().getCategory())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .totalDays(request.getTotalDays())
            .totalHours(request.getTotalHours())
            .reason(request.getReason())
            .attachmentUrl(request.getAttachmentUrl())
            .status(request.getStatus())
            .statusDescription(request.getStatus().getDescription())
            .requestedAt(request.getRequestedAt())
            .approvedAt(request.getApprovedAt())
            .approvedBy(request.getApprovedBy())
            .approvedByName(null)
            .approvalNotes(request.getApprovalNotes())
            .rejectedAt(request.getRejectedAt())
            .rejectedBy(request.getRejectedBy())
            .rejectedByName(null)
            .rejectionReason(request.getRejectionReason())
            .cancelledAt(request.getCancelledAt())
            .cancelledBy(request.getCancelledBy())
            .cancelledByName(null)
            .cancellationReason(request.getCancellationReason())
            .build();
    }

    private LeaveDto.LeaveBalanceResponse toBalanceResponse(LeaveBalance balance) {
        return LeaveDto.LeaveBalanceResponse.builder()
            .id(balance.getId())
            .uuid(balance.getUuid())
            .employeeId(balance.getEmployee().getId())
            .employeeName(balance.getEmployee().getFullName())
            .leaveTypeId(balance.getLeaveType().getId())
            .leaveTypeCode(balance.getLeaveType().getCode())
            .leaveTypeName(balance.getLeaveType().getName())
            .referenceYear(balance.getReferenceYear())
            .accruedDays(balance.getAccruedDays())
            .usedDays(balance.getUsedDays())
            .pendingDays(balance.getPendingDays())
            .carriedOverDays(balance.getCarriedOverDays())
            .expiredDays(balance.getExpiredDays())
            .availableDays(balance.getAvailableDaysCalculated())
            .lastAccrualDate(balance.getLastAccrualDate())
            .build();
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}