package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment_methods", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_method_code", columnNames = "code")
})
public class PaymentMethod {

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

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 20)
    private PaymentMethodType methodType;

    @Column(name = "requires_bank_account", nullable = false)
    private boolean requiresBankAccount = true;

    @Column(name = "requires_authorization", nullable = false)
    private boolean requiresAuthorization = false;

    @Column(name = "processing_days")
    private Integer processingDays = 0;

    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private java.math.BigDecimal feePercentage = java.math.BigDecimal.ZERO;

    @Column(name = "fixed_fee", precision = 19, scale = 2)
    private java.math.BigDecimal fixedFee = java.math.BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_default", nullable = false)
    private boolean defaultMethod = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public PaymentMethod() {
        this.createdAt = LocalDate.now();
    }

    public PaymentMethod(String code, String name, PaymentMethodType methodType) {
        this();
        this.code = code;
        this.name = name;
        this.methodType = methodType;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PaymentMethodType getMethodType() { return methodType; }
    public void setMethodType(PaymentMethodType methodType) { this.methodType = methodType; }
    public boolean isRequiresBankAccount() { return requiresBankAccount; }
    public void setRequiresBankAccount(boolean requiresBankAccount) { this.requiresBankAccount = requiresBankAccount; }
    public boolean isRequiresAuthorization() { return requiresAuthorization; }
    public void setRequiresAuthorization(boolean requiresAuthorization) { this.requiresAuthorization = requiresAuthorization; }
    public Integer getProcessingDays() { return processingDays; }
    public void setProcessingDays(Integer processingDays) { this.processingDays = processingDays; }
    public java.math.BigDecimal getFeePercentage() { return feePercentage; }
    public void setFeePercentage(java.math.BigDecimal feePercentage) { this.feePercentage = feePercentage; }
    public java.math.BigDecimal getFixedFee() { return fixedFee; }
    public void setFixedFee(java.math.BigDecimal fixedFee) { this.fixedFee = fixedFee; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDefaultMethod() { return defaultMethod; }
    public void setDefaultMethod(boolean defaultMethod) { this.defaultMethod = defaultMethod; }
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