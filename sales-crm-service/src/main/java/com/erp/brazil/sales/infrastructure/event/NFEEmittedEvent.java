package com.erp.brazil.sales.infrastructure.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record NFEEmittedEvent(
        UUID invoiceId,
        UUID companyId,
        String accessKey,
        String xmlPath,
        String xmlContent,
        byte[] pdfContent,
        LocalDateTime authorizedAt,
        String protocol
) {}