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
@Table(name = "fiscal_nfe_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NFEEvent {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "chave_nfe", length = 44, nullable = false)
    private String chaveNfe;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 30)
    private NFEEventType eventType;

    @Column(name = "sequencia", length = 2)
    private String sequencia = "1";

    @Column(name = "cnpj_autor", length = 14, nullable = false)
    private String cnpjAutor;

    @Column(name = "nome_autor", length = 60)
    private String nomeAutor;

    @Column(name = "justificativa", length = 255)
    private String justificativa;

    @Column(name = "correcao", columnDefinition = "TEXT")
    private String correcao;

    @Column(name = "condicao_uso", length = 1)
    private String condicaoUso;

    @Column(name = "xml_evento", columnDefinition = "TEXT")
    private String xmlEvento;

    @Column(name = "xml_retorno", columnDefinition = "TEXT")
    private String xmlRetorno;

    @Column(name = "protocolo", length = 50)
    private String protocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private NFEEventStatus status = NFEEventStatus.PENDENTE;

    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}