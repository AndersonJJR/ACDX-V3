package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.TimeRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimeRecordDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    @NotNull Long employeeId,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
    @NotNull TimeRecord.RecordType recordType,
    @NotNull @JsonFormat(pattern = "HH:mm:ss") LocalTime recordTime,
    @NotNull TimeRecord.RecordSource recordSource,
    Double latitude,
    Double longitude,
    String deviceId,
    String ipAddress,
    boolean isManualAdjustment,
    String adjustmentReason
) {
    public static TimeRecordDTO from(TimeRecord record) {
        return new TimeRecordDTO(
            record.uuid(), record.employeeId(), record.recordDate(), record.recordType(),
            record.recordTime(), record.recordSource(), record.latitude(), record.longitude(),
            record.deviceId(), record.ipAddress(), record.isManualAdjustment(), record.adjustmentReason()
        );
    }
}