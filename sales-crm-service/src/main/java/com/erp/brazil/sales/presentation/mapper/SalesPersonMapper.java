package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.SalesPerson;
import com.erp.brazil.sales.presentation.dto.SalesPersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SalesPersonMapper {

    SalesPersonMapper INSTANCE = Mappers.getMapper(SalesPersonMapper.class);

    @Mappings({
        @Mapping(target = "territoryName", ignore = true),
        @Mapping(target = "salesTeamName", ignore = true)
    })
    SalesPersonDTO toDTO(SalesPerson salesPerson);
}