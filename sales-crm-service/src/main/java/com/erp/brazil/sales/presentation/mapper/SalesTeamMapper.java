package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.SalesTeam;
import com.erp.brazil.sales.presentation.dto.SalesTeamDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SalesTeamMapper {

    SalesTeamMapper INSTANCE = Mappers.getMapper(SalesTeamMapper.class);

    @Mappings({
        @Mapping(target = "managerName", ignore = true),
        @Mapping(target = "members", ignore = true)
    })
    SalesTeamDTO toDTO(SalesTeam salesTeam);
}