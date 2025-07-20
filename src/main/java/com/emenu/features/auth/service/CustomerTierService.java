package com.emenu.features.auth.service;

import com.emenu.enums.CustomerTier;
import com.emenu.features.auth.models.User;

public interface CustomerTierService {
    void updateCustomerTier(User customer);
    void addLoyaltyPoints(User customer, int points);
    CustomerTier calculateTierByPoints(int points);
    void sendTierUpgradeNotification(User customer, CustomerTier oldTier, CustomerTier newTier);
}