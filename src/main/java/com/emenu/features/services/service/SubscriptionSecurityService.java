package com.emenu.features.services.service;

import java.util.UUID;

public interface SubscriptionSecurityService {
    boolean canAccessSubscription(UUID subscriptionId);
    boolean isSubscriptionOwner(UUID subscriptionId);
}