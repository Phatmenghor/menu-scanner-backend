package com.emenu.features.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionUsageService {

    private final SubscriptionService subscriptionService;

    public void trackStaffCreation(UUID businessId) {
        try {
            // This would be called when creating a new staff member
            // Implementation would count current staff and update subscription
            log.info("Tracking staff creation for business: {}", businessId);
            // subscriptionService.updateStaffUsage(businessId, newCount);
        } catch (Exception e) {
            log.error("Failed to track staff creation for business: {}", businessId, e);
        }
    }

    public void trackMenuItemCreation(UUID businessId) {
        try {
            log.info("Tracking menu item creation for business: {}", businessId);
            // subscriptionService.updateMenuItemUsage(businessId, newCount);
        } catch (Exception e) {
            log.error("Failed to track menu item creation for business: {}", businessId, e);
        }
    }

    public void trackTableCreation(UUID businessId) {
        try {
            log.info("Tracking table creation for business: {}", businessId);
            // subscriptionService.updateTableUsage(businessId, newCount);
        } catch (Exception e) {
            log.error("Failed to track table creation for business: {}", businessId, e);
        }
    }

    public boolean validateStaffLimit(UUID businessId) {
        return subscriptionService.canAddStaff(businessId);
    }

    public boolean validateMenuItemLimit(UUID businessId) {
        return subscriptionService.canAddMenuItem(businessId);
    }

    public boolean validateTableLimit(UUID businessId) {
        return subscriptionService.canAddTable(businessId);
    }
}