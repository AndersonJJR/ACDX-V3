package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.application.usecase.CreateQuotationUseCase;
import com.erp.brazil.sales.application.usecase.ConvertQuotationToOrderUseCase;
import com.erp.brazil.sales.domain.model.Quotation;
import com.erp.brazil.sales.domain.model.QuotationStatus;
import com.erp.brazil.sales.domain.repository.QuotationRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.QuotationDTO;
import com.erp.brazil.sales.presentation.dto.SalesOrderDTO;
import com.erp.brazil.sales.presentation.dto.request.AcceptQuotationRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateQuotationItemRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateQuotationRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateQuotationVersionRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesOrderRequest;
import com.erp.brazil.sales.presentation.dto.request.RejectQuotationRequest;
import com.erp.brazil.sales.presentation.dto.request.SendQuotationRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateQuotationRequest;
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
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    private final QuotationRepository quotationRepository;
    private final CreateQuotationUseCase createQuotationUseCase;
    private final ConvertQuotationToOrderUseCase convertQuotationUseCase;

    public QuotationController(QuotationRepository quotationRepository,
                               CreateQuotationUseCase createQuotationUseCase,
                               ConvertQuotationToOrderUseCase convertQuotationUseCase) {
        this.quotationRepository = quotationRepository;
        this.createQuotationUseCase = createQuotationUseCase;
        this.convertQuotationUseCase = convertQuotationUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuotationDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @RequestHeader("X-User-ID") UUID userId,
                                                             @RequestBody CreateQuotationRequest request) {
        QuotationDTO quotation = createQuotationUseCase.execute(companyId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(quotation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @PathVariable UUID id) {
        Quotation q = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(q)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id,
                                                             @RequestBody UpdateQuotationRequest request) {
        Quotation q = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));

        if (q.getStatus() != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Can only update quotations in DRAFT status");
        }

        if (request.title() != null) q.setTitle(request.title());
        if (request.description() != null) q.setDescription(request.description());
        if (request.validFrom() != null) q.setValidFrom(request.validFrom());
        if (request.validUntil() != null) q.setValidUntil(request.validUntil());
        if (request.paymentTerms() != null) q.setPaymentTerms(request.paymentTerms());
        if (request.deliveryTerms() != null) q.setDeliveryTerms(request.deliveryTerms());
        if (request.notes() != null) q.setNotes(request.notes());
        if (request.internalNotes() != null) q.setInternalNotes(request.internalNotes());
        if (request.opportunityId() != null) {
            q.setOpportunity(new com.erp.brazil.sales.domain.model.Opportunity());
            q.getOpportunity().setId(request.opportunityId());
        }
        if (request.customerId() != null) {
            q.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
            q.getCustomer().setId(request.customerId());
        }
        if (request.contactId() != null) {
            q.setContact(new com.erp.brazil.sales.domain.model.Contact());
            q.getContact().setId(request.contactId());
        }
        if (request.billingAddressId() != null) {
            q.setBillingAddress(new com.erp.brazil.sales.domain.model.Address());
            q.getBillingAddress().setId(request.billingAddressId());
        }
        if (request.shippingAddressId() != null) {
            q.setShippingAddress(new com.erp.brazil.sales.domain.model.Address());
            q.getShippingAddress().setId(request.shippingAddressId());
        }

        q = quotationRepository.save(q);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(q)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        Quotation q = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));

        if (q.getStatus() != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Can only delete quotations in DRAFT status");
        }

        quotationRepository.delete(q);
        return ResponseEntity.ok(ApiResponse.ok(null, "Quotation deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<QuotationDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                         @RequestParam(required = false) QuotationStatus status,
                                                                         Pageable pageable) {
        Page<Quotation> page;
        if (status != null) {
            page = quotationRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = quotationRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<QuotationDTO>> send(@RequestHeader("X-Company-ID") UUID companyId,
                                                           @PathVariable UUID id,
                                                           @RequestBody SendQuotationRequest request) {
        Quotation q = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));

        if (q.getStatus() != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Can only send quotations in DRAFT status");
        }

        q.setStatus(QuotationStatus.SENT);
        q.setSentAt(java.time.LocalDateTime.now());
        q = quotationRepository.save(q);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(q)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<QuotationDTO>> accept(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id,
                                                             @RequestBody AcceptQuotationRequest request) {
        Quotation q = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));

        if (q.getStatus() != QuotationStatus.SENT) {
            throw new IllegalStateException("Can only accept quotations in SENT status");
        }

        if (!q.isValid()) {
            throw new IllegalStateException("Quotation has expired");
        }

        q.setStatus(QuotationStatus.ACCEPTED);
        q.setAcceptedAt(java.time.LocalDateTime.now());
        q = quotationRepository.save(q);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(q)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<QuotationDTO>> reject(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id,
                                                             @RequestBody RejectQuotationRequest request) {
        Quotation q = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));

        q.setStatus(QuotationStatus.REJECTED);
        q.setRejectedAt(java.time.LocalDateTime.now());
        q.setRejectionReason(request.rejectionReason());
        q = quotationRepository.save(q);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(q)));
    }

    @PostMapping("/{id}/version")
    public ResponseEntity<ApiResponse<QuotationDTO>> createVersion(@RequestHeader("X-Company-ID") UUID companyId,
                                                                    @PathVariable UUID id,
                                                                    @RequestBody CreateQuotationVersionRequest request) {
        Quotation original = quotationRepository.findById(id)
                .filter(qt -> qt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + id));

        Quotation newVersion = new Quotation();
        newVersion.setCompanyId(companyId);
        newVersion.setNumber(generateQuotationNumber(companyId));
        newVersion.setSeries("1");
        newVersion.setVersion(original.getVersion() + 1);
        newVersion.setParentQuotationId(original.getId());
        newVersion.setTitle(original.getTitle());
        newVersion.setDescription(original.getDescription());
        newVersion.setStatus(QuotationStatus.DRAFT);
        newVersion.setValidFrom(original.getValidFrom());
        newVersion.setValidUntil(original.getValidUntil());
        newVersion.setPaymentTerms(original.getPaymentTerms());
        newVersion.setDeliveryTerms(original.getDeliveryTerms());
        newVersion.setNotes(original.getNotes());
        newVersion.setInternalNotes(original.getInternalNotes());
        newVersion.setOpportunity(original.getOpportunity());
        newVersion.setCustomer(original.getCustomer());
        newVersion.setContact(original.getContact());
        newVersion.setBillingAddress(original.getBillingAddress());
        newVersion.setShippingAddress(original.getShippingAddress());
        newVersion.setCreatedBy(original.getCreatedBy());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        List<com.erp.brazil.sales.domain.model.QuotationItem> items = request.items().stream().map(itemReq -> {
            com.erp.brazil.sales.domain.model.QuotationItem item = new com.erp.brazil.sales.domain.model.QuotationItem();
            item.setQuotation(newVersion);
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

            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);

            return item;
        }).collect(Collectors.toList());

        newVersion.setItems(items);
        newVersion.setSubtotal(subtotal);
        newVersion.setDiscountAmount(discountAmount);
        newVersion.setTaxAmount(taxAmount);
        newVersion.setTotalAmount(subtotal.subtract(discountAmount).add(taxAmount));

        newVersion = quotationRepository.save(newVersion);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(newVersion)));
    }

    @PostMapping("/{id}/convert-to-order")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> convertToOrder(@RequestHeader("X-Company-ID") UUID companyId,
                                                                      @PathVariable UUID id,
                                                                      @RequestHeader("X-User-ID") UUID userId,
                                                                      @RequestBody CreateSalesOrderRequest request) {
        SalesOrderDTO order = convertQuotationUseCase.execute(id, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    private String generateQuotationNumber(UUID companyId) {
        long count = quotationRepository.countByCompanyId(companyId);
        return String.format("QUO-%06d", count + 1);
    }

    private QuotationDTO mapToDTO(Quotation q) {
        List<QuotationDTO.QuotationItemDTO> itemDTOs = q.getItems().stream().map(item ->
                new QuotationDTO.QuotationItemDTO(
                        item.getId(), item.getLineNumber(), item.getProductId(), item.getProductCode(),
                        item.getProductName(), item.getDescription(), item.getQuantity(), item.getUnitOfMeasure(),
                        item.getUnitPrice(), item.getDiscountPercent(), item.getDiscountAmount(),
                        item.getTaxPercent(), item.getTaxAmount(), item.getTotalAmount(), item.getNotes()
                )
        ).collect(Collectors.toList());

        List<QuotationDTO.QuotationVersionDTO> versionDTOs = q.getVersions().stream().map(v ->
                new QuotationDTO.QuotationVersionDTO(
                        v.getId(), v.getVersion(), v.getNumber(), v.getStatus(),
                        v.getTotalAmount(), v.getChangeReason(), v.getCreatedAt(),
                        v.getCreatedBy() != null ? v.getCreatedBy().getId() : null,
                        v.getCreatedBy() != null ? v.getCreatedBy().getFullName() : null
                )
        ).collect(Collectors.toList());

        return new QuotationDTO(
                q.getId(), q.getCompanyId(), q.getNumber(), q.getSeries(), q.getVersion(),
                q.getParentQuotationId(), q.getParentQuotationId() != null ? "Parent" : null,
                q.getTitle(), q.getDescription(), q.getStatus(),
                q.getValidFrom(), q.getValidUntil(),
                q.getSubtotal(), q.getDiscountPercent(), q.getDiscountAmount(), q.getTaxAmount(), q.getTotalAmount(),
                q.getCurrency(), q.getPaymentTerms(), q.getDeliveryTerms(), q.getNotes(), q.getInternalNotes(),
                q.getSentAt(), q.getAcceptedAt(), q.getRejectedAt(), q.getRejectionReason(),
                q.getOpportunity() != null ? q.getOpportunity().getId() : null,
                q.getOpportunity() != null ? q.getOpportunity().getOpportunityCode() : null,
                q.getCustomer() != null ? q.getCustomer().getId() : null,
                q.getCustomer() != null ? q.getCustomer().getTradeName() : null,
                q.getContact() != null ? q.getContact().getId() : null,
                q.getContact() != null ? q.getContact().getName() : null,
                q.getBillingAddress() != null ? q.getBillingAddress().getId() : null,
                q.getShippingAddress() != null ? q.getShippingAddress().getId() : null,
                q.getCreatedBy() != null ? q.getCreatedBy().getId() : null,
                q.getCreatedBy() != null ? q.getCreatedBy().getFullName() : null,
                q.getApprovedBy() != null ? q.getApprovedBy().getId() : null,
                q.getApprovedBy() != null ? q.getApprovedBy().getFullName() : null,
                q.getCreatedAt(), q.getUpdatedAt(),
                itemDTOs, versionDTOs,
                q.getSalesOrder() != null
        );
    }

    private PageResponse<QuotationDTO> mapToPageResponse(Page<Quotation> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}