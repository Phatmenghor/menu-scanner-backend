package com.emenu.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
    FREE("Free Plan", 0.00, 30, 1, 10, 2),
    BASIC("Basic Plan", 29.99, 30, 3, 50, 10),
    PROFESSIONAL("Professional Plan", 79.99, 30, 10, 200, 25),
    ENTERPRISE("Enterprise Plan", 199.99, 30, -1, -1, -1); // -1 = unlimited

    private final String displayName;
    private final double price;
    private final int durationDays;
    private final int maxStaff;
    private final int maxMenuItems;
    private final int maxTables;

    SubscriptionPlan(String displayName, double price, int durationDays, int maxStaff, int maxMenuItems, int maxTables) {
        this.displayName = displayName;
        this.price = price;
        this.durationDays = durationDays;
        this.maxStaff = maxStaff;
        this.maxMenuItems = maxMenuItems;
        this.maxTables = maxTables;
    }

    public boolean isUnlimited(String feature) {
        return switch (feature.toLowerCase()) {
            case "staff" -> maxStaff == -1;
            case "menu" -> maxMenuItems == -1;
            case "tables" -> maxTables == -1;
            default -> false;
        };
    }
}