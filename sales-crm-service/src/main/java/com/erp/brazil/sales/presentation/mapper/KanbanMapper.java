package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.Opportunity;
import com.erp.brazil.sales.domain.model.Lead;
import com.erp.brazil.sales.presentation.dto.KanbanOpportunityDTO;
import com.erp.brazil.sales.presentation.dto.LeadKanbanCardDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KanbanMapper {

    KanbanMapper INSTANCE = Mappers.getMapper(KanbanMapper.class);

    @Mappings({
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "salesPersonName", ignore = true),
        @Mapping(target = "stageId", ignore = true),
        @Mapping(target = "stageName", ignore = true),
        @Mapping(target = "stageColor", ignore = true)
    })
    KanbanOpportunityDTO toOpportunityDTO(Opportunity opportunity);

    @Mappings({
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "salesPersonName", ignore = true)
    })
    LeadKanbanCardDTO toLeadDTO(Lead lead);
}