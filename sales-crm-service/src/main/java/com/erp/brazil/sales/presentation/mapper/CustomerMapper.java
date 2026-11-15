package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.Customer;
import com.erp.brazil.sales.presentation.dto.CustomerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    @Mappings({
        @Mapping(target = "territoryName", ignore = true),
        @Mapping(target = "salesPersonName", ignore = true),
        @Mapping(target = "addresses", ignore = true),
        @Mapping(target = "contacts", ignore = true)
    })
    CustomerDTO toDTO(Customer customer);
}