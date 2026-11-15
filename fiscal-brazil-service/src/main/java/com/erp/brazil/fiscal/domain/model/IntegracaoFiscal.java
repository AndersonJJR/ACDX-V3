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

import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_integracao_fiscal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegracaoFiscal {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "company_cnpj", length = 14, nullable = false)
    private String companyCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private IntegracaoTipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "provedor", length = 30)
    private IntegracaoProvedor provedor;

    @Column(name = "url_base", length = 500)
    private String urlBase;

    @Column(name = "url_homologacao", length = 500)
    private String urlHomologacao;

    @Column(name = "certificado_id", length = 50)
    private String certificadoId;

    @Column(name = "token", length = 500)
    private String token;

    @Column(name = "config_json", columnDefinition = "JSONB")
    private String configJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private IntegracaoStatus status = IntegracaoStatus.INATIVA;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}