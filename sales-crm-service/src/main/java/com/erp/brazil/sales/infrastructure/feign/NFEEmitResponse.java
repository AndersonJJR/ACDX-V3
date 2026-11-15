package com.erp.brazil.sales.infrastructure.feign;

import java.time.LocalDateTime;

public record NFEEmitResponse(
        String accessKey,
        String xmlPath,
        String xmlContent,
        byte[] pdfContent,
        LocalDateTime authorizedAt,
        String protocol,
        String status
) {}