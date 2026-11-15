package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.Territory;
import com.erp.brazil.sales.presentation.dto.TerritoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TerritoryMapper {

    TerritoryMapper INSTANCE = Mappers.getMapper(TerritoryMapper.class);

    @Mappings({
        @Mapping(target = "parentTerritoryName", ignore = true),
        @Mapping(target = "cities", expression = "java(territory.getCities() != null ? List.of(territory.getCities().split(\",\")) : List.of())"),
        @Mapping(target = "zipCodeRanges", expression = "java(territory.getZipCodeRanges() != null ? List.of(territory.getZipCodeRanges().split(\",\")) : List.of())"),
        @Mapping(target = "subTerritories", ignore = true),
        @Mapping(target = "customerCount", expression = "java((long) territory.getCustomers().size())"),
        @Mapping(target = "leadCount", expression = "java((long) territory.getLeads().size())"),
        @Mapping(target = "opportunityCount", expression = "java((long) territory.getOpportunities().size())"),
        @Mapping(target = "salesPersonCount", expression = "java((long) territory.getSalesPersons().size())")
    })
    TerritoryDTO toDTO(Territory territory);
}