package com.erp.brazil.fiscal.infrastructure.sefaz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "sefaz")
@Getter
@Setter
public class SEFAZConfig {

    private Map<String, UFConfig> ufs = new HashMap<>();

    private String defaultEnvironment = "1";

    private int connectTimeout = 30000;

    private int readTimeout = 60000;

    private boolean sslValidation = true;

    @Getter
    @Setter
    public static class UFConfig {
        private String emissionUrl;
        private String consultUrl;
        private String cancellationUrl;
        private String inutilizationUrl;
        private String statusUrl;
        private String homologationEmissionUrl;
        private String homologationConsultUrl;
        private String homologationCancellationUrl;
        private String homologationInutilizationUrl;
    }

    public String getEmissionUrl(String uf, String environment) {
        UFConfig config = ufs.get(uf.toUpperCase());
        if (config == null) {
            throw new IllegalArgumentException("UF not configured: " + uf);
        }
        return "2".equals(environment) ? config.getHomologationEmissionUrl() : config.getEmissionUrl();
    }

    public String getConsultUrl(String uf, String environment) {
        UFConfig config = ufs.get(uf.toUpperCase());
        if (config == null) {
            throw new IllegalArgumentException("UF not configured: " + uf);
        }
        return "2".equals(environment) ? config.getHomologationConsultUrl() : config.getConsultUrl();
    }

    public String getCancellationUrl(String uf, String environment) {
        UFConfig config = ufs.get(uf.toUpperCase());
        if (config == null) {
            throw new IllegalArgumentException("UF not configured: " + uf);
        }
        return "2".equals(environment) ? config.getHomologationCancellationUrl() : config.getCancellationUrl();
    }

    public String getInutilizationUrl(String uf, String environment) {
        UFConfig config = ufs.get(uf.toUpperCase());
        if (config == null) {
            throw new IllegalArgumentException("UF not configured: " + uf);
        }
        return "2".equals(environment) ? config.getHomologationInutilizationUrl() : config.getInutilizationUrl();
    }
}