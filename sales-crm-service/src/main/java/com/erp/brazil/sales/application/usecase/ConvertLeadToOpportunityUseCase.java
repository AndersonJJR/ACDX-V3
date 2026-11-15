package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.*;
import com.erp.brazil.sales.domain.repository.LeadRepository;
import com.erp.brazil.sales.domain.repository.OpportunityRepository;
import com.erp.brazil.sales.domain.repository.PipelineStageRepository;
import com.erp.brazil.sales.domain.repository.SalesPipelineRepository;
import com.erp.brazil.sales.presentation.dto.request.ConvertLeadRequest;
import com.erp.brazil.sales.presentation.dto.OpportunityDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConvertLeadToOpportunityUseCase {

    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final SalesPipelineRepository salesPipelineRepository;

    public ConvertLeadToOpportunityUseCase(LeadRepository leadRepository,
                                           OpportunityRepository opportunityRepository,
                                           PipelineStageRepository pipelineStageRepository,
                                           SalesPipelineRepository salesPipelineRepository) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.salesPipelineRepository = salesPipelineRepository;
    }

    @Transactional
    public OpportunityDTO execute(UUID leadId, ConvertLeadRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        if (lead.isConverted()) {
            throw new IllegalStateException("Lead already converted: " + leadId);
        }

        if (!lead.isQualified()) {
            throw new IllegalStateException("Lead must be qualified before conversion: " + leadId);
        }

        PipelineStage targetStage = pipelineStageRepository.findById(request.stageId())
                .orElseThrow(() -> new IllegalArgumentException("Pipeline stage not found: " + request.stageId()));

        SalesPipeline pipeline = salesPipelineRepository.findById(request.pipelineId())
                .orElseThrow(() -> new IllegalArgumentException("Sales pipeline not found: " + request.pipelineId()));

        if (!targetStage.getPipeline().getId().equals(pipeline.getId())) {
            throw new IllegalArgumentException("Stage does not belong to the specified pipeline");
        }

        String opportunityCode = generateOpportunityCode(lead.getCompanyId());

        Opportunity opportunity = new Opportunity();
        opportunity.setCompanyId(lead.getCompanyId());
        opportunity.setOpportunityCode(opportunityCode);
        opportunity.setTitle(lead.getTitle());
        opportunity.setDescription(lead.getDescription());
        opportunity.setStatus(OpportunityStatus.OPEN);
        opportunity.setAmount(lead.getEstimatedValue() != null ? lead.getEstimatedValue() : BigDecimal.ZERO);
        opportunity.setProbability(targetStage.getProbability() != null ? targetStage.getProbability() : 10);
        opportunity.setExpectedCloseDate(lead.getExpectedCloseDate());
        opportunity.setNotes(request.notes());
        opportunity.setLead(lead);
        opportunity.setCustomer(lead.getCustomer());
        opportunity.setContact(lead.getContact());
        opportunity.setTerritory(lead.getTerritory());
        opportunity.setAssignedSalesPerson(lead.getAssignedSalesPerson());
        opportunity.setPipeline(pipeline);
        opportunity.setCurrentStage(targetStage);

        opportunity = opportunityRepository.save(opportunity);

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedAt(LocalDateTime.now());
        leadRepository.save(lead);

        return mapToDTO(opportunity);
    }

    private String generateOpportunityCode(UUID companyId) {
        long count = opportunityRepository.countByCompanyId(companyId);
        return String.format("OPP-%06d", count + 1);
    }

    private OpportunityDTO mapToDTO(Opportunity o) {
        return new OpportunityDTO(
                o.getId(), o.getCompanyId(), o.getOpportunityCode(), o.getTitle(), o.getDescription(),
                o.getStatus(), o.getAmount(), o.getProbability(), o.getWeightedAmount(),
                o.getExpectedCloseDate(), o.getActualCloseDate(), o.getLostReason(), o.getLostToCompetitor(),
                o.getNotes(),
                o.getLead() != null ? o.getLead().getId() : null,
                o.getLead() != null ? o.getLead().getLeadCode() : null,
                o.getCustomer() != null ? o.getCustomer().getId() : null,
                o.getCustomer() != null ? o.getCustomer().getTradeName() : null,
                o.getContact() != null ? o.getContact().getId() : null,
                o.getContact() != null ? o.getContact().getName() : null,
                o.getTerritory() != null ? o.getTerritory().getId() : null,
                o.getTerritory() != null ? o.getTerritory().getName() : null,
                o.getAssignedSalesPerson() != null ? o.getAssignedSalesPerson().getId() : null,
                o.getAssignedSalesPerson() != null ? o.getAssignedSalesPerson().getFullName() : null,
                o.getPipeline() != null ? o.getPipeline().getId() : null,
                o.getPipeline() != null ? o.getPipeline().getName() : null,
                o.getCurrentStage() != null ? o.getCurrentStage().getId() : null,
                o.getCurrentStage() != null ? o.getCurrentStage().getName() : null,
                o.getCreatedAt(), o.getUpdatedAt(),
                java.util.List.of(), java.util.List.of()
        );
    }
}