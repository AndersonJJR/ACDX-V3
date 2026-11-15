package com.erp.brazil.identity.presentation.response;

import com.erp.brazil.common.domain.model.BaseEntity;
import com.erp.brazil.identity.domain.model.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private String id;
    private String code;
    private String name;
    private String tradeName;
    private String cnpj;
    private String stateRegistration;
    private String municipalRegistration;
    private String address;
    private String city;
    private String state;
    private String cep;
    private String phone;
    private String email;
    private String website;
    private CompanyStatus status;
    private String settings;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static CompanyResponse from(Company company) {
        if (company == null) {
            return null;
        }
        return CompanyResponse.builder()
                .id(company.getId())
                .code(company.getCode())
                .name(company.getName())
                .tradeName(company.getTradeName())
                .cnpj(company.getCnpj())
                .stateRegistration(company.getStateRegistration())
                .municipalRegistration(company.getMunicipalRegistration())
                .address(company.getAddress())
                .city(company.getCity())
                .state(company.getState())
                .cep(company.getCep())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .status(company.getStatus())
                .settings(company.getSettings())
                .logoUrl(company.getLogoUrl())
                .primaryColor(company.getPrimaryColor())
                .secondaryColor(company.getSecondaryColor())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .createdBy(company.getCreatedBy())
                .updatedBy(company.getUpdatedBy())
                .build();
    }

    public enum CompanyStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        TRIAL,
        CANCELLED
    }
}