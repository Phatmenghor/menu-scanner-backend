package com.emenu.enums;

public enum SubscriptionPlan {
    FREE("Free Plan", 0.0, 1, 10, 2, 30, false, false, false),
    BASIC("Basic Plan", 29.99, 3, 50, 10, 90, true, false, false),
    PROFESSIONAL("Professional Plan", 79.99, 10, 200, 25, 365, true, true, false),
    ENTERPRISE("Enterprise Plan", 199.99, -1, -1, -1, 365, true, true, true),
    CUSTOM("Custom Plan", 0.0, -1, -1, -1, 365, true, true, true);

    private final String displayName;
    private final double monthlyPrice;
    private final int maxStaffMembers; // -1 for unlimited
    private final int maxMenuItems; // -1 for unlimited
    private final int maxTables; // -1 for unlimited
    private final int defaultDurationDays;
    private final boolean hasAnalytics;
    private final boolean hasCustomization;
    private final boolean hasPrioritySupport;

    SubscriptionPlan(String displayName, double monthlyPrice, int maxStaffMembers, 
                    int maxMenuItems, int maxTables, int defaultDurationDays,
                    boolean hasAnalytics, boolean hasCustomization, boolean hasPrioritySupport) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.maxStaffMembers = maxStaffMembers;
        this.maxMenuItems = maxMenuItems;
        this.maxTables = maxTables;
        this.defaultDurationDays = defaultDurationDays;
        this.hasAnalytics = hasAnalytics;
        this.hasCustomization = hasCustomization;
        this.hasPrioritySupport = hasPrioritySupport;
    }

    public String getDisplayName() { return displayName; }
    public double getMonthlyPrice() { return monthlyPrice; }
    public int getMaxStaffMembers() { return maxStaffMembers; }
    public int getMaxMenuItems() { return maxMenuItems; }
    public int getMaxTables() { return maxTables; }
    public int getDefaultDurationDays() { return defaultDurationDays; }
    public boolean hasAnalytics() { return hasAnalytics; }
    public boolean hasCustomization() { return hasCustomization; }
    public boolean hasPrioritySupport() { return hasPrioritySupport; }

    public boolean isUnlimited(String feature) {
        return switch (feature.toLowerCase()) {
            case "staff" -> maxStaffMembers == -1;
            case "menu" -> maxMenuItems == -1;
            case "tables" -> maxTables == -1;
            default -> false;
        };
    }
}