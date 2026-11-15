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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public enum SupplierType {
        REGULAR, PREFERRED, STRATEGIC, ONE_TIME, DROPSHIP
    }

    public enum TaxRegime {
        SIMPLES_NACIONAL, LUCRO_PRESUMIDO, LUCRO_REAL, MEI
    }
}