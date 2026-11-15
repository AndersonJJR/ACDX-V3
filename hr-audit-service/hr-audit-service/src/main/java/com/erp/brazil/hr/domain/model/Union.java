package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "union_entity",
    uniqueConstraints = @UniqueConstraint(name = "uk_union_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Union extends BaseEntity {

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 18)
    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 2)
    @Column(name = "state", length = 2)
    private String state;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "contribution_rate", precision = 5, scale = 4)
    private java.math.BigDecimal contributionRate;

    @Column(name = "is_active")
    private Boolean isActive = true;
}