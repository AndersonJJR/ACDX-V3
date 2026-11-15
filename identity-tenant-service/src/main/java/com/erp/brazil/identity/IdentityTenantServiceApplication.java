package com.erp.brazil.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Identity & Tenant Service Application Entry Point.
 * Manages companies, users, roles, permissions, branches, departments,
 * cost centers, numbering series, and authentication.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAware")
@EnableJpaRepositories(basePackages = "com.erp.brazil.identity.infrastructure.persistence.jpa")
@EnableMongoRepositories(basePackages = "com.erp.brazil.identity.infrastructure.persistence.mongo")
@EntityScan(basePackages = "com.erp.brazil.identity.domain.model")
@ComponentScan(basePackages = {"com.erp.brazil.identity", "com.erp.brazil.common"})
@EnableScheduling
public class IdentityTenantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityTenantServiceApplication.class, args);
    }
}