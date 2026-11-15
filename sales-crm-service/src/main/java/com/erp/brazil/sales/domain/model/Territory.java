package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "territories")
public class Territory extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    
    @Column(name = "code", length = 20, unique = true, nullable = false)
    private String code;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "state", length = 2)
    private String state;
    
    @Column(name = "cities", columnDefinition = "TEXT")
    private String cities; // JSON array of city names/IBGE codes
    
    @Column(name = "zip_code_ranges", columnDefinition = "TEXT")
    private String zipCodeRanges; // JSON array of zip code ranges
    
    @Column(name = "active")
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_territory_id")
    private Territory parentTerritory;
    
    @OneToMany(mappedBy = "parentTerritory", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Territory> subTerritories = new ArrayList<>();
    
    @OneToMany(mappedBy = "territory", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Customer> customers = new ArrayList<>();
    
    @OneToMany(mappedBy = "territory", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Lead> leads = new ArrayList<>();
    
    @OneToMany(mappedBy = "territory", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Opportunity> opportunities = new ArrayList<>();
    
    @OneToMany(mappedBy = "territory", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SalesPerson> salesPersons = new ArrayList<>();
    
    @OneToMany(mappedBy = "territory", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<CommissionRule> commissionRules = new ArrayList<>();
}