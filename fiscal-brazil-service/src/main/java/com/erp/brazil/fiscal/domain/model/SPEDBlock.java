package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_sped_block")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SPEDBlock {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sped_type", length = 20, nullable = false)
    private SPEDType spedType;

    @Column(name = "block", length = 10, nullable = false)
    private String block;

    @Column(name = "register", length = 10, nullable = false)
    private String register;

    @Column(name = "company_cnpj", length = 14, nullable = false)
    private String companyCnpj;

    @Column(name = "reference_month", length = 7, nullable = false) // YYYYMM
    private String referenceMonth;

    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @Column(name = "data", columnDefinition = "JSONB")
    private String data; // JSON serialized block data

    @Column(name = "line_number")
    private Long lineNumber;

    @Column(name = "hash", length = 64)
    private String hash;

    @Column(name = "is_valid")
    private Boolean valid = true;

    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "transmitted_at")
    private LocalDateTime transmittedAt;

    @Column(name = "protocol", length = 50)
    private String protocol;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SPEDStatus status = SPEDStatus.GERADO;
}