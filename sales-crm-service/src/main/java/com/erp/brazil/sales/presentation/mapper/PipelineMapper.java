package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.SalesPipeline;
import com.erp.brazil.sales.presentation.dto.PipelineDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PipelineMapper {

    PipelineMapper INSTANCE = Mappers.getMapper(PipelineMapper.class);

    @Mappings({
        @Mapping(target = "salesTeamName", ignore = true),
        @Mapping(target = "stages", ignore = true)
    })
    PipelineDTO toDTO(SalesPipeline pipeline);
}