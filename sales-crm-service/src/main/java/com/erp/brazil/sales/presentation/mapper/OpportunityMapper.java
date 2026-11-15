package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.Opportunity;
import com.erp.brazil.sales.presentation.dto.OpportunityDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OpportunityMapper {

    OpportunityMapper INSTANCE = Mappers.getMapper(OpportunityMapper.class);

    @Mappings({
        @Mapping(target = "leadCode", ignore = true),
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "contactName", ignore = true),
        @Mapping(target = "territoryName", ignore = true),
        @Mapping(target = "assignedSalesPersonName", ignore = true),
        @Mapping(target = "pipelineName", ignore = true),
        @Mapping(target = "currentStageName", ignore = true),
        @Mapping(target = "activities", ignore = true),
        @Mapping(target = "products", ignore = true)
    })
    OpportunityDTO toDTO(Opportunity opportunity);
}