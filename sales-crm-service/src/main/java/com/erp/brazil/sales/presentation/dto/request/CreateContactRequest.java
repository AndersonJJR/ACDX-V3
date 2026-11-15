package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContactRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @Size(max = 20) String mobile,
        @Size(max = 100) String position,
        @Size(max = 100) String department,
        Boolean isPrimary,
        @Size(max = 500) String notes
) {}