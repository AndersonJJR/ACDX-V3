package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fiscal_cfop")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CFOP {
    @Id
    @Column(name = "code", length = 4)
    private String code;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "type", length = 20)
    private String type; // ENTRADA, SAIDA, DEVOLUCAO, etc.

    @Column(name = "category", length = 50)
    private String category; // VENDA, COMPRA, TRANSFERENCIA, etc.

    @Column(name = "generates_icms")
    private boolean generatesIcms = true;

    @Column(name = "generates_ipi")
    private boolean generatesIpi = false;

    @Column(name = "generates_pis_cofins")
    private boolean generatesPisCofins = true;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "last_sync")
    private java.time.LocalDateTime lastSync;
}