package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.domain.event.LeaveApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApproveLeaveRequestUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LeaveRequest approve(Long leaveRequestId, Long approvedBy, String notes) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!request.canBeCancelled() && request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        if (leaveType.isPaidByDefault() && leaveType.requiresApproval) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
                request.getEmployeeId(), leaveType.getId(), request.getStartDate().getYear())
                .orElseThrow(() -> new IllegalStateException("Leave balance not found"));

            if (!balance.hasSufficientBalance(request.getTotalDays())) {
                throw new IllegalStateException("Insufficient leave balance");
            }

            balance.addUsedDays(request.getTotalDays());
            balance.addPendingDays(request.getTotalDays().negate());
            leaveBalanceRepository.save(balance);
        }

        request.setStatus(LeaveRequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(java.time.Instant.now());
        request.setApprovalNotes(notes);

        LeaveRequest saved = leaveRequestRepository.save(request);

        Employee employee = employeeRepository.findById(request.getEmployeeId()).orElse(null);
        eventPublisher.publishEvent(LeaveApprovedEvent.builder()
            .leaveRequestId(saved.getId())
            .employeeId(request.getEmployeeId())
            .employeeName(employee != null ? employee.getFullName() : "")
            .leaveTypeCode(leaveType.getCode())
            .leaveTypeName(leaveType.getName())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .totalDays(request.getTotalDays())
            .approvalNotes(notes)
            .approvedBy(approvedBy)
            .companyId(employee != null ? employee.getCompanyId() : null)
            .build());

        log.info("Leave request {} approved by {}", leaveRequestId, approvedBy);
        return saved;
    }

    @Transactional
    public LeaveRequest reject(Long leaveRequestId, Long rejectedBy, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }

        if (request.getLeaveTypeId() != null) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
                request.getEmployeeId(), request.getLeaveTypeId(), request.getStartDate().getYear())
                .orElse(null);

            if (balance != null) {
                balance.cancelPendingDays(request.getTotalDays());
                leaveBalanceRepository.save(balance);
            }
        }

        request.setStatus(LeaveRequestStatus.REJECTED);
        request.setRejectedBy(rejectedBy);
        request.setRejectedAt(java.time.Instant.now());
        request.setRejectionReason(reason);

        LeaveRequest saved = leaveRequestRepository.save(request);
        log.info("Leave request {} rejected by {}", leaveRequestId, rejectedBy);
        return saved;
    }

    @Transactional
    public LeaveRequest cancel(Long leaveRequestId, Long cancelledBy, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!request.canBeCancelled()) {
            throw new IllegalStateException("Request cannot be cancelled in current status: " + request.getStatus());
        }

        if (request.getStatus() == LeaveRequestStatus.APPROVED && request.getLeaveTypeId() != null) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
                request.getEmployeeId(), request.getLeaveTypeId(), request.getStartDate().getYear())
                .orElse(null);

            if (balance != null) {
                balance.addUsedDays(request.getTotalDays().negate());
                leaveBalanceRepository.save(balance);
            }
        } else if (request.getStatus() == LeaveRequestStatus.PENDING && request.getLeaveTypeId() != null) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
                request.getEmployeeId(), request.getLeaveTypeId(), request.getStartDate().getYear())
                .orElse(null);

            if (balance != null) {
                balance.cancelPendingDays(request.getTotalDays());
                leaveBalanceRepository.save(balance);
            }
        }

        request.setStatus(LeaveRequestStatus.CANCELLED);
        request.setCancelledBy(cancelledBy);
        request.setCancelledAt(java.time.Instant.now());
        request.setCancellationReason(reason);

        LeaveRequest saved = leaveRequestRepository.save(request);
        log.info("Leave request {} cancelled by {}", leaveRequestId, cancelledBy);
        return saved;
    }

    @Transactional
    public LeaveRequest requestLeave(Long employeeId, Long leaveTypeId, LocalDate startDate, LocalDate endDate,
                                     String reason, String attachmentUrl) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
            .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot request leave for past dates");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal totalDays = BigDecimal.valueOf(days);

        if (leaveType.isPaidByDefault()) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
                employeeId, leaveTypeId, startDate.getYear())
                .orElseThrow(() -> new IllegalStateException("Leave balance not found for this year"));

            if (!balance.hasSufficientBalance(totalDays)) {
                throw new IllegalStateException("Insufficient leave balance. Available: " + balance.getAvailableDaysCalculated());
            }

            balance.addPendingDays(totalDays);
            leaveBalanceRepository.save(balance);
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(leaveType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setTotalDays(totalDays);
        request.setReason(reason);
        request.setAttachmentUrl(attachmentUrl);
        request.setStatus(LeaveRequestStatus.PENDING);
        request.setRequestedAt(java.time.Instant.now());

        LeaveRequest saved = leaveRequestRepository.save(request);
        log.info("Leave request created for employee {}: {} to {}", employeeId, startDate, endDate);
        return saved;
    }

    public List<LeaveRequest> getEmployeeRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public void accrueLeaveBalances(Long companyId, Integer year) {
        List<Employee> employees = employeeRepository.findByCompanyIdAndStatusIn(companyId,
            List.of(EmployeeStatus.ACTIVE, EmployeeStatus.ON_LEAVE));

        for (Employee employee : employees) {
            List<LeaveType> leaveTypes = leaveTypeRepository.findByCompanyIdAndIsActiveTrue(companyId);

            for (LeaveType leaveType : leaveTypes) {
                if (leaveType.getMaxDaysPerYear() != null) {
                    LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndReferenceYear(
                        employee.getId(), leaveType.getId(), year)
                        .orElseGet(() -> {
                            LeaveBalance lb = new LeaveBalance();
                            lb.setEmployee(employee);
                            lb.setLeaveType(leaveType);
                            lb.setReferenceYear(year);
                            return lb;
                        });

                    BigDecimal monthlyAccrual = BigDecimal.valueOf(leaveType.getMaxDaysPerYear())
                        .divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
                    balance.addAccruedDays(monthlyAccrual);
                    leaveBalanceRepository.save(balance);
                }
            }
        }
    }
}