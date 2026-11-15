package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder;
import com.erp.brazil.manufacturing.domain.model.SubcontractingReceipt;
import com.erp.brazil.manufacturing.domain.repository.SubcontractingOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.SubcontractingReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ReceiveSubcontractedUseCase {

    private final SubcontractingOrderRepository subcontractingOrderRepository;
    private final SubcontractingReceiptRepository receiptRepository;

    public ReceiveSubcontractedUseCase(SubcontractingOrderRepository subcontractingOrderRepository,
                                        SubcontractingReceiptRepository receiptRepository) {
        this.subcontractingOrderRepository = subcontractingOrderRepository;
        this.receiptRepository = receiptRepository;
    }

    public SubcontractingReceipt receiveGoods(UUID subcontractingOrderId,
                                               BigDecimal receivedQuantity,
                                               BigDecimal acceptedQuantity,
                                               BigDecimal rejectedQuantity,
                                               UUID warehouseId,
                                               UUID locationId,
                                               String receiptNumber,
                                               String receivedBy) {
        SubcontractingOrder order = subcontractingOrderRepository.findById(subcontractingOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Subcontracting Order not found: " + subcontractingOrderId));

        if (order.getStatus() == SubcontractingOrder.SubcontractingStatus.COMPLETED ||
            order.getStatus() == SubcontractingOrder.SubcontractingStatus.CLOSED ||
            order.getStatus() == SubcontractingOrder.SubcontractingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot receive goods for order in status: " + order.getStatus());
        }

        SubcontractingReceipt receipt = SubcontractingReceipt.builder()
            .subcontractingOrder(order)
            .receiptNumber(receiptNumber != null ? receiptNumber : generateReceiptNumber())
            .receiptDate(LocalDateTime.now())
            .productId(order.getProductId())
            .receivedQuantity(receivedQuantity)
            .acceptedQuantity(acceptedQuantity)
            .rejectedQuantity(rejectedQuantity)
            .unitOfMeasure(order.getUnitOfMeasure())
            .warehouseId(warehouseId)
            .locationId(locationId)
            .status(SubcontractingReceipt.ReceiptStatus.PENDING_INSPECTION)
            .build();

        SubcontractingReceipt saved = receiptRepository.save(receipt);

        // Update order quantities
        order.setReceivedQuantity(order.getReceivedQuantity().add(acceptedQuantity));
        order.setRejectedQuantity(order.getRejectedQuantity().add(rejectedQuantity));

        // Update order status
        if (order.getReceivedQuantity().compareTo(order.getOrderedQuantity()) >= 0) {
            order.setStatus(SubcontractingOrder.SubcontractingStatus.RECEIVED);
            order.setActualEndDate(LocalDateTime.now());
        } else {
            order.setStatus(SubcontractingOrder.SubcontractingStatus.PARTIAL_RECEIPT);
        }

        subcontractingOrderRepository.save(order);

        // Add receipt to order
        order.addReceipt(saved);

        return saved;
    }

    public SubcontractingReceipt inspectReceipt(UUID receiptId,
                                                 BigDecimal acceptedQuantity,
                                                 BigDecimal rejectedQuantity,
                                                 UUID qualityInspectionId,
                                                 String inspectedBy) {
        SubcontractingReceipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptId));

        receipt.setAcceptedQuantity(acceptedQuantity);
        receipt.setRejectedQuantity(rejectedQuantity);
        receipt.setQualityInspectionId(qualityInspectionId);

        if (rejectedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            receipt.setStatus(SubcontractingReceipt.ReceiptStatus.INSPECTION_FAILED);
        } else {
            receipt.setStatus(SubcontractingReceipt.ReceiptStatus.INSPECTION_PASSED);
        }

        SubcontractingOrder order = receipt.getSubcontractingOrder();
        order.setReceivedQuantity(order.getReceivedQuantity()
            .subtract(receipt.getAcceptedQuantity())
            .add(acceptedQuantity));
        order.setRejectedQuantity(order.getRejectedQuantity()
            .subtract(receipt.getRejectedQuantity())
            .add(rejectedQuantity));

        if (order.getReceivedQuantity().compareTo(order.getOrderedQuantity()) >= 0) {
            order.setStatus(SubcontractingOrder.SubcontractingStatus.RECEIVED);
        }

        subcontractingOrderRepository.save(order);
        return receiptRepository.save(receipt);
    }

    public void acceptReceipt(UUID receiptId, String acceptedBy) {
        SubcontractingReceipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptId));

        receipt.setStatus(SubcontractingReceipt.ReceiptStatus.ACCEPTED);
        receiptRepository.save(receipt);
    }

    public void rejectReceipt(UUID receiptId, String reason, String rejectedBy) {
        SubcontractingReceipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptId));

        receipt.setStatus(SubcontractingReceipt.ReceiptStatus.REJECTED);
        receipt.setNotes(reason);
        receiptRepository.save(receipt);
    }

    private String generateReceiptNumber() {
        long count = receiptRepository.count() + 1;
        return String.format("SCR-%06d", count);
    }
}