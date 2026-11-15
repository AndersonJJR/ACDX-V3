package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.CustomerType;
import com.erp.brazil.sales.domain.model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 20) String customerCode,
        @Size(max = 200) String tradeName,
        @NotBlank @Size(max = 200) String legalName,
        @NotNull CustomerType customerType,
        @NotBlank @Size(max = 4) String documentType,
        @NotBlank @Size(max = 18) String documentNumber,
        @Size(max = 20) String stateRegistration,
        @Size(max = 20) String municipalRegistration,
        @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @Size(max = 200) String website,
        BigDecimal creditLimit,
        Integer paymentTermsDays,
        UUID paymentTermId,
        UUID priceListId,
        @Size(max = 2000) String notes,
        UUID territoryId,
        UUID salesPersonId,
        List<CreateAddressRequest> addresses,
        List<CreateContactRequest> contacts
) {}