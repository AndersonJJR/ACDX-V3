package com.erp.brazil.manufacturing.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${spring.application.name:manufacturing-projects-service}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP Brazil - Manufacturing & Projects Service API")
                        .version("1.0.0")
                        .description("Manufacturing and Projects management API for ERP Brazil")
                        .license(new License()
                                .name("Proprietary")
                                .url("https://erp-brazil.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8082" + contextPath).description("Development Server"),
                        new Server().url("https://api.erp-brazil.com" + contextPath).description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token from Keycloak")));
    }
}