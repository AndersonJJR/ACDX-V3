package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pipeline_stages")
public class PipelineStage extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "stage_order", nullable = false)
    private Integer order;

    @Column(name = "probability", nullable = false)
    private Integer probability = 0;

    @Column(name = "is_won")
    private Boolean isWon = false;

    @Column(name = "is_lost")
    private Boolean isLost = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "stage_type")
    @Enumerated(EnumType.STRING)
    private PipelineStageType stageType = PipelineStageType.OPPORTUNITY;

    @Column(name = "days_stagnant_limit")
    private Integer daysStagnantLimit = 30;

    @Column(name = "auto_advance_days")
    private Integer autoAdvanceDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private SalesPipeline pipeline;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lead> leads = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opportunity> opportunities = new ArrayList<>();

    public PipelineStage() {}

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    public Boolean getIsWon() {
        return isWon;
    }

    public void setIsWon(Boolean isWon) {
        this.isWon = isWon;
    }

    public Boolean getIsLost() {
        return isLost;
    }

    public void setIsLost(Boolean isLost) {
        this.isLost = isLost;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public PipelineStageType getStageType() {
        return stageType;
    }

    public void setStageType(PipelineStageType stageType) {
        this.stageType = stageType;
    }

    public Integer getDaysStagnantLimit() {
        return daysStagnantLimit;
    }

    public void setDaysStagnantLimit(Integer daysStagnantLimit) {
        this.daysStagnantLimit = daysStagnantLimit;
    }

    public Integer getAutoAdvanceDays() {
        return autoAdvanceDays;
    }

    public void setAutoAdvanceDays(Integer autoAdvanceDays) {
        this.autoAdvanceDays = autoAdvanceDays;
    }

    public SalesPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(SalesPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public List<Lead> getLeads() {
        return leads;
    }

    public void setLeads(List<Lead> leads) {
        this.leads = leads;
    }

    public List<Opportunity> getOpportunities() {
        return opportunities;
    }

    public void setOpportunities(List<Opportunity> opportunities) {
        this.opportunities = opportunities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PipelineStage that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}