package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.*;
import com.erp.brazil.sales.domain.repository.*;
import com.erp.brazil.sales.presentation.dto.request.CreateQuotationRequest;
import com.erp.brazil.sales.presentation.dto.QuotationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreateQuotationUseCase {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final ContactRepository contactRepository;
    private final AddressRepository addressRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final QuotationItemRepository quotationItemRepository;

    public CreateQuotationUseCase(QuotationRepository quotationRepository,
                                  CustomerRepository customerRepository,
                                  ContactRepository contactRepository,
                                  AddressRepository addressRepository,
                                  OpportunityRepository opportunityRepository,
                                  SalesPersonRepository salesPersonRepository,
                                  QuotationItemRepository quotationItemRepository) {
        this.quotationRepository = quotationRepository;
        this.customerRepository = customerRepository;
        this.contactRepository = contactRepository;
        this.addressRepository = addressRepository;
        this.opportunityRepository = opportunityRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.quotationItemRepository = quotationItemRepository;
    }

    @Transactional
    public QuotationDTO execute(UUID companyId, UUID createdById, CreateQuotationRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        if (!customer.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Customer does not belong to company");
        }

        if (!customer.getIsActive() || customer.getIsBlocked()) {
            throw new IllegalStateException("Customer is not active or is blocked");
        }

        Contact contact = null;
        if (request.contactId() != null) {
            contact = contactRepository.findById(request.contactId())
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + request.contactId()));
            if (!contact.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Contact does not belong to customer");
            }
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
            if (!opportunity.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Opportunity does not belong to customer");
            }
        }

        SalesPerson createdBy = salesPersonRepository.findById(createdById)
                .orElseThrow(() -> new IllegalArgumentException("Sales person not found: " + createdById));

        String number = generateQuotationNumber(companyId);

        Quotation quotation = new Quotation();
        quotation.setCompanyId(companyId);
        quotation.setNumber(number);
        quotation.setSeries("1");
        quotation.setVersion(1);
        quotation.setTitle(request.title());
        quotation.setDescription(request.description());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setValidFrom(request.validFrom());
        quotation.setValidUntil(request.validUntil());
        quotation.setPaymentTerms(request.paymentTerms());
        quotation.setDeliveryTerms(request.deliveryTerms());
        quotation.setNotes(request.notes());
        quotation.setInternalNotes(request.internalNotes());
        quotation.setOpportunity(opportunity);
        quotation.setCustomer(customer);
        quotation.setContact(contact);
        quotation.setBillingAddress(billingAddress);
        quotation.setShippingAddress(shippingAddress);
        quotation.setCreatedBy(createdBy);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        List<QuotationItem> items = request.items().stream().map(itemReq -> {
            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
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

        quotation.setItems(items);
        quotation.setSubtotal(subtotal);
        quotation.setDiscountAmount(discountAmount);
        quotation.setTaxAmount(taxAmount);
        quotation.setTotalAmount(subtotal.subtract(discountAmount).add(taxAmount));

        quotation = quotationRepository.save(quotation);

        return mapToDTO(quotation);
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
                q.getParentQuotationId(), null,
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
}