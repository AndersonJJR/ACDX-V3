package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.*;
import com.erp.brazil.sales.domain.repository.*;
import com.erp.brazil.sales.infrastructure.feign.FiscalServiceClient;
import com.erp.brazil.sales.infrastructure.feign.NFEEmitRequest;
import com.erp.brazil.sales.infrastructure.feign.NFEEmitResponse;
import com.erp.brazil.sales.infrastructure.event.SalesInvoiceCreatedEvent;
import com.erp.brazil.sales.infrastructure.event.EventPublisher;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesInvoiceRequest;
import com.erp.brazil.sales.presentation.dto.SalesInvoiceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceSalesOrderUseCase {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceItemRepository salesInvoiceItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final FiscalServiceClient fiscalServiceClient;
    private final EventPublisher eventPublisher;

    public InvoiceSalesOrderUseCase(SalesInvoiceRepository salesInvoiceRepository,
                                    SalesInvoiceItemRepository salesInvoiceItemRepository,
                                    SalesOrderRepository salesOrderRepository,
                                    DeliveryNoteRepository deliveryNoteRepository,
                                    CustomerRepository customerRepository,
                                    AddressRepository addressRepository,
                                    FiscalServiceClient fiscalServiceClient,
                                    EventPublisher eventPublisher) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesInvoiceItemRepository = salesInvoiceItemRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.fiscalServiceClient = fiscalServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SalesInvoiceDTO execute(UUID companyId, UUID createdById, CreateSalesInvoiceRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        if (!customer.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Customer does not belong to company");
        }

        SalesOrder salesOrder = null;
        if (request.salesOrderId() != null) {
            salesOrder = salesOrderRepository.findById(request.salesOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + request.salesOrderId()));
            if (!salesOrder.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Sales order does not belong to customer");
            }
        }

        DeliveryNote deliveryNote = null;
        if (request.deliveryNoteId() != null) {
            deliveryNote = deliveryNoteRepository.findById(request.deliveryNoteId())
                    .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + request.deliveryNoteId()));
        }

        Address billingAddress = null;
        if (request.billingAddressId() != null) {
            billingAddress = addressRepository.findById(request.billingAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Billing address not found: " + request.billingAddressId()));
        }

        String number = generateInvoiceNumber(companyId);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setCompanyId(companyId);
        invoice.setNumber(number);
        invoice.setSeries("1");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(request.issueDate());
        invoice.setOperationDate(request.operationDate() != null ? request.operationDate() : request.issueDate());
        invoice.setDueDate(request.dueDate());
        invoice.setInvoiceType(request.invoiceType());
        invoice.setNatureOfOperation(request.natureOfOperation());
        invoice.setCurrency(request.currency() != null ? request.currency() : "BRL");
        invoice.setSalesOrder(salesOrder);
        invoice.setDeliveryNoteId(deliveryNote != null ? deliveryNote.getId() : null);
        invoice.setCustomer(customer);
        invoice.setBillingAddress(billingAddress);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxIcms = BigDecimal.ZERO;
        BigDecimal taxIpi = BigDecimal.ZERO;
        BigDecimal taxPis = BigDecimal.ZERO;
        BigDecimal taxCofins = BigDecimal.ZERO;
        BigDecimal taxIss = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        List<SalesInvoiceItem> items = request.items().stream().map(itemReq -> {
            SalesInvoiceItem item = new SalesInvoiceItem();
            item.setSalesInvoice(invoice);
            item.setLineNumber(itemReq.lineNumber());
            item.setProductId(itemReq.productId());
            item.setProductCode(itemReq.productCode());
            item.setProductName(itemReq.productName());
            item.setDescription(itemReq.description());
            item.setQuantity(itemReq.quantity());
            item.setUnitOfMeasure(itemReq.unitOfMeasure());
            item.setUnitPrice(itemReq.unitPrice());
            item.setDiscountAmount(itemReq.discountAmount() != null ? itemReq.discountAmount() : BigDecimal.ZERO);
            item.setTaxIcms(itemReq.taxIcms() != null ? itemReq.taxIcms() : BigDecimal.ZERO);
            item.setTaxIpi(itemReq.taxIpi() != null ? itemReq.taxIpi() : BigDecimal.ZERO);
            item.setTaxPis(itemReq.taxPis() != null ? itemReq.taxPis() : BigDecimal.ZERO);
            item.setTaxCofins(itemReq.taxCofins() != null ? itemReq.taxCofins() : BigDecimal.ZERO);
            item.setTaxIss(itemReq.taxIss() != null ? itemReq.taxIss() : BigDecimal.ZERO);
            item.setCfop(itemReq.cfop());
            item.setNcm(itemReq.ncm());
            item.setCest(itemReq.cest());

            BigDecimal lineSubtotal = itemReq.unitPrice().multiply(itemReq.quantity());
            BigDecimal lineNet = lineSubtotal.subtract(item.getDiscountAmount());
            BigDecimal lineTaxTotal = item.getTaxIcms().add(item.getTaxIpi())
                    .add(item.getTaxPis()).add(item.getTaxCofins()).add(item.getTaxIss());
            BigDecimal lineTotal = lineNet.add(lineTaxTotal);

            item.setTotalTax(lineTaxTotal);
            item.setTotalAmount(lineTotal);

            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(item.getDiscountAmount());
            taxIcms = taxIcms.add(item.getTaxIcms());
            taxIpi = taxIpi.add(item.getTaxIpi());
            taxPis = taxPis.add(item.getTaxPis());
            taxCofins = taxCofins.add(item.getTaxCofins());
            taxIss = taxIss.add(item.getTaxIss());
            totalTax = totalTax.add(lineTaxTotal);

            return item;
        }).collect(Collectors.toList());

        invoice.setItems(items);
        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTaxIcms(taxIcms);
        invoice.setTaxIpi(taxIpi);
        invoice.setTaxPis(taxPis);
        invoice.setTaxCofins(taxCofins);
        invoice.setTaxIss(taxIss);
        invoice.setTotalTax(totalTax);
        invoice.setTotalAmount(subtotal.subtract(discountAmount).add(totalTax));

        invoice = salesInvoiceRepository.save(invoice);

        try {
            NFEEmitRequest nfeRequest = buildNFERequest(invoice);
            NFEEmitResponse nfeResponse = fiscalServiceClient.emitNFE(nfeRequest);

            invoice.setAccessKey(nfeResponse.accessKey());
            invoice.setXmlPath(nfeResponse.xmlPath());
            invoice.setXmlContent(nfeResponse.xmlContent());
            invoice.setPdfContent(nfeResponse.pdfContent());
            invoice.setStatus(InvoiceStatus.AUTHORIZED);
            invoice.setAuthorizedAt(nfeResponse.authorizedAt());
            invoice = salesInvoiceRepository.save(invoice);

            eventPublisher.publish(new SalesInvoiceCreatedEvent(
                    invoice.getId(),
                    invoice.getCompanyId(),
                    invoice.getNumber(),
                    invoice.getAccessKey(),
                    invoice.getCustomer().getId(),
                    invoice.getTotalAmount(),
                    invoice.getAuthorizedAt()
            ));

        } catch (Exception e) {
            invoice.setStatus(InvoiceStatus.ERROR);
            invoice.setNotes("NF-e emission failed: " + e.getMessage());
            salesInvoiceRepository.save(invoice);
            throw new RuntimeException("Failed to emit NF-e: " + e.getMessage(), e);
        }

        if (salesOrder != null) {
            boolean allInvoiced = salesOrder.getInvoices().stream()
                    .allMatch(i -> i.getStatus() == InvoiceStatus.AUTHORIZED);
            if (allInvoiced) {
                salesOrder.setStatus(SalesOrderStatus.INVOICED);
                salesOrderRepository.save(salesOrder);
            }
        }

        return mapToDTO(invoice);
    }

    private NFEEmitRequest buildNFERequest(SalesInvoice invoice) {
        return new NFEEmitRequest(
                invoice.getCompanyId().toString(),
                invoice.getNumber(),
                invoice.getSeries(),
                invoice.getInvoiceType(),
                invoice.getNatureOfOperation(),
                invoice.getIssueDate().toString(),
                invoice.getOperationDate().toString(),
                invoice.getCustomer().getDocumentType(),
                invoice.getCustomer().getDocumentNumber(),
                invoice.getCustomer().getLegalName(),
                invoice.getCustomer().getTradeName(),
                invoice.getBillingAddress() != null ? invoice.getBillingAddress().getZipCode() : "",
                invoice.getBillingAddress() != null ? invoice.getBillingAddress().getStreet() : "",
                invoice.getBillingAddress() != null ? invoice.getBillingAddress().getNumber() : "",
                invoice.getBillingAddress() != null ? invoice.getBillingAddress().getNeighborhood() : "",
                invoice.getBillingAddress() != null ? invoice.getBillingAddress().getCity() : "",
                invoice.getBillingAddress() != null ? invoice.getBillingAddress().getState() : "",
                invoice.getItems().stream().map(item -> new NFEEmitRequest.Item(
                        item.getProductCode(),
                        item.getProductName(),
                        item.getNcm(),
                        item.getCfop(),
                        item.getUnitOfMeasure(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getDiscountAmount(),
                        item.getTaxIcms(),
                        item.getTaxIpi(),
                        item.getTaxPis(),
                        item.getTaxCofins(),
                        item.getTaxIss()
                )).collect(Collectors.toList()),
                invoice.getTotalAmount()
        );
    }

    private String generateInvoiceNumber(UUID companyId) {
        long count = salesInvoiceRepository.countByCompanyId(companyId);
        return String.format("INV-%06d", count + 1);
    }

    private SalesInvoiceDTO mapToDTO(SalesInvoice inv) {
        List<SalesInvoiceDTO.SalesInvoiceItemDTO> itemDTOs = inv.getItems().stream().map(item ->
                new SalesInvoiceDTO.SalesInvoiceItemDTO(
                        item.getId(), item.getLineNumber(), item.getProductId(), item.getProductCode(),
                        item.getProductName(), item.getDescription(), item.getQuantity(), item.getUnitOfMeasure(),
                        item.getUnitPrice(), item.getDiscountAmount(), item.getTaxIcms(), item.getTaxIpi(),
                        item.getTaxPis(), item.getTaxCofins(), item.getTaxIss(), item.getTotalTax(),
                        item.getTotalAmount(), item.getCfop(), item.getNcm(), item.getCest()
                )
        ).collect(Collectors.toList());

        List<SalesInvoiceDTO.PaymentDTO> paymentDTOs = inv.getPayments().stream().map(p ->
                new SalesInvoiceDTO.PaymentDTO(
                        p.getId(), p.getPaymentMethod(), p.getAmount(), p.getDueDate(),
                        p.getPaidDate(), p.getStatus(), p.getTransactionId(), p.getNotes()
                )
        ).collect(Collectors.toList());

        return new SalesInvoiceDTO(
                inv.getId(), inv.getCompanyId(), inv.getNumber(), inv.getSeries(),
                inv.getAccessKey(), inv.getXmlPath(), inv.getStatus(),
                inv.getIssueDate(), inv.getOperationDate(), inv.getDueDate(),
                inv.getInvoiceType(), inv.getNatureOfOperation(),
                inv.getSubtotal(), inv.getDiscountAmount(), inv.getTaxIcms(), inv.getTaxIpi(),
                inv.getTaxPis(), inv.getTaxCofins(), inv.getTaxIss(), inv.getTotalTax(),
                inv.getFreightValue(), inv.getInsuranceValue(), inv.getOtherExpenses(), inv.getTotalAmount(),
                inv.getCurrency(), inv.getAuthorizedAt(), inv.getCancelledAt(), inv.getCancelledById(),
                null, inv.getCancellationReason(), inv.getXmlContent(),
                inv.getSalesOrder() != null ? inv.getSalesOrder().getId() : null,
                inv.getSalesOrder() != null ? inv.getSalesOrder().getNumber() : null,
                inv.getDeliveryNoteId(),
                inv.getDeliveryNote() != null ? inv.getDeliveryNote().getNumber() : null,
                inv.getCustomer() != null ? inv.getCustomer().getId() : null,
                inv.getCustomer() != null ? inv.getCustomer().getTradeName() : null,
                inv.getBillingAddress() != null ? inv.getBillingAddress().getId() : null,
                inv.getCreatedAt(), inv.getUpdatedAt(), itemDTOs, paymentDTOs
        );
    }
}