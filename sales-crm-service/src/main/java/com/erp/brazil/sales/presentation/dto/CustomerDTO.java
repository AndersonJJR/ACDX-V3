package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.CustomerType;
import com.erp.brazil.sales.domain.model.Address;
import com.erp.brazil.sales.domain.model.Contact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CustomerDTO(
        UUID id,
        UUID companyId,
        String customerCode,
        String tradeName,
        String legalName,
        CustomerType customerType,
        String documentType,
        String documentNumber,
        String stateRegistration,
        String municipalRegistration,
        String email,
        String phone,
        String website,
        BigDecimal creditLimit,
        Integer paymentTermsDays,
        UUID paymentTermId,
        UUID priceListId,
        Boolean isActive,
        Boolean isBlocked,
        String blockReason,
        String notes,
        UUID territoryId,
        String territoryName,
        UUID salesPersonId,
        String salesPersonName,
        LocalDate createdAt,
        LocalDate updatedAt,
        List<AddressDTO> addresses,
        List<ContactDTO> contacts
) {
    public record AddressDTO(
            UUID id,
            Address.AddressType addressType,
            String zipCode,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state,
            String country,
            String ibgeCode,
            Boolean isDefault,
            String notes
    ) {}

    public record ContactDTO(
            UUID id,
            String name,
            String email,
            String phone,
            String mobile,
            String position,
            String department,
            Boolean isPrimary,
            String notes
    ) {}

    public record CreditStatusResponse(
            UUID customerId,
            String customerName,
            BigDecimal creditLimit,
            BigDecimal usedCredit,
            BigDecimal availableCredit,
            BigDecimal overdueAmount,
            Boolean isBlocked,
            String blockReason,
            Integer overdueInvoicesCount
    ) {}
}