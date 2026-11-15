package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ApproveLeaveRequestUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditEventUseCase auditEventUseCase;

    public ApproveLeaveRequestUseCase(LeaveRequestRepository leaveRequestRepository,
                                       LeaveBalanceRepository leaveBalanceRepository,
                                       EmployeeRepository employeeRepository,
                                       AuditEventUseCase auditEventUseCase) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.auditEventUseCase = auditEventUseCase;
    }

    @Transactional
    public LeaveRequest approve(Long requestId, Long approvedBy, String notes) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + requestId));

        if (!request.canApprove()) {
            throw new IllegalStateException("Request cannot be approved, current status: " + request.status());
        }

        // Check for overlapping approved requests
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
            request.employeeId(), request.startDate(), request.endDate(),
            List.of(LeaveRequest.LeaveStatus.APPROVED, LeaveRequest.LeaveStatus.PENDING), requestId);

        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Overlapping approved leave request exists");
        }

        // Check and update balance
        if (requiresBalanceDeduction(request)) {
            deductFromBalance(request.employeeId(), request.leaveTypeId(), request.totalDays());
        }

        LeaveRequest approved = new LeaveRequest(
            request.id(), request.uuid(), request.employeeId(), request.leaveTypeId(),
            request.startDate(), request.endDate(), request.startTime(), request.endTime(),
            request.totalDays(), request.totalHours(), request.reason(), request.attachmentUrl(),
            LeaveRequest.LeaveStatus.APPROVED, request.requestedAt(), LocalDateTime.now(),
            approvedBy, notes, null, null, null, null, null, null
        );

        LeaveRequest saved = leaveRequestRepository.save(approved);
        auditEventUseCase.logEvent("LEAVE_APPROVED", "LeaveRequest", saved.uuid().toString(),
            getCompanyId(saved.employeeId()), approvedBy, saved);

        return saved;
    }

    @Transactional
    public LeaveRequest reject(Long requestId, Long rejectedBy, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + requestId));

        if (!request.canApprove()) {
            throw new IllegalStateException("Request cannot be rejected, current status: " + request.status());
        }

        LeaveRequest rejected = new LeaveRequest(
            request.id(), request.uuid(), request.employeeId(), request.leaveTypeId(),
            request.startDate(), request.endDate(), request.startTime(), request.endTime(),
            request.totalDays(), request.totalHours(), request.reason(), request.attachmentUrl(),
            LeaveRequest.LeaveStatus.REJECTED, request.requestedAt(), null, null, null,
            LocalDateTime.now(), rejectedBy, reason, null, null, null
        );

        LeaveRequest saved = leaveRequestRepository.save(rejected);
        auditEventUseCase.logEvent("LEAVE_REJECTED", "LeaveRequest", saved.uuid().toString(),
            getCompanyId(saved.employeeId()), rejectedBy, saved);

        return saved;
    }

    @Transactional
    public LeaveRequest cancel(Long requestId, Long cancelledBy, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + requestId));

        if (request.status() == LeaveRequest.LeaveStatus.CANCELLED) {
            throw new IllegalStateException("Request already cancelled");
        }

        // Restore balance if was approved
        if (request.status() == LeaveRequest.LeaveStatus.APPROVED && requiresBalanceDeduction(request)) {
            restoreBalance(request.employeeId(), request.leaveTypeId(), request.totalDays());
        }

        LeaveRequest cancelled = new LeaveRequest(
            request.id(), request.uuid(), request.employeeId(), request.leaveTypeId(),
            request.startDate(), request.endDate(), request.startTime(), request.endTime(),
            request.totalDays(), request.totalHours(), request.reason(), request.attachmentUrl(),
            LeaveRequest.LeaveStatus.CANCELLED, request.requestedAt(), request.approvedAt(),
            request.approvedBy(), request.approvalNotes(), request.rejectedAt(),
            request.rejectedBy(), request.rejectionReason(), LocalDateTime.now(), cancelledBy, reason
        );

        LeaveRequest saved = leaveRequestRepository.save(cancelled);
        auditEventUseCase.logEvent("LEAVE_CANCELLED", "LeaveRequest", saved.uuid().toString(),
            getCompanyId(saved.employeeId()), cancelledBy, saved);

        return saved;
    }

    private boolean requiresBalanceDeduction(LeaveRequest request) {
        // Vacation, sick leave, unpaid leave typically deduct from balance
        return true; // Simplified - would check leave type configuration
    }

    private void deductFromBalance(Long employeeId, Long leaveTypeId, BigDecimal days) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
            employeeId, leaveTypeId, LocalDate.now().getYear())
            .orElseThrow(() -> new IllegalStateException("Leave balance not found"));

        if (!balance.hasAvailableDays(days)) {
            throw new IllegalStateException("Insufficient leave balance");
        }

        LeaveBalance updated = new LeaveBalance(
            balance.id(), balance.uuid(), balance.employeeId(), balance.leaveTypeId(),
            balance.referenceYear(), balance.accruedDays(),
            balance.usedDays().add(days), balance.pendingDays().subtract(days),
            balance.carriedOverDays(), balance.expiredDays(), balance.lastAccrualDate()
        );

        leaveBalanceRepository.save(updated);
    }

    private void restoreBalance(Long employeeId, Long leaveTypeId, BigDecimal days) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
            employeeId, leaveTypeId, LocalDate.now().getYear())
            .orElseThrow(() -> new IllegalStateException("Leave balance not found"));

        LeaveBalance updated = new LeaveBalance(
            balance.id(), balance.uuid(), balance.employeeId(), balance.leaveTypeId(),
            balance.referenceYear(), balance.accruedDays(),
            balance.usedDays().subtract(days), balance.pendingDays(),
            balance.carriedOverDays(), balance.expiredDays(), balance.lastAccrualDate()
        );

        leaveBalanceRepository.save(updated);
    }

    private Long getCompanyId(Long employeeId) {
        return employeeRepository.findById(employeeId)
            .map(Employee::companyId)
            .orElse(null);
    }
}