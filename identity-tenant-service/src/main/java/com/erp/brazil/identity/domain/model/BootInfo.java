package com.erp.brazil.identity.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter
import lombok.ToString;

import java.io.Serializable;

/**
 * BootInfo entity.
 * Stores system initialization and configuration information.
 * Used by frontend to initialize the application.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "boot_info")
public class BootInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 100, nullable = false)
    private String id = "system";

    @Column(name = "app_name", length = 100)
    private String appName = "ERP Brazil";

    @Column(name = "app_version", length = 50)
    private String appVersion = "1.0.0";

    @Column(name = "build_number", length = 50)
    private String buildNumber;

    @Column(name = "build_date")
    private java.time.OffsetDateTime buildDate;

    @Column(name = "api_base_url", length = 500)
    private String apiBaseUrl;

    @Column(name = "auth_base_url", length = 500)
    private String authBaseUrl;

    @Column(name = "cdn_base_url", length = 500)
    private String cdnBaseUrl;

    @Column(name = "default_language", length = 10)
    private String defaultLanguage = "pt-BR";

    @Column(name = "supported_languages", length = 500)
    private String supportedLanguages = "pt-BR,en-US,es-ES";

    @Column(name = "default_timezone", length = 50)
    private String defaultTimezone = "America/Sao_Paulo";

    @Column(name = "supported_currencies", length = 500)
    private String supportedCurrencies = "BRL,USD,EUR";

    @Column(name = "features", length = 2000)
    private String features; // JSON array of enabled features

    @Column(name = "modules", length = 2000)
    private String modules; // JSON array of available modules

    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode = false;

    @Column(name = "maintenance_message", length = 1000)
    private String maintenanceMessage;

    @Column(name = "session_timeout_minutes", nullable = false)
    private Integer sessionTimeoutMinutes = 480;

    @Column(name = "max_file_upload_mb", nullable = false)
    private Integer maxFileUploadMb = 50;

    @Column(name = "allowed_file_types", length = 500)
    private String allowedFileTypes = "pdf,xlsx,csv,xml,jpg,png";

    @Column(name = "updated_at")
    private java.time.OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public void updateTimestamp(String updatedBy) {
        this.updatedAt = java.time.OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }
}