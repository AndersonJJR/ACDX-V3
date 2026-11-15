package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.CustomerType;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCustomerRequest(
        @Size(max = 20) String customerCode,
        @Size(max = 200) String tradeName,
        @Size(max = 200) String legalName,
        CustomerType customerType,
        @Size(max = 4) String documentType,
        @Size(max = 18) String documentNumber,
        @Size(max = 20) String stateRegistration,
        @Size(max = 20) String municipalRegistration,
        @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @Size(max = 200) String website,
        BigDecimal creditLimit,
        Integer paymentTermsDays,
        UUID paymentTermId,
        UUID priceListId,
        Boolean isActive,
        Boolean isBlocked,
        @Size(max = 500) String blockReason,
        @Size(max = 2000) String notes,
        UUID territoryId,
        UUID salesPersonId
) {}