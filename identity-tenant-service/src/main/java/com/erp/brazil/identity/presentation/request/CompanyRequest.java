package com.erp.brazil.identity.presentation.request;

import com.erp.brazil.common.domain.validation.ValidCnpj;
import com.erp.brazil.identity.domain.model.Company;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 200, message = "Trade name must not exceed 200 characters")
    private String tradeName;

    @ValidCnpj
    @NotBlank(message = "CNPJ is required")
    private String cnpj;

    @Size(max = 20, message = "State registration must not exceed 20 characters")
    private String stateRegistration;

    @Size(max = 20, message = "Municipal registration must not exceed 20 characters")
    private String municipalRegistration;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 2, message = "State must be 2 characters")
    private String state;

    @Size(max = 10, message = "CEP must not exceed 10 characters")
    private String cep;

    @Size(max = 100, message = "Phone must not exceed 100 characters")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @Size(max = 200, message = "Website must not exceed 200 characters")
    private String website;

    private String settings; // JSON

    private String logoUrl;

    @Size(max = 7, message = "Primary color must be 7 characters (hex)")
    private String primaryColor;

    @Size(max = 7, message = "Secondary color must be 7 characters (hex)")
    private String secondaryColor;

    public Company toEntity() {
        Company company = new Company();
        company.setCode(this.code);
        company.setName(this.name);
        company.setTradeName(this.tradeName);
        company.setCnpj(this.cnpj);
        company.setStateRegistration(this.stateRegistration);
        company.setMunicipalRegistration(this.municipalRegistration);
        company.setAddress(this.address);
        company.setCity(this.city);
        company.setState(this.state);
        company.setCep(this.cep);
        company.setPhone(this.phone);
        company.setEmail(this.email);
        company.setWebsite(this.website);
        company.setSettings(this.settings);
        company.setLogoUrl(this.logoUrl);
        company.setPrimaryColor(this.primaryColor);
        company.setSecondaryColor(this.secondaryColor);
        return company;
    }
}