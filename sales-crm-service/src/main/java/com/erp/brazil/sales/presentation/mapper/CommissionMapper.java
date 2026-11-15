package com.erp.brazil.sales.presentation.mapper;

import com.erp.brazil.sales.domain.model.CommissionRule;
import com.erp.brazil.sales.domain.model.CommissionPayment;
import com.erp.brazil.sales.presentation.dto.CommissionRuleDTO;
import com.erp.brazil.sales.presentation.dto.CommissionPaymentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommissionMapper {

    CommissionMapper INSTANCE = Mappers.getMapper(CommissionMapper.class);

    @Mappings({
        @Mapping(target = "productCategoryName", ignore = true),
        @Mapping(target = "salesTeamName", ignore = true),
        @Mapping(target = "salesPersonName", ignore = true),
        @Mapping(target = "territoryName", ignore = true),
        @Mapping(target = "tiers", ignore = true)
    })
    CommissionRuleDTO toDTO(CommissionRule rule);

    @Mappings({
        @Mapping(target = "commissionRuleCode", ignore = true),
        @Mapping(target = "salesPersonName", ignore = true),
        @Mapping(target = "salesOrderNumber", ignore = true),
        @Mapping(target = "salesInvoiceNumber", ignore = true),
        @Mapping(target = "calculatedByName", ignore = true),
        @Mapping(target = "paidByName", ignore = true)
    })
    CommissionPaymentDTO toDTO(CommissionPayment payment);
}