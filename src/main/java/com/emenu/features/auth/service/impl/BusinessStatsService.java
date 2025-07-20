package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.response.BusinessStatsResponse;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.messaging.repository.MessageRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BusinessStatsService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SubscriptionRepository subscriptionRepository;

    public BusinessStatsResponse getBusinessStats(UUID businessId) {
        BusinessStatsResponse stats = new BusinessStatsResponse();

        // Staff statistics
        long totalStaff = userRepository.countByBusinessIdAndIsDeletedFalse(businessId);
        stats.setTotalStaff((int) totalStaff);
        stats.setActiveStaff((int) totalStaff); // Simplified - could filter by active status

        // Customer statistics (simplified - would need business-customer relationship)
        stats.setTotalCustomers(0);

        // Message statistics
        long totalMessages = messageRepository.countByIsDeletedFalse();
        long unreadMessages = messageRepository.countUnreadByBusinessIdAndIsDeletedFalse(businessId);
        stats.setTotalMessages((int) totalMessages);
        stats.setUnreadMessages((int) unreadMessages);

        // Current month statistics
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long messagesThisMonth = messageRepository.countByCreatedAtAfterAndIsDeletedFalse(startOfMonth);
        stats.setMessagesThisMonth((int) messagesThisMonth);
        stats.setNewCustomersThisMonth(0); // Would need proper tracking

        // Subscription information
        subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .ifPresentOrElse(subscription -> {
                    stats.setCurrentPlan(subscription.getPlan().getDisplayName());
                    stats.setSubscriptionActive(subscription.isActive());
                    if (subscription.getEndDate() != null) {
                        stats.setSubscriptionEndDate(subscription.getEndDate().toLocalDate().toString());
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                                LocalDateTime.now(), subscription.getEndDate());
                        stats.setDaysRemaining((int) Math.max(0, daysRemaining));
                    }
                }, () -> {
                    stats.setCurrentPlan("No Subscription");
                    stats.setSubscriptionActive(false);
                    stats.setDaysRemaining(0);
                });

        return stats;
    }
}