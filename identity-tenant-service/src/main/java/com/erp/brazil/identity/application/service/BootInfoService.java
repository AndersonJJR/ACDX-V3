package com.erp.brazil.identity.application.service;

import com.erp.brazil.identity.domain.model.BootInfo;
import com.erp.brazil.identity.infrastructure.persistence.jpa.BootInfoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Boot Info Service.
 * Manages system boot configuration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BootInfoService {

    private final BootInfoJpaRepository bootInfoRepository;

    @Transactional(readOnly = true)
    public BootInfo getBootInfo() {
        return bootInfoRepository.findById("system")
                .orElseGet(this::createDefaultBootInfo);
    }

    public BootInfo updateBootInfo(BootInfo bootInfo, String updatedBy) {
        BootInfo existing = bootInfoRepository.findById("system")
                .orElseGet(this::createDefaultBootInfo);

        existing.setAppName(bootInfo.getAppName());
        existing.setAppVersion(bootInfo.getAppVersion());
        existing.setBuildNumber(bootInfo.getBuildNumber());
        existing.setApiBaseUrl(bootInfo.getApiBaseUrl());
        existing.setAuthBaseUrl(bootInfo.getAuthBaseUrl());
        existing.setCdnBaseUrl(bootInfo.getCdnBaseUrl());
        existing.setDefaultLanguage(bootInfo.getDefaultLanguage());
        existing.setSupportedLanguages(bootInfo.getSupportedLanguages());
        existing.setDefaultTimezone(bootInfo.getDefaultTimezone());
        existing.setSupportedCurrencies(bootInfo.getSupportedCurrencies());
        existing.setFeatures(bootInfo.getFeatures());
        existing.setModules(bootInfo.getModules());
        existing.setMaintenanceMode(bootInfo.getMaintenanceMode());
        existing.setMaintenanceMessage(bootInfo.getMaintenanceMessage());
        existing.setSessionTimeoutMinutes(bootInfo.getSessionTimeoutMinutes());
        existing.setMaxFileUploadMb(bootInfo.getMaxFileUploadMb());
        existing.setAllowedFileTypes(bootInfo.getAllowedFileTypes());
        existing.updateTimestamp(updatedBy);

        return bootInfoRepository.save(existing);
    }

    private BootInfo createDefaultBootInfo() {
        BootInfo bootInfo = new BootInfo();
        bootInfo.setId("system");
        bootInfo.setAppName("ERP Brazil");
        bootInfo.setAppVersion("1.0.0");
        bootInfo.setBuildNumber("1");
        bootInfo.setApiBaseUrl("/api");
        bootInfo.setAuthBaseUrl("/api/auth");
        bootInfo.setDefaultLanguage("pt-BR");
        bootInfo.setSupportedLanguages("pt-BR,en-US,es-ES");
        bootInfo.setDefaultTimezone("America/Sao_Paulo");
        bootInfo.setSupportedCurrencies("BRL,USD,EUR");
        bootInfo.setFeatures("[\"identity\",\"finance\",\"fiscal\",\"hr\",\"inventory\",\"manufacturing\",\"sales\"]");
        bootInfo.setModules("[\"identity-tenant\",\"finance-accounting\",\"fiscal-brazil\",\"hr-audit\",\"inventory-supply\",\"manufacturing-projects\",\"sales-crm\"]");
        bootInfo.setMaintenanceMode(false);
        bootInfo.setSessionTimeoutMinutes(480);
        bootInfo.setMaxFileUploadMb(50);
        bootInfo.setAllowedFileTypes("pdf,xlsx,csv,xml,jpg,png");
        return bootInfoRepository.save(bootInfo);
    }
}