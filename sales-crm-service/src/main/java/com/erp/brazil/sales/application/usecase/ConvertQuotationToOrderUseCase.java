package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.*;
import com.erp.brazil.sales.domain.repository.*;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesOrderRequest;
import com.erp.brazil.sales.presentation.dto.SalesOrderDTO;
import com.erp.brazil.sales.infrastructure.event.SalesOrderConfirmedEvent;
import com.erp.brazil.sales.infrastructure.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConvertQuotationToOrderUseCase {

    private final QuotationRepository quotationRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final ContactRepository contactRepository;
    private final AddressRepository addressRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final EventPublisher eventPublisher;

    public ConvertQuotationToOrderUseCase(QuotationRepository quotationRepository,
                                          SalesOrderRepository salesOrderRepository,
                                          SalesOrderItemRepository salesOrderItemRepository,
                                          CustomerRepository customerRepository,
                                          ContactRepository contactRepository,
                                          AddressRepository addressRepository,
                                          OpportunityRepository opportunityRepository,
                                          SalesPersonRepository salesPersonRepository,
                                          EventPublisher eventPublisher) {
        this.quotationRepository = quotationRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.customerRepository = customerRepository;
        this.contactRepository = contactRepository;
        this.addressRepository = addressRepository;
        this.opportunityRepository = opportunityRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SalesOrderDTO execute(UUID quotationId, UUID createdById, CreateSalesOrderRequest request) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + quotationId));

        if (!quotation.canBeConverted()) {
            throw new IllegalStateException("Quotation cannot be converted to order. Status: " + quotation.getStatus() + ", Valid: " + quotation.isValid());
        }

        if (quotation.getSalesOrder() != null) {
            throw new IllegalStateException("Quotation already converted to order: " + quotation.getSalesOrder().getId());
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        if (!customer.getCompanyId().equals(quotation.getCompanyId())) {
            throw new IllegalArgumentException("Customer does not belong to company");
        }

        Contact contact = null;
        if (request.contactId() != null) {
            contact = contactRepository.findById(request.contactId())
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + request.contactId()));
        }

        Address billingAddress = null;
        if (request.billingAddressId() != null) {
            billingAddress = addressRepository.findById(request.billingAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Billing address not found: " + request.billingAddressId()));
        }

        Address shippingAddress = null;
        if (request.shippingAddressId() != null) {
            shippingAddress = addressRepository.findById(request.shippingAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipping address not found: " + request.shippingAddressId()));
        }

        Opportunity opportunity = null;
        if (request.opportunityId() != null) {
            opportunity = opportunityRepository.findById(request.opportunityId())
                    .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + request.opportunityId()));
        } else if (quotation.getOpportunity() != null) {
            opportunity = quotation.getOpportunity();
        }

        SalesPerson salesPerson = null;
        if (request.salesPersonId() != null) {
            salesPerson = salesPersonRepository.findById(request.salesPersonId())
                    .orElseThrow(() -> new IllegalArgumentException("Sales person not found: " + request.salesPersonId()));
        } else if (quotation.getCreatedBy() != null) {
            salesPerson = quotation.getCreatedBy();
        }

        String number = generateOrderNumber(quotation.getCompanyId());

        SalesOrder order = new SalesOrder();
        order.setCompanyId(quotation.getCompanyId());
        order.setNumber(number);
        order.setSeries("1");
        order.setQuotationId(quotation.getId());
        order.setStatus(SalesOrderStatus.DRAFT);
        order.setOrderDate(request.orderDate() != null ? request.orderDate() : LocalDate.now());
        order.setDeliveryDate(request.deliveryDate());
        order.setPaymentTerms(request.paymentTerms());
        order.setDeliveryTerms(request.deliveryTerms());
        order.setNotes(request.notes());
        order.setInternalNotes(request.internalNotes());
        order.setOpportunity(opportunity);
        order.setCustomer(customer);
        order.setContact(contact);
        order.setBillingAddress(billingAddress);
        order.setShippingAddress(shippingAddress);
        order.setSalesPerson(salesPerson);
        order.setCurrency(quotation.getCurrency());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        List<SalesOrderItem> items = quotation.getItems().stream().map(qItem -> {
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrder(order);
            item.setLineNumber(qItem.getLineNumber());
            item.setProductId(qItem.getProductId());
            item.setProductCode(qItem.getProductCode());
            item.setProductName(qItem.getProductName());
            item.setDescription(qItem.getDescription());
            item.setQuantity(qItem.getQuantity());
            item.setUnitOfMeasure(qItem.getUnitOfMeasure());
            item.setUnitPrice(qItem.getUnitPrice());
            item.setDiscountPercent(qItem.getDiscountPercent());
            item.setTaxPercent(qItem.getTaxPercent());
            item.setNotes(qItem.getNotes());

            BigDecimal lineSubtotal = qItem.getUnitPrice().multiply(qItem.getQuantity());
            BigDecimal lineDiscount = lineSubtotal.multiply(qItem.getDiscountPercent()).divide(BigDecimal.valueOf(100));
            BigDecimal lineNet = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = lineNet.multiply(qItem.getTaxPercent()).divide(BigDecimal.valueOf(100));
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

        quotation.setSalesOrder(order);
        quotation.setStatus(QuotationStatus.CONVERTED);
        quotationRepository.save(quotation);

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
                )).toList(),
                LocalDateTime.now()
        ));

        return mapToDTO(order);
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
}