package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.application.usecase.ConfirmSalesOrderUseCase;
import com.erp.brazil.sales.domain.model.SalesOrder;
import com.erp.brazil.sales.domain.model.SalesOrderStatus;
import com.erp.brazil.sales.domain.repository.SalesOrderRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.SalesOrderDTO;
import com.erp.brazil.sales.presentation.dto.request.CancelSalesOrderRequest;
import com.erp.brazil.sales.presentation.dto.request.ConfirmSalesOrderRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesOrderItemRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesOrderRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateSalesOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController {

    private final SalesOrderRepository salesOrderRepository;
    private final ConfirmSalesOrderUseCase confirmSalesOrderUseCase;

    public SalesOrderController(SalesOrderRepository salesOrderRepository,
                                ConfirmSalesOrderUseCase confirmSalesOrderUseCase) {
        this.salesOrderRepository = salesOrderRepository;
        this.confirmSalesOrderUseCase = confirmSalesOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesOrderDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @RequestBody CreateSalesOrderRequest request) {
        SalesOrder order = new SalesOrder();
        order.setCompanyId(companyId);
        order.setNumber(generateOrderNumber(companyId));
        order.setSeries("1");
        order.setStatus(SalesOrderStatus.DRAFT);
        order.setOrderDate(request.orderDate() != null ? request.orderDate() : LocalDate.now());
        order.setDeliveryDate(request.deliveryDate());
        order.setPaymentTerms(request.paymentTerms());
        order.setDeliveryTerms(request.deliveryTerms());
        order.setNotes(request.notes());
        order.setInternalNotes(request.internalNotes());
        order.setCurrency("BRL");

        if (request.quotationId() != null) {
            order.setQuotationId(request.quotationId());
            order.setQuotation(new com.erp.brazil.sales.domain.model.Quotation());
            order.getQuotation().setId(request.quotationId());
        }
        if (request.opportunityId() != null) {
            order.setOpportunity(new com.erp.brazil.sales.domain.model.Opportunity());
            order.getOpportunity().setId(request.opportunityId());
        }
        order.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
        order.getCustomer().setId(request.customerId());
        if (request.contactId() != null) {
            order.setContact(new com.erp.brazil.sales.domain.model.Contact());
            order.getContact().setId(request.contactId());
        }
        if (request.billingAddressId() != null) {
            order.setBillingAddress(new com.erp.brazil.sales.domain.model.Address());
            order.getBillingAddress().setId(request.billingAddressId());
        }
        if (request.shippingAddressId() != null) {
            order.setShippingAddress(new com.erp.brazil.sales.domain.model.Address());
            order.getShippingAddress().setId(request.shippingAddressId());
        }
        if (request.salesPersonId() != null) {
            order.setSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            order.getSalesPerson().setId(request.salesPersonId());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        List<com.erp.brazil.sales.domain.model.SalesOrderItem> items = request.items().stream().map(itemReq -> {
            com.erp.brazil.sales.domain.model.SalesOrderItem item = new com.erp.brazil.sales.domain.model.SalesOrderItem();
            item.setSalesOrder(order);
            item.setLineNumber(itemReq.lineNumber());
            item.setProductId(itemReq.productId());
            item.setProductCode(itemReq.productCode());
            item.setProductName(itemReq.productName());
            item.setDescription(itemReq.description());
            item.setQuantity(itemReq.quantity());
            item.setUnitOfMeasure(itemReq.unitOfMeasure());
            item.setUnitPrice(itemReq.unitPrice());
            item.setDiscountPercent(itemReq.discountPercent() != null ? itemReq.discountPercent() : BigDecimal.ZERO);
            item.setTaxPercent(itemReq.taxPercent() != null ? itemReq.taxPercent() : BigDecimal.ZERO);
            item.setNotes(itemReq.notes());

            BigDecimal lineSubtotal = itemReq.unitPrice().multiply(itemReq.quantity());
            BigDecimal lineDiscount = lineSubtotal.multiply(item.getDiscountPercent()).divide(BigDecimal.valueOf(100));
            BigDecimal lineNet = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = lineNet.multiply(item.getTaxPercent()).divide(BigDecimal.valueOf(100));
            BigDecimal lineTotal = lineNet.add(lineTax);

            item.setDiscountAmount(lineDiscount);
            item.setTaxAmount(lineTax);
            item.setTotalAmount(lineTotal);
            item.setDeliveredQuantity(BigDecimal.ZERO);

            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);

            return item;
        }).collect(Collectors.toList());

        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(subtotal.subtract(discountAmount).add(taxAmount));

        order = salesOrderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(order)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @PathVariable UUID id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(order)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @PathVariable UUID id,
                                                              @RequestBody UpdateSalesOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + id));

        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new IllegalStateException("Can only update orders in DRAFT status");
        }

        if (request.orderDate() != null) order.setOrderDate(request.orderDate());
        if (request.deliveryDate() != null) order.setDeliveryDate(request.deliveryDate());
        if (request.paymentTerms() != null) order.setPaymentTerms(request.paymentTerms());
        if (request.deliveryTerms() != null) order.setDeliveryTerms(request.deliveryTerms());
        if (request.notes() != null) order.setNotes(request.notes());
        if (request.internalNotes() != null) order.setInternalNotes(request.internalNotes());
        if (request.customerId() != null) {
            order.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
            order.getCustomer().setId(request.customerId());
        }
        if (request.contactId() != null) {
            order.setContact(new com.erp.brazil.sales.domain.model.Contact());
            order.getContact().setId(request.contactId());
        }
        if (request.billingAddressId() != null) {
            order.setBillingAddress(new com.erp.brazil.sales.domain.model.Address());
            order.getBillingAddress().setId(request.billingAddressId());
        }
        if (request.shippingAddressId() != null) {
            order.setShippingAddress(new com.erp.brazil.sales.domain.model.Address());
            order.getShippingAddress().setId(request.shippingAddressId());
        }
        if (request.salesPersonId() != null) {
            order.setSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            order.getSalesPerson().setId(request.salesPersonId());
        }

        order = salesOrderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(order)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + id));

        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new IllegalStateException("Can only delete orders in DRAFT status");
        }

        salesOrderRepository.delete(order);
        return ResponseEntity.ok(ApiResponse.ok(null, "Sales order deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesOrderDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                          @RequestParam(required = false) SalesOrderStatus status,
                                                                          Pageable pageable) {
        Page<SalesOrder> page;
        if (status != null) {
            page = salesOrderRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = salesOrderRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> confirm(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @PathVariable UUID id,
                                                               @RequestBody ConfirmSalesOrderRequest request) {
        SalesOrderDTO order = confirmSalesOrderUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> cancel(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @PathVariable UUID id,
                                                              @RequestBody CancelSalesOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + id));

        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled. Status: " + order.getStatus());
        }

        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setCancelledAt(java.time.LocalDateTime.now());
        order.setCancelledById(request.cancelledById());
        order.setCancellationReason(request.cancellationReason());

        order = salesOrderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(order)));
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO.SalesOrderItemDTO>>> getReservations(@RequestHeader("X-Company-ID") UUID companyId,
                                                                                               @PathVariable UUID id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + id));

        List<SalesOrderDTO.SalesOrderItemDTO> items = order.getItems().stream().map(item ->
                new SalesOrderDTO.SalesOrderItemDTO(
                        item.getId(), item.getLineNumber(), item.getProductId(), item.getProductCode(),
                        item.getProductName(), item.getDescription(), item.getQuantity(), item.getUnitOfMeasure(),
                        item.getUnitPrice(), item.getDiscountPercent(), item.getDiscountAmount(),
                        item.getTaxPercent(), item.getTaxAmount(), item.getTotalAmount(),
                        item.getDeliveredQuantity(), item.getQuantity().subtract(item.getDeliveredQuantity()), item.getNotes()
                )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    private String generateOrderNumber(UUID companyId) {
        long count = salesOrderRepository.countByCompanyId(companyId);
        return String.format("ORD-%06d", count + 1);
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
                o.getConfirmedById(), null,
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

    private PageResponse<SalesOrderDTO> mapToPageResponse(Page<SalesOrder> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}