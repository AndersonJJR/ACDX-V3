package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment_terms", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_term_code", columnNames = "code")
})
public class PaymentTerm {

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
    @Column(name = "term_type", nullable = false, length = 20)
    private PaymentTermType termType;

    @Column(name = "days_to_due")
    private Integer daysToDue;

    @Column(name = "fixed_due_day")
    private Integer fixedDueDay;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private java.math.BigDecimal discountPercentage = java.math.BigDecimal.ZERO;

    @Column(name = "discount_days")
    private Integer discountDays;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_default", nullable = false)
    private boolean defaultTerm = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public PaymentTerm() {
        this.createdAt = LocalDate.now();
    }

    public PaymentTerm(String code, String name, PaymentTermType termType) {
        this();
        this.code = code;
        this.name = name;
        this.termType = termType;
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
    public PaymentTermType getTermType() { return termType; }
    public void setTermType(PaymentTermType termType) { this.termType = termType; }
    public Integer getDaysToDue() { return daysToDue; }
    public void setDaysToDue(Integer daysToDue) { this.daysToDue = daysToDue; }
    public Integer getFixedDueDay() { return fixedDueDay; }
    public void setFixedDueDay(Integer fixedDueDay) { this.fixedDueDay = fixedDueDay; }
    public java.math.BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(java.math.BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    public Integer getDiscountDays() { return discountDays; }
    public void setDiscountDays(Integer discountDays) { this.discountDays = discountDays; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDefaultTerm() { return defaultTerm; }
    public void setDefaultTerm(boolean defaultTerm) { this.defaultTerm = defaultTerm; }
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