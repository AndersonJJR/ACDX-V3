package com.erp.brazil.sales.presentation.dto.request;

import java.time.LocalDateTime;

public record NFEEmittedResponse(
        String accessKey,
        String xmlPath,
        String xmlContent,
        byte[] pdfContent,
        LocalDateTime authorizedAt
) {}