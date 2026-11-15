package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.DeliveryNote;
import com.erp.brazil.sales.presentation.dto.DeliveryNoteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DeliveryNoteMapper {

    DeliveryNoteMapper INSTANCE = Mappers.getMapper(DeliveryNoteMapper.class);

    @Mappings({
        @Mapping(target = "salesOrderNumber", ignore = true),
        @Mapping(target = "confirmedByName", ignore = true),
        @Mapping(target = "cancelledByName", ignore = true),
        @Mapping(target = "items", ignore = true)
    })
    DeliveryNoteDTO toDTO(DeliveryNote deliveryNote);
}