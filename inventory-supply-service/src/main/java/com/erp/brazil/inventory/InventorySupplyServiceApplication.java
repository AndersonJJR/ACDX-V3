package com.erp.brazil.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EntityScan(basePackages = "com.erp.brazil.inventory.domain.model")
@EnableJpaRepositories(basePackages = "com.erp.brazil.inventory.domain.repository")
@EnableMongoRepositories(basePackages = "com.erp.brazil.inventory.domain.repository.mongo")
@ComponentScan(basePackages = "com.erp.brazil.inventory")
public class InventorySupplyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventorySupplyServiceApplication.class, args);
    }
}