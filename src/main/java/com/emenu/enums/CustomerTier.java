package com.emenu.enums;

public enum CustomerTier {
    BRONZE("Bronze", 0, 99, 1.0, 0.0),
    SILVER("Silver", 100, 499, 1.05, 2.0),
    GOLD("Gold", 500, 999, 1.1, 5.0),
    PLATINUM("Platinum", 1000, 4999, 1.15, 8.0),
    VIP("VIP", 5000, Integer.MAX_VALUE, 1.2, 10.0);

    private final String displayName;
    private final int minPoints;
    private final int maxPoints;
    private final double pointMultiplier;
    private final double discountPercentage;

    CustomerTier(String displayName, int minPoints, int maxPoints, double pointMultiplier, double discountPercentage) {
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

    public static CustomerTier getTierByPoints(int points) {
        for (CustomerTier tier : values()) {
            if (points >= tier.minPoints && points <= tier.maxPoints) {
                return tier;
            }
        }
        return BRONZE; // Default
    }

    public CustomerTier getNextTier() {
        CustomerTier[] tiers = values();
        int currentIndex = this.ordinal();
        if (currentIndex < tiers.length - 1) {
            return tiers[currentIndex + 1];
        }
        return this; // Already at highest tier
    }
}
