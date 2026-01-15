package com.emenu.enums.hr;

import lombok.Getter;

@Getter
public enum AttendanceStatusEnum {
    PRESENT("Present", "Employee attended work"),
    ABSENT("Absent", "Employee absent from work"),
    LATE("Late", "Employee arrived late"),
    HALF_DAY("Half Day", "Employee attended half day only");

    private final String displayName;
    private final String description;

    AttendanceStatusEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isPresent() {
        return this == PRESENT;
    }

    public boolean isAbsent() {
        return this == ABSENT;
    }

    public boolean isLate() {
        return this == LATE;
    }

    public boolean isHalfDay() {
        return this == HALF_DAY;
    }
}


