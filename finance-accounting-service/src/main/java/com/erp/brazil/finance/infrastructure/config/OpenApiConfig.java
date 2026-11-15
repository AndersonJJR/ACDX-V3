package com.erp.brazil.finance.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Accounting Service API")
                        .version("1.0.0")
                        .description("ERP Brazil - Finance, Accounting & Assets Service REST API")
                        .license(new License()
                                .name("Proprietary")
                                .url("https://erp-brazil.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.erp-brazil.com/finance").description("Production Server")
                ));
    }
}