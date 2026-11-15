package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "company",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_company_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_company_cnpj", columnNames = "cnpj")
    },
    indexes = {
        @Index(name = "idx_company_cnpj", columnList = "cnpj"),
        @Index(name = "idx_company_active", columnList = "is_active")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "corporate_name", nullable = false, length = 255)
    private String corporateName;

    @Size(max = 255)
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @NotBlank
    @Size(max = 18)
    @Column(name = "cnpj", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Size(max = 20)
    @Column(name = "state_registration", length = 20)
    private String stateRegistration;

    @Size(max = 20)
    @Column(name = "municipal_registration", length = 20)
    private String municipalRegistration;

    @Size(max = 10)
    @Column(name = "address_cep", length = 10)
    private String addressCep;

    @Size(max = 255)
    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Size(max = 20)
    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Size(max = 100)
    @Column(name = "address_complement", length = 100)
    private String addressComplement;

    @Size(max = 100)
    @Column(name = "address_neighborhood", length = 100)
    private String addressNeighborhood;

    @Size(max = 100)
    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Size(max = 2)
    @Column(name = "address_state", length = 2)
    private String addressState;

    @Size(max = 50)
    @Column(name = "address_country", length = 50)
    private String addressCountry = "BRASIL";

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Size(max = 255)
    @Column(name = "website", length = 255)
    private String website;

    @Size(max = 7)
    @Column(name = "cnae_primary", length = 7)
    private String cnaePrimary;

    @Size(max = 500)
    @Column(name = "cnae_secondary", length = 500)
    private String cnaeSecondary;

    @Size(max = 100)
    @Column(name = "legal_nature", length = 100)
    private String legalNature;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "company_size", nullable = false, length = 20)
    private CompanySize companySize = CompanySize.SMALL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_regime", nullable = false, length = 20)
    private TaxRegime taxRegime = TaxRegime.SIMPLES_NACIONAL;

    @Column(name = "is_active")
    private boolean isActive = true;

    public String getDisplayName() {
        return tradeName != null ? tradeName : corporateName;
    }

    public String getFormattedCnpj() {
        if (cnpj == null) return "";
        String digits = cnpj.replaceAll("[^0-9]", "");
        if (digits.length() != 14) return cnpj;
        return digits.substring(0, 2) + "." + digits.substring(2, 5) + "." + digits.substring(5, 8) + "/" + digits.substring(8, 12) + "-" + digits.substring(12, 14);
    }

    public enum CompanySize {
        MEI, MICRO, SMALL, MEDIUM, LARGE
    }

    public enum TaxRegime {
        SIMPLES_NACIONAL, LUCRO_PRESUMIDO, LUCRO_REAL
    }
}