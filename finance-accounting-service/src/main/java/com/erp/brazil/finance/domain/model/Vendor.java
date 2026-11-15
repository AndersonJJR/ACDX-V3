package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vendors", uniqueConstraints = {
    @UniqueConstraint(name = "uk_vendor_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_vendor_document", columnNames = "document_number")
}, indexes = {
    @Index(name = "idx_vendor_name", columnList = "name"),
    @Index(name = "idx_vendor_status", columnList = "status")
})
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 200)
    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    private DocumentType documentType;

    @NotBlank
    @Size(max = 20)
    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Size(max = 15)
    @Column(name = "state_registration", length = 15)
    private String stateRegistration;

    @Size(max = 15)
    @Column(name = "municipal_registration", length = 15)
    private String municipalRegistration;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 2)
    @Column(name = "state", length = 2)
    private String state;

    @Size(max = 10)
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Size(max = 2)
    @Column(name = "country", length = 2)
    private String country = "BR";

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 200)
    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id")
    private PaymentTerm paymentTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_payable_account_id")
    private ChartOfAccounts defaultPayableAccount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VendorStatus status = VendorStatus.ACTIVE;

    @Column(name = "credit_limit", precision = 19, scale = 2)
    private java.math.BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private java.math.BigDecimal currentBalance = java.math.BigDecimal.ZERO;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked = false;

    @Size(max = 500)
    @Column(name = "block_reason", length = 500)
    private String blockReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Vendor() {
        this.createdAt = LocalDate.now();
    }

    public Vendor(String code, String name, DocumentType documentType, String documentNumber) {
        this();
        this.code = code;
        this.name = name;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getStateRegistration() { return stateRegistration; }
    public void setStateRegistration(String stateRegistration) { this.stateRegistration = stateRegistration; }
    public String getMunicipalRegistration() { return municipalRegistration; }
    public void setMunicipalRegistration(String municipalRegistration) { this.municipalRegistration = municipalRegistration; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public PaymentTerm getPaymentTerm() { return paymentTerm; }
    public void setPaymentTerm(PaymentTerm paymentTerm) { this.paymentTerm = paymentTerm; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public ChartOfAccounts getDefaultPayableAccount() { return defaultPayableAccount; }
    public void setDefaultPayableAccount(ChartOfAccounts defaultPayableAccount) { this.defaultPayableAccount = defaultPayableAccount; }
    public VendorStatus getStatus() { return status; }
    public void setStatus(VendorStatus status) { this.status = status; }
    public java.math.BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(java.math.BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public java.math.BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(java.math.BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }
}