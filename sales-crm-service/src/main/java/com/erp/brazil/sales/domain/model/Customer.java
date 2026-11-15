package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "customer_code", length = 20, unique = true, nullable = false)
    private String customerCode;
    
    @Column(name = "trade_name", length = 200)
    private String tradeName;
    
    @Column(name = "legal_name", length = 200, nullable = false)
    private String legalName;
    
    @Column(name = "customer_type")
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;
    
    @Column(name = "document_type", length = 4)
    private String documentType; // CPF or CNPJ
    
    @Column(name = "document_number", length = 18, unique = true)
    private String documentNumber;
    
    @Column(name = "state_registration", length = 20)
    private String stateRegistration;
    
    @Column(name = "municipal_registration", length = 20)
    private String municipalRegistration;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "website", length = 200)
    private String website;
    
    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;
    
    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays = 30;
    
    @Column(name = "payment_term_id")
    private UUID paymentTermId;
    
    @Column(name = "price_list_id")
    private UUID priceListId;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;
    
    @Column(name = "block_reason", columnDefinition = "TEXT")
    private String blockReason;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Address> addresses = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Contact> contacts = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Lead> leads = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Opportunity> opportunities = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Quotation> quotations = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SalesOrder> salesOrders = new ArrayList<>();
    
    // LGPD Consent tracking
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<LGPDConsent> lgpdConsents = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    private Territory territory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id")
    private SalesPerson salesPerson;
    
    public Address getDefaultBillingAddress() {
        return addresses.stream()
            .filter(a -> a.getAddressType() == Address.AddressType.BILLING && a.isDefault())
            .findFirst()
            .orElse(addresses.stream()
                .filter(a -> a.getAddressType() == Address.AddressType.BILLING)
                .findFirst()
                .orElse(null));
    }
    
    public Address getDefaultShippingAddress() {
        return addresses.stream()
            .filter(a -> a.getAddressType() == Address.AddressType.SHIPPING && a.isDefault())
            .findFirst()
            .orElse(addresses.stream()
                .filter(a -> a.getAddressType() == Address.AddressType.SHIPPING)
                .findFirst()
                .orElse(null));
    }
    
    public Contact getPrimaryContact() {
        return contacts.stream()
            .filter(Contact::isPrimary)
            .findFirst()
            .orElse(contacts.isEmpty() ? null : contacts.get(0));
    }
}