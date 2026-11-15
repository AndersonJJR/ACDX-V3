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
@Table(name = "fiscal_cest")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CEST {
    @Id
    @Column(name = "code", length = 7)
    private String code;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "segment", length = 100)
    private String segment;

    @Column(name = "ncm_code", length = 8)
    private String ncmCode;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "last_sync")
    private java.time.LocalDateTime lastSync;
}