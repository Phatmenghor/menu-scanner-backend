package com.emenu.features.services.service;

import java.util.UUID;

public interface UsageTrackingService {
    void trackUserCreation(UUID businessId);
    void trackMenuCreation(UUID businessId);
    void trackOrderProcessing(UUID businessId);
    boolean checkUserLimit(UUID businessId);
    boolean checkMenuLimit(UUID businessId);
    boolean checkOrderLimit(UUID businessId);
    void resetMonthlyUsage(UUID businessId);
}