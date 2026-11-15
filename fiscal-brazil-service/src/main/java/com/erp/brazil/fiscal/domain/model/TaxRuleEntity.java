package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for the base tax rule table (fiscal_tax_rule).
 * Uses JOINED inheritance strategy.
 * Domain logic uses the TaxRule sealed interface and its implementations.
 */
@Entity
@Table(name = "fiscal_tax_rule")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class TaxRuleEntity {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "tax_type", length = 20, nullable = false)
    private String taxType; // ICMS, IPI, PIS, COFINS, ISS

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "uf_origin", length = 2)
    private String ufOrigin;

    @Column(name = "uf_destination", length = 2)
    private String ufDestination;

    @Column(name = "ncm_code", length = 8)
    private String ncmCode;

    @Column(name = "cest_code", length = 7)
    private String cestCode;

    @Column(name = "cfop_code", length = 4)
    private String cfopCode;

    @Column(name = "tax_regime", length = 20)
    private String taxRegime;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "priority")
    private int priority = 0;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public abstract BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context);
}