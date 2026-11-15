package com.erp.brazil.identity.application.service;

import com.erp.brazil.common.domain.event.OutboxEventPublisher;
import com.erp.brazil.common.domain.model.TenantContext;
import com.erp.brazil.identity.domain.event.CompanyCreatedEvent;
import com.erp.brazil.identity.domain.model.Company;
import com.erp.brazil.identity.infrastructure.persistence.jpa.CompanyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Company Service.
 * Manages company/tenant lifecycle and operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyJpaRepository companyRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    public Company createCompany(Company company, String createdBy) {
        log.info("Creating company: {}", company.getCode());

        // Validate uniqueness
        if (companyRepository.existsByCodeAndDeletedFalse(company.getCode())) {
            throw new IllegalArgumentException("Company code already exists: " + company.getCode());
        }
        if (companyRepository.existsByCnpjAndDeletedFalse(company.getCnpj())) {
            throw new IllegalArgumentException("Company CNPJ already exists: " + company.getCnpj());
        }
        if (companyRepository.existsByEmailAndDeletedFalse(company.getEmail())) {
            throw new IllegalArgumentException("Company email already exists: " + company.getEmail());
        }

        // Set tenant context
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = UUID.randomUUID().toString();
        }
        
        company.setTenantId(tenantId);
        company.setCreatedBy(createdBy);
        
        Company saved = companyRepository.save(company);

        // Publish domain event
        CompanyCreatedEvent event = new CompanyCreatedEvent(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getTradeName(),
                saved.getCnpj(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getAddress(),
                saved.getCity(),
                saved.getState(),
                saved.getCep(),
                saved.getTenantId()
        );
        outboxEventPublisher.saveToOutbox(event);

        log.info("Company created: {} (ID: {})", saved.getCode(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Company getCompany(String id) {
        return companyRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Company> listCompanies(Pageable pageable) {
        return companyRepository.findByTenantId(TenantContext.getTenantId(), pageable);
    }

    public Company updateCompany(String id, Company updates, String updatedBy) {
        Company company = getCompany(id);
        
        // Update fields
        company.setName(updates.getName());
        company.setTradeName(updates.getTradeName());
        company.setStateRegistration(updates.getStateRegistration());
        company.setMunicipalRegistration(updates.getMunicipalRegistration());
        company.setAddress(updates.getAddress());
        company.setCity(updates.getCity());
        company.setState(updates.getState());
        company.setCep(updates.getCep());
        company.setPhone(updates.getPhone());
        company.setEmail(updates.getEmail());
        company.setWebsite(updates.getWebsite());
        company.setSettings(updates.getSettings());
        company.setLogoUrl(updates.getLogoUrl());
        company.setPrimaryColor(updates.getPrimaryColor());
        company.setSecondaryColor(updates.getSecondaryColor());
        company.setUpdatedBy(updatedBy);

        return companyRepository.save(company);
    }

    public void deleteCompany(String id, String deletedBy) {
        Company company = getCompany(id);
        company.softDelete(deletedBy);
        companyRepository.save(company);
        log.info("Company soft deleted: {}", id);
    }

    public Company activateCompany(String id, String updatedBy) {
        Company company = getCompany(id);
        company.activate();
        company.setUpdatedBy(updatedBy);
        return companyRepository.save(company);
    }

    public Company deactivateCompany(String id, String updatedBy) {
        Company company = getCompany(id);
        company.deactivate();
        company.setUpdatedBy(updatedBy);
        return companyRepository.save(company);
    }

    public Company suspendCompany(String id, String updatedBy) {
        Company company = getCompany(id);
        company.suspend();
        company.setUpdatedBy(updatedBy);
        return companyRepository.save(company);
    }
}