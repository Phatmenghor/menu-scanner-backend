package com.emenu.enums;

public enum SubscriptionPlan {
    FREE("Free Plan", 0.0, 1, 10, 2),
    BASIC("Basic Plan", 29.99, 3, 50, 10),
    PROFESSIONAL("Professional Plan", 79.99, 10, 200, 25),
    ENTERPRISE("Enterprise Plan", 199.99, -1, -1, -1);

    private final String displayName;
    private final double monthlyPrice;
    private final int maxStaffMembers;
    private final int maxMenuItems;
    private final int maxTables;

    SubscriptionPlan(String displayName, double monthlyPrice, int maxStaffMembers, 
                    int maxMenuItems, int maxTables) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.maxStaffMembers = maxStaffMembers;
        this.maxMenuItems = maxMenuItems;
        this.maxTables = maxTables;
    }

    public String getDisplayName() { return displayName; }
    public double getMonthlyPrice() { return monthlyPrice; }
    public int getMaxStaffMembers() { return maxStaffMembers; }
    public int getMaxMenuItems() { return maxMenuItems; }
    public int getMaxTables() { return maxTables; }
}