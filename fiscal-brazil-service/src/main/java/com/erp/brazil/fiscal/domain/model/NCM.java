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
@Table(name = "fiscal_ncm")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NCM {
    @Id
    @Column(name = "code", length = 8)
    private String code;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "chapter", length = 2)
    private String chapter;

    @Column(name = "position", length = 4)
    private String position;

    @Column(name = "subposition", length = 6)
    private String subposition;

    @Column(name = "item", length = 8)
    private String item;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "last_sync")
    private java.time.LocalDateTime lastSync;
}