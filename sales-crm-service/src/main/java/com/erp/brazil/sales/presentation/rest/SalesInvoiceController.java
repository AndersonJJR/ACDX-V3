package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.application.usecase.InvoiceSalesOrderUseCase;
import com.erp.brazil.sales.domain.model.SalesInvoice;
import com.erp.brazil.sales.domain.model.InvoiceStatus;
import com.erp.brazil.sales.domain.repository.SalesInvoiceRepository;
import com.erp.brazil.sales.infrastructure.feign.FiscalServiceClient;
import com.erp.brazil.sales.infrastructure.feign.NFEEmitRequest;
import com.erp.brazil.sales.infrastructure.feign.NFEEmitResponse;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.SalesInvoiceDTO;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesInvoiceItemRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesInvoiceRequest;
import com.erp.brazil.sales.presentation.dto.request.EmitNFERequest;
import com.erp.brazil.sales.presentation.dto.request.NFEEmittedResponse;
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
@RequestMapping("/api/v1/sales-invoices")
public class SalesInvoiceController {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final InvoiceSalesOrderUseCase invoiceSalesOrderUseCase;
    private final FiscalServiceClient fiscalServiceClient;

    public SalesInvoiceController(SalesInvoiceRepository salesInvoiceRepository,
                                  InvoiceSalesOrderUseCase invoiceSalesOrderUseCase,
                                  FiscalServiceClient fiscalServiceClient) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.invoiceSalesOrderUseCase = invoiceSalesOrderUseCase;
        this.fiscalServiceClient = fiscalServiceClient;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @RequestHeader("X-User-ID") UUID userId,
                                                                @RequestBody CreateSalesInvoiceRequest request) {
        SalesInvoiceDTO invoice = invoiceSalesOrderUseCase.execute(companyId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(invoice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesInvoiceDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                                 @PathVariable UUID id) {
        SalesInvoice invoice = salesInvoiceRepository.findById(id)
                .filter(i -> i.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales invoice not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(invoice)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesInvoiceDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                            @RequestParam(required = false) InvoiceStatus status,
                                                                            Pageable pageable) {
        Page<SalesInvoice> page;
        if (status != null) {
            page = salesInvoiceRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = salesInvoiceRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{id}/emit-nfe")
    public ResponseEntity<ApiResponse<NFEEmitResponse>> emitNFE(@RequestHeader("X-Company-ID") UUID companyId,
                                                                 @PathVariable UUID id) {
        SalesInvoice invoice = salesInvoiceRepository.findById(id)
                .filter(i -> i.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.AUTHORIZED) {
            throw new IllegalStateException("NF-e already emitted for this invoice");
        }

        try {
            NFEEmitRequest nfeRequest = buildNFERequest(invoice);
            NFEEmitResponse response = fiscalServiceClient.emitNFE(nfeRequest);

            invoice.setAccessKey(response.accessKey());
            invoice.setXmlPath(response.xmlPath());
            invoice.setXmlContent(response.xmlContent());
            invoice.setPdfContent(response.pdfContent());
            invoice.setStatus(InvoiceStatus.AUTHORIZED);
            invoice.setAuthorizedAt(response.authorizedAt());
            salesInvoiceRepository.save(invoice);

            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            invoice.setStatus(InvoiceStatus.ERROR);
            invoice.setNotes("NF-e emission failed: " + e.getMessage());
            salesInvoiceRepository.save(invoice);
            throw new RuntimeException("Failed to emit NF-e: " + e.getMessage(), e);
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<SalesInvoiceDTO>>> getOverdue(@RequestHeader("X-Company-ID") UUID companyId) {
        List<InvoiceStatus> paidStatuses = List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED);
        List<SalesInvoice> invoices = salesInvoiceRepository.findOverdueInvoices(companyId, LocalDate.now(), paidStatuses);
        return ResponseEntity.ok(ApiResponse.ok(invoices.stream().map(this::mapToDTO).toList()));
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

    private PageResponse<SalesInvoiceDTO> mapToPageResponse(Page<SalesInvoice> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}