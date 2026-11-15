package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suppliers", indexes = {
    @Index(name = "idx_supplier_company_code", columnList = "company_id, supplier_code", unique = true),
    @Index(name = "idx_supplier_cnpj", columnList = "cnpj", unique = true)
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "supplier_code", nullable = false, length = 50)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "supplier_type", length = 20)
    @Enumerated(EnumType.STRING)
    private SupplierType supplierType = SupplierType.REGULAR;

    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Column(name = "cpf", length = 14)
    private String cpf;

    @Column(name = "ie", length = 20)
    private String ie;

    @Column(name = "im", length = 20)
    private String im;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 2)
    private String state;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "credit_limit", precision = 20, scale = 6)
    private BigDecimal creditLimit;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    @Column(name = "tax_regime", length = 20)
    @Enumerated(EnumType.STRING)
    private TaxRegime taxRegime;

    @Column(name = "crt", length = 1)
    private String crt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public SupplierType getSupplierType() { return supplierType; }
    public void setSupplierType(SupplierType supplierType) { this.supplierType = supplierType; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getIe() { return ie; }
    public void setIe(String ie) { this.ie = ie; }

    public String getIm() { return im; }
    public void setIm(String im) { this.im = im; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }

    public TaxRegime getTaxRegime() { return taxRegime; }
    public void setTaxRegime(TaxRegime taxRegime) { this.taxRegime = taxRegime; }

    public String getCrt() { return crt; }
    public void setCrt(String crt) { this.crt = crt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum SupplierType {
        REGULAR, PREFERRED, STRATEGIC, ONE_TIME, DROPSHIP
    }

    public enum TaxRegime {
        SIMPLES_NACIONAL, LUCRO_PRESUMIDO, LUCRO_REAL, MEI
    }
}