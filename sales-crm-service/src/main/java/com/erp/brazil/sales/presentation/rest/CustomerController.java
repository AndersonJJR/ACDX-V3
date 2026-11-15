package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.Customer;
import com.erp.brazil.sales.domain.model.CustomerType;
import com.erp.brazil.sales.domain.repository.CustomerRepository;
import com.erp.brazil.sales.domain.repository.SalesOrderRepository;
import com.erp.brazil.sales.domain.repository.SalesInvoiceRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.CustomerDTO;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.SalesInvoiceDTO;
import com.erp.brazil.sales.presentation.dto.SalesOrderDTO;
import com.erp.brazil.sales.presentation.dto.request.CreateAddressRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateContactRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateCustomerRequest;
import com.erp.brazil.sales.presentation.dto.request.CreditStatusResponse;
import com.erp.brazil.sales.presentation.dto.request.UpdateCustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;

    public CustomerController(CustomerRepository customerRepository,
                              SalesOrderRepository salesOrderRepository,
                              SalesInvoiceRepository salesInvoiceRepository) {
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                            @RequestBody CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setCompanyId(companyId);
        customer.setCustomerCode(request.customerCode());
        customer.setTradeName(request.tradeName());
        customer.setLegalName(request.legalName());
        customer.setCustomerType(request.customerType());
        customer.setDocumentType(request.documentType());
        customer.setDocumentNumber(request.documentNumber());
        customer.setStateRegistration(request.stateRegistration());
        customer.setMunicipalRegistration(request.municipalRegistration());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setWebsite(request.website());
        customer.setCreditLimit(request.creditLimit() != null ? request.creditLimit() : BigDecimal.ZERO);
        customer.setPaymentTermsDays(request.paymentTermsDays() != null ? request.paymentTermsDays() : 30);
        customer.setPaymentTermId(request.paymentTermId());
        customer.setPriceListId(request.priceListId());
        customer.setIsActive(request.isActive() != null ? request.isActive() : true);
        customer.setIsBlocked(request.isBlocked() != null ? request.isBlocked() : false);
        customer.setBlockReason(request.blockReason());
        customer.setNotes(request.notes());
        if (request.territoryId() != null) {
            customer.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            customer.getTerritory().setId(request.territoryId());
        }
        if (request.salesPersonId() != null) {
            customer.setSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            customer.getSalesPerson().setId(request.salesPersonId());
        }

        if (request.addresses() != null) {
            List<com.erp.brazil.sales.domain.model.Address> addresses = request.addresses().stream().map(a -> {
                com.erp.brazil.sales.domain.model.Address addr = new com.erp.brazil.sales.domain.model.Address();
                addr.setCustomer(customer);
                addr.setAddressType(a.addressType());
                addr.setZipCode(a.zipCode());
                addr.setStreet(a.street());
                addr.setNumber(a.number());
                addr.setComplement(a.complement());
                addr.setNeighborhood(a.neighborhood());
                addr.setCity(a.city());
                addr.setState(a.state());
                addr.setCountry(a.country() != null ? a.country() : "BR");
                addr.setIbgeCode(a.ibgeCode());
                addr.setDefault(a.isDefault() != null ? a.isDefault() : false);
                addr.setNotes(a.notes());
                return addr;
            }).collect(Collectors.toList());
            customer.setAddresses(addresses);
        }

        if (request.contacts() != null) {
            List<com.erp.brazil.sales.domain.model.Contact> contacts = request.contacts().stream().map(c -> {
                com.erp.brazil.sales.domain.model.Contact contact = new com.erp.brazil.sales.domain.model.Contact();
                contact.setCustomer(customer);
                contact.setName(c.name());
                contact.setEmail(c.email());
                contact.setPhone(c.phone());
                contact.setMobile(c.mobile());
                contact.setPosition(c.position());
                contact.setDepartment(c.department());
                contact.setPrimary(c.isPrimary() != null ? c.isPrimary() : false);
                contact.setNotes(c.notes());
                return contact;
            }).collect(Collectors.toList());
            customer.setContacts(contacts);
        }

        customer = customerRepository.save(customer);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(customer)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                            @PathVariable UUID id,
                                                            @RequestBody UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        if (request.customerCode() != null) customer.setCustomerCode(request.customerCode());
        if (request.tradeName() != null) customer.setTradeName(request.tradeName());
        if (request.legalName() != null) customer.setLegalName(request.legalName());
        if (request.customerType() != null) customer.setCustomerType(request.customerType());
        if (request.documentType() != null) customer.setDocumentType(request.documentType());
        if (request.documentNumber() != null) customer.setDocumentNumber(request.documentNumber());
        if (request.stateRegistration() != null) customer.setStateRegistration(request.stateRegistration());
        if (request.municipalRegistration() != null) customer.setMunicipalRegistration(request.municipalRegistration());
        if (request.email() != null) customer.setEmail(request.email());
        if (request.phone() != null) customer.setPhone(request.phone());
        if (request.website() != null) customer.setWebsite(request.website());
        if (request.creditLimit() != null) customer.setCreditLimit(request.creditLimit());
        if (request.paymentTermsDays() != null) customer.setPaymentTermsDays(request.paymentTermsDays());
        if (request.paymentTermId() != null) customer.setPaymentTermId(request.paymentTermId());
        if (request.priceListId() != null) customer.setPriceListId(request.priceListId());
        if (request.isActive() != null) customer.setIsActive(request.isActive());
        if (request.isBlocked() != null) customer.setIsBlocked(request.isBlocked());
        if (request.blockReason() != null) customer.setBlockReason(request.blockReason());
        if (request.notes() != null) customer.setNotes(request.notes());
        if (request.territoryId() != null) {
            customer.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            customer.getTerritory().setId(request.territoryId());
        }
        if (request.salesPersonId() != null) {
            customer.setSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            customer.getSalesPerson().setId(request.salesPersonId());
        }

        customer = customerRepository.save(customer);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(customer)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        customerRepository.delete(customer);
        return ResponseEntity.ok(ApiResponse.ok(null, "Customer deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                        @RequestParam(required = false) CustomerType type,
                                                                        @RequestParam(required = false) String search,
                                                                        Pageable pageable) {
        Page<Customer> page;
        if (search != null && !search.isBlank()) {
            page = customerRepository.searchByCompanyId(companyId, "%" + search + "%", pageable);
        } else if (type != null) {
            page = customerRepository.findByCompanyIdAndCustomerType(companyId, type, pageable);
        } else {
            page = customerRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<PageResponse<SalesOrderDTO>>> getOrders(@RequestHeader("X-Company-ID") UUID companyId,
                                                                               @PathVariable UUID id,
                                                                               Pageable pageable) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        Page<com.erp.brazil.sales.domain.model.SalesOrder> page = salesOrderRepository.findByCustomerId(id, pageable);
        return ResponseEntity.ok(ApiResponse.ok(mapToOrderPageResponse(page)));
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<ApiResponse<PageResponse<SalesInvoiceDTO>>> getInvoices(@RequestHeader("X-Company-ID") UUID companyId,
                                                                                   @PathVariable UUID id,
                                                                                   Pageable pageable) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        Page<com.erp.brazil.sales.domain.model.SalesInvoice> page = salesInvoiceRepository.findByCustomerId(id, pageable);
        return ResponseEntity.ok(ApiResponse.ok(mapToInvoicePageResponse(page)));
    }

    @GetMapping("/{id}/credit-status")
    public ResponseEntity<ApiResponse<CustomerDTO.CreditStatusResponse>> getCreditStatus(@RequestHeader("X-Company-ID") UUID companyId,
                                                                                          @PathVariable UUID id) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        BigDecimal usedCredit = salesOrderRepository.findByCustomerId(id)
                .stream()
                .filter(o -> o.getStatus() != com.erp.brazil.sales.domain.model.SalesOrderStatus.CANCELLED)
                .map(com.erp.brazil.sales.domain.model.SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overdueAmount = salesInvoiceRepository.findByCustomerId(id)
                .stream()
                .filter(i -> i.getStatus() != com.erp.brazil.sales.domain.model.InvoiceStatus.CANCELLED 
                        && i.getStatus() != com.erp.brazil.sales.domain.model.InvoiceStatus.PAID)
                .filter(i -> i.isOverdue())
                .map(i -> i.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueCount = salesInvoiceRepository.findByCustomerId(id)
                .stream()
                .filter(i -> i.isOverdue())
                .count();

        BigDecimal availableCredit = customer.getCreditLimit().subtract(usedCredit);

        CustomerDTO.CreditStatusResponse response = new CustomerDTO.CreditStatusResponse(
                customer.getId(),
                customer.getTradeName(),
                customer.getCreditLimit(),
                usedCredit,
                availableCredit,
                overdueAmount,
                customer.getIsBlocked(),
                customer.getBlockReason(),
                (int) overdueCount
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private CustomerDTO mapToDTO(Customer c) {
        List<CustomerDTO.AddressDTO> addresses = c.getAddresses().stream().map(a ->
                new CustomerDTO.AddressDTO(
                        a.getId(), a.getAddressType(), a.getZipCode(), a.getStreet(), a.getNumber(),
                        a.getComplement(), a.getNeighborhood(), a.getCity(), a.getState(),
                        a.getCountry(), a.getIbgeCode(), a.isDefault(), a.getNotes()
                )
        ).collect(Collectors.toList());

        List<CustomerDTO.ContactDTO> contacts = c.getContacts().stream().map(ct ->
                new CustomerDTO.ContactDTO(
                        ct.getId(), ct.getName(), ct.getEmail(), ct.getPhone(), ct.getMobile(),
                        ct.getPosition(), ct.getDepartment(), ct.isPrimary(), ct.getNotes()
                )
        ).collect(Collectors.toList());

        return new CustomerDTO(
                c.getId(), c.getCompanyId(), c.getCustomerCode(), c.getTradeName(), c.getLegalName(),
                c.getCustomerType(), c.getDocumentType(), c.getDocumentNumber(),
                c.getStateRegistration(), c.getMunicipalRegistration(), c.getEmail(), c.getPhone(),
                c.getWebsite(), c.getCreditLimit(), c.getPaymentTermsDays(), c.getPaymentTermId(),
                c.getPriceListId(), c.getIsActive(), c.getIsBlocked(), c.getBlockReason(), c.getNotes(),
                c.getTerritory() != null ? c.getTerritory().getId() : null,
                c.getTerritory() != null ? c.getTerritory().getName() : null,
                c.getSalesPerson() != null ? c.getSalesPerson().getId() : null,
                c.getSalesPerson() != null ? c.getSalesPerson().getFullName() : null,
                c.getCreatedAt(), c.getUpdatedAt(), addresses, contacts
        );
    }

    private PageResponse<CustomerDTO> mapToPageResponse(Page<Customer> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }

    private PageResponse<SalesOrderDTO> mapToOrderPageResponse(Page<com.erp.brazil.sales.domain.model.SalesOrder> page) {
        return new PageResponse<>(
                page.getContent().stream().map(o -> new SalesOrderDTO(
                        o.getId(), o.getCompanyId(), o.getNumber(), o.getSeries(), o.getQuotationId(),
                        o.getQuotation() != null ? o.getQuotation().getNumber() : null,
                        o.getStatus(), o.getOrderDate(), o.getDeliveryDate(), o.getConfirmedAt(),
                        o.getConfirmedById(), null, o.getSubtotal(), o.getDiscountPercent(),
                        o.getDiscountAmount(), o.getTaxAmount(), o.getTotalAmount(), o.getShippingAmount(),
                        o.getCurrency(), o.getPaymentTerms(), o.getDeliveryTerms(), o.getNotes(),
                        o.getInternalNotes(), o.getCancelledAt(), o.getCancelledById(), null,
                        o.getCancellationReason(), o.getOpportunity() != null ? o.getOpportunity().getId() : null,
                        o.getOpportunity() != null ? o.getOpportunity().getOpportunityCode() : null,
                        o.getCustomer() != null ? o.getCustomer().getId() : null,
                        o.getCustomer() != null ? o.getCustomer().getTradeName() : null,
                        o.getContact() != null ? o.getContact().getId() : null,
                        o.getContact() != null ? o.getContact().getName() : null,
                        o.getBillingAddress() != null ? o.getBillingAddress().getId() : null,
                        o.getShippingAddress() != null ? o.getShippingAddress().getId() : null,
                        o.getSalesPerson() != null ? o.getSalesPerson().getId() : null,
                        o.getSalesPerson() != null ? o.getSalesPerson().getFullName() : null,
                        o.getCreatedAt(), o.getUpdatedAt(), java.util.List.of(), java.util.List.of(), java.util.List.of()
                )).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }

    private PageResponse<SalesInvoiceDTO> mapToInvoicePageResponse(Page<com.erp.brazil.sales.domain.model.SalesInvoice> page) {
        return new PageResponse<>(
                page.getContent().stream().map(i -> new SalesInvoiceDTO(
                        i.getId(), i.getCompanyId(), i.getNumber(), i.getSeries(), i.getAccessKey(),
                        i.getXmlPath(), i.getStatus(), i.getIssueDate(), i.getOperationDate(), i.getDueDate(),
                        i.getInvoiceType(), i.getNatureOfOperation(), i.getSubtotal(), i.getDiscountAmount(),
                        i.getTaxIcms(), i.getTaxIpi(), i.getTaxPis(), i.getTaxCofins(), i.getTaxIss(),
                        i.getTotalTax(), i.getFreightValue(), i.getInsuranceValue(), i.getOtherExpenses(),
                        i.getTotalAmount(), i.getCurrency(), i.getAuthorizedAt(), i.getCancelledAt(),
                        i.getCancelledById(), null, i.getCancellationReason(), i.getXmlContent(),
                        i.getSalesOrder() != null ? i.getSalesOrder().getId() : null,
                        i.getSalesOrder() != null ? i.getSalesOrder().getNumber() : null,
                        i.getDeliveryNoteId(), i.getDeliveryNote() != null ? i.getDeliveryNote().getNumber() : null,
                        i.getCustomer() != null ? i.getCustomer().getId() : null,
                        i.getCustomer() != null ? i.getCustomer().getTradeName() : null,
                        i.getBillingAddress() != null ? i.getBillingAddress().getId() : null,
                        i.getCreatedAt(), i.getUpdatedAt(), java.util.List.of(), java.util.List.of()
                )).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}