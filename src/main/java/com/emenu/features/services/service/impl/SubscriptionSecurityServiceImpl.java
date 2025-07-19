package com.emenu.features.services.service.impl;

import com.emenu.features.services.repository.UserSubscriptionRepository;
import com.emenu.features.services.service.SubscriptionSecurityService;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionSecurityServiceImpl implements SubscriptionSecurityService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SecurityUtils securityUtils;

    @Override
    public boolean canAccessSubscription(UUID subscriptionId) {
        if (securityUtils.isPlatformAdmin()) {
            return true;
        }

        return subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .map(subscription -> securityUtils.isCurrentUser(subscription.getUserId()))
                .orElse(false);
    }

    @Override
    public boolean isSubscriptionOwner(UUID subscriptionId) {
        return subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .map(subscription -> securityUtils.isCurrentUser(subscription.getUserId()))
                .orElse(false);
    }
}