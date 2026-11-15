package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.SalesOrder;
import com.erp.brazil.sales.domain.model.SalesOrderStatus;
import com.erp.brazil.sales.domain.repository.SalesOrderRepository;
import com.erp.brazil.sales.domain.repository.SalesPersonRepository;
import com.erp.brazil.sales.infrastructure.event.SalesOrderConfirmedEvent;
import com.erp.brazil.sales.infrastructure.event.EventPublisher;
import com.erp.brazil.sales.presentation.dto.request.ConfirmSalesOrderRequest;
import com.erp.brazil.sales.presentation.dto.SalesOrderDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConfirmSalesOrderUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final EventPublisher eventPublisher;

    public ConfirmSalesOrderUseCase(SalesOrderRepository salesOrderRepository,
                                    SalesPersonRepository salesPersonRepository,
                                    EventPublisher eventPublisher) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SalesOrderDTO execute(UUID orderId, ConfirmSalesOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        if (!order.canBeConfirmed()) {
            throw new IllegalStateException("Order cannot be confirmed. Status: " + order.getStatus() + ", Items: " + order.getItems().size());
        }

        SalesPerson confirmedBy = salesPersonRepository.findById(request.confirmedById())
                .orElseThrow(() -> new IllegalArgumentException("Sales person not found: " + request.confirmedById()));

        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        order.setConfirmedById(request.confirmedById());

        order = salesOrderRepository.save(order);

        eventPublisher.publish(new SalesOrderConfirmedEvent(
                order.getId(),
                order.getCompanyId(),
                order.getNumber(),
                order.getCustomer().getId(),
                order.getTotalAmount(),
                order.getItems().stream().map(item -> new SalesOrderConfirmedEvent.OrderItemEvent(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getWarehouseId()
                )).collect(Collectors.toList()),
                LocalDateTime.now()
        ));

        return mapToDTO(order);
    }

    private SalesOrderDTO mapToDTO(SalesOrder o) {
        List<SalesOrderDTO.SalesOrderItemDTO> itemDTOs = o.getItems().stream().map(item ->
                new SalesOrderDTO.SalesOrderItemDTO(
                        item.getId(), item.getLineNumber(), item.getProductId(), item.getProductCode(),
                        item.getProductName(), item.getDescription(), item.getQuantity(), item.getUnitOfMeasure(),
                        item.getUnitPrice(), item.getDiscountPercent(), item.getDiscountAmount(),
                        item.getTaxPercent(), item.getTaxAmount(), item.getTotalAmount(),
                        item.getDeliveredQuantity(), item.getQuantity().subtract(item.getDeliveredQuantity()), item.getNotes()
                )
        ).collect(Collectors.toList());

        return new SalesOrderDTO(
                o.getId(), o.getCompanyId(), o.getNumber(), o.getSeries(), o.getQuotationId(),
                o.getQuotation() != null ? o.getQuotation().getNumber() : null,
                o.getStatus(), o.getOrderDate(), o.getDeliveryDate(), o.getConfirmedAt(),
                o.getConfirmedById(), o.getConfirmedById() != null ? "User" : null,
                o.getSubtotal(), o.getDiscountPercent(), o.getDiscountAmount(), o.getTaxAmount(), o.getTotalAmount(),
                o.getShippingAmount(), o.getCurrency(), o.getPaymentTerms(), o.getDeliveryTerms(),
                o.getNotes(), o.getInternalNotes(),
                o.getCancelledAt(), o.getCancelledById(), null, o.getCancellationReason(),
                o.getOpportunity() != null ? o.getOpportunity().getId() : null,
                o.getOpportunity() != null ? o.getOpportunity().getOpportunityCode() : null,
                o.getCustomer() != null ? o.getCustomer().getId() : null,
                o.getCustomer() != null ? o.getCustomer().getTradeName() : null,
                o.getContact() != null ? o.getContact().getId() : null,
                o.getContact() != null ? o.getContact().getName() : null,
                o.getBillingAddress() != null ? o.getBillingAddress().getId() : null,
                o.getShippingAddress() != null ? o.getShippingAddress().getId() : null,
                o.getSalesPerson() != null ? o.getSalesPerson().getId() : null,
                o.getSalesPerson() != null ? o.getSalesPerson().getFullName() : null,
                o.getCreatedAt(), o.getUpdatedAt(),
                itemDTOs, java.util.List.of(), java.util.List.of()
        );
    }
}