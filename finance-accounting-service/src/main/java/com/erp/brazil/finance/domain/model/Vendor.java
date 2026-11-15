package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendors", uniqueConstraints = {
    @UniqueConstraint(name = "uk_vendor_document", columnNames = {"company_id", "document_type", "document_number"}),
    @UniqueConstraint(name = "uk_vendor_code", columnNames = {"company_id", "code"})
}, indexes = {
    @Index(name = "idx_vendor_company", columnList = "company_id"),
    @Index(name = "idx_vendor_name", columnList = "name"),
    @Index(name = "idx_vendor_document_number", columnList = "document_number")
})
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    private DocumentType documentType;

    @NotBlank
    @Size(max = 20)
    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 200)
    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Size(max = 200)
    @Column(name = "email", length = 200)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100)
    @Column(name = "pix_key", length = 100)
    private String pixKey;

    @Embedded
    private BankAccountInfo bankAccount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Vendor() {
        this.createdAt = LocalDateTime.now();
    }

    public Vendor(UUID companyId, String code, DocumentType documentType, String documentNumber, String name) {
        this();
        this.companyId = companyId;
        this.code = code;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.name = name;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }
    public BankAccountInfo getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccountInfo bankAccount) { this.bankAccount = bankAccount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Embeddable
    public static class BankAccountInfo {
        @Size(max = 10)
        @Column(name = "bank_code", length = 10)
        private String bankCode;

        @Size(max = 20)
        @Column(name = "agency", length = 20)
        private String agency;

        @Size(max = 10)
        @Column(name = "agency_digit", length = 10)
        private String agencyDigit;

        @Size(max = 30)
        @Column(name = "account_number", length = 30)
        private String accountNumber;

        @Size(max = 10)
        @Column(name = "account_digit", length = 10)
        private String accountDigit;

        @Enumerated(EnumType.STRING)
        @Column(name = "account_type", length = 20)
        private BankAccountType accountType;

        // Getters and Setters
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }
        public String getAgency() { return agency; }
        public void setAgency(String agency) { this.agency = agency; }
        public String getAgencyDigit() { return agencyDigit; }
        public void setAgencyDigit(String agencyDigit) { this.agencyDigit = agencyDigit; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountDigit() { return accountDigit; }
        public void setAccountDigit(String accountDigit) { this.accountDigit = accountDigit; }
        public BankAccountType getAccountType() { return accountType; }
        public void setAccountType(BankAccountType accountType) { this.accountType = accountType; }
    }
}