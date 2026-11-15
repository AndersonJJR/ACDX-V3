package com.erp.brazil.hr.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimeRecord(
    Long id,
    UUID uuid,
    Long employeeId,
    LocalDate recordDate,
    RecordType recordType,
    LocalTime recordTime,
    RecordSource recordSource,
    Double latitude,
    Double longitude,
    String deviceId,
    String ipAddress,
    boolean isManualAdjustment,
    String adjustmentReason,
    Long adjustedBy,
    java.time.Instant adjustedAt,
    RecordStatus status,
    Long approvedBy,
    java.time.Instant approvedAt
) {
    public enum RecordType {
        ENTRY, EXIT_LUNCH, RETURN_LUNCH, EXIT, OVERTIME_ENTRY, OVERTIME_EXIT, ABSENCE, MEDICAL_LEAVE, JUSTIFIED_ABSENCE
    }

    public enum RecordSource {
        BIOMETRIC, MOBILE_APP, WEB_PORTAL, MANUAL, IMPORT, GPS, FACE_RECOGNITION
    }

    public enum RecordStatus {
        PENDING, APPROVED, REJECTED, DISPUTED
    }

    public boolean isEntry() {
        return recordType == RecordType.ENTRY || recordType == RecordType.RETURN_LUNCH || recordType == RecordType.OVERTIME_ENTRY;
    }

    public boolean isExit() {
        return recordType == RecordType.EXIT_LUNCH || recordType == RecordType.EXIT || recordType == RecordType.OVERTIME_EXIT;
    }
}