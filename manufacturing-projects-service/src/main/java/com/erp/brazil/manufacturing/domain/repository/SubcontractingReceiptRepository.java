package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.SubcontractingReceipt;
import com.erp.brazil.manufacturing.domain.model.SubcontractingReceipt.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubcontractingReceiptRepository extends JpaRepository<SubcontractingReceipt, UUID> {

    Optional<SubcontractingReceipt> findByReceiptNumber(String receiptNumber);

    List<SubcontractingReceipt> findBySubcontractingOrderId(UUID subcontractingOrderId);

    List<SubcontractingReceipt> findByStatus(ReceiptStatus status);

    Page<SubcontractingReceipt> findByStatus(ReceiptStatus status, Pageable pageable);

    @Query("SELECT sr FROM SubcontractingReceipt sr WHERE sr.subcontractingOrder.id = :orderId AND sr.status = 'PENDING_INSPECTION'")
    List<SubcontractingReceipt> findPendingInspectionByOrder(@Param("orderId") UUID orderId);

    @Query("SELECT sr FROM SubcontractingReceipt sr WHERE sr.receiptDate BETWEEN :startDate AND :endDate")
    List<SubcontractingReceipt> findByReceiptDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByReceiptNumber(String receiptNumber);
}