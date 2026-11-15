package com.erp.brazil.fiscal.presentation.rest.dto;

import com.erp.brazil.fiscal.domain.model.SPEDType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SPEDRequestDTO {
    @NotBlank
    private String companyCnpj;

    @NotNull
    private SPEDType spedType;

    @NotBlank
    private String referenceMonth; // YYYY-MM

    @NotNull
    private Integer referenceYear;
}