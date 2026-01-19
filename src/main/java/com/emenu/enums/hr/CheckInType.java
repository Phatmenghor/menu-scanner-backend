package com.emenu.enums.hr;

import lombok.Getter;

@Getter
public enum CheckInType {
    START("Clock In", "Employee clocks in to start work"),
    END("Clock Out", "Employee clocks out to end work");

    private final String displayName;
    private final String description;

    CheckInType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isStart() {
        return this == START;
    }

    public boolean isEnd() {
        return this == END;
    }
}