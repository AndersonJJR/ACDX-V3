package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAddressRequest(
        @NotNull Address.AddressType addressType,
        @NotBlank @Size(max = 10) String zipCode,
        @NotBlank @Size(max = 200) String street,
        @Size(max = 20) String number,
        @Size(max = 100) String complement,
        @NotBlank @Size(max = 100) String neighborhood,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 2) String state,
        @Size(max = 2) String country,
        @Size(max = 7) String ibgeCode,
        Boolean isDefault,
        @Size(max = 500) String notes
) {}