package com.erp.brazil.fiscal.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDigitalRequestDTO {
    @NotBlank
    private String companyCnpj;

    @NotBlank
    private String companyName;

    @NotBlank
    private String alias;

    @NotBlank
    private String pkcs12Base64;

    @NotBlank
    private String password;

    @NotNull
    private LocalDate notBefore;

    @NotNull
    private LocalDate notAfter;

    private String environment = "1";
}