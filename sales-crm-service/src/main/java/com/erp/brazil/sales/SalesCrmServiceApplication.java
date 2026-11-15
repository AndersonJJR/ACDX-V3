package com.erp.brazil.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.erp.brazil.sales.infrastructure.feign")
@EnableScheduling
public class SalesCrmServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesCrmServiceApplication.class, args);
	}

}