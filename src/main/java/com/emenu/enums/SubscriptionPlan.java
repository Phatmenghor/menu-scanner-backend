package com.emenu.enums;

public enum SubscriptionPlan {
    FREE("Free Plan", 0.0, 1, 10, 2, 30),
    BASIC("Basic Plan", 29.99, 3, 50, 10, 90),
    PROFESSIONAL("Professional Plan", 79.99, 10, 200, 25, 365),
    ENTERPRISE("Enterprise Plan", 199.99, -1, -1, -1, 365);

    private final String displayName;
    private final double monthlyPrice;
    private final int maxStaffMembers; // -1 for unlimited
    private final int maxMenuItems; // -1 for unlimited
    private final int maxTables; // -1 for unlimited
    private final int defaultDurationDays;

    SubscriptionPlan(String displayName, double monthlyPrice, int maxStaffMembers, 
                    int maxMenuItems, int maxTables, int defaultDurationDays) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.maxStaffMembers = maxStaffMembers;
        this.maxMenuItems = maxMenuItems;
        this.maxTables = maxTables;
        this.defaultDurationDays = defaultDurationDays;
    }

    public String getDisplayName() { return displayName; }
    public double getMonthlyPrice() { return monthlyPrice; }
    public int getMaxStaffMembers() { return maxStaffMembers; }
    public int getMaxMenuItems() { return maxMenuItems; }
    public int getMaxTables() { return maxTables; }
    public int getDefaultDurationDays() { return defaultDurationDays; }

    public boolean isUnlimited(String feature) {
        return switch (feature.toLowerCase()) {
            case "staff" -> maxStaffMembers == -1;
            case "menu" -> maxMenuItems == -1;
            case "tables" -> maxTables == -1;
            default -> false;
        };
    }
}