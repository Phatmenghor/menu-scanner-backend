package com.emenu.enums;

import lombok.Getter;

@Getter
public enum MessageStatus {
    DRAFT("Draft"),
    SENT("Sent"),
    DELIVERED("Delivered"),
    READ("Read"),
    REPLIED("Replied"),
    FAILED("Failed");

    private final String description;

    MessageStatus(String description) {
        this.description = description;
    }
}