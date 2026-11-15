package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.TimeRecordStatus;
import com.erp.brazil.hr.domain.model.TimeRecordSource;
import com.erp.brazil.hr.domain.model.TimeRecordType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeRecordDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private LocalDate recordDate;
        private TimeRecordType recordType;
        private LocalTime recordTime;
        private TimeRecordSource recordSource;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String deviceId;
        private String ipAddress;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManualAdjustmentRequest {
        private LocalDate recordDate;
        private TimeRecordType recordType;
        private LocalTime recordTime;
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String uuid;
        private Long employeeId;
        private String employeeName;
        private LocalDate recordDate;
        private TimeRecordType recordType;
        private String recordTypeDescription;
        private LocalTime recordTime;
        private TimeRecordSource recordSource;
        private String recordSourceDescription;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String deviceId;
        private String ipAddress;
        private Boolean isManualAdjustment;
        private String adjustmentReason;
        private TimeRecordStatus status;
        private String statusDescription;
        private java.time.Instant approvedAt;
        private java.time.Instant adjustedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<Response> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySummary {
        private LocalDate date;
        private LocalTime entry;
        private LocalTime exitLunch;
        private LocalTime returnLunch;
        private LocalTime exit;
        private BigDecimal workedHours;
        private BigDecimal overtimeHours;
        private boolean complete;
        private List<String> missingRecords;
    }
}