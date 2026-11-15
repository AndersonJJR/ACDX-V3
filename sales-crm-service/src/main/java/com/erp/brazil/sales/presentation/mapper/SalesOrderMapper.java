package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.SalesOrder;
import com.erp.brazil.sales.presentation.dto.SalesOrderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SalesOrderMapper {

    SalesOrderMapper INSTANCE = Mappers.getMapper(SalesOrderMapper.class);

    @Mappings({
        @Mapping(target = "quotationNumber", ignore = true),
        @Mapping(target = "opportunityCode", ignore = true),
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "contactName", ignore = true),
        @Mapping(target = "salesPersonName", ignore = true),
        @Mapping(target = "confirmedByName", ignore = true),
        @Mapping(target = "cancelledByName", ignore = true),
        @Mapping(target = "items", ignore = true),
        @Mapping(target = "deliveryNotes", ignore = true),
        @Mapping(target = "invoices", ignore = true)
    })
    SalesOrderDTO toDTO(SalesOrder order);
}