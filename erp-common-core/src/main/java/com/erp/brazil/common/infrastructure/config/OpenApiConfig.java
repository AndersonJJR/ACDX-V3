package com.erp.brazil.common.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Provides JWT Bearer authentication and multi-server support.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.title:ERP Brazil API}")
    private String title;

    @Value("${app.openapi.version:1.0.0}")
    private String version;

    @Value("${app.openapi.description:ERP Brazil Microservices API}")
    private String description;

    @Value("${app.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${app.openapi.prod-url:https://api.erp.brazil.com}")
    private String prodUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name("ERP Brazil Team")
                                .email("dev@erp.brazil.com")
                                .url("https://erp.brazil.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://erp.brazil.com/license")))
                .servers(List.of(
                        new Server().url(devUrl).description("Development Server"),
                        new Server().url(prodUrl).description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("X-Tenant-ID"));
    }

    @Bean
    public SpringDocConfiguration springDocConfiguration() {
        return new SpringDocConfiguration()
                .apiDocsPath("/v3/api-docs")
                .swaggerUiPath("/swagger-ui.html")
                .showActuator(true);
    }
}