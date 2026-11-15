package com.erp.brazil.sales.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fiscal-brazil-service", url = "${fiscal.service.url:http://localhost:8083}", path = "/api/v1/nfe")
public interface FiscalServiceClient {

    @PostMapping("/emit")
    NFEEmitResponse emitNFE(@RequestBody NFEEmitRequest request);
}