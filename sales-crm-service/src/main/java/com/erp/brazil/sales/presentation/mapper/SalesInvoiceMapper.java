package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.SalesInvoice;
import com.erp.brazil.sales.presentation.dto.SalesInvoiceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SalesInvoiceMapper {

    SalesInvoiceMapper INSTANCE = Mappers.getMapper(SalesInvoiceMapper.class);

    @Mappings({
        @Mapping(target = "salesOrderNumber", ignore = true),
        @Mapping(target = "deliveryNoteNumber", ignore = true),
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "cancelledByName", ignore = true),
        @Mapping(target = "items", ignore = true),
        @Mapping(target = "payments", ignore = true)
    })
    SalesInvoiceDTO toDTO(SalesInvoice invoice);
}