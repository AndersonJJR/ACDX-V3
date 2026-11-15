package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.Lead;
import com.erp.brazil.sales.presentation.dto.LeadDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LeadMapper {

    LeadMapper INSTANCE = Mappers.getMapper(LeadMapper.class);

    @Mappings({
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "contactName", ignore = true),
        @Mapping(target = "territoryName", ignore = true),
        @Mapping(target = "assignedSalesPersonName", ignore = true),
        @Mapping(target = "pipelineName", ignore = true),
        @Mapping(target = "currentStageName", ignore = true),
        @Mapping(target = "activities", ignore = true)
    })
    LeadDTO toDTO(Lead lead);
}