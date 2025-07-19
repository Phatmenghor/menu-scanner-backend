package com.emenu.enums;

public enum CustomerTier {
    BRONZE("Bronze", 0, 100, 1.0, 0.0),
    SILVER("Silver", 100, 500, 1.05, 0.02),
    GOLD("Gold", 500, 1000, 1.1, 0.05),
    PLATINUM("Platinum", 1000, 5000, 1.15, 0.08),
    VIP("VIP", 5000, Integer.MAX_VALUE, 1.2, 0.10);

    private final String displayName;
    private final int minPoints;
    private final int maxPoints;
    private final double pointMultiplier;
    private final double discountPercentage;

    CustomerTier(String displayName, int minPoints, int maxPoints, 
                double pointMultiplier, double discountPercentage) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.pointMultiplier = pointMultiplier;
        this.discountPercentage = discountPercentage;
    }

    public String getDisplayName() { return displayName; }
    public int getMinPoints() { return minPoints; }
    public int getMaxPoints() { return maxPoints; }
    public double getPointMultiplier() { return pointMultiplier; }
    public double getDiscountPercentage() { return discountPercentage; }

    public static CustomerTier fromPoints(int points) {
        for (CustomerTier tier : values()) {
            if (points >= tier.minPoints && points < tier.maxPoints) {
                return tier;
            }
        }
        return VIP;
    }
}