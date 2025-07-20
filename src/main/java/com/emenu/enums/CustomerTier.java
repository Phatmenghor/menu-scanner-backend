package com.emenu.enums;

public enum CustomerTier {
    CUSTOMER("Customer");

    private final String displayName;

    CustomerTier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { 
        return displayName; 
    }
}
