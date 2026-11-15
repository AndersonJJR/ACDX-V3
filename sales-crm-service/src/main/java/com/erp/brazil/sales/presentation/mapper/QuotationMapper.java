package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.Quotation;
import com.erp.brazil.sales.presentation.dto.QuotationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    QuotationMapper INSTANCE = Mappers.getMapper(QuotationMapper.class);

    @Mappings({
        @Mapping(target = "parentQuotationNumber", ignore = true),
        @Mapping(target = "opportunityCode", ignore = true),
        @Mapping(target = "customerName", ignore = true),
        @Mapping(target = "contactName", ignore = true),
        @Mapping(target = "createdByName", ignore = true),
        @Mapping(target = "approvedByName", ignore = true),
        @Mapping(target = "items", ignore = true),
        @Mapping(target = "versions", ignore = true),
        @Mapping(target = "hasSalesOrder", expression = "java(quotation.getSalesOrder() != null)")
    })
    QuotationDTO toDTO(Quotation quotation);
}