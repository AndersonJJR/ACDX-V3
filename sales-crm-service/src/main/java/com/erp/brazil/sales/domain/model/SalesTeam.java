package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_teams")
public class SalesTeam extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    
    @Column(name = "team_code", length = 20, unique = true, nullable = false)
    private String teamCode;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private SalesPerson manager;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_team_id")
    private SalesTeam parentTeam;
    
    @OneToMany(mappedBy = "parentTeam", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SalesTeam> subTeams = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesTeam", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SalesPerson> members = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesTeam", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<CommissionRule> commissionRules = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesTeam", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SalesPipeline> pipelines = new ArrayList<>();

    public UUID getCompanyId() { return companyId; }
    public String getTeamCode() { return teamCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public SalesPerson getManager() { return manager; }
    public void setManager(SalesPerson manager) { this.manager = manager; }
    public SalesTeam getParentTeam() { return parentTeam; }
    public void setParentTeam(SalesTeam parentTeam) { this.parentTeam = parentTeam; }
    public List<SalesTeam> getSubTeams() { return subTeams; }
    public List<SalesPerson> getMembers() { return members; }
    public void setMembers(List<SalesPerson> members) { this.members = members; }
    public List<CommissionRule> getCommissionRules() { return commissionRules; }
    public List<SalesPipeline> getPipelines() { return pipelines; }
}