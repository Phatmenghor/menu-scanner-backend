package com.emenu.enumations;

public enum LicensePlan {
    TRIAL("Trial", 30, 2, 10, 5, 0.00),
    BASIC("Basic", 365, 5, 50, 20, 29.99),
    PROFESSIONAL("Professional", 365, 15, 200, 50, 79.99),
    ENTERPRISE("Enterprise", 365, -1, -1, -1, 199.99);

    private final String displayName;
    private final int defaultDurationDays;
    private final int maxStaffMembers;
    private final int maxMenuItems;
    private final int maxTables;
    private final double monthlyPrice;

    LicensePlan(String displayName, int defaultDurationDays, int maxStaffMembers, 
                int maxMenuItems, int maxTables, double monthlyPrice) {
        this.displayName = displayName;
        this.defaultDurationDays = defaultDurationDays;
        this.maxStaffMembers = maxStaffMembers;
        this.maxMenuItems = maxMenuItems;
        this.maxTables = maxTables;
        this.monthlyPrice = monthlyPrice;
    }

    public String getDisplayName() { return displayName; }
    public int getDefaultDurationDays() { return defaultDurationDays; }
    public int getMaxStaffMembers() { return maxStaffMembers; }
    public int getMaxMenuItems() { return maxMenuItems; }
    public int getMaxTables() { return maxTables; }
    public double getMonthlyPrice() { return monthlyPrice; }

    public boolean isUnlimited(String feature) {
        return switch (feature.toLowerCase()) {
            case "staff" -> maxStaffMembers == -1;
            case "menu" -> maxMenuItems == -1;
            case "tables" -> maxTables == -1;
            default -> false;
        };
    }
}