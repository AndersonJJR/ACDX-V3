package com.erp.brazil.hr.domain.model;

public enum TimeRecordSource {
    BIOMETRIC("Biometria"),
    MOBILE_APP("App Mobile"),
    WEB_PORTAL("Portal Web"),
    MANUAL("Manual"),
    IMPORT("Importação"),
    GPS("GPS"),
    FACE_RECOGNITION("Reconhecimento Facial");

    private final String description;

    TimeRecordSource(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isAutomatic() {
        return this != MANUAL && this != IMPORT;
    }
}