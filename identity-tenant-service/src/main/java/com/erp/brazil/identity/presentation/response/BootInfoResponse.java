package com.erp.brazil.identity.presentation.response;

import com.erp.brazil.identity.domain.model.BootInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Boot Info response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootInfoResponse {

    private String appName;
    private String appVersion;
    private String buildNumber;
    private String apiBaseUrl;
    private String authBaseUrl;
    private String cdnBaseUrl;
    private String defaultLanguage;
    private String supportedLanguages;
    private String defaultTimezone;
    private String supportedCurrencies;
    private String features;
    private String modules;
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    private Integer sessionTimeoutMinutes;
    private Integer maxFileUploadMb;
    private String allowedFileTypes;

    public static BootInfoResponse from(BootInfo bootInfo) {
        if (bootInfo == null) {
            return null;
        }
        return BootInfoResponse.builder()
                .appName(bootInfo.getAppName())
                .appVersion(bootInfo.getAppVersion())
                .buildNumber(bootInfo.getBuildNumber())
                .apiBaseUrl(bootInfo.getApiBaseUrl())
                .authBaseUrl(bootInfo.getAuthBaseUrl())
                .cdnBaseUrl(bootInfo.getCdnBaseUrl())
                .defaultLanguage(bootInfo.getDefaultLanguage())
                .supportedLanguages(bootInfo.getSupportedLanguages())
                .defaultTimezone(bootInfo.getDefaultTimezone())
                .supportedCurrencies(bootInfo.getSupportedCurrencies())
                .features(bootInfo.getFeatures())
                .modules(bootInfo.getModules())
                .maintenanceMode(bootInfo.getMaintenanceMode())
                .maintenanceMessage(bootInfo.getMaintenanceMessage())
                .sessionTimeoutMinutes(bootInfo.getSessionTimeoutMinutes())
                .maxFileUploadMb(bootInfo.getMaxFileUploadMb())
                .allowedFileTypes(bootInfo.getAllowedFileTypes())
                .build();
    }

    public BootInfo toEntity() {
        BootInfo bootInfo = new BootInfo();
        bootInfo.setAppName(this.appName);
        bootInfo.setAppVersion(this.appVersion);
        bootInfo.setBuildNumber(this.buildNumber);
        bootInfo.setApiBaseUrl(this.apiBaseUrl);
        bootInfo.setAuthBaseUrl(this.authBaseUrl);
        bootInfo.setCdnBaseUrl(this.cdnBaseUrl);
        bootInfo.setDefaultLanguage(this.defaultLanguage);
        bootInfo.setSupportedLanguages(this.supportedLanguages);
        bootInfo.setDefaultTimezone(this.defaultTimezone);
        bootInfo.setSupportedCurrencies(this.supportedCurrencies);
        bootInfo.setFeatures(this.features);
        bootInfo.setModules(this.modules);
        bootInfo.setMaintenanceMode(this.maintenanceMode);
        bootInfo.setMaintenanceMessage(this.maintenanceMessage);
        bootInfo.setSessionTimeoutMinutes(this.sessionTimeoutMinutes);
        bootInfo.setMaxFileUploadMb(this.maxFileUploadMb);
        bootInfo.setAllowedFileTypes(this.allowedFileTypes);
        return bootInfo;
    }
}