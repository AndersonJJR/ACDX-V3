package com.erp.brazil.hr.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.server-url:http://localhost:8080/api}")
    private String serverUrl;

    @Value("${app.openapi.title:HR Audit Service API}")
    private String title;

    @Value("${app.openapi.version:1.0.0}")
    private String version;

    @Value("${app.openapi.description:ERP Brazil - HR, Audit & Notifications Service}")
    private String description;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name("ACDX ERP Team")
                                .email("api-support@erp-brazil.com")
                                .url("https://erp-brazil.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://erp-brazil.com/license")))
                .servers(List.of(new Server().url(serverUrl).description("Development Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")));
    }
}