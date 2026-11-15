package com.erp.brazil.hr.domain.model;

public enum WorkJourney {
    FULL_TIME("Tempo Integral", 44.0, 8.0),
    PART_TIME_MORNING("Meio Período Manhã", 22.0, 4.0),
    PART_TIME_AFTERNOON("Meio Período Tarde", 22.0, 4.0),
    PART_TIME_NIGHT("Meio Período Noite", 22.0, 4.0),
    SHIFT_12X36("Escala 12x36", 36.0, 12.0),
    SHIFT_6X1("Escala 6x1", 44.0, 7.33),
    CUSTOM("Personalizada", 0.0, 0.0);

    private final String description;
    private final double weeklyHours;
    private final double dailyHours;

    WorkJourney(String description, double weeklyHours, double dailyHours) {
        this.description = description;
        this.weeklyHours = weeklyHours;
        this.dailyHours = dailyHours;
    }

    public String getDescription() { return description; }
    public double getWeeklyHours() { return weeklyHours; }
    public double getDailyHours() { return dailyHours; }
    public boolean isStandard() { return this == FULL_TIME; }
    public boolean isShift() { return this == SHIFT_12X36 || this == SHIFT_6X1; }
}