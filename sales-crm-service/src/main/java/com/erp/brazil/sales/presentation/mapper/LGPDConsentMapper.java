package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.LGPDConsent;
import com.erp.brazil.sales.presentation.dto.LGPDConsentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LGPDConsentMapper {

    LGPDConsentMapper INSTANCE = Mappers.getMapper(LGPDConsentMapper.class);

    @Mappings({
        @Mapping(target = "entityName", expression = "java(consent.getEntityType() + \"-\" + consent.getEntityId().toString().substring(0, 8))")
    })
    LGPDConsentDTO toDTO(LGPDConsent consent);
}